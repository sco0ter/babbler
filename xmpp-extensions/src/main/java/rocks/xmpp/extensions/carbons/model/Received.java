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

package rocks.xmpp.extensions.carbons.model;

import rocks.xmpp.extensions.forward.model.Forwarded;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <received/>} element in the {@code urn:xmpp:carbons:2} namespace, used to mark a carbon copied message as received.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0280.html">XEP-0280: Message Carbons</a>
 * @see <a href="http://xmpp.org/extensions/xep-0280.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "received")
public final class Received {

    @XmlElementRef
    private Forwarded forwarded;

    private Received() {
    }

    /**
     * @param forwardedMessage The forwarded message.
     */
    public Received(Forwarded forwardedMessage) {
        this.forwarded = forwardedMessage;
    }

    /**
     * Gets the forwarded message.
     *
     * @return The forwarded message.
     */
    public Forwarded getForwardedMessage() {
        return forwarded;
    }
}
