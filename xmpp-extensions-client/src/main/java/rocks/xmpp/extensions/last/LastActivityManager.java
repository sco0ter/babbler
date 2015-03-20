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

package rocks.xmpp.extensions.last;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.PresenceListener;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.AbstractPresence;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.last.model.LastActivity;

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
 * LastActivityManager lastActivityManager = xmppSession.getManager(LastActivityManager.class);
 * LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.valueOf("juliet@example.com/balcony"));
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class LastActivityManager extends ExtensionManager {

    private volatile LastActivityStrategy lastActivityStrategy;

    private LastActivityManager(final XmppSession xmppSession) {
        super(xmppSession, LastActivity.NAMESPACE);
        lastActivityStrategy = new DefaultLastActivityStrategy(xmppSession);
        setEnabled(true);
    }

    @Override
    protected void initialize() {
        xmppSession.addSessionStatusListener(new SessionStatusListener() {
            @Override
            public void sessionStatusChanged(SessionStatusEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    lastActivityStrategy = null;
                }
            }
        });
        xmppSession.addOutboundPresenceListener(new PresenceListener() {
            @Override
            public void handlePresence(PresenceEvent e) {
                if (isEnabled()) {
                    AbstractPresence presence = e.getPresence();
                    if (presence.getTo() == null) {
                        synchronized (LastActivityManager.this) {
                            // If an available presence with <show/> value 'away' or 'xa' is sent, append last activity information.
                            if (lastActivityStrategy != null && lastActivityStrategy.getLastActivity() != null && presence.isAvailable() && (presence.getShow() == AbstractPresence.Show.AWAY || presence.getShow() == AbstractPresence.Show.XA) && presence.getExtension(LastActivity.class) == null) {
                                presence.getExtensions().add(new LastActivity(getSecondsSince(lastActivityStrategy.getLastActivity()), presence.getStatus()));
                            }
                        }
                    }
                }
            }
        });
        xmppSession.addIQHandler(LastActivity.class, new AbstractIQHandler(this, AbstractIQ.Type.GET) {
            @Override
            protected IQ processRequest(IQ iq) {
                // If someone asks me to get my last activity, reply.
                synchronized (LastActivityManager.this) {
                    long seconds = (lastActivityStrategy != null && lastActivityStrategy.getLastActivity() != null) ? getSecondsSince(lastActivityStrategy.getLastActivity()) : 0;
                    return iq.createResult(new LastActivity(seconds, null));
                }
            }
        });
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
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public LastActivity getLastActivity(Jid jid) throws XmppException {
        IQ result = xmppSession.query(new IQ(jid, IQ.Type.GET, new LastActivity()));
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
    private static class DefaultLastActivityStrategy implements LastActivityStrategy, MessageListener, PresenceListener {
        private volatile Date lastActivity;

        public DefaultLastActivityStrategy(XmppSession xmppSession) {
            xmppSession.addOutboundMessageListener(this);
            xmppSession.addOutboundPresenceListener(this);
        }

        @Override
        public Date getLastActivity() {
            return lastActivity;
        }

        @Override
        public void handleMessage(MessageEvent e) {
            lastActivity = new Date();
        }

        @Override
        public void handlePresence(PresenceEvent e) {
            AbstractPresence presence = e.getPresence();
            if (!presence.isAvailable() || presence.getShow() != AbstractPresence.Show.AWAY && presence.getShow() != AbstractPresence.Show.XA) {
                lastActivity = new Date();
            }
        }
    }
}
