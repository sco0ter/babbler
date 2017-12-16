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

package rocks.xmpp.websocket;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Connection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.dns.DnsResolver;
import rocks.xmpp.dns.TxtRecord;
import rocks.xmpp.extensions.sm.StreamManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.CompletionStages;
import rocks.xmpp.websocket.model.Close;
import rocks.xmpp.websocket.model.Open;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.SessionException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An XMPP WebSocket connection method.
 *
 * @author Christian Schudt
 * @see WebSocketConnectionConfiguration
 * @see <a href="https://tools.ietf.org/html/rfc7395">XMPP Subprotocol for WebSocket</a>
 * @since 0.7.0
 */
public final class WebSocketConnection extends Connection {

    private final StreamFeaturesManager streamFeaturesManager;

    private final StreamManager streamManager;

    private final XmppDebugger debugger;

    private final WebSocketConnectionConfiguration connectionConfiguration;

    private final Set<String> pings = new CopyOnWriteArraySet<>();

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private CompletableFuture<Void> closeReceived;

    /**
     * Guarded by "this".
     */
    private URI uri;

    /**
     * Guarded by "this".
     */
    private Session session;

    /**
     * Guarded by "this".
     */
    private String streamId;

    /**
     * Guarded by "this".
     */
    private Throwable exception;

    private ScheduledExecutorService executorService;

    /**
     * Guarded by "this".
     */
    private Future<?> pingFuture;

    /**
     * Guarded by "this".
     */
    private Future<?> pongFuture;

    WebSocketConnection(XmppSession xmppSession, WebSocketConnectionConfiguration connectionConfiguration) {
        super(xmppSession, connectionConfiguration);
        this.connectionConfiguration = connectionConfiguration;
        this.debugger = xmppSession.getDebugger();
        this.streamFeaturesManager = xmppSession.getManager(StreamFeaturesManager.class);
        this.streamManager = xmppSession.getManager(StreamManager.class);
    }

