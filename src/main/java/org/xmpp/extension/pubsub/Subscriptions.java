package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class Subscriptions {
    @XmlElements({
            @XmlElement(name = "subscription"),
            @XmlElement(name = "subscription", namespace = PubSub.EVENT_NAMESPACE),
            @XmlElement(name = "subscription", namespace = PubSub.OWNER_NAMESPACE)})
    private List<Subscription> subscriptions;

    @XmlAttribute(name = "node")
    private String node;

    private Subscriptions() {
    }

    public Subscriptions(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }
}
