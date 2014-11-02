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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A helper class to build a standard {@link rocks.xmpp.extensions.data.model.DataForm}, which can be used to configure a PubSub node.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0060.html#registrar-formtypes-config">16.4.4 pubsub#node_config FORM_TYPE</a>
 */
public final class NodeConfigurationBuilder {

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

    private AccessModel accessModel;

    private URL bodyXslt;

    private ChildrenAssociationPolicy childrenAssociationPolicy;

    private Jid[] childrenAssociationWhitelist;

    private String[] children;

    private Integer childrenMax;

    private String[] collection;

    private Jid[] contact;

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

    private String[] rosterGroupsAllowed;

    private SendLastPublishedItem sendLastPublishedItem;

    private Boolean temporarySubscriptions;

    private Boolean allowSubscriptions;

    private String title;

    private String type;

    /**
     * Who may subscribe and retrieve items.
     *
     * @param accessModel Who may subscribe and retrieve items.
     * @return The builder.
     */
    public NodeConfigurationBuilder accessModel(AccessModel accessModel) {
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
    public NodeConfigurationBuilder bodyXslt(URL bodyXslt) {
        this.bodyXslt = bodyXslt;
        return this;
    }

    /**
     * Who may associate leaf nodes with a collection.
     *
     * @param childrenAssociationPolicy Who may associate leaf nodes with a collection.
     * @return The builder.
     */
    public NodeConfigurationBuilder childrenAssociationPolicy(ChildrenAssociationPolicy childrenAssociationPolicy) {
        this.childrenAssociationPolicy = childrenAssociationPolicy;
        return this;
    }

    /**
     * The list of JIDs that may associate leaf nodes with a collection.
     *
     * @param childrenAssociationWhitelist The list of JIDs that may associate leaf nodes with a collection.
     * @return The builder.
     */
    public NodeConfigurationBuilder childrenAssociationWhitelist(Jid... childrenAssociationWhitelist) {
        this.childrenAssociationWhitelist = childrenAssociationWhitelist;
        return this;
    }

    /**
     * The child nodes (leaf or collection) associated with a collection.
     *
     * @param children The child nodes (leaf or collection) associated with a collection.
     * @return The builder.
     */
    public NodeConfigurationBuilder children(String... children) {
        this.children = children;
        return this;
    }

    /**
     * The maximum number of child nodes that can be associated with a collection.
     *
     * @param childrenMax The maximum number of child nodes that can be associated with a collection.
     * @return The builder.
     */
    public NodeConfigurationBuilder childrenMax(int childrenMax) {
        this.childrenMax = childrenMax;
        return this;
    }

    /**
     * The collection(s) with which a node is affiliated.
     *
     * @param collection The collection(s) with which a node is affiliated.
     * @return The builder.
     */
    public NodeConfigurationBuilder collection(String... collection) {
        this.collection = collection;
        return this;
    }

    /**
     * The JIDs of those to contact with questions
     *
     * @param contact The JIDs of those to contact with questions.
     * @return The builder.
     */
    public NodeConfigurationBuilder contact(Jid... contact) {
        this.contact = contact;
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
    public NodeConfigurationBuilder dataformXslt(URL dataformXslt) {
        this.dataformXslt = dataformXslt;
        return this;
    }

    /**
     * Whether to deliver event notifications.
     *
     * @param deliverNotifications Whether to deliver event notifications.
     * @return The builder.
     */
    public NodeConfigurationBuilder deliverNotifications(boolean deliverNotifications) {
        this.deliverNotifications = deliverNotifications;
        return this;
    }

    /**
     * Whether to deliver payloads with event notifications; applies only to leaf nodes.
     *
     * @param deliverPayloads Whether to deliver payloads with event notifications; applies only to leaf nodes.
     * @return The builder.
     */
    public NodeConfigurationBuilder deliverPayloads(boolean deliverPayloads) {
        this.deliverPayloads = deliverPayloads;
        return this;
    }

    /**
     * A description of the node.
     *
     * @param description A description of the node.
     * @return The builder.
     */
    public NodeConfigurationBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Number of seconds after which to automatically purge items.
     *
     * @param itemExpire Number of seconds after which to automatically purge items.
     * @return The builder.
     */
    public NodeConfigurationBuilder itemExpire(int itemExpire) {
        this.itemExpire = itemExpire;
        return this;
    }

    /**
     * Whether owners or publisher should receive replies to items.
     *
     * @param itemReply Whether owners or publisher should receive replies to items.
     * @return The builder.
     */
    public NodeConfigurationBuilder itemReply(ItemReply itemReply) {
        this.itemReply = itemReply;
        return this;
    }

    /**
     * The default language of the node.
     *
     * @param language The default language of the node.
     * @return The builder.
     */
    public NodeConfigurationBuilder language(String language) {
        this.language = language;
        return this;
    }

    /**
     * The maximum number of items to persist.
     *
     * @param maxItems The maximum number of items to persist.
     * @return The builder.
     */
    public NodeConfigurationBuilder maxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    /**
     * The maximum payload size in bytes.
     *
     * @param maxPayloadSize The maximum payload size in bytes.
     * @return The builder.
     */
    public NodeConfigurationBuilder maxPayloadSize(int maxPayloadSize) {
        this.maxPayloadSize = maxPayloadSize;
        return this;
    }

    /**
     * Whether the node is a leaf (default) or a collection.
     *
     * @param nodeType Whether the node is a leaf (default) or a collection.
     * @return The builder.
     */
    public NodeConfigurationBuilder nodeType(NodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    /**
     * Specify the delivery style for notifications.
     *
     * @param notificationType The notification type.
     * @return The builder.
     */
    public NodeConfigurationBuilder notificationType(AbstractMessage.Type notificationType) {
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
    public NodeConfigurationBuilder notifyConfig(boolean notifyConfig) {
        this.notifyConfig = notifyConfig;
        return this;
    }

    /**
     * Whether to notify subscribers when the node is deleted.
     *
     * @param notifyDelete Whether to notify subscribers when the node is deleted.
     * @return The builder.
     */
    public NodeConfigurationBuilder notifyDelete(boolean notifyDelete) {
        this.notifyDelete = notifyDelete;
        return this;
    }

    /**
     * Whether to notify subscribers when items are removed from the node.
     *
     * @param notifyRetract Whether to notify subscribers when items are removed from the node.
     * @return The builder.
     */
    public NodeConfigurationBuilder notifyRetract(boolean notifyRetract) {
        this.notifyRetract = notifyRetract;
        return this;
    }

    /**
     * Whether to notify owners about new subscribers and unsubscribes.
     *
     * @param notifySub Whether to notify owners about new subscribers and unsubscribes.
     * @return The builder.
     */
    public NodeConfigurationBuilder notifySub(boolean notifySub) {
        this.notifySub = notifySub;
        return this;
    }

    /**
     * Whether to persist items to storage.
     *
     * @param persistItems Whether to persist items to storage.
     * @return The builder.
     */
    public NodeConfigurationBuilder persistItems(boolean persistItems) {
        this.persistItems = persistItems;
        return this;
    }

    /**
     * Whether to deliver notifications to available users only.
     *
     * @param presenceBasedDelivery Whether to deliver notifications to available users only.
     * @return The builder.
     */
    public NodeConfigurationBuilder presenceBasedDelivery(boolean presenceBasedDelivery) {
        this.presenceBasedDelivery = presenceBasedDelivery;
        return this;
    }

    /**
     * The publisher model.
     *
     * @param publisherModel The publisher model.
     * @return The builder.
     */
    public NodeConfigurationBuilder publisherModel(PublisherModel publisherModel) {
        this.publisherModel = publisherModel;
        return this;
    }

    /**
     * Whether to purge all items when the relevant publisher goes offline.
     *
     * @param purgeOffline Whether to purge all items when the relevant publisher goes offline.
     * @return The builder.
     */
    public NodeConfigurationBuilder purgeOffline(boolean purgeOffline) {
        this.purgeOffline = purgeOffline;
        return this;
    }

    /**
     * The roster group(s) allowed to subscribe and retrieve items.
     *
     * @param rosterGroupsAllowed The roster group(s) allowed to subscribe and retrieve items.
     * @return The builder.
     */
    public NodeConfigurationBuilder rosterGroupsAllowed(String... rosterGroupsAllowed) {
        this.rosterGroupsAllowed = rosterGroupsAllowed;
        return this;
    }

    /**
     * When to send the last published item.
     *
     * @param sendLastPublishedItem When to send the last published item.
     * @return The builder.
     */
    public NodeConfigurationBuilder sendLastPublishedItem(SendLastPublishedItem sendLastPublishedItem) {
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
    public NodeConfigurationBuilder temporarySubscriptions(boolean temporarySubscriptions) {
        this.temporarySubscriptions = temporarySubscriptions;
        return this;
    }

    /**
     * Whether to allow subscriptions.
     *
     * @param allowSubscriptions Whether to allow subscriptions.
     * @return The builder.
     */
    public NodeConfigurationBuilder allowSubscriptions(boolean allowSubscriptions) {
        this.allowSubscriptions = allowSubscriptions;
        return this;
    }

    /**
     * A friendly name for the node
     *
     * @param title The title.
     * @return The name.
     */
    public NodeConfigurationBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * The type of node data, usually specified by the namespace of the payload (if any).
     *
     * @param type The type.
     * @return The builder.
     */
    public NodeConfigurationBuilder type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Builds the data form.
     *
     * @return The data form.
     */
    public DataForm build() {

        List<DataForm.Field> fields = new ArrayList<>();
        if (accessModel != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, ACCESS_MODEL, accessModel.name().toLowerCase()));
        }
        if (bodyXslt != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, BODY_XSLT, bodyXslt.toString()));
        }
        if (childrenAssociationPolicy != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, CHILDREN_ASSOCIATION_POLICY, childrenAssociationPolicy.name().toLowerCase()));
        }
        if (childrenAssociationWhitelist != null && childrenAssociationWhitelist.length > 0) {
            List<String> values = new ArrayList<>();
            for (Jid jid : childrenAssociationWhitelist) {
                values.add(jid.toEscapedString());
            }
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, CHILDREN_ASSOCIATION_WHITELIST, values));
        }
        if (children != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_MULTI, CHILDREN, Arrays.asList(children)));
        }
        if (childrenMax != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, CHILDREN_MAX, childrenMax.toString()));
        }
        if (collection != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_MULTI, COLLECTION, Arrays.asList(collection)));
        }
        if (contact != null) {
            List<String> values = new ArrayList<>();
            for (Jid jid : contact) {
                values.add(jid.toEscapedString());
            }
            fields.add(new DataForm.Field(DataForm.Field.Type.JID_MULTI, CONTACT, values));
        }
        if (dataformXslt != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, DATAFORM_XSLT, dataformXslt.toString()));
        }
        if (deliverNotifications != null) {
            fields.add(new DataForm.Field(DELIVER_NOTIFICATIONS, deliverNotifications));
        }
        if (deliverPayloads != null) {
            fields.add(new DataForm.Field(DELIVER_PAYLOADS, deliverPayloads));
        }
        if (description != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, DESCRIPTION, description));
        }
        if (itemExpire != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, ITEM_EXPIRE, itemExpire.toString()));
        }
        if (itemReply != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, ITEM_REPLY, itemReply.name().toLowerCase()));
        }
        if (language != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, LANGUAGE, language));
        }
        if (maxItems != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, MAX_ITEMS, maxItems.toString()));
        }
        if (maxPayloadSize != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, MAX_PAYLOAD_SIZE, maxPayloadSize.toString()));
        }
        if (nodeType != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, NODE_TYPE, nodeType.name().toLowerCase()));
        }
        if (notificationType != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, NOTIFICATION_TYPE, notificationType.name().toLowerCase()));
        }
        if (notifyConfig != null) {
            fields.add(new DataForm.Field(NOTIFY_CONFIG, notifyConfig));
        }
        if (notifyDelete != null) {
            fields.add(new DataForm.Field(NOTIFY_DELETE, notifyDelete));
        }
        if (notifyRetract != null) {
            fields.add(new DataForm.Field(NOTIFY_RETRACT, notifyRetract));
        }
        if (notifySub != null) {
            fields.add(new DataForm.Field(NOTIFY_SUB, notifySub));
        }
        if (persistItems != null) {
            fields.add(new DataForm.Field(PERSIST_ITEMS, persistItems));
        }
        if (presenceBasedDelivery != null) {
            fields.add(new DataForm.Field(PRESENCE_BASED_DELIVERY, presenceBasedDelivery));
        }
        if (publisherModel != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, PUBLISH_MODEL, publisherModel.name().toLowerCase()));
        }
        if (purgeOffline != null) {
            fields.add(new DataForm.Field(PURGE_OFFLINE, purgeOffline));
        }
        if (rosterGroupsAllowed != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_MULTI, ROSTER_GROUPS_ALLOWED, Arrays.asList(rosterGroupsAllowed)));
        }
        if (sendLastPublishedItem != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, SEND_LAST_PUBLISHED_ITEM, sendLastPublishedItem.name().toLowerCase()));
        }
        if (temporarySubscriptions != null) {
            fields.add(new DataForm.Field(TEMPSUB, temporarySubscriptions));
        }
        if (allowSubscriptions != null) {
            fields.add(new DataForm.Field(SUBSCRIBE, allowSubscriptions));
        }
        if (title != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, TITLE, title));
        }
        if (type != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, TYPE, type));
        }

        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        dataForm.setFormType(FORM_TYPE);
        dataForm.getFields().addAll(fields);
        return dataForm;
    }
}