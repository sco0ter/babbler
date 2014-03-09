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

package org.xmpp.extension.pubsub.event;

import org.xmpp.Jid;
import org.xmpp.extension.pubsub.Subscription;
import org.xmpp.extension.pubsub.SubscriptionStatus;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Date;

/**
 * @author Christian Schudt
 */
final class SubscriptionInfo extends PubSubEventChildElement implements Subscription {

    @XmlAttribute(name = "expiry")
    private Date expiry;

    @XmlAttribute(name = "jid")
    private Jid jid;

    @XmlAttribute(name = "subid")
    private String subid;

    @XmlAttribute(name = "subscription")
    private SubscriptionStatus subscriptionStatus;

    @Override
    public Jid getJid() {
        return jid;
    }

    @Override
    public String getSubId() {
        return subid;
    }

    @Override
    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    @Override
    public Date getExpiry() {
        return expiry;
    }

    @Override
    public boolean isConfigurationRequired() {
        return false;
    }

    @Override
    public boolean isConfigurationSupported() {
        return false;
    }
}
