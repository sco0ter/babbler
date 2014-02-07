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
               @XmlElement(name = "subscription", namespace = "http://jabber.org/protocol/pubsub#event"),
               @XmlElement(name = "subscription", namespace = "http://jabber.org/protocol/pubsub#owner")})
    private List<Subscription> subscriptions;

    @XmlAttribute(name = "node")
    private String node;

    public String getNode() {
        return node;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }
}
