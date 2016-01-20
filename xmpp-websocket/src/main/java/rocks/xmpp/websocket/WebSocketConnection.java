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
import rocks.xmpp.core.session.Connection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.extensions.sm.StreamManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.websocket.model.Close;
import rocks.xmpp.websocket.model.Open;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * An XMPP WebSocket connection method.
 *
 * @author Christian Schudt
 * @see WebSocketConnectionConfiguration
 * @see <a href="https://tools.ietf.org/html/rfc7395">XMPP Subprotocol for WebSocket</a>
 * @since 0.7.0
 */
public final class WebSocketConnection extends Connection {

    private final StreamManager streamManager;

    private final XmppDebugger debugger;

    private final WebSocketConnectionConfiguration connectionConfiguration;

    private final Lock lock = new ReentrantLock();

    private final Condition closeReceived = lock.newCondition();

    /**
     * Guarded by "this".
     */
    private boolean closedByServer;

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

    private ExecutorService executorService;

    WebSocketConnection(XmppSession xmppSession, WebSocketConnectionConfiguration connectionConfiguration) {
        super(xmppSession, connectionConfiguration);
        this.connectionConfiguration = connectionConfiguration;
        this.debugger = xmppSession.getDebugger();
        this.streamManager = getXmppSession().getManager(StreamManager.class);
    }

    void initialize() {
        StreamFeaturesManager streamFeaturesManager = getXmppSession().getManager(StreamFeaturesManager.class);
        streamFeaturesManager.addFeatureNegotiator(streamManager);
    }

    private static String findWebSocketEndpoint(String xmppServiceDomain) {

        try {
            String query = "_xmppconnect." + xmppServiceDomain;

            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

            DirContext ctx = new InitialDirContext(env);

            Attributes attributes = ctx.getAttributes(query, new String[]{"TXT"});
            Attribute srvAttribute = attributes.get("TXT");

            if (srvAttribute != null) {
                NamingEnumeration<?> enumeration = srvAttribute.getAll();
                while (enumeration.hasMore()) {
                    String txtRecord = (String) enumeration.next();
                    String[] txtRecordParts = txtRecord.split("=");
                    String key = txtRecordParts[0];
                    String value = txtRecordParts[1];
                    if ("_xmpp-client-websocket".equals(key)) {
                        return value;
                    }
                }
            }
        } catch (NamingException e) {
            return null;
        }
        return null;
    }

    @Override
    protected final void restartStream() {
        send(new Open(getXmppSession().getDomain(), getXmppSession().getConfiguration().getLanguage()));
    }

    @Override
    public final synchronized Future<?> send(StreamElement streamElement) {
        // Note: The TyrusFuture returned by session.getAsyncRemote().sendText() is not cancellable.
        // Therefore use our own future with the BasicRemote.
        return executorService.submit(() -> {
            try (StringWriter writer = new StringWriter()) {
                XMLStreamWriter xmlStreamWriter = null;
                try {
                    xmlStreamWriter = XmppUtils.createXmppStreamWriter(getXmppSession().getConfiguration().getXmlOutputFactory().createXMLStreamWriter(writer), null);
                    getXmppSession().createMarshaller().marshal(streamElement, xmlStreamWriter);
                    xmlStreamWriter.flush();
                    String xml = writer.toString();
                    if (streamElement instanceof Stanza) {
                        // When about to send a stanza, first put the stanza (paired with the current value of X) in an "unacknowleged" queue.
                        this.streamManager.markUnacknowledged((Stanza) streamElement);
                    }

                    try {
                        session.getBasicRemote().sendText(xml);
                        if (streamElement instanceof Stanza && streamManager.isActive() && streamManager.getRequestStrategy().test((Stanza) streamElement)) {
                            send(StreamManagement.REQUEST);
                        }
                        if (debugger != null) {
                            debugger.writeStanza(xml, streamElement);
                        }
                    } catch (IOException e) {
                        getXmppSession().notifyException(e);
                    }

                } finally {
                    if (xmlStreamWriter != null) {
                        xmlStreamWriter.close();
                    }
                }
            } catch (Exception e) {
                getXmppSession().notifyException(e);
            }
            return null;
        });
    }

    @Override
    public final void connect(final Jid from, final String namespace, final Consumer<Jid> onStreamOpened) throws IOException {

        try {
            final URI path;
            synchronized (this) {
                if (session != null && session.isOpen()) {
                    // Already connected.
                    return;
                }
                this.exception = null;
                this.closedByServer = false;
                this.executorService = Executors.newSingleThreadExecutor();

                if (uri == null) {
                    String protocol = connectionConfiguration.isSecure() ? "wss" : "ws";
                    // If no port has been configured, use the default ports.
                    int targetPort = getPort() > 0 ? getPort() : (connectionConfiguration.isSecure() ? 5281 : 5280);
                    // If a hostname has been configured, use it to connect.
                    if (getHostname() != null) {
                        uri = new URI(protocol, null, getHostname(), targetPort, connectionConfiguration.getPath(), null, null);
                    } else if (getXmppSession().getDomain() != null) {
                        // If a URL has not been set, try to find the URL by the domain via a DNS-TXT lookup as described in XEP-0156.
                        String resolvedUrl = findWebSocketEndpoint(getXmppSession().getDomain().toString());
                        if (resolvedUrl != null) {
                            uri = new URI(resolvedUrl);
                        } else {
                            // Fallback mechanism:
                            // If the URL could not be resolved, use the domain name and port 5280 as default.
                            uri = new URI(protocol, null, getXmppSession().getDomain().toString(), targetPort, connectionConfiguration.getPath(), null, null);
                        }
                        this.port = uri.getPort();
                        this.hostname = uri.getHost();
                    } else {
                        throw new IllegalStateException("Neither an URL nor a domain given for a BOSH connection.");
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

            final Proxy proxy = connectionConfiguration.getProxy();
            if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
                client.getProperties().put(ClientProperties.PROXY_URI, "http://" + proxy.address().toString());
            }

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
                    }

                    session.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String message) {
                            try {
                                Object element = getXmppSession().createUnmarshaller().unmarshal(new StringReader(message));
                                if (debugger != null) {
                                    debugger.readStanza(message, element);
                                }
                                if (element instanceof Open) {
                                    Open open = (Open) element;
                                    onStreamOpened.accept(open.getFrom());
                                    synchronized (WebSocketConnection.this) {
                                        streamId = open.getId();
                                    }
                                } else if (element instanceof Close) {
                                    synchronized (WebSocketConnection.this) {
                                        closedByServer = true;
                                    }
                                    close();
                                    lock.lock();
                                    try {
                                        closeReceived.signalAll();
                                    } finally {
                                        lock.unlock();
                                    }
                                }
                                if (getXmppSession().handleElement(element)) {
                                    restartStream();
                                }
                            } catch (Exception e) {
                                getXmppSession().notifyException(e);
                            }
                        }
                    });

                    // Opens the stream
                    restartStream();
                }

                @Override
                public void onError(Session session, Throwable t) {
                    synchronized (WebSocketConnection.this) {
                        exception = t;
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
            }
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
    public final synchronized void close() throws Exception {
        if (session != null && session.isOpen()) {
            send(new Close());

            lock.lock();
            try {
                if (!closedByServer) {
                    closeReceived.await(500, TimeUnit.MILLISECONDS);
                }
            } finally {
                lock.unlock();
            }
            executorService.shutdown();
            executorService = null;
            session.close();
        }
    }

    @Override
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder("WebSocket connection");
        if (hostname != null) {
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
}
