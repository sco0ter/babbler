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

package org.xmpp.extension.pubsub.owner;

import org.xmpp.Jid;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.pubsub.Affiliation;
import org.xmpp.extension.pubsub.AffiliationNode;
import org.xmpp.extension.pubsub.Subscription;
import org.xmpp.extension.pubsub.SubscriptionState;

import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
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

    public static PubSubOwner forConfiguration(String node) {
        return new PubSubOwner(new Configure(node));
    }

    public static PubSubOwner forConfiguration(String node, DataForm dataForm) {
        return new PubSubOwner(new Configure(node, dataForm));
    }

    public static PubSubOwner forDefaultConfiguration() {
        return new PubSubOwner(new Default());
    }

    public static PubSubOwner forDelete(String node) {
        return new PubSubOwner(new Delete(node));
    }

    public static PubSubOwner forDelete(String node, URI replacementNode) {
        return new PubSubOwner(new Delete(node, new Delete.Redirect(replacementNode)));
    }

    public static PubSubOwner forPurge(String node) {
        return new PubSubOwner(new Purge(node));
    }

    public static PubSubOwner forSubscriptions(String node) {
        return new PubSubOwner(new Subscriptions(node));
    }

    public static PubSubOwner forSubscriptions(String node, org.xmpp.extension.pubsub.Subscription... subscriptions) {
        return new PubSubOwner((new Subscriptions(node))); // TODO
    }

    public static PubSubOwner forAffiliations(String node) {
        return new PubSubOwner((new Affiliations(node)));
    }

    public static PubSubOwner forAffiliations(String node, AffiliationNode... affiliationNodes) {
        return new PubSubOwner((new Affiliations(node))); // TODO
    }

    public DataForm getConfiguration() {
        return type instanceof Configure ? ((Configure) type).getDataForm() : null;
    }

    public DataForm getDefaultConfiguration() {
        return type instanceof Default ? ((Default) type).getDataForm() : null;
    }

    public String getNode() {
        return type != null ? type.getNode() : null;
    }

    public boolean isConfigure() {
        return type instanceof Configure;
    }

    public boolean isDefault() {
        return type instanceof Default;
    }

    public boolean isDelete() {
        return type instanceof Delete;
    }

    public boolean isPurge() {
        return type instanceof Purge;
    }

    public boolean isSubscriptions() {
        return type instanceof Subscriptions;
    }

    public List<? extends AffiliationNode> getAffiliations() {
        if (type instanceof Affiliations) {
            return ((Affiliations) type).getAffiliations();
        }
        return null;
    }

    public URI getRedirectUri() {
        if (type instanceof Delete && ((Delete) type).getRedirect() != null) {
            return ((Delete) type).getRedirect().getUri();
        }
        return null;
    }

    public List<? extends Subscription> getSubscriptions() {
        if (type instanceof Subscriptions) {
            return ((Subscriptions) type).getSubscriptions();
        }
        return null;
    }

    private static final class Affiliations extends PubSubOwnerChildElement {

        @XmlElement(name = "affiliation")
        private List<AffiliationNodeOwner> affiliations = new ArrayList<>();

        private Affiliations() {
            super(null);
        }

        private Affiliations(String node) {
            super(node);
        }

        private List<? extends AffiliationNode> getAffiliations() {
            return affiliations;
        }

        private static final class AffiliationNodeOwner implements AffiliationNode {

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

    /**
     * @author Christian Schudt
     */
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

        private final static class Redirect {
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

            public Options getOptions() {
                return options;
            }

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
