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

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class ReconnectionManager extends Manager {

    private static final Logger logger = Logger.getLogger(ReconnectionManager.class.getName());

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
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                switch (e.getStatus()) {
                    case DISCONNECTED:
                        // Reconnect if we were connected or logged in and an exception has occurred.
                        if (e.getOldStatus() == XmppSession.Status.CONNECTED || e.getOldStatus() == XmppSession.Status.AUTHENTICATED) {
                            scheduleReconnection(0);
                        }
                        break;
                    case CONNECTED:
                        if (scheduledFuture != null) {
                            // Cancel / unschedule any scheduled reconnection task, if the connection is established (e.g. manually) before the next reconnection attempt.
                            scheduledFuture.cancel(false);
                            nextReconnectionAttempt = null;
                        }
                        break;
                    case CLOSED:
                        scheduledExecutorService.shutdown();
                        break;
                }
            }
        });
    }

    private void scheduleReconnection(final int attempt) {
        if (isEnabled()) {
            int seconds = reconnectionStrategy.getNextReconnectionAttempt(attempt);
            nextReconnectionAttempt = new Date(System.currentTimeMillis() + seconds * 1000);
            System.out.println("Reconnecting in: " + seconds);
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

    public Date getNextReconnectionAttempt() {
        return nextReconnectionAttempt;
    }
}
