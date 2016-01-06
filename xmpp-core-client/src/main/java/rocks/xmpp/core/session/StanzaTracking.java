/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

import rocks.xmpp.core.stanza.model.Stanza;

import java.util.function.Consumer;

/**
 * @author Christian Schudt
 */
final class StanzaTracking<S extends Stanza> implements Trackable<S> {

    private final S stanza;

    /**
     * Guarded by this.
     */
    private Consumer<S> onAcknowledged;

    /**
     * Guarded by this.
     */
    private boolean receivedByServer;

    StanzaTracking(S stanza) {
        this.stanza = stanza;
    }

    @Override
    public void onAcknowledged(Consumer<S> onAcknowledged) {
        boolean received;
        synchronized (this) {
            received = receivedByServer;
            this.onAcknowledged = onAcknowledged;
        }
        if (received) {
            onAcknowledged.accept(stanza);
        }
    }

    void receivedByServer() {
        Consumer<S> consumer;
        synchronized (this) {
            receivedByServer = true;
            consumer = onAcknowledged;
        }
        if (consumer != null) {
            consumer.accept(stanza);
        }
    }
}