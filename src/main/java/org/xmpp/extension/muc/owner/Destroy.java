package org.xmpp.extension.muc.owner;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Christian Schudt
 */
public final class Destroy {

    @XmlElement(name = "password")
    private String password;

    @XmlElement(name = "reason")
    private String reason;

    @XmlAttribute(name = "jid")
    private Jid jid;
}
