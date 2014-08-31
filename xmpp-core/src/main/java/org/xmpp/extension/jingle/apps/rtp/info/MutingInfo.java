package org.xmpp.extension.jingle.apps.rtp.info;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Christian Schudt
 */
@XmlTransient
abstract class MutingInfo {

    @XmlAttribute(name = "creator")
    private Creator creator;

    @XmlAttribute(name = "name")
    private String name;

    public Creator getCreator() {
        return creator;
    }

    public String getName() {
        return name;
    }

    public enum Creator {
        @XmlEnumValue("initiator")
        INITIATOR,
        @XmlEnumValue("responder")
        RESPONDER
    }
}
