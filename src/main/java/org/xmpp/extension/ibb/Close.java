package org.xmpp.extension.ibb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "close")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Close {

    @XmlAttribute(name = "sid")
    private String sid;

    private Close() {

    }

    public Close(String sid) {
        this.sid = sid;
    }

    public String getSessionId() {
        return sid;
    }
}
