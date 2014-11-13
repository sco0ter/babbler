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

package rocks.xmpp.extensions.pubsub.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stanza.model.AbstractMessage;
import rocks.xmpp.extensions.data.model.DataForm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to build a standard {@link rocks.xmpp.extensions.data.model.DataForm}, which can be used to configure a PubSub node.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0060.html#registrar-formtypes-config">16.4.4 pubsub#node_config FORM_TYPE</a>
 */
public final class NodeConfiguration {

    private static final String FORM_TYPE = "http://jabber.org/protocol/pubsub#node_config";

    private static final String ACCESS_MODEL = "pubsub#access_model";

    private static final String BODY_XSLT = "pubsub#body_xslt";

    private static final String CHILDREN_ASSOCIATION_POLICY = "pubsub#children_association_policy";

    private static final String CHILDREN_ASSOCIATION_WHITELIST = "pubsub#children_association_whitelist";

    private static final String CHILDREN = "pubsub#children";

    private static final String CHILDREN_MAX = "pubsub#children_max";

    private static final String COLLECTION = "pubsub#collection";

    private static final String CONTACT = "pubsub#contact";

    private static final String DATAFORM_XSLT = "pubsub#dataform_xslt";

    private static final String DELIVER_NOTIFICATIONS = "pubsub#deliver_notifications";

    private static final String DELIVER_PAYLOADS = "pubsub#deliver_payloads";

    private static final String DESCRIPTION = "pubsub#description";

    private static final String ITEM_EXPIRE = "pubsub#item_expire";

    private static final String ITEM_REPLY = "pubsub#itemreply";

    private static final String LANGUAGE = "pubsub#language";

    private static final String MAX_ITEMS = "pubsub#max_items";

    private static final String MAX_PAYLOAD_SIZE = "pubsub#max_payload_size";

    private static final String NODE_TYPE = "pubsub#node_type";

    private static final String NOTIFICATION_TYPE = "pubsub#notification_type";

    private static final String NOTIFY_CONFIG = "pubsub#notify_config";

    private static final String NOTIFY_DELETE = "pubsub#notify_delete";

    private static final String NOTIFY_RETRACT = "pubsub#notify_retract";

    private static final String NOTIFY_SUB = "pubsub#notify_sub";

    private static final String PERSIST_ITEMS = "pubsub#persist_items";

    private static final String PRESENCE_BASED_DELIVERY = "pubsub#presence_based_delivery";

    private static final String PUBLISH_MODEL = "pubsub#publish_model";

    private static final String PURGE_OFFLINE = "pubsub#purge_offline";

    private static final String ROSTER_GROUPS_ALLOWED = "pubsub#roster_groups_allowed";

    private static final String SEND_LAST_PUBLISHED_ITEM = "pubsub#send_last_published_item";

    private static final String TEMPSUB = "pubsub#tempsub";

    private static final String SUBSCRIBE = "pubsub#subscribe";

    private static final String TITLE = "pubsub#title";

    private static final String TYPE = "pubsub#type";

    private final DataForm dataForm;

