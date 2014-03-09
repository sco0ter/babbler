package org.xmpp.extension.pubsub;

/**
 * @author Christian Schudt
 */

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Christian Schudt
 */
abstract class PubSubChildElement {

    @XmlAttribute(name = "node")
    private String node;

    PubSubChildElement() {
    }

    PubSubChildElement(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }
}
