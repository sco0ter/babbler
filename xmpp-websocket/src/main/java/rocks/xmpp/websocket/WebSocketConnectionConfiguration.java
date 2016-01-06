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

package rocks.xmpp.websocket;

import rocks.xmpp.core.session.Connection;
import rocks.xmpp.core.session.ConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;

import java.net.Proxy;

/**
 * A configuration for a WebSocket connection.
 * It allows you to configure basic connection settings like hostname and port, as well as the path in the WebSocket URI.
 * <h3>Usage</h3>
 * In order to create an instance of this class you have to use the builder pattern as shown below.
 * <pre>
 * {@code
 * WebSocketConnectionConfiguration connectionConfiguration = WebSocketConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(7443)
 *     .file("/ws/")
 *     .sslContext(sslContext)
 *     .secure(true)
 *     .build();
 * }
 * </pre>
 * The above sample configuration will connect to <code>wss://localhost:7443/ws/</code> using SSL with a custom SSLContext.
 * <p>
 * This class is immutable.
 *
 * @see WebSocketConnection
 */
public final class WebSocketConnectionConfiguration extends ConnectionConfiguration {
    private static volatile WebSocketConnectionConfiguration defaultConfiguration;

    private final String path;

    protected WebSocketConnectionConfiguration(Builder builder) {
        super(builder);
        this.path = builder.path;
    }

    /**
     * Gets the default configuration.
     *
     * @return The default configuration.
     */
    public static WebSocketConnectionConfiguration getDefault() {
        // Use double-checked locking idiom
        if (defaultConfiguration == null) {
            synchronized (WebSocketConnectionConfiguration.class) {
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
    public static void setDefault(WebSocketConnectionConfiguration configuration) {
        synchronized (WebSocketConnectionConfiguration.class) {
            defaultConfiguration = configuration;
        }
    }

    /**
     * Creates a new builder.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the path on the host, e.g. "/ws/".
     *
     * @return The path on the host.
     */
    public final String getPath() {
        return path;
    }

    @Override
    public final Connection createConnection(XmppSession xmppSession) {
        WebSocketConnection connection = new WebSocketConnection(xmppSession, this);
        connection.initialize();
        return connection;
    }

    /**
     * A builder to create a {@link WebSocketConnectionConfiguration} instance.
     */
    public static final class Builder extends ConnectionConfiguration.Builder<Builder> {

        private String path;

        private Builder() {
            // default values
            path("/ws/");
        }

        /**
         * Sets the path on the host, e.g. "/ws/"
         *
         * @param path The path on the host.
         * @return The builder.
         */
        public final Builder path(String path) {
            this.path = path;
            return this;
        }

        @Override
        protected final Builder self() {
            return this;
        }

        @Override
        public final WebSocketConnectionConfiguration build() {
            if (proxy != null && proxy.type() != Proxy.Type.HTTP && proxy.type() != Proxy.Type.DIRECT) {
                throw new UnsupportedOperationException("Non-HTTP proxies are not supported by WebSockets.");
            }
            return new WebSocketConnectionConfiguration(this);
        }
    }
}
