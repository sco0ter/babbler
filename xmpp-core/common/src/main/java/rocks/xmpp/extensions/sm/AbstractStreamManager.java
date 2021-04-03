/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

import java.util.Collections;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.extensions.sm.model.StreamManagement;

/**
 * Contains stream management logic which is shared between clients and servers.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0198.html">XEP-0198: Stream Management</a>
 */
public abstract class AbstractStreamManager implements StreamFeatureNegotiator<StreamManagement>, ExtensionProtocol {

    /**
     * 2^32 - 1
     * <p>
     * In the unlikely case that the number of stanzas handled during a stream management session exceeds the number of
     * digits that can be represented by the unsignedInt datatype as specified in XML Schema Part 2 [10] (i.e., 232),
     * the value of 'h' SHALL be reset from 232-1 back to zero (rather than being incremented to 232).
     */
    private static final long MAX_H = 0xFFFFFFFFL;
    /**
     * Keep a private queue of unacknowledged stanzas.
     */
    protected final Queue<Stanza> unacknowledgedStanzas = new ConcurrentLinkedDeque<>();
    /**
     * Guarded by "this".
     */
    protected final AtomicBoolean enabledByClient = new AtomicBoolean();
    private final Session session;
    /**
     * Tracks the count of inbound stanzas, we have received from the server.
     */
    protected long inboundCount = 0;
    /**
     * Guarded by "this".
     */
    protected long acknowledgedStanzaCount = 0;

    protected long outboundCount = 0;

    /**
     * Guarded by "this".
     */
    private boolean enabled = true;
    /**
     * Guarded by "this".
     */
    private Predicate<Stanza> requestStrategy = RequestStrategies.forEachMessageOrEveryXStanzas(3);

    protected AbstractStreamManager(final Session session) {
        this.session = session;
    }

    static long diff(long h, long acknowledgedCount) {
        return h - acknowledgedCount & MAX_H;
    }

    @Override
    public final synchronized boolean isEnabled() {
        return enabled;
    }

    public final synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the request strategy.
     *
     * @return The request strategy.
     * @see RequestStrategies
     */
    public final synchronized Predicate<Stanza> getRequestStrategy() {
        return requestStrategy;
    }

    /**
     * Sets the request strategy.
     *
     * @param requestStrategy The request strategy.
     * @see RequestStrategies
     */
    public final synchronized void setRequestStrategy(Predicate<Stanza> requestStrategy) {
        this.requestStrategy = requestStrategy;
    }

    @Override
    public final Set<String> getFeatures() {
        return Collections.emptySet();
    }

    @Override
    public StreamNegotiationResult processNegotiation(Object element) throws StreamNegotiationException {
        try {
            if (element instanceof StreamManagement.Request) {
                // When an <r/> element ("request") is received, the recipient MUST acknowledge it by sending an <a/> element
                // to the sender containing a value of 'h' that is equal to the number of stanzas handled by the recipient of the <r/> element.
                StreamManagement.Answer answer;
                synchronized (this) {
                    answer = new StreamManagement.Answer(inboundCount);
                }
                session.send(answer);
            } else if (element instanceof StreamManagement.Answer) {
                StreamManagement.Answer answer = (StreamManagement.Answer) element;
                // When receiving an <a/> element with an 'h' attribute,
                // all stanzas whose paired value (X at the time of queueing) is less than or equal to the value of 'h'
                // can be removed from the unacknowledged queue.
                markAcknowledged(answer.getLastHandledStanza());
            }
        } catch (StreamErrorException e) {
            session.closeAsync(e.getError());
            throw new StreamNegotiationException(e);
        }
        return StreamNegotiationResult.IGNORE;
    }

    protected void markAcknowledged(Long h) throws StreamErrorException {
        if (h != null) {
            long x;
            synchronized (this) {
                x = diff(h, acknowledgedStanzaCount);
                // When a party receives an <a/> element, it SHOULD keep a record of the 'h' value returned as the
                // sequence number of the last handled outbound stanza for the current stream (and discard the previous value).
                acknowledgedStanzaCount = h;
                if (h > outboundCount) {
                    throw new StreamErrorException(new StreamError(Condition.UNDEFINED_CONDITION, "", Locale.ENGLISH, new StreamManagement.HandledCountTooHigh(h, outboundCount)));
                }
            }

            for (long i = 0; i < x; i++) {
                // Remove X stanzas from the head of the queue and mark them as acknowledged.
                Stanza stanza = unacknowledgedStanzas.poll();
                if (stanza != null) {
                    onAcknowledged(stanza);
                }
            }
        }
    }

    protected void onAcknowledged(Stanza stanza) {
    }

    /**
     * Increments the inbound stanza count.
     */
    public synchronized void incrementInboundStanzaCount() {
        inboundCount = inboundCount + 1 & MAX_H;
    }

    /**
     * Marks a stanza as unacknowledged.
     *
     * @param stanza The stanza.
     */
    public synchronized void markUnacknowledged(Stanza stanza) {
        if (enabledByClient.get()) {
            unacknowledgedStanzas.offer(stanza);
            outboundCount = outboundCount + 1 & MAX_H;
        }
    }
}
