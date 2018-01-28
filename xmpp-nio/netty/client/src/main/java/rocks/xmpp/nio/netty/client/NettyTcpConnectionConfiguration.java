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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import rocks.xmpp.core.session.Connection;
import rocks.xmpp.core.session.ConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;

/**
 * @author Christian Schudt
 */
public final class NettyTcpConnectionConfiguration extends ConnectionConfiguration {

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
        return new NettyTcpConnection(xmppSession, this);
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
    public static final class Builder extends ConnectionConfiguration.Builder<NettyTcpConnectionConfiguration.Builder> {

        private EventLoopGroup eventLoopGroup;

        private Builder() {
            // default values.
            secure(true);
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
