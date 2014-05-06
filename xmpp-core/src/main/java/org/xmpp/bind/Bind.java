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

package org.xmpp.bind;

import org.xmpp.Jid;
import org.xmpp.stream.Feature;
import org.xmpp.JidAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The implementation of the {@code <bind/>} element, which is used during resource binding.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#bind-feature">7.4.  Advertising Support</a></cite></p>
 * <p>Upon sending a new response stream header to the client after successful SASL negotiation, the server MUST include a {@code <bind/>} element qualified by the 'urn:ietf:params:xml:ns:xmpp-bind' namespace in the stream features it presents to the client.</p>
 * </blockquote>
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#bind-servergen-success">7.6.1.  Success Case</a></cite></p>
 * <p>A client requests a server-generated resourcepart by sending an IQ stanza of type "set" (see Section 8.2.3) containing an empty {@code <bind/>} element qualified by the 'urn:ietf:params:xml:ns:xmpp-bind' namespace.</p>
 * <p>Once the server has generated an XMPP resourcepart for the client, it MUST return an IQ stanza of type "result" to the client, which MUST include a {@code <jid/>} child element that specifies the full JID for the connected resource as determined by the server. </p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement
public final class Bind extends Feature {

    @XmlElement
    @XmlJavaTypeAdapter(JidAdapter.class)
    private Jid jid;

    @XmlElement
    private String resource;

    /**
     * Creates a {@code <bind/>} element.
     */
    public Bind() {
    }

    /**
     * Creates a {@code <bind/>} element with a resource.
     *
     * @param resource The resource.
     */
    public Bind(String resource) {
        this.resource = resource;
    }

    /**
     * Gets the JID, which has been generated by the server after resource binding.
     *
     * @return The JID.
     */
    public Jid getJid() {
        return jid;
    }

    @Override
    public boolean isMandatory() {
        return true;
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
