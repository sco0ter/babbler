/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.nio.netty.client;

import io.netty.channel.Channel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.net.ConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.tls.client.StartTlsManager;
import rocks.xmpp.extensions.sm.StreamManager;
import rocks.xmpp.nio.netty.NettyChannelConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class NettyTcpConnection extends NettyChannelConnection {

    private static final Logger logger = Logger.getLogger(NettyTcpConnection.class.getName());

    private final StreamManager streamManager;

    private final NettyTcpConnectionConfiguration connectionConfiguration;

    private final XmppSession xmppSession;

    /**
     * Creates a connection to the specified host and port through a proxy.
     *
     * @param xmppSession             The XMPP session.
     * @param connectionConfiguration The connection configuration.
     */
    NettyTcpConnection(final Channel channel, final XmppSession xmppSession, final NettyTcpConnectionConfiguration connectionConfiguration) {
        super(channel, null, xmppSession::createUnmarshaller,
                xmppSession.getDebugger()::writeStanza,
                xmppSession::createMarshaller,
                xmppSession::notifyException);
        this.xmppSession = xmppSession;
        this.connectionConfiguration = connectionConfiguration;
        StreamFeaturesManager streamFeaturesManager = xmppSession.getManager(StreamFeaturesManager.class);
        this.streamManager = xmppSession.getManager(StreamManager.class);
        StartTlsManager startTlsManager = new StartTlsManager(xmppSession, () -> {
            try {
                secureConnection();
            } catch (Exception e) {
                throw new StreamNegotiationException(e);
            }
        }, connectionConfiguration.isSecure());
        streamFeaturesManager.addFeatureNegotiator(startTlsManager);
        streamFeaturesManager.addFeatureNegotiator(streamManager);
    }

    @Override
    protected void onRead(final String xml, final StreamElement streamElement) {
        super.onRead(xml, streamElement);
        if (xmppSession.getDebugger() != null) {
            xmppSession.getDebugger().readStanza(xml, streamElement);
        }
        try {
            if (xmppSession.handleElement(streamElement)) {
                open(sessionOpen);
                restartStream();
            }
        } catch (XmppException e) {
            xmppSession.notifyException(e);
        }
    }

    @Override
    public final ConnectionConfiguration getConfiguration() {
        return connectionConfiguration;
    }

    private synchronized void secureConnection() throws NoSuchAlgorithmException {
        final SSLContext sslContext = getConfiguration().getSSLContext() != null ? getConfiguration().getSSLContext() : SSLContext.getDefault();
        final SslContext sslCtx = new JdkSslContext(sslContext, true, ClientAuth.OPTIONAL);
        final SslHandler handler = sslCtx.newHandler(channel.alloc(), String.valueOf(xmppSession.getDomain()), connectionConfiguration.getPort());
        final HostnameVerifier verifier = connectionConfiguration.getHostnameVerifier();
        final SSLEngine sslEngine = handler.engine();

        if (verifier == null) {
            final SSLParameters sslParameters = sslEngine.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            sslEngine.setSSLParameters(sslParameters);
        } else {
            Future<Channel> handshakeFuture = handler.handshakeFuture();
            handshakeFuture.addListener(future -> {
                if (future.isSuccess()) {
                    if (!verifier.verify(xmppSession.getDomain().toString(), sslEngine.getSession())) {
                        throw new CertificateException("Server failed to authenticate as " + xmppSession.getDomain());
                    }
                    logger.log(Level.FINE, "Connection has been secured via TLS.");
                }
            });
        }
        channel.pipeline().addFirst("SSL", handler);
    }

    @Override
    public final boolean isUsingAcknowledgements() {
        return streamManager.isActive();
    }

    @Override
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder("TCP NIO connection");
        sb.append(" to ").append(connectionConfiguration.getHostname()).append(':').append(connectionConfiguration.getPort());
        if (getStreamId() != null) {
            sb.append(" (").append(getStreamId()).append(')');
        }
        if (sessionOpen != null) {
            sb.append(", from: ").append(sessionOpen.getFrom());
        }
        return sb.toString();
    }
}
