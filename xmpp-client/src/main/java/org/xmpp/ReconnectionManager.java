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

package org.xmpp;

import org.xmpp.stream.StreamException;
import org.xmpp.stream.errors.Conflict;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;

/**
 * If the connection goes down, this class automatically reconnects, if the user was authenticated.
 * <p>
 * Reconnection is not performed, if the user got disconnected due to an {@code <conflict/>} stream error.
 * </p>
 * The default reconnection strategy is a so called truncated binary exponential back off (as proposed by the XMPP specification),
 * which means that the first reconnection attempt is performed X seconds after the disconnect, where X is between 0 and 60.<br>
 * The second attempt chooses a random number between 0 and 180.<br>
 * The third attempt chooses a random number between 0 and 420.<br>
 * The forth attempt chooses a random number between 0 and 900.<br>
 * The fifth attempt chooses a random number between 0 and 1860.<br>
 * <p>
 * Generally speaking it is <code>2^attempt * 60</code> seconds.
 * <p>
 * You can also {@linkplain #setReconnectionStrategy(ReconnectionStrategy) set} your own reconnection strategy.
 * </p>
 * Use {@link #getNextReconnectionAttempt()} if you want to find out, when the next reconnection attempt will happen.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-reconnect">3.3.  Reconnection</a>
 */
public final class ReconnectionManager extends Manager {

    private final ScheduledExecutorService scheduledExecutorService;

    private final XmppSession xmppSession;

    private ReconnectionStrategy reconnectionStrategy;

    private volatile ScheduledFuture<?> scheduledFuture;

    private volatile Date nextReconnectionAttempt;

    ReconnectionManager(final XmppSession xmppSession) {
        this.xmppSession = xmppSession;
        this.reconnectionStrategy = new TruncatedBinaryExponentialBackOffStrategy(60, 5);

        // Enable by default.
        setEnabled(true);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "XMPP Reconnection Thread");
                thread.setDaemon(true);
                return thread;
            }
        });

        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                switch (e.getStatus()) {
                    case DISCONNECTED:
                        // Reconnect if we were connected or logged in and an exception has occurred, that is not a <conflict/> stream error.
                        if ((!(e.getException() instanceof StreamException) || !(((StreamException) e.getException()).getStreamError().getCondition() instanceof Conflict)) && e.getOldStatus() == XmppSession.Status.AUTHENTICATED) {
                            scheduleReconnection(0);
                        }
                        break;
                    case CONNECTED:
                        cancel();
                        break;
                    case CLOSED:
                        cancel();
                        scheduledExecutorService.shutdown();
                        break;
                }
            }
        });
    }

    /**
     * Cancels the next reconnection attempt.
     */
    private void cancel() {
        if (scheduledFuture != null) {
            // Cancel / unschedule any scheduled reconnection task, if the connection is established (e.g. manually) before the next reconnection attempt.
            scheduledFuture.cancel(false);
            nextReconnectionAttempt = null;
        }
    }

    private void scheduleReconnection(final int attempt) {
        if (isEnabled()) {
            int seconds = reconnectionStrategy.getNextReconnectionAttempt(attempt);
            nextReconnectionAttempt = new Date(System.currentTimeMillis() + seconds * 1000);
            scheduledFuture = scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        xmppSession.reconnect();
                    } catch (IOException | LoginException e1) {
                        scheduleReconnection(attempt + 1);
                    }
                }
            }, seconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Gets the reconnection strategy.
     *
     * @return The reconnection strategy.
     */
    public ReconnectionStrategy getReconnectionStrategy() {
        return reconnectionStrategy;
    }

    /**
     * Sets the reconnection strategy.
     *
     * @param reconnectionStrategy The reconnection strategy.
     */
    public void setReconnectionStrategy(ReconnectionStrategy reconnectionStrategy) {
        this.reconnectionStrategy = reconnectionStrategy;
    }

    /**
     * Gets the date of the next reconnection attempt.
     *
     * @return The next reconnection attempt or null if there is none.
     */
    public Date getNextReconnectionAttempt() {
        return nextReconnectionAttempt;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            cancel();
        }
    }
}
