/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp.extension.muc.user;

import org.xmpp.Jid;
import org.xmpp.extension.muc.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@code <x/>} element.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#schemas-user">18.2 http://jabber.org/protocol/muc#user</a>
 */
@XmlRootElement(name = "x")
public final class MucUser {

    @XmlElement(name = "item")
    private MucUserItem item;

    @XmlElement(name = "status")
    private List<Status> statusCodes = new ArrayList<>();

    @XmlElement(name = "invite")
    private List<Invite> invites = new ArrayList<>();

    @XmlElement(name = "decline")
    private Decline decline;

    @XmlElement(name = "destroy")
    private MucUserDestroy destroy;

    @XmlElement(name = "password")
    private String password;

    private MucUser() {
    }

    public MucUser(Invite invite) {
        this.invites.add(invite);
    }

    MucUser(Destroy destroy) {
        this.destroy = new MucUserDestroy(destroy);
    }

    /**
     * Gets the status codes.
     *
     * @return The status codes.
     */
    public List<Status> getStatusCodes() {
        return statusCodes;
    }

    /**
     * Gets the item.
     *
     * @return The item.
     */
    public Item getItem() {
        return item;
    }

    /**
     * Gets the invites
     *
     * @return The invites.
     */
    public List<Invite> getInvites() {
        return invites;
    }

    /**
     * Gets the decline.
     *
     * @return The decline.
     */
    public Decline getDecline() {
        return decline;
    }

    /**
     * Gets the destroy element.
     *
     * @return The destroy element.
     */
    public Destroy getDestroy() {
        return destroy;
    }

    public String getPassword() {
        return password;
    }

    static class MucUserItem implements Item {

        @XmlElement(name = "actor")
        private MucAdminActor actor;

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

        private MucUserItem() {
        }

        MucUserItem(Affiliation affiliation) {
            this(affiliation, null, null);
        }

        MucUserItem(Affiliation affiliation, Jid jid) {
            this(affiliation, jid, null);
        }

        MucUserItem(Affiliation affiliation, Jid jid, String reason) {
            this(affiliation, jid, reason, null);
        }

        MucUserItem(Affiliation affiliation, Jid jid, String reason, String nick) {
            this.affiliation = affiliation;
            this.jid = jid;
            this.reason = reason;
            this.nick = nick;
        }

        MucUserItem(Role role, String nick) {
            this(role, nick, null);
        }

        MucUserItem(Role role, String nick, String reason) {
            this.role = role;
            this.nick = nick;
            this.reason = reason;
        }

        @Override
        public String getNick() {
            return nick;
        }

        @Override
        public Role getRole() {
            return role;
        }

        @Override
        public Jid getJid() {
            return jid;
        }

        @Override
        public Affiliation getAffiliation() {
            return affiliation;
        }

        @Override
        public String getReason() {
            return reason;
        }

        @Override
        public Actor getActor() {
            return actor;
        }
    }

    static final class MucUserDestroy implements Destroy {

        @XmlElement(name = "reason")
        private String reason;

        @XmlAttribute(name = "jid")
        private Jid jid;

        private MucUserDestroy() {
        }

        MucUserDestroy(Jid jid, String reason) {
            this.jid = jid;
            this.reason = reason;
        }

        private MucUserDestroy(Destroy destroy) {
            this.jid = destroy.getJid();
            this.reason = destroy.getReason();
        }

        @Override
        public Jid getJid() {
            return jid;
        }

        @Override
        public String getReason() {
            return reason;
        }
    }

    static final class MucAdminActor implements Actor {
        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "nick")
        private String nick;

        private MucAdminActor() {
        }

        MucAdminActor(Actor actor) {
            this.jid = actor.getJid();
            this.nick = actor.getNick();
        }

        @Override
        public String getNick() {
            return nick;
        }

        @Override
        public Jid getJid() {
            return jid;
        }
    }
}
