package org.xmpp.extension.pubsub.owner;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Christian Schudt
 */
abstract class PubSubOwnerChildElement {

    @XmlAttribute(name = "node")
    private String node;

    PubSubOwnerChildElement() {
    }

    PubSubOwnerChildElement(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }
}
