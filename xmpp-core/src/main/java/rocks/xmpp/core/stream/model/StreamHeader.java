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

package rocks.xmpp.core.stream.model;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.model.SessionOpen;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents the XMPP stream header.
 * <p>
 * Usage:
 * <pre>
 * {@code
 * StreamHeader streamHeader = StreamHeader.initialClientToServer(from, to, Locale.GERMAN);
 * streamHeader.writeTo(xmlStreamWriter);
 * }
 * </pre>
 *
 * @author Christian Schudt
 */
public final class StreamHeader implements SessionOpen {

    private final Jid from;

    private final Jid to;

    private final String id;

    private final Locale lang;

    private final String contentNamespace;

    private final List<QName> additionalNamespaces = new ArrayList<>();

    /**
     * @param from                 The 'from' attribute specifies an XMPP identity of the entity sending the stream element.
     * @param to                   The 'to' attribute.
     * @param id                   The 'id' attribute specifies a unique identifier for the stream, called a "stream ID".
     * @param lang                 The 'xml:lang' attribute specifies an entity's preferred or default language for any human-readable XML character data to be sent over the stream.
     * @param additionalNamespaces Any optional additional namespace declarations.
     */
    private StreamHeader(Jid from, Jid to, String id, Locale lang, String contentNamespace, QName... additionalNamespaces) {
        this.from = from;
        this.to = to;
        this.id = id;
        this.lang = lang;
        this.contentNamespace = contentNamespace;
        this.additionalNamespaces.addAll(Arrays.asList(additionalNamespaces));
    }

    /**
     * Creates an initial stream header for client-to-server streams.
     *
     * @param from                 The XMPP identity of the principal controlling the client, i.e., a JID of the form {@code localpart@domainpart>}.
     * @param to                   A domainpart that the initiating entity knows or expects the receiving entity to service.
     * @param lang                 An entity's preferred or default language for any human-readable XML character data to be sent over the stream.
     * @param additionalNamespaces Any optional additional namespace declarations.
     * @return The stream header.
     */
    public static StreamHeader initialClientToServer(Jid from, Jid to, Locale lang, QName... additionalNamespaces) {
        // For initial stream headers in both client-to-server and server-to-server communication, the initiating entity MUST include the 'to' attribute and MUST set its value to a domainpart that the initiating entity knows or expects the receiving entity to service.
        // For initial stream headers, the initiating entity MUST NOT include the 'id' attribute;
        return initialClientToServer(from, to, lang, "jabber:client", additionalNamespaces);
    }

    /**
     * Creates an initial stream header for client-to-server or component-to-server streams.
     *
     * @param from                 The XMPP identity of the principal controlling the client, i.e., a JID of the form {@code localpart@domainpart>}.
     * @param to                   A domainpart that the initiating entity knows or expects the receiving entity to service.
     * @param lang                 An entity's preferred or default language for any human-readable XML character data to be sent over the stream.
     * @param contentNamespace     The content namespace.
     * @param additionalNamespaces Any optional additional namespace declarations.
     * @return The stream header.
     */
    public static StreamHeader initialClientToServer(Jid from, Jid to, Locale lang, String contentNamespace, QName... additionalNamespaces) {
        // For initial stream headers in both client-to-server and server-to-server communication, the initiating entity MUST include the 'to' attribute and MUST set its value to a domainpart that the initiating entity knows or expects the receiving entity to service.
        // For initial stream headers, the initiating entity MUST NOT include the 'id' attribute;
        return new StreamHeader(from, Objects.requireNonNull(to).asBareJid().withLocal(null), null, lang, contentNamespace, additionalNamespaces);
    }

    /**
     * Creates a response stream header for client-to-server streams.
     *
     * @param from                 One of the receiving entity's FQDNs.
     * @param to                   The bare JID specified in the 'from' attribute of the initial stream header
     * @param id                   A unique identifier for the stream, called a "stream ID".
     * @param lang                 An entity's preferred or default language for any human-readable XML character data to be sent over the stream.
     * @param additionalNamespaces Any optional additional namespace declarations.
     * @return The stream header.
     */
    public static StreamHeader responseClientToServer(Jid from, Jid to, String id, Locale lang, QName... additionalNamespaces) {
        // For response stream headers in both client-to-server and server-to-server communication, the receiving entity MUST include the 'from' attribute and MUST set its value to one of the receiving entity's FQDNs
        // For response stream headers in client-to-server communication, if the client included a 'from' attribute in the initial stream header then the server MUST include a 'to' attribute in the response stream header and MUST set its value to the bare JID specified in the 'from' attribute of the initial stream header.
        // For response stream headers, the receiving entity MUST include the 'id' attribute.
        // For response stream headers, the receiving entity MUST include the 'xml:lang' attribute.
        return new StreamHeader(Objects.requireNonNull(from).asBareJid().withLocal(null), to != null ? to.asBareJid() : null, Objects.requireNonNull(id), Objects.requireNonNull(lang), "jabber:client", additionalNamespaces);
    }

