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

package rocks.xmpp.extensions.sm;

import rocks.xmpp.core.session.NoResponseException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.extensions.sm.model.StreamManagement;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * @author Christian Schudt
 */
public final class StreamManager extends StreamFeatureNegotiator {

    /**
     * 2^32 - 1
     * <p>
     * In the unlikely case that the number of stanzas handled during a stream management session exceeds the number of
     * digits that can be represented by the unsignedInt datatype as specified in XML Schema Part 2 [10] (i.e., 232),
     * the value of 'h' SHALL be reset from 232-1 back to zero (rather than being incremented to 232).
     */
    private static final long MAX_H = 0xFFFFFFFFL;

    private final Lock lock = new ReentrantLock();

    private final Condition resumed = lock.newCondition();

    /**
     * Tracks the count of inbound stanzas, we have received from the server.
     */
    long inboundCount = 0;

    /**
     * Guarded by "this".
     */
    private long acknowledgedStanzaCount = 0;

    /**
     * Guarded by "this".
     */
    private boolean enabledByClient;

    /**
     * Guarded by "this".
     */
    private StreamManagement.Enabled enabled;

    /**
     * Guarded by "this".
     */
    private Predicate<Stanza> requestStrategy = RequestStrategies.forEveryXStanzas(1);

    /**
     * Guarded by "this".
     */
    private boolean couldResume;

    /**
     * Guarded by "this".
     */
    private int preferredMaximumResumptionTime;

    private StreamManager(XmppSession xmppSession) {
        super(xmppSession, StreamManagement.class);
    }

    static long diff(long h, long acknowledgedCount) {
        return h - acknowledgedCount & MAX_H;
    }

    @Override
    public final Status processNegotiation(Object element) throws StreamNegotiationException {
        if (!isEnabled()) {
            return Status.IGNORE;
        }

        Status status = Status.INCOMPLETE;
        if (element instanceof StreamManagement) {
            // Client sets outbound count to zero.
            synchronized (this) {
                acknowledgedStanzaCount = 0;
                enabledByClient = true;
            }
            xmppSession.send(new StreamManagement.Enable(true));
        } else if (element instanceof StreamManagement.Enabled) {
            // In addition, client sets inbound count to zero.
            synchronized (this) {
                inboundCount = 0;
                enabled = (StreamManagement.Enabled) element;
            }
            return Status.SUCCESS;
        } else if (element instanceof StreamManagement.Failed) {
            releaseLock();
            // Stream management errors SHOULD be considered recoverable.
            // Therefore don't throw an exception.
            // Most likely Stream Resumption failed, because the server session timed out.
            // Return Status.INCOMPLETE here, so that SM negotiation can be renegotiated normally.
        } else if (element instanceof StreamManagement.Request) {
            // Server wants to know how many stanzas we have received.
            synchronized (this) {
                xmppSession.send(new StreamManagement.Answer(inboundCount));
            }
        } else if (element instanceof StreamManagement.Answer) {
            StreamManagement.Answer answer = (StreamManagement.Answer) element;
            // When receiving an <a/> element with an 'h' attribute,
            // all stanzas whose paired value (X at the time of queueing) is less than or equal to the value of 'h'
            // can be removed from the unacknowledged queue.
            markAcknowledged(answer.getLastHandledStanza());
        } else if (element instanceof StreamManagement.Resumed) {
            StreamManagement.Resumed resumed = (StreamManagement.Resumed) element;
            markAcknowledged(resumed.getLastHandledStanza());

            synchronized (this) {
                couldResume = true;
            }

            releaseLock();

            return Status.SUCCESS;
        }

        return status;
    }

    private void markAcknowledged(Long h) {
        if (h != null) {
            long x;
            synchronized (this) {
                x = diff(h, acknowledgedStanzaCount);

                // When a party receives an <a/> element, it SHOULD keep a record of the 'h' value returned as the
                // sequence number of the last handled outbound stanza for the current stream (and discard the previous value).
                acknowledgedStanzaCount = h;
            }
            for (long i = 0; i < x; i++) {
                xmppSession.markAcknowledged(xmppSession.getUnacknowledgedStanzas().poll());
            }
        }
    }

    @Override
    public final boolean canProcess(Object element) {
        return element instanceof StreamManagement.Request || element instanceof StreamManagement.Answer || element instanceof StreamManagement.Enabled || element instanceof StreamManagement.Failed || element instanceof StreamManagement.Resumed;
    }

    /**
     * Increments the inbound stanza count.
     */
    public final synchronized void incrementInboundStanzaCount() {
        inboundCount = ++inboundCount & MAX_H;
    }

    /**
     * Marks a stanza as unacknowledged.
     *
     * @param stanza The stanza.
     */
    public synchronized void markUnacknowledged(Stanza stanza) {
        if (enabledByClient) {
            xmppSession.getUnacknowledgedStanzas().offer(stanza);
        }
    }

    public final synchronized Predicate<Stanza> getRequestStrategy() {
        return requestStrategy;
    }

    public final synchronized void setRequestStrategy(Predicate<Stanza> requestStrategy) {
        this.requestStrategy = requestStrategy;
    }

    public final synchronized boolean isActive() {
        return enabled != null;
    }

    private void releaseLock() {
        lock.lock();
        try {
            resumed.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * If the server will allow the stream to be resumed.
     *
     * @return If the server will allow the stream to be resumed.
     */
    public final synchronized boolean isResumable() {
        // If the server will allow the stream to be resumed,
        // it MUST include a 'resume' attribute set to "true" or "1" on the <enabled/> element
        return enabled != null && enabled.isResume();
    }

    /**
     * Gets the "SM-ID".
     * <blockquote>
     * <p>The 'id' attribute defines a unique identifier for purposes of stream management (an "SM-ID").
     * The SM-ID MUST be generated by the server. The client MUST consider the SM-ID to be opaque and therefore MUST NOT assign any semantic meaning to the SM-ID.</p>
     * </blockquote>
     *
     * @return The "SM-ID".
     */
    public final synchronized String getStreamManagementId() {
        return enabled != null ? enabled.getId() : null;
    }

    public boolean resume() throws InterruptedException, NoResponseException {
        if (!isResumable()) {
            return false;
        }
        synchronized (this) {
            couldResume = false;
            // The <resume/> element MUST include a 'previd' attribute whose value is the SM-ID of the former stream and
            // MUST include an 'h' attribute that identifies the sequence number of the last handled stanza
            // sent over the former stream from the server to the client
            xmppSession.send(new StreamManagement.Resume(inboundCount, getStreamManagementId()));
        }
        lock.lock();
        try {
            if (!resumed.await(xmppSession.getConfiguration().getDefaultResponseTimeout(), TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Stream could not be resumed in time.");
            }
        } finally {
            lock.unlock();
        }
        synchronized (this) {
            return couldResume;
        }
    }
}
