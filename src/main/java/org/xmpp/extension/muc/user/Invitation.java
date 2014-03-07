package org.xmpp.extension.muc.user;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Christian Schudt
 */
public final class Invitation {

    @XmlElement(name = "reason")
    private String reason;

    @XmlAttribute(name = "from")
    private Jid from;

    @XmlAttribute(name = "to")
    private Jid to;

    @XmlElement(name = "continue")
    private Continue aContinue;

    public String getReason() {
        return reason;
    }

    public Jid getFrom() {
        return from;
    }

    public Jid getTo() {
        return to;
    }

    public Continue getContinue() {
        return aContinue;
    }
}
