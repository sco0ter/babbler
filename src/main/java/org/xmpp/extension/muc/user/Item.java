package org.xmpp.extension.muc.user;

import org.xmpp.Jid;
import org.xmpp.extension.muc.Actor;
import org.xmpp.extension.muc.Affiliation;
import org.xmpp.extension.muc.Role;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Christian Schudt
 */
public final class Item {

    @XmlElement(name = "actor")
    private Actor actor;

    @XmlElement(name = "reason")
    private String reason;

    @XmlAttribute(name = "affiliation")
    private Affiliation affiliation;

    @XmlAttribute(name = "jid")
    private Jid jid;

    @XmlAttribute(name = "nick")
    private String nick;

    @XmlAttribute(name = "role")
    private Role role;

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public Role getRole() {
        return role;
    }

    public Jid getJid() {
        return jid;
    }

    public String getNick() {
        return nick;
    }
}
