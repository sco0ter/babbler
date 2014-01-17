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

package org.xmpp.tls;

import org.xmpp.stream.Feature;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Represents the STARTTLS feature and initiates the TLS negotiation process.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#tls-process-initiate-command">5.4.2.1.  STARTTLS Command</a></cite></p>
 * <p>In order to begin the STARTTLS negotiation, the initiating entity issues the STARTTLS command (i.e., a {@code <starttls/>} element qualified by the 'urn:ietf:params:xml:ns:xmpp-tls' namespace) to instruct the receiving entity that it wishes to begin a STARTTLS negotiation to secure the stream.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "starttls")
@XmlSeeAlso({Proceed.class, Failure.class})
public final class StartTls extends Feature {

    @XmlElement
    protected String required;

    @Override
    public boolean isMandatory() {
        return required != null;
    }

    @Override
    public int getPriority() {
        return -2;
    }

    public void setMandatory(boolean mandatory) {
        if (mandatory) {
            required = "";
        } else {
            required = null;
        }
    }
}
