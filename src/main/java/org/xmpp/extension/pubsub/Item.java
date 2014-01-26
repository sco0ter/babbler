package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "item")
public final class Item {
    @XmlAnyElement
    private Object object;

    @XmlAttribute(name = "id")
    private String id;
}
