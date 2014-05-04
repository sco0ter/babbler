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

package org.xmpp.extension.stream.ibb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Christian Schudt
 */
@XmlRootElement
public final class Data {

    @XmlAttribute
    private String sid;

    @XmlAttribute
    private Integer seq;

    @XmlValue
    private byte[] bytes;

    /**
     * Private constructor for unmarshalling.
     */
    private Data() {

    }

    /**
     * Creates a new data element.
     *
     * @param bytes The bytes.
     * @param sid   The session id.
     * @param seq   The sequence number.
     */
    public Data(byte[] bytes, String sid, int seq) {
        this.bytes = bytes;
        this.sid = sid;
        this.seq = seq;
    }

    /**
     * Gets the sequence number.
     *
     * @return The sequence number.
     */
    public int getSequence() {
        return seq;
    }

    /**
     * Gets the session id.
     *
     * @return The session id.
     */
    public String getSessionId() {
        return sid;
    }

    /**
     * Gets the bytes.
     *
     * @return The bytes.
     */
    public byte[] getBytes() {
        return bytes;
    }
}
