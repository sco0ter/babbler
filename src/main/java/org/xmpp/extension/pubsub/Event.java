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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "event", namespace = PubSub.EVENT_NAMESPACE)
@XmlType(namespace = PubSub.EVENT_NAMESPACE)
public final class Event {

    @XmlElement(name = "items", namespace = PubSub.EVENT_NAMESPACE)
    private Items items;

    @XmlElement(name = "retract", namespace = PubSub.EVENT_NAMESPACE)
    private Retract retract;

    @XmlElement(name = "purge", namespace = PubSub.EVENT_NAMESPACE)
    private Purge purge;

    @XmlElement(name = "configuration", namespace = PubSub.EVENT_NAMESPACE)
    private Configuration configuration;

    @XmlElement(name = "delete", namespace = PubSub.EVENT_NAMESPACE)
    private Delete delete;

    @XmlElement(name = "subscription", namespace = PubSub.EVENT_NAMESPACE)
    private Subscription subscription;

    private Event() {
    }

    Event(Delete delete) {
        this.delete = delete;
    }

    Purge getPurge() {
        return purge;
    }

    Items getItems() {
        return items;
    }

    Configuration getConfiguration() {
        return configuration;
    }

    public Delete getDelete() {
        return delete;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}
