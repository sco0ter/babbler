package org.xmpp.extension.bob;

import javax.xml.bind.annotation.*;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Data {

    @XmlAttribute(name = "cid")
    private String cid;

    @XmlAttribute(name = "max-age")
    private int maxAge;

    @XmlAttribute(name = "type")
    private String type;

    @XmlValue
    private byte[] bytes;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
