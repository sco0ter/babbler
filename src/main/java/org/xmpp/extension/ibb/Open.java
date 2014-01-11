package org.xmpp.extension.ibb;

import javax.xml.bind.annotation.*;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "open")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Open {
    @XmlAttribute(name = "block-size")
    private Short blockSize;

    @XmlAttribute(name = "sid")
    private String sid;

    @XmlAttribute(name = "iq")
    private String stanza;

    public Open() {

    }

    public Open(short blockSize, String sid) {
        this.blockSize = blockSize;
        this.sid = sid;
    }

    @XmlEnum
    public enum StanzaType {
        @XmlEnumValue("iq")
        IQ,
        @XmlEnumValue("message")
        MESSAGE
    }
}
