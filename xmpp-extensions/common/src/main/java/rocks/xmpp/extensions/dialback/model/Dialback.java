/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.extensions.dialback.model;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.server.ServerStanzaError;
import rocks.xmpp.extensions.dialback.model.feature.DialbackFeature;

/**
 * The implementation of the dialback elements {@code <result/>} and {@code <verify/>} in the {@code jabber:server:dialback} namespace.
 * <p>
 * To generate a dialback key use {@link #generateKey(String, String, String, String)}.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0220.html">XEP-0220: Server Dialback</a>
 * @see Result
 * @see Verify
 */
@XmlTransient
@XmlSeeAlso({Dialback.Result.class, Dialback.Verify.class, DialbackFeature.class})
public abstract class Dialback {

    /**
     * jabber:server:dialback
     */
    public static final String NAMESPACE = "jabber:server:dialback";

    @XmlAttribute
    private final Jid from;

    @XmlAttribute
    private final Jid to;

    @XmlAttribute
    private final Type type;

    @XmlMixed
    @XmlElementRef(type = ServerStanzaError.class)
    private final List<Object> mixedContent = new ArrayList<>();

    private Dialback() {
        this.from = null;
        this.to = null;
        this.type = null;
    }

    private Dialback(final Jid from, final Jid to, final String key, final Boolean valid) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.type = valid != null ? valid ? Type.VALID : Type.INVALID : null;
        this.mixedContent.add(key);
    }

    private Dialback(final Jid from, final Jid to, final StanzaError stanzaError) {
        this.from = from;
        this.to = to;
        this.type = Type.ERROR;
        this.mixedContent.add(ServerStanzaError.from(stanzaError));
    }

    /**
     * Generates a key using the following recommended algorithm.
     * <pre>
     *     HMAC-SHA256
     *       (
     *         SHA256(Secret),
     *         {
     *           Receiving Server, ' ',
     *           Originating Server, ' ',
     *           Stream ID
     *         }
     *       )
     * </pre>
     *
     * @param secret            The secret.
     * @param receivingServer   The receiving server.
     * @param originatingServer The originating server.
     * @param streamId          The stream id.
     * @return The dialback key.
     * @see <a href="https://xmpp.org/extensions/xep-0185.html">XEP-0185: Dialback Key Generation and Validation</a>
     */
    public static String generateKey(final String secret, final String receivingServer, final String originatingServer, final String streamId) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");

            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] sha = digest.digest(secret.getBytes(StandardCharsets.UTF_8));

            final Key key = new SecretKeySpec(DatatypeConverter.printHexBinary(sha).toLowerCase(Locale.ENGLISH).getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            mac.update((receivingServer + ' ' + originatingServer + ' ' + streamId).getBytes(StandardCharsets.UTF_8));

            return DatatypeConverter.printHexBinary(mac.doFinal()).toLowerCase(Locale.ENGLISH);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AssertionError(e);
        }
    }

    public final Jid getFrom() {
        return from;
    }

    public final Jid getTo() {
        return to;
    }

    public final boolean isValid() {
        return type == null || type == Type.VALID;
    }

    public final String getKey() {
        return mixedContent.stream()
                       .filter(o -> o instanceof String)
                       .map(o -> (String) o)
                       .findAny()
                       .orElse(null);
    }

    public final StanzaError getError() {
        return mixedContent.stream()
                       .filter(o -> o instanceof StanzaError)
                       .map(o -> (StanzaError) o)
                       .findAny()
                       .orElse(null);
    }

    private enum Type {
        @XmlEnumValue("error")
        ERROR,
        @XmlEnumValue("invalid")
        INVALID,
        @XmlEnumValue("valid")
        VALID
    }

    /**
     * An outbound request for authorization by receiving server or a verification result from the receiving server.
     */
    @XmlRootElement(name = "result")
    public static final class Result extends Dialback {

        private Result() {
        }

        /**
         * Creates a verification request from the initiating server to the receiving server.
         *
         * @param from The sender domain.
         * @param to   The target domain.
         * @param key  The dialback key.
         * @see #generateKey(String, String, String, String)
         */
        public Result(final Jid from, final Jid to, final String key) {
            super(from, to, Objects.requireNonNull(key), null);
        }

        /**
         * Creates a valid or invalid verification result from the receiving server to the initiating server.
         *
         * @param from  The sender domain.
         * @param to    The target domain.
         * @param valid If the key could be verified.
         */
        public Result(final Jid from, final Jid to, final boolean valid) {
            super(from, to, null, valid);
        }

        /**
         * Creates a dialback error from the receiving server to the initiating server.
         *
         * @param from  The sender domain.
         * @param to    The target domain.
         * @param error The error.
         */
        public Result(final Jid from, final Jid to, final StanzaError error) {
            super(from, to, error);
        }
    }

    /**
     * A verification request sent from the receiving server to the authoritative server or a verification result sent in the opposite direction.
     */
    @XmlRootElement(name = "verify")
    public static final class Verify extends Dialback {

        @XmlAttribute
        private final String id;

        private Verify() {
            this.id = null;
        }

        /**
         * Creates a verification request from the receiving server to the authoritative server.
         *
         * @param from The target domain.
         * @param to   The sender domain.
         * @param id   The stream ID of the response stream header sent from the Receiving Server to the Initiating Server.
         * @param key  The dialback key.
         */
        public Verify(final Jid from, final Jid to, final String id, final String key) {
            super(from, to, key, null);
            this.id = id;
        }

        /**
         * Creates a verification result from the authoritative server to the receiving server.
         *
         * @param from  The sender domain.
         * @param to    The target domain.
         * @param id    The stream ID.
         * @param valid If the key could be verified.
         */
        public Verify(final Jid from, final Jid to, final String id, final boolean valid) {
            super(from, to, null, valid);
            this.id = id;
        }

        /**
         * Creates a dialback error from the authoritative server to the receiving server.
         *
         * @param from  The sender domain.
         * @param to    The target domain.
         * @param id    The stream id.
         * @param error The error.
         */
        public Verify(final Jid from, final Jid to, final String id, final StanzaError error) {
            super(from, to, error);
            this.id = id;
        }

        /**
         * Gets the stream id.
         *
         * @return The stream id.
         */
        public final String getId() {
            return id;
        }
    }
}
