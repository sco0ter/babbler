/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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
import rocks.xmpp.core.stream.model.StreamElement;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * The base connection class which provides hostname, port and proxy information.
 *
 * @author Christian Schudt
 */
public abstract class Connection implements AutoCloseable {

    /**
     * The proxy, which is used while connecting to a host.
     */
    private final Proxy proxy;

    protected String hostname;

    protected int port;

    protected Jid from;

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
     * Gets the XMPP session which is associated with this connection. This method should only be called from the session itself.
     *
     * @return The XMPP session.
     */
    public final XmppSession getXmppSession() {
        return xmppSession;
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
     * @param streamElement The element.
     * @return The future representing the sent process and which allows to cancel it.
     */
    public abstract Future<?> send(StreamElement streamElement);

    /**
     * Connects to the server and provides an optional 'from' attribute.
     *
     * @param from           The 'from' attribute.
     * @param namespace      The content namespace, e.g. "jabber:client".
     * @param onStreamOpened The callback which gets notified when the stream gets opened, i.e. when the server has responded with a stream header. The parameter of the consumer is the stream id.
     * @throws IOException If no connection could be established, e.g. due to unknown host.
     */
    public abstract void connect(Jid from, String namespace, Consumer<Jid> onStreamOpened) throws IOException;

    /**
     * Indicates whether this connection is secured by TLS/SSL.
     *
     * @return True, if this connection is secured.
     */
    public abstract boolean isSecure();

    /**
     * Gets the stream id of this connection.
     *
     * @return The stream id.
     */
    public abstract String getStreamId();

    public abstract boolean isUsingAcknowledgements();
}
