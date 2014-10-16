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

package rocks.xmpp.extensions.bytestreams.ibb.model;

import javax.xml.bind.annotation.*;

/**
 * This class is a container for the three different In-Band ByteStream elements and provides the namespace for IBB, so that it can be used by other protocols such as XEP-0095.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a>
 */
@XmlTransient
@XmlSeeAlso({InBandByteStream.Open.class, InBandByteStream.Data.class, InBandByteStream.Close.class})
public final class InBandByteStream {

    /**
     * The IBB namespace <code>http://jabber.org/protocol/ibb</code>.
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/ibb";

    private InBandByteStream() {
    }

    /**
     * The implementation of the {@code <open/>} element in the {@code http://jabber.org/protocol/ibb} namespace.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a>
     * @see <a href="http://xmpp.org/extensions/xep-0047.html#schema">XML Schema</a>
     */
    @XmlRootElement
    public static final class Open {
        @XmlAttribute(name = "block-size")
        private int blockSize;

        @XmlAttribute
        private String sid;

        @XmlAttribute
        private StanzaType stanza;

        private Open() {
        }

        /**
         * Creates the {@code <open/>} element.
         *
         * @param blockSize The block size.
         * @param sessionId The session id.
         */
        public Open(int blockSize, String sessionId) {
            this.blockSize = blockSize;
            this.sid = sessionId;
        }

        /**
         * Gets the block size;
         *
         * @return The block size.
         */
        public int getBlockSize() {
            return blockSize;
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
         * Gets the stanza type.
         *
         * @return The stanza type.
         */
        public StanzaType getStanzaType() {
            return stanza;
        }

        /**
         * The stanza type to be used for IBB.
         */
        public enum StanzaType {
            /**
             * IQ stanzas are used (default)
             */
            @XmlEnumValue("iq")
            IQ,
            /**
             * Message stanzas are used.
             */
            @XmlEnumValue("message")
            MESSAGE
        }
    }

    /**
     * The implementation of the {@code <data/>} element in the {@code http://jabber.org/protocol/ibb} namespace.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a>
     * @see <a href="http://xmpp.org/extensions/xep-0047.html#schema">XML Schema</a>
     */
    @XmlRootElement
    public static final class Data {

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
         * Creates the {@code <data/>} element.
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

    /**
     * The implementation of the {@code <close/>} element in the {@code http://jabber.org/protocol/ibb} namespace.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a>
     * @see <a href="http://xmpp.org/extensions/xep-0047.html#schema">XML Schema</a>
     */
    @XmlRootElement
    public static final class Close {

        @XmlAttribute(name = "sid")
        private String sid;

        private Close() {
        }

        /**
         * Creates the {@code <close/>} element.
         *
         * @param sessionId The session id.
         */
        public Close(String sessionId) {
            this.sid = sessionId;
        }

        /**
         * Gets the session id.
         *
         * @return The session id.
         */
        public String getSessionId() {
            return sid;
        }
    }
}
