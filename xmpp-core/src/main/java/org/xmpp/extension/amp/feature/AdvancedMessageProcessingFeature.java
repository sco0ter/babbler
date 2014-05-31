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

package org.xmpp.extension.amp.feature;

import org.xmpp.stream.Feature;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <amp/>} element in the {@code http://jabber.org/features/amp} namespace.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0079.html#streamfeature">Stream Feature</a></cite></p>
 * <p><a href="http://tools.ietf.org/html/rfc6120">XMPP Core</a> defines methods for advertising feature support during stream negotiation. For the sake of efficiency, it may be desirable for a server to advertise support for Advanced Message Processing as a stream feature. The namespace for reporting support within {@code <stream:features/>} is "http://jabber.org/features/amp". Upon receiving a stream header from the initiating entity, the receiving entity (usually a server) returns a stream header to initiating entity and MAY announce support for Advanced Message Processing registration by including the relevant stream feature</p>
 * </blockquote>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0079.html">XEP-0079: Advanced Message Processing</a>
 * @see <a href="http://xmpp.org/extensions/xep-0079.html#schemas-streams">XML Schema</a>
 */
@XmlRootElement(name = "amp")
public final class AdvancedMessageProcessingFeature extends Feature {

    private AdvancedMessageProcessingFeature() {
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
