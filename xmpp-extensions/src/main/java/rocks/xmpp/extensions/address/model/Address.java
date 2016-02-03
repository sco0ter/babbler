/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.extensions.address.model;

import rocks.xmpp.addr.Jid;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The implementation of the {@code <address/>} element in the {@code http://jabber.org/protocol/address} namespace.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0033.html">XEP-0033: Extended Stanza Addressing</a>
 * @see <a href="http://xmpp.org/extensions/xep-0033.html#schema">XML Schema</a>
 * @see Addresses
 */
public final class Address {

    /**
     * http://jabber.org/protocol/address
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/address";

    @XmlAttribute
    private final Type type;

    @XmlAttribute
    private final Jid jid;

    @XmlAttribute
    private final String desc;

    @XmlAttribute
    private final String node;

    @XmlAttribute
    private final URI uri;

    @XmlAnyElement(lax = true)
    private final List<Object> extensions = new ArrayList<>();

    private Address() {
        this.type = null;
        this.jid = null;
        this.desc = null;
        this.node = null;
        this.uri = null;
    }

    /**
     * @param type       The address type.
     * @param jid        Specifies a simple Jabber ID associated with this address.
     * @param extensions The extensions.
     */
    public Address(Type type, Jid jid, Object... extensions) {
        this(type, jid, null, extensions);
    }

    /**
     * @param type        The address type.
     * @param jid         Specifies a simple Jabber ID associated with this address.
     * @param description Specifies human-readable information for this address. This data may be used by clients to provide richer address-book integration.
     * @param extensions  The extensions.
     */
    public Address(Type type, Jid jid, CharSequence description, Object... extensions) {
        this(type, jid, description, null, extensions);
    }

    /**
     * @param type        The address type.
     * @param jid         Specifies a simple Jabber ID associated with this address.
     * @param description Specifies human-readable information for this address. This data may be used by clients to provide richer address-book integration.
     * @param node        Specifies a sub-addressable unit at a particular JID, corresponding to a Service Discovery node.
     * @param extensions  The extensions.
     */
    public Address(Type type, Jid jid, CharSequence description, CharSequence node, Object... extensions) {
        this.type = Objects.requireNonNull(type);
        this.jid = jid;
        this.desc = description != null ? description.toString() : null;
        this.node = node != null ? node.toString() : null;
        this.uri = null;
        this.extensions.addAll(Arrays.asList(extensions));
    }

    /**
     * @param type        The address type.
     * @param uri         Specifies an external system address, such as a sip:, sips:, or im: URI.
     * @param description Specifies human-readable information for this address. This data may be used by clients to provide richer address-book integration.
     * @param extensions  The extensions.
     */
    public Address(Type type, URI uri, CharSequence description, Object... extensions) {
        // If the 'uri' attribute is specified, the 'jid' and 'node' attributes MUST NOT be specified.
        this.type = Objects.requireNonNull(type);
        this.jid = null;
        this.uri = uri;
        this.desc = description != null ? description.toString() : null;
        this.node = null;
        this.extensions.addAll(Arrays.asList(extensions));
    }

    /**
     * Gets the address type.
     *
     * @return The address type.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type">4.6 'type' attribute</a>
     */
    public final Type getType() {
        return type;
    }

    /**
     * Gets the JID.
     *
     * @return The JID.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-jid">4.1 'jid' attribute</a>
     */
    public final Jid getJid() {
        return jid;
    }

    /**
     * Gets the description. It specifies human-readable information for this address. This data may be used by clients to provide richer address-book integration.
     *
     * @return The description.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-desc">4.4 'desc' attribute</a>
     */
    public final String getDescription() {
        return desc;
    }

    /**
     * Gets the node. It specifies a sub-addressable unit at a particular JID, corresponding to a Service Discovery node.
     *
     * @return The node.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-node">4.3 'node' attribute</a>
     */
    public final String getNode() {
        return node;
    }

    /**
     * Gets the URI. It specifies an external system address, such as a sip:, sips:, or im: URI.
     *
     * @return The URI.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-uri">4.2 'uri' attribute</a>
     */
    public final URI getUri() {
        return uri;
    }

    /**
     * Gets the extensions as unmodifiable list.
     *
     * @return The extensions.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#extensibility">4.7 Extensibility</a>
     */
    public final List<Object> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

    /**
     * Gets the extension.
     *
     * @return The extension or null.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#extensibility">4.7 Extensibility</a>
     */
    @SuppressWarnings("unchecked")
    public final <T> T getExtension(Class<T> clazz) {
        for (Object extension : extensions) {
            if (clazz.isAssignableFrom(extension.getClass())) {
                return (T) extension;
            }
        }
        return null;
    }

    /**
     * The type of the address.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type">4.6 'type' attribute</a>
     */
    public enum Type {
        /**
         * These addressees should receive 'blind carbon copies' of the stanza. This means that the server MUST remove these addresses before the stanza is delivered to anyone other than the given bcc addressee or the multicast service of the bcc addressee.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type-bcc">4.6.3 Address type='bcc'</a>
         */
        @XmlEnumValue(value = "bcc")
        BCC,
        /**
         * These addressees are the secondary recipients of the stanza.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type-cc">4.6.2 Address type='cc'</a>
         */
        @XmlEnumValue(value = "cc")
        CC,
        /**
         * This address type contains no actual address information. Instead, it means that the receiver SHOULD NOT reply to the message. This is useful when broadcasting messages to many receivers.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type-noreply">4.6.6 Address type='noreply'</a>
         */
        @XmlEnumValue(value = "noreply")
        NOREPLY,
        /**
         * This is the JID of a Multi-User Chat (XEP-0045) [5] room to which responses should be sent. When a user wants to reply to this stanza, the client SHOULD join this room first. Clients SHOULD respect this request unless an explicit override occurs. There MAY be more than one replyto or replyroom on a stanza, in which case the reply stanza MUST be routed to all of the addresses.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type-replyroom">4.6.5 Address type='replyroom'</a>
         */
        @XmlEnumValue(value = "replyroom")
        REPLYROOM,
        /**
         * This is the address to which all replies are requested to be sent. Clients SHOULD respect this request unless an explicit override occurs. There MAY be more than one replyto or replyroom on a stanza, in which case the reply stanza MUST be routed to all of the addresses.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type-replyto">4.6.4 Address type='replyto'</a>
         */
        @XmlEnumValue(value = "replyto")
        REPLYTO,
        /**
         * These addressees are the primary recipients of the stanza.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type-to">4.6.1 Address type='to'</a>
         */
        @XmlEnumValue(value = "to")
        TO,
        /**
         * In Multi-User Chat, if the room is non-anonymous, notes the original full JID of the sender.
         *
         * @see <a href="http://xmpp.org/extensions/xep-0045.html#enter-history">7.2.14 Discussion History</a>
         */
        @XmlEnumValue(value = "ofrom")
        OFROM
    }
}