    public NodeConfiguration(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    public static Builder builder() {
        return new Builder();
    }

    public DataForm getDataForm() {
        return dataForm;
    }

    /**
     * Who may subscribe and retrieve items.
     *
     * @return The access model.
     */
    public AccessModel getAccessModel() {
        String value = dataForm.findValue(ACCESS_MODEL);
        if (value != null) {
            return AccessModel.valueOf(value.toUpperCase());
        }
        return null;
    }

    /**
     * The URL of an XSL transformation which can be
     * applied to payloads in order to generate an
     * appropriate message body element.
     *
     * @return The URL.
     */
    public URL getBodyXslt() {
        String value = dataForm.findValue(BODY_XSLT);
        try {
            return value != null ? new URL(value) : null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Who may associate leaf nodes with a collection
     *
     * @return The children association policy.
     */
    public ChildrenAssociationPolicy getChildrenAssociationPolicy() {
        String value = dataForm.findValue(CHILDREN_ASSOCIATION_POLICY);
        if (value != null) {
            return ChildrenAssociationPolicy.valueOf(value.toUpperCase());
        }
        return null;
    }

    /**
     * The list of JIDs that may associate leaf nodes with a collection.
     *
     * @return The whitelist.
     */
    public List<Jid> getChildrenAssociationWhitelist() {
        return dataForm.findValuesAsJid(CHILDREN_ASSOCIATION_WHITELIST);
    }

    /**
     * The child nodes (leaf or collection) associated with a collection.
     *
     * @return The child nodes.
     */
    public List<String> getChildren() {
        return dataForm.findValues(CHILDREN);
    }

    /**
     * The maximum number of child nodes that can be associated with a collection.
     *
     * @return The max child nodes.
     */
    public Integer getChildrenMax() {
        return dataForm.findValueAsInteger(CHILDREN_MAX);
    }

    /**
     * The collection(s) with which a node is affiliated.
     *
     * @return The collection(s).
     */
    public List<String> getCollection() {
        return dataForm.findValues(COLLECTION);
    }

    /**
     * The JIDs of those to contact with questions.
     *
     * @return The contacts.
     */
    public List<Jid> getContact() {
        return dataForm.findValuesAsJid(CONTACT);
    }

    /**
     * The URL of an XSL transformation which can be
     * applied to the payload format in order to generate
     * a valid Data Forms result that the client could
     * display using a generic Data Forms rendering
     * engine.
     *
     * @return The URL.
     */
    public URL getDataformXslt() {
        String value = dataForm.findValue(DATAFORM_XSLT);
        try {
            return value != null ? new URL(value) : null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Whether to deliver event notifications.
     *
     * @return True, if whether event notifications are delivered.
     */
    public Boolean isDeliverNotifications() {
        return dataForm.findValueAsBoolean(DELIVER_NOTIFICATIONS);
    }

    /**
     * Whether to deliver payloads with event notifications; applies only to leaf nodes.
     *
     * @return True, if payloads are delivered.
     */
    public Boolean isDeliverPayloads() {
        return dataForm.findValueAsBoolean(DELIVER_PAYLOADS);
    }

    /**
     * A description of the node.
     *
     * @return The description.
     */
    public String getDescription() {
        return dataForm.findValue(DESCRIPTION);
    }

    /**
     * Number of seconds after which to automatically purge items.
     *
     * @return The seconds.
     */
    public Integer getItemExpire() {
        return dataForm.findValueAsInteger(ITEM_EXPIRE);
    }

    /**
     * Whether owners or publisher should receive replies to items.
     *
     * @return The item reply.
     */
    public ItemReply getItemReply() {
        String value = dataForm.findValue(ITEM_REPLY);
        if (value != null) {
            return ItemReply.valueOf(value.toUpperCase());
        }
        return null;
    }

    /**
     * The default language of the node.
     *
     * @return The language.
     */
    public String getLanguage() {
        return dataForm.findValue(LANGUAGE);
    }

    /**
     * The maximum number of items to persist.
     *
     * @return The max items.
     */
    public Integer getMaxItems() {
        return dataForm.findValueAsInteger(MAX_ITEMS);
    }

    /**
     * The maximum payload size in bytes
     *
     * @return The max payload size.
     */
    public Integer getMaxPayloadSize() {
        return dataForm.findValueAsInteger(MAX_PAYLOAD_SIZE);
    }

    /**
     * Whether the node is a leaf (default) or a collection.
     *
     * @return The node type.
     */
    public NodeType getNodeType() {
        String value = dataForm.findValue(NODE_TYPE);
        if (value != null) {
            return NodeType.valueOf(value.toUpperCase());
        }
        return null;
    }

    /**
     * Specifies the delivery style for notifications.
     *
     * @return The notification type.
     */
    public AbstractMessage.Type getNotificationType() {
        String value = dataForm.findValue(NOTIFICATION_TYPE);
        if (value != null) {
            return AbstractMessage.Type.valueOf(value.toUpperCase());
        }
        return null;
    }

    /**
     * Whether to notify subscribers when the node configuration changes.
     *
     * @return True, if subscribers are notified when the node configuration changes.
     */
    public Boolean isNotifyConfig() {
        return dataForm.findValueAsBoolean(NOTIFY_CONFIG);
    }

    /**
     * Whether to notify subscribers when the node is deleted.
     *
     * @return True, if subscribers are notified when the node is deleted.
     */
    public Boolean isNotifyDelete() {
        return dataForm.findValueAsBoolean(NOTIFY_DELETE);
    }

    /**
     * Whether to notify subscribers when items are removed from the node.
     *
     * @return True, if subscribers are notified when items are removed from the node.
     */
    public Boolean isNotifyRetract() {
        return dataForm.findValueAsBoolean(NOTIFY_RETRACT);
    }

    /**
     * Whether to notify owners about new subscribers and unsubscribes.
     *
     * @return True, if owners are notified about new subscribers and unsubscribes.
     */
    public Boolean isNotifySub() {
        return dataForm.findValueAsBoolean(NOTIFY_SUB);
    }

    /**
     * Whether to persist items to storage.
     *
     * @return True if items are persisted to storage.
     */
    public Boolean isPersistItems() {
        return dataForm.findValueAsBoolean(PERSIST_ITEMS);
    }

    /**
     * Whether to deliver notifications to available users only.
     *
     * @return True, if notifications are only delivered to available users.
     */
    public Boolean isPresenceBasedDelivery() {
        return dataForm.findValueAsBoolean(PRESENCE_BASED_DELIVERY);
    }

    /**
     * The publisher model.
     *
     * @return The publisher model.
     */
    public PublisherModel getPublisherModel() {
        String value = dataForm.findValue(PUBLISH_MODEL);
        if (value != null) {
            return PublisherModel.valueOf(value.toUpperCase());
        }
        return null;
    }

    /**
     * Whether to purge all items when the relevant publisher goes offline.
     *
     * @return True, if all items are purged, when the relevant publisher goes offline.
     */
    public Boolean isPurgeOffline() {
        return dataForm.findValueAsBoolean(PURGE_OFFLINE);
    }

    /**
     * Gets the allowed roster groups.
     *
     * @return The allowed roster groups.
     */
    public List<String> getRosterGroupsAllowed() {
        return dataForm.findValues(ROSTER_GROUPS_ALLOWED);
    }

    /**
     * Gets the value which specifies when to send the last published item.
     *
     * @return When to send the last published item.
     */
    public SendLastPublishedItem getSendLastPublishedItem() {
        String value = dataForm.findValue(SEND_LAST_PUBLISHED_ITEM);
        if (value != null) {
            return SendLastPublishedItem.valueOf(value.toUpperCase());
        }
        return null;
    }

    /**
     * Whether to make all subscriptions temporary, based on subscriber presence.
     *
     * @return True, if subscriptions are temporary.
     */
    public Boolean isTemporarySubscriptions() {
        return dataForm.findValueAsBoolean(TEMPSUB);
    }

    /**
     * Whether to allow subscriptions.
     *
     * @return True, if subscriptions are allowed.
     */
    public Boolean isAllowSubscriptions() {
        return dataForm.findValueAsBoolean(SUBSCRIBE);
    }

    /**
     * A friendly name for the node.
     *
     * @return The title.
     */
    public String getTitle() {
        return dataForm.findValue(TITLE);
    }

    /**
     * The type of node data, usually specified by
     * the namespace of the payload (if any).
     *
     * @return The payload type.
     */
    public String getPayloadType() {
        return dataForm.findValue(TYPE);
    }

    /**
     * A builder to build node configurations.
     */
    public static final class Builder extends DataForm.Builder<Builder> {

        private final List<Jid> contacts = new ArrayList<>();

        private final List<String> rosterGroupsAllowed = new ArrayList<>();

        private AccessModel accessModel;

        private URL bodyXslt;

        private ChildrenAssociationPolicy childrenAssociationPolicy;

        private List<Jid> childrenAssociationWhitelist = new ArrayList<>();

        private List<String> children = new ArrayList<>();

        private Integer childrenMax;

        private List<String> collection = new ArrayList<>();

        private URL dataformXslt;

        private Boolean deliverNotifications;

        private Boolean deliverPayloads;

        private String description;

        private Integer itemExpire;

        private ItemReply itemReply;

        private String language;

        private Integer maxItems;

        private Integer maxPayloadSize;

        private NodeType nodeType;

        private AbstractMessage.Type notificationType;

        private Boolean notifyConfig;

        private Boolean notifyDelete;

        private Boolean notifyRetract;

        private Boolean notifySub;

        private Boolean persistItems;

        private Boolean presenceBasedDelivery;

        private PublisherModel publisherModel;

        private Boolean purgeOffline;

        private SendLastPublishedItem sendLastPublishedItem;

        private Boolean temporarySubscriptions;

        private Boolean allowSubscriptions;

        private String title;

        private String type;

        private Builder() {
        }

        /**
         * Who may subscribe and retrieve items.
         *
         * @param accessModel Who may subscribe and retrieve items.
         * @return The builder.
         */
        public Builder accessModel(AccessModel accessModel) {
            this.accessModel = accessModel;
            return this;
        }

        /**
         * The URL of an XSL transformation which can be
         * applied to payloads in order to generate an
         * appropriate message body element.
         *
         * @param bodyXslt The URL.
         * @return The builder.
         */
        public Builder bodyXslt(URL bodyXslt) {
            this.bodyXslt = bodyXslt;
            return this;
        }

        /**
         * Who may associate leaf nodes with a collection.
         *
         * @param childrenAssociationPolicy Who may associate leaf nodes with a collection.
         * @return The builder.
         */
        public Builder childrenAssociationPolicy(ChildrenAssociationPolicy childrenAssociationPolicy) {
            this.childrenAssociationPolicy = childrenAssociationPolicy;
            return this;
        }

        /**
         * The list of JIDs that may associate leaf nodes with a collection.
         *
         * @param childrenAssociationWhitelist The list of JIDs that may associate leaf nodes with a collection.
         * @return The builder.
         */
        public Builder childrenAssociationWhitelist(List<Jid> childrenAssociationWhitelist) {
            this.childrenAssociationWhitelist.clear();
            if (childrenAssociationWhitelist != null) {
                this.childrenAssociationWhitelist.addAll(childrenAssociationWhitelist);
            }
            return this;
        }

        /**
         * The child nodes (leaf or collection) associated with a collection.
         *
         * @param children The child nodes (leaf or collection) associated with a collection.
         * @return The builder.
         */
        public Builder children(List<String> children) {
            this.children.clear();
            if (children != null) {
                this.children.addAll(children);
            }
            return this;
        }

        /**
         * The maximum number of child nodes that can be associated with a collection.
         *
         * @param childrenMax The maximum number of child nodes that can be associated with a collection.
         * @return The builder.
         */
        public Builder childrenMax(int childrenMax) {
            this.childrenMax = childrenMax;
            return this;
        }

        /**
         * The collection(s) with which a node is affiliated.
         *
         * @param collection The collection(s) with which a node is affiliated.
         * @return The builder.
         */
        public Builder collection(List<String> collection) {
            this.collection.clear();
            if (collection != null) {
                this.collection.addAll(collection);
            }
            return this;
        }

        /**
         * The JIDs of those to contact with questions
         *
         * @param contacts The JIDs of those to contact with questions.
         * @return The builder.
         */
        public Builder contact(List<Jid> contacts) {
            this.contacts.clear();
            if (contacts != null) {
                this.contacts.addAll(contacts);
            }
            return this;
        }

        /**
         * The URL of an XSL transformation which can be
         * applied to the payload format in order to generate
         * a valid Data Forms result that the client could
         * display using a generic Data Forms rendering
         * engine
         *
         * @param dataformXslt The URL.
         * @return The builder.
         */
        public Builder dataformXslt(URL dataformXslt) {
            this.dataformXslt = dataformXslt;
            return this;
        }

        /**
         * Whether to deliver event notifications.
         *
         * @param deliverNotifications Whether to deliver event notifications.
         * @return The builder.
         */
        public Builder deliverNotifications(boolean deliverNotifications) {
            this.deliverNotifications = deliverNotifications;
            return this;
        }

        /**
         * Whether to deliver payloads with event notifications; applies only to leaf nodes.
         *
         * @param deliverPayloads Whether to deliver payloads with event notifications; applies only to leaf nodes.
         * @return The builder.
         */
        public Builder deliverPayloads(boolean deliverPayloads) {
            this.deliverPayloads = deliverPayloads;
            return this;
        }

        /**
         * A description of the node.
         *
         * @param description A description of the node.
         * @return The builder.
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Number of seconds after which to automatically purge items.
         *
         * @param itemExpire Number of seconds after which to automatically purge items.
         * @return The builder.
         */
        public Builder itemExpire(int itemExpire) {
            this.itemExpire = itemExpire;
            return this;
        }

        /**
         * Whether owners or publisher should receive replies to items.
         *
         * @param itemReply Whether owners or publisher should receive replies to items.
         * @return The builder.
         */
        public Builder itemReply(ItemReply itemReply) {
            this.itemReply = itemReply;
            return this;
        }

        /**
         * The default language of the node.
         *
         * @param language The default language of the node.
         * @return The builder.
         */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * The maximum number of items to persist.
         *
         * @param maxItems The maximum number of items to persist.
         * @return The builder.
         */
        public Builder maxItems(int maxItems) {
            this.maxItems = maxItems;
            return this;
        }

        /**
         * The maximum payload size in bytes.
         *
         * @param maxPayloadSize The maximum payload size in bytes.
         * @return The builder.
         */
        public Builder maxPayloadSize(int maxPayloadSize) {
            this.maxPayloadSize = maxPayloadSize;
            return this;
        }

        /**
         * Whether the node is a leaf (default) or a collection.
         *
         * @param nodeType Whether the node is a leaf (default) or a collection.
         * @return The builder.
         */
        public Builder nodeType(NodeType nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        /**
         * Specify the delivery style for notifications.
         *
         * @param notificationType The notification type.
         * @return The builder.
         */
        public Builder notificationType(AbstractMessage.Type notificationType) {
            if (notificationType != AbstractMessage.Type.HEADLINE && notificationType != AbstractMessage.Type.NORMAL) {
                throw new IllegalArgumentException("only 'normal' and 'headline' type allowed.");
            }
            this.notificationType = notificationType;
            return this;
        }

        /**
         * Whether to notify subscribers when the node configuration changes.
         *
         * @param notifyConfig Whether to notify subscribers when the node configuration changes.
         * @return The builder.
         */
        public Builder notifyConfig(boolean notifyConfig) {
            this.notifyConfig = notifyConfig;
            return this;
        }

        /**
         * Whether to notify subscribers when the node is deleted.
         *
         * @param notifyDelete Whether to notify subscribers when the node is deleted.
         * @return The builder.
         */
        public Builder notifyDelete(boolean notifyDelete) {
            this.notifyDelete = notifyDelete;
            return this;
        }

        /**
         * Whether to notify subscribers when items are removed from the node.
         *
         * @param notifyRetract Whether to notify subscribers when items are removed from the node.
         * @return The builder.
         */
        public Builder notifyRetract(boolean notifyRetract) {
            this.notifyRetract = notifyRetract;
            return this;
        }

        /**
         * Whether to notify owners about new subscribers and unsubscribes.
         *
         * @param notifySub Whether to notify owners about new subscribers and unsubscribes.
         * @return The builder.
         */
        public Builder notifySub(boolean notifySub) {
            this.notifySub = notifySub;
            return this;
        }

        /**
         * Whether to persist items to storage.
         *
         * @param persistItems Whether to persist items to storage.
         * @return The builder.
         */
        public Builder persistItems(boolean persistItems) {
            this.persistItems = persistItems;
            return this;
        }

        /**
         * Whether to deliver notifications to available users only.
         *
         * @param presenceBasedDelivery Whether to deliver notifications to available users only.
         * @return The builder.
         */
        public Builder presenceBasedDelivery(boolean presenceBasedDelivery) {
            this.presenceBasedDelivery = presenceBasedDelivery;
            return this;
        }

        /**
         * The publisher model.
         *
         * @param publisherModel The publisher model.
         * @return The builder.
         */
        public Builder publisherModel(PublisherModel publisherModel) {
            this.publisherModel = publisherModel;
            return this;
        }

        /**
         * Whether to purge all items when the relevant publisher goes offline.
         *
         * @param purgeOffline Whether to purge all items when the relevant publisher goes offline.
         * @return The builder.
         */
        public Builder purgeOffline(boolean purgeOffline) {
            this.purgeOffline = purgeOffline;
            return this;
        }

        /**
         * The roster group(s) allowed to subscribe and retrieve items.
         *
         * @param rosterGroupsAllowed The roster group(s) allowed to subscribe and retrieve items.
         * @return The builder.
         */
        public Builder rosterGroupsAllowed(List<String> rosterGroupsAllowed) {
            this.rosterGroupsAllowed.clear();
            if (rosterGroupsAllowed != null) {
                this.rosterGroupsAllowed.addAll(rosterGroupsAllowed);
            }
            return this;
        }

        /**
         * When to send the last published item.
         *
         * @param sendLastPublishedItem When to send the last published item.
         * @return The builder.
         */
        public Builder sendLastPublishedItem(SendLastPublishedItem sendLastPublishedItem) {
            this.sendLastPublishedItem = sendLastPublishedItem;
            return this;
        }

        /**
         * Whether to make all subscriptions temporary, based on subscriber presence.
         *
         * @param temporarySubscriptions Whether to make all subscriptions temporary, based on subscriber presence.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#impl-tempsub">12.4 Temporary Subscriptions</a>
         */
        public Builder temporarySubscriptions(boolean temporarySubscriptions) {
            this.temporarySubscriptions = temporarySubscriptions;
            return this;
        }

        /**
         * Whether to allow subscriptions.
         *
         * @param allowSubscriptions Whether to allow subscriptions.
         * @return The builder.
         */
        public Builder allowSubscriptions(boolean allowSubscriptions) {
            this.allowSubscriptions = allowSubscriptions;
            return this;
        }

        /**
         * A friendly name for the node
         *
         * @param title The title.
         * @return The name.
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * The type of node data, usually specified by the namespace of the payload (if any).
         *
         * @param type The type.
         * @return The builder.
         */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /**
         * Builds the node configuration.
         *
         * @return The data form.
         */
        public NodeConfiguration build() {

            List<DataForm.Field> fields = new ArrayList<>();
            if (accessModel != null) {
                fields.add(DataForm.Field.builder().var(ACCESS_MODEL).value(accessModel.name().toLowerCase()).type(DataForm.Field.Type.LIST_SINGLE).build());
            }
            if (bodyXslt != null) {
                fields.add(DataForm.Field.builder().var(BODY_XSLT).value(bodyXslt.toString()).build());
            }
            if (childrenAssociationPolicy != null) {
                fields.add(DataForm.Field.builder().var(CHILDREN_ASSOCIATION_POLICY).value(childrenAssociationPolicy.name().toLowerCase()).type(DataForm.Field.Type.LIST_SINGLE).build());
            }
            if (!childrenAssociationWhitelist.isEmpty()) {
                fields.add(DataForm.Field.builder().var(CHILDREN_ASSOCIATION_WHITELIST).valuesJid(childrenAssociationWhitelist).build());
            }
            if (!children.isEmpty()) {
                fields.add(DataForm.Field.builder().var(CHILDREN).values(children).build());
            }
            if (childrenMax != null) {
                fields.add(DataForm.Field.builder().var(CHILDREN_MAX).value(childrenMax.toString()).build());
            }
            if (!collection.isEmpty()) {
                fields.add(DataForm.Field.builder().var(COLLECTION).values(collection).build());
            }
            if (!contacts.isEmpty()) {
                fields.add(DataForm.Field.builder().var(CONTACT).valuesJid(contacts).build());
            }
            if (dataformXslt != null) {
                fields.add(DataForm.Field.builder().var(DATAFORM_XSLT).value(dataformXslt.toString()).build());
            }
            if (deliverNotifications != null) {
                fields.add(DataForm.Field.builder().var(DELIVER_NOTIFICATIONS).value(deliverNotifications).build());
            }
            if (deliverPayloads != null) {
                fields.add(DataForm.Field.builder().var(DELIVER_PAYLOADS).value(deliverPayloads).build());
            }
            if (description != null) {
                fields.add(DataForm.Field.builder().var(DESCRIPTION).value(description).build());
            }
            if (itemExpire != null) {
                fields.add(DataForm.Field.builder().var(ITEM_EXPIRE).value(itemExpire.toString()).build());
            }
            if (itemReply != null) {
                fields.add(DataForm.Field.builder().var(ITEM_REPLY).value(itemReply.name().toLowerCase()).type(DataForm.Field.Type.LIST_SINGLE).build());
            }
            if (language != null) {
                fields.add(DataForm.Field.builder().var(LANGUAGE).value(language).type(DataForm.Field.Type.LIST_SINGLE).build());
            }
            if (maxItems != null) {
                fields.add(DataForm.Field.builder().var(MAX_ITEMS).value(maxItems.toString()).build());
            }
            if (maxPayloadSize != null) {
                fields.add(DataForm.Field.builder().var(MAX_PAYLOAD_SIZE).value(maxPayloadSize.toString()).build());
            }
            if (nodeType != null) {
                fields.add(DataForm.Field.builder().var(NODE_TYPE).value(nodeType.name().toLowerCase()).type(DataForm.Field.Type.LIST_SINGLE).build());
            }
            if (notificationType != null) {
                fields.add(DataForm.Field.builder().var(NOTIFICATION_TYPE).value(notificationType.name().toLowerCase()).type(DataForm.Field.Type.LIST_SINGLE).build());
            }
            if (notifyConfig != null) {
                fields.add(DataForm.Field.builder().var(NOTIFY_CONFIG).value(notifyConfig).build());
            }
            if (notifyDelete != null) {
                fields.add(DataForm.Field.builder().var(NOTIFY_DELETE).value(notifyDelete).build());
            }
            if (notifyRetract != null) {
                fields.add(DataForm.Field.builder().var(NOTIFY_RETRACT).value(notifyRetract).build());
            }
            if (notifySub != null) {
                fields.add(DataForm.Field.builder().var(NOTIFY_SUB).value(notifySub).build());
            }
            if (persistItems != null) {
                fields.add(DataForm.Field.builder().var(PERSIST_ITEMS).value(persistItems).build());
            }
            if (presenceBasedDelivery != null) {
                fields.add(DataForm.Field.builder().var(PRESENCE_BASED_DELIVERY).value(presenceBasedDelivery).build());
            }
            if (publisherModel != null) {
                fields.add(DataForm.Field.builder().var(PUBLISH_MODEL).value(publisherModel.name().toLowerCase()).type(DataForm.Field.Type.LIST_SINGLE).build());
            }
            if (purgeOffline != null) {
                fields.add(DataForm.Field.builder().var(PURGE_OFFLINE).value(purgeOffline).build());
            }
            if (!rosterGroupsAllowed.isEmpty()) {
                fields.add(DataForm.Field.builder().var(ROSTER_GROUPS_ALLOWED).values(rosterGroupsAllowed).type(DataForm.Field.Type.LIST_MULTI).build());
            }
            if (sendLastPublishedItem != null) {
                fields.add(DataForm.Field.builder().var(SEND_LAST_PUBLISHED_ITEM).value(sendLastPublishedItem.name().toLowerCase()).type(DataForm.Field.Type.LIST_SINGLE).build());
            }
            if (temporarySubscriptions != null) {
                fields.add(DataForm.Field.builder().var(TEMPSUB).value(temporarySubscriptions).build());
            }
            if (allowSubscriptions != null) {
                fields.add(DataForm.Field.builder().var(SUBSCRIBE).value(allowSubscriptions).build());
            }
            if (title != null) {
                fields.add(DataForm.Field.builder().var(TITLE).value(title).build());
            }
            if (type != null) {
                fields.add(DataForm.Field.builder().var(TYPE).value(type).build());
            }
            fields(fields).formType(FORM_TYPE).type(DataForm.Type.SUBMIT);
            return new NodeConfiguration(new DataForm(this));
        }


        @Override
        protected Builder self() {
            return this;
        }
    }
}