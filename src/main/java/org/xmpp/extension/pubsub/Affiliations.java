package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Christian Schudt
 */
final class Affiliations {
    @XmlAttribute(name = "node")
    private String node;

    @XmlElements({
            @XmlElement(name = "affiliation"),
            @XmlElement(name = "affiliation", namespace = PubSub.EVENT_NAMESPACE),
            @XmlElement(name = "affiliation", namespace = PubSub.OWNER_NAMESPACE)})
    private List<Affiliation> affiliations = new ArrayList<>();

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