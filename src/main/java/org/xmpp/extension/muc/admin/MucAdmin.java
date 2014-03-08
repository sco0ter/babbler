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

package org.xmpp.extension.muc.admin;

import org.xmpp.Jid;
import org.xmpp.extension.muc.Actor;
import org.xmpp.extension.muc.Affiliation;
import org.xmpp.extension.muc.Item;
import org.xmpp.extension.muc.Role;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@code <query/>} element in the muc#admin namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#schemas-admin">18.3 http://jabber.org/protocol/muc#admin</a>
 */
@XmlRootElement(name = "query")
public final class MucAdmin {

    @XmlElement(name = "item")
    private List<MucAdminItem> items = new ArrayList<>();

    private MucAdmin() {
    }

    /**
     * Creates a muc#admin extension with items.
     *
     * @param items The items.
     */
    public MucAdmin(Item... items) {
        for (Item item : items) {
            this.items.add(new MucAdminItem(item));
        }
    }

    /**
     * Gets the items.
     *
     * @return The items.
     */
    public List<? extends Item> getItems() {
        return items;
    }

    static final class MucAdminItem implements Item {

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

        private MucAdminItem() {
        }

        MucAdminItem(Affiliation affiliation) {
            this(affiliation, null, null);
        }

        MucAdminItem(Affiliation affiliation, Jid jid) {
            this(affiliation, jid, null);
        }

        MucAdminItem(Affiliation affiliation, Jid jid, String reason) {
            this(affiliation, jid, reason, null);
        }

        MucAdminItem(Affiliation affiliation, Jid jid, String reason, String nick) {
            this.affiliation = affiliation;
            this.jid = jid;
            this.reason = reason;
            this.nick = nick;
        }

        MucAdminItem(Role role, String nick) {
            this(role, nick, null);
        }

        MucAdminItem(Role role, String nick, String reason) {
            this.role = role;
            this.nick = nick;
            this.reason = reason;
        }

        MucAdminItem(Item item) {
            if (item.getActor() != null) {
                this.actor = new MucAdminActor(item.getActor());
            }
            this.affiliation = item.getAffiliation();
            this.jid = item.getJid();
            this.role = item.getRole();
            this.nick = item.getNick();
            this.reason = item.getReason();
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
}
