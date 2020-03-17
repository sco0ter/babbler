/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.extensions.pubsub.model.event;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.pubsub.model.Item;
import rocks.xmpp.extensions.pubsub.model.Subscription;
import rocks.xmpp.extensions.pubsub.model.SubscriptionState;
import rocks.xmpp.util.adapters.InstantAdapter;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The implementation of the {@code <event/>} element in the {@code http://jabber.org/protocol/pubsub#event} namespace.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0060.html">XEP-0060: Publish-Subscribe</a>
 * @see <a href="https://xmpp.org/extensions/xep-0060.html#schemas-event">XML Schema</a>
 */
@SuppressWarnings("unused")
@XmlRootElement(name = "event")
public final class Event {

    @XmlElements({
            @XmlElement(name = "configuration", type = Configuration.class),
            @XmlElement(name = "delete", type = Delete.class),
            @XmlElement(name = "items", type = Items.class),
            @XmlElement(name = "purge", type = Purge.class),
            @XmlElement(name = "subscription", type = SubscriptionInfo.class)
    })
    private final PubSubEventChildElement type;

    private Event() {
        this.type = null;
    }

    private Event(PubSubEventChildElement type) {
        this.type = type;
    }

    /**
     * Creates a pub-sub event with a configuration form.
     *
     * @param node          The node (required).
     * @param configuration The configuration form (required).
     * @return The pub-sub event.
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#example-152">Example 152</a>
     */
    public static Event withConfiguration(String node, DataForm configuration) {
        return new Event(new Configuration(node, configuration));
    }

    /**
     * Creates a pub-sub event with delete information.
     *
     * @param node The node (required).
     * @return The pub-sub event.
     * @see #withDeletion(String) (String, URI)
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#example-160">Example 160</a>
     */
    public static Event withDeletion(String node) {
        return new Event(new Delete(node));
    }

    /**
     * Creates a pub-sub event with delete information.
     *
     * @param node        The node (required).
     * @param redirectUri The redirect URI (optional).
     * @return The pub-sub event.
     * @see #withDeletion(String)
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#example-160">Example 160</a>
     */
    public static Event withDeletion(String node, URI redirectUri) {
        return new Event(new Delete(node, redirectUri));
    }

    /**
     * Creates a pub-sub event with a single item.
     *
     * @param node      The node (required).
     * @param payload   The item payload.
     * @param id        The item id (optional).
     * @param publisher The publisher (optional).
     * @return The pub-sub event.
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#example-2">Example 2</a>
     */
    public static Event withItem(String node, Object payload, String id, Jid publisher) {
        return new Event(new Items(node, Collections.singletonList(new ItemElement(Objects.requireNonNull(payload), id, publisher)), null));
    }

    /**
     * Creates a pub-sub event with items.
     *
     * @param node  The node (required).
     * @param items The items.
     * @return The pub-sub event.
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#example-2">Example 2</a>
     */
    public static Event withItems(String node, List<Item> items) {
        return new Event(new Items(node, items, null));
    }

    /**
     * Creates a pub-sub event with delete information.
     *
     * @param node           The node (required).
     * @param deletedItemIds The deleted items' ids.
     * @return The pub-sub event.
     * @see rocks.xmpp.extensions.pubsub.model.PubSubFeature#DELETE_ITEMS
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#publisher-delete-success">7.2.2.1 Delete And Notify</a>
     */
    public static Event withRetractedItems(String node, List<String> deletedItemIds) {
        return new Event(new Items(node, null, deletedItemIds));
    }

    /**
     * Creates a pub-sub event with purge information.
     *
     * @param node The purged node (required).
     * @return The pub-sub event.
     * @see rocks.xmpp.extensions.pubsub.model.PubSubFeature#PURGE_NODES
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#owner-purge"></a>
     */
    public static Event withPurge(String node) {
        return new Event(new Purge(node));
    }

    /**
     * Creates a pub-sub event with subscription information.
     *
     * @param node         The node (required).
     * @param jid          The JID.
     * @param subscription The subscription state.
     * @return The pub-sub event.
     * @see #withSubscription(String, Jid, SubscriptionState, Instant, String)
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#example-172">Example 172</a>
     */
    public static Event withSubscription(String node, Jid jid, SubscriptionState subscription) {
        return new Event(new SubscriptionInfo(node, null, jid, null, subscription));
    }

    /**
     * Creates a pub-sub event with subscription information.
     *
     * @param node         The node (required).
     * @param expiry       The expiration date.
     * @param jid          The JID.
     * @param subid        The sub id.
     * @param subscription The subscription state.
     * @return The pub-sub event.
     * @see #withSubscription(String, Jid, SubscriptionState)
     * @see <a href="https://xmpp.org/extensions/xep-0060.html#example-172">Example 172</a>
     */
    public static Event withSubscription(String node, Jid jid, SubscriptionState subscription, Instant expiry, String subid) {
        return new Event(new SubscriptionInfo(node, expiry, jid, subid, subscription));
    }

    /**
     * Gets the 'node' attribute of the child element.
     *
     * @return The node.
     */
    public final String getNode() {
        return type != null ? type.getNode() : null;
    }

    /**
     * Indicates, whether the event is a configuration change event.
     *
     * @return True, if the configuration has changed.
     * @see #getConfigurationForm()
     */
    public final boolean isConfiguration() {
        return type instanceof Configuration;
    }

    /**
     * Indicates, whether the event is a delete event, i.e. if a node has been deleted.
     *
     * @return True, if a node has been deleted.
     * @see #getRedirectUri()
     */
    public final boolean isDelete() {
        return type instanceof Delete;
    }

    /**
     * Indicates, whether the event is purge event.
     *
     * @return True, if a node has been purged.
     */
    public final boolean isPurge() {
        return type instanceof Purge;
    }

    /**
     * Indicates, whether the has items.
     *
     * @return True, if it has items.
     * @see #getItems()
     */
    public final boolean hasItems() {
        return type instanceof Items && ((Items) type).item != null;
    }

    /**
     * Indicates, whether the event is a retract event.
     *
     * @return True, if items have been retracted.
     * @see #getRetractedItems()
     */
    public final boolean isRetract() {
        return type instanceof Items && ((Items) type).retract != null;
    }

    /**
     * Gets the subscription approval.
     *
     * @return The subscription approval or null, if the event did not include a subscription.
     */
    public final Subscription getSubscription() {
        if (type instanceof SubscriptionInfo) {
            return ((SubscriptionInfo) type);
        }
        return null;
    }

    /**
     * Gets the items of the event.
     *
     * @return The items of the event or an empty list, if the event did not include any items.
     */
    public final List<Item> getItems() {
        if (type instanceof Items && (((Items) type).item) != null) {
            return Collections.unmodifiableList(((Items) type).item);
        }
        return Collections.emptyList();
    }

    /**
     * Gets the retracted (deleted) items.
     *
     * @return The retracted items.
     */
    public final List<String> getRetractedItems() {
        if (type instanceof Items && (((Items) type).retract) != null) {
            return Collections.unmodifiableList(((Items) type).retract.stream().map(retract -> retract.id).collect(Collectors.toList()));
        }
        return Collections.emptyList();
    }

    /**
     * Gets the configuration form.
     *
     * @return The configuration form or null, if the configuration form isn't included.
     * @see #isConfiguration()
     */
    public final DataForm getConfigurationForm() {
        if (type instanceof Configuration) {
            return ((Configuration) type).dataForm;
        }
        return null;
    }

    /**
     * Gets the redirect URI in case the event is a delete event.
     *
     * @return The redirect URI or null, if a redirect URI isn't included.
     * @see #isDelete()
     */
    public final URI getRedirectUri() {
        if (type instanceof Delete && ((Delete) type).redirect != null) {
            return ((Delete) type).redirect.uri;
        }
        return null;
    }

    private static final class Configuration extends PubSubEventChildElement {

        @XmlElementRef
        private final DataForm dataForm;

        private Configuration() {
            this.dataForm = null;
        }

        private Configuration(String node, DataForm dataForm) {
            super(node);
            this.dataForm = dataForm;
        }
    }

    private static final class Delete extends PubSubEventChildElement {

        private final Redirect redirect;

        private Delete() {
            this.redirect = null;
        }

        private Delete(String node) {
            super(node);
            this.redirect = null;
        }

        private Delete(String node, URI redirect) {
            super(node);
            this.redirect = new Redirect(redirect);
        }

        private static final class Redirect {

            @XmlAttribute
            private final URI uri;

            private Redirect() {
                this.uri = null;
            }

            private Redirect(URI uri) {
                this.uri = Objects.requireNonNull(uri);
            }
        }
    }

    private static final class Items extends PubSubEventChildElement {

        private final List<ItemElement> item;

        private final List<Retract> retract;

        private Items() {
            this.item = null;
            this.retract = null;
        }

        private Items(String node, Collection<Item> items, Collection<String> retractedItems) {
            super(Objects.requireNonNull(node));
            if (items != null) {
                this.item = items.stream().map(i -> new ItemElement(i.getPayload(), i.getId(), i.getPublisher())).collect(Collectors.toList());
            } else {
                this.item = null;
            }
            if (retractedItems != null) {
                this.retract = retractedItems.stream().map(Retract::new).collect(Collectors.toList());
            } else {
                this.retract = null;
            }
        }
    }

    private static final class Purge extends PubSubEventChildElement {

        private Purge() {
        }

        private Purge(String node) {
            super(node);
        }
    }

    private static final class Retract {

        @XmlAttribute
        private final String id;

        private Retract() {
            this.id = null;
        }

        private Retract(String id) {
            this.id = Objects.requireNonNull(id);
        }
    }

    private static final class SubscriptionInfo extends PubSubEventChildElement implements Subscription {

        @XmlAttribute
        @XmlJavaTypeAdapter(InstantAdapter.class)
        private final Instant expiry;

        @XmlAttribute
        private final Jid jid;

        @XmlAttribute
        private final String subid;

        @XmlAttribute
        private final SubscriptionState subscription;

        private SubscriptionInfo() {
            this.expiry = null;
            this.jid = null;
            this.subid = null;
            this.subscription = null;
        }

        private SubscriptionInfo(String node, Instant expiry, Jid jid, String subid, SubscriptionState subscription) {
            super(node);
            this.expiry = expiry;
            this.jid = jid;
            this.subid = subid;
            this.subscription = subscription;
        }

        @Override
        public final Jid getJid() {
            return jid;
        }

        @Override
        public final String getSubId() {
            return subid;
        }

        @Override
        public final SubscriptionState getSubscriptionState() {
            return subscription;
        }

        @Override
        public final Instant getExpiry() {
            return expiry;
        }

        @Override
        public final boolean isConfigurationRequired() {
            return false;
        }

        @Override
        public final boolean isConfigurationSupported() {
            return false;
        }
    }

    private static final class ItemElement implements Item {

        @XmlAnyElement(lax = true)
        private final Object object;

        @XmlAttribute
        private final String id;

        @XmlAttribute
        private final Jid publisher;

        private ItemElement() {
            this.object = null;
            this.id = null;
            this.publisher = null;
        }

        private ItemElement(Object object, String id, Jid publisher) {
            this.object = object;
            this.id = id;
            this.publisher = publisher;
        }

        @Override
        public final Object getPayload() {
            return object;
        }

        @Override
        public final String getId() {
            return id;
        }

        @Override
        public final Jid getPublisher() {
            return publisher;
        }
    }

    @XmlTransient
    private abstract static class PubSubEventChildElement {

        @XmlAttribute
        private final String node;

        private PubSubEventChildElement() {
            this.node = null;
        }

        private PubSubEventChildElement(String node) {
            this.node = Objects.requireNonNull(node);
        }

        public final String getNode() {
            return node;
        }
    }
}
