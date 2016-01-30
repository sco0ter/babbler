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

package rocks.xmpp.extensions.httpbind;

import rocks.xmpp.core.session.Connection;
import rocks.xmpp.core.session.ConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;

/**
 * A configuration for a BOSH connection.
 * It allows you to configure basic connection settings like hostname and port, but also BOSH specific settings like the wait interval, a route or the use of a key sequencing mechanism.
 * <h3>Usage</h3>
 * In order to create an instance of this class you have to use the builder pattern as shown below.
 * <pre>
 * {@code
 * BoshConnectionConfiguration boshConnectionConfiguration = BoshConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(5280)
 *     .path("/http-bind/")
 *     .build();
 * }
 * </pre>
 * The above sample configuration will connect to <code>http://localhost:5280/http-bind/</code>.
 * <p>
 * This class is immutable.
 *
 * @see rocks.xmpp.core.session.TcpConnectionConfiguration
 * @see BoshConnection
 */
public final class BoshConnectionConfiguration extends ConnectionConfiguration {

    private static volatile BoshConnectionConfiguration defaultConfiguration;

    private final int wait;

    private final String path;

    private final String route;

    private final boolean useKeySequence;

    private BoshConnectionConfiguration(Builder builder) {
        super(builder);
        this.wait = builder.wait;
        this.path = builder.path;
        this.route = builder.route;
        this.useKeySequence = builder.useKeySequence;
    }

    /**
     * Gets the default configuration.
     *
     * @return The default configuration.
     */
    public static BoshConnectionConfiguration getDefault() {
        // Use double-checked locking idiom
        if (defaultConfiguration == null) {
            synchronized (BoshConnectionConfiguration.class) {
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
    public static void setDefault(BoshConnectionConfiguration configuration) {
        synchronized (BoshConnectionConfiguration.class) {
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

    @Override
    public Connection createConnection(XmppSession xmppSession) {
        return new BoshConnection(xmppSession, this);
    }

    /**
     * Gets the longest time (in seconds) that the connection manager is allowed to wait before responding to any request during the session.
     *
     * @return The wait time.
     */
    public final int getWait() {
        return wait;
    }

    /**
     * Gets the path on the host, e.g. "/http-bind/".
     *
     * @return The path on the host.
     */
    public final String getPath() {
        return path;
    }

    /**
     * Gets the route.
     *
     * @return The route.
     */
    public final String getRoute() {
        return route;
    }

    /**
     * If the connection is secured via a key sequence mechanism.
     *
     * @return If the connection is secured via a key sequence mechanism.
     */
    public final boolean isUseKeySequence() {
        return useKeySequence;
    }

    @Override
    public final String toString() {
        return "BOSH connection configuration: " + (isSecure() ? "https" : "http") + "://" + super.toString() + path;
    }

    /**
     * A builder to create a {@link rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration} instance.
     */
    public static final class Builder extends ConnectionConfiguration.Builder<Builder> {

        private int wait;

        private String path;

        private String route;

        private boolean useKeySequence;

        private Builder() {
            // default values
            wait(60);
            path("/http-bind/");
        }

        /**
         * Sets the path on the host, e.g. "/http-bind/"
         *
         * @param path The path on the host.
         * @return The builder.
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the longest time (in seconds) that the connection manager is allowed to wait before responding to any request during the session.
         *
         * @param wait The time in seconds.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0124.html#session-request">7.1 Session Creation Request</a>
         */
        public Builder wait(int wait) {
            this.wait = wait;
            return this;
        }

        /**
         * Sets the route, formatted as "protocol:host:port" (e.g., "xmpp:example.com:9999").
         *
         * @param route The route.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0124.html#session-request">7.1 Session Creation Request</a>
         */
        public Builder route(String route) {
            this.route = route;
            return this;
        }

        /**
         * Indicates whether a key sequencing mechanism is used to secure a connection.
         * Note that if the connection is already secured via HTTPS, it is not necessary to use this mechanism.
         *
         * @param useKeySequence If a key sequence should be used.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0124.html#keys">15. Protecting Insecure Sessions</a>
         */
        public Builder useKeySequence(boolean useKeySequence) {
            this.useKeySequence = useKeySequence;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public BoshConnectionConfiguration build() {
            return new BoshConnectionConfiguration(this);
        }
    }
}
