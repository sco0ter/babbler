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
import org.xmpp.im.Contact;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * A privacy rule for privacy lists, which is applied by the server.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0016.html#protocol-rules">2.2 Business Rules</a></cite></p>
 * <ul>
 * <li>The order in which privacy list items are processed by the server is important. List items MUST be processed in ascending order determined by the integer values of the 'order' attribute for each {@code <item/>}.</li>
 * <li>As soon as a stanza is matched against a privacy list rule, the server MUST appropriately handle the stanza in accordance with the rule and cease processing.</li>
 * <li>If no fall-through item is provided in a list, the fall-through action is assumed to be "allow".</li>
 * </ul>
 * </blockquote>
 *
 * @author Christian Schudt
 */
public final class PrivacyRule {

    @XmlAttribute(name = "type")
    private Type type;

    @XmlAttribute(name = "value")
    private String value;

    @XmlAttribute(name = "action")
    private Action action;

    @XmlAttribute(name = "order")
    private long order;

    @XmlElement(name = "iq")
    private String iq;

    @XmlElement(name = "message")
    private String message;

    @XmlElement(name = "presence-in")
    private String presenceIn;

    @XmlElement(name = "presence-out")
    private String presenceOut;

    /**
     * Creates a privacy list item, which allows or blocks everything.
     *
     * @param action The action to perform, i.e. either allow or deny.
     * @param order  The order in which the privacy item is processed by the server. A non-negative integer that is unique among all items in the list.
     */
    public PrivacyRule(Action action, long order) {
        this(action, order, null, null);
    }

