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

package org.xmpp.extension.amp;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The implementation of the {@code <rule/>} element, used both in the {@code http://jabber.org/protocol/amp} namespace as well as in the {@code http://jabber.org/protocol/amp#errors} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0079.html">XEP-0079: Advanced Message Processing</a>
 * @see <a href="http://xmpp.org/extensions/xep-0079.html#schemas-amp">XML Schema</a>
 */
public final class Rule {
    @XmlAttribute(name = "action")
    private Action action;

    @XmlAttribute(name = "condition")
    private Condition condition;

    @XmlAttribute(name = "value")
    private String value;

    private Rule() {
    }

    /**
     * @param action    The action.
     * @param condition The condition.
     * @param value     The value. This depends on the condition.
     */
    public Rule(Action action, Condition condition, String value) {
        this.action = action;
        this.condition = condition;
        this.value = value;
    }

    /**
     * The 'action' attribute defines the result for this rule.
     *
     * @return The action.
     */
    public Action getAction() {
        return action;
    }

    /**
     * The 'condition' attribute defines the overall condition this rule applies to.
     *
     * @return The condition.
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * The 'value' attribute defines how the condition is matched.
     *
     * @return The value.
     */
    public String getValue() {
        return value;
    }

    /**
     * The action defines what occurs when a particular rule is triggered. The value of the action attribute determines the behavior if the rule's condition is met.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0079.html#actions-def">3.4 Defined Actions</a>
     */
    public enum Action {
        /**
         * The "alert" action triggers a reply {@code <message/>} stanza to the sending entity.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0079.html#actions-def-alert">3.4.1 alert</a>
         */
        @XmlEnumValue("alert")
        ALERT,
        /**
         * The "drop" action silently discards the message from any further delivery attempts and ensures that it is not placed into offline storage.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0079.html#actions-def-drop">3.4.2 drop</a>
         */
        @XmlEnumValue("drop")
        DROP,
        /**
         * The "error" action triggers a reply {@code <message/>} stanza of type "error" to the sending entity.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0079.html#actions-def-error">3.4.3 error</a>
         */
        @XmlEnumValue("error")
        ERROR,
        /**
         * The "notify" action triggers a reply {@code <message/>} stanza to the sending entity.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0079.html#actions-def-notify">3.4.4 notify</a>
         */
        @XmlEnumValue("notify")
        NOTIFY
    }

    /**
     * The condition defines how or when a particular rule is triggered. The value of the condition attribute determines what the contents of the {@code <rule/>} mean.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0079.html#conditions-def">3.3 Defined Conditions</a>
     */
    public enum Condition {
        /**
         * The "deliver" condition is used to ensure delivery (or non-delivery) in one of five ways.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0079.html#conditions-def-expireat">3.3.2 expire-at</a>
         */
        @XmlEnumValue("deliver")
        DELIVER,
        /**
         * The "expire-at" condition is used to ensure delivery before an absolute point in time.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0079.html#conditions-def-expireat">3.3.2 expire-at</a>
         */
        @XmlEnumValue("expire-at")
        EXPIRE_AT,
        /**
         * The "match-resource" condition is used to restrict delivery based on the resource identifier of the recipient JID.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0079.html#conditions-def-match">3.3.3 match-resource</a>
         */
        @XmlEnumValue("match-resource")
        MATCH_RESOURCE,
    }
}
