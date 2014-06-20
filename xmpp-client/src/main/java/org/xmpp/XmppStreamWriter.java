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

import org.xmpp.stream.ClientStreamElement;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
final class XmppStreamWriter {

    private static final Logger logger = Logger.getLogger(XmppStreamWriter.class.getName());

    private final XmppSession xmppSession;

    private final ExecutorService executor;

    private final XMLOutputFactory xmlOutputFactory;

    private final Marshaller marshaller;

    private final ScheduledExecutorService keepAliveExecutor;

    private volatile XMLStreamWriter prefixFreeCanonicalizationWriter;

    private XMLStreamWriter xmlStreamWriter;

    private OutputStream lastOutputStream;

    private ByteArrayOutputStream byteArrayOutputStream;

    public XmppStreamWriter(final OutputStream outputStream, final XmppSession xmppSession) throws XMLStreamException, IOException {
        this.xmppSession = xmppSession;
        this.xmlOutputFactory = XMLOutputFactory.newFactory();
        this.marshaller = xmppSession.getMarshaller();

        reset(outputStream);

        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "XMPP Writer Thread");
                thread.setDaemon(true);
                return thread;
            }
        });

        keepAliveExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "XMPP KeepAlive Thread");
                thread.setDaemon(true);
                return thread;

            }
        });
        keepAliveExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (xmppSession.getStatus() == XmppSession.Status.CONNECTED || xmppSession.getStatus() == XmppSession.Status.AUTHENTICATED) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (xmlOutputFactory) {
                                try {
                                    xmlStreamWriter.writeCharacters(" ");
                                    xmlStreamWriter.flush();
                                } catch (XMLStreamException e) {
                                    xmppSession.notifyException(e);
                                }
                            }
                        }
                    });
                }
            }
        }, 0, 20, TimeUnit.SECONDS);
    }

    void reset(OutputStream outputStream) throws XMLStreamException, IOException {
        synchronized (xmlOutputFactory) {
            // Only recreate the writer, if the stream has changed (e.g. due to TLS or compression)
            if (lastOutputStream != outputStream) {
                lastOutputStream = outputStream;
                if (prefixFreeCanonicalizationWriter != null) {
                    // This also closes the xmlStreamWriter
                    prefixFreeCanonicalizationWriter.close();
                }

                if (xmlStreamWriter != null) {
                    xmlStreamWriter.close();
                }
                if (prefixFreeCanonicalizationWriter != null) {
                    prefixFreeCanonicalizationWriter.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                byteArrayOutputStream = new ByteArrayOutputStream();
                BranchedOutputStream branchedOutputStream = new BranchedOutputStream(outputStream, byteArrayOutputStream);
                xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(branchedOutputStream);

                prefixFreeCanonicalizationWriter = XmppUtils.createXmppStreamWriter(xmlOutputFactory.createXMLStreamWriter(branchedOutputStream), true);
            }
        }
    }

    void send(final ClientStreamElement clientStreamElement) {
        if (!executor.isShutdown() && clientStreamElement != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (xmlOutputFactory) {
                        try {
                            marshaller.marshal(clientStreamElement, prefixFreeCanonicalizationWriter);
                            prefixFreeCanonicalizationWriter.flush();
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("-->  " + new String(byteArrayOutputStream.toByteArray()));
                            }
                            byteArrayOutputStream.reset();
                        } catch (XMLStreamException | JAXBException e) {
                            xmppSession.notifyException(e);
                        }
                    }
                }
            });
        }
    }

    void openStream(final Jid from) {
        if (!executor.isShutdown()) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    synchronized (xmlOutputFactory) {
                        try {
                            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
                            xmlStreamWriter.writeStartElement("stream", "stream", "http://etherx.jabber.org/streams");
                            xmlStreamWriter.writeAttribute(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI, "lang", Locale.getDefault().getLanguage());
                            if (xmppSession.getXmppServiceDomain() != null) {
                                xmlStreamWriter.writeAttribute("to", xmppSession.getXmppServiceDomain());
                            }
                            if (from != null) {
                                xmlStreamWriter.writeAttribute("from", from.toString());
                            }
                            xmlStreamWriter.writeAttribute("version", "1.0");
                            xmlStreamWriter.writeNamespace("", "jabber:client");
                            xmlStreamWriter.writeNamespace("stream", "http://etherx.jabber.org/streams");
                            xmlStreamWriter.writeCharacters("");
                            xmlStreamWriter.flush();
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("-->  " + new String(byteArrayOutputStream.toByteArray()));
                            }
                            byteArrayOutputStream.reset();
                        } catch (XMLStreamException e) {
                            xmppSession.notifyException(e);
                        }
                    }
                }
            });
        }
    }

    private void closeStream() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (xmlOutputFactory) {
                    // Close the stream.
                    try {
                        xmlStreamWriter.writeEndElement();
                        xmlStreamWriter.flush();
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("-->  " + new String(byteArrayOutputStream.toByteArray()));
                        }
                        byteArrayOutputStream.reset();
                        xmlStreamWriter.close();
                    } catch (XMLStreamException e) {
                        xmppSession.notifyException(e);
                    }
                }
            }
        });
    }

    /**
     * Closes the stream by sending a closing {@code </stream:stream>} to the server.
     * This method waits until this task is completed, but not more than 0.5 seconds.
     * <p/>
     * Make sure to synchronize this method.
     * Otherwise multiple threads could call {@link #closeStream()} which may result in a {@link RejectedExecutionException}, if it has been shutdown by another thread in the meantime.
     */
    synchronized void shutdown() {
        // If the writer is still active, close the stream and afterwards shutdown the writer.
        if (!executor.isShutdown()) {
            // Send the closing stream tag.
            closeStream();
            // Shutdown the executor
            keepAliveExecutor.shutdown();
            executor.shutdown();

            // Wait for termination after shutdown.
            try {
                keepAliveExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
                executor.awaitTermination(250, TimeUnit.MILLISECONDS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