    private static String findWebSocketEndpoint(String xmppServiceDomain, String nameServer, long timeout) {

        try {
            List<TxtRecord> txtRecords = DnsResolver.resolveTXT(xmppServiceDomain, nameServer, timeout);
            for (TxtRecord txtRecord : txtRecords) {
                Map<String, String> attributes = txtRecord.asAttributes();
                String url = attributes.get("_xmpp-client-websocket");
                if (url != null) {
                    return url;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @Override
    protected final void restartStream() {
        send(new Open(xmppSession.getDomain(), xmppSession.getConfiguration().getLanguage()));
    }

    @Override
    public final synchronized CompletableFuture<Void> send(StreamElement streamElement) {
        // Note: The TyrusFuture returned by session.getAsyncRemote().sendText() is not cancellable.
        // Therefore use our own future with the BasicRemote.
        return CompletableFuture.runAsync(() -> {
            try (StringWriter writer = new StringWriter()) {
                XMLStreamWriter xmlStreamWriter = null;
                try {
                    xmlStreamWriter = XmppUtils.createXmppStreamWriter(xmppSession.getConfiguration().getXmlOutputFactory().createXMLStreamWriter(writer), null);
                    xmppSession.createMarshaller().marshal(streamElement, xmlStreamWriter);
                    xmlStreamWriter.flush();
                    String xml = writer.toString();
                    if (streamElement instanceof Stanza) {
                        // When about to send a stanza, first put the stanza (paired with the current value of X) in an "unacknowleged" queue.
                        this.streamManager.markUnacknowledged((Stanza) streamElement);
                    }

                    session.getBasicRemote().sendText(xml);
                    if (streamElement instanceof Stanza && streamManager.isActive() && streamManager.getRequestStrategy().test((Stanza) streamElement)) {
                        send(StreamManagement.REQUEST);
                    }
                    if (debugger != null) {
                        debugger.writeStanza(xml, streamElement);
                    }

                } finally {
                    if (xmlStreamWriter != null) {
                        xmlStreamWriter.close();
                    }
                }
            } catch (Exception e) {
                xmppSession.notifyException(e);
                throw new CompletionException(e);
            }
        }, executorService);
    }

    @Override
    public final void connect(final Jid from, final String namespace) throws IOException {

        try {
            final URI path;
            synchronized (this) {
                if (session != null && session.isOpen()) {
                    // Already connected.
                    return;
                }
                this.exception = null;

                if (uri == null) {
                    String protocol = connectionConfiguration.isSecure() ? "wss" : "ws";
                    // If no port has been configured, use the default ports.
                    int targetPort = getPort() > 0 ? getPort() : (connectionConfiguration.isSecure() ? 5281 : 5280);
                    // If a hostname has been configured, use it to connect.
                    if (getHostname() != null) {
                        uri = new URI(protocol, null, getHostname(), targetPort, connectionConfiguration.getPath(), null, null);
                    } else if (xmppSession.getDomain() != null) {
                        // If a URL has not been set, try to find the URL by the domain via a DNS-TXT lookup as described in XEP-0156.
                        String resolvedUrl = findWebSocketEndpoint(xmppSession.getDomain().toString(), xmppSession.getConfiguration().getNameServer(), connectionConfiguration.getConnectTimeout());
                        if (resolvedUrl != null) {
                            uri = new URI(resolvedUrl);
                        } else {
                            // Fallback mechanism:
                            // If the URL could not be resolved, use the domain name and port 5280 as default.
                            uri = new URI(protocol, null, xmppSession.getDomain().toString(), targetPort, connectionConfiguration.getPath(), null, null);
                        }
                        this.port = uri.getPort();
                        this.hostname = uri.getHost();
                    } else {
                        throw new IllegalStateException("Neither an URL nor a domain given for a WebSocket connection.");
                    }
                }
                path = uri;
            }
            final AtomicBoolean handshakeSucceeded = new AtomicBoolean();
            final ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().configurator(new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(Map<String, List<String>> headers) {
                    // During the WebSocket handshake, the client MUST include the value
                    // 'xmpp' in the list of protocols for the 'Sec-WebSocket-Protocol'
                    // header.
                    headers.put("Sec-WebSocket-Protocol", Collections.singletonList("xmpp"));
                }

                @Override
                public void afterResponse(HandshakeResponse response) {
                    // If a client receives a handshake response that does not include
                    // 'xmpp' in the 'Sec-WebSocket-Protocol' header, then an XMPP
                    // subprotocol WebSocket connection was not established and the client
                    // MUST close the WebSocket connection.
                    List<String> responseHeader = response.getHeaders().get("Sec-WebSocket-Protocol");
                    if (responseHeader != null && responseHeader.contains("xmpp")) {
                        handshakeSucceeded.set(true);
                    }
                }
            }).build();

            final ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());
            if (connectionConfiguration.getSSLContext() != null) {
                SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(connectionConfiguration.getSSLContext());
                client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
                sslEngineConfigurator.setHostnameVerifier(connectionConfiguration.getHostnameVerifier());
            }

            int connectTimeout = connectionConfiguration.getConnectTimeout();
            if (connectTimeout > 0) {
                client.getProperties().put(ClientProperties.HANDSHAKE_TIMEOUT, connectTimeout);
            }
            final Proxy proxy = connectionConfiguration.getProxy();
            if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
                InetSocketAddress inetSocketAddress = ((InetSocketAddress) proxy.address());
                client.getProperties().put(ClientProperties.PROXY_URI, "http://" + inetSocketAddress.getHostName() + ':' + inetSocketAddress.getPort());
            }

            streamFeaturesManager.addFeatureNegotiator(streamManager);
            streamManager.reset();

            closeReceived = new CompletableFuture<>();
            final Session session = client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    synchronized (WebSocketConnection.this) {
                        WebSocketConnection.this.session = session;

                        if (!handshakeSucceeded.get()) {

                            try {
                                String msg = "Server response did not include 'Sec-WebSocket-Protocol' header with value 'xmpp'.";
                                exception = new IOException(msg);
                                session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, msg));
                                return;
                            } catch (IOException e) {
                                exception.addSuppressed(e);
                            }
                        }
                        WebSocketConnection.this.executorService = Executors.newSingleThreadScheduledExecutor(XmppUtils.createNamedThreadFactory("WebSocket send thread"));
                    }

                    session.addMessageHandler(String.class, message -> {
                        try {
                            Object element = xmppSession.createUnmarshaller().unmarshal(new StringReader(message));
                            if (debugger != null) {
                                debugger.readStanza(message, element);
                            }
                            if (element instanceof Open) {
                                Open open = (Open) element;
                                synchronized (WebSocketConnection.this) {
                                    streamId = open.getId();
                                }
                            } else if (element instanceof Close) {
                                CompletableFuture<Void> future;
                                synchronized (WebSocketConnection.this) {
                                    future = closeReceived;
                                }
                                if (future != null) {
                                    future.complete(null);
                                }
                                close();
                            }
                            if (xmppSession.handleElement(element)) {
                                restartStream();
                            }
                        } catch (Exception e) {
                            xmppSession.notifyException(e);
                        }
                    });

                    session.addMessageHandler(new PongHandler());

                    // Opens the stream
                    restartStream();
                }

