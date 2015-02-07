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

package rocks.xmpp.extensions.ping;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.IQExtensionManager;
import rocks.xmpp.core.session.NoResponseException;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQListener;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.PresenceListener;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.StanzaException;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.ping.model.Ping;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the application-level ping mechanism as specified in <a href="http://xmpp.org/extensions/xep-0199.html">XEP-0199: XMPP Ping</a>.
 * <p>
 * If enabled, it periodically pings the server to ensure a stable connection. These pings are not sent as long as other stanzas are sent, because they serve the same purpose (telling the server, that we are still available).
 * <p>
 * For <a href="http://xmpp.org/extensions/xep-0199.html#s2c">Server-To-Client Pings</a> it automatically responds with a result (pong), if enabled.
 * <p>
 * It also allows to ping the server manually (<a href="http://xmpp.org/extensions/xep-0199.html#c2s">Client-To-Server Pings</a>) or to ping other XMPP entities (<a href="http://xmpp.org/extensions/xep-0199.html#e2e">Client-to-Client Pings</a>).
 *
 * @author Christian Schudt
 */
public final class PingManager extends IQExtensionManager implements SessionStatusListener {

    private static final Logger logger = Logger.getLogger(PingManager.class.getName());

    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * guarded by "this"
     */
    private ScheduledFuture<?> nextPing;

    /**
     * guarded by "this"
     */
    private long pingInterval = 900; // 15 minutes

    /**
     * Creates the ping manager.
     *
     * @param xmppSession The underlying XMPP session.
     */
    private PingManager(final XmppSession xmppSession) {
        super(xmppSession, AbstractIQ.Type.GET, Ping.NAMESPACE);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(XmppUtils.createNamedThreadFactory("XMPP Scheduled Ping Thread"));
        setEnabled(true);
    }

    @Override
    protected final void initialize() {
        xmppSession.addIQHandler(Ping.class, this);
        xmppSession.addSessionStatusListener(this);

        // Reschedule server pings, whenever we send a stanza to the server.
        // Pinging the server is only necessary to let him know, that we are still connected.
        // Therefore sending a message and shortly after it sending a ping would add no value.
        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handleMessage(MessageEvent e) {
                if (!e.isIncoming()) {
                    rescheduleNextPing();
                }
            }
        });
        xmppSession.addPresenceListener(new PresenceListener() {
            @Override
            public void handlePresence(PresenceEvent e) {
                if (!e.isIncoming()) {
                    rescheduleNextPing();
                }
            }
        });
        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handleIQ(IQEvent e) {
                if (!e.isIncoming()) {
                    rescheduleNextPing();
                }
            }
        });
    }

    /**
     * Pings the given XMPP entity.
     *
     * @param jid The JID to ping.
     * @return True if a response has been received within the timeout and the recipient is available, false otherwise.
     */
    public final boolean ping(Jid jid) {
        return ping(jid, xmppSession.getConfiguration().getDefaultResponseTimeout());
    }

    /**
     * Pings the given XMPP entity.
     *
     * @param jid     The JID to ping.
     * @param timeout The timeout in milliseconds.
     * @return True if a response has been received within the timeout and the recipient is available, false otherwise.
     */
    public final boolean ping(Jid jid, long timeout) {
        try {
            xmppSession.query(new IQ(jid, IQ.Type.GET, Ping.INSTANCE), timeout);
            return true;
        } catch (StanzaException e) {
            // If we pinged a full JID and the resource if offline, the server will respond on behalf of the user with <service-unavailable/>.
            // In this case we want to return false, because the intended recipient is unavailable.
            // If we pinged a bare JID, the server will respond. If it returned a <service-unavailable/> error, it just means it doesn't understand the ping protocol.
            // Nonetheless an error response is still a valid pong, hence always return true in this case.
            // If any other error is returned, most likely <remote-server-not-found/>, <remote-server-timeout/>, <gone/> return false.
            return (jid == null || jid.isBareJid()) && e.getStanza().getError().getCondition() == Condition.SERVICE_UNAVAILABLE;
        } catch (NoResponseException e) {
            return false;
        }
    }

    /**
     * Pings the connected server.
     *
     * @return True if a response has been received, false otherwise.
     */
    public final boolean pingServer() {
        return ping(new Jid(xmppSession.getDomain()));
    }

    /**
     * Gets the ping interval in seconds. The default ping interval is 900 seconds (15 minutes).
     *
     * @return The ping interval in seconds.
     * @see #setPingInterval(long)
     */
    public final synchronized long getPingInterval() {
        return pingInterval;
    }

    /**
     * Sets the automatic ping interval in seconds. Any scheduled future ping is canceled and a new ping is scheduled after the specified interval.
     *
     * @param pingInterval The ping interval in seconds.
     * @see #getPingInterval()
     */
    public final synchronized void setPingInterval(long pingInterval) {
        this.pingInterval = pingInterval;
        rescheduleNextPing();
    }

    @Override
    public final void setEnabled(boolean enabled) {
        boolean wasEnabled = isEnabled();
        super.setEnabled(enabled);

        if (enabled && !wasEnabled) {
            rescheduleNextPing();
        } else if (!enabled && wasEnabled) {
            cancelNextPing();
        }
    }

    private synchronized void rescheduleNextPing() {
        cancelNextPing();
        if (pingInterval > 0 && !scheduledExecutorService.isShutdown()) {
            nextPing = scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    if (isEnabled() && xmppSession.getStatus() == XmppSession.Status.AUTHENTICATED) {
                        if (!pingServer()) {
                            logger.log(Level.WARNING, "Timeout reached while pinging server.");
                        }
                    }
                    nextPing = scheduledExecutorService.schedule(this, pingInterval, TimeUnit.SECONDS);
                }
            }, pingInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    protected final IQ processRequest(final IQ iq) {
        return iq.createResult();
    }

    @Override
    public final void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            // Shutdown the ping executor service and cancel the next ping.
            synchronized (this) {
                cancelNextPing();
                scheduledExecutorService.shutdown();
            }
        }
    }

    /**
     * If a ping has been scheduled, it will be canceled.
     */
    private synchronized void cancelNextPing() {
        if (nextPing != null) {
            nextPing.cancel(true);
            nextPing = null;
        }
    }
}
