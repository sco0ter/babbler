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

package org.xmpp.extension.privacy;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of a privacy list.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0016.html#protocol">2. Protocol</a></cite></p>
 * <p>Most instant messaging systems have found it necessary to implement some method for users to block communications from particular other users (this is also required by sections 5.1.5, 5.1.15, 5.3.2, and 5.4.10 of RFC 2779 [3]. In XMPP this is done by managing one's privacy lists using the 'jabber:iq:privacy' namespace.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0016.html">XEP-0016: Privacy Lists</a>
 */
public final class PrivacyList implements Comparable<PrivacyList> {
    @XmlElement(name = "item")
    private final List<PrivacyRule> items = new ArrayList<>();

    @XmlTransient
    boolean isActive;

    @XmlTransient
    boolean isDefault;

    @XmlAttribute(name = "name")
    private String name;

    private PrivacyList() {
    }

    /**
     * Creates a privacy list.
     *
     * @param name The privacy list's name.
     */
    public PrivacyList(String name) {
        this.name = name;
    }

    /**
     * Creates a global invisibility list.
     *
     * @return The invisibility list.
     */
    public static PrivacyList createInvisibilityList() {
        return createInvisibilityListExceptForUsers("invisible");
    }

    /**
     * Creates a global invisibility list, where you are still visible to some contacts.
     *
     * @param listName The list name. See <a href="http://xmpp.org/extensions/xep-0126.html#impl">4. Implementation Notes</a> for recommended list names.
     * @param jids     The JIDs to which you are still visible.
     * @return The invisibility list.
     * @see <a href="http://xmpp.org/extensions/xep-0126.html">XEP-0126: Invisibility</a>
     * @see <a href="http://xmpp.org/extensions/xep-0126.html#vis-select-jid">3.2.1 Becoming Visible by JID</a>
     */
    public static PrivacyList createInvisibilityListExceptForUsers(String listName, Jid... jids) {
        PrivacyList privacyList = new PrivacyList(listName);
        long order = 1;
        for (Jid jid : jids) {
            PrivacyRule privacyRule = new PrivacyRule(PrivacyRule.Action.ALLOW, order++, jid);
            privacyRule.setFilterPresenceOut(true);
            privacyList.getPrivacyRules().add(privacyRule);
        }
        PrivacyRule privacyRule = new PrivacyRule(PrivacyRule.Action.DENY, order);
        privacyRule.setFilterPresenceOut(true);
        privacyList.getPrivacyRules().add(privacyRule);
        return privacyList;
    }

    /**
     * Creates a global invisibility list, where you are still visible to some contacts.
     *
     * @param listName The list name. See <a href="http://xmpp.org/extensions/xep-0126.html#impl">4. Implementation Notes</a> for recommended list names.
     * @param groups   The roster groups to which you are still visible.
     * @return The invisibility list.
     * @see <a href="http://xmpp.org/extensions/xep-0126.html">XEP-0126: Invisibility</a>
     * @see <a href="http://xmpp.org/extensions/xep-0126.html#vis-select-roster">3.2.2 Becoming Visible by Roster Group</a>
     */
    public static PrivacyList createInvisibilityListExceptForGroups(String listName, String... groups) {
        PrivacyList privacyList = new PrivacyList(listName);
        long order = 1;
        for (String group : groups) {
            PrivacyRule privacyRule = new PrivacyRule(PrivacyRule.Action.ALLOW, order++, group);
            privacyRule.setFilterPresenceOut(true);
            privacyList.getPrivacyRules().add(privacyRule);
        }
        PrivacyRule privacyRule = new PrivacyRule(PrivacyRule.Action.DENY, order);
        privacyRule.setFilterPresenceOut(true);
        privacyList.getPrivacyRules().add(privacyRule);
        return privacyList;
    }

    /**
     * Creates a selective invisibility list. You are only invisible to the provided JIDs. You are visible to everyone else.
     *
     * @param listName The list name. See <a href="http://xmpp.org/extensions/xep-0126.html#impl">4. Implementation Notes</a> for recommended list names.
     * @param jids     The JIDs to which you appear invisible.
     * @return The invisibility list.
     * @see <a href="http://xmpp.org/extensions/xep-0126.html">XEP-0126: Invisibility</a>
     * @see <a href="http://xmpp.org/extensions/xep-0126.html#invis-select-jid">3.4.1 Becoming Invisible by JID</a>
     */
    public static PrivacyList createInvisibilityListForUsers(String listName, Jid... jids) {
        PrivacyList privacyList = new PrivacyList(listName);
        long order = 1;
        for (Jid jid : jids) {
            PrivacyRule privacyRule = new PrivacyRule(PrivacyRule.Action.DENY, order++, jid);
            privacyRule.setFilterPresenceOut(true);
            privacyList.getPrivacyRules().add(privacyRule);
        }
        PrivacyRule privacyRule = new PrivacyRule(PrivacyRule.Action.ALLOW, order);
        privacyRule.setFilterPresenceOut(true);
        privacyList.getPrivacyRules().add(privacyRule);
        return privacyList;
    }

    /**
     * Creates a selective invisibility list. You are only invisible to the provided JIDs. You are visible to everyone else.
     *
     * @param listName The list name. See <a href="http://xmpp.org/extensions/xep-0126.html#impl">4. Implementation Notes</a> for recommended list names.
     * @param groups   The roster groups to which you appear invisible.
     * @return The invisibility list.
     * @see <a href="http://xmpp.org/extensions/xep-0126.html">XEP-0126: Invisibility</a>
     * @see <a href="http://xmpp.org/extensions/xep-0126.html#invis-select-roster">3.4.2 Becoming Invisible by Roster Group</a>
     */
    public static PrivacyList createInvisibilityListForGroups(String listName, String... groups) {
        PrivacyList privacyList = new PrivacyList(listName);
        long order = 1;
        for (String group : groups) {
            PrivacyRule privacyRule = new PrivacyRule(PrivacyRule.Action.DENY, order++, group);
            privacyRule.setFilterPresenceOut(true);
            privacyList.getPrivacyRules().add(privacyRule);
        }
        PrivacyRule privacyRule = new PrivacyRule(PrivacyRule.Action.ALLOW, order);
        privacyRule.setFilterPresenceOut(true);
        privacyList.getPrivacyRules().add(privacyRule);
        return privacyList;
    }

    /**
     * Gets the privacy rules.
     *
     * @return The privacy rules.
     */
    public List<PrivacyRule> getPrivacyRules() {
        return items;
    }

    /**
     * Gets the name of the privacy list.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether this is the default list.
     *
     * @return True, if this is the default list.
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Indicates whether this is the active list.
     *
     * @return True, if this is the active list.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Compares this privacy list with another list. When sorted, default lists are listed first, then active lists, then lists are sorted by their name.
     *
     * @param o The other list.
     * @return The comparison result.
     */
    @Override
    public int compareTo(PrivacyList o) {
        if (this == o) {
            return 0;
        }
        if (o != null) {
            if (isDefault) {
                if (o.isDefault) {
                    return name != null ? name.compareTo(o.name) : 1;
                } else {
                    return -1;
                }
            } else if (isActive) {
                if (o.isDefault) {
                    return 1;
                } else if (o.isActive) {
                    return name != null ? name.compareTo(o.name) : 1;
                } else {
                    return -1;
                }
            } else {
                if (o.isDefault || o.isActive) {
                    return 1;
                } else {
                    return name != null ? name.compareTo(o.name) : 1;
                }
            }
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}