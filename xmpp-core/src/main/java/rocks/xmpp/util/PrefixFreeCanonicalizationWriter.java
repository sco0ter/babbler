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

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.soap.SOAPConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

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

    private final XMLStreamWriter xsw;

    private final ContentNamespaceContext nc;

    PrefixFreeCanonicalizationWriter(XMLStreamWriter xsw, String contentNamespace) throws XMLStreamException {
        this.xsw = xsw;
        nc = new ContentNamespaceContext(contentNamespace);
        xsw.setNamespaceContext(nc);
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        nc.pushNamespaceUri(XMLConstants.NULL_NS_URI);
        xsw.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        nc.pushNamespaceUri(namespaceURI);
        xsw.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        nc.pushNamespaceUri(namespaceURI);
        if (nc.shouldWriteNamespacePrefix()) {
            xsw.writeStartElement(prefix, localName, namespaceURI);
        } else {
            // If the writer wants to write a prefix, instead don't write it.
            xsw.writeStartElement(namespaceURI, localName);
            writeDefaultNamespaceIfNecessary(namespaceURI);
        }
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        nc.pushNamespaceUri(namespaceURI);
        nc.currentNamespaceUris.clear();
        xsw.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        nc.pushNamespaceUri(namespaceURI);
        if (nc.shouldWriteNamespacePrefix()) {
            xsw.writeEmptyElement(prefix, localName, namespaceURI);
        } else {
            // If the writer wants to write a prefix, instead don't write it.
            xsw.writeEmptyElement(namespaceURI, localName);
            writeDefaultNamespaceIfNecessary(namespaceURI);
        }
    }

    private void writeDefaultNamespaceIfNecessary(String namespaceURI) throws XMLStreamException {
        if (namespaceURI != null && namespaceURI.length() > 0) {
            String currentDefaultNS = nc.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
            // If the namespace is not the current namespace, write it.
            if (!namespaceURI.equals(currentDefaultNS)) {
                writeDefaultNamespace(namespaceURI);
                nc.setDefaultNS(namespaceURI);
            }
        }
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        xsw.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        xsw.writeEndElement();
        nc.popNamespaceUri();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        xsw.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        xsw.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        xsw.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        xsw.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        // If an attribute has an extra namespace, we need to write that namespace to the element.
        // Do it only once for each element.
        if (nc.currentNamespaceUris.add(namespaceURI)) {
            xsw.writeNamespace(prefix, namespaceURI);
        }
        xsw.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        xsw.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        // do not write a namespace with a prefix, except it's allowed.
        if (nc.shouldWriteNamespacePrefix()) {
            xsw.writeNamespace(prefix, namespaceURI);
        }
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        xsw.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        xsw.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        xsw.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        xsw.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        xsw.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        xsw.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        xsw.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        xsw.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        xsw.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        xsw.writeStartDocument(encoding, version);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        xsw.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        xsw.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return xsw.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        xsw.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        xsw.setDefaultNamespace(uri);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return xsw.getNamespaceContext();
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        xsw.setNamespaceContext(context);
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return xsw.getProperty(name);
    }

    /**
     * Manages the current namespace, which is used for an element.
     */
    private static class ContentNamespaceContext implements NamespaceContext {

        private static final Collection<String> PREFIXED_NAMESPACES = new ArrayList<>(Arrays.asList(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE));

        /**
         * This is the default content namespace.
         * See also <a href="http://xmpp.org/rfcs/rfc6120.html#streams-ns-content">http://xmpp.org/rfcs/rfc6120.html#streams-ns-content</a>
         */
        private final String contentNamespace;

        private final Set<String> currentNamespaceUris = new HashSet<>();

        private final Stack<String> namespaces = new Stack<>();

        private String defaultNS;

        private ContentNamespaceContext(String contentNamespace) {
            this.defaultNS = contentNamespace;
            this.contentNamespace = contentNamespace;
        }

        private void setDefaultNS(String ns) {
            defaultNS = ns;
        }

        private void pushNamespaceUri(String namespaceUri) {
            namespaces.push(namespaceUri);
            currentNamespaceUris.clear();
        }

        private void popNamespaceUri() {
            namespaces.pop();
            if (!namespaces.empty()) {
                defaultNS = namespaces.peek();
            } else {
                defaultNS = contentNamespace;
            }
        }

        private boolean shouldWriteNamespacePrefix() {
            return !Collections.disjoint(namespaces, PREFIXED_NAMESPACES);
        }

        @Override
        public String getNamespaceURI(String prefix) {
            Objects.requireNonNull(prefix, "prefix must not be null.");
            if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
                return defaultNS;
            }
            return XMLConstants.NULL_NS_URI;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
