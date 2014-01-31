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

package org.xmpp.extension.headers;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * A header element which hold stanza header information or internet metadata.
 *
 * @author Christian Schudt
 */
public final class Header {

    @XmlAttribute(name = "name")
    private String name;

    @XmlValue
    private String value;

    private Header() {
    }

    /**
     * Creates a header.
     *
     * @param name  The name of the header. See <a href="http://xmpp.org/extensions/xep-0131.html#registrar-shim">9.3 SHIM Headers Registry</a> for registered headers.
     * @param value The header value.
     */
    public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the name of the header.
     *
     * @return The header.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the header.
     *
     * @return The header.
     */
    public String getValue() {
        return value;
    }
}
