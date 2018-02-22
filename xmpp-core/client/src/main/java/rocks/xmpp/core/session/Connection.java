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

import rocks.xmpp.core.net.AbstractConnection;
import rocks.xmpp.core.session.model.SessionOpen;

import java.net.Proxy;

/**
 * The base connection class which provides hostname, port and proxy information.
 *
 * @author Christian Schudt
 */
public abstract class Connection extends AbstractConnection {

    private final ConnectionConfiguration connectionConfiguration;

    protected final XmppSession xmppSession;

    /**
     * The proxy, which is used while connecting to a host.
     */
    private final Proxy proxy;

    protected String hostname;

    protected int port;

    protected SessionOpen sessionOpen;

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
        this.connectionConfiguration = connectionConfiguration;
    }

    /**
     * Gets the configuration for this connection.
     *
     * @return The configuration.
     */
    public final ConnectionConfiguration getConfiguration() {
        return connectionConfiguration;
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
     * Indicates, whether this connection is using acknowledgements.
     * <p>
     * TCP and WebSocket connections use <a href="http://xmpp.org/extensions/xep-0198.html">XEP-0198: Stream Management</a> to acknowledge stanzas.
     * <p>
     * BOSH connections use another mechanism for acknowledgements, described in <a href="http://xmpp.org/extensions/xep-0124.html#ack">XEP-0124 § 9. Acknowledgements</a>.
     *
     * @return True, if this connection is using acknowledgements.
     * @see <a href="http://xmpp.org/extensions/xep-0198.html">XEP-0198: Stream Management</a>
     * @see <a href="http://xmpp.org/extensions/xep-0124.html#ack">XEP-0124 § 9. Acknowledgements</a>
     */
    public abstract boolean isUsingAcknowledgements();
}
