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

package rocks.xmpp.core.session;

import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.stream.StreamErrorException;
import rocks.xmpp.core.stream.model.errors.Condition;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * <p>
 * This class is unconditionally thread-safe.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-reconnect">3.3.  Reconnection</a>
 */
public final class ReconnectionManager extends Manager {

    private static final Logger logger = Logger.getLogger(ReconnectionManager.class.getName());

    private final ScheduledExecutorService scheduledExecutorService;

    private ReconnectionStrategy reconnectionStrategy;

    private ScheduledFuture<?> scheduledFuture;

    private Instant nextReconnectionAttempt;

    private ReconnectionManager(final XmppSession xmppSession) {
        super(xmppSession, false);
        this.reconnectionStrategy = ReconnectionStrategy.truncatedBinaryExponentialBackoffStrategy(60, 5);

        // Enable by default.
        setEnabled(true);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(XmppUtils.createNamedThreadFactory("XMPP Reconnection Thread"));
    }


    @Override
    protected final void initialize() {
        xmppSession.addSessionStatusListener(e -> {
            switch (e.getStatus()) {
                case DISCONNECTED:
                    // Reconnect if we were connected or logged in and an exception has occurred, that is not a <conflict/> stream error.
                    if ((!(e.getThrowable() instanceof StreamErrorException) || !(((StreamErrorException) e.getThrowable()).getStreamError().getCondition() == Condition.CONFLICT)) && e.getOldStatus() == XmppSession.Status.AUTHENTICATED) {
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
        });
    }

    /**
     * Cancels the next reconnection attempt.
     */
    private synchronized void cancel() {
        if (scheduledFuture != null) {
            // Cancel / unschedule any scheduled reconnection task, if the connection is established (e.g. manually) before the next reconnection attempt.
            scheduledFuture.cancel(false);
            nextReconnectionAttempt = null;
        }
    }

    private synchronized void scheduleReconnection(final int attempt) {
        if (isEnabled()) {
            long seconds = reconnectionStrategy.getNextReconnectionAttempt(attempt);
            if (attempt == 0) {
                logger.log(Level.FINE, "Disconnect detected. Next reconnection attempt in {0} seconds.", seconds);
            } else {
                logger.log(Level.FINE, "Still disconnected after {0} retries. Next reconnection attempt in {1} seconds.", new Object[]{attempt, seconds});
            }

            nextReconnectionAttempt = Instant.now().plusSeconds(seconds);
            scheduledFuture = scheduledExecutorService.schedule(() -> {
                try {
                    xmppSession.connect();
                    logger.log(Level.FINE, "Reconnection successful.");
                } catch (XmppException e) {
                    logger.log(Level.FINE, "Reconnection failed.", e);
                    scheduleReconnection(attempt + 1);
                }
            }, seconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Gets the reconnection strategy.
     *
     * @return The reconnection strategy.
     */
    public final synchronized ReconnectionStrategy getReconnectionStrategy() {
        return reconnectionStrategy;
    }

    /**
     * Sets the reconnection strategy.
     *
     * @param reconnectionStrategy The reconnection strategy.
     */
    public final synchronized void setReconnectionStrategy(ReconnectionStrategy reconnectionStrategy) {
        this.reconnectionStrategy = reconnectionStrategy;
    }

    /**
     * Gets the date of the next reconnection attempt.
     *
     * @return The next reconnection attempt or null if there is none.
     */
    public final synchronized Instant getNextReconnectionAttempt() {
        return nextReconnectionAttempt;
    }

    @Override
    public final void onDisable() {
        super.onDisable();
        cancel();
    }
}
