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
import org.xmpp.stanza.errors.UnexpectedRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<Jid, Roster.Contact> contactMap = new ConcurrentHashMap<>();

    private final Set<RosterListener> rosterListeners = new CopyOnWriteArraySet<>();

    private final Connection connection;

    private boolean retrieveRosterOnLogin = true;

    public RosterManager(final Connection connection) {
        this.connection = connection;
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    Roster roster = iq.getExtension(Roster.class);
                    if (roster != null) {
                        if (iq.getType() == IQ.Type.SET) {
                            // Gracefully send an empty result.
                            connection.send(iq.createResult());
                            updateRoster(roster);
                        } else if (iq.getType() == IQ.Type.GET) {
                            connection.send(iq.createError(new StanzaError(new UnexpectedRequest())));
                        } else if (iq.getType() == IQ.Type.RESULT) {
                            updateRoster(roster);
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
     * Gets a contact by its JID or null, if it does not exist.
     *
     * @param jid The JID.
     * @return The contact or null.
     */
    public Roster.Contact getContact(Jid jid) {
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null");
        }
        return contactMap.get(jid.asBareJid());
    }

    void updateRoster(Roster roster) {
        List<Roster.Contact> addedContacts = new ArrayList<>();
        List<Roster.Contact> updatedContacts = new ArrayList<>();
        List<Roster.Contact> removedContacts = new ArrayList<>();
        Collections.sort(roster.getContacts());
        // Loop through the new roster and compare it with the old ones.
        for (Roster.Contact contact : roster.getContacts()) {
            Roster.Contact oldContact = contactMap.get(contact.getJid());
            if (contact.getSubscription() == Roster.Contact.Subscription.REMOVE) {
                contactMap.remove(contact.getJid());
                removedContacts.add(contact);
            } else if (oldContact != null && !oldContact.equals(contact)) {
                contactMap.put(contact.getJid(), contact);
                updatedContacts.add(contact);
            } else if (oldContact == null) {
                contactMap.put(contact.getJid(), contact);
                addedContacts.add(contact);
            }
        }
        notifyRosterListeners(new RosterEvent(this, addedContacts, updatedContacts, removedContacts));
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
    public void addContact(Roster.Contact contact, boolean requestSubscription, String status) throws XmppException {
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
    public void updateContact(Roster.Contact contact) throws XmppException {
        addContact(contact, false, null);
    }

    /**
     * Removes a contact from the roster.
     *
     * @param jid The contact's JID.
     */
    public void removeContact(Jid jid) {
        Roster roster = new Roster();
        Roster.Contact contact = new Roster.Contact(jid);
        contact.setSubscription(Roster.Contact.Subscription.REMOVE);
        roster.getContacts().add(contact);
        connection.send(new IQ(IQ.Type.SET, roster));
    }
}
