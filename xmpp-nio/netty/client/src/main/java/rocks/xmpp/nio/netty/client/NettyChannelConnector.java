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

package rocks.xmpp.nio.netty.client;

import java.net.Proxy;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.client.AbstractTcpConnector;
import rocks.xmpp.core.net.client.SocketConnector;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.net.client.TransportConnector;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.nio.netty.net.NettyChannelConnection;

/**
 * A TCP transport connector which uses the {@link Channel}.
 *
 * <p>Unlike {@link SocketConnector}, this connector is NIO based and therefore does not use blocking IO and
 * therefore less threads.</p>
 *
 * <h3>Sample Usage</h3>
 *
 * <pre>{@code
 * TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(5222)
 *     .sslContext(sslContext)
 *     .channelEncryption(ChannelEncryption.REQUIRED)
 *     .connector(new NettyChannelConnector())
 *     .build();
 * }</pre>
 *
 * @see SocketConnector
 * @see TcpConnectionConfiguration.Builder#connector(TransportConnector)
 */
public final class NettyChannelConnector extends AbstractTcpConnector<Channel> {

    private final EventLoopGroup eventLoopGroup;

    /**
     * Creates a connector using a default {@link NioEventLoopGroup}.
     */
    public NettyChannelConnector() {
        this(new NioEventLoopGroup());
    }

    /**
     * Creates a connector using a given {@link NioEventLoopGroup}.
     *
     * @param eventLoopGroup The Netty event loop group.
     */
    public NettyChannelConnector(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = Objects.requireNonNull(eventLoopGroup);
    }

    @Override
    protected final CompletableFuture<Channel> connect(final String hostname, final int port,
                                                       final TcpConnectionConfiguration configuration) {
        final Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, configuration.getConnectTimeout());
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public final void initChannel(final SocketChannel ch) {
                Proxy proxy = configuration.getProxy();
                if (proxy != null) {
                    if (proxy.type() == Proxy.Type.SOCKS) {
                        ch.pipeline().addFirst(new Socks5ProxyHandler(configuration.getProxy().address()));
                    } else if (proxy.type() == Proxy.Type.HTTP) {
                        ch.pipeline().addFirst(new HttpProxyHandler(configuration.getProxy().address()));
                    }
                }
            }
        });
        ChannelFuture channelFuture = b.connect(hostname, port);
        return NettyChannelConnection.completableFutureFromNettyFuture(channelFuture)
                .thenApply(aVoid -> channelFuture.channel());
    }

    @Override
    public final CompletableFuture<Connection> connect(final XmppSession xmppSession,
                                                       final TcpConnectionConfiguration configuration,
                                                       final SessionOpen sessionOpen) {
        return createConnection(xmppSession, configuration,
                (channel, config) -> new NettyChannelClientConnection(channel, xmppSession, config), sessionOpen);
    }
}
