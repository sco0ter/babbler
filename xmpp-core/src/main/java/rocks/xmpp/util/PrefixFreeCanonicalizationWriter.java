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

package rocks.xmpp.util;

import rocks.xmpp.core.stream.model.StreamFeatures;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.soap.SOAPConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;

/**
 * Writes XML in a prefix-free canonicalization form.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-extended">8.4.  Extended Content</a></cite></p>
 * <p>It is conventional in the XMPP community for implementations to not generate namespace prefixes for elements that are qualified by extended namespaces (in the XML community, this convention is sometimes called "prefix-free canonicalization"). However, if an implementation generates such namespace prefixes then it MUST include the namespace declaration in the stanza itself or a child element of the stanza, not in the stream header (see Section 4.8.4).</p>
 * </blockquote>
 * See also <a href="http://stackoverflow.com/questions/5720501/jaxb-marshalling-xmpp-stanzas">here</a> for implementation idea.
 *
 * @author Christian Schudt
 */
final class PrefixFreeCanonicalizationWriter implements XMLStreamWriter {

    private static final Collection<String> PREFIXED_NAMESPACES = Arrays.asList(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);

    /**
     * This is the default content namespace.
     * See also <a href="http://xmpp.org/rfcs/rfc6120.html#streams-ns-content">http://xmpp.org/rfcs/rfc6120.html#streams-ns-content</a>
     */
    private final String contentNamespace;

    private final Collection<String> currentNamespaceUris = new HashSet<>();

    private final Deque<String> namespaces = new ArrayDeque<>();

    private final XMLStreamWriter xsw;

    private final boolean writeStreamNamespace;

    private String defaultNS;

    PrefixFreeCanonicalizationWriter(final XMLStreamWriter xsw, final String contentNamespace, boolean writeStreamNamespace) throws XMLStreamException {
        this.xsw = xsw;
        this.defaultNS = this.contentNamespace = contentNamespace;
        this.writeStreamNamespace = writeStreamNamespace;
    }

    @Override
    public final void writeStartElement(final String localName) throws XMLStreamException {
        pushNamespaceUri(XMLConstants.NULL_NS_URI);
        xsw.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        pushNamespaceUri(namespaceURI);
        xsw.writeStartElement(namespaceURI, localName);
    }

