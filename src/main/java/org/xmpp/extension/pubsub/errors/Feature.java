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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Christian Schudt
 */
@XmlEnum
public enum Feature {

    @XmlEnumValue("access-authorize")
    ACCESS_AUTHORIZE,

    @XmlEnumValue("access-open")
    ACCESS_OPEN,

    @XmlEnumValue("access-presence")
    ACCESS_PRESENCE,

    @XmlEnumValue("access-roster")
    ACCESS_ROSTER,

    @XmlEnumValue("access-whitelist")
    ACCESS_WHITELIST,

    @XmlEnumValue("auto-create")
    AUTO_CREATE,

    @XmlEnumValue("auto-subscribe")
    AUTO_SUBSCRIBE,

    @XmlEnumValue("collections")
    COLLECTIONS,

    @XmlEnumValue("config-node")
    CONFIG_NODE,

    @XmlEnumValue("create-and-configure")
    CREATE_AND_CONFIGURE,

    @XmlEnumValue("create-nodes")
    CREATE_NODES,

    @XmlEnumValue("delete-items")
    DELETE_ITEMS,

    @XmlEnumValue("delete-nodes")
    DELETE_NODES,

    @XmlEnumValue("filtered-notifications")
    FILTERED_NOTIFICATIONS,

    @XmlEnumValue("get-pending")
    GET_PENDING,

    @XmlEnumValue("instant-nodes")
    INSTANT_NODES,

    @XmlEnumValue("item-ids")
    ITEM_IDS,

    @XmlEnumValue("last-published")
    LAST_PUBLISHED,

    @XmlEnumValue("leased-subscription")
    LEASED_SUBSCRIPTION,

    @XmlEnumValue("manage-subscriptions")
    MANAGE_SUBSCRIPTIONS,

    @XmlEnumValue("member-affiliation")
    MEMBER_AFFILIATION,

    @XmlEnumValue("meta-data")
    META_DATA,

    @XmlEnumValue("modify-affiliations")
    MODIFY_AFFILIATIONS,

    @XmlEnumValue("multi-collection")
    MULTI_COLLECTION,

    @XmlEnumValue("multi-subscribe")
    MULTI_SUBSCRIBE,

    @XmlEnumValue("outcast-affiliation")
    OUTCAST_AFFILIATION,

    @XmlEnumValue("persistent-items")
    PERSISTENT_ITEMS,

    @XmlEnumValue("presence-notifications")
    PRESENCE_NOTIFICATIONS,

    @XmlEnumValue("presence-subscribe")
    PRESENCE_SUBSCRIBE,

    @XmlEnumValue("publish")
    PUBLISH,

    @XmlEnumValue("publish-options")
    PUBLISH_OPTIONS,

    @XmlEnumValue("publish-only-affiliation")
    PUBLISH_ONLY_AFFILIATION,

    @XmlEnumValue("publisher-affiliation")
    PUBLISHER_AFFILIATION,

    @XmlEnumValue("purge-nodes")
    PURGE_NODES,

    @XmlEnumValue("retract-items")
    RETRACT_ITEMS,

    @XmlEnumValue("retrieve-affiliations")
    RETRIEVE_AFFILIATIONS,

    @XmlEnumValue("retrieve-default")
    RETRIEVE_DEFAULT,

    @XmlEnumValue("retrieve-items")
    RETRIEVE_ITEMS,

    @XmlEnumValue("retrieve-subscriptions")
    RETRIEVE_SUBSCRIPTIONS,

    @XmlEnumValue("subscribe")
    SUBSCRIBE,

    @XmlEnumValue("subscription-options")
    SUBSCRIPTION_OPTIONS,

    @XmlEnumValue("subscription-notifications")
    SUBSCRIPTION_NOTIFICATIONS

}