                @Override
                public void onError(Session session, Throwable t) {
                    synchronized (WebSocketConnection.this) {
                        exception = t;
                    }
                    xmppSession.notifyException(t);
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    if (closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
                        xmppSession.notifyException(new SessionException(closeReason.toString(), null, session));
                    }
                }
            }, clientEndpointConfig, path);

            if (!session.isOpen()) {
                throw new IOException("Session could not be opened.");
            }
            synchronized (this) {
                if (exception != null) {
                    throw exception instanceof IOException ? (IOException) exception : new IOException(exception);
                }

                if (connectionConfiguration.getPingInterval() != null && !connectionConfiguration.getPingInterval().isNegative() && !connectionConfiguration.getPingInterval().isZero()) {
                    pingFuture = this.executorService.scheduleAtFixedRate(() -> {
                        // Send a WebSocket ping in an interval.
                        synchronized (this) {
                            try {
                                if (this.session != null && this.session.isOpen()) {
                                    String uuid = UUID.randomUUID().toString();
                                    if (pings.add(uuid)) {
                                        // Send the ping with the UUID as application data, so that we can match it to the pong.
                                        this.session.getBasicRemote().sendPing(ByteBuffer.wrap(uuid.getBytes(StandardCharsets.UTF_8)));
                                        // Later check if the ping has been answered by a pong.
                                        pongFuture = this.executorService.schedule(() -> {
                                            if (pings.remove(uuid)) {
                                                // Ping has not been removed by a corresponding pong (still unanswered).
                                                // Notify the session with an exception.
                                                xmppSession.notifyException(new XmppException("No WebSocket pong received in time."));
                                            }
                                        }, xmppSession.getConfiguration().getDefaultResponseTimeout().toMillis(), TimeUnit.MILLISECONDS);
                                    }
                                }
                            } catch (IOException e) {
                                xmppSession.notifyException(e);
                            }
                        }
                    }, 0, connectionConfiguration.getPingInterval().toMillis(), TimeUnit.MILLISECONDS);
                }
            }
            closed.set(false);
        } catch (DeploymentException | URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public final boolean isSecure() {
        return connectionConfiguration.isSecure();
    }

    @Override
    public final synchronized String getStreamId() {
        return streamId;
    }

    @Override
    public final boolean isUsingAcknowledgements() {
        return streamManager.isActive();
    }

    @Override
    public final void close() throws Exception {
        try {
            // Prevent that the connection is closed twice.
            if (closed.compareAndSet(false, true)) {
                Session session;
                synchronized (this) {
                    session = this.session;
                }
                if (session != null && session.isOpen()) {
                    send(new Close());

                    CompletableFuture<Void> future;
                    synchronized (this) {
                        future = closeReceived;
                    }
                    if (future != null) {
                        // Wait until we receive the "close" frame from the server, then close the session.
                        future.runAfterEither(CompletionStages.timeoutAfter(500, TimeUnit.MILLISECONDS), () -> {
                            try {
                                session.close();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                    } else {
                        session.close();
                    }
                }
            }
        } finally {
            streamFeaturesManager.removeFeatureNegotiator(streamManager);
            pings.clear();
            synchronized (this) {
                if (executorService != null) {
                    executorService.shutdown();
                    try {
                        if (!executorService.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                            executorService.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        // (Re-)Cancel if current thread also interrupted
                        executorService.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                    executorService = null;
                }
                if (pingFuture != null) {
                    pingFuture.cancel(false);
                    pingFuture = null;
                }
                if (pongFuture != null) {
                    pongFuture.cancel(false);
                    pongFuture = null;
                }
            }
        }
    }

    @Override
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder("WebSocket connection");
        if (uri != null) {
            sb.append(" to ").append(uri);
        }
        if (streamId != null) {
            sb.append(" (").append(streamId).append(')');
        }
        if (from != null) {
            sb.append(", from: ").append(from);
        }
        return sb.toString();
    }

    private final class PongHandler implements MessageHandler.Whole<PongMessage> {

        @Override
        public final void onMessage(final PongMessage message) {
            // We received a pong from the server.
            // We can now remove the corresponding ping.
            final byte[] bytes = new byte[message.getApplicationData().limit()];
            message.getApplicationData().get(bytes);
            pings.remove(new String(bytes, StandardCharsets.UTF_8));
        }
    }
}
