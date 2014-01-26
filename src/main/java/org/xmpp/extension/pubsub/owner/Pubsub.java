package org.xmpp.extension.pubsub.owner;

import org.xmpp.Jid;
import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class PubSub {
    @XmlElement(name = "affiliations")
    private Affiliations affiliations;

    @XmlElement(name = "configure")
    private Configure configure;

    @XmlElement(name = "default")
    private Default aDefault;

    @XmlElement(name = "delete")
    private Delete delete;

    @XmlElement(name = "purge")
    private Purge purge;

    @XmlElement(name = "subscriptions")
    private Subscriptions subscriptions;

    public static final class Affiliations {
        @XmlElementRef(name = "affiliations")
        private List<Affiliation> affiliations;

        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Affiliation {
        @XmlAttribute(name = "affiliation")
        private Type type;

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlEnum
        public enum Type {
            @XmlEnumValue("member")
            MEMBER,
            @XmlEnumValue("none")
            NONE,
            @XmlEnumValue("outcast")
            OUTCAST,
            @XmlEnumValue("owner")
            OWNER,
            @XmlEnumValue("publisher")
            PUBLISHER,
            @XmlEnumValue("publish-only")
            PUBLISH_ONLY
        }
    }

    public static final class Configure {
        @XmlElementRef
        private DataForm dataForm;

        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Default {
        @XmlElementRef
        private DataForm dataForm;
    }

    public static final class Delete {
        @XmlAttribute(name = "node")
        private String node;

        @XmlElementRef(name = "redirect")
        private Redirect redirect;

    }

    public static final class Purge {
        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Redirect {
        @XmlAttribute(name = "uri")
        private URI uri;
    }

    public static final class Subscriptions {

        @XmlElement(name = "subscription")
        private List<Subscription> subscriptions;

        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Subscription {

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "subscription")
        private Type type;

        @XmlEnum
        public enum Type {
            @XmlEnumValue("none")
            NONE,
            @XmlEnumValue("pending")
            PENDING,
            @XmlEnumValue("subscribed")
            SUBSCRIBED,
            @XmlEnumValue("unconfigured")
            UNCONFIGURED
        }
    }
}
