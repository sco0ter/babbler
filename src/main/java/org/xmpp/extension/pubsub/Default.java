package org.xmpp.extension.pubsub;

import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Christian Schudt
 */
final class Default {
    @XmlAttribute(name = "node")
    private String node;

    @XmlAttribute(name = "type")
    private Type type;

    @XmlElementRef
    private DataForm dataForm;

    public Default() {

    }

    public Default(String node) {
        this.node = node;
    }

    public DataForm getDataForm() {
        return dataForm;
    }

    public enum Type {
        @XmlEnumValue("collection")
        COLLECTION,
        @XmlEnumValue("leaf")
        LEAF
    }
}
