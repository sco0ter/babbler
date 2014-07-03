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

package org.xmpp.extension.reach;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.*;
import org.xmpp.stanza.client.IQ;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows to query for reachability addresses of another contact, automatically responds to reachability queries and notifies {@linkplain ReachabilityListener}s,
 * when the reachability of a contact has changed either via presence or PEP.
 * <p>
 * By default this manager is not enabled. If you support reachability addresses you have to {@linkplain #setEnabled(boolean) enable} it.
 * </p>
 *
 * @author Christian Schudt
 */
public final class ReachabilityManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(ReachabilityManager.class.getName());

    private final Set<ReachabilityListener> reachabilityListeners = new CopyOnWriteArraySet<>();

    private final Map<Jid, List<Address>> reachabilities = new ConcurrentHashMap<>();

    private final List<Address> addresses = new CopyOnWriteArrayList<>();

    private ReachabilityManager(final XmppSession xmppSession) {
        super(xmppSession, Reachability.NAMESPACE);
        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    reachabilityListeners.clear();
                    reachabilities.clear();
                    addresses.clear();
                }
            }
        });

        xmppSession.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                AbstractPresence presence = e.getPresence();
                if (e.isIncoming()) {
                    boolean hasReachability = checkStanzaForReachabilityAndNotify(presence);
                    Jid contact = presence.getFrom().asBareJid();
                    if (!hasReachability && reachabilities.remove(contact) != null) {
                        // If no reachability was found in presence, check, if the contact has previously sent any reachability via presence.
                        notifyReachabilityListeners(contact, new ArrayList<Address>());
                    }
                } else {
                    if (presence.isAvailable() && presence.getTo() == null) {
                        synchronized (addresses) {
                            if (!addresses.isEmpty()) {
                                presence.getExtensions().add(new Reachability(new ArrayList<>(addresses)));
                            }
                        }
                    }
                }
            }
        });

        // A user MAY send reachability addresses in an XMPP <message/> stanza.
        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    checkStanzaForReachabilityAndNotify(e.getMessage());
                }
            }
        });

        // In addition, a contact MAY request a user's reachability addresses in an XMPP <iq/> stanza of type "get"
        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    Reachability reachability = iq.getExtension(Reachability.class);
                    if (reachability != null) {
                        if (iq.getType() == IQ.Type.GET) {
                            if (isEnabled()) {
                                IQ result = iq.createResult();
                                result.setExtension(new Reachability(new ArrayList<>(addresses)));
                                xmppSession.send(result);
                            } else {
                                sendServiceUnavailable(iq);
                            }
                        } else if (iq.getType() == IQ.Type.SET) {
                            sendServiceUnavailable(iq);
                        }
                    }
                }
            }
        });
        // TODO: implement similar logic for PEP
    }

    public List<Address> getReachabilityAddresses() {
        return addresses;
    }

    private boolean checkStanzaForReachabilityAndNotify(Stanza stanza) {
        Reachability reachability = stanza.getExtension(Reachability.class);
        if (stanza.getFrom() != null) {
            Jid contact = stanza.getFrom().asBareJid();
            if (reachability != null) {
                synchronized (reachabilities) {
                    List<Address> oldReachabilityAddresses = reachabilities.get(contact);
                    if (oldReachabilityAddresses == null || !oldReachabilityAddresses.equals(reachability.getAddresses())) {
                        reachabilities.put(contact, reachability.getAddresses());
                        notifyReachabilityListeners(contact, reachability.getAddresses());
                    }
                }
            }
        }
        return reachability != null;
    }

    private void notifyReachabilityListeners(Jid from, List<Address> reachabilityAddresses) {
        for (ReachabilityListener reachabilityListener : reachabilityListeners) {
            try {
                reachabilityListener.reachabilityChanged(new ReachabilityEvent(ReachabilityManager.this, from, reachabilityAddresses));
            } catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Adds a reachability listener, which allows to listen for reachability updates.
     *
     * @param reachabilityListener The listener.
     * @see #removeReachabilityListener(ReachabilityListener)
     */
    public void addReachabilityListener(ReachabilityListener reachabilityListener) {
        reachabilityListeners.add(reachabilityListener);
    }

    /**
     * Removes a previously added reachability listener.
     *
     * @param reachabilityListener The listener.
     * @see #addReachabilityListener(ReachabilityListener)
     */
    public void removeReachabilityListener(ReachabilityListener reachabilityListener) {
        reachabilityListeners.remove(reachabilityListener);
    }

    /**
     * Requests the reachability addresses of a contact.
     *
     * @param contact The contact.
     * @return The reachability addresses.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public List<Address> requestReachabilityAddresses(Jid contact) throws XmppException {
        // In addition, a contact MAY request a user's reachability addresses in an XMPP <iq/> stanza of type "get".
        IQ result = xmppSession.query(new IQ(contact, IQ.Type.GET, new Reachability()));
        Reachability reachability = result.getExtension(Reachability.class);
        if (reachability != null) {
            return reachability.getAddresses();
        }
        return null;
    }
}
