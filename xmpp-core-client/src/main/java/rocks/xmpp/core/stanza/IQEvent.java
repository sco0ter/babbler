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

package rocks.xmpp.core.stanza;

import rocks.xmpp.core.stanza.model.client.IQ;

/**
 * An IQ event is fired whenever an IQ stanza is received or sent.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see IQListener
 */
public final class IQEvent extends StanzaEvent<IQ> {

    private boolean consumed;

    /**
     * Constructs an IQ event.
     *
     * @param source   The object on which the event initially occurred.
     * @param iq       The IQ stanza.
     * @param incoming True, if the stanza is incoming.
     * @throws IllegalArgumentException if source is null.
     */
    public IQEvent(Object source, IQ iq, boolean incoming) {
        super(source, iq, incoming);
    }

    /**
     * Gets the IQ stanza.
     *
     * @return The IQ stanza.
     */
    public final IQ getIQ() {
        return stanza;
    }

    /**
     * Indicates, if this event has been consumed by an event handler, i.e. if the IQ has been responded to.
     *
     * @return True, if this event has been consumed.
     */
    public final boolean isConsumed() {
        return consumed;
    }

    /**
     * Consumes this event, which basically indicates, that an IQ response has been sent by an event handler.
     */
    public final void consume() {
        consumed = true;
    }
}
