package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Christian Schudt
 */
final class Affiliations {
    @XmlAttribute(name = "node")
    private String node;

    @XmlElements({
            @XmlElement(name = "affiliation", namespace = "http://jabber.org/protocol/pubsub#event"),
            @XmlElement(name = "affiliation", namespace = "http://jabber.org/protocol/pubsub#owner")})
    private List<Affiliation> affiliations = new CopyOnWriteArrayList<>();

    public Affiliations() {

    }

    public Affiliations(String node) {
        this.node = node;
    }

    public List<Affiliation> getAffiliations() {
        return affiliations;
    }

    public String getNode() {
        return node;
    }
}