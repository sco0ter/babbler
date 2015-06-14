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

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;

import javax.xml.XMLConstants;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for opening and closing the XMPP stream as well as writing any XML elements to the stream.
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
final class XmppStreamWriter {

    private final XmppSession xmppSession;

    private final ExecutorService executor;

    private final XMLOutputFactory xmlOutputFactory;

    private final Marshaller marshaller;

    private final XmppDebugger debugger;

    /**
     * An executor which periodically schedules a whitespace ping.
     * Guarded by "this".
     */
    private ScheduledExecutorService keepAliveExecutor;

    /**
     * Will be accessed only by the writer thread.
     */
    private XMLStreamWriter prefixFreeCanonicalizationWriter;

    /**
     * Will be accessed only by the writer thread.
     */
    private XMLStreamWriter xmlStreamWriter;

    /**
     * Will be accessed only by the writer thread.
     */
    private ByteArrayOutputStream byteArrayOutputStream;

    /**
     * Indicates whether the stream has been opened. Will be accessed only by the writer thread.
     */
    private boolean streamOpened;

    private final String namespace;

    XmppStreamWriter(String namespace, final XmppSession xmppSession, XMLOutputFactory xmlOutputFactory) {
        this.namespace = namespace;
        this.xmppSession = xmppSession;
        this.xmlOutputFactory = xmlOutputFactory;
        this.marshaller = xmppSession.createMarshaller();
        this.debugger = xmppSession.getDebugger();

        executor = Executors.newSingleThreadExecutor(XmppUtils.createNamedThreadFactory("XMPP Writer Thread"));
    }

    void initialize(int keepAliveInterval) {
        if (keepAliveInterval > 0) {
            synchronized (this) {
                keepAliveExecutor = Executors.newSingleThreadScheduledExecutor(XmppUtils.createNamedThreadFactory("XMPP KeepAlive Thread"));
                keepAliveExecutor.scheduleAtFixedRate(() -> {
                    if (EnumSet.of(XmppSession.Status.CONNECTED, XmppSession.Status.AUTHENTICATED).contains(xmppSession.getStatus())) {
                        executor.execute(() -> {
                            try {
                                xmlStreamWriter.writeCharacters(" ");
                                xmlStreamWriter.flush();
                            } catch (Exception e) {
                                notifyException(e);
                            }
                        });
                    }
                }, 0, keepAliveInterval, TimeUnit.SECONDS);
            }
        }
    }

    synchronized void send(final StreamElement clientStreamElement) {
        if (!executor.isShutdown() && clientStreamElement != null) {
            executor.execute(() -> {
                try {
                    marshaller.marshal(clientStreamElement, prefixFreeCanonicalizationWriter);
                    prefixFreeCanonicalizationWriter.flush();

                    // Workaround: Simulate keep-alive packet to convince client to process the already transmitted packet.
                    if (clientStreamElement instanceof Stanza) {
                        prefixFreeCanonicalizationWriter.writeCharacters(" ");
                        prefixFreeCanonicalizationWriter.flush();
                    }

                    if (debugger != null) {
                        debugger.writeStanza(new String(byteArrayOutputStream.toByteArray()).trim(), clientStreamElement);
                        byteArrayOutputStream.reset();
                    }
                } catch (Exception e) {
                    notifyException(e);
                }
            });
        }
    }

    synchronized void openStream(final OutputStream outputStream, final Jid from) {
        if (!executor.isShutdown()) {
            executor.execute(() -> {
                try {

                    OutputStream xmppOutputStream;
                    if (debugger != null) {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        xmppOutputStream = debugger.createOutputStream(XmppUtils.createBranchedOutputStream(outputStream, byteArrayOutputStream));
                    } else {
                        xmppOutputStream = outputStream;
                    }
                    xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(xmppOutputStream, "UTF-8");

                    prefixFreeCanonicalizationWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, namespace);
                    streamOpened = false;

                    xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
                    xmlStreamWriter.writeStartElement("stream", "stream", "http://etherx.jabber.org/streams");
                    xmlStreamWriter.writeAttribute(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI, "lang", Locale.getDefault().getLanguage());
                    if (xmppSession.getDomain() != null && !xmppSession.getDomain().isEmpty()) {
                        xmlStreamWriter.writeAttribute("to", xmppSession.getDomain());
                    }
                    if (from != null) {
                        xmlStreamWriter.writeAttribute("from", from.toString());
                    }
                    xmlStreamWriter.writeAttribute("version", "1.0");
                    xmlStreamWriter.writeNamespace("", namespace);
                    xmlStreamWriter.writeNamespace("stream", "http://etherx.jabber.org/streams");
                    xmlStreamWriter.writeCharacters("");
                    xmlStreamWriter.flush();
                    if (debugger != null) {
                        debugger.writeStanza(new String(byteArrayOutputStream.toByteArray()).trim(), null);
                        byteArrayOutputStream.reset();
                    }
                    streamOpened = true;
                } catch (Exception e) {
                    notifyException(e);
                }
            });
        }
    }

    private void closeStream() {
        executor.execute(() -> {
            if (streamOpened) {
                // Close the stream.
                try {
                    xmlStreamWriter.writeEndElement();
                    xmlStreamWriter.flush();
                    if (debugger != null) {
                        debugger.writeStanza(new String(byteArrayOutputStream.toByteArray()).trim(), null);
                        byteArrayOutputStream.reset();
                    }
                    xmlStreamWriter.close();
                    streamOpened = false;
                } catch (Exception e) {
                    notifyException(e);
                }
            }
        });
    }

    /**
     * Shuts down the executors, cleans up resources and notifies the session about the exception.
     *
     * @param exception The exception which occurred during writing.
     */
    private void notifyException(Exception exception) {

        // Shutdown the executors.
        synchronized (this) {
            if (keepAliveExecutor != null) {
                keepAliveExecutor.shutdown();
                keepAliveExecutor = null;
            }
            executor.shutdown();

            if (prefixFreeCanonicalizationWriter != null) {
                try {
                    prefixFreeCanonicalizationWriter.close();
                    prefixFreeCanonicalizationWriter = null;
                } catch (Exception e) {
                    exception.addSuppressed(e);
                }
            }
            byteArrayOutputStream = null;
        }

        xmppSession.notifyException(exception);
    }

    /**
     * Closes the stream by sending a closing {@code </stream:stream>} to the server.
     * This method waits until this task is completed, but not more than 0.25 seconds.
     * <p>
     * Make sure to synchronize this method.
     * Otherwise multiple threads could call {@link #closeStream()} which may result in a {@link RejectedExecutionException}, if it has been shutdown by another thread in the meantime.
     */
    synchronized void shutdown() {
        // If the writer is still active, close the stream and afterwards shutdown the writer.
        if (!executor.isShutdown()) {
            // Send the closing stream tag.
            closeStream();
            // Shutdown the executor
            if (keepAliveExecutor != null) {
                keepAliveExecutor.shutdown();
            }
            executor.shutdown();

            // Wait for termination after shutdown.
            try {
                if (keepAliveExecutor != null) {
                    keepAliveExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
                }
                executor.awaitTermination(250, TimeUnit.MILLISECONDS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
