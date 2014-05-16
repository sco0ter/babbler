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
    private MucAdmin(Item... items) {
        for (Item item : items) {
            this.items.add(new MucAdminItem(item));
        }
    }

    /**
     * Creates a {@code <query/>} element with an {@code <item/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <query xmlns='http://jabber.org/protocol/muc#admin'>
     *     <item role='participant'/>
     * </query>
     * }
     * </pre>
     *
     * @param role The role.
     * @return The {@link MucAdmin} instance.
     */
    public static MucAdmin withItem(Role role) {
        return new MucAdmin(new MucAdminItem(role, null, null));
    }

    /**
     * Creates a {@code <query/>} element with an {@code <item/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <query xmlns='http://jabber.org/protocol/muc#admin'>
     *     <item affiliation='outcast'/>
     * </query>
     *
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @return The {@link MucAdmin} instance.
     */
    public static MucAdmin withItem(Affiliation affiliation) {
        return new MucAdmin(new MucAdminItem(affiliation, null, null));
    }

    /**
     * Creates a {@code <query/>} element with an {@code <item/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <query xmlns='http://jabber.org/protocol/muc#admin'>
     *     <item nick='thirdwitch'
     *           role='participant'>
     *         <reason>A worthy witch indeed!</reason>
     *     </item>
     * </query>
     * }
     * </pre>
     *
     * @param role   The role.
     * @param nick   The nick.
     * @param reason The reason.
     * @return The {@link MucAdmin} instance.
     */
    public static MucAdmin withItem(Role role, String nick, String reason) {
        return new MucAdmin(new MucAdminItem(role, nick, reason));
    }

    /**
     * Creates a {@code <query/>} element with an {@code <item/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <query xmlns='http://jabber.org/protocol/muc#admin'>
     *     <item affiliation='outcast'
     *           jid='earlofcambridge@shakespeare.lit'/>
     * </query>
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @param jid         The JID.
     * @param reason      The reason.
     * @return The {@link MucAdmin} instance.
     */
    public static MucAdmin withItem(Affiliation affiliation, Jid jid, String reason) {
        return new MucAdmin(new MucAdminItem(affiliation, jid, reason));
    }

    /**
     * Creates a {@code <query/>} element with {@code <item/>} child elements.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <query xmlns='http://jabber.org/protocol/muc#admin'>
     *     <item affiliation='none'
     *           jid='polonius@hamlet/castle'
     *           nick='Polo'
     *           role='participant'/>
     *     <item affiliation='none'
     *           jid='horatio@hamlet/castle'
     *           nick='horotoro'
     *           role='participant'/>
     *     <item affiliation='member'
     *           jid='hecate@shakespeare.lit/broom'
     *           nick='Hecate'
     *           role='participant'/>
     * </query>
     * }
     * </pre>
     *
     * @param items The items.
     * @return The {@link MucAdmin} instance.
     */
    public static MucAdmin withItems(List<Item> items) {
        Item[] array = new Item[items.size()];
        items.toArray(array);
        return new MucAdmin(array);
    }

    /**
     * Creates a {@code <query/>} element with {@code <item/>} child elements.
     *
     * @param items The items.
     * @return The {@link MucAdmin} instance.
     * @see #withItems(org.xmpp.extension.muc.Item...)
     */
    public static MucAdmin withItems(Item... items) {
        return new MucAdmin(items);
    }

    /**
     * Creates an item, which can be used as input parameter for {@link #withItems(org.xmpp.extension.muc.Item...)}.
     *
     * @param affiliation The affiliation.
     * @param role        The role.
     * @param jid         The JID.
     * @param nick        The nick.
     * @param actor       The actor.
     * @param reason      The reason.
     * @return The item.
     */
    public static Item createItem(Affiliation affiliation, Role role, Jid jid, String nick, Actor actor, String reason) {
        return new MucAdminItem(affiliation, role, jid, nick, actor, reason);
    }

    /**
     * Creates an item, which can be used as input parameter for {@link #withItems(org.xmpp.extension.muc.Item...)}.
     *
     * @param role   The role.
     * @param nick   The nick.
     * @param reason The reason.
     * @return The item.
     */
    public static Item createItem(Role role, String nick, String reason) {
        return new MucAdminItem(role, nick, reason);
    }

    /**
     * Creates an item, which can be used as input parameter for {@link #withItems(org.xmpp.extension.muc.Item...)}.
     *
     * @param role The role.
     * @param nick The nick.
     * @return The item.
     */
    public static Item createItem(Role role, String nick) {
        return new MucAdminItem(role, nick, null);
    }

    /**
     * Creates an item, which can be used as input parameter for {@link #withItems(org.xmpp.extension.muc.Item...)}.
     *
     * @param role The role.
     * @return The item.
     */
    public static Item createItem(Role role) {
        return new MucAdminItem(role, null, null);
    }

    /**
     * Creates an item, which can be used as input parameter for {@link #withItems(org.xmpp.extension.muc.Item...)}.
     *
     * @param affiliation The affiliation.
     * @return The item.
     */
    public static Item createItem(Affiliation affiliation) {
        return new MucAdminItem(affiliation, null, null);
    }

    /**
     * Creates an item, which can be used as input parameter for {@link #withItems(org.xmpp.extension.muc.Item...)}.
     *
     * @param affiliation The affiliation.
     * @param jid         The JID.
     * @return The item.
     */
    public static Item createItem(Affiliation affiliation, Jid jid) {
        return new MucAdminItem(affiliation, jid, null);
    }

    /**
     * Creates an item, which can be used as input parameter for {@link #withItems(org.xmpp.extension.muc.Item...)}.
     *
     * @param affiliation The affiliation.
     * @param jid         The JID.
     * @param reason      The reason.
     * @return The item.
     */
    public static Item createItem(Affiliation affiliation, Jid jid, String reason) {
        return new MucAdminItem(affiliation, jid, reason);
    }

    /**
     * Creates an item, which can be used as input parameter for {@link #withItems(org.xmpp.extension.muc.Item...)}.
     *
     * @param affiliation The affiliation.
     * @param jid         The JID.
     * @param nick        The nick.
     * @param reason      The reason.
     * @return The item.
     */
    public static Item createItem(Affiliation affiliation, Jid jid, String nick, String reason) {
        return new MucAdminItem(affiliation, null, jid, nick, null, reason);
    }

    /**
     * Gets the items.
     *
     * @return The items.
     */
    public List<? extends Item> getItems() {
        return items;
    }

    private static final class MucAdminItem implements Item {

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

        private MucAdminItem(Affiliation affiliation, Jid jid, String reason) {
            this(affiliation, null, jid, null, null, reason);
        }

        private MucAdminItem(Affiliation affiliation, Role role, Jid jid, String nick, Actor actor, String reason) {
            this.affiliation = affiliation;
            this.role = role;
            this.jid = jid;
            this.nick = nick;
            this.actor = actor != null ? new MucAdminActor(actor) : null;
            this.reason = reason;
        }

        private MucAdminItem(Role role, String nick, String reason) {
            this.role = role;
            this.nick = nick;
            this.reason = reason;
        }

        private MucAdminItem(Item item) {
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

        private static final class MucAdminActor implements Actor {
            @XmlAttribute(name = "jid")
            private Jid jid;

            @XmlAttribute(name = "nick")
            private String nick;

            private MucAdminActor() {
            }

            private MucAdminActor(Actor actor) {
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
