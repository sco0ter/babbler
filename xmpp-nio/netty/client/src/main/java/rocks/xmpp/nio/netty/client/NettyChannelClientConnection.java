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

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.core.tls.client.StartTlsManager;
import rocks.xmpp.extensions.compress.CompressionManager;
import rocks.xmpp.extensions.sm.client.ClientStreamManager;
import rocks.xmpp.nio.netty.net.NettyChannelConnection;

/**
 * Client initiated TCP connection based on Netty channels.
 */
final class NettyChannelClientConnection extends NettyChannelConnection {

    private static final System.Logger logger = System.getLogger(NettyChannelClientConnection.class.getName());

    private final StreamFeaturesManager streamFeaturesManager;

    private final ClientStreamManager streamManager;

    private final StartTlsManager startTlsManager;

    private final CompressionManager compressionManager;

    private final TcpConnectionConfiguration connectionConfiguration;

    private final XmppSession xmppSession;

    /**
     * Creates a connection to the specified host and port through a proxy.
     *
     * @param xmppSession             The XMPP session.
     * @param connectionConfiguration The connection configuration.
     */
    NettyChannelClientConnection(final Channel channel, final XmppSession xmppSession,
                                 final TcpConnectionConfiguration connectionConfiguration) {
        super(channel, xmppSession, xmppSession,
                xmppSession.getReaderInterceptors(),
                xmppSession::createUnmarshaller,
                xmppSession.getWriterInterceptors(),
                xmppSession::createMarshaller,
                xmppSession::notifyException,
                connectionConfiguration);
        this.xmppSession = xmppSession;
        this.connectionConfiguration = connectionConfiguration;

        Duration keepAliveInterval = connectionConfiguration.getKeepAliveInterval();

        if (keepAliveInterval != null && !keepAliveInterval.isNegative() && !keepAliveInterval.isZero()) {
            channel.pipeline().addLast("idleStateHandler",
                    new IdleStateHandler(0, keepAliveInterval.toSeconds(), 0, TimeUnit.SECONDS));
            channel.pipeline().addLast("idleStateEventHandler", new ChannelDuplexHandler() {

                @Override
                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                    if (evt instanceof IdleStateEvent) {
                        ctx.writeAndFlush(' ');
                    }
                }

            });
        }

        closeFuture().whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                xmppSession.notifyException(throwable);
            } else if (!isClosed()) {
                // If the server closed the connection, initiate a reconnection.
                xmppSession.notifyException(new StreamErrorException(
                        new StreamError(Condition.UNDEFINED_CONDITION, "Stream closed by server", Locale.ENGLISH,
                                null)));
            }
        });

        this.streamManager = xmppSession.getManager(ClientStreamManager.class);
        this.startTlsManager = new StartTlsManager(xmppSession, this, connectionConfiguration.getChannelEncryption());

        this.compressionManager = new CompressionManager(xmppSession, this);
        this.compressionManager.getConfiguredCompressionMethods().clear();
        this.compressionManager.getConfiguredCompressionMethods()
                .addAll(connectionConfiguration.getCompressionMethods());

        this.streamFeaturesManager = xmppSession.getManager(StreamFeaturesManager.class);
        this.streamFeaturesManager.addFeatureNegotiator(startTlsManager);
        this.streamFeaturesManager.addFeatureNegotiator(compressionManager);
    }

    @Override
    protected final void restartStream() {
        super.restartStream();
        open(sessionOpen);
    }

    @Override
    public final void secureConnection() throws NoSuchAlgorithmException {
        final SSLContext sslContext = getConfiguration().getSSLContext() != null ? getConfiguration().getSSLContext()
                : SSLContext.getDefault();
        final SslContext sslCtx = new JdkSslContext(sslContext, true, ClientAuth.OPTIONAL);
        final SslHandler handler = sslCtx.newHandler(channel.alloc(), String.valueOf(xmppSession.getDomain()),
                connectionConfiguration.getPort());
        final HostnameVerifier verifier = connectionConfiguration.getHostnameVerifier();
        final SSLEngine sslEngine = handler.engine();

        if (verifier == null) {
            final SSLParameters sslParameters = sslEngine.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            sslEngine.setSSLParameters(sslParameters);
        }

        final Future<Channel> handshakeFuture = handler.handshakeFuture();
        handshakeFuture.addListener(future -> {
            if (future.isSuccess()) {
                if (verifier != null && !verifier.verify(xmppSession.getDomain().toString(), sslEngine.getSession())) {
                    xmppSession.notifyException(
                            new CertificateException("Server failed to authenticate as " + xmppSession.getDomain()));
                } else {
                    logger.log(System.Logger.Level.DEBUG, "Connection has been secured via TLS.");
                }
            } else {
                xmppSession.notifyException(future.cause());
            }
        });
        channel.pipeline().addFirst("SSL", handler);
    }

    @Override
    public final boolean isUsingAcknowledgements() {
        return streamManager.isActive();
    }

    @Override
    protected final CompletionStage<Void> closeStream() {
        this.streamFeaturesManager.removeFeatureNegotiator(startTlsManager);
        this.streamFeaturesManager.removeFeatureNegotiator(compressionManager);
        return super.closeStream();
    }
}
