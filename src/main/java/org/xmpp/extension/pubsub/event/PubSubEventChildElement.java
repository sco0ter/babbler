package org.xmpp.extension.pubsub.event;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Christian Schudt
 */
abstract class PubSubEventChildElement {

    @XmlAttribute(name = "node")
    private String node;

    PubSubEventChildElement() {
    }

    PubSubEventChildElement(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }
}
