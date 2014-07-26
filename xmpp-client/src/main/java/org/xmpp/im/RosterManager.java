/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp.im;

import org.xmpp.*;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.ServiceUnavailable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages the roster (aka contact or buddy list).
 * <p>
 * Contacts are often organized in groups, which may contain nested groups by themselves.
 * This manager takes care for sorting the the contacts in the right group and also creates the group hierarchy for nested groups.
 * </p>
 * <p>Nested groups are determined by a delimiter, which must be {@linkplain #setGroupDelimiter(String) set} prior to requesting the roster.</p>
 * <p>
 * By adding a {@link RosterListener}, you can listen for roster updates (aka roster pushes) in order to update the view.
 * </p>
 *
 * @author Christian Schudt
 */
public final class RosterManager {
    private static final Logger logger = Logger.getLogger(RosterManager.class.getName());

    private final Map<Jid, Contact> contactMap = new ConcurrentHashMap<>();

    private final Set<RosterListener> rosterListeners = new CopyOnWriteArraySet<>();

    private final XmppSession xmppSession;

    private final Set<ContactGroup> groups = new TreeSet<>();

    private final Set<Contact> unaffiliatedContacts = new TreeSet<>();

    private final Map<String, ContactGroup> rosterGroupMap = new HashMap<>();

    private boolean retrieveRosterOnLogin = true;

    private String groupDelimiter = null;

    public RosterManager(final XmppSession xmppSession) {
        this.xmppSession = xmppSession;
        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming() && !e.isConsumed()) {
                    IQ iq = e.getIQ();
                    Roster roster = iq.getExtension(Roster.class);
                    if (roster != null) {
                        // 2.1.6.  Roster Push
                        if (iq.getType() == IQ.Type.SET) {
                            // A receiving client MUST ignore the stanza unless it has no 'from' attribute (i.e., implicitly from the bare JID of the user's account) or it has a 'from' attribute whose value matches the user's bare JID <user@domainpart>.
                            if (iq.getFrom() == null || iq.getFrom().equals(xmppSession.getConnectedResource().asBareJid())) {
                                // Gracefully send an empty result.
                                xmppSession.send(iq.createResult());
                                updateRoster(roster, true);
                            } else {
                                // If the client receives a roster push from an unauthorized entity, it MUST NOT process the pushed data; in addition, the client can either return a stanza error of <service-unavailable/> error
                                xmppSession.send(iq.createError(new StanzaError(new ServiceUnavailable())));
                            }
                        } else if (iq.getType() == IQ.Type.RESULT) {
                            updateRoster(roster, false);
                        }
                        e.consume();
                    }
                }
            }
        });
        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    rosterListeners.clear();
                }
            }
        });
    }

    /**
     * Gets the contacts.
     *
     * @return The contacts.
     */
    public Collection<Contact> getContacts() {
        return Collections.unmodifiableCollection(contactMap.values());
    }

    /**
     * Gets a contact by its JID.
     *
     * @param jid The JID.
     * @return The contact or null, if it does not exist.
     */
    public Contact getContact(Jid jid) {
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null");
        }
        return contactMap.get(jid.asBareJid());
    }

    void updateRoster(Roster roster, boolean isRosterPush) {
        List<Contact> addedContacts = new ArrayList<>();
        List<Contact> updatedContacts = new ArrayList<>();
        List<Contact> removedContacts = new ArrayList<>();
        Collections.sort(roster.getContacts());
        synchronized (this) {
            if (!isRosterPush) {
                rosterGroupMap.clear();
                contactMap.clear();
            }

            // Loop through the new roster and compare it with the old one.
            for (Contact contact : roster.getContacts()) {
                Contact oldContact = contactMap.get(contact.getJid());
                if (contact.getSubscription() == Contact.Subscription.REMOVE) {
                    contactMap.remove(contact.getJid());
                    removedContacts.add(contact);
                } else if (oldContact != null && !oldContact.equals(contact)) {
                    contactMap.put(contact.getJid(), contact);
                    updatedContacts.add(contact);
                } else if (oldContact == null) {
                    contactMap.put(contact.getJid(), contact);
                    addedContacts.add(contact);
                }

                if (contact.getSubscription() != Contact.Subscription.REMOVE) {

                    for (String group : contact.getGroups()) {
                        String[] nestedGroups;
                        if (groupDelimiter != null) {
                            nestedGroups = group.split(groupDelimiter);
                        } else {
                            nestedGroups = new String[]{group};
                        }

                        String currentGroupName = "";
                        ContactGroup currentGroup = null;
                        for (int i = 0; i < nestedGroups.length; i++) {
                            String nestedGroupName = nestedGroups[i];
                            currentGroupName += nestedGroupName;
                            ContactGroup nestedGroup = rosterGroupMap.get(currentGroupName);
                            if (nestedGroup == null) {
                                nestedGroup = new ContactGroup(nestedGroupName, currentGroupName, currentGroup);
                                rosterGroupMap.put(currentGroupName, nestedGroup);
                                // Only add top level groups.
                                if (i == 0) {
                                    groups.add(nestedGroup);
                                }
                                if (currentGroup != null) {
                                    currentGroup.getGroups().add(nestedGroup);
                                }
                            }

                            currentGroup = nestedGroup;
                            if (i < nestedGroups.length - 1) {
                                currentGroupName += groupDelimiter;
                            }
                        }
                        if (currentGroup != null) {
                            removeContactByJid(contact, currentGroup.getContacts());
                            currentGroup.getContacts().add(contact);
                        }
                    }
                }
                removeContactByJid(contact, unaffiliatedContacts);
                // Add the contact to the list of unaffiliated contacts, if it has no groups and it hasn't been removed from the roster.
                if (contact.getGroups().isEmpty() && contact.getSubscription() != Contact.Subscription.REMOVE) {
                    unaffiliatedContacts.add(contact);
                }
                removeContactsFromGroups(contact, groups);
            }
        }
        notifyRosterListeners(new RosterEvent(this, addedContacts, updatedContacts, removedContacts));
    }

    private void removeContactByJid(Contact contact, Collection<Contact> contacts) {
        for (Contact c : contacts) {
            if (c.getJid().equals(contact.getJid())) {
                contacts.remove(c);
                break;
            }
        }
    }

    /**
     * Recursively removes the contact from the groups and subsequently removes all empty groups.
     *
     * @param contact       The contact.
     * @param contactGroups The contact groups.
     */
    private void removeContactsFromGroups(Contact contact, Collection<ContactGroup> contactGroups) {
        List<ContactGroup> emptyGroups = new ArrayList<>();
        for (ContactGroup group : contactGroups) {
            // Recursively remove the contact from the nested subgroups.
            // If the nested group is empty, it can be removed.
            if (removeRecursively(contact, group)) {
                emptyGroups.add(group);
                rosterGroupMap.remove(group.getFullName());
            }
        }
        // Remove all empty sub groups
        contactGroups.removeAll(emptyGroups);
    }

    private boolean removeRecursively(final Contact contact, final ContactGroup contactGroup) {
        // Recursively remove the contacts from nested groups.
        removeContactsFromGroups(contact, contactGroup.getGroups());

        // Check, if the contact still exists in the group.
        boolean contactExistsInGroup = false;
        for (Contact c : contactGroup.getContacts()) {
            if (c.getJid().equals(contact.getJid())) {
                for (String groupName : contact.getGroups()) {
                    if (groupName.equals(contactGroup.getFullName())) {
                        contactExistsInGroup = true;
                        break;
                    }
                }
                break;
            }
        }

        //  If the contact does not exist or was removed, remove it.
        if (!contactExistsInGroup || contact.getSubscription() == Contact.Subscription.REMOVE) {
            removeContactByJid(contact, contactGroup.getContacts());
        }
        // Return, if the group is empty, so that the parent group can remove it.
        return contactGroup.getContacts().isEmpty() && contactGroup.getGroups().isEmpty();
    }

    /**
     * Gets the contact groups.
     *
     * @return The contact groups.
     */
    public synchronized Collection<ContactGroup> getContactGroups() {
        return Collections.unmodifiableCollection(groups);
    }

    /**
     * Gets the contacts, which are not affiliated to any group.
     *
     * @return The contacts, which are not affiliated to any group.
     */
    public synchronized Collection<Contact> getUnaffiliatedContacts() {
        return Collections.unmodifiableCollection(unaffiliatedContacts);
    }

    /**
     * Adds a roster listener, which will get notified, whenever the roster changes.
     *
     * @param rosterListener The roster listener.
     * @see #removeRosterListener(RosterListener)
     */
    public void addRosterListener(RosterListener rosterListener) {
        rosterListeners.add(rosterListener);
    }

    /**
     * Removes a previously added roster listener.
     *
     * @param rosterListener The roster listener.
     * @see #addRosterListener(RosterListener)
     */
    public void removeRosterListener(RosterListener rosterListener) {
        rosterListeners.remove(rosterListener);
    }

    private void notifyRosterListeners(RosterEvent rosterEvent) {
        for (RosterListener rosterListener : rosterListeners) {
            try {
                rosterListener.rosterChanged(rosterEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Controls, whether the roster is automatically retrieved as soon as the user has logged in.
     *
     * @return True, if the roster is automatically retrieved after login.
     * @see #setRetrieveRosterOnLogin(boolean)
     */
    public boolean isRetrieveRosterOnLogin() {
        return this.retrieveRosterOnLogin;
    }

    /**
     * Controls, whether the roster is automatically retrieved as soon as the user has logged in.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#roster-login">2.2.  Retrieving the Roster on Login</a></cite></p>
     * <p>Upon authenticating with a server and binding a resource (thus becoming a connected resource as defined in [XMPP-CORE]), a client SHOULD request the roster before sending initial presence (however, because receiving the roster is not necessarily desirable for all resources, e.g., a connection with limited bandwidth, the client's request for the roster is not mandatory).</p>
     * </blockquote>
     *
     * @param retrieveRosterOnLogin True, if the roster is automatically retrieved after login.
     */
    public void setRetrieveRosterOnLogin(boolean retrieveRosterOnLogin) {
        this.retrieveRosterOnLogin = retrieveRosterOnLogin;
    }

    /**
     * Requests the roster from the server. When the server returns the result, the {@link RosterListener} are notified.
     * That means, you should first {@linkplain #addRosterListener(RosterListener) register} a {@link RosterListener} prior to calling this method.
     */
    public void requestRoster() {
        IQ iq = new IQ(IQ.Type.GET);
        iq.setExtension(new Roster());
        this.xmppSession.send(iq);
    }

    /**
     * Adds a contact to the roster and optionally also sends a subscription request to it.
     *
     * @param contact             The contact.
     * @param requestSubscription If true, the contact is also sent a subscription request.
     * @param status              The optional status text, which is sent together with a subscription request. May be null.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void addContact(Contact contact, boolean requestSubscription, String status) throws XmppException {
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null.");
        }
        Roster roster = new Roster();
        roster.getContacts().add(contact);
        xmppSession.query(new IQ(IQ.Type.SET, roster));
        if (requestSubscription) {
            xmppSession.getPresenceManager().requestSubscription(contact.getJid(), status);
        }
    }

    /**
     * Updates a contact in the roster.
     *
     * @param contact The contact to update.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void updateContact(Contact contact) throws XmppException {
        addContact(contact, false, null);
    }

    /**
     * Removes a contact from the roster.
     *
     * @param jid The contact's JID.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void removeContact(Jid jid) throws XmppException {
        Roster roster = new Roster();
        Contact contact = new Contact(jid);
        contact.setSubscription(Contact.Subscription.REMOVE);
        roster.getContacts().add(contact);
        xmppSession.query(new IQ(IQ.Type.SET, roster));
    }

    /**
     * Renames a contact group.
     *
     * @param contactGroup The contact group.
     * @param name         The new name.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public synchronized void renameContactGroup(ContactGroup contactGroup, String name) throws XmppException {
        // Make this method synchronized so that roster pushes (which will occur during this method) don't mess up with this logic here (because the ContactGroup objects are reused and modified).
        int depth = -1;
        // Determine the depth of this group.
        ContactGroup parentGroup = contactGroup;
        do {
            parentGroup = parentGroup.getParentGroup();
            depth++;
        } while (parentGroup != null);
        replaceGroupName(contactGroup, name, depth);
    }

    /**
     * If the group delimiter is still set, it replaces the group name at the index.
     *
     * @param contactGroup The contact group.
     * @param name         The new group name,
     * @param index        The index of the group name.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    private void replaceGroupName(ContactGroup contactGroup, String name, int index) throws XmppException {
        // Update each contact in this group with the new group name.
        String newName = name;
        if (groupDelimiter != null) {
            String[] groups = contactGroup.getFullName().split(groupDelimiter);
            if (index < groups.length) {
                groups[index] = name;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < groups.length - 1; i++) {
                sb.append(groups[i]);
                sb.append(groupDelimiter);
            }
            sb.append(groups[groups.length - 1]);
            newName = sb.toString();
        }

        for (Contact contact : contactGroup.getContacts()) {
            List<String> newGroups = new ArrayList<>(contact.getGroups());
            newGroups.remove(contactGroup.getFullName());
            newGroups.add(newName);
            // Only do a roster update, if the groups have really changed.
            if (!contact.getGroups().equals(newGroups)) {
                updateContact(new Contact(contact.getJid(), contact.getName(), newGroups));
            }
        }
        for (ContactGroup subGroup : contactGroup.getGroups()) {
            replaceGroupName(subGroup, name, index);
        }
    }

    /**
     * Removes a contact group. If the group has sub groups, all sub groups are removed as well.
     * All contacts in this group and all sub groups are moved to the parent group (if present) or to no group at all.
     *
     * @param contactGroup The contact group.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public synchronized void removeContactGroup(ContactGroup contactGroup) throws XmppException {
        Collection<Contact> allContacts = collectAllContactsInGroup(contactGroup);
        if (contactGroup.getParentGroup() != null) {
            for (Contact contact : allContacts) {
                updateContact(new Contact(contact.getJid(), contact.getName(), contactGroup.getParentGroup().getFullName()));
            }
        } else {
            for (Contact contact : allContacts) {
                updateContact(new Contact(contact.getJid(), contact.getName()));
            }
        }
    }

    /**
     * Recursively collects all contacts in all (sub-) groups of a group.
     *
     * @param contactGroup The contact group.
     * @return All contacts.
     */
    private static Collection<Contact> collectAllContactsInGroup(ContactGroup contactGroup) {
        Collection<Contact> contacts = new ArrayList<>();
        // First, add all contact from this group.
        for (Contact contact : contactGroup.getContacts()) {
            addContactIfNotExists(contact, contacts);
        }
        // Then add all contacts from the subgroups
        Collection<Contact> contactsInSubGroups = new ArrayList<>();
        for (ContactGroup subGroup : contactGroup.getGroups()) {
            contactsInSubGroups.addAll(collectAllContactsInGroup(subGroup));
        }
        for (Contact contact : contactsInSubGroups) {
            addContactIfNotExists(contact, contacts);
        }
        return contacts;
    }

    /**
     * Adds a contact to the list, if its JID isn't contained in the list.
     *
     * @param contact  The contact.
     * @param contacts The contacts.
     */
    private static void addContactIfNotExists(Contact contact, Collection<Contact> contacts) {
        boolean contactExists = false;
        for (Contact c : contacts) {
            if (c.getJid().equals(contact.getJid())) {
                contactExists = true;
            }
        }
        if (!contactExists) {
            contacts.add(contact);
        }
    }

    /**
     * Gets the group delimiter.
     *
     * @return The group delimiter.
     * @see #setGroupDelimiter(String)
     */
    public synchronized String getGroupDelimiter() {
        return groupDelimiter;
    }

    /**
     * Sets the group delimiter.
     * <p>
     * If this is set to a non-null value, contact groups are split by the specified delimiter in order to build a nested hierarchy of groups.
     * </p>
     *
     * @param groupDelimiter The group delimiter.
     * @see <a href="http://xmpp.org/extensions/xep-0083.html">XEP-0083: Nested Roster Groups</a>
     */
    public synchronized void setGroupDelimiter(String groupDelimiter) {
        this.groupDelimiter = groupDelimiter;
    }
}
