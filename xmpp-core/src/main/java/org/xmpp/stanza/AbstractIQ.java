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

package org.xmpp.stanza;

import org.xmpp.Jid;

import javax.xml.bind.annotation.*;
import java.util.UUID;

/**
 * The implementation of the {@code <iq/>} stanza.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-semantics-iq">8.2.3.  IQ Semantics</a></cite></p>
 * <p>Info/Query, or IQ, is a "request-response" mechanism, similar in some ways to the Hypertext Transfer Protocol [HTTP]. The semantics of IQ enable an entity to make a request of, and receive a response from, another entity. The data content of the request and response is defined by the schema or other structural definition associated with the XML namespace that qualifies the direct child element of the IQ element (see Section 8.4), and the interaction is tracked by the requesting entity through use of the 'id' attribute. Thus, IQ interactions follow a common pattern of structured data exchange such as get/result or set/result (although an error can be returned in reply to a request if appropriate)</p>
 * <div>To enforce these semantics, the following rules apply:
 * <ol>
 * <li>The 'id' attribute is REQUIRED for IQ stanzas.</li>
 * <li>The 'type' attribute is REQUIRED for IQ stanzas. The value MUST be one of the following; if not, the recipient or an intermediate router MUST return a {@code <bad-request/>} stanza error (Section 8.3.3.1).
 * <ul>
 * <li>get -- The stanza requests information, inquires about what data is needed in order to complete further operations, etc.</li>
 * <li>set -- The stanza provides data that is needed for an operation to be completed, sets new values, replaces existing values, etc.</li>
 * <li>result -- The stanza is a response to a successful get or set request. </li>
 * <li>error -- The stanza reports an error that has occurred regarding processing or delivery of a get or set request (see Section 8.3).</li>
 * </ul>
 * </li>
 * <li>An entity that receives an IQ request of type "get" or "set" MUST reply with an IQ response of type "result" or "error". The response MUST preserve the 'id' attribute of the request (or be empty if the generated stanza did not include an 'id' attribute).</li>
 * <li>An entity that receives a stanza of type "result" or "error" MUST NOT respond to the stanza by sending a further IQ response of type "result" or "error"; however, the requesting entity MAY send another request (e.g., an IQ of type "set" to provide obligatory information discovered through a get/result pair).</li>
 * <li>An IQ stanza of type "get" or "set" MUST contain exactly one child element, which specifies the semantics of the particular request.</li>
 * <li>An IQ stanza of type "result" MUST include zero or one child elements.</li>
 * <li>An IQ stanza of type "error" MAY include the child element contained in the associated "get" or "set" and MUST include an {@code <error/>} child.</li>
 * </ol>
 * </div>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlTransient
@XmlType(propOrder = {"from", "id", "to", "type", "extension", "error"})
public abstract class AbstractIQ extends Stanza {

    @XmlAttribute
    private Type type;

    @XmlAnyElement(lax = true)
    private Object extension;

    /**
     * Default constructor for unmarshalling.
     */
    @SuppressWarnings("unused")
    protected AbstractIQ() {
    }

    /**
     * Creates an IQ stanza with the given type. The id attribute will be generated randomly.
     *
     * @param type The type.
     */
    protected AbstractIQ(Type type) {
        // The 'id' attribute is REQUIRED for IQ stanzas.
        this(UUID.randomUUID().toString(), type);
    }

    /**
     * Creates an IQ stanza with the given type and extension. The id attribute will be generated randomly.
     *
     * @param type      The type.
     * @param extension The extension.
     */
    protected AbstractIQ(Type type, Object extension) {
        this(UUID.randomUUID().toString(), type, extension);
    }

    /**
     * Creates an IQ stanza with the given id and type.
     * Not that, if the type is {@link Type#SET} or {@link Type#GET}, you will have to also set an extension.
     *
     * @param id   The id.
     * @param type The type.
     */
    protected AbstractIQ(String id, Type type) {
        this(id, type, null);
    }

    /**
     * Creates an IQ stanza with the given id, type and extension.
     *
     * @param id        The id.
     * @param type      The type.
     * @param extension The extension.
     */
    protected AbstractIQ(String id, Type type, Object extension) {
        this(null, id, type, extension);
    }

    /**
     * Creates an IQ stanza with the given receiver, type and extension. The id attribute will be generated randomly.
     *
     * @param to        The receiver.
     * @param type      The type.
     * @param extension The extension.
     */
    protected AbstractIQ(Jid to, Type type, Object extension) {
        this(to, UUID.randomUUID().toString(), type, extension);
    }

    /**
     * Creates an IQ stanza with the given receiver, id, type and extension.
     *
     * @param to        The receiver.
     * @param id        The id.
     * @param type      The type.
     * @param extension The extension.
     */
    protected AbstractIQ(Jid to, String id, Type type, Object extension) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null.");
        }
        this.to = to;
        this.id = id;
        this.type = type;
        this.extension = extension;
    }

    /**
     * Gets the type.
     *
     * @return The type.
     */
    public final Type getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T getExtension(Class<T> type) {
        if (extension != null && type != null && type.isAssignableFrom(extension.getClass())) {
            return (T) extension;
        }
        return null;
    }

    /**
     * Sets the extension.
     *
     * @param extension The extension.
     */
    public final void setExtension(Object extension) {
        this.extension = extension;
    }

    /**
     * Represents a {@code <iq/>} 'type' attribute.
     */
    @XmlEnum
    @XmlType(name = "iqType")
    public enum Type {
        /**
         * The stanza reports an error that has occurred regarding processing or delivery of a get or set request.
         */
        @XmlEnumValue(value = "error")
        ERROR,
        /**
         * The stanza requests information, inquires about what data is needed in order to complete further operations, etc.
         */
        @XmlEnumValue(value = "get")
        GET,
        /**
         * The stanza is a response to a successful get or set request.
         */
        @XmlEnumValue(value = "result")
        RESULT,
        /**
         * The stanza provides data that is needed for an operation to be completed, sets new values, replaces existing values, etc.
         */
        @XmlEnumValue(value = "set")
        SET
    }
}
