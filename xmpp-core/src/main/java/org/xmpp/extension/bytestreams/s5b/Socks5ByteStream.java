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

package org.xmpp.extension.bytestreams.s5b;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@code <query/>} element in the {@code http://jabber.org/protocol/bytestreams} namespace.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class Socks5ByteStream {

    public static final String NAMESPACE = "http://jabber.org/protocol/bytestreams";

    @XmlElement(name = "streamhost")
    private List<StreamHost> streamHosts = new ArrayList<>();

    @XmlElement(name = "streamhost-used")
    private StreamHostUsed streamHostUsed;

    @XmlAttribute(name = "dstaddr")
    private String dstaddr;

    @XmlElement(name = "activate")
    private Jid activate;

    @XmlAttribute(name = "mode")
    private Mode mode;

    @XmlAttribute(name = "sid")
    private String sid;

    /**
     * Creates an empty {@code <query/>} element.
     */
    public Socks5ByteStream() {
    }

    /**
     * Creates a {@code <query/>} element with an {@code <streamhost/>} child elements.
     *
     * @param sessionId   The session id.
     * @param streamHosts The stream hosts.
     * @param dstaddr     The destination address (i.e. the hash).
     */
    public Socks5ByteStream(String sessionId, List<StreamHost> streamHosts, String dstaddr) {
        this.sid = sessionId;
        this.streamHosts.addAll(streamHosts);
        this.dstaddr = dstaddr;
    }

    /**
     * Creates a {@code <query/>} element with an {@code <activate/>} child element.
     *
     * @param sessionId The session id.
     * @param jid       The JID.
     * @return The query element.
     */
    public static Socks5ByteStream activate(String sessionId, Jid jid) {
        Socks5ByteStream socks5ByteStream = new Socks5ByteStream();
        socks5ByteStream.sid = sessionId;
        socks5ByteStream.activate = jid;
        return socks5ByteStream;
    }

    /**
     * Creates a {@code <query/>} element with an {@code <streamhost-used/>} child element.
     *
     * @param jid The JID.
     * @return The query element.
     */
    public static Socks5ByteStream streamHostUsed(Jid jid) {
        Socks5ByteStream socks5ByteStream = new Socks5ByteStream();
        socks5ByteStream.streamHostUsed = new StreamHostUsed(jid);
        return socks5ByteStream;
    }

    /**
     * Creates the hexadecimal-encoded SHA-1 hash for usage in SOCKS5 negotiation.
     *
     * @param sessionId    The session id
     * @param requesterJid The requester JID
     * @param targetJid    The target JID.
     * @return The hexadecimal-encoded SHA-1 hash.
     * @see <a href="http://xmpp.org/extensions/xep-0065.html#mediated-proto-establish">6.3.2 Target Establishes SOCKS5 Connection with Proxy</a>
     */
    public static String hash(String sessionId, Jid requesterJid, Jid targetJid) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(sessionId.getBytes());
            messageDigest.update(requesterJid.toEscapedString().getBytes());
            messageDigest.update(targetJid.toEscapedString().getBytes());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the stream hosts.
     *
     * @return The stream hosts.
     */
    public List<StreamHost> getStreamHosts() {
        return streamHosts;
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
     * Gets the used stream host.
     *
     * @return The used stream host.
     */
    public Jid getStreamHostUsed() {
        return streamHostUsed != null ? streamHostUsed.jid : null;
    }

    public enum Mode {
        @XmlEnumValue("tcp")
        TCP,
        @XmlEnumValue("udp")
        UDP
    }
}
