package org.xmpp.extension.pubsub;

import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;

/**
 * @author Christian Schudt
 */
final class Configuration {

    @XmlAttribute(name = "node")
    private String node;

    @XmlElementRef
    private DataForm dataForm;

    public String getNode() {
        return node;
    }

    public DataForm getDataForm() {
        return dataForm;
    }
}