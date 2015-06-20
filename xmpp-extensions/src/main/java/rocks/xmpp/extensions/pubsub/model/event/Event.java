/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <event/>} element in the {@code http://jabber.org/protocol/pubsub#event} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0060.html">XEP-0060: Publish-Subscribe</a>
 * @see <a href="http://xmpp.org/extensions/xep-0060.html#schemas-event">XML Schema</a>
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

    /**
     * Gets the 'node' attribute of the child element.
     *
     * @return The node.
     */
    public String getNode() {
        return type != null ? type.getNode() : null;
    }

    /**
     * Indicates, whether the event is a configuration change event.
     *
     * @return True, if the configuration has changed.
     */
    public boolean isConfiguration() {
        return type instanceof Configuration;
    }

    /**
     * Indicates, whether the event is a delete event.
     *
     * @return True, if a node has been deleted.
     */
    public boolean isDelete() {
        return type instanceof Delete;
    }

    /**
     * Indicates, whether the event is purge event.
     *
     * @return True, if a node has been purged.
     */
    public boolean isPurge() {
        return type instanceof Purge;
    }

    /**
     * Gets the subscription approval.
     *
     * @return The subscription approval or null, if the event did not include a subscription.
     */
    public Subscription getSubscription() {
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
    public List<Item> getItems() {
        if (type instanceof Items) {
            return Collections.unmodifiableList(((Items) type).getItems());
        }
        return Collections.emptyList();
    }

    /**
     * Gets the configuration form.
     *
     * @return The configuration form or null, if the configuration form isn't included.
     * @see #isConfiguration()
     */
    public DataForm getConfigurationForm() {
        if (type instanceof Configuration) {
            return ((Configuration) type).getConfigurationForm();
        }
        return null;
    }

    /**
     * Gets the redirect URI in case the event is a delete event.
     *
     * @return The redirect URI or null, if a redirect URI isn't included.
     * @see #isDelete()
     */
    public URI getRedirectUri() {
        if (type instanceof Delete && ((Delete) type).getRedirect() != null) {
            return ((Delete) type).getRedirect().getUri();
        }
        return null;
    }

    private static final class Configuration extends PubSubEventChildElement {

        @XmlElementRef
        private DataForm dataForm;

        private DataForm getConfigurationForm() {
            return dataForm;
        }
    }

    private static final class Delete extends PubSubEventChildElement {

        private Redirect redirect;

        private Delete() {
        }

        private Delete(String node) {
            super(node);
        }

        private Delete(String node, Redirect redirect) {
            super(node);
            this.redirect = redirect;
        }

        private Redirect getRedirect() {
            return redirect;
        }

        private static final class Redirect {
            @XmlAttribute
            private URI uri;

            private Redirect() {
            }

            private Redirect(URI uri) {
                this.uri = uri;
            }

            private URI getUri() {
                return uri;
            }
        }
    }

    private static final class Items extends PubSubEventChildElement {

        private final List<ItemElement> item = new ArrayList<>();

        @XmlAttribute(name = "max_items")
        private Long maxItems;

        @XmlAttribute
        private String subid;

        private Retract retract;

        private Items() {
        }

        private Items(String node) {
            super(node);
        }

        private Items(String node, long maxItems) {
            super(node);
            this.maxItems = maxItems;
        }

        private Items(String node, ItemElement item) {
            super(node);
            this.item.add(item);
        }

        private List<? extends Item> getItems() {
            return item;
        }

        private Retract getRetract() {
            return retract;
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
        private String node;

        @XmlAttribute
        private Boolean notify;

        private ItemElement item;

        @XmlAttribute
        private String id;

        private Retract() {
        }

        private Retract(String node, ItemElement item, Boolean notify) {
            this.node = node;
            this.item = item;
            this.notify = notify;
        }

        private String getId() {
            return id;
        }
    }

    private static final class SubscriptionInfo extends PubSubEventChildElement implements Subscription {

        @XmlAttribute
        @XmlJavaTypeAdapter(InstantAdapter.class)
        private Instant expiry;

        @XmlAttribute
        private Jid jid;

        @XmlAttribute
        private String subid;

        @XmlAttribute
        private SubscriptionState subscription;

        @Override
        public Jid getJid() {
            return jid;
        }

        @Override
        public String getSubId() {
            return subid;
        }

        @Override
        public SubscriptionState getSubscriptionState() {
            return subscription;
        }

        @Override
        public Instant getExpiry() {
            return expiry;
        }

        @Override
        public boolean isConfigurationRequired() {
            return false;
        }

        @Override
        public boolean isConfigurationSupported() {
            return false;
        }
    }

    private static final class ItemElement implements Item {

        @XmlAnyElement(lax = true)
        private Object object;

        @XmlAttribute
        private String id;

        @XmlAttribute
        private String node;

        @XmlAttribute
        private String publisher;

        private ItemElement() {
        }

        private ItemElement(String id) {
            this.id = id;
        }

        private ItemElement(Object object) {
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
            return node;
        }

        @Override
        public String getPublisher() {
            return publisher;
        }
    }

    private abstract static class PubSubEventChildElement {

        @XmlAttribute
        private String node;

        private PubSubEventChildElement() {
        }

        private PubSubEventChildElement(String node) {
            this.node = node;
        }

        public String getNode() {
            return node;
        }
    }
}
