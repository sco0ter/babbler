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

import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.pubsub.Item;
import org.xmpp.extension.pubsub.Subscription;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "event")
public final class Event {

    @XmlElements({
            @XmlElement(name = "configuration", type = Configuration.class),
            @XmlElement(name = "delete", type = Delete.class),
            @XmlElement(name = "items", type = Items.class),
            @XmlElement(name = "purge", type = Purge.class),
            @XmlElement(name = "subscription", type = SubscriptionInfo.class)
    })
    private PubSubEventChildElement type;

    private Event() {
    }

    private Event(PubSubEventChildElement type) {
        this.type = type;
    }

    public static Event forDelete(String node) {
        return new Event(new Delete(node));
    }

    public String getNode() {
        return type != null ? type.getNode() : null;
    }

    public boolean isConfigure() {
        return type instanceof Configuration;
    }

    public boolean isDelete() {
        return type instanceof Delete;
    }

    public boolean isPurge() {
        return type instanceof Purge;
    }

    public Subscription getSubscription() {
        if (type instanceof SubscriptionInfo) {
            return ((SubscriptionInfo) type);
        }
        return null;
    }

    public List<? extends Item> getItems() {
        if (type instanceof Items) {
            return ((Items) type).getItems();
        }
        return null;
    }

    public DataForm getConfigurationForm() {
        if (type instanceof Configuration) {
            return ((Configuration) type).getConfigurationForm();
        }
        return null;
    }

    public URI getRedirectUri() {
        if (type instanceof Delete && ((Delete) type).getRedirect() != null) {
            return ((Delete) type).getRedirect().getUri();
        }
        return null;
    }
}
