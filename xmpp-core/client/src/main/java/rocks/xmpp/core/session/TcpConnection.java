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
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.core.tls.client.StartTlsManager;
import rocks.xmpp.dns.DnsResolver;
import rocks.xmpp.dns.SrvRecord;
import rocks.xmpp.extensions.compress.CompressionManager;
import rocks.xmpp.extensions.compress.CompressionMethod;
import rocks.xmpp.extensions.compress.model.StreamCompression;
import rocks.xmpp.extensions.sm.StreamManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default TCP socket connection as described in <a href="http://xmpp.org/rfcs/rfc6120.html#tcp">TCP Binding</a>.
 * <p>
 * If no hostname is set (null or empty) the connection tries to resolve the hostname via an <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">SRV DNS lookup</a>.
 * <p>
 * This class is unconditionally thread-safe.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/rfcs/rfc6120.html#tcp">3.  TCP Binding</a>
 */
public final class TcpConnection extends Connection {

    private static final Logger logger = Logger.getLogger(TcpConnection.class.getName());

    private final StreamFeaturesManager streamFeaturesManager;

    private final StartTlsManager securityManager;

    private final CompressionManager compressionManager;

    private final StreamManager streamManager;

    private final TcpConnectionConfiguration tcpConnectionConfiguration;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * The stream id, which is assigned by the server.
     * guarded by "this"
     */
    String streamId;

    /**
     * guarded by "this"
     */
    private Socket socket;

    /**
     * guarded by "this"
     */
    private XmppStreamWriter xmppStreamWriter;

    /**
     * guarded by "this"
     */
    private XmppStreamReader xmppStreamReader;

    /**
     * guarded by "this"
     */
    private InputStream inputStream;

    /**
     * guarded by "this"
     */
    private OutputStream outputStream;

    TcpConnection(XmppSession xmppSession, TcpConnectionConfiguration configuration) {
        super(xmppSession, configuration);
        this.tcpConnectionConfiguration = configuration;
        this.streamFeaturesManager = xmppSession.getManager(StreamFeaturesManager.class);
        this.streamManager = xmppSession.getManager(StreamManager.class);
        this.securityManager = new StartTlsManager(xmppSession, () -> {
            try {
                secureConnection();
                logger.log(Level.FINE, "Connection has been secured via TLS.");
            } catch (Exception e) {
                throw new StreamNegotiationException(e);
            }
        }, tcpConnectionConfiguration.isSecure());
        this.compressionManager = xmppSession.getManager(CompressionManager.class);
        compressionManager.getConfiguredCompressionMethods().addAll(tcpConnectionConfiguration.getCompressionMethods());
        compressionManager.addFeatureListener(() -> {
            CompressionMethod compressionMethod = compressionManager.getNegotiatedCompressionMethod();
            // We are in the reader thread here. Make sure it sees the streams assigned by the application thread in the connect() method by using synchronized.
            // The following might look overly verbose, but it follows the rule to "never call an alien method from within a synchronized region".
            InputStream iStream;
            OutputStream oStream;
            synchronized (TcpConnection.this) {
                iStream = inputStream;
                oStream = outputStream;
            }
            try {
                iStream = compressionMethod.decompress(iStream);
                oStream = compressionMethod.compress(oStream);
                synchronized (TcpConnection.this) {
                    inputStream = iStream;
                    outputStream = oStream;
                }
            } catch (IOException e) {
                // If compression processing fails after the new (compressed) stream has been established, the entity that detects the error SHOULD generate a stream error and close the stream
                xmppSession.send(new StreamError(Condition.UNDEFINED_CONDITION, new StreamCompression.Failure(StreamCompression.Failure.Condition.PROCESSING_FAILED)));
                try {
                    xmppSession.close();
                } catch (XmppException e1) {
                    xmppSession.notifyException(e1);
                }
                throw new StreamNegotiationException(e);
            }
        });
    }

