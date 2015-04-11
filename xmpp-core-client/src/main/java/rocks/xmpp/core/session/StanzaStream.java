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

import rocks.xmpp.core.stanza.model.Stanza;

import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Manages stanza streams.
 *
 * @author Christian Schudt
 */
final class StanzaStream {

    private final BlockingQueue<Stanza> stanzas = new LinkedBlockingQueue<>();

    private final Stream<Stanza> stream;

    private volatile boolean closed;

    StanzaStream(XmppSession xmppSession) {
        stream = StreamSupport.stream(new InfiniteQueueSpliterator(), false);
        stream.onClose(() -> {
            xmppSession.inboundStanzaStreams.remove(this);
            closed = true;
        });
    }

    void offer(Stanza e) {
        stanzas.offer(e);
    }

    Stream<Stanza> stream() {
        return stream;
    }

    private final class InfiniteQueueSpliterator extends Spliterators.AbstractSpliterator<Stanza> {

        protected InfiniteQueueSpliterator() {
            super(Long.MAX_VALUE, IMMUTABLE);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Stanza> action) {

            Stanza element = null;
            try {
                while (element == null) {
                    // If the stream has been closed.
                    if (closed) {
                        return false;
                    }
                    // Let's see, if there's some data for me.
                    element = stanzas.poll(1, TimeUnit.SECONDS);
                }
                action.accept(element);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
}
