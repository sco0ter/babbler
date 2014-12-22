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

package rocks.xmpp.core.session;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stream.model.ClientStreamElement;

import java.io.Closeable;
import java.io.IOException;
import java.net.Proxy;

/**
 * The base connection class which provides hostname, port and proxy information.
 *
 * @author Christian Schudt
 */
public abstract class Connection implements Closeable {

    /**
     * The proxy, which is used while connecting to a host.
     */
    private final Proxy proxy;

    protected String hostname;

    protected int port;

    private XmppSession xmppSession;

    /**
     * Creates a connection to the specified host and port through a proxy.
     *
     * @param connectionConfiguration The connection configuration.
     */
    protected Connection(XmppSession xmppSession, ConnectionConfiguration connectionConfiguration) {
        this.xmppSession = xmppSession;
        this.hostname = connectionConfiguration.getHostname();
        this.port = connectionConfiguration.getPort();
        this.proxy = connectionConfiguration.getProxy();
    }

    /**
     * Creates a connection to the specified host and port through a proxy.
     *
     * @param hostname The host, which is used to establish the connection.
     * @param port     The port, which is used to establish the connection.
     * @param proxy    The proxy.
     */
    protected Connection(String hostname, int port, Proxy proxy) {
        this.hostname = hostname;
        this.port = port;
        this.proxy = proxy;
    }

    /**
     * Gets the XMPP session which is associated with this connection. This method should only be called from the session itself.
     *
     * @return The XMPP session.
     */
    public final XmppSession getXmppSession() {
        return xmppSession;
    }

    /**
     * Sets the XMPP session which is associated with this connection. This method should only be called from the session itself.
     *
     * @param xmppSession The XMPP session.
     */
    @Deprecated
    public final void initialize(XmppSession xmppSession) {
        this.xmppSession = xmppSession;
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
     * Gets the proxy.
     *
     * @return The proxy.
     */
    public final Proxy getProxy() {
        return proxy;
    }

    /**
     * Restarts the stream.
     */
    protected abstract void restartStream();

    /**
     * Sends an element.
     *
     * @param clientStreamElement The element.
     */
    public abstract void send(ClientStreamElement clientStreamElement);

    /**
     * Compresses the stream.
     */
    protected void compressStream() {
    }

    /**
     * Connects to the server.
     *
     * @throws IOException If no connection could be established, e.g. due to unknown host.
     * @deprecated Use {@link #connect(rocks.xmpp.core.Jid)}
     */
    @Deprecated
    public abstract void connect() throws IOException;

    /**
     * Connects to the server and provides an optional 'from' attribute.
     *
     * @param from The 'from' attribute.
     * @throws IOException If no connection could be established, e.g. due to unknown host.
     */
    public abstract void connect(Jid from) throws IOException;
}
