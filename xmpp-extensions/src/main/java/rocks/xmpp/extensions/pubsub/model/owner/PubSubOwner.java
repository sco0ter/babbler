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

package rocks.xmpp.extensions.pubsub.model.owner;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.pubsub.model.Affiliation;
import rocks.xmpp.extensions.pubsub.model.AffiliationState;
import rocks.xmpp.extensions.pubsub.model.Subscription;
import rocks.xmpp.extensions.pubsub.model.SubscriptionState;

import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The implementation of the {@code <pubsub/>} element in the {@code http://jabber.org/protocol/pubsub#owner} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0060.html">XEP-0060: Publish-Subscribe</a>
 * @see <a href="http://xmpp.org/extensions/xep-0060.html#schemas-owner">XML Schema</a>
 */
@XmlRootElement(name = "pubsub")
public final class PubSubOwner {

    @XmlElements({
            @XmlElement(name = "affiliations", type = Affiliations.class),
            @XmlElement(name = "configure", type = Configure.class),
            @XmlElement(name = "default", type = Default.class),
            @XmlElement(name = "delete", type = Delete.class),
            @XmlElement(name = "purge", type = Purge.class),
            @XmlElement(name = "subscriptions", type = Subscriptions.class)
    })
    private PubSubOwnerChildElement type;

    private PubSubOwner() {
    }

    private PubSubOwner(PubSubOwnerChildElement type) {
        this.type = type;
    }

    /**
     * Creates a pubsub element with an {@code <configure/>} child element and a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <configure node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure">8.2 Configure a Node</a>
     */
    public static PubSubOwner withConfigure(String node) {
        return new PubSubOwner(new Configure(node));
    }

    /**
     * Creates a pubsub element with an {@code <configure/>} child element and a 'node' attribute and a configuration form.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <configure node='princely_musings'/>
     *        <x xmlns='jabber:x:data' type='submit'>
     *            <field var='FORM_TYPE' type='hidden'>
     *        ...
     * </pubsub>
     * }
     * </pre>
     *
     * @param dataForm The configuration form.
     * @param node     The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure">8.2.4 Form Submission</a>
     */
    public static PubSubOwner withConfigure(String node, DataForm dataForm) {
        return new PubSubOwner(new Configure(node, dataForm));
    }

    /**
     * Creates a pubsub element with a {@code <default/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <default/>
     * </pubsub>
     * }
     * </pre>
     *
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-default">8.3 Request Default Node Configuration Options</a>
     */
    public static PubSubOwner withDefault() {
        return new PubSubOwner(new Default());
    }

    /**
     * Creates a pubsub element with a {@code <delete/>} child element and a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <delete node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
     */
    public static PubSubOwner withDelete(String node) {
        return new PubSubOwner(new Delete(node));
    }

    /**
     * Creates a pubsub element with a {@code <delete/>} child element, a 'node' attribute and a replacement node.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <delete node='princely_musings'>
     *         <redirect uri='xmpp:hamlet@denmark.lit?;node=blog'/>
     *     </delete>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node            The node.
     * @param replacementNode The replacement node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
     */
    public static PubSubOwner withDelete(String node, URI replacementNode) {
        return new PubSubOwner(new Delete(node, new Delete.Redirect(replacementNode)));
    }

    /**
     * Creates a pubsub element with a {@code <purge/>} child element and a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <purge node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-purge">8.5 Purge All Node Items</a>
     */
    public static PubSubOwner withPurge(String node) {
        return new PubSubOwner(new Purge(node));
    }

    /**
     * Creates a pubsub element with a {@code <subscriptions/>} child element and a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <subscriptions node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-subscriptions">8.8 Manage Subscriptions</a>
     */
    public static PubSubOwner withSubscriptions(String node) {
        return new PubSubOwner(new Subscriptions(node));
    }

    /**
     * Creates a pubsub element with a {@code <subscriptions/>} child element with {@code <subscription/>} elements.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <subscriptions node='princely_musings'>
     *         <subscription jid='bard@shakespeare.lit' subscription='subscribed'/>
     *     </subscriptions>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node          The node.
     * @param subscriptions The subscriptions.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-subscriptions-modify">8.8.2 Modify Subscriptions</a>
     */
    public static PubSubOwner withSubscriptions(String node, Subscription... subscriptions) {
        return new PubSubOwner((new Subscriptions(node))); // TODO
    }

    /**
     * Creates a pubsub element with a {@code <affiliations/>} child element and a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <affiliations node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-affiliations">8.9 Manage Affiliations</a>
     */
    public static PubSubOwner withAffiliations(String node) {
        return new PubSubOwner((new Affiliations(node)));
    }

    /**
     * Creates a pubsub element with a {@code <affiliations/>} child element with {@code <affiliation/>} elements.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
     *     <affiliations node='princely_musings'>
     *         <affiliation jid='bard@shakespeare.lit' affiliation='publisher'/>
     *     </affiliations>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node             The node.
     * @param affiliationNodes The affiliations.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-affiliations-modify">8.9.2 Modify Affiliation</a>
     */
    public static PubSubOwner withAffiliations(String node, Affiliation... affiliationNodes) {
        return new PubSubOwner((new Affiliations(node))); // TODO
    }

    /**
     * Gets the configuration form if the pubsub element contains either a {@code <configure/>} or a {@code <default/>} element.
     *
     * @return The configuration form or null.
     * @see #isConfigure()
     * @see #isDefault()
     */
    public DataForm getConfigurationForm() {
        if (type instanceof Configure) {
            return ((Configure) type).getDataForm();
        } else if (type instanceof Default) {
            return ((Default) type).getDataForm();
        }
        return null;
    }

    /**
     * Gets the node of the child element.
     *
     * @return The node.
     */
    public String getNode() {
        return type != null ? type.getNode() : null;
    }

    /**
     * Indicates, whether this pubsub element contains a 'configure' child element.
     *
     * @return True, if the pubsub element contains a 'configure' child element.
     */
    public boolean isConfigure() {
        return type instanceof Configure;
    }

    /**
     * Indicates, whether this pubsub element contains a 'default' child element.
     *
     * @return True, if the pubsub element contains a 'default' child element.
     */
    public boolean isDefault() {
        return type instanceof Default;
    }

    /**
     * Indicates, whether this pubsub element contains a 'delete' child element.
     *
     * @return True, if the pubsub element contains a 'delete' child element.
     */
    public boolean isDelete() {
        return type instanceof Delete;
    }

    /**
     * Indicates, whether this pubsub element contains a 'purge' child element.
     *
     * @return True, if the pubsub element contains a 'purge' child element.
     */
    public boolean isPurge() {
        return type instanceof Purge;
    }

    /**
     * Indicates, whether this pubsub element contains a 'subscriptions' child element.
     *
     * @return True, if the pubsub element contains a 'subscriptions' child element.
     */
    public boolean isSubscriptions() {
        return type instanceof Subscriptions;
    }

    /**
     * Gets the subscriptions, if this pubsub element contains 'subscriptions' element.
     *
     * @return The subscriptions or null.
     */
    public List<? extends Subscription> getSubscriptions() {
        if (type instanceof Subscriptions) {
            return ((Subscriptions) type).getSubscriptions();
        }
        return null;
    }

    /**
     * Gets the affiliations, if this pubsub element contains 'affiliations' element.
     *
     * @return True, if the pubsub element contains a 'affiliations' child element.
     */
    public List<? extends Affiliation> getAffiliations() {
        if (type instanceof Affiliations) {
            return ((Affiliations) type).getAffiliations();
        }
        return null;
    }

    /**
     * Gets the redirect URI, if this pubsub element contains 'delete' element.
     *
     * @return The redirect URI, if this pubsub element contains 'delete' element.
     */
    public URI getRedirectUri() {
        if (type instanceof Delete && ((Delete) type).getRedirect() != null) {
            return ((Delete) type).getRedirect().getUri();
        }
        return null;
    }

    private static final class Affiliations extends PubSubOwnerChildElement {

        @XmlElement(name = "affiliation")
        private final List<AffiliationNodeOwner> affiliations = new ArrayList<>();

        private Affiliations() {
            super(null);
        }

        private Affiliations(String node) {
            super(node);
        }

        private List<? extends Affiliation> getAffiliations() {
            return affiliations;
        }

        private static final class AffiliationNodeOwner implements Affiliation {

            @XmlAttribute(name = "node")
            private String node;

            @XmlAttribute(name = "affiliation")
            private AffiliationState affiliation;

            @XmlAttribute(name = "jid")
            private Jid jid;

            @Override
            public Jid getJid() {
                return jid;
            }

            @Override
            public AffiliationState getAffiliationState() {
                return affiliation;
            }

            @Override
            public String getNode() {
                return node;
            }
        }
    }

    private static final class Configure extends PubSubOwnerChildElement {

        @XmlElementRef
        private DataForm dataForm;

        private Configure() {
        }

        private Configure(String node) {
            super(node);
        }

        private Configure(String node, DataForm dataForm) {
            super(node);
            this.dataForm = dataForm;
        }

        private Configure(DataForm dataForm) {
            this.dataForm = dataForm;
        }

        private DataForm getDataForm() {
            return dataForm;
        }
    }

    private static final class Default extends PubSubOwnerChildElement {

        @XmlElementRef
        private DataForm dataForm;

        private Default() {
        }

        private DataForm getDataForm() {
            return dataForm;
        }
    }

    private static final class Delete extends PubSubOwnerChildElement {

        @XmlElement(name = "redirect")
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
            @XmlAttribute(name = "uri")
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

    private static final class Purge extends PubSubOwnerChildElement {

        private Purge() {
        }

        private Purge(String node) {
            super(node);
        }
    }

    private static final class Subscriptions extends PubSubOwnerChildElement {

        @XmlElement(name = "subscription")
        private List<SubscriptionOwner> subscriptions;

        private Subscriptions() {
        }

        private Subscriptions(String node) {
            super(node);
        }

        private List<SubscriptionOwner> getSubscriptions() {
            return subscriptions;
        }

        private static final class SubscriptionOwner implements Subscription {
            @XmlAttribute(name = "node")
            private String node;

            @XmlAttribute(name = "jid")
            private Jid jid;

            @XmlAttribute(name = "subid")
            private String subid;

            @XmlAttribute(name = "subscription")
            private SubscriptionState status;

            @XmlAttribute(name = "expiry")
            private Date expiry;

            @XmlElement(name = "subscribe-options")
            private Options options;

            @Override
            public SubscriptionState getSubscriptionState() {
                return status;
            }

            @Override
            public String getNode() {
                return node;
            }

            @Override
            public Jid getJid() {
                return jid;
            }

            @Override
            public String getSubId() {
                return subid;
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

            private static final class Options {

                @XmlElement(name = "required")
                private String required;

                private boolean isRequired() {
                    return required != null;
                }
            }
        }
    }

    private static abstract class PubSubOwnerChildElement {

        @XmlAttribute(name = "node")
        private String node;

        private PubSubOwnerChildElement() {
        }

        private PubSubOwnerChildElement(String node) {
            this.node = node;
        }

        private String getNode() {
            return node;
        }
    }
}
