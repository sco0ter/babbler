package org.xmpp.extension.muc.user;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Christian Schudt
 */
public class Decline {

    @XmlElement(name = "reason")
    private String reason;

    @XmlAttribute(name = "from")
    private Jid from;

    @XmlAttribute(name = "to")
    private Jid to;

    public String getReason() {
        return reason;
    }

    public Jid getFrom() {
        return from;
    }

    public Jid getTo() {
        return to;
    }
}
