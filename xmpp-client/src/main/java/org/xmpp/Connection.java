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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * @author Christian Schudt
 */
public abstract class Connection implements Closeable {

    /**
     * The proxy, which is used while connecting to a host.
     */
    protected final Proxy proxy;

    private final String hostname;

    private final int port;

    protected XmppSession xmppSession;

    /**
     * Any exception that occurred during stream negotiation ({@link #connect()}).
     */
    private volatile Exception exception;

    /**
     * Creates a connection to the specified host and port through a proxy.
     *
     * @param hostname          The host, which is used to establish the connection.
     * @param port              The port, which is used to establish the connection.
     * @param proxy             The proxy.
     */
    protected Connection(XmppSession xmppSession, String hostname, int port, Proxy proxy) {
        this.xmppSession = xmppSession;
        this.hostname = hostname;
        this.port = port;
        this.proxy = proxy;
    }

    public void setXmppSession(XmppSession xmppSession) {
        this.xmppSession = xmppSession;
    }

    /**
     * Creates the reader to read the stream.
     *
     * @param inputStream The input stream.
     * @return The XML reader.
     * @throws javax.xml.stream.XMLStreamException If the reader could not be created.
     */
    protected XMLEventReader createXMLEventReader(InputStream inputStream) throws XMLStreamException {
        return xmppSession.xmlInputFactory.createXMLEventReader(inputStream);
    }

    /**
     * Gets the hostname, which is used for the connection.
     *
     * @return The hostname.
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * Gets the port, which is used for the connection.
     *
     * @return The port.
     */
    public final int getPort() {
        return port;
    }

    /**
     * Restarts the stream.
     */
    protected abstract void restartStream();

    public abstract void send(ClientStreamElement clientStreamElement);

    /**
     * Compresses the stream.
     */
    protected void compressStream() {
    }

    public abstract void connect() throws IOException;

    /**
     * Secures the connection, i.e. negotiates TLS.
     *
     * @throws IOException If an error occurs during TLS negotiation.
     */
    protected void secureConnection() throws IOException {
    }

    /**
     * Waits until SASL negotiation has started and then releases the lock. This method must be invoked at the end of the {@link #connect()} method.
     *
     * @throws NoResponseException If no response was received from the server.
     * @throws IOException         If any exception occurred during stream negotiation.
     */
    protected final void waitUntilSaslNegotiationStarted() throws NoResponseException, IOException {
        // Wait for the response and wait until all features have been negotiated.
        xmppSession.lock.lock();
        try {
            if (!xmppSession.streamNegotiatedUntilSasl.await(10000, TimeUnit.SECONDS)) {
                throw new NoResponseException("Timeout reached while connecting.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            xmppSession.lock.unlock();
        }

        // Check if an exception has occurred during stream negotiation and throw it.
        if (exception != null) {
            try {
                throw new IOException(exception);
            } finally {
                exception = null;
            }
        }
    }
}