    /**
     * Connects to the specified XMPP server using a socket connection.
     * Stream features are negotiated until SASL negotiation, which will be negotiated separately in the {@link XmppClient#login(String, String)} method.
     * <p>If only a XMPP service domain has been specified, it is tried to resolve the FQDN via SRV lookup.<br>
     * If that fails, it is tried to connect directly the XMPP service domain on port 5222.<br>
     * If a hostname and port have been specified, these are used to establish the connection.<br>
     * If a proxy has been specified, the connection is established through this proxy.<br>
     * </p>
     *
     * @param from      The optional 'from' attribute in the stream header.
     * @param namespace The content namespace, e.g. "jabber:client".
     * @throws IOException If the underlying socket throws an exception.
     */
    @Override
    public final synchronized void connect(Jid from, String namespace) throws IOException {

        if (socket != null) {
            if (!socket.isClosed() && socket.isConnected()) {
                // Already connected.
                return;
            }

            try {
                close();
            } catch (final Exception e) {
                // ignored
            }
        }

        if (getHostname() != null && !getHostname().isEmpty()) {
            this.socket = createAndConnectSocket(InetSocketAddress.createUnresolved(getHostname(), getPort()), getProxy());
        } else if (xmppSession.getDomain() != null) {
            if (!connectWithXmppServiceDomain(xmppSession.getDomain())) {
                // 9. If the initiating entity does not receive a response to its SRV query, it SHOULD attempt the fallback process described in the next section.
                this.socket = createAndConnectSocket(InetSocketAddress.createUnresolved(xmppSession.getDomain().toString(), getPort()), getProxy());
            }
        } else {
            throw new IllegalStateException("Neither 'xmppServiceDomain' nor 'host' is set.");
        }

        this.from = from;
        streamFeaturesManager.addFeatureNegotiator(securityManager);
        streamFeaturesManager.addFeatureNegotiator(compressionManager);
        streamFeaturesManager.addFeatureNegotiator(streamManager);
        streamManager.reset();

        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = new BufferedInputStream(socket.getInputStream());
        // Start writing to the output stream.
        xmppStreamWriter = new XmppStreamWriter(namespace, streamManager, this.xmppSession);
        xmppStreamWriter.initialize(tcpConnectionConfiguration.getKeepAliveInterval());
        xmppStreamWriter.openStream(outputStream, from);

        // Start reading from the input stream.
        xmppStreamReader = new XmppStreamReader(namespace, this, this.xmppSession);
        xmppStreamReader.startReading(inputStream);
        closed.set(false);
    }

    @Override
    public synchronized boolean isSecure() {
        return socket instanceof SSLSocket;
    }

    private Socket createAndConnectSocket(InetSocketAddress unresolvedAddress, Proxy proxy) throws IOException {
        final Socket socket;
        if (tcpConnectionConfiguration.getSocketFactory() == null) {
            if (proxy != null) {
                socket = new Socket(proxy);
            } else {
                socket = new Socket();
            }
        } else {
            socket = tcpConnectionConfiguration.getSocketFactory().createSocket();
        }
        // SocketFactory may return an already connected socket, so check the connected state to prevent SocketException.
        if (!socket.isConnected()) {
            socket.connect(new InetSocketAddress(unresolvedAddress.getHostName(), unresolvedAddress.getPort()), tcpConnectionConfiguration.getConnectTimeout());
        }
        this.port = unresolvedAddress.getPort();
        this.hostname = unresolvedAddress.getHostName();
        return socket;
    }

    /**
     * This method is called from the reader thread. Because it accesses shared data (socket, outputStream, inputStream) it should be synchronized.
     */
    private void secureConnection() throws IOException, CertificateException, NoSuchAlgorithmException {

        SSLContext sslContext = tcpConnectionConfiguration.getSSLContext();
        if (sslContext == null) {
            sslContext = SSLContext.getDefault();
        }
        SSLSocket sslSocket;

        // synchronize socket because it's also used by the isSecure() method.
        synchronized (this) {
            socket = sslContext.getSocketFactory().createSocket(
                    socket,
                    xmppSession.getDomain().toString(),
                    socket.getPort(),
                    true);
            sslSocket = (SSLSocket) socket;
        }

        HostnameVerifier verifier = tcpConnectionConfiguration.getHostnameVerifier();

        // See
        // http://op-co.de/blog/posts/java_sslsocket_mitm/
        // http://tersesystems.com/2014/03/23/fixing-hostname-verification/

        // If no hostname verifier has been set, use the default one, which is used by HTTPS, too.
        if (verifier == null) {
            SSLParameters sslParameters = sslSocket.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            sslSocket.setSSLParameters(sslParameters);
        } else {
            sslSocket.startHandshake();
            // We are calling an "alien" method here, i.e. code we don't control.
            // Don't call alien methods from within synchronized regions, that's why the regions are split.
            if (!verifier.verify(xmppSession.getDomain().toString(), sslSocket.getSession())) {
                throw new CertificateException("Server failed to authenticate as " + xmppSession.getDomain());
            }
        }

        synchronized (this) {
            outputStream = new BufferedOutputStream(socket.getOutputStream());
            // http://java-performance.info/java-io-bufferedinputstream-and-java-util-zip-gzipinputstream/
            inputStream = new BufferedInputStream(socket.getInputStream(), 65536);
        }
    }

    @Override
    public final synchronized CompletableFuture<Void> send(StreamElement element) {
        return xmppStreamWriter.send(element).thenRun(() -> {
            if (element instanceof Stanza && streamManager.isActive() && streamManager.getRequestStrategy().test((Stanza) element)) {
                send(StreamManagement.REQUEST);
            }
        });
    }

    @Override
    protected final synchronized void restartStream() {
        xmppStreamWriter.openStream(outputStream, from);
        xmppStreamReader.startReading(inputStream);
    }

