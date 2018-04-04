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
 * The implementation of the {@code <subscription-required/>} stanza error.
 * <blockquote>
 * <p><cite><a href="https://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-subscription-required">8.3.3.20.  subscription-required</a></cite></p>
 * <p>The requesting entity is not authorized to access the requested service because a prior subscription is necessary (examples of prior subscription include authorization to receive presence information as defined in [XMPP-IM] and opt-in data feeds for XMPP publish-subscribe as defined in [XEP-0060]); the associated error type SHOULD be "auth".</p>
 * </blockquote>
 * This class is a singleton.
 *
 * @see #SUBSCRIPTION_REQUIRED
 */
@XmlRootElement(name = "subscription-required")
@XmlType(factoryMethod = "create")
final class SubscriptionRequired extends Condition {

    SubscriptionRequired() {
    }

    private static SubscriptionRequired create() {
        return (SubscriptionRequired) SUBSCRIPTION_REQUIRED;
    }
}