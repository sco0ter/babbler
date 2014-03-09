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
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.pubsub.errors.*;
import org.xmpp.extension.pubsub.event.Event;
import org.xmpp.extension.pubsub.owner.PubSubOwner;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "pubsub")
@XmlSeeAlso({Unsupported.class, InvalidJid.class, PresenceSubscriptionRequired.class, NotInRosterGroup.class, ClosedNode.class, PendingSubscription.class, TooManySubscriptions.class, ConfigurationRequired.class, SubIdRequired.class, NotSubscribed.class, NotSubscribed.class, InvalidSubId.class, JidRequired.class, InvalidOptions.class, PayloadTooBig.class, InvalidPayload.class, ItemRequired.class, PayloadRequired.class, ItemForbidden.class, NodeIdRequired.class, MaxItemsExceeded.class, MaxNodesExceeded.class,
        Event.class,
        PubSubOwner.class
})
public final class PubSub {

    @XmlElement(name = "create")
    private Create create;

    @XmlElement(name = "configure")
    private Configure configure;

    @XmlElement(name = "subscribe")
    private Subscribe subscribe;

    @XmlElement(name = "options")
    private Options options;

    @XmlElements({
            @XmlElement(name = "affiliations", type = AffiliationsElement.class),
            @XmlElement(name = "default", type = Default.class),
            @XmlElement(name = "items", type = Items.class),
            @XmlElement(name = "publish", type = Publish.class),
            @XmlElement(name = "retract", type = RetractElement.class),
            @XmlElement(name = "subscription", type = SubscriptionInfo.class),
            @XmlElement(name = "subscriptions", type = Subscriptions.class),
            @XmlElement(name = "unsubscribe", type = Unsubscribe.class),
    })
    private PubSubChildElement type;

    @XmlElement(name = "default")
    private Default aDefault;

    @XmlElement(name = "items")
    private Items items;

    @XmlElement(name = "publish")
    private Publish publish;

    @XmlElement(name = "retract")
    private RetractElement retract;

    @XmlElement(name = "subscription")
    private SubscriptionInfo subscription;

    @XmlElement(name = "subscriptions")
    private Subscriptions subscriptions;

    @XmlElement(name = "unsubscribe")
    private Unsubscribe unsubscribe;


    private PubSub() {
    }

    public PubSub(Create create, Configure configure) {
        this.create = create;
        this.configure = configure;
    }

    public PubSub(Subscribe subscribe, Options options) {
        this.subscribe = subscribe;
        this.options = options;
    }

    private PubSub(Options options) {
        this.options = options;
    }

    private PubSub(Unsubscribe unsubscribe) {
        this.unsubscribe = unsubscribe;
    }

    private PubSub(Default aDefault) {
        this.aDefault = aDefault;
    }

    private PubSub(Items items) {
        this.items = items;
    }

    private PubSub(Publish publish) {
        this.publish = publish;
    }

    public PubSub(RetractElement retract) {
        this.retract = retract;
    }

    public PubSub(Configure configure) {
        this.configure = configure;
    }

    private PubSub(Subscriptions subscriptions) {
        this.subscriptions = subscriptions;
    }

    private PubSub(AffiliationsElement affiliations) {
        this.type = affiliations;
    }

    private PubSub(Subscribe subscribe) {
        this.subscribe = subscribe;
    }

    public static PubSub forSubscriptionsRequest() {
        return forSubscriptionsRequest(null);
    }

    public static PubSub forSubscriptionsRequest(String node) {
        return new PubSub(new Subscriptions(node));
    }

    public static PubSub forAffiliationsRequest() {
        return forAffiliationsRequest(null);
    }

    public static PubSub forAffiliationsRequest(String node) {
        return new PubSub(new AffiliationsElement(node));
    }

    public static PubSub forSubscribe(String node, Jid jid) {
        return new PubSub(new Subscribe(node, jid));
    }

    public static PubSub forSubscriptionOptions(String node, Jid jid) {
        return new PubSub(new Options(node, jid));
    }

    public static PubSub forUnsubscribe(String node, Jid jid) {
        return new PubSub(new Unsubscribe(node, jid));
    }

    public static PubSub forRequestDefault() {
        return forRequestDefault(null);
    }

    public static PubSub forRequestDefault(String node) {
        return new PubSub(new Default(node));
    }

    public static PubSub forRequestItems(String node) {
        return new PubSub(new Items(node));
    }

    public static PubSub forRequestItems(String node, int max) {
        return new PubSub(new Items(node, max));
    }

    public static PubSub forRequestItem(String node, String id) {
        return new PubSub(new Items(node, new ItemElement(id)));
    }

    public static PubSub forPublish(String node, String id, Object item) {
        return new PubSub(new Publish(node, new ItemElement(id, item)));
    }

    public Subscription getSubscription() {
        if (type instanceof SubscriptionInfo) {
            return (SubscriptionInfo) type;
        }
        return null;
    }

    public Options getOptions() {
        return options;
    }

    public Items getItems() {
        if (type instanceof Items) {
            return (Items) type;
        }
        return null;
    }

    public Publish getPublish() {
        if (type instanceof Publish) {
            return (Publish) type;
        }
        return null;
    }

    public DataForm getConfigurationForm() {
        if (configure != null) {
            return configure.getConfigurationForm();
        }
        return null;
    }

    public List<? extends Subscription> getSubscriptions() {
        if (type instanceof Subscriptions) {
            return ((Subscriptions) type).getSubscriptions();
        }
        return null;
    }

    public List<? extends AffiliationNode> getAffiliations() {
        if (type instanceof AffiliationsElement) {
            return ((AffiliationsElement) type).getAffiliations();
        }
        return null;
    }

    public static final class Create {
        @XmlAttribute(name = "node")
        private String node;

        private Create() {
        }

        public Create(String node) {
            this.node = node;
        }
    }

    public static final class Subscribe extends PubSubChildElement {

        @XmlAttribute(name = "jid")
        private Jid jid;


        private Subscribe() {

        }

        public Subscribe(String node, Jid jid) {
            super(node);
            this.jid = jid;
        }
    }

    public static final class Unsubscribe extends PubSubChildElement {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "jid")
        private Jid jid;

        private Unsubscribe() {
        }

        public Unsubscribe(String node, Jid jid) {
            super(node);
            this.jid = jid;
        }
    }

    public static final class Options {
        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlElementRef
        private DataForm dataForm;

        private Options() {
        }

        public Options(String node, Jid jid) {
            this.node = node;
            this.jid = jid;
        }

        public Options(DataForm dataForm) {
            this.dataForm = dataForm;
        }

        public DataForm getDataForm() {
            return dataForm;
        }
    }
}
