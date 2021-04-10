/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.xml.XMLConstants;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.net.ReaderInterceptor;
import rocks.xmpp.core.net.ReaderInterceptorChain;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.core.stream.model.errors.Condition;

/**
 *
 */
public class XmppStreamDecoder implements ReaderInterceptor {

    private static final QName STREAM_ID = new QName("id");

    private static final QName FROM = new QName("from");

    private static final QName TO = new QName("to");

    private static final QName VERSION = new QName("version");

    private static final QName LANG = new QName(XMLConstants.XML_NS_URI, "lang");

    private final XMLInputFactory inputFactory;

    private final Supplier<Unmarshaller> unmarshaller;

    private final String namespace;

    private boolean doRestart;

    /**
     * Creates the XMPP encoder.
     *
     * <p>Because {@link Marshaller} is not thread-safe, it is recommended to pass a {@code ThreadLocal<Marshaller>} to
     * this constructor, which ensures thread-safety during marshalling.</p>
     *
     * @param inputFactory The XML input factory.
     * @param unmarshaller Supplies the marshaller which will convert objects to XML.
     * @param namespace    If the stream namespace should be written in the root element.
     */
    public XmppStreamDecoder(final XMLInputFactory inputFactory, final Supplier<Unmarshaller> unmarshaller,
                             final String namespace) {
        this.unmarshaller = unmarshaller;
        this.inputFactory = inputFactory;
        this.namespace = namespace;
    }

    /**
     * Encodes an XMPP element to a {@link Writer}.
     *
     * @param streamElementConsumer The stream element.
     * @param reader                The writer to write to.
     * @throws StreamErrorException If the element could not be marshalled.
     */
    public final void decode(final Reader reader, Consumer<StreamElement> streamElementConsumer)
            throws StreamErrorException {
        XMLEventReader xmlEventReader = null;
        try {
            try {
                doRestart = false;
                xmlEventReader = inputFactory.createXMLEventReader(reader);
                XMLEvent xmlEvent;
                StreamHeader streamHeader = null;
                while (!doRestart && (xmlEvent = xmlEventReader.peek()) != null) {
                    switch (xmlEvent.getEventType()) {
                        case XMLStreamConstants.START_ELEMENT:

                            StartElement startElement = xmlEvent.asStartElement();
                            if (StreamHeader.LOCAL_NAME.equals(startElement.getName().getLocalPart())
                                    && StreamHeader.STREAM_NAMESPACE.equals(startElement.getName().getNamespaceURI())) {
                                Attribute idAttribute = startElement.getAttributeByName(STREAM_ID);
                                final Attribute fromAttribute = startElement.getAttributeByName(FROM);
                                final Attribute toAttribute = startElement.getAttributeByName(TO);
                                final Attribute versionAttribute = startElement.getAttributeByName(VERSION);
                                final Attribute langAttribute = startElement.getAttributeByName(LANG);
                                final Jid from = fromAttribute != null ? Jid.ofEscaped(fromAttribute.getValue()) : null;
                                final Jid to = toAttribute != null ? Jid.ofEscaped(toAttribute.getValue()) : null;
                                final String id = idAttribute != null ? idAttribute.getValue() : null;
                                final String version = versionAttribute != null ? versionAttribute.getValue() : null;
                                final Locale lang =
                                        langAttribute != null ? Locale.forLanguageTag(langAttribute.getValue()) : null;
                                streamHeader = StreamHeader.create(from, to, id, version, lang, namespace);
                                streamElementConsumer.accept(streamHeader);
                                xmlEventReader.nextEvent();
                            } else {
                                if (streamHeader != null) {
                                    unmarshaller.get()
                                            .setListener(new LanguageUnmarshallerListener(streamHeader.getLanguage()));
                                }
                                StreamElement object = (StreamElement) unmarshaller.get().unmarshal(xmlEventReader);
                                streamElementConsumer.accept(object);
                            }
                            break;
                        case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        case XMLStreamConstants.COMMENT:
                        case XMLStreamConstants.ENTITY_REFERENCE:
                        case XMLStreamConstants.DTD:
                        case XMLStreamConstants.NOTATION_DECLARATION:
                        case XMLStreamConstants.ENTITY_DECLARATION:
                            throw new StreamErrorException(new StreamError(Condition.RESTRICTED_XML));
                        default:
                            xmlEventReader.nextEvent();
                            break;
                    }
                    if (xmlEvent.isEndElement()) {
                        streamElementConsumer.accept(StreamHeader.CLOSING_STREAM_TAG);
                    }
                }
            } finally {
                if (xmlEventReader != null) {
                    xmlEventReader.close();
                }
            }
        } catch (StreamErrorException e) {
            throw e;
        } catch (XMLStreamException e) {
            throw new StreamErrorException(new StreamError(Condition.NOT_WELL_FORMED), e);
        } catch (Exception e) {
            throw new StreamErrorException(new StreamError(Condition.INTERNAL_SERVER_ERROR), e);
        }
    }

    public synchronized void restart() {
        doRestart = true;
    }

    @Override
    public void process(Reader reader, Consumer<StreamElement> streamElement, ReaderInterceptorChain chain)
            throws Exception {
        decode(reader, streamElement);
        chain.proceed(reader, streamElement);
    }
}