    /**
     * Creates an initial stream header for server-to-server streams.
     *
     * @param from                 One of the configured FQDNs of the server, i.e., a JID of the form {@code <domainpart>}.
     * @param to                   A domainpart that the initiating entity knows or expects the receiving entity to service.
     * @param lang                 An entity's preferred or default language for any human-readable XML character data to be sent over the stream.
     * @param additionalNamespaces Any optional additional namespace declarations.
     * @return The stream header.
     */
    public static StreamHeader initialServerToServer(Jid from, Jid to, Locale lang, QName... additionalNamespaces) {
        // For initial stream headers in both client-to-server and server-to-server communication, the initiating entity MUST include the 'to' attribute and MUST set its value to a domainpart that the initiating entity knows or expects the receiving entity to service.
        // For initial stream headers, the initiating entity MUST NOT include the 'id' attribute;
        return new StreamHeader(from, Objects.requireNonNull(to).asBareJid().withLocal(null), null, lang, "jabber:server", additionalNamespaces);
    }

    /**
     * Creates a response stream header for server-to-server streams.
     *
     * @param from                 One of the receiving entity's FQDNs.
     * @param to                   The domainpart specified in the 'from' attribute of the initial stream header.
     * @param id                   A unique identifier for the stream, called a "stream ID".
     * @param lang                 An entity's preferred or default language for any human-readable XML character data to be sent over the stream.
     * @param additionalNamespaces Any optional additional namespace declarations.
     * @return The stream header.
     */
    public static StreamHeader responseServerToServer(Jid from, Jid to, String id, Locale lang, QName... additionalNamespaces) {
        // For response stream headers in both client-to-server and server-to-server communication, the receiving entity MUST include the 'from' attribute and MUST set its value to one of the receiving entity's FQDNs
        // For response stream headers in server-to-server communication, the receiving entity MUST include a 'to' attribute in the response stream header and MUST set its value to the domainpart specified in the 'from' attribute of the initial stream header.
        // For response stream headers, the receiving entity MUST include the 'id' attribute.
        // For response stream headers, the receiving entity MUST include the 'xml:lang' attribute.
        return new StreamHeader(Objects.requireNonNull(from).asBareJid().withLocal(null), Objects.requireNonNull(to).asBareJid().withLocal(null), Objects.requireNonNull(id), Objects.requireNonNull(lang), "jabber:server", additionalNamespaces);
    }

    /**
     * Writes the stream header to a {@link XMLStreamWriter}.
     *
     * @param writer The writer.
     * @throws XMLStreamException If writing to the writer fails.
     */
    public final void writeTo(final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        writer.writeStartElement("stream", "stream", StreamFeatures.NAMESPACE);

        if (from != null) {
            writer.writeAttribute("from", from.toString());
        }
        if (to != null) {
            writer.writeAttribute("to", to.toString());
        }
        if (id != null) {
            writer.writeAttribute("id", id);
        }
        writer.writeAttribute("version", "1.0");
        if (lang != null) {
            writer.writeAttribute(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI, "lang", lang.toLanguageTag());
        }
        writer.writeNamespace(XMLConstants.DEFAULT_NS_PREFIX, contentNamespace);
        writer.writeNamespace("stream", StreamFeatures.NAMESPACE);

        for (QName qName : additionalNamespaces) {
            writer.writeNamespace(qName.getPrefix(), qName.getNamespaceURI());
        }
        // Close the stream header tag
        writer.writeCharacters("");
        writer.flush();
    }

    /**
     * Gets the 'from' attribute.
     *
     * @return The 'from' attribute.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#streams-attr-from">4.7.1.  from</a>
     */
    @Override
    public final Jid getFrom() {
        return from;
    }

    /**
     * Gets the 'to' attribute.
     *
     * @return The 'to' attribute.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#streams-attr-to">4.7.2.  to</a>
     */
    @Override
    public final Jid getTo() {
        return to;
    }

    /**
     * Gets the 'id' attribute.
     *
     * @return The 'id' attribute.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#streams-attr-id">4.7.3.  id</a>
     */
    @Override
    public final String getId() {
        return id;
    }

    /**
     * Gets the 'xml:lang' attribute.
     *
     * @return The 'xml:lang' attribute.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#streams-attr-xmllang">4.7.4.  xml:lang</a>
     */
    @Override
    public final Locale getLanguage() {
        return lang;
    }

    /**
     * Gets additional namespaces other than the content namespace and the stream namespace.
     *
     * @return The namespaces.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#streams-ns-other">4.8.4.  Other Namespaces</a>
     */
    public final List<QName> getAdditionalNamespaces() {
        return Collections.unmodifiableList(additionalNamespaces);
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder("<stream:stream");
        if (from != null) {
            sb.append(" from=\"").append(from).append('"');
        }
        if (to != null) {
            sb.append(" to=\"").append(to).append('"');
        }
        if (id != null) {
            sb.append(" to=\"").append(id).append('"');
        }
        sb.append(" version=\"1.0\"");
        if (id != null) {
            sb.append(" to=\"").append(id).append('"');
        }
        if (lang != null) {
            sb.append(" xml:lang=\"").append(lang.toLanguageTag()).append('"');
        }
        sb.append(" xmlns=\"")
                .append(contentNamespace)
                .append("\" xmlns:stream=\"").append(StreamFeatures.NAMESPACE).append('"');

        for (QName qName : additionalNamespaces) {
            sb.append(" xmlns:").append(qName.getPrefix()).append("=\"").append(qName.getNamespaceURI()).append('"');
        }

        sb.append('>');
        return sb.toString();
    }
}
