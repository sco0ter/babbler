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

package org.xmpp.extension.delayeddelivery;

import org.xmpp.Jid;
import org.xmpp.util.JidAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0203.html">XEP-0203: Delayed Delivery</a>.
 * <blockquote>
 * <p>The XML namespace defined herein is used to provide timestamp information about data stored for later delivery. The most common uses of this namespace are to stamp:</p>
 * <ul>
 * <li>A message that is sent to an offline entity and stored for later delivery (see Best Practices for Handling Offline Messages [3]).</li>
 * <li>The last available presence stanza sent by a connected client to a server.</li>
 * <li>Messages cached by a Multi-User Chat [4] room for delivery to new participants when they join the room.</li>
 * </ul>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "delay")
@XmlAccessorType(XmlAccessType.FIELD)
public final class DelayedDelivery {

    @XmlJavaTypeAdapter(JidAdapter.class)
    @XmlAttribute(name = "from")
    private Jid from;

    @XmlAttribute(name = "stamp")
    private Date timestamp;

    @XmlValue
    private String reason;

    /**
     * Private default constructor for unmarshalling.
     */
    private DelayedDelivery() {
    }

    /**
     * Gets the Jabber ID of the entity that originally sent the XML stanza or that delayed the delivery of the stanza (e.g., the address of a multi-user chat room).
     *
     * @return The entity who originally sent the XML stanza.
     */
    public Jid getFrom() {
        return from;
    }

    /**
     * Gets the he time when the XML stanza was originally sent.
     *
     * @return The time when the XML stanza was originally sent.
     */
    public Date getTimeStamp() {
        return timestamp;
    }

    /**
     * Gets the natural-language description of the reason for the delay.
     *
     * @return The natural-language description of the reason for the delay.
     */
    public String getReason() {
        return reason;
    }
}
