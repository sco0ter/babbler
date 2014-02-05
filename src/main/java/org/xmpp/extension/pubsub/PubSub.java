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
import org.xmpp.extension.pubsub.errors.*;
import org.xmpp.extension.pubsub.event.Event;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "pubsub")
@XmlSeeAlso({Unsupported.class, InvalidJid.class, PresenceSubscriptionRequired.class, NotInRosterGroup.class, ClosedNode.class, PendingSubscription.class, TooManySubscriptions.class, ConfigurationRequired.class, SubIdRequired.class, NotSubscribed.class, NotSubscribed.class, InvalidSubId.class, JidRequired.class, InvalidOptions.class, PayloadTooBig.class, InvalidPayload.class, ItemRequired.class, PayloadRequired.class, ItemForbidden.class, NodeIdRequired.class, MaxItemsExceeded.class, MaxNodesExceeded.class,
        org.xmpp.extension.pubsub.owner.PubSub.class,
        PubSub.class,
        PubSubEvent.class,
        Purge.class

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

    @XmlElement(name = "affiliations")
    private Affiliations affiliations;

    @XmlElement(name = "default")
    private Default aDefault;

    @XmlElement(name = "items")
    private Items items;

    @XmlElement(name = "publish")
    private Publish publish;

    @XmlElement(name = "retract")
    private Retract retract;

    @XmlElement(name = "subscription")
    private Subscription subscription;

    @XmlElement(name = "subscriptions")
    private Subscription subscriptions;

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

    public PubSub(Options options) {
        this.options = options;
    }

    public PubSub(Unsubscribe unsubscribe) {
        this.unsubscribe = unsubscribe;
    }

    public PubSub(Default aDefault) {
        this.aDefault = aDefault;
    }

    public PubSub(Items items) {
        this.items = items;
    }

    public PubSub(Publish publish) {
        this.publish = publish;
    }

    public PubSub(Retract retract) {
        this.retract = retract;
    }

    public PubSub(Configure configure) {
        this.configure = configure;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public Options getOptions() {
        return options;
    }

    public Items getItems() {
        return items;
    }

    public Publish getPublish() {
        return publish;
    }

    public Configure getConfigure() {
        return configure;
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

    public static final class Configure {
        @XmlAttribute(name = "node")
        private String node;

        @XmlElementRef
        private DataForm dataForm;

        private Configure() {

        }

        public Configure(String node) {
            this.node = node;
        }

        public Configure(String node, DataForm dataForm) {
            this.node = node;
            this.dataForm = dataForm;
        }

        public Configure(DataForm dataForm) {
            this.dataForm = dataForm;
        }

        public DataForm getDataForm() {
            return dataForm;
        }
    }

    public static final class Subscribe {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "jid")
        private Jid jid;


        private Subscribe() {

        }

        public Subscribe(String node, Jid jid) {
            this.node = node;
            this.jid = jid;
        }
    }

    public static final class Unsubscribe {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "jid")
        private Jid jid;


        private Unsubscribe() {

        }

        public Unsubscribe(String node, Jid jid) {
            this.node = node;
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

    public static final class Affiliations {
        @XmlAttribute(name = "node")
        private String node;

        @XmlElement(name = "affiliation")
        private List<Affiliation> affiliations;

        public Affiliations() {

        }

        public Affiliations(String node) {
            this.node = node;
        }

        public List<Affiliation> getAffiliations() {
            return affiliations;
        }
    }

    public static final class Default {
        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "type")
        private Type type;

        private Default() {

        }

        public Default(String node) {
            this.node = node;
        }

        public enum Type {
            @XmlEnumValue("collection")
            COLLECTION,
            @XmlEnumValue("leaf")
            LEAF
        }
    }

    public static final class Publish {
        @XmlAttribute(name = "node")
        private String node;

        @XmlElement
        private Item item;

        private Publish() {

        }

        public Publish(String node, Item item) {
            this.node = node;
            this.item = item;
        }

        public Item getItem() {
            return item;
        }
    }

    @XmlType(name = "test")
    public static final class Retract {
        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "notify")
        private Boolean notify;

        @XmlElement
        private Item item;

        private Retract() {

        }

        public Retract(String node, Item item, Boolean notify) {
            this.node = node;
            this.item = item;
            this.notify = notify;
        }
    }

    @XmlType(name = "items", namespace = "http://jabber.org/protocol/pubsub")
    @XmlRootElement(name = "items")
    public static final class Items {

        private List<Item> items = new ArrayList<>();

        @XmlAttribute(name = "max_items")
        private Long maxItems;

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "subid")
        private String subid;

        private Items() {
        }

        public Items(String node, Long maxItems) {
            this.node = node;
            this.maxItems = maxItems;
        }

        public Items(String node, Item item) {
            this.node = node;
            this.items.add(item);
        }

        public List<Item> getItems() {
            return items;
        }
    }

    public static final class Subscriptions {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "subscription")
        private List<Subscription> subscriptions;

        public Subscriptions() {
        }

        public Subscriptions(String node) {

        }

        public List<Subscription> getSubscriptions() {
            return subscriptions;
        }
    }

}
