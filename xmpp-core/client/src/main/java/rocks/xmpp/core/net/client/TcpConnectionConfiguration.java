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

package rocks.xmpp.core.net.client;

import java.util.concurrent.CompletableFuture;

import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.session.XmppSession;

/**
 * A configuration for a TCP connection.
 *
 * @author Christian Schudt
 * @see rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration
 * @see SocketConnection
 */
public final class TcpConnectionConfiguration extends ClientConnectionConfiguration {

    private final int keepAliveInterval;

    private final TransportConnector<TcpConnectionConfiguration> connector;

    protected TcpConnectionConfiguration(Builder builder) {
        super(builder);
        this.keepAliveInterval = builder.keepAliveInterval;
        this.connector = builder.getConnector();
    }

    /**
     * Creates a new builder for this class.
     *
     * @return The builder.
     */
    public static TcpConnectionConfiguration.Builder builder() {
        return new TcpConnectionConfiguration.Builder();
    }

    /**
     * Gets the whitespace keep-alive interval.
     *
     * @return The whitespace keep-alive interval.
     */
    public final int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    @Override
    public CompletableFuture<Connection> createConnection(XmppSession xmppSession) throws Exception {
        if (connector == null) {
            return new SocketConnector().connect(xmppSession, this);
        }
        return connector.connect(xmppSession, this);
    }

    /**
     * A builder to create a {@link TcpConnectionConfiguration} instance.
     */
    public static class Builder extends ClientConnectionConfiguration.Builder<Builder, TcpConnectionConfiguration> {

        private int keepAliveInterval;

        protected Builder() {
            // default values.
            channelEncryption(ChannelEncryption.OPTIONAL);
            port(5222);
            keepAliveInterval(30);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public TcpConnectionConfiguration build() {
            return new TcpConnectionConfiguration(this);
        }

        /**
         * Sets the whitespace keep-alive interval in seconds. If the interval is negative, no whitespace will be sent
         * at all.
         *
         * @param keepAliveInterval The whitespace keep-alive interval.
         * @return The builder.
         */
        public final Builder keepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }
    }
}
