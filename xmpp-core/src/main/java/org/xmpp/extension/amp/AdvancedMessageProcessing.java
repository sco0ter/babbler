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

import org.xmpp.Jid;
import org.xmpp.extension.amp.errors.FailedRules;
import org.xmpp.extension.amp.feature.AdvancedMessageProcessingFeature;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The implementation of the {@code <amp/>} element in the {@code http://jabber.org/protocol/amp} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0079.html">XEP-0079: Advanced Message Processing</a>
 * @see <a href="http://xmpp.org/extensions/xep-0079.html#schemas-amp">XML Schema</a>
 */
@XmlRootElement(name = "amp")
@XmlSeeAlso({FailedRules.class, UnsupportedActions.class, UnsupportedConditions.class, InvalidRules.class, AdvancedMessageProcessingFeature.class})
public final class AdvancedMessageProcessing {

    @XmlAttribute(name = "from")
    private Jid from;

    @XmlAttribute(name = "per-hop")
    private Boolean perHop;

    @XmlAttribute(name = "status")
    private Rule.Action status;

    @XmlAttribute(name = "to")
    private Jid to;

    @XmlElement(name = "rule")
    private List<Rule> rules = new ArrayList<>();

    private AdvancedMessageProcessing() {
    }

    /**
     * Constructs an {@code <amp/>} element with rules.
     *
     * @param rules The rules.
     */
    public AdvancedMessageProcessing(Rule... rules) {
        this.rules = Arrays.asList(rules);
    }

    /**
     * Constructs an {@code <amp/>} element with rules.
     *
     * @param rules The rules.
     */
    public AdvancedMessageProcessing(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * Constructs an {@code <amp/>} element with rules and a per-hop attribute.
     *
     * @param rules  The rules.
     * @param perHop The per-hop attribute.
     */
    public AdvancedMessageProcessing(List<Rule> rules, boolean perHop) {
        this.rules = rules;
        this.perHop = perHop;
    }

    /**
     * Constructs an {@code <amp/>} element with rules, status, from and to attribute.
     *
     * @param rules  The rules.
     * @param status The status.
     * @param from   The from attribute.
     * @param to     The to attribute.
     */
    public AdvancedMessageProcessing(List<Rule> rules, Rule.Action status, Jid from, Jid to) {
        this.rules = rules;
        this.status = status;
        this.from = from;
        this.to = to;
    }

    /**
     * Gets the rules.
     *
     * @return The rules.
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * The 'per-hop' attribute flags the contained ruleset for processing at each server in the route between the original sender and original intended recipient.
     *
     * @return The per-hop attribute.
     */
    public boolean isPerHop() {
        return perHop != null && perHop;
    }

    /**
     * The 'status' attribute specifies the reason for this {@code <amp/>} element. When specifying semantics to be applied (client to server), this attribute MUST NOT be present. When replying to a sending entity regarding a met condition, this attribute MUST be present and SHOULD be the value of the 'action' attribute for the triggered rule. (Note: Individual action definitions MAY provide their own requirements.)
     *
     * @return The status.
     */
    public Rule.Action getStatus() {
        return status;
    }

    /**
     * The 'from' attribute specifies the original sender of the containing {@code <message/>} stanza. This attribute MUST be specified for any {@code <message/>} stanza sent from a supporting server, regardless of the recipient. It SHOULD NOT be specified otherwise. The value of the 'from' attribute MUST be the full JID (node@domain/resource) of the sender for the original {@code <message/>} stanza.
     *
     * @return The from attribute.
     */
    public Jid getFrom() {
        return from;
    }

    /**
     * The 'to' attribute specifies the original (intended) recipient of the containing {@code <message/>} stanza. This attribute MUST be specified for any {@code <message/>} stanza sent from a supporting server, regardless of the recipient. It SHOULD NOT be specified otherwise. The value of the 'to' attribute MUST be the full JID (node@domain/resource) of the intended recipient for the original {@code <message/>} stanza.
     *
     * @return The to attribute.
     */
    public Jid getTo() {
        return to;
    }
}
