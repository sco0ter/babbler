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

package rocks.xmpp.core.stanza.model;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.errors.Condition;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.EnumSet;
import java.util.Objects;
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
public abstract class AbstractIQ extends Stanza {

    @XmlAttribute
    private final Type type;

    @XmlAnyElement(lax = true)
    private Object extension;

    /**
     * Default constructor for unmarshalling.
     */
    @SuppressWarnings("unused")
    protected AbstractIQ() {
        this.type = null;
    }

    /**
     * Creates an IQ stanza with the given receiver, id, type and extension.
     *
     * @param to        The receiver.
     * @param id        The id.
     * @param type      The type.
     * @param extension The extension.
     */
    protected AbstractIQ(Jid to, Type type, Object extension, String id, Jid from, String language, StanzaError error) {
        super(to, from, id == null ? UUID.randomUUID().toString() : id, language, error);
        this.type = Objects.requireNonNull(type, "type must not be null.");
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

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder("IQ");
        if (type != null) {
            sb.append("-").append(type.name().toLowerCase());
        }
        return sb.append(super.toString()).toString();
    }

    /**
     * Returns if this is a request IQ, i.e. of type get or set.
     *
     * @return True if this is a get or set IQ.
     */
    public final boolean isRequest() {
        return EnumSet.of(Type.GET, Type.SET).contains(type);
    }

    /**
     * Returns true, if this IQ is a response (either a positive result or an error).
     *
     * @return If this is a result or error IQ.
     */
    public final boolean isResponse() {
        return EnumSet.of(Type.RESULT, Type.ERROR).contains(type);
    }

    /**
     * Creates a result IQ stanza, i.e. it uses the same id as this IQ, sets the type to 'result' and switches the 'to' and 'from' attribute.
     *
     * @return The result IQ stanza.
     */
    public abstract AbstractIQ createResult();

    /**
     * Creates a result IQ stanza with a payload, i.e. it uses the same id as this IQ, sets the type to 'result' and switches the 'to' and 'from' attribute.
     *
     * @param extension The extension.
     * @return The result IQ stanza.
     */
    public abstract AbstractIQ createResult(Object extension);

    @Override
    public abstract AbstractIQ createError(StanzaError error);

    @Override
    public abstract AbstractIQ createError(Condition condition);

    /**
     * Represents a {@code <iq/>} 'type' attribute.
     */
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
