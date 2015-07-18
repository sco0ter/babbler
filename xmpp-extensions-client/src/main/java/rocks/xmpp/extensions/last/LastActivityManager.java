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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.idle.IdleManager;
import rocks.xmpp.extensions.last.model.LastActivity;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

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
 * This manager has a dependency to {@link IdleManager}, i.e. it uses the same idle time as that one.
 * </p>
 * <p>
 * Automatic inclusion of last activity information in presence stanzas and support for this protocol can be {@linkplain #setEnabled(boolean)} enabled or disabled}.
 * </p>
 * <h3>Code sample</h3>
 * <pre>
 * <code>
 * LastActivityManager lastActivityManager = xmppSession.getManager(LastActivityManager.class);
 * LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.of("juliet@example.com/balcony"));
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 * @see IdleManager
 */
public final class LastActivityManager extends Manager {

    private final IdleManager idleManager;

    private final Consumer<PresenceEvent> outboundPresenceListener;

    private final IQHandler iqHandler;

    private LastActivityManager(final XmppSession xmppSession) {
        super(xmppSession);
        this.idleManager = xmppSession.getManager(IdleManager.class);
        this.outboundPresenceListener = e -> {
            Presence presence = e.getPresence();
            if (presence.getTo() == null) {
                synchronized (LastActivityManager.this) {
                    // If an available presence with <show/> value 'away' or 'xa' is sent, append last activity information.
                    if (idleManager.getIdleStrategy() != null && presence.isAvailable() && (presence.getShow() == Presence.Show.AWAY || presence.getShow() == Presence.Show.XA) && !presence.hasExtension(LastActivity.class)) {
                        presence.addExtension(new LastActivity(getSecondsSince(idleManager.getIdleStrategy().get()), presence.getStatus()));
                    }
                }
            }
        };
        this.iqHandler = new AbstractIQHandler(IQ.Type.GET) {
            @Override
            protected IQ processRequest(IQ iq) {
                // If someone asks me to get my last activity, reply.
                synchronized (idleManager) {
                    Instant idleSince = idleManager.getIdleStrategy() != null ? idleManager.getIdleStrategy().get() : null;
                    if (idleSince != null) {
                        return iq.createResult(new LastActivity(getSecondsSince(idleSince), null));
                    } else {
                        // A client that does not support the protocol, or that does not wish to divulge this information, MUST return a <service-unavailable/> error.
                        return iq.createError(Condition.SERVICE_UNAVAILABLE);
                    }
                }
            }
        };
    }

    static long getSecondsSince(Instant date) {
        return Math.max(0, Duration.between(date, Instant.now()).getSeconds());
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        xmppSession.addOutboundPresenceListener(outboundPresenceListener);
        xmppSession.addIQHandler(LastActivity.class, iqHandler);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        xmppSession.removeOutboundPresenceListener(outboundPresenceListener);
        xmppSession.removeIQHandler(LastActivity.class);
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
        IQ result = xmppSession.query(IQ.get(jid, new LastActivity()));
        return result.getExtension(LastActivity.class);
    }
}
