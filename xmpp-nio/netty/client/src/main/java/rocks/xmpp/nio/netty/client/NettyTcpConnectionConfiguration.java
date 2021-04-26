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

import java.util.concurrent.CompletableFuture;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.client.ClientConnectionConfiguration;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.net.client.TransportConnector;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;

/**
 * @author Christian Schudt
 * @deprecated Use {@link TcpConnectionConfiguration.Builder#connector(TransportConnector)} with {@link
 * NettyChannelConnector}.
 */
@Deprecated(forRemoval = true)
public final class NettyTcpConnectionConfiguration extends ClientConnectionConfiguration {

    private final EventLoopGroup eventLoopGroup;

    private final int keepAliveInterval;

    private NettyTcpConnectionConfiguration(final NettyTcpConnectionConfiguration.Builder builder) {
        super(builder);
        this.keepAliveInterval = builder.keepAliveInterval;
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
    public final CompletableFuture<Connection> createConnection(final XmppSession xmppSession, SessionOpen sessionOpen) {
        return new NettyChannelConnector(eventLoopGroup).connect(xmppSession,
                TcpConnectionConfiguration.builder().keepAliveInterval(keepAliveInterval).build(), sessionOpen);
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
    public static final class Builder
            extends
            ClientConnectionConfiguration.Builder<NettyTcpConnectionConfiguration.Builder, TcpConnectionConfiguration> {

        private EventLoopGroup eventLoopGroup;

        private int keepAliveInterval;

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

        public final Builder keepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
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
