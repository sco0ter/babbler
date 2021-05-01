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

package rocks.xmpp.websocket.net.client;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.client.TransportConnector;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;

/**
 * A WebSocket transport connector which uses {@link java.net.http.WebSocket}.
 *
 * <p>This is the default connector for WebSocket based XMPP connections if none is defined.</p>
 *
 * <p>However, you could still explicitly set it as shown below.</p>
 *
 * <h3>Sample Usage</h3>
 *
 * <pre>{@code
 * WebSocketConnectionConfiguration webSocketConfiguration = WebSocketConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(443)
 *     .sslContext(sslContext)
 *     .channelEncryption(ChannelEncryption.DIRECT)
 *     .connector(new HttpClientWebSocketConnector())
 *     .build();
 * }</pre>
 *
 * <h3>Hostname Verification</h3>
 *
 * <p>Please note that the WebSocket API does not allow to set a hostname verifier. To disable hostname verification
 * you have to set the system property:</p>
 *
 * <pre>-Djdk.internal.httpclient.disableHostnameVerification</pre>
 *
 * @see WebSocketConnectionConfiguration.Builder#connector(TransportConnector)
 */
public final class HttpClientWebSocketConnector extends AbstractWebSocketConnector {

    @Override
    public final CompletableFuture<Connection> connect(final XmppSession xmppSession,
                                                       final WebSocketConnectionConfiguration configuration,
                                                       final SessionOpen sessionOpen) {
        final CompletableFuture<Void> closeFuture = new CompletableFuture<>();

        final URI uri;
        try {
            uri = getUri(xmppSession, configuration);
        } catch (URISyntaxException e) {
            return CompletableFuture.failedFuture(e);
        }
        HttpClientWebSocketConnection webSocketClientConnection =
                new HttpClientWebSocketConnection(configuration, uri, xmppSession, closeFuture);
        HttpClient.Builder builder = HttpClient.newBuilder();

        if (configuration.getSSLContext() != null) {
            builder.sslContext(configuration.getSSLContext());
        }
        if (configuration.getProxy() != null) {
            builder.proxy(ProxySelector.of((InetSocketAddress) configuration.getProxy().address()));
        }
        if (configuration.getConnectTimeout() > 0) {
            builder.connectTimeout(Duration.ofMillis(configuration.getConnectTimeout()));
        }
        return builder.build()
                .newWebSocketBuilder()
                .subprotocols("xmpp")
                .buildAsync(uri, webSocketClientConnection)
                .thenCompose(webSocket -> webSocketClientConnection.open(sessionOpen))
                .thenApply(aVoid -> webSocketClientConnection);
    }
}
