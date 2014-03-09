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

package org.xmpp.extension.pubsub;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public class SubscriptionInfo extends PubSubChildElement implements Subscription {

    @XmlAttribute(name = "jid")
    private Jid jid;

    @XmlAttribute(name = "subid")
    private String subid;

    @XmlAttribute(name = "subscription")
    private SubscriptionStatus type;

    @XmlAttribute(name = "expiry")
    private Date expiry;

    @XmlElement(name = "subscribe-options")
    private Options options;

    public Options getOptions() {
        return options;
    }

    public SubscriptionStatus getType() {
        return type;
    }

    @Override
    public String getSubId() {
        return subid;
    }

    @Override
    public SubscriptionStatus getSubscriptionStatus() {
        return type;
    }

    @Override
    public Jid getJid() {
        return jid;
    }

    @Override
    public Date getExpiry() {
        return expiry;
    }

    @Override
    public boolean isConfigurationRequired() {
        return options != null && options.isRequired();
    }

    @Override
    public boolean isConfigurationSupported() {
        return options != null;
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
