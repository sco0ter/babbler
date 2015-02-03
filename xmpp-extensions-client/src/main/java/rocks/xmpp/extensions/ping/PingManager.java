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
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;
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
 * For <a href="http://xmpp.org/extensions/xep-0199.html#s2c">Server-To-Client Pings</a> it automatically responds with a result (pong), in enabled.
 * </p>
 * <p>
 * It also allows to ping the server (<a href="http://xmpp.org/extensions/xep-0199.html#c2s">Client-To-Server Pings</a>) or to ping other XMPP entities (<a href="http://xmpp.org/extensions/xep-0199.html#e2e">Client-to-Client Pings</a>).
 * </p>
 *
 * @author Christian Schudt
 */
public final class PingManager extends IQExtensionManager implements SessionStatusListener {

    private static final Logger logger = Logger.getLogger(PingManager.class.getName());

    private final ScheduledExecutorService scheduledExecutorService;

    private volatile ScheduledFuture<?> nextPing;

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
    protected void initialize() {
        xmppSession.addIQHandler(Ping.class, this);
        xmppSession.addSessionStatusListener(this);
    }


    /**
     * Pings the given XMPP entity.
     *
     * @param jid The JID to ping.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public void ping(Jid jid) throws XmppException {
        xmppSession.query(new IQ(jid, IQ.Type.GET, Ping.INSTANCE));
    }

    /**
     * Pings the connected server.
     *
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public void pingServer() throws XmppException {
        ping(null);
    }

    /**
     * Gets the ping interval in seconds. The default ping interval is 900 seconds (15 minutes).
     *
     * @return The ping interval in seconds.
     * @see #setPingInterval(long)
     */
    public synchronized long getPingInterval() {
        return pingInterval;
    }

    /**
     * Sets the automatic ping interval in seconds. Any scheduled future ping is canceled and a new ping is scheduled after the specified interval.
     *
     * @param pingInterval The ping interval in seconds.
     * @see #getPingInterval()
     */
    public synchronized void setPingInterval(long pingInterval) {
        this.pingInterval = pingInterval;
        if (nextPing != null) {
            nextPing.cancel(false);
        }
        startPinging();
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean wasEnabled = isEnabled();
        super.setEnabled(enabled);

        if (enabled && !wasEnabled) {
            startPinging();
        } else if (!enabled && wasEnabled) {
            synchronized (this) {
                if (nextPing != null) {
                    nextPing.cancel(false);
                }
            }
        }
    }

    private synchronized void startPinging() {
        if (pingInterval > 0 && !scheduledExecutorService.isShutdown()) {
            nextPing = scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    if (isEnabled() && xmppSession.getStatus() == XmppSession.Status.AUTHENTICATED) {
                        try {
                            pingServer();
                        } catch (XmppException e) {
                            logger.log(Level.WARNING, "Pinging server failed.", e);
                        }
                    }
                    nextPing = scheduledExecutorService.schedule(this, pingInterval, TimeUnit.SECONDS);
                }
            }, pingInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    protected IQ processRequest(final IQ iq) {
        return iq.createResult();
    }

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            // Shutdown the ping executor service and cancel the next ping.
            synchronized (PingManager.this) {
                if (nextPing != null) {
                    nextPing.cancel(false);
                }
                nextPing = null;
                scheduledExecutorService.shutdown();
            }
        }
    }
}
