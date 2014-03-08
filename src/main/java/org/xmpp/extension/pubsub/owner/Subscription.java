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

package org.xmpp.extension.pubsub.owner;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public class Subscription {
    @XmlAttribute(name = "node")
    private String node;

    @XmlAttribute(name = "jid")
    private Jid jid;

    @XmlAttribute(name = "subid")
    private String subid;

    @XmlAttribute(name = "subscription")
    private SubscriptionType type;

    @XmlAttribute(name = "expiry")
    private Date expiry;

    @XmlElement(name = "subscribe-options")
    private Options options;

    public Options getOptions() {
        return options;
    }

    public SubscriptionType getType() {
        return type;
    }

    public String getNode() {
        return node;
    }

    public Jid getJid() {
        return jid;
    }

    public String getSubid() {
        return subid;
    }

    public Date getExpiry() {
        return expiry;
    }

    public enum SubscriptionType {
        @XmlEnumValue("none")
        NONE,
        @XmlEnumValue("pending")
        PENDING,
        @XmlEnumValue("subscribed")
        SUBSCRIBED,
        @XmlEnumValue("unconfigured")
        UNCONFIGURED,
    }

    @XmlType(name = "subscription-options")
    public static final class Options {

        @XmlElement(name = "required")
        private String required;

        public boolean isRequired() {
            return required != null;
        }
    }
}
