package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Christian Schudt
 */
final class Publish extends PubSubChildElement {

    @XmlElement(name = "item")
    private ItemElement item;

    private Publish() {
    }

    public Publish(String node, ItemElement item) {
        super(node);
        this.item = item;
    }

    public ItemElement getItem() {
        return item;
    }
}
