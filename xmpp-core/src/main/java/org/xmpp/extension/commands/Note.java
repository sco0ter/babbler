package org.xmpp.extension.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Christian Schudt
 */
public class Note {

    @XmlValue
    private String value;

    @XmlAttribute(name = "type")
    private Type type;

    public enum Type {
        @XmlEnumValue(value = "error")
        ERROR,
        @XmlEnumValue(value = "info")
        INFO,
        @XmlEnumValue(value = "warn")
        WARN
    }
}
