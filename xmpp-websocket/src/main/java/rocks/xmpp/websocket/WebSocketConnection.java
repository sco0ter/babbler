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

package rocks.xmpp.websocket;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.Connection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stream.model.StreamElement;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
 * @see <a href="https://tools.ietf.org/html/rfc7395">XMPP Subprotocol for WebSocket</a>
 */
public final class WebSocketConnection extends Connection {

    private final XmppDebugger debugger;

    private final WebSocketConnectionConfiguration connectionConfiguration;

    private final Lock lock = new ReentrantLock();

    private final Condition closeReceived = lock.newCondition();

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
    private IOException exception;

    protected WebSocketConnection(XmppSession xmppSession, WebSocketConnectionConfiguration connectionConfiguration) {
        super(xmppSession, connectionConfiguration);
        this.connectionConfiguration = connectionConfiguration;
        this.debugger = xmppSession.getDebugger();
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
        send(new Open(getXmppSession().getDomain(), Locale.getDefault().getLanguage()));
    }

    @Override
    public final synchronized void send(StreamElement streamElement) {
        if (session != null && session.isOpen()) {
            try {
                StringWriter writer = new StringWriter();
                XMLStreamWriter xmlStreamWriter = XmppUtils.createXmppStreamWriter(getXmppSession().getConfiguration().getXmlOutputFactory().createXMLStreamWriter(writer));
                getXmppSession().createMarshaller().marshal(streamElement, xmlStreamWriter);
                xmlStreamWriter.flush();
                String xml = writer.toString();
                session.getAsyncRemote().sendText(xml, result -> {
                    if (result.getException() != null) {
                        getXmppSession().notifyException(result.getException());
                    }
                });
                if (debugger != null) {
                    debugger.writeStanza(xml, streamElement);
                }
            } catch (Exception e) {
                getXmppSession().notifyException(e);
            }
        }
    }

    @Override
    public final void connect(Jid from, String namespace, Consumer<Jid> onStreamOpened) throws IOException {

        try {
            URI path;
            synchronized (this) {
                if (session != null) {
                    // Already connected.
                    return;
                }
                exception = null;
                closedByServer = false;

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
            AtomicBoolean handshakeSucceeded = new AtomicBoolean();
            ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().configurator(new ClientEndpointConfig.Configurator() {
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

            ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());
            if (connectionConfiguration.getSSLContext() != null) {
                SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(connectionConfiguration.getSSLContext());
                client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
                sslEngineConfigurator.setHostnameVerifier(connectionConfiguration.getHostnameVerifier());
            }
            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    synchronized (WebSocketConnection.this) {
                        WebSocketConnection.this.session = session;
                    }

                    if (!handshakeSucceeded.get()) {
                        try {
                            session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Server response did not include 'Sec-WebSocket-Protocol' header with value 'xmpp'."));
                            return;
                        } catch (IOException e) {
                            synchronized (WebSocketConnection.this) {
                                exception = e;
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
                                    closedByServer = true;
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
                    getXmppSession().notifyException(t);
                }

            }, clientEndpointConfig, path);

            synchronized (WebSocketConnection.this) {
                if (exception != null) {
                    throw exception;
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