    /**
     * Closes the TCP connection.
     * It first sends a {@code </stream:stream>}, then shuts down the writer so that no more stanzas can be sent.
     * After that it shuts down the reader and awaits shortly for any stanzas from the server and the server gracefully closing the stream with {@code </stream:stream>}.
     * Eventually the socket is closed.
     */
    @Override
    public final synchronized CompletableFuture<Void> closeAsync() {
        if (closed.compareAndSet(false, true)) {

            final XmppStreamWriter writer;
            final XmppStreamReader reader;

            synchronized (this) {
                writer = xmppStreamWriter;
                reader = xmppStreamReader;
            }
            final CompletableFuture<Void> writeFuture;
            if (writer != null) {
                writeFuture = writer.shutdown();
            } else {
                writeFuture = CompletableFuture.completedFuture(null);
            }
            // This call closes the stream and waits until everything has been sent to the server.
            return writeFuture.handle((aVoid, throwable) -> {
                // This call shuts down the reader and waits for a </stream> response from the server, if it hasn't already shut down before by the server.
                if (reader != null) {
                    return reader.shutdown();
                }
                return CompletableFuture.<Void>completedFuture(null);
            }).thenCompose(Function.identity())
                    .whenComplete((aVoid, throwable) -> {
                        streamFeaturesManager.removeFeatureNegotiator(securityManager);
                        streamFeaturesManager.removeFeatureNegotiator(compressionManager);
                        streamFeaturesManager.removeFeatureNegotiator(streamManager);
                        synchronized (this) {
                            inputStream = null;
                            outputStream = null;
                            streamId = null;

                            // We have sent a </stream:stream> to close the stream and waited for a server response, which also closes the stream by sending </stream:stream>.
                            // Now close the socket.
                            if (socket != null) {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                } finally {
                                    socket = null;
                                }
                            }
                        }
                    });
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * This is the preferred way to resolve the FQDN.
     *
     * @param xmppServiceDomain The fully qualified domain name.
     * @return If the connection could be established.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">3.2.1.  Preferred Process: SRV Lookup</a>
     */
    private boolean connectWithXmppServiceDomain(final Jid xmppServiceDomain) {

        // 1. The initiating entity constructs a DNS SRV query whose inputs are:
        //
        //   * Service of "xmpp-client" (for client-to-server connections) or "xmpp-server" (for server-to-server connections)
        try {
            final List<SrvRecord> srvRecords = DnsResolver.resolveSRV("xmpp-client", xmppServiceDomain, xmppSession.getConfiguration().getNameServer(), tcpConnectionConfiguration.getConnectTimeout());

            // 3. If a response is received, it will contain one or more combinations of a port and FDQN, each of which is weighted and prioritized as described in [DNS-SRV].
            // Sort the entries, so that the best one is tried first.
            srvRecords.sort(null);
            IOException ex = null;
            for (SrvRecord srvRecord : srvRecords) {
                if (srvRecord != null) {
                    // (However, if the result of the SRV lookup is a single resource record with a Target of ".", i.e., the root domain, then the initiating entity MUST abort SRV processing at this point because according to [DNS-SRV] such a Target "means that the service is decidedly not available at this domain".)
                    if (".".equals(srvRecord.getTarget())) {
                        return false;
                    }

                    try {
                        // 4. The initiating entity chooses at least one of the returned FQDNs to resolve (following the rules in [DNS-SRV]), which it does by performing DNS "A" or "AAAA" lookups on the FDQN; this will result in an IPv4 or IPv6 address.
                        // 5. The initiating entity uses the IP address(es) from the successfully resolved FDQN (with the corresponding port number returned by the SRV lookup) as the connection address for the receiving entity.
                        // 6. If the initiating entity fails to connect using that IP address but the "A" or "AAAA" lookups returned more than one IP address, then the initiating entity uses the next resolved IP address for that FDQN as the connection address.
                        this.socket = createAndConnectSocket(InetSocketAddress.createUnresolved(srvRecord.getTarget(), srvRecord.getPort()), getProxy());
                        return true;
                    } catch (IOException e) {
                        // 7. If the initiating entity fails to connect using all resolved IP addresses for a given FDQN, then it repeats the process of resolution and connection for the next FQDN returned by the SRV lookup based on the priority and weight as defined in [DNS-SRV].
                        ex = e;
                    }
                }
            }

            // 8. If the initiating entity receives a response to its SRV query but it is not able to establish an XMPP connection using the data received in the response, it SHOULD NOT attempt the fallback process described in the next section (this helps to prevent a state mismatch between inbound and outbound connections).
            if (!srvRecords.isEmpty()) {
                throw new IOException("Could not connect to any host.", ex);
            }
        } catch (Exception e) {
            // Unable to resolve the domain, try fallback.
            return false;
        }
        return false;
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
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder("TCP connection");
        if (hostname != null) {
            sb.append(" to ").append(hostname).append(':').append(port);
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
