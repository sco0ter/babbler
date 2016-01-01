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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.util.XmppUtils;

import javax.xml.XMLConstants;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.EnumSet;
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

    private final ScheduledExecutorService executor;

    private final Marshaller marshaller;

    private final XmppDebugger debugger;

    private final String namespace;

    private final TcpConnection connection;

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

    XmppStreamWriter(String namespace, TcpConnection tcpConnection, final XmppSession xmppSession) {
        this.namespace = namespace;
        this.xmppSession = xmppSession;
        this.marshaller = xmppSession.createMarshaller();
        this.debugger = xmppSession.getDebugger();
        this.executor = Executors.newSingleThreadScheduledExecutor(XmppUtils.createNamedThreadFactory("XMPP Writer Thread"));
        this.connection = tcpConnection;
    }

    void initialize(int keepAliveInterval) {
        if (keepAliveInterval > 0) {
            executor.scheduleAtFixedRate(() -> {
                if (EnumSet.of(XmppSession.Status.CONNECTED, XmppSession.Status.AUTHENTICATED).contains(xmppSession.getStatus())) {
                    try {
                        xmlStreamWriter.writeCharacters(" ");
                        xmlStreamWriter.flush();
                    } catch (Exception e) {
                        notifyException(e);
                    }
                }
            }, 0, keepAliveInterval, TimeUnit.SECONDS);
        }
    }

    synchronized void send(final StreamElement clientStreamElement) {
        if (!executor.isShutdown() && clientStreamElement != null) {
            executor.execute(() -> {
                try {
                    marshaller.marshal(clientStreamElement, prefixFreeCanonicalizationWriter);
                    prefixFreeCanonicalizationWriter.flush();

                    if (clientStreamElement instanceof Stanza) {
                        // Workaround: Simulate keep-alive packet to convince client to process the already transmitted packet.
                        prefixFreeCanonicalizationWriter.writeCharacters(" ");
                        prefixFreeCanonicalizationWriter.flush();

                        // When about to send a stanza, first put the stanza (paired with the current value of X) in an "unacknowleged" queue.
                        connection.streamManager.markUnacknowledged((Stanza) clientStreamElement);
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
                    xmlStreamWriter = xmppSession.getConfiguration().getXmlOutputFactory().createXMLStreamWriter(xmppOutputStream, "UTF-8");

                    prefixFreeCanonicalizationWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, namespace);
                    streamOpened = false;

                    xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
                    xmlStreamWriter.writeStartElement("stream", "stream", StreamFeatures.NAMESPACE);
                    xmlStreamWriter.writeAttribute(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI, "lang", xmppSession.getConfiguration().getLanguage().toLanguageTag());
                    if (xmppSession.getDomain() != null) {
                        xmlStreamWriter.writeAttribute("to", xmppSession.getDomain().toString());
                    }
                    if (from != null) {
                        xmlStreamWriter.writeAttribute("from", from.toEscapedString());
                    }
                    xmlStreamWriter.writeAttribute("version", "1.0");
                    xmlStreamWriter.writeNamespace("", namespace);
                    xmlStreamWriter.writeNamespace("stream", StreamFeatures.NAMESPACE);
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
            executor.shutdown();
            try {
                // Wait for the closing stream element to be sent before we can close the socket.
                executor.awaitTermination(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
