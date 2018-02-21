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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Connection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.tls.client.StartTlsManager;
import rocks.xmpp.extensions.sm.StreamManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;
import rocks.xmpp.nio.netty.NettyChannelConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Proxy;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class NettyTcpConnection extends Connection {

    private static final Logger logger = Logger.getLogger(NettyTcpConnection.class.getName());

    private final StreamFeaturesManager streamFeaturesManager;

    private final StartTlsManager startTlsManager;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final StreamManager streamManager;

    private Channel channel;

    private NettyChannelConnection channelConnection;

    /**
     * Creates a connection to the specified host and port through a proxy.
     *
     * @param xmppSession             The XMPP session.
     * @param connectionConfiguration The connection configuration.
     */
    NettyTcpConnection(final XmppSession xmppSession, final NettyTcpConnectionConfiguration connectionConfiguration) {
        super(xmppSession, connectionConfiguration);

        this.streamFeaturesManager = xmppSession.getManager(StreamFeaturesManager.class);
        this.streamManager = xmppSession.getManager(StreamManager.class);
        this.startTlsManager = new StartTlsManager(xmppSession, () -> {
            try {
                secureConnection();
            } catch (Exception e) {
                throw new StreamNegotiationException(e);
            }
        }, connectionConfiguration.isSecure());
    }

    private void onRead(final String xml, final StreamElement streamElement) {
        if (xmppSession.getDebugger() != null) {
            xmppSession.getDebugger().readStanza(xml, streamElement);
        }
        try {
            if (xmppSession.handleElement(streamElement)) {
                channelConnection.open(sessionOpen);
                channelConnection.restartStream();
            }
        } catch (XmppException e) {
            xmppSession.notifyException(e);
        }
    }

    private synchronized void secureConnection() throws NoSuchAlgorithmException {
        final SSLContext sslContext = getConfiguration().getSSLContext() != null ? getConfiguration().getSSLContext() : SSLContext.getDefault();
        final SslContext sslCtx = new JdkSslContext(sslContext, true, ClientAuth.OPTIONAL);
        final SslHandler handler = sslCtx.newHandler(channel.alloc(), String.valueOf(xmppSession.getDomain()), getPort());
        final HostnameVerifier verifier = getConfiguration().getHostnameVerifier();
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
    protected final void restartStream() {
    }

    @Override
    public final synchronized void connect() throws IOException {
        try {
            final ChannelFuture channelFuture;

            closed.set(false);
            final Bootstrap b = new Bootstrap();
            b.group(((NettyTcpConnectionConfiguration) getConfiguration()).getEventLoopGroup());
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public final void initChannel(final SocketChannel ch) {
                    Proxy proxy = getConfiguration().getProxy();
                    if (proxy != null) {
                        if (proxy.type() == Proxy.Type.SOCKS) {
                            ch.pipeline().addFirst(new Socks5ProxyHandler(getConfiguration().getProxy().address()));
                        } else if (proxy.type() == Proxy.Type.HTTP) {
                            ch.pipeline().addFirst(new HttpProxyHandler(getConfiguration().getProxy().address()));
                        }
                    }
                    channelConnection = new NettyChannelConnection(ch,
                            NettyTcpConnection.this::onRead,
                            xmppSession::createUnmarshaller,
                            xmppSession.getDebugger()::writeStanza,
                            xmppSession::createMarshaller,
                            xmppSession::notifyException);
                }
            });
            channelFuture = b.connect(getHostname(), getPort());
            this.channel = channelFuture.channel();
            channelFuture.get();
            this.streamFeaturesManager.addFeatureNegotiator(startTlsManager);
            this.streamFeaturesManager.addFeatureNegotiator(streamManager);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }

    @Override
    public final CompletionStage<Void> open(final SessionOpen sessionOpen) {
        this.sessionOpen = sessionOpen;
        return channelConnection.open(sessionOpen);
    }

    @Override
    public final CompletableFuture<Void> send(final StreamElement streamElement) {
        final CompletableFuture<Void> future;
        final boolean requestStanzaCount = streamElement instanceof Stanza && streamManager.isActive() && streamManager.getRequestStrategy().test((Stanza) streamElement);
        if (!requestStanzaCount) {
            future = write(streamElement);
        } else {
            future = write(streamElement);
            write(StreamManagement.REQUEST);
        }
        this.flush();
        return future;
    }

    @Override
    public final CompletableFuture<Void> write(final StreamElement streamElement) {
        return channelConnection.write(streamElement).toCompletableFuture();
    }

    @Override
    public final void flush() {
        channelConnection.flush();
    }

    @Override
    public final synchronized boolean isSecure() {
        return channelConnection != null && channelConnection.isSecure();
    }

    @Override
    public final synchronized String getStreamId() {
        return channelConnection.getStreamId();
    }

    @Override
    public final boolean isUsingAcknowledgements() {
        return streamManager.isActive();
    }

    @Override
    public final synchronized CompletableFuture<Void> closeAsync() {
        return channelConnection.closeAsync().toCompletableFuture();
    }

    @Override
    public CompletionStage<Void> closeAsync(StreamError streamError) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder("TCP NIO connection");
        if (hostname != null) {
            sb.append(" to ").append(hostname).append(':').append(port);
        }
        if (channelConnection != null) {
            sb.append(" (").append(channelConnection.getStreamId()).append(')');
        }
        if (sessionOpen != null) {
            sb.append(", from: ").append(sessionOpen.getFrom());
        }
        return sb.toString();
    }
}
