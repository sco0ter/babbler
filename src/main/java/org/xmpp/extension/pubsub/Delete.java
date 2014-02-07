package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * @author Christian Schudt
 */
final class Delete {
    @XmlAttribute(name = "node")
    private String node;

    @XmlElements({
            @XmlElement(name = "redirect", namespace = "http://jabber.org/protocol/pubsub#event"),
            @XmlElement(name = "redirect", namespace = "http://jabber.org/protocol/pubsub#owner")})
    private Redirect redirect;

    private Delete() {

    }

    public Delete(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }

    public Redirect getRedirect() {
        return redirect;
    }
}
