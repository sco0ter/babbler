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
import org.xmpp.stanza.Stanza;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <forwarded/>} element in the {@code urn:xmpp:forward:0} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0297.html">XEP-0297: Stanza Forwarding</a>
 * @see <a href="http://xmpp.org/extensions/xep-0297.html#schema">XML Schema</a>
 */
@XmlRootElement
public final class Forwarded {

    static final String NAMESPACE = "urn:xmpp:forward:0";

    @XmlElementRef
    private DelayedDelivery delayedDelivery;

    @XmlElementRefs({@XmlElementRef(type = Message.class), @XmlElementRef(type = Presence.class), @XmlElementRef(type = IQ.class),
            @XmlElementRef(type = org.xmpp.stanza.server.Message.class), @XmlElementRef(type = org.xmpp.stanza.server.Presence.class), @XmlElementRef(type = org.xmpp.stanza.server.IQ.class),})
    private Stanza stanza;

    private Forwarded() {
    }

    /**
     * Creates a forwarded element.
     *
     * @param stanza The stanza to forward.
     */
    public Forwarded(Stanza stanza) {
        this(stanza, null);
    }

    /**
     * Creates a forwarded element.
     *
     * @param delayedDelivery The delayed delivery, which indicates, when the forwarded stanza was originally received by the forwarder.
     * @param stanza          The stanza to forward.
     */
    public Forwarded(Stanza stanza, DelayedDelivery delayedDelivery) {
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
