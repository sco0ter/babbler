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

import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import javax.net.SocketFactory;

import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;

/**
 * A configuration for a TCP connection using {@link Socket}.
 *
 * <p>It allows you to configure various connection settings for a TCP socket connection, most importantly the host
 * address and port, but also a whitespace keep-alive interval, a custom socket factory, a custom SSL context and
 * compression methods.</p>
 *
 * <h3>Usage</h3>
 *
 * <p>In order to create an instance of this class you have to use the builder pattern as shown below.</p>
 *
 * <pre>{@code
 * SocketConnectionConfiguration socketConfiguration = SocketConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(5222)
 *     .sslContext(sslContext)
 *     .channelEncryption(ChannelEncryption.DISABLED)
 *     .build();
 * }</pre>
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 * @see rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration
 * @see SocketConnection
 * @deprecated Use {@link TcpConnectionConfiguration.Builder#connector(TransportConnector)} with {@link
 * SocketConnector}.
 */
@Deprecated(forRemoval = true)
public final class SocketConnectionConfiguration extends ClientConnectionConfiguration {

    private static volatile SocketConnectionConfiguration defaultConfiguration;

    private final SocketFactory socketFactory;

    private final int keepAliveInterval;

    private SocketConnectionConfiguration(Builder builder) {
        super(builder);
        this.keepAliveInterval = builder.keepAliveInterval;
        this.socketFactory = builder.socketFactory;
    }

    /**
     * Gets the default configuration.
     *
     * @return The default configuration.
     */
    public static SocketConnectionConfiguration getDefault() {
        // Use double-checked locking idiom
        if (defaultConfiguration == null) {
            synchronized (SocketConnectionConfiguration.class) {
                if (defaultConfiguration == null) {
                    defaultConfiguration = builder().build();
                }
            }
        }
        return defaultConfiguration;
    }

    /**
     * Sets the default configuration.
     *
     * @param configuration The default configuration.
     */
    public static void setDefault(SocketConnectionConfiguration configuration) {
        synchronized (SocketConnectionConfiguration.class) {
            defaultConfiguration = configuration;
        }
    }

    /**
     * Creates a new builder for this class.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public final CompletableFuture<Connection> createConnection(XmppSession xmppSession, SessionOpen sessionOpen) {
        return new SocketConnector().createConnection(xmppSession,
                TcpConnectionConfiguration.builder().keepAliveInterval(keepAliveInterval).build(),
                (socket, config) -> new SocketConnection(socket, xmppSession, config), sessionOpen);
    }

    /**
     * Gets the socket factory.
     *
     * @return The socket factory.
     */
    public final SocketFactory getSocketFactory() {
        return socketFactory;
    }

    /**
     * A builder to create a {@link TcpConnectionConfiguration} instance.
     */
    public static final class Builder
            extends ClientConnectionConfiguration.Builder<Builder, TcpConnectionConfiguration> {

        private SocketFactory socketFactory;

        private int keepAliveInterval;

        private Builder() {
            // default values.
            channelEncryption(ChannelEncryption.OPTIONAL);
            port(5222);
            keepAliveInterval(30);
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

        /**
         * Sets a socket factory which creates the socket. This can be useful if you want connect to the legacy SSL port
         * (usually 5223) and the connection is encrypted right from the beginning.
         *
         * <p>However, usually, there's no need to set a custom socket factory.</p>
         *
         * @param socketFactory The socket factory.
         * @return The builder.
         * @see #channelEncryption(ChannelEncryption)
         */
        public Builder socketFactory(SocketFactory socketFactory) {
            this.socketFactory = socketFactory;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public SocketConnectionConfiguration build() {
            return new SocketConnectionConfiguration(this);
        }
    }
}
