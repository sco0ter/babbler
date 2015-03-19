/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.privacy.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.roster.model.Contact;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Objects;

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
 * This class is immutable.
 *
 * @author Christian Schudt
 */
public final class PrivacyRule implements Comparable<PrivacyRule> {

    @XmlAttribute(name = "type")
    private final Type type;

    @XmlAttribute(name = "value")
    private final String value;

    @XmlAttribute(name = "action")
    private final Action action;

    @XmlAttribute(name = "order")
    private final long order;

    @XmlElement(name = "message")
    private final String message;

    @XmlElement(name = "presence-in")
    private final String presenceIn;

    @XmlElement(name = "presence-out")
    private final String presenceOut;

    @XmlElement(name = "iq")
    private final String iq;

    private PrivacyRule() {
        this.action = null;
        this.order = 0;
        this.type = null;
        this.value = null;
        this.message = null;
        this.presenceIn = null;
        this.presenceOut = null;
        this.iq = null;
    }

    /**
     * Creates a privacy list item, which allows or blocks everything.
     *
     * @param action The action to perform, i.e. either allow or deny.
     * @param order  The order in which the privacy item is processed by the server. A non-negative integer that is unique among all items in the list.
     */
    public PrivacyRule(Action action, long order) {
        this(action, order, null, null, false, false, false, false);
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
    private PrivacyRule(Action action, long order, Type type, String value, boolean filterMessage, boolean filterPresenceIn, boolean filterPresenceOut, boolean filterIQ) {
        Objects.requireNonNull(action, "action must not be null.");
        if (order < 0) {
            throw new IllegalArgumentException("order must be greater 0.");
        }
        this.action = action;
        this.order = order;
        this.type = type;
        this.value = value;
        this.message = filterMessage ? "" : null;
        this.presenceIn = filterPresenceIn ? "" : null;
        this.presenceOut = filterPresenceOut ? "" : null;
        this.iq = filterIQ ? "" : null;
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
        this.message = null;
        this.presenceIn = null;
        this.presenceOut = null;
        this.iq = null;
    }

    /**
     * Creates a privacy rule of type 'jid'.
     *
     * @param action The action to perform, i.e. either allow or deny.
     * @param order  The order in which the privacy item is processed by the server. A non-negative integer that is unique among all items in the list.
     * @param jid    The JID.
     */
    public PrivacyRule(Action action, long order, Jid jid) {
        this(action, order, Type.JID, jid.toEscapedString(), false, false, false, false);
    }

    /**
     * Creates a privacy rule of type 'group'.
     *
     * @param action The action to perform, i.e. either allow or deny.
     * @param order  The order in which the privacy item is processed by the server. A non-negative integer that is unique among all items in the list.
     * @param group  The contact group.
     */
    public PrivacyRule(Action action, long order, String group) {
        this(action, order, Type.GROUP, group, false, false, false, false);
    }

    /**
     * Gets the type.
     *
     * @return The type.
     */
    public final Type getType() {
        return type;
    }

    /**
     * Sets the action to perform (either allow or deny).
     *
     * @return The action.
     */
    public final Action getAction() {
        return action;
    }

    /**
     * Gets the value.
     *
     * @return The value.
     */
    public final String getValue() {
        return value;
    }

    /**
     * Gets the order in which privacy items are processed by the server.
     *
     * @return The order.
     */
    public final long getOrder() {
        return order;
    }

    /**
     * Indicates, whether inbound message stanzas are filtered.
     *
     * @return True, if inbound message stanzas are filtered.
     */
    public final boolean isFilterMessage() {
        return message != null || isFilterEverything();
    }

    /**
     * Creates a privacy rule, which filters message stanzas.
     *
     * @return The privacy rule.
     * @see #isFilterMessage()
     */
    public final PrivacyRule filterMessage() {
        return new PrivacyRule(action, order, type, value, true, presenceIn != null, presenceOut != null, iq != null);
    }

    /**
     * Indicates, whether inbound IQ stanzas are filtered.
     *
     * @return True, if inbound IQ stanzas are filtered.
     */
    public final boolean isFilterIQ() {
        return iq != null || isFilterEverything();
    }

    /**
     * Creates a privacy rule, which filters IQ stanzas.
     *
     * @return The privacy rule.
     * @see #isFilterIQ()
     */
    public final PrivacyRule filterIQ() {
        return new PrivacyRule(action, order, type, value, message != null, presenceIn != null, presenceOut != null, true);
    }

    /**
     * Indicates, whether inbound presence notifications are filtered.
     *
     * @return True, if inbound presence notifications are filtered.
     */
    public final boolean isFilterPresenceIn() {
        return presenceIn != null || isFilterEverything();
    }

    /**
     * Creates a privacy rule, which filters inbound presence stanzas.
     *
     * @return The privacy rule.
     * @see #isFilterPresenceIn()
     */
    public final PrivacyRule filterPresenceIn() {
        return new PrivacyRule(action, order, type, value, message != null, true, presenceOut != null, iq != null);
    }

    /**
     * Indicates, whether outbound presence notifications are filtered.
     *
     * @return True, if outbound presence notifications are filtered.
     */
    public final boolean isFilterPresenceOut() {
        return presenceOut != null || isFilterEverything();
    }

    /**
     * Creates a privacy rule, which filters outbound presence stanzas.
     *
     * @return The privacy rule.
     * @see #isFilterPresenceOut()
     */
    public final PrivacyRule filterPresenceOut() {
        return new PrivacyRule(action, order, type, value, message != null, presenceIn != null, true, iq != null);
    }

    private boolean isFilterEverything() {
        return presenceIn == null && presenceOut == null && message == null && iq == null;
    }

    /**
     * Compares this rule with another rule by comparing their order.
     *
     * @param o The other rule.
     * @return The comparison result.
     */
    @Override
    public final int compareTo(PrivacyRule o) {
        if (this == o) {
            return 0;
        }
        return o != null ? Long.compare(order, o.order) : -1;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(action.name().toLowerCase());
        sb.append(", ");
        sb.append(order);

        if (type != null) {
            sb.append(", ");
            sb.append(type.name().toLowerCase());
        }
        if (value != null) {
            sb.append(", ");
            sb.append(value);
        }

        return sb.toString();
    }

    /**
     * Defines the type of communication which should be allowed of denied.
     */
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
