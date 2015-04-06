/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.core.roster;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.roster.model.Contact;
import rocks.xmpp.core.roster.model.ContactGroup;
import rocks.xmpp.core.roster.model.Roster;
import rocks.xmpp.core.roster.versioning.model.RosterVersioning;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.core.subscription.PresenceManager;
import rocks.xmpp.core.util.cache.DirectoryCache;
import rocks.xmpp.extensions.privatedata.PrivateDataManager;
import rocks.xmpp.extensions.privatedata.rosterdelimiter.model.RosterDelimiter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages the roster (aka contact or buddy list).
 * <h3>Roster Versioning</h3>
 * Because rosters can become quite large, but usually change infrequently, rosters can be cached on client side.
 * Hence before {@linkplain #requestRoster() requesting the roster}, it is checked if there's a cached version of your roster in the {@linkplain rocks.xmpp.core.session.XmppSessionConfiguration#getCacheDirectory() cache directory}.
 * If so, the server is informed about your version and will not send the full roster, but only "diffs" to your version, thus being more efficient.
 * <h3>Retrieving the Roster on Login</h3>
 * As per <a href="http://xmpp.org/rfcs/rfc6121.html#roster-login">RFC 6121</a> the roster should be retrieved on login.
 * This behavior can also be {@linkplain #setRetrieveRosterOnLogin(boolean) changed}.
 * <h3>Nested Roster Groups</h3>
 * <a href="http://xmpp.org/extensions/xep-0083.html">XEP-0083: Nested Roster Groups</a> are supported, but are disabled by default, which means the group delimiter is not retrieved before {@linkplain #requestRoster() requesting the roster}.
 * You can {@linkplain #setAskForGroupDelimiter(boolean) change} this behavior or {@linkplain #setGroupDelimiter(String) set a group delimiter} without retrieving it from the server in case you want to use a fix roster group delimiter.
 * <p>
 * You can listen for roster updates (aka roster pushes) and for initial roster retrieval, by {@linkplain #addRosterListener(Consumer) adding} a {@link Consumer}.
 * </p>
 * This class is unconditionally thread-safe.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/rfcs/rfc6121.html#roster-login">2.2.  Retrieving the Roster on Login</a>
 * @see <a href="http://xmpp.org/rfcs/rfc6121.html#roster-versioning">2.6.  Roster Versioning</a>
 * @see <a href="http://xmpp.org/extensions/xep-0083.html">XEP-0083: Nested Roster Groups</a>
 */
public final class RosterManager extends Manager {
    private static final Logger logger = Logger.getLogger(RosterManager.class.getName());

    private final Map<Jid, Contact> contactMap = new ConcurrentHashMap<>();

    private final Set<Consumer<RosterEvent>> rosterListeners = new CopyOnWriteArraySet<>();

    private final XmppSession xmppSession;

    private final Map<String, byte[]> rosterCacheDirectory;

    /**
     * guarded by "this"
     */
    private final Set<ContactGroup> groups = new TreeSet<>();

    /**
     * guarded by "this"
     */
    private final Set<Contact> unaffiliatedContacts = new TreeSet<>();

    /**
     * guarded by "this"
     */
    private final Map<String, ContactGroup> rosterGroupMap = new HashMap<>();

    private final PrivateDataManager privateDataManager;

    /**
     * guarded by "this"
     */
    private boolean retrieveRosterOnLogin = true;

    /**
     * guarded by "this"
     */
    private boolean askForGroupDelimiter;

    /**
     * guarded by "this"
     */
    private String groupDelimiter;

    private RosterManager(final XmppSession xmppSession) {
        this.xmppSession = xmppSession;
        privateDataManager = xmppSession.getManager(PrivateDataManager.class);

        this.rosterCacheDirectory = xmppSession.getConfiguration().getCacheDirectory() != null ? new DirectoryCache(xmppSession.getConfiguration().getCacheDirectory().resolve("rosterver")) : null;
        setEnabled(true);
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

    @Override
    protected final void initialize() {
        xmppSession.addIQHandler(Roster.class, new AbstractIQHandler(this, AbstractIQ.Type.SET) {
            @Override
            public IQ processRequest(IQ iq) {
                Roster roster = iq.getExtension(Roster.class);
                // 2.1.6.  Roster Push
                // A receiving client MUST ignore the stanza unless it has no 'from' attribute (i.e., implicitly from the bare JID of the user's account) or it has a 'from' attribute whose value matches the user's bare JID <user@domainpart>.
                if (iq.getFrom() == null || iq.getFrom().equals(xmppSession.getConnectedResource().asBareJid())) {
                    updateRoster(roster, true);
                    // Gracefully send an empty result.
                    return iq.createResult();
                } else {
                    // If the client receives a roster push from an unauthorized entity, it MUST NOT process the pushed data; in addition, the client can either return a stanza error of <service-unavailable/> error
                    return iq.createError(Condition.SERVICE_UNAVAILABLE);
                }
            }
        }, false); // Roster pushes should be processed in order as they arrive, so that they don't mess up the roster.
        xmppSession.addSessionStatusListener(e -> {
            if (e.getStatus() == XmppSession.Status.CLOSED) {
                rosterListeners.clear();
            }
        });
    }

    /**
     * Gets the contacts.
     *
     * @return The contacts.
     */
    public final Collection<Contact> getContacts() {
        // return defensive copies of mutable internal fields
        return Collections.unmodifiableCollection(new ArrayList<>(contactMap.values()));
    }

    /**
     * Gets a contact by its JID.
     *
     * @param jid The JID.
     * @return The contact or null, if it does not exist.
     */
    public final Contact getContact(Jid jid) {
        return contactMap.get(Objects.requireNonNull(jid, "jid must not be null").asBareJid());
    }

    void updateRoster(Roster roster, boolean isRosterPush) {
        List<Contact> addedContacts = new ArrayList<>();
        List<Contact> updatedContacts = new ArrayList<>();
        List<Contact> removedContacts = new ArrayList<>();
        List<Contact> contacts = new ArrayList<>(roster.getContacts());
        contacts.sort(null);
        synchronized (this) {
            if (!isRosterPush) {
                rosterGroupMap.clear();
                contactMap.clear();
            }

            // Loop through the new roster and compare it with the old one.
            for (Contact contact : contacts) {
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
                        if (groupDelimiter != null && !groupDelimiter.isEmpty()) {
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
            cacheRoster(roster.getVersion());
        }
        XmppUtils.notifyEventListeners(rosterListeners, new RosterEvent(this, addedContacts, updatedContacts, removedContacts));
    }

    private void cacheRoster(String version) {
        if (rosterCacheDirectory != null && version != null) {
            Roster roster = new Roster(contactMap.values(), version);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                XMLStreamWriter xmppStreamWriter = null;
                try {
                    xmppStreamWriter = XmppUtils.createXmppStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream), true);
                    xmppStreamWriter.flush();
                    xmppSession.createMarshaller().marshal(roster, xmppStreamWriter);
                } finally {
                    if (xmppStreamWriter != null) {
                        xmppStreamWriter.close();
                    }
                }
                rosterCacheDirectory.put(XmppUtils.hash(xmppSession.getConnectedResource().asBareJid().toString().getBytes()) + ".xml", outputStream.toByteArray());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not write roster to cache.", e);
            }
        }
    }

    private Roster readRosterFromCache() {
        if (rosterCacheDirectory != null) {
            try {
                byte[] rosterData = rosterCacheDirectory.get(XmppUtils.hash(xmppSession.getConnectedResource().asBareJid().toString().getBytes()) + ".xml");
                if (rosterData != null) {
                    try (InputStream inputStream = new ByteArrayInputStream(rosterData)) {
                        return (Roster) xmppSession.createUnmarshaller().unmarshal(inputStream);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not read roster from cache.", e);
            }
        }
        return null;
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
        // Recursively remove the contact from the nested subgroups.
// If the nested group is empty, it can be removed.
        contactGroups.stream().filter(group -> removeRecursively(contact, group)).forEach(group -> {
            emptyGroups.add(group);
            rosterGroupMap.remove(group.getFullName());
        });
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
     * Gets the contact groups. The returned collection is sorted. It should not be shared
     *
     * @return The contact groups.
     */
    public final synchronized Collection<ContactGroup> getContactGroups() {
        // return defensive copies of mutable internal fields
        return Collections.unmodifiableCollection(new ArrayList<>(groups));
    }

    /**
     * Gets the contacts, which are not affiliated to any group.
     *
     * @return The contacts, which are not affiliated to any group.
     */
    public final synchronized Collection<Contact> getUnaffiliatedContacts() {
        // return defensive copies of mutable internal fields
        return Collections.unmodifiableCollection(new ArrayList<>(unaffiliatedContacts));
    }

    /**
     * Adds a roster listener, which will get notified, whenever the roster changes.
     *
     * @param rosterListener The roster listener.
     * @see #removeRosterListener(Consumer)
     */
    public final void addRosterListener(Consumer<RosterEvent> rosterListener) {
        rosterListeners.add(rosterListener);
    }

    /**
     * Removes a previously added roster listener.
     *
     * @param rosterListener The roster listener.
     * @see #addRosterListener(Consumer)
     */
    public final void removeRosterListener(Consumer<RosterEvent> rosterListener) {
        rosterListeners.remove(rosterListener);
    }

    /**
     * Controls, whether the roster is automatically retrieved as soon as the user has logged in.
     *
     * @return True, if the roster is automatically retrieved after login.
     * @see #setRetrieveRosterOnLogin(boolean)
     */
    public final synchronized boolean isRetrieveRosterOnLogin() {
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
    public final synchronized void setRetrieveRosterOnLogin(boolean retrieveRosterOnLogin) {
        this.retrieveRosterOnLogin = retrieveRosterOnLogin;
    }

    /**
     * Requests the roster from the server. When the server returns the result, the {@link Consumer} are notified.
     * That means, you should first {@linkplain #addRosterListener(Consumer) register} a {@link Consumer} prior to calling this method.
     * <p>
     * <a href="http://xmpp.org/rfcs/rfc6121.html#roster-versioning">Roster Versioning</a> is supported, which means that this method checks
     * if there's a cached version of your roster in the {@linkplain rocks.xmpp.core.session.XmppSessionConfiguration#getCacheDirectory() cache directory}.
     * If so and if Roster Versioning is supported by the server, the cached version is returned and any missing roster items are sent later by the server via roster pushes.
     *
     * @return The roster.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final Roster requestRoster() throws XmppException {
        // XEP-0083: A compliant client SHOULD ask for the nested delimiter before requesting the user's roster
        if (isAskForGroupDelimiter()) {
            try {
                PrivateDataManager privateDataManager = xmppSession.getManager(PrivateDataManager.class);
                RosterDelimiter rosterDelimiter = privateDataManager.getData(RosterDelimiter.class);
                setGroupDelimiter(rosterDelimiter.getRosterDelimiter());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Roster delimiter could not be retrieved from private storage.");
            }
        }

        Roster rosterRequest;
        Roster roster = null;
        if (isRosterVersioningSupported()) {
            // If a client supports roster versioning and the server to which it has connected advertises support for roster versioning as described in the foregoing section, then the client SHOULD include the 'ver' element in its request for the roster.
            // If the client includes the 'ver' attribute in its roster get, it sets the attribute's value to the version ID associated with its last cache of the roster.
            roster = readRosterFromCache();
            // If the client has not yet cached the roster or the cache is lost or corrupted, but the client wishes to bootstrap the use of roster versioning, it MUST set the 'ver' attribute to the empty string (i.e., ver="").
            String ver = roster != null ? roster.getVersion() : "";
            rosterRequest = new Roster(ver);
        } else {
            // If the server does not advertise support for roster versioning, the client MUST NOT include the 'ver' attribute.
            rosterRequest = new Roster();
        }

        IQ result = xmppSession.query(new IQ(IQ.Type.GET, rosterRequest));
        Roster rosterResult = result.getExtension(Roster.class);
        // null result means, the requested roster version (from cache) is taken and any updates (if any) are done via roster pushes.
        if (rosterResult != null) {
            roster = rosterResult;
        }
        updateRoster(roster, false);
        return roster;
    }

    /**
     * Adds a contact to the roster and optionally also sends a subscription request to it.
     *
     * @param contact             The contact.
     * @param requestSubscription If true, the contact is also sent a subscription request.
     * @param status              The optional status text, which is sent together with a subscription request. May be null.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final void addContact(Contact contact, boolean requestSubscription, String status) throws XmppException {
        Objects.requireNonNull(contact, "contact must not be null.");
        xmppSession.query(new IQ(IQ.Type.SET, new Roster(contact)));
        if (requestSubscription) {
            xmppSession.getManager(PresenceManager.class).requestSubscription(contact.getJid(), status);
        }
    }

    /**
     * Updates a contact in the roster.
     *
     * @param contact The contact to update.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final void updateContact(Contact contact) throws XmppException {
        addContact(contact, false, null);
    }

    /**
     * Removes a contact from the roster.
     *
     * @param jid The contact's JID.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final void removeContact(Jid jid) throws XmppException {
        Roster roster = new Roster(new Contact(jid, null, null, null, Contact.Subscription.REMOVE, Collections.emptyList()));
        xmppSession.query(new IQ(IQ.Type.SET, roster));
    }

    /**
     * Renames a contact group.
     *
     * @param contactGroup The contact group.
     * @param name         The new name.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final void renameContactGroup(ContactGroup contactGroup, String name) throws XmppException {
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
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    private synchronized void replaceGroupName(ContactGroup contactGroup, String name, int index) throws XmppException {
        // Update each contact in this group with the new group name.
        String newName = name;
        if (groupDelimiter != null && !groupDelimiter.isEmpty()) {
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
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final void removeContactGroup(ContactGroup contactGroup) throws XmppException {
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
     * Gets the group delimiter.
     *
     * @return The group delimiter.
     * @see #setGroupDelimiter(String)
     */
    public final synchronized String getGroupDelimiter() {
        return groupDelimiter;
    }

    /**
     * Sets the group delimiter without storing it on the server.
     * <p>
     * If this is set to a non-null value, contact groups are split by the specified delimiter in order to build a nested hierarchy of groups.
     * </p>
     *
     * @param groupDelimiter The group delimiter.
     * @see #storeGroupDelimiter(String)
     * @see <a href="http://xmpp.org/extensions/xep-0083.html">XEP-0083: Nested Roster Groups</a>
     */
    public final synchronized void setGroupDelimiter(String groupDelimiter) {
        this.groupDelimiter = groupDelimiter;
    }

    /**
     * Stores the roster group delimiter in the private storage and afterwards sets it.
     *
     * @param groupDelimiter The group delimiter.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see #setGroupDelimiter(String)
     * @see <a href="http://xmpp.org/extensions/xep-0083.html">XEP-0083: Nested Roster Groups</a>
     */
    public final synchronized void storeGroupDelimiter(String groupDelimiter) throws XmppException {
        privateDataManager.storeData(new RosterDelimiter(groupDelimiter));
        setGroupDelimiter(groupDelimiter);
    }

    /**
     * Indicates whether the server is asked for the roster delimiter before requesting the roster.
     *
     * @return True, if the server is asked for the roster delimiter.
     */
    public synchronized boolean isAskForGroupDelimiter() {
        return askForGroupDelimiter;
    }

    /**
     * Sets whether the server is asked for the roster delimiter before requesting the roster.
     *
     * @param askForGroupDelimiter True, if the server is asked for the roster delimiter before requesting the roster.
     * @see #requestRoster()
     */
    public synchronized void setAskForGroupDelimiter(boolean askForGroupDelimiter) {
        this.askForGroupDelimiter = askForGroupDelimiter;
    }

    /**
     * Indicates whether the server supports roster versioning.
     *
     * @return True, if roster versioning is supported.
     * @see <a href="http://xmpp.org/rfcs/rfc6121.html#roster-versioning">2.6.  Roster Versioning</a>
     */
    public boolean isRosterVersioningSupported() {
        return xmppSession.getManager(StreamFeaturesManager.class).getFeatures().containsKey(RosterVersioning.class);
    }
}
