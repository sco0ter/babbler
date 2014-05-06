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

package org.xmpp;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
final class XmppStreamReader {

    private static final Logger logger = Logger.getLogger(XmppStreamReader.class.getName());

    private final TcpConnection connection;

    private final ExecutorService executorService;

    public XmppStreamReader(final TcpConnection connection) throws JAXBException {
        this.connection = connection;

        executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "XMPP Reader Thread");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    void startReading(final InputStream inputStream) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean doRestart = false;
                try {

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    InputStream xmppInputStream = new BranchedInputStream(inputStream, byteArrayOutputStream);
                    XMLEventReader xmlEventReader = connection.createXMLEventReader(xmppInputStream);

                    while (!doRestart && xmlEventReader.hasNext()) {
                        XMLEvent xmlEvent = xmlEventReader.peek();

                        if (xmlEvent.isStartElement()) {
                            StartElement startElement = xmlEvent.asStartElement();
                            if (startElement.getName().getLocalPart().equals("stream") && startElement.getName().getNamespaceURI().equals("http://etherx.jabber.org/streams")) {
                                Attribute idAttribute = startElement.getAttributeByName(new QName("id"));
                                if (idAttribute != null) {
                                    connection.streamId = idAttribute.getValue();
                                }
                                Attribute fromAttribute = startElement.getAttributeByName(new QName("from"));
                                if (fromAttribute != null) {
                                    connection.xmppServiceDomain = fromAttribute.getValue();
                                }
                                xmlEventReader.next();
                            } else {
                                Object object = connection.unmarshaller.unmarshal(xmlEventReader);
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine("<--  " + new String(byteArrayOutputStream.toByteArray()));
                                }
                                byteArrayOutputStream.reset();
                                doRestart = connection.handleElement(object);
                            }
                        } else {
                            xmlEventReader.next();
                        }

                    }
                    xmlEventReader.close();
                } catch (Exception e) {
                    connection.notifyException(e);
                } finally {
                    if (doRestart) {
                        connection.restartStream();
                    } else {
                        synchronized (XmppStreamReader.this) {
                            if (!executorService.isShutdown()) {
                                // shutdown the service, but don't await termination, in order to not block the reader thread.
                                executorService.shutdown();
                                // Then close the connection, which will only close the writer thread and the socket.
                                try {
                                    connection.close();
                                } catch (IOException e) {
                                    logger.log(Level.WARNING, e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
            }
        });
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
