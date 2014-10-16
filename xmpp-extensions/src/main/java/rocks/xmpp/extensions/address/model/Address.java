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

package rocks.xmpp.extensions.address.model;

import rocks.xmpp.core.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import java.net.URI;

/**
 * The implementation of the {@code <address/>} element in the {@code http://jabber.org/protocol/address} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0033.html">XEP-0033: Extended Stanza Addressing</a>
 * @see <a href="http://xmpp.org/extensions/xep-0033.html#schema">XML Schema</a>
 * @see Addresses
 */
public final class Address {

    @XmlAttribute(name = "type")
    private Type type;

    @XmlAttribute(name = "jid")
    private Jid jid;

    @XmlAttribute(name = "desc")
    private String description;

    @XmlAttribute(name = "node")
    private String node;

    @XmlAttribute(name = "uri")
    private URI uri;

    private Address() {
    }

    /**
     * @param type The address type.
     * @param jid  Specifies a simple Jabber ID associated with this address.
     */
    public Address(Type type, Jid jid) {
        this(type, jid, null);
    }

    /**
     * @param type        The address type.
     * @param jid         Specifies a simple Jabber ID associated with this address.
     * @param description Specifies human-readable information for this address. This data may be used by clients to provide richer address-book integration.
     */
    public Address(Type type, Jid jid, String description) {
        this(type, jid, description, null);
    }

    /**
     * @param type        The address type.
     * @param jid         Specifies a simple Jabber ID associated with this address.
     * @param description Specifies human-readable information for this address. This data may be used by clients to provide richer address-book integration.
     * @param node        Specifies a sub-addressable unit at a particular JID, corresponding to a Service Discovery node.
     */
    public Address(Type type, Jid jid, String description, String node) {
        this.type = type;
        this.jid = jid;
        this.description = description;
        this.node = node;
    }

    /**
     * @param type        The address type.
     * @param uri         Specifies an external system address, such as a sip:, sips:, or im: URI.
     * @param description Specifies human-readable information for this address. This data may be used by clients to provide richer address-book integration.
     */
    public Address(Type type, URI uri, String description) {
        // If the 'uri' attribute is specified, the 'jid' and 'node' attributes MUST NOT be specified.
        this.type = type;
        this.uri = uri;
        this.description = description;
    }

    /**
     * Gets the address type.
     *
     * @return The address type.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-type">4.6 'type' attribute</a>
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the JID.
     *
     * @return The JID.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-jid">4.1 'jid' attribute</a>
     */
    public Jid getJid() {
        return jid;
    }

    /**
     * Gets the description. It specifies human-readable information for this address. This data may be used by clients to provide richer address-book integration.
     *
     * @return The description.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-desc">4.4 'desc' attribute</a>
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the node. It specifies a sub-addressable unit at a particular JID, corresponding to a Service Discovery node.
     *
     * @return The node.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-node">4.3 'node' attribute</a>
     */
    public String getNode() {
        return node;
    }

    /**
     * Gets the URI. It specifies an external system address, such as a sip:, sips:, or im: URI.
     *
     * @return The URI.
     * @see <a href="http://xmpp.org/extensions/xep-0033.html#addr-uri">4.2 'uri' attribute</a>
     */
    public URI getUri() {
        return uri;
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
        TO
    }
}
