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

import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.net.AbstractConnection;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.core.tls.client.StartTlsManager;
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
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
public final class TcpConnection extends AbstractConnection {

    private static final Logger logger = Logger.getLogger(TcpConnection.class.getName());

    private final StreamFeaturesManager streamFeaturesManager;

    private final StartTlsManager securityManager;

    private final CompressionManager compressionManager;

    private final StreamManager streamManager;

    private final TcpConnectionConfiguration tcpConnectionConfiguration;

    private final XmppSession xmppSession;

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

    private SessionOpen sessionOpen;

    TcpConnection(final Socket socket, final XmppSession xmppSession, final TcpConnectionConfiguration configuration) throws IOException {
        super(configuration);
        this.socket = socket;
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());
        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.xmppSession = xmppSession;
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
        streamFeaturesManager.addFeatureNegotiator(securityManager);
        streamFeaturesManager.addFeatureNegotiator(compressionManager);
        streamFeaturesManager.addFeatureNegotiator(streamManager);
        streamManager.reset();
    }

    @Override
    public final CompletionStage<Void> open(final SessionOpen sessionOpen) {

        StreamHeader streamHeader = (StreamHeader) sessionOpen;
        this.sessionOpen = sessionOpen;

        // Start reading from the input stream.
        xmppStreamReader = new XmppStreamReader(streamHeader.getContentNamespace(), this, this.xmppSession);
        xmppStreamReader.startReading(inputStream, this::openedByPeer, this::closedByPeer);

        // Start writing to the output stream.
        xmppStreamWriter = new XmppStreamWriter(streamHeader.getContentNamespace(), streamManager, this.xmppSession);
        xmppStreamWriter.initialize(tcpConnectionConfiguration.getKeepAliveInterval());
        return xmppStreamWriter.openStream(outputStream, streamHeader);
    }

    @Override
    public synchronized boolean isSecure() {
        return socket instanceof SSLSocket;
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
        final boolean requestStanzaCount = element instanceof Stanza && streamManager.isActive() && streamManager.getRequestStrategy().test((Stanza) element);
        // If the stanza count will be request immediately after, don't flush now, but later.
        CompletableFuture<Void> future = xmppStreamWriter.write(element, !requestStanzaCount);
        if (requestStanzaCount) {
            return future.thenRun(() -> xmppStreamWriter.write(StreamManagement.REQUEST, true));
        }
        return future;
    }

    @Override
    public final CompletableFuture<Void> write(final StreamElement streamElement) {
        return xmppStreamWriter.write(streamElement, false);
    }

    @Override
    public final void flush() {
        xmppStreamWriter.flush();
    }

    @Override
    protected final synchronized void restartStream() {
        xmppStreamWriter.openStream(outputStream, (StreamHeader) sessionOpen);
        xmppStreamReader.startReading(inputStream, this::openedByPeer, this::closedByPeer);
    }

    @Override
    protected CompletionStage<Void> closeStream() {

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
        return writeFuture.whenCompleteAsync((aVoid, throwable) -> {
            // This call shuts down the reader and waits for a </stream> response from the server, if it hasn't already shut down before by the server.
            if (reader != null) {
                reader.shutdown();
            }
        });
    }

    @Override
    protected CompletionStage<Void> closeConnection() {

        streamFeaturesManager.removeFeatureNegotiator(securityManager);
        streamFeaturesManager.removeFeatureNegotiator(compressionManager);
        streamFeaturesManager.removeFeatureNegotiator(streamManager);
        synchronized (this) {
            inputStream = null;
            outputStream = null;

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
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public final boolean isUsingAcknowledgements() {
        return streamManager.isActive();
    }

    @Override
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder("TCP connection");
        if (socket != null) {
            sb.append(" to ").append(socket.getInetAddress()).append(':').append(socket.getPort());
        }
        if (getStreamId() != null) {
            sb.append(" (").append(getStreamId()).append(')');
        }
        return sb.toString();
    }
}
