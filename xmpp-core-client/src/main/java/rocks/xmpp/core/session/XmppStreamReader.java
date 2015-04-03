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

package rocks.xmpp.core.session;

import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stream.StreamErrorException;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.core.stream.model.errors.Text;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for reading the inbound XMPP stream. It starts one "reader thread", which keeps reading the XMPP document from the stream until the stream is closed or disconnected.
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
final class XmppStreamReader {

    private final TcpConnection connection;

    private final XmppSession xmppSession;

    private final ExecutorService executorService;

    private final XMLInputFactory xmlInputFactory;

    private final XMLOutputFactory xmlOutputFactory;

    private final XmppDebugger debugger;

    private final Marshaller marshaller;

    private final Unmarshaller unmarshaller;

    public XmppStreamReader(final TcpConnection connection, XmppSession xmppSession, XMLOutputFactory xmlOutputFactory) {
        this.connection = connection;
        this.xmppSession = xmppSession;
        this.debugger = xmppSession.getDebugger();
        this.marshaller = xmppSession.createMarshaller();
        this.unmarshaller = xmppSession.createUnmarshaller();
        executorService = Executors.newSingleThreadExecutor(XmppUtils.createNamedThreadFactory("XMPP Reader Thread"));
        this.xmlInputFactory = XMLInputFactory.newFactory();
        this.xmlOutputFactory = xmlOutputFactory;
    }

    synchronized void startReading(final InputStream inputStream) {

        if (!executorService.isShutdown()) {
            executorService.execute(() -> {
                boolean doRestart = false;
                XMLEventReader xmlEventReader = null;
                try {

                    InputStream xmppInputStream;
                    ByteArrayOutputStream byteArrayOutputStream = null;
                    if (debugger != null) {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        xmppInputStream = debugger.createInputStream(XmppUtils.createBranchedInputStream(inputStream, byteArrayOutputStream));
                    } else {
                        xmppInputStream = inputStream;
                    }

                    xmlEventReader = xmlInputFactory.createXMLEventReader(xmppInputStream, "UTF-8");
                    boolean isFirstPass = true;
                    while (!doRestart && xmlEventReader.hasNext()) {
                        XMLEvent xmlEvent = xmlEventReader.peek();

                        if (xmlEvent.isStartElement()) {
                            StartElement startElement = xmlEvent.asStartElement();
                            if (startElement.getName().getLocalPart().equals("stream") && startElement.getName().getNamespaceURI().equals("http://etherx.jabber.org/streams")) {
                                Attribute idAttribute = startElement.getAttributeByName(new QName("id"));
                                if (idAttribute != null) {
                                    synchronized (connection) {
                                        connection.streamId = idAttribute.getValue();
                                    }
                                }
                                Attribute fromAttribute = startElement.getAttributeByName(new QName("from"));
                                if (fromAttribute != null) {
                                    xmppSession.setXmppServiceDomain(fromAttribute.getValue());
                                }

                                xmlEventReader.next();
                            } else {
                                Object object = unmarshaller.unmarshal(xmlEventReader);

                                if (debugger != null) {
                                    if (isFirstPass && byteArrayOutputStream != null) {
                                        // If it's the first pass, include the stream header with the <features/>, which are both in the byteArrayOutputStream at this point.
                                        debugger.readStanza(byteArrayOutputStream.toString(), object);
                                    } else {
                                        // Otherwise marshal the inbound stanza. The byteArrayOutputStream cannot be used for that, even if we reset() it, because it could already contain the next stanza.
                                        StringWriter stringWriter = new StringWriter();
                                        XMLStreamWriter xmlStreamWriter = XmppUtils.createXmppStreamWriter(xmlOutputFactory.createXMLStreamWriter(stringWriter), true);
                                        marshaller.marshal(object, xmlStreamWriter);
                                        xmlStreamWriter.flush();
                                        debugger.readStanza(stringWriter.toString(), object);
                                    }
                                }
                                isFirstPass = false;
                                doRestart = xmppSession.handleElement(object);
                            }
                        } else {
                            xmlEventReader.next();
                        }
                        if (!isFirstPass && byteArrayOutputStream != null) {
                            // Avoid that the stream grows to the size of the whole XMPP stream.
                            byteArrayOutputStream.reset();
                        }
                        if (xmlEvent.getEventType() == XMLEvent.END_ELEMENT) {
                            // The stream gets closed with </stream:stream>
                            if (debugger != null) {
                                QName qName = xmlEvent.asEndElement().getName();
                                debugger.readStanza("</" + qName.getPrefix() + ":" + qName.getLocalPart() + ">", null);
                            }
                        }
                    }

                    xmlEventReader.close();
                    if (!doRestart && xmppSession.getStatus() != XmppSession.Status.CLOSING) {
                        // The server initiated a graceful disconnect by sending <stream:stream/> without an stream error.
                        // In this case we want to reconnect, therefore throw an exception as if a stream error has occurred.
                        throw new StreamErrorException(new StreamError(Condition.UNDEFINED_CONDITION, new Text("Stream closed by server", "en"), null));
                    }
                } catch (Exception e) {
                    synchronized (XmppStreamReader.this) {
                        if (!executorService.isShutdown()) {
                            // shutdown the service, but don't await termination, in order to not block the reader thread.
                            executorService.shutdown();
                        }
                    }
                    xmppSession.notifyException(e);
                } finally {
                    if (xmlEventReader != null) {
                        try {
                            xmlEventReader.close();
                        } catch (XMLStreamException e) {
                            xmppSession.notifyException(e);
                        }
                    }
                    if (doRestart) {
                        connection.restartStream();
                    }
                }
            });
        }
    }

    /**
     * Shuts down the executor and waits maximal 0.5 seconds for the reader thread to finish, i.e. when the server sends a {@code </stream:stream>} response.
     */
    synchronized void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
