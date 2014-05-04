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

package org.xmpp.extension.forward;

import org.xmpp.extension.delay.DelayedDelivery;
import org.xmpp.stanza.AbstractIQ;
import org.xmpp.stanza.AbstractMessage;
import org.xmpp.stanza.AbstractPresence;
import org.xmpp.stanza.Stanza;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <forwarded/>} element.
 *
 * @author Christian Schudt
 */
@XmlRootElement
public final class Forwarded {

    static final String NAMESPACE = "urn:xmpp:forward:0";

    @XmlElementRef
    private DelayedDelivery delayedDelivery;

    //@XmlElementRefs({@XmlElementRef(type = AbstractMessage.class), @XmlElementRef(type = AbstractPresence.class), @XmlElementRef(type = AbstractIQ.class)})
    private Stanza stanza;

    private Forwarded() {

    }

    /**
     * Creates a forwarded element.
     *
     * @param delayedDelivery The delayed delivery, which indicates, when the forwarded stanza was originally received by the forwarder.
     * @param stanza          The stanza to forward.
     */
    public Forwarded(DelayedDelivery delayedDelivery, Stanza stanza) {
        this.delayedDelivery = delayedDelivery;
        this.stanza = stanza;
    }

    /**
     * Gets the date, when the forwarding entity received the forwarded stanza.
     *
     * @return The delay.
     */
    public DelayedDelivery getDelayedDelivery() {
        return delayedDelivery;
    }

    /**
     * Gets the forwarded stanza.
     *
     * @return The forwarded stanza.
     */
    public Stanza getStanza() {
        return stanza;
    }
}
