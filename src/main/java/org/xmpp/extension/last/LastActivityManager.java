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

package org.xmpp.extension.last;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.*;

import java.util.Date;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0012.html">XEP-0012: Last Activity</a> and <a href="http://xmpp.org/extensions/xep-0256.html">XEP-0256: Last Activity in Presence</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0012.html#intro">1. Introduction</a></cite></p>
 * <p>It is often helpful to know the time of the last activity associated with a entity. The canonical usage is to discover when a disconnected user last accessed its server. The 'jabber:iq:last' namespace provides a method for retrieving that information. The 'jabber:iq:last' namespace can also be used to discover or publicize when a connected user was last active on the server (i.e., the user's idle time) or to query servers and components about their current uptime.</p>
 * </blockquote>
 * This class also takes care about the following use case, by automatically appending last activity information to 'away' and 'xa' presences:
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0256.html#away">1.2 Away and Extended Away</a></cite></p>
 * <p>When a client automatically sets the user's {@code <show/>} value to "away" or "xa" (extended away), it can indicate when that particular was last active during the current presence session.</p>
 * </blockquote>
 * <p>
 * By default last activity for the connected resource is updated whenever a message or available non-away, non-xa presence is sent.
 * This strategy of determining last activity can be changed by {@linkplain #setLastActivityStrategy(LastActivityStrategy) setting another strategy}, e.g. a strategy which determines
 * last activity by idle mouse activity.
 * </p>
 * <p>
 * Automatic inclusion of last activity information in presence stanzas and support for this protocol can be {@linkplain #setEnabled(boolean)} enabled or disabled}.
 * </p>
 * <h3>Code sample</h3>
 * <pre>
 * <code>
 * LastActivityManager lastActivityManager = connection.getExtensionManager(LastActivityManager.class);
 * LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.fromString("juliet@example.com/balcony"));
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class LastActivityManager extends ExtensionManager {

    private volatile LastActivityStrategy lastActivityStrategy;

    private LastActivityManager(final Connection connection) {
        super(connection, "jabber:iq:last");
        lastActivityStrategy = new DefaultLastActivityStrategy(connection);

        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == Connection.Status.CLOSED) {
                    lastActivityStrategy = null;
                }
            }
        });
        connection.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (!e.isIncoming() && isEnabled()) {
                    Presence presence = e.getPresence();
                    synchronized (LastActivityManager.this) {
                        // If an available presence with <show/> value 'away' or 'xa' is sent, append last activity information.
                        if (lastActivityStrategy != null && lastActivityStrategy.getLastActivity() != null && presence.isAvailable() && (presence.getShow() == Presence.Show.AWAY || presence.getShow() == Presence.Show.XA) && presence.getExtension(LastActivity.class) == null) {
                            presence.getExtensions().add(new LastActivity(getSecondsSince(lastActivityStrategy.getLastActivity()), presence.getStatus()));
                        }
                    }
                }
            }
        });

        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    // If someone asks me to get my last activity, reply.
                    if (iq.getType() == IQ.Type.GET && iq.getExtension(LastActivity.class) != null) {
                        synchronized (LastActivityManager.this) {
                            if (isEnabled()) {
                                IQ result = iq.createResult();
                                long seconds = (lastActivityStrategy != null && lastActivityStrategy.getLastActivity() != null) ? getSecondsSince(lastActivityStrategy.getLastActivity()) : 0;
                                result.setExtension(new LastActivity(seconds, null));
                                connection.send(result);
                            } else {
                                sendServiceUnavailable(iq);
                            }
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    private long getSecondsSince(Date date) {
        return Math.max(0, System.currentTimeMillis() - date.getTime()) / 1000;
    }

    /**
     * Gets the last activity of the specified user.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0012.html#impl">7. Implementation Notes</a></cite></p>
     * <p>The information contained in an IQ reply for this namespace is inherently ambiguous. Specifically, for a bare JID {@code <localpart@domain.tld>} the information is the time since the JID was last connected to its server; for a full JID {@code <localpart@domain.tld/resource>} the information is the time since the resource was last active in the context of an existing session; and for a bare domain the information is the uptime for the server or component. An application MUST take these differences into account when presenting the information to a human user (if any).</p>
     * </blockquote>
     *
     * @param jid The JID for which the last activity is requested.
     * @return The last activity of the requested JID or null if the feature is not implemented or a time out has occurred.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public LastActivity getLastActivity(Jid jid) throws XmppException {
        IQ result = connection.query(new IQ(jid, IQ.Type.GET, new LastActivity()));
        return result.getExtension(LastActivity.class);
    }

    /**
     * Gets the currently used strategy to determine last activity of a client.
     *
     * @return The strategy.
     * @see #setLastActivityStrategy(LastActivityStrategy)
     */
    public synchronized LastActivityStrategy getLastActivityStrategy() {
        return this.lastActivityStrategy;
    }

    /**
     * Sets a strategy, which is used to determine last activity of a client.
     *
     * @param lastActivityStrategy The strategy.
     * @see #getLastActivityStrategy()
     */
    public synchronized void setLastActivityStrategy(LastActivityStrategy lastActivityStrategy) {
        this.lastActivityStrategy = lastActivityStrategy;
    }

    /**
     * The default strategy to determine last activity. It simply sets the date of last activity, whenever a message or presence is sent.
     */
    private static class DefaultLastActivityStrategy implements LastActivityStrategy {
        private volatile Date lastActivity;

        public DefaultLastActivityStrategy(Connection connection) {
            connection.addMessageListener(new MessageListener() {
                @Override
                public void handle(MessageEvent e) {
                    if (!e.isIncoming()) {
                        lastActivity = new Date();
                    }
                }
            });
            connection.addPresenceListener(new PresenceListener() {
                @Override
                public void handle(PresenceEvent e) {
                    Presence presence = e.getPresence();
                    if (!e.isIncoming() && (!presence.isAvailable() || presence.getShow() != Presence.Show.AWAY && presence.getShow() != Presence.Show.XA)) {
                        lastActivity = new Date();
                    }
                }
            });
        }

        @Override
        public Date getLastActivity() {
            return lastActivity;
        }
    }
}
