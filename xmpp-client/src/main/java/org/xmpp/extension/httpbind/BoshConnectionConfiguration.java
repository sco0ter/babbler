/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
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

package org.xmpp.extension.httpbind;

import org.xmpp.Connection;
import org.xmpp.ConnectionConfiguration;
import org.xmpp.XmppSession;
import org.xmpp.XmppSessionConfiguration;

/**
 * A configuration for the BOSH connection.
 *
 * @author Christian Schudt
 * @see org.xmpp.extension.httpbind.BoshConnection
 */
public final class BoshConnectionConfiguration extends ConnectionConfiguration {

    private static volatile BoshConnectionConfiguration defaultConfiguration;

    private final int wait;

    private final String file;

    private final String route;

    private BoshConnectionConfiguration(Builder builder) {
        super(builder);
        this.wait = builder.wait;
        this.file = builder.file;
        this.route = builder.route;
    }

    /**
     * Gets the default configuration.
     *
     * @return The default configuration.
     */
    public static BoshConnectionConfiguration getDefault() {
        // Use double-checked locking idiom
        if (defaultConfiguration == null) {
            synchronized (XmppSessionConfiguration.class) {
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
    public int getWait() {
        return wait;
    }

    /**
     * Gets the file on the host, e.g. "/http-bind/".
     *
     * @return The file on the host.
     */
    public String getFile() {
        return file;
    }

    /**
     * Gets the route.
     *
     * @return The route.
     */
    public String getRoute() {
        return route;
    }

    /**
     * A builder to create a {@link org.xmpp.extension.httpbind.BoshConnectionConfiguration} instance.
     */
    public static final class Builder extends ConnectionConfiguration.Builder<Builder> {

        private int wait;

        private String file;

        private String route;

        private Builder() {
            // default values
            port(5280);
            wait(60);
            file("/http-bind/");
        }

        /**
         * Sets the file on the host, e.g. "/http-bind/"
         *
         * @param file The file on the host.
         * @return The builder.
         */
        public Builder file(String file) {
            this.file = file;
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
