package org.xmpp.extension.pubsub;

import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;

/**
 * @author Christian Schudt
 */
final class Configure {
    @XmlAttribute(name = "node")
    private String node;

    @XmlElementRef
    private DataForm dataForm;

    private Configure() {

    }

    public Configure(String node) {
        this.node = node;
    }

    public Configure(String node, DataForm dataForm) {
        this.node = node;
        this.dataForm = dataForm;
    }

    public Configure(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    public DataForm getDataForm() {
        return dataForm;
    }

    public String getNode() {
        return node;
    }
}
