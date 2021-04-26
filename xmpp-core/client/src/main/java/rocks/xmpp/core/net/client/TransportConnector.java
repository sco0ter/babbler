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

package rocks.xmpp.core.net.client;

import java.util.concurrent.CompletableFuture;

import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;

/**
 * Transport Connectors bind XMPP to a transport protocol such as TCP, BOSH or WebSockets.
 *
 * <p>Connector implementations establish a connection using a {@link ClientConnectionConfiguration connection
 * configuration} and a concrete transport or protocol implementation.
 *
 * <h3>Examples</h3>
 *
 * <p>TCP connections could be established with different low-level TCP implementations such as
 * {@link java.net.Socket} or {@link java.nio.channels.SocketChannel}.</p>
 *
 * <p>BOSH connections could use {@link java.net.HttpURLConnection} or {@link java.net.http.HttpClient}</p>
 *
 * <p>WebSocket connections could either use {@link java.net.http.WebSocket} or the Jakarta WebSocket API.</p>
 *
 * @param <T> A specific configuration for the transport protocol, e.g. a configuration for TCP, BOSH or WebSocket
 *            connections.
 */
public interface TransportConnector<T extends ClientConnectionConfiguration> {

    /**
     * Establishes a connection using the transport protocol specific configuration.
     *
     * @param xmppSession   The XMPP session which will be bound to the transport protocol.
     * @param configuration The connection configuration for the specific transport protocol.
     * @param sessionOpen   The session open element.
     * @return A {@link CompletableFuture} which returns an established (connected) connection on completion.
     */
    CompletableFuture<Connection> connect(XmppSession xmppSession, T configuration, SessionOpen sessionOpen);
}
