package org.xmpp.extension.ibb;

import javax.xml.bind.annotation.*;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "open")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Open {
    @XmlAttribute(name = "block-size")
    private Integer blockSize;

    @XmlAttribute(name = "sid")
    private String sid;

    @XmlAttribute(name = "stanza")
    private StanzaType stanzaType;

    public Open() {

    }

    public Open(int blockSize, String sid) {
        this.blockSize = blockSize;
        this.sid = sid;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public String getSessionId() {
        return sid;
    }

    public StanzaType getStanzaType() {
        return stanzaType;
    }

    @XmlEnum
    public enum StanzaType {
        @XmlEnumValue("iq")
        IQ,
        @XmlEnumValue("message")
        MESSAGE
    }
}
