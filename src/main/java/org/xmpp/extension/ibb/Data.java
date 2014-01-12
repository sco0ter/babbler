package org.xmpp.extension.ibb;

import javax.xml.bind.annotation.*;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Data {

    @XmlAttribute(name = "sid")
    private String sid;

    @XmlAttribute(name = "seq")
    private Integer seq;

    @XmlValue
    private byte[] bytes;

    /**
     * Private constructor for unmarshalling.
     */
    private Data() {

    }

    /**
     * Creates a new data element.
     *
     * @param bytes The bytes.
     * @param sid   The session id.
     * @param seq   The sequence number.
     */
    public Data(byte[] bytes, String sid, int seq) {
        this.bytes = bytes;
        this.sid = sid;
        this.seq = seq;
    }

    /**
     * Gets the sequence number.
     *
     * @return The sequence number.
     */
    public int getSequence() {
        return seq;
    }

    /**
     * Gets the session id.
     *
     * @return The session id.
     */
    public String getSessionId() {
        return sid;
    }

    /**
     * Gets the bytes.
     *
     * @return The bytes.
     */
    public byte[] getBytes() {
        return bytes;
    }
}
