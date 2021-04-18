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

package rocks.xmpp.websocket.net.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.SessionException;
import javax.websocket.server.HandshakeRequest;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.glassfish.tyrus.client.ThreadPoolConfig;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.client.TransportConnector;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.websocket.codec.XmppWebSocketDecoder;
import rocks.xmpp.websocket.codec.XmppWebSocketEncoder;

/**
 * A WebSocket transport connector which uses Jakarta WebSocket API with Tyrus as reference implementation.
 *
 * <h3>Sample Usage</h3>
 *
 * <pre>{@code
 * WebSocketConnectionConfiguration webSocketConfiguration = WebSocketConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(443)
 *     .sslContext(sslContext)
 *     .channelEncryption(ChannelEncryption.DIRECT)
 *     .connector(new JakartaWebSocketConnector())
 *     .build();
 * }</pre>
 *
 * @see WebSocketConnectionConfiguration.Builder#connector(TransportConnector)
 */
public final class JakartaWebSocketConnector extends AbstractWebSocketConnector {

    @Override
    public final CompletableFuture<Connection> connect(final XmppSession xmppSession,
                                                       final WebSocketConnectionConfiguration configuration) {
        final CompletableFuture<Void> closeFuture = new CompletableFuture<>();

        final AtomicBoolean handshakeSucceeded = new AtomicBoolean();
        final ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create()
                .encoders(Collections.singletonList(XmppWebSocketEncoder.class))
                .decoders(Collections.singletonList(XmppWebSocketDecoder.class))
                .preferredSubprotocols(Collections.singletonList("xmpp"))
                .configurator(new ClientEndpointConfig.Configurator() {
                    @Override
                    public void afterResponse(HandshakeResponse response) {
                        // If a client receives a handshake response that does not include
                        // 'xmpp' in the 'Sec-WebSocket-Protocol' header, then an XMPP
                        // subprotocol WebSocket connection was not established and the client
                        // MUST close the WebSocket connection.
                        List<String> responseHeader =
                                response.getHeaders().get(HandshakeRequest.SEC_WEBSOCKET_PROTOCOL);
                        if (responseHeader != null && responseHeader.contains("xmpp")) {
                            handshakeSucceeded.set(true);
                        }
                    }
                }).build();
        clientEndpointConfig.getUserProperties().put(XmppWebSocketEncoder.UserProperties.MARSHALLER,
                (Supplier<Marshaller>) xmppSession::createMarshaller);
        clientEndpointConfig.getUserProperties().put(XmppWebSocketDecoder.UserProperties.UNMARSHALLER,
                (Supplier<Unmarshaller>) xmppSession::createUnmarshaller);
        if (xmppSession.getDebugger() != null) {
            clientEndpointConfig.getUserProperties()
                    .put(XmppWebSocketEncoder.UserProperties.ON_WRITE, xmppSession.getWriterInterceptors());
            clientEndpointConfig.getUserProperties()
                    .put(XmppWebSocketDecoder.UserProperties.ON_READ, Collections.singleton(xmppSession.getDebugger()));
        }
        clientEndpointConfig.getUserProperties().put(XmppWebSocketEncoder.UserProperties.XML_OUTPUT_FACTORY,
                xmppSession.getConfiguration().getXmlOutputFactory());
        clientEndpointConfig.getUserProperties().put(XmppWebSocketDecoder.UserProperties.XML_INPUT_FACTORY,
                xmppSession.getConfiguration().getXmlInputFactory());

        final ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());
        if (configuration.getSSLContext() != null) {
            SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(configuration.getSSLContext());
            client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
            sslEngineConfigurator.setHostnameVerifier(configuration.getHostnameVerifier());
        }

        final ThreadPoolConfig config = ThreadPoolConfig.defaultConfig();
        config.setThreadFactory(xmppSession.getConfiguration().getThreadFactory("WebSocket Client"));
        client.getProperties().put(ClientProperties.WORKER_THREAD_POOL_CONFIG, config);

        int connectTimeout = configuration.getConnectTimeout();
        if (connectTimeout > 0) {
            client.getProperties().put(ClientProperties.HANDSHAKE_TIMEOUT, connectTimeout);
        }
        final Proxy proxy = configuration.getProxy();
        if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
            client.getProperties().put(ClientProperties.PROXY_URI,
                    "http://" + inetSocketAddress.getHostName() + ':' + inetSocketAddress.getPort());
        }

        final URI uri;
        CompletableFuture<Connection> connectionFuture = new CompletableFuture<>();
        try {
            uri = getUri(xmppSession, configuration);

            client.asyncConnectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    if (!handshakeSucceeded.get()) {
                        try {
                            String msg =
                                    "Server did not respond with 'Sec-WebSocket-Protocol' header with value 'xmpp'.";
                            session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, msg));
                        } catch (IOException e) {
                            xmppSession.notifyException(e);
                        }
                    } else {
                        JakartaWebSocketClientConnection webSocketConnection =
                                new JakartaWebSocketClientConnection(session, closeFuture, xmppSession,
                                        configuration, uri);
                        connectionFuture.complete(webSocketConnection);
                        config.getUserProperties().put(XmppWebSocketEncoder.UserProperties.SESSION, xmppSession);
                        config.getUserProperties()
                                .put(XmppWebSocketEncoder.UserProperties.CONNECTION, webSocketConnection);
                    }
                }

                @Override
                public void onError(Session session, Throwable t) {
                    connectionFuture.completeExceptionally(t);
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    if (closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
                        closeFuture.completeExceptionally(new SessionException(closeReason.toString(), null, session));
                    }
                    closeFuture.complete(null);
                }
            }, clientEndpointConfig, uri);
        } catch (Exception e) {
            connectionFuture.completeExceptionally(e);
        }

        return connectionFuture;
    }
}
