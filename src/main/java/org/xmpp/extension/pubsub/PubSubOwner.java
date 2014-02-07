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
import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "pubsub", namespace = "http://jabber.org/protocol/pubsub#owner")
public final class PubSubOwner {
    @XmlElement(name = "affiliations", namespace = "http://jabber.org/protocol/pubsub#owner")
    private Affiliations affiliations;

    @XmlElement(name = "configure", namespace = "http://jabber.org/protocol/pubsub#owner")
    private Configure configure;

    @XmlElement(name = "default", namespace = "http://jabber.org/protocol/pubsub#owner")
    private Default aDefault;

    @XmlElement(name = "delete", namespace = "http://jabber.org/protocol/pubsub#owner")
    private Delete delete;

    @XmlElement(name = "purge", namespace = "http://jabber.org/protocol/pubsub#owner")
    private Purge purge;

    @XmlElement(name = "subscriptions", namespace = "http://jabber.org/protocol/pubsub#owner")
    private Subscriptions subscriptions;

    private PubSubOwner() {
    }

    public PubSubOwner(Delete delete) {
        this.delete = delete;
    }

    public Configure getConfigure() {
        return configure;
    }

    public Default getDefault() {
        return aDefault;
    }

    public Delete getDelete() {
        return delete;
    }

    public Purge getPurge() {
        return purge;
    }

    public Subscriptions getSubscriptions() {
        return subscriptions;
    }


    public Affiliations getAffiliations() {
        return affiliations;
    }
}
