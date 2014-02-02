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

package org.xmpp.extension.pubsub.errors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <unsupported/>} pubsub error.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "unsupported")
public final class Unsupported {

    @XmlAttribute(name = "feature")
    private Feature feature;

    private Unsupported() {
    }

    /**
     * Gets the unsupported feature.
     *
     * @return The unsupported feature.
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * Represents a pubsub feature.
     */
    @XmlEnum
    public enum Feature {
        /**
         * The "access-authorize" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#accessmodels">4.5 Node Access Models</a>
         */
        @XmlEnumValue("access-authorize")
        ACCESS_AUTHORIZE,
        /**
         * The "access-open" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#accessmodels">4.5 Node Access Models</a>
         */
        @XmlEnumValue("access-open")
        ACCESS_OPEN,
        /**
         * The "access-presence" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#accessmodels">4.5 Node Access Models</a>
         */
        @XmlEnumValue("access-presence")
        ACCESS_PRESENCE,
        /**
         * The "access-roster" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#accessmodels">4.5 Node Access Models</a>
         */
        @XmlEnumValue("access-roster")
        ACCESS_ROSTER,
        /**
         * The "access-whitelist" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#accessmodels">4.5 Node Access Models</a>
         */
        @XmlEnumValue("access-whitelist")
        ACCESS_WHITELIST,
        /**
         * The "auto-create" feature.
         * The service supports auto-creation of nodes on publish to a non-existent node.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish-autocreate">7.1.4 Automatic Node Creation</a>
         */
        @XmlEnumValue("auto-create")
        AUTO_CREATE,
        /**
         * The "auto-subscribe" feature.
         * The service supports auto-subscription to a nodes based on presence subscription.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#auto-subscribe">9.1 Auto-Subscribe</a>
         */
        @XmlEnumValue("auto-subscribe")
        AUTO_SUBSCRIBE,
        /**
         * The "collections" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0248.html">XEP-0248: PubSub Collection Nodes</a>
         */
        @XmlEnumValue("collections")
        COLLECTIONS,
        /**
         * The "config-node" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure">8.2 Configure a Node</a>
         */
        @XmlEnumValue("config-node")
        CONFIG_NODE,
        /**
         * The "create-and-configure" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create-and-configure">8.1.3 Create and Configure a Node</a>
         */
        @XmlEnumValue("create-and-configure")
        CREATE_AND_CONFIGURE,
        /**
         * The "create-nodes" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create">8.1 Create a Node</a>
         */
        @XmlEnumValue("create-nodes")
        CREATE_NODES,
        /**
         * The "delete-items" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-delete">7.2 Delete an Item from a Node</a>
         */
        @XmlEnumValue("delete-items")
        DELETE_ITEMS,
        /**
         * The "delete-nodes" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
         */
        @XmlEnumValue("delete-nodes")
        DELETE_NODES,
        /**
         * The "filtered-notifications" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#filtered-notifications">9.2 Filtered Notifications</a>
         */
        @XmlEnumValue("filtered-notifications")
        FILTERED_NOTIFICATIONS,
        /**
         * The "get-pending" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-subreq">8.6 Manage Subscription Requests</a>
         */
        @XmlEnumValue("get-pending")
        GET_PENDING,
        /**
         * The "instant-nodes" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create">8.1 Create a Node</a>
         */
        @XmlEnumValue("instant-nodes")
        INSTANT_NODES,
        /**
         * The "item-ids" feature.
         */
        @XmlEnumValue("item-ids")
        ITEM_IDS,
        /**
         * The "last-published" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#events">4.3 Event Types</a>
         */
        @XmlEnumValue("last-published")
        LAST_PUBLISHED,
        /**
         * The "leased-subscription" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#impl-leases">12.18 Time-Based Subscriptions (Leases)</a>
         */
        @XmlEnumValue("leased-subscription")
        LEASED_SUBSCRIPTION,
        /**
         * The "manage-subscriptions" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-subscriptions">8.8 Manage Subscriptions</a>
         */
        @XmlEnumValue("manage-subscriptions")
        MANAGE_SUBSCRIPTIONS,
        /**
         * The "member-affiliation" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#affiliations">4.1 Affiliations</a>
         */
        @XmlEnumValue("member-affiliation")
        MEMBER_AFFILIATION,
        /**
         * The "meta-data" feature.
         */
        @XmlEnumValue("meta-data")
        META_DATA,
        /**
         * The "modify-affiliations" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-affiliations">8.9 Manage Affiliations</a>
         */
        @XmlEnumValue("modify-affiliations")
        MODIFY_AFFILIATIONS,
        /**
         * The "multi-collection" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0248.html">XEP-0248: PubSub Collection Nodes</a>
         */
        @XmlEnumValue("multi-collection")
        MULTI_COLLECTION,
        /**
         * The "multi-subscribe" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-subscribe-multi">6.1.6 Multiple Subscriptions</a>
         */
        @XmlEnumValue("multi-subscribe")
        MULTI_SUBSCRIBE,
        /**
         * The "outcast-affiliation" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#affiliations">4.1 Affiliations</a>
         */
        @XmlEnumValue("outcast-affiliation")
        OUTCAST_AFFILIATION,
        /**
         * The "persistent-items" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-error-persistent">6.5.9.4 Persistent Items Not Supported</a>
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-delete-error-persistent">7.2.3.5 Persistent Items Not Supported</a>
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-purge-error-nopersist">8.5.3.3 Node Does Not Persist Items</a>
         */
        @XmlEnumValue("persistent-items")
        PERSISTENT_ITEMS,
        /**
         * The "presence-notifications" feature.
         */
        @XmlEnumValue("presence-notifications")
        PRESENCE_NOTIFICATIONS,
        /**
         * The "presence-subscribe" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#auto-subscribe">9.1 Auto-Subscribe</a>
         */
        @XmlEnumValue("presence-subscribe")
        PRESENCE_SUBSCRIBE,
        /**
         * The "publish" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
         */
        @XmlEnumValue("publish")
        PUBLISH,
        /**
         * The "publish-options" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish-options">7.1.5 Publishing Options</a>
         */
        @XmlEnumValue("publish-options")
        PUBLISH_OPTIONS,
        /**
         * The "publish-only-affiliation" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#affiliations">4.1 Affiliations</a>
         */
        @XmlEnumValue("publish-only-affiliation")
        PUBLISH_ONLY_AFFILIATION,
        /**
         * The "publisher-affiliation" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#affiliations">4.1 Affiliations</a>
         */
        @XmlEnumValue("publisher-affiliation")
        PUBLISHER_AFFILIATION,
        /**
         * The "purge-nodes" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-purge">8.5 Purge All Node Items</a>
         */
        @XmlEnumValue("purge-nodes")
        PURGE_NODES,
        /**
         * The "retract-items" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-delete">7.2 Delete an Item from a Node</a>
         */
        @XmlEnumValue("retract-items")
        RETRACT_ITEMS,
        /**
         * The "retrieve-affiliations" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-affiliations">5.7 Retrieve Affiliations</a>
         */
        @XmlEnumValue("retrieve-affiliations")
        RETRIEVE_AFFILIATIONS,
        /**
         * The "retrieve-default" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-default">8.3 Request Default Node Configuration Options</a>
         */
        @XmlEnumValue("retrieve-default")
        RETRIEVE_DEFAULT,
        /**
         * The "retrieve-default-sub" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscribe-default">6.4 Request Default Subscription Configuration Options</a>
         */
        @XmlEnumValue("retrieve-default-sub")
        RETRIEVE_DEFAULT_SUB,
        /**
         * The "retrieve-items" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve">6.5 Retrieve Items from a Node</a>
         */
        @XmlEnumValue("retrieve-items")
        RETRIEVE_ITEMS,
        /**
         * The "retrieve-subscriptions" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-subscriptions">5.6 Retrieve Subscriptions</a>
         */
        @XmlEnumValue("retrieve-subscriptions")
        RETRIEVE_SUBSCRIPTIONS,
        /**
         * The "subscribe" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-subscribe">6.1 Subscribe to a Node</a>
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-unsubscribe">6.2 Unsubscribe from a Node</a>
         */
        @XmlEnumValue("subscribe")
        SUBSCRIBE,
        /**
         * The "subscription-options" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure">6.3 Configure Subscription Options</a>
         */
        @XmlEnumValue("subscription-options")
        SUBSCRIPTION_OPTIONS,
        /**
         * The "subscription-notifications" feature.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#impl-subchange">12.13 Notification of Subscription State Changes</a>
         */
        @XmlEnumValue("subscription-notifications")
        SUBSCRIPTION_NOTIFICATIONS
    }
}