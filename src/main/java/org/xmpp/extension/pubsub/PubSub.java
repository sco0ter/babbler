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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

    static final String NAMESPACE = "http://jabber.org/protocol/pubsub";

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
            @XmlElement(name = "retract", type = Retract.class),
            @XmlElement(name = "subscription", type = SubscriptionInfo.class),
            @XmlElement(name = "subscriptions", type = Subscriptions.class),
            @XmlElement(name = "unsubscribe", type = Unsubscribe.class),
    })
    private PubSubChildElement type;

    private PubSub() {
    }

    private PubSub(Create create, Configure configure) {
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

    private PubSub(PubSubChildElement pubSubChildElement) {
        this.type = pubSubChildElement;
    }

    private PubSub(Configure configure) {
        this.configure = configure;
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

    public static PubSub forSubscribe(String node, Jid jid, DataForm dataForm) {
        return new PubSub(new Subscribe(node, jid), new Options(dataForm));
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

    public static PubSub forRequestItems(String node, String... ids) {
        List<ItemElement> items = new ArrayList<>();
        for (String id : ids) {
            items.add(new ItemElement(id));
        }
        return new PubSub(new Items(node, items));
    }

    public static PubSub forRequestItems(String node, int max) {
        return new PubSub(new Items(node, max));
    }

    public static PubSub forPublish(String node, String id, Object item) {
        return new PubSub(new Publish(node, new ItemElement(id, item)));
    }

    public static PubSub forRetract(String node, String id, boolean notify) {
        return new PubSub(new Retract(node, new ItemElement(id), notify));
    }

    public static PubSub forRequestSubscriptions() {
        return new PubSub(new Subscriptions());
    }

    public static PubSub forRequestSubscriptions(String node) {
        return new PubSub(new Subscriptions(node));
    }

    public static PubSub forCreate(String node) {
        return new PubSub(new Create(node), null);
    }

    public static PubSub forCreate(String node, DataForm configurationForm) {
        return new PubSub(new Create(node), new Configure(configurationForm));
    }

    public static PubSub forConfiguration() {
        return new PubSub(new Configure());
    }

    public static PubSub forConfiguration(String node, DataForm configurationForm) {
        return new PubSub(new Configure(node, configurationForm));
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

    public List<Item> getItems() {
        if (type instanceof Items) {
            return Collections.unmodifiableList(new ArrayList<>(((Items) type).getItems()));
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

    public List<Subscription> getSubscriptions() {
        if (type instanceof Subscriptions) {
            return Collections.unmodifiableList(new ArrayList<>(((Subscriptions) type).getSubscriptions()));
        }
        return null;
    }

    public List<AffiliationNode> getAffiliations() {
        if (type instanceof AffiliationsElement) {
            return Collections.unmodifiableList(new ArrayList<>(((AffiliationsElement) type).getAffiliations()));
        }
        return null;
    }

    public String getNode() {
        if (type != null) {
            return type.getNode();
        } else if (create != null) {
            return create.node;
        } else if (subscribe != null) {
            return subscribe.getNode();
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

    private static final class AffiliationsElement extends PubSubChildElement {

        @XmlElement(name = "affiliation")
        private List<AffiliationInfo> affiliations = new ArrayList<>();

        private AffiliationsElement() {

        }

        private AffiliationsElement(String node) {
            super(node);
        }

        private List<? extends AffiliationNode> getAffiliations() {
            return affiliations;
        }

        private static final class AffiliationInfo implements AffiliationNode {

            @XmlAttribute(name = "node")
            private String node;

            @XmlAttribute(name = "affiliation")
            private Affiliation affiliation;

            @XmlAttribute(name = "jid")
            private Jid jid;

            @Override
            public Jid getJid() {
                return jid;
            }

            @Override
            public Affiliation getAffiliation() {
                return affiliation;
            }

            @Override
            public String getNode() {
                return node;
            }
        }
    }

    private static final class Default extends PubSubChildElement {

        @XmlAttribute(name = "type")
        private Type type;

        @XmlElementRef
        private DataForm dataForm;

        public Default() {

        }

        private Default(String node) {
            super(node);
        }

        private DataForm getDataForm() {
            return dataForm;
        }

        public enum Type {
            @XmlEnumValue("collection")
            COLLECTION,
            @XmlEnumValue("leaf")
            LEAF
        }
    }

    private static final class Items extends PubSubChildElement {

        @XmlElement(name = "item")
        private List<ItemElement> items = new ArrayList<>();

        @XmlAttribute(name = "max_items")
        private Integer maxItems;

        @XmlAttribute(name = "subid")
        private String subid;

        @XmlElement(name = "retract")
        private Retract retract;

        private Items() {
        }

        private Items(String node) {
            super(node);
        }

        private Items(String node, int maxItems) {
            super(node);
            this.maxItems = maxItems;
        }

        private Items(String node, List<ItemElement> items) {
            super(node);
            this.items.addAll(items);
        }

        private List<? extends Item> getItems() {
            return items;
        }

        private Retract getRetract() {
            return retract;
        }
    }

    static final class Publish extends PubSubChildElement {

        @XmlElement(name = "item")
        private ItemElement item;

        private Publish() {
        }

        private Publish(String node, ItemElement item) {
            super(node);
            this.item = item;
        }

        public ItemElement getItem() {
            return item;
        }
    }

    private static final class Retract extends PubSubChildElement {

        @XmlAttribute(name = "notify")
        private Boolean notify;

        @XmlElement
        private ItemElement item;

        @XmlAttribute
        private String id;

        private Retract() {

        }

        private Retract(String node, ItemElement item, Boolean notify) {
            super(node);
            this.item = item;
            this.notify = notify;
        }

        private String getId() {
            return id;
        }
    }

    private static final class SubscriptionInfo extends PubSubChildElement implements Subscription {

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "subid")
        private String subid;

        @XmlAttribute(name = "subscription")
        private SubscriptionState type;

        @XmlAttribute(name = "expiry")
        private Date expiry;

        @XmlElement(name = "subscribe-options")
        private Options options;

        public Options getOptions() {
            return options;
        }

        public SubscriptionState getType() {
            return type;
        }

        @Override
        public String getSubId() {
            return subid;
        }

        @Override
        public SubscriptionState getSubscriptionState() {
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

    private static final class Subscriptions extends PubSubChildElement {

        @XmlElement(name = "subscription")
        private List<SubscriptionInfo> subscriptions = new ArrayList<>();

        private Subscriptions() {
        }

        public Subscriptions(String node) {
            super(node);
        }

        public List<? extends Subscription> getSubscriptions() {
            return subscriptions;
        }
    }

    private static final class Unsubscribe extends PubSubChildElement {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "jid")
        private Jid jid;

        private Unsubscribe() {
        }

        private Unsubscribe(String node, Jid jid) {
            super(node);
            this.jid = jid;
        }
    }

    private static final class Configure {
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

        public DataForm getConfigurationForm() {
            return dataForm;
        }

        public String getNode() {
            return node;
        }
    }

    static final class ItemElement implements Item {

        @XmlAnyElement
        private Object object;

        @XmlAttribute(name = "id")
        private String id;

        private ItemElement() {
        }

        ItemElement(String id) {
            this.id = id;
        }

        private ItemElement(String id, Object object) {
            this.id = id;
            this.object = object;
        }

        @Override
        public Object getPayload() {
            return object;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getNode() {
            return null;
        }

        @Override
        public String getPublisher() {
            return null;
        }

        public Object getObject() {
            return object;
        }
    }

    private static abstract class PubSubChildElement {

        @XmlAttribute(name = "node")
        private String node;

        PubSubChildElement() {
        }

        PubSubChildElement(String node) {
            this.node = node;
        }

        public String getNode() {
            return node;
        }
    }
}