    /**
     * Creates a privacy list item.
     *
     * @param action The action to perform, i.e. either allow or deny.
     * @param order  The order in which the privacy item is processed by the server. A non-negative integer that is unique among all items in the list.
     * @param type   The type of the privacy item.
     * @param value  <ul>
     *               <li>If the type is {@link Type#JID}, then the value must be a valid JID.</li>
     *               <li>If the type is {@link Type#GROUP}, then the value should be the name of a group in the user's roster.</li>
     *               <li>If the type is {@link Type#SUBSCRIPTION}, then the value must be one of "both", "to", "from", or "none".</li>
     *               </ul>
     */
    private PrivacyRule(Action action, long order, Type type, String value) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null.");
        }
        if (order < 0) {
            throw new IllegalArgumentException("order must be greater 0.");
        }
        this.action = action;
        this.order = order;
        this.type = type;
        this.value = value;
    }

    private PrivacyRule() {
    }

    /**
     * Creates a privacy rule of type 'subscription'.
     *
     * @param action       The action to perform, i.e. either allow or deny.
     * @param order        The order in which the privacy item is processed by the server. A non-negative integer that is unique among all items in the list.
     * @param subscription The subscription.
     */
    public PrivacyRule(Action action, long order, Contact.Subscription subscription) {
        if (Contact.Subscription.REMOVE.equals(subscription)) {
            throw new IllegalArgumentException("subscription must not be 'remove'");
        }
        this.action = action;
        this.order = order;
        this.type = Type.SUBSCRIPTION;
        this.value = subscription.name().toLowerCase();
    }

    /**
     * Creates a privacy rule of type 'jid'.
     *
     * @param action The action to perform, i.e. either allow or deny.
     * @param order  The order in which the privacy item is processed by the server. A non-negative integer that is unique among all items in the list.
     * @param jid    The JID.
     */
    public PrivacyRule(Action action, long order, Jid jid) {
        this(action, order, Type.JID, jid.toEscapedString());
    }

    /**
     * Creates a privacy rule of type 'group'.
     *
     * @param action The action to perform, i.e. either allow or deny.
     * @param order  The order in which the privacy item is processed by the server. A non-negative integer that is unique among all items in the list.
     * @param group  The contact group.
     */
    public PrivacyRule(Action action, long order, String group) {
        this(action, order, Type.GROUP, group);
    }

    /**
     * Gets the type.
     *
     * @return The type.
     * @see #setType(org.xmpp.extension.privacy.PrivacyRule.Type)
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type The type.
     * @see #getType()
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Sets the action to perform (either allow or deny).
     *
     * @return The action.
     * @see #setAction(org.xmpp.extension.privacy.PrivacyRule.Action)
     */
    public Action getAction() {
        return action;
    }

    /**
     * Sets the action to perform (either allow or deny).
     *
     * @param action The action.
     * @see #getAction()
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Gets the value.
     *
     * @return The value.
     * @see #setValue(String)
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     * <ul>
     * <li>If the type is {@link Type#JID}, then the value must be a valid JID.</li>
     * <li>If the type is {@link Type#GROUP}, then the value should be the name of a group in the user's roster.</li>
     * <li>If the type is {@link Type#SUBSCRIPTION}, then the value must be one of "both", "to", "from", or "none".</li>
     * </ul>
     *
     * @param value The value.
     * @see #getValue()
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the order in which privacy items are processed by the server.
     *
     * @return The order.
     * @see #setOrder(int)
     */
    public long getOrder() {
        return order;
    }

    /**
     * Gets the order in which privacy items are processed by the server.
     *
     * @param order The order.
     * @see #getOrder()
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Indicates, whether incoming message stanzas are filtered.
     *
     * @return True, if incoming message stanzas are filtered.
     * @see #setFilterMessage(boolean)
     */
    public boolean isFilterMessage() {
        return message != null || isFilterEverything();
    }

    /**
     * Indicates, whether incoming message stanzas are filtered.
     *
     * @param filterMessages True, if incoming message stanzas are filtered.
     * @see #isFilterMessage()
     */
    public void setFilterMessage(boolean filterMessages) {
        this.message = filterMessages ? "" : null;
    }

    /**
     * Indicates, whether incoming IQ stanzas are filtered.
     *
     * @return True, if incoming IQ stanzas are filtered.
     * @see #setFilterIQ(boolean)
     */
    public boolean isFilterIQ() {
        return iq != null || isFilterEverything();
    }

    /**
     * Indicates, whether incoming IQ stanzas are filtered.
     *
     * @param filterIQ True, if incoming IQ stanzas are filtered.
     * @see #isFilterIQ()
     */
    public void setFilterIQ(boolean filterIQ) {
        this.iq = filterIQ ? "" : null;
    }

    /**
     * Indicates, whether incoming presence notifications are filtered.
     *
     * @return True, if incoming presence notifications are filtered.
     * @see #setFilterPresenceIn(boolean)
     */
    public boolean isFilterPresenceIn() {
        return presenceIn != null || isFilterEverything();
    }

    /**
     * Indicates, whether incoming presence notifications are filtered.
     *
     * @param filterPresenceIn True, if incoming presence notifications are filtered.
     * @see #isFilterPresenceIn()
     */
    public void setFilterPresenceIn(boolean filterPresenceIn) {
        this.presenceIn = filterPresenceIn ? "" : null;
    }

    /**
     * Indicates, whether outgoing presence notifications are filtered.
     *
     * @return True, if outgoing presence notifications are filtered.
     * @see #setFilterPresenceOut(boolean)
     */
    public boolean isFilterPresenceOut() {
        return presenceOut != null || isFilterEverything();
    }

    /**
     * Indicates, whether outgoing presence notifications are filtered.
     *
     * @param filterPresenceOut True, if outgoing presence notifications are filtered.
     * @see #isFilterPresenceOut()
     */
    public void setFilterPresenceOut(boolean filterPresenceOut) {
        this.presenceOut = filterPresenceOut ? "" : null;
    }

    private boolean isFilterEverything() {
        return (presenceIn == null && presenceOut == null && message == null && iq == null);
    }

    /**
     * Defines the type of communication which should be allowed of denied.
     */
    @XmlEnum
    public enum Type {
        /**
         * Allows or blocks communication based on a user's roster group name.
         */
        @XmlEnumValue("group")
        GROUP,
        /**
         * Allows or blocks communication based on a JID.
         */
        @XmlEnumValue("jid")
        JID,
        /**
         * Allows or blocks communication based on subscription.
         */
        @XmlEnumValue("subscription")
        SUBSCRIPTION
    }

    /**
     * Defines the action to perform with the privacy item: either allow or deny communication.
     */
    @XmlEnum
    public enum Action {
        /**
         * Allows communication.
         */
        @XmlEnumValue("allow")
        ALLOW,
        /**
         * Denies (blocks) communication.
         */
        @XmlEnumValue("deny")
        DENY
    }
}
