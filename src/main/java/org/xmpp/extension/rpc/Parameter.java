package org.xmpp.extension.rpc;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Christian Schudt
 */
final class Parameter {

    @XmlElement(name = "value")
    private Value value;

    private Parameter() {
    }

    public Parameter(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}
