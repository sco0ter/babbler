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

package org.xmpp.extension.bob;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The implementation of the {@code <data/>} element in the {@code urn:xmpp:bob} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0231.html">XEP-0231: Bits of Binary</a>
 * @see <a href="http://xmpp.org/extensions/xep-0231.html#schema">XML Schema</a>
 */
@XmlRootElement
public final class Data {

    @XmlAttribute
    private String cid;

    @XmlAttribute(name = "max-age")
    private int maxAge;

    @XmlAttribute
    private String type;

    @XmlValue
    private byte[] bytes;

    private Data() {
    }

    /**
     * @param cid The content id.
     */
    public Data(String cid) {
        this.cid = cid;
    }

    /**
     * @param cid    The The content id.
     * @param type   The type.
     * @param maxAge The max age.
     */
    public Data(String cid, String type, int maxAge) {
        this.cid = cid;
        this.type = type;
        this.maxAge = maxAge;
    }

    /**
     * A Content-ID that can be mapped to a cid: URL as specified in <a href="http://tools.ietf.org/html/rfc2111">RFC 2111</a>. The 'cid' value SHOULD be of the form algo+hash@bob.xmpp.org, where the "algo" is the hashing algorithm used (e.g., "sha1" for the SHA-1 algorithm as specified in <a href="http://tools.ietf.org/html/rfc3174">RFC 3174</a>) and the "hash" is the hex output of the algorithm applied to the binary data itself.
     *
     * @return The content id.
     * @see #setContentId(String)
     */
    public String getContentId() {
        return cid;
    }

    /**
     * Sets the content id.
     *
     * @param cid The content id.
     * @see #getContentId()
     */
    public void setContentId(String cid) {
        this.cid = cid;
    }

    /**
     * A suggestion regarding how long (in seconds) to cache the data; the meaning matches the Max-Age attribute from <a href="http://tools.ietf.org/html/rfc2965">RFC 2965</a>.
     *
     * @return The max age.
     * @see #setMaxAge(int)
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Sets the max age.
     *
     * @param maxAge The max age.
     * @see #getMaxAge()
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * The value of the 'type' attribute MUST match the syntax specified in <a href="http://tools.ietf.org/html/rfc2045">RFC 2045</a>. That is, the value MUST include a top-level media type, the "/" character, and a subtype; in addition, it MAY include one or more optional parameters (e.g., the "audio/ogg" MIME type in the example shown below includes a "codecs" parameter as specified in <a href="http://tools.ietf.org/html/rfc4281">RFC 4281</a>). The "type/subtype" string SHOULD be registered in the <a href="http://www.iana.org/assignments/media-types/media-types.xhtml">IANA MIME Media Types Registry</a>, but MAY be an unregistered or yet-to-be-registered value.
     *
     * @return The type.
     * @see #setType(String)
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type The type.
     * @see #getType()
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the bytes.
     *
     * @return The bytes.
     * @see #setBytes(byte[])
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Sets the bytes.
     *
     * @param bytes The bytes.
     * @see #getBytes()
     */
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