    @Override
    public final void writeStartElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
        pushNamespaceUri(namespaceURI);
        if (shouldWriteNamespacePrefix(namespaceURI)) {
            xsw.writeStartElement(prefix, localName, namespaceURI);
        } else {
            // If the writer wants to write a prefix, instead don't write it.
            xsw.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, localName, namespaceURI);
            writeDefaultNamespaceIfNecessary(namespaceURI);
        }
    }

    @Override
    public final void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException {
        pushNamespaceUri(namespaceURI);
        currentNamespaceUris.clear();
        xsw.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public final void writeEmptyElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
        pushNamespaceUri(namespaceURI);
        if (shouldWriteNamespacePrefix(namespaceURI)) {
            xsw.writeEmptyElement(prefix, localName, namespaceURI);
        } else {
            // If the writer wants to write a prefix, instead don't write it.
            xsw.writeEmptyElement(namespaceURI, localName);
            writeDefaultNamespaceIfNecessary(namespaceURI);
        }
    }

    private void writeDefaultNamespaceIfNecessary(final String namespaceURI) throws XMLStreamException {
        if (namespaceURI != null && namespaceURI.length() > 0) {
            // If the namespace is not the current namespace, write it.
            if (!namespaceURI.equals(defaultNS)) {
                writeDefaultNamespace(namespaceURI);
                defaultNS = namespaceURI;
            }
        }
    }

    @Override
    public final void writeEmptyElement(final String localName) throws XMLStreamException {
        xsw.writeEmptyElement(localName);
    }

    @Override
    public final void writeEndElement() throws XMLStreamException {
        xsw.writeEndElement();
        popNamespaceUri();
    }

    @Override
    public final void writeEndDocument() throws XMLStreamException {
        xsw.writeEndDocument();
    }

    @Override
    public final void close() throws XMLStreamException {
        xsw.close();
    }

    @Override
    public final void flush() throws XMLStreamException {
        xsw.flush();
    }

    @Override
    public final void writeAttribute(final String localName, final String value) throws XMLStreamException {
        xsw.writeAttribute(localName, value);
    }

    @Override
    public final void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value) throws XMLStreamException {
        // If an attribute has an extra namespace, we need to write that namespace to the element.
        // Do it only once for each element.
        if (currentNamespaceUris.add(namespaceURI)) {
            xsw.writeNamespace(prefix, namespaceURI);
        }
        xsw.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public final void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException {
        xsw.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public final void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException {
        // do not write a namespace with a prefix, except it's allowed.
        if (shouldWriteNamespace(namespaceURI)) {
            xsw.writeNamespace(prefix, namespaceURI);
        }
    }

    @Override
    public final void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException {
        xsw.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public final void writeComment(final String data) throws XMLStreamException {
        xsw.writeComment(data);
    }

    @Override
    public final void writeProcessingInstruction(final String target) throws XMLStreamException {
        xsw.writeProcessingInstruction(target);
    }

    @Override
    public final void writeProcessingInstruction(final String target, final String data) throws XMLStreamException {
        xsw.writeProcessingInstruction(target, data);
    }

    @Override
    public final void writeCData(final String data) throws XMLStreamException {
        xsw.writeCData(data);
    }

    @Override
    public final void writeDTD(final String dtd) throws XMLStreamException {
        xsw.writeDTD(dtd);
    }

    @Override
    public final void writeEntityRef(final String name) throws XMLStreamException {
        xsw.writeEntityRef(name);
    }

    @Override
    public final void writeStartDocument() throws XMLStreamException {
        xsw.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        xsw.writeStartDocument(version);
    }

    @Override
    public final void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
        xsw.writeStartDocument(encoding, version);
    }

    @Override
    public final void writeCharacters(final String text) throws XMLStreamException {
        xsw.writeCharacters(text);
    }

    @Override
    public final void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException {
        xsw.writeCharacters(text, start, len);
    }

    @Override
    public final String getPrefix(final String uri) throws XMLStreamException {
        return xsw.getPrefix(uri);
    }

    @Override
    public final void setPrefix(final String prefix, final String uri) throws XMLStreamException {
        xsw.setPrefix(prefix, uri);
    }

    @Override
    public final void setDefaultNamespace(final String uri) throws XMLStreamException {
        xsw.setDefaultNamespace(uri);
    }

    @Override
    public final NamespaceContext getNamespaceContext() {
        return xsw.getNamespaceContext();
    }

    @Override
    public final void setNamespaceContext(final NamespaceContext context) throws XMLStreamException {
        xsw.setNamespaceContext(context);
    }

    @Override
    public final Object getProperty(final String name) throws IllegalArgumentException {
        return xsw.getProperty(name);
    }

    private void pushNamespaceUri(final String namespaceUri) {
        namespaces.addFirst(namespaceUri);
        currentNamespaceUris.clear();
    }

    private void popNamespaceUri() {
        namespaces.removeFirst();
        if (!namespaces.isEmpty()) {
            defaultNS = namespaces.peekFirst();
        } else {
            defaultNS = contentNamespace;
        }
    }

    private boolean shouldWriteNamespace(String namespaceURI) {
        return !Collections.disjoint(namespaces, PREFIXED_NAMESPACES) || StreamFeatures.NAMESPACE.equals(namespaceURI) && writeStreamNamespace;
    }

    private boolean shouldWriteNamespacePrefix(String namespaceURI) {
        return shouldWriteNamespace(namespaceURI) || StreamFeatures.NAMESPACE.equals(namespaceURI);
    }
}