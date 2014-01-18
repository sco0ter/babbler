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

import org.xmpp.Connection;
import org.xmpp.ConnectionEvent;
import org.xmpp.ConnectionListener;
import org.xmpp.Jid;
import org.xmpp.stanza.Presence;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages subscription requests and presences.
 * <p>
 * See also <a href="http://xmpp.org/rfcs/rfc6121.html#sub">3.  Managing Presence Subscriptions</a>
 * </p>
 * <p>
 * This class allows to request, approve, deny and unsubscribe subscriptions.
 * </p>
 *
 * @author Christian Schudt
 */
public final class PresenceManager {

    // TODO auto deny or auto approve some or all requests.

    private final Connection connection;

    private final Map<Jid, Map<String, Presence>> presenceMap = new ConcurrentHashMap<>();

    private final Map<String, Presence> lastSentPresences = new ConcurrentHashMap<>();

    public PresenceManager(final Connection connection) {

        this.connection = connection;

        connection.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                Presence presence = e.getPresence();
                if (e.isIncoming()) {

                    if (!presenceMap.containsKey(presence.getFrom().toBareJid())) {
                        // Store the user (bare JID) in the map, associated with different resources.
                        presenceMap.put(presence.getFrom().toBareJid(), new ConcurrentHashMap<String, Presence>());
                    }
                    Map<String, Presence> presencesPerResource = presenceMap.get(presence.getFrom().toBareJid());
                    // Update the contact's resource with the presence.
                    presencesPerResource.put(presence.getFrom().getResource() != null ? presence.getFrom().getResource() : "", presence);
                } else {
                    // Store the last sent presences, in order to automatically resend them, after a disconnect.
                    if (presence.getType() == null || presence.getType() == Presence.Type.UNAVAILABLE) {
                        if (presence.getTo() == null) {
                            lastSentPresences.put("", presence);
                        } else {
                            lastSentPresences.put(presence.getTo().toString(), presence);
                        }
                    }
                }
            }
        });

        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                // Resend the last presences, as soon as we are reconnected.
                if (e.getStatus() == Connection.Status.CONNECTED) {
                    for (Presence presence : lastSentPresences.values()) {
                        connection.send(presence);
                    }
                }
            }
        });
    }

    /**
     * Gets the presence for a given contact.
     * <p>
     * If the given JID is a bare JID, and the contact has sent multiple presences with different resources,
     * the "best" presence is returned, i.e. the presence with the highest priority or the presence with the "most available" {@code <show/>} element.
     * </p>
     * <p>
     * If the JID is a full JID, the exact presence of that JID is returned.
     * </p>
     * <p>
     * If no presence could be found an unavailable presence is returned.
     * </p>
     *
     * @param jid The JID.
     * @return The presence.
     */
    public Presence getPresence(Jid jid) {
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }

        if (jid.isBareJid()) {
            Map<String, Presence> presencesPerResource = presenceMap.get(jid);
            if (presencesPerResource != null) {
                List<Presence> presences = new ArrayList<>(presencesPerResource.values());
                if (!presences.isEmpty()) {
                    Collections.sort(presences);
                    return presences.get(0);
                }
            }
        } else {
            Map<String, Presence> presencesPerResource = presenceMap.get(jid.toBareJid());
            if (presencesPerResource != null) {
                Presence presence = presencesPerResource.get(jid.getResource());
                if (presence != null) {
                    return presence;
                }
            }
        }

        Presence presence = new Presence(Presence.Type.UNAVAILABLE);
        presence.setFrom(jid);
        return presence;
    }

    /**
     * Sends a subscription request to a potential contact.
     * <p>
     * See also <a href="http://xmpp.org/rfcs/rfc6121.html#sub-request-gen">3.1.1.  Client Generation of Outbound Subscription Request</a>
     * </p>
     *
     * @param jid    The contact's JID.
     * @param status The status, which is used for additional information during the subscription request.
     * @return The id, which is used for the request.
     */
    public String requestSubscription(Jid jid, String status) {
        Presence presence = new Presence(Presence.Type.SUBSCRIBE);
        // the value of the 'to' attribute MUST be a bare JID
        presence.setTo(jid.toBareJid());
        presence.setId(UUID.randomUUID().toString());
        presence.setStatus(status);
        connection.send(presence);
        return presence.getId();
    }

    /**
     * Approves a subscription request.
     * <p>
     * See also <a href="http://xmpp.org/rfcs/rfc6121.html#sub-request-handle">3.1.4.  Client Processing of Inbound Subscription Request</a>
     * </p>
     *
     * @param jid The contact's JID, who has previously requested a subscription.
     * @return The id, which is used for the approval.
     */
    public String approveSubscription(Jid jid) {
        Presence presence = new Presence(Presence.Type.SUBSCRIBED);
        // For tracking purposes, a client SHOULD include an 'id' attribute in a subscription approval or subscription denial; this 'id' attribute MUST NOT mirror the 'id' attribute of the subscription request.
        presence.setId(UUID.randomUUID().toString());
        presence.setTo(jid);
        connection.send(presence);
        return presence.getId();
    }

    /**
     * Cancels a previously granted subscription or denies a subscription request.
     * <p>
     * This basically means that the contact won't receive presence information from you.
     * </p>
     * See also <a href="http://xmpp.org/rfcs/rfc6121.html#sub-cancel-gen">3.2.1.  Client Generation of Subscription Cancellation</a>
     *
     * @param jid The contact's JID, whose subscription is denied or canceled.
     * @return The id, which is used for the subscription denial.
     */
    public String denySubscription(Jid jid) {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBED);
        // For tracking purposes, a client SHOULD include an 'id' attribute in a subscription approval or subscription denial; this 'id' attribute MUST NOT mirror the 'id' attribute of the subscription request.
        presence.setId(UUID.randomUUID().toString());
        presence.setTo(jid);
        connection.send(presence);
        return presence.getId();
    }

    /**
     * Unsubscribes from a contact's presence.
     * <p>
     * That basically means, that you will not receive any presence information from the contact anymore.
     * </p>
     * See also <a href="http://xmpp.org/rfcs/rfc6121.html#sub-unsub-gen">3.3.1.  Client Generation of Unsubscribe</a>
     *
     * @param jid The contact's JID.
     * @return The id, which is used for the unsubscription.
     */
    public String unsubscribe(Jid jid) {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBE);
        // For tracking purposes, a client SHOULD include an 'id' attribute in a subscription approval or subscription denial; this 'id' attribute MUST NOT mirror the 'id' attribute of the subscription request.
        presence.setId(UUID.randomUUID().toString());
        presence.setTo(jid);
        connection.send(presence);
        return presence.getId();
    }

    /**
     * Gets the last sent presence, that has been broadcast by the server.
     *
     * @return The presence.
     */
    public Presence getLastSentPresence() {
        return lastSentPresences.get("");
    }
}
