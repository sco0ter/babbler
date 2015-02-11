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

package rocks.xmpp.extensions.hashes.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * This class represents a hash value in conjunction with its algorithm.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0300.html">XEP-0300: Use of Cryptographic Hash Functions in XMPP</a>
 * @see <a href="http://xmpp.org/extensions/xep-0300.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "hash")
public final class Hash {

    /**
     * urn:xmpp:hashes:1
     */
    public static final String NAMESPACE = "urn:xmpp:hashes:1";

    @XmlValue
    private byte[] value;

    @XmlAttribute(name = "algo")
    private String algorithm;

    private Hash() {
    }

    /**
     * Creates hash value.
     *
     * @param value     The hash value.
     * @param algorithm The hash algorithm.
     */
    public Hash(byte[] value, String algorithm) {
        this.value = value;
        this.algorithm = algorithm;
    }

    /**
     * Gets the hash algorithm.
     *
     * @return The hash algorithm.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Gets the hash value.
     *
     * @return The hash value.
     */
    public byte[] getValue() {
        return value;
    }
}
