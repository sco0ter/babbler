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
import org.xmpp.stanza.*;
import org.xmpp.stanza.errors.ServiceUnavailable;
import org.xmpp.stanza.errors.UnexpectedRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the roster.
 *
 * @author Christian Schudt
 */
public final class RosterManager {
    private static final Logger logger = Logger.getLogger(RosterManager.class.getName());

    private final Map<Jid, Contact> contactMap = new ConcurrentHashMap<>();

    private final Set<RosterListener> rosterListeners = new CopyOnWriteArraySet<>();

    private final Connection connection;

    private final List<ContactGroup> groups = new CopyOnWriteArrayList<>();

    private final List<Contact> unaffiliatedContacts = new ArrayList<>();

    private final Map<String, ContactGroup> rosterGroupMap = new ConcurrentHashMap<>();

    private boolean retrieveRosterOnLogin = true;

    private String groupDelimiter = null;

    public RosterManager(final Connection connection) {
        this.connection = connection;
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    Roster roster = iq.getExtension(Roster.class);
                    if (roster != null) {
                        // 2.1.6.  Roster Push
                        if (iq.getType() == IQ.Type.SET) {
                            // A receiving client MUST ignore the stanza unless it has no 'from' attribute (i.e., implicitly from the bare JID of the user's account) or it has a 'from' attribute whose value matches the user's bare JID <user@domainpart>.
                            if (iq.getFrom() == null || iq.getFrom().equals(connection.getConnectedResource().asBareJid())) {
                                // Gracefully send an empty result.
                                connection.send(iq.createResult());
                                updateRoster(roster, true);
                            } else {
                                // If the client receives a roster push from an unauthorized entity, it MUST NOT process the pushed data; in addition, the client can either return a stanza error of <service-unavailable/> error
                                connection.send(iq.createError(new StanzaError(new ServiceUnavailable())));
                            }
                        } else if (iq.getType() == IQ.Type.GET) {
                            connection.send(iq.createError(new StanzaError(new UnexpectedRequest())));
                        } else if (iq.getType() == IQ.Type.RESULT) {
                            updateRoster(roster, false);
                        }
                    }
                }
            }
        });
        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == Connection.Status.CLOSED) {
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
     * Gets a contact by its JID or null, if it does not exist.
     *
     * @param jid The JID.
     * @return The contact or null.
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
                        ContactGroup parentGroup = null;
                        for (int i = 0; i < nestedGroups.length; i++) {
                            String nestedGroup = nestedGroups[i];
                            currentGroupName += nestedGroup;
                            ContactGroup currentGroup = rosterGroupMap.get(currentGroupName);
                            if (currentGroup == null) {
                                currentGroup = new ContactGroup(nestedGroup, currentGroupName, parentGroup);
                                rosterGroupMap.put(currentGroupName, currentGroup);
                                // Only add top level groups.
                                if (i == 0) {
                                    groups.add(currentGroup);
                                }
                                if (parentGroup != null) {
                                    parentGroup.getGroups().add(currentGroup);
                                }
                            }

                            parentGroup = currentGroup;
                            if (i < nestedGroups.length - 1) {
                                currentGroupName += groupDelimiter;
                            }
                        }
                        if (parentGroup != null && !parentGroup.getContacts().contains(contact)) {
                            parentGroup.getContacts().add(contact);
                        }
                    }
                }

                // Add the contact to the list of unaffiliated contacts, if it has no groups and it hasn't been removed from the roster.
                if (contact.getGroups().isEmpty() && contact.getSubscription() != Contact.Subscription.REMOVE) {
                    for (Contact c : unaffiliatedContacts) {
                        if (c.getJid().equals(contact.getJid())) {
                            // Remove any previous contact.
                            unaffiliatedContacts.remove(c);
                            break;
                        }
                    }
                    unaffiliatedContacts.add(contact);
                } else {
                    // If the contact has groups or has been removed from the roster, remove it from the unaffiliated contacts.
                    for (Contact c : unaffiliatedContacts) {
                        if (c.getJid().equals(contact.getJid())) {
                            unaffiliatedContacts.remove(c);
                            break;
                        }
                    }
                }
                removeContactsFromGroups(contact, groups);
            }
        }
        notifyRosterListeners(new RosterEvent(this, addedContacts, updatedContacts, removedContacts));
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
            for (Contact c : contactGroup.getContacts()) {
                if (c.getJid().equals(contact.getJid())) {
                    contactGroup.getContacts().remove(c);
                    break;
                }
            }
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
    public Collection<Contact> getUnaffiliatedContacts() {
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
        this.connection.send(iq);
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
        connection.query(new IQ(IQ.Type.SET, roster));
        if (requestSubscription) {
            connection.getPresenceManager().requestSubscription(contact.getJid(), status);
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
     */
    public void removeContact(Jid jid) {
        Roster roster = new Roster();
        Contact contact = new Contact(jid);
        contact.setSubscription(Contact.Subscription.REMOVE);
        roster.getContacts().add(contact);
        connection.send(new IQ(IQ.Type.SET, roster));
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
