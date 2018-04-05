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
import rocks.xmpp.core.net.client.ClientConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.net.ChannelEncryption;

import java.net.Proxy;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

/**
 * @author Christian Schudt
 */
public final class NettyTcpConnectionConfiguration extends ClientConnectionConfiguration {

    private final EventLoopGroup eventLoopGroup;

    private NettyTcpConnectionConfiguration(final NettyTcpConnectionConfiguration.Builder builder) {
        super(builder);
        this.eventLoopGroup = builder.eventLoopGroup != null ? builder.eventLoopGroup : new NioEventLoopGroup();
    }

    /**
     * Creates a new builder for this class.
     *
     * @return The builder.
     */
    public static NettyTcpConnectionConfiguration.Builder builder() {
        return new NettyTcpConnectionConfiguration.Builder();
    }

    @Override
    public final Connection createConnection(final XmppSession xmppSession) {
        try {
            final Bootstrap b = new Bootstrap();
            b.group(getEventLoopGroup());
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public final void initChannel(final SocketChannel ch) {
                    Proxy proxy = getProxy();
                    if (proxy != null) {
                        if (proxy.type() == Proxy.Type.SOCKS) {
                            ch.pipeline().addFirst(new Socks5ProxyHandler(getProxy().address()));
                        } else if (proxy.type() == Proxy.Type.HTTP) {
                            ch.pipeline().addFirst(new HttpProxyHandler(getProxy().address()));
                        }
                    }
                }
            });
            ChannelFuture channelFuture = b.connect(getHostname(), getPort());
            channelFuture.get();
            NettyTcpConnection nettyTcpConnection = new NettyTcpConnection(channelFuture.channel(),
                    xmppSession, this);
            if (getChannelEncryption() == ChannelEncryption.DIRECT) {
                try {
                    nettyTcpConnection.secureConnection();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
            return nettyTcpConnection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * Gets the event loop group.
     *
     * @return The event loop group.
     */
    public final EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public final String toString() {
        return "Netty connection configuration: " + super.toString();
    }

    /**
     * A builder to create a {@link NettyTcpConnectionConfiguration} instance.
     */
    public static final class Builder extends ClientConnectionConfiguration.Builder<NettyTcpConnectionConfiguration.Builder> {

        private EventLoopGroup eventLoopGroup;

        private Builder() {
            // default values.
            channelEncryption(ChannelEncryption.OPTIONAL);
            port(5222);
        }

        /**
         * Sets the NIO event loop.
         *
         * @param eventLoopGroup The loop.
         * @return The builder.
         */
        public Builder eventLoopGroup(final EventLoopGroup eventLoopGroup) {
            this.eventLoopGroup = eventLoopGroup;
            return this;
        }

        @Override
        protected final NettyTcpConnectionConfiguration.Builder self() {
            return this;
        }

        @Override
        public final NettyTcpConnectionConfiguration build() {
            return new NettyTcpConnectionConfiguration(this);
        }
    }
}
