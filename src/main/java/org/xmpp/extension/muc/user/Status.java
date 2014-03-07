package org.xmpp.extension.muc.user;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Christian Schudt
 */
public class Status {

    @XmlAttribute(name = "code")
    private Integer code;

    public Integer getCode() {
        return code;
    }
}
