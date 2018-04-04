/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.core.stanza.model.errors;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The implementation of the {@code <policy-violation/>} stanza error.
 * <blockquote>
 * <p><cite><a href="https://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-policy-violation">8.3.3.12.  policy-violation</a></cite></p>
 * <p>The entity has violated some local service policy (e.g., a message contains words that are prohibited by the service) and the server MAY choose to specify the policy in the {@code <text/>} element or in an application-specific condition element; the associated error type SHOULD be "modify" or "wait" depending on the policy being violated.</p>
 * </blockquote>
 * This class is a singleton.
 *
 * @see #POLICY_VIOLATION
 */
@XmlRootElement(name = "policy-violation")
@XmlType(factoryMethod = "create")
final class PolicyViolation extends Condition {

    PolicyViolation() {
    }

    private static PolicyViolation create() {
        return (PolicyViolation) POLICY_VIOLATION;
    }
}