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

package rocks.xmpp.core.session;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.net.Proxy;

/**
 * A base class for connection configurations.
 *
 * @author Christian Schudt
 */
public abstract class ConnectionConfiguration {

    private final String hostname;

    private final int port;

    private final Proxy proxy;

    private final boolean secure;

    private final SSLContext sslContext;

    private HostnameVerifier hostnameVerifier;

    protected ConnectionConfiguration(Builder builder) {
        this.hostname = builder.hostname;
        this.port = builder.port;
        this.proxy = builder.proxy;
        this.secure = builder.secure;
        this.sslContext = builder.sslContext;
        this.hostnameVerifier = builder.hostnameVerifier;
    }

    /**
     * A factory method to create the connection.
     *
     * @param xmppSession The XMPP session, which is associated with the connection.
     * @return The connection.
     */
    public abstract Connection createConnection(XmppSession xmppSession);

    /**
     * Gets the hostname.
     *
     * @return The hostname.
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * Gets the port.
     *
     * @return The port.
     */
    public final int getPort() {
        return port;
    }

    /**
     * Gets the proxy.
     *
     * @return The proxy.
     */
    public final Proxy getProxy() {
        return proxy;
    }

    /**
     * Indicates whether the connection is secured by SSL.
     *
     * @return If the connection is to be secured.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Gets the SSL context.
     *
     * @return The SSL context.
     */
    public final SSLContext getSSLContext() {
        return sslContext;
    }

    /**
     * Gets the hostname verifier.
     *
     * @return The hostname verifier.
     */
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * An abstract builder class for building immutable configuration objects.
     *
     * @param <T> The concrete builder class.
     */
    public abstract static class Builder<T extends Builder<T>> {

        private String hostname;

        private int port;

        private Proxy proxy;

        private boolean secure;

        private SSLContext sslContext;

        private HostnameVerifier hostnameVerifier;

        protected Builder() {
        }

        /**
         * Returns an instance of the concrete builder.
         *
         * @return The concrete builder.
         */
        protected abstract T self();

        /**
         * Sets the hostname.
         *
         * @param hostname The hostname.
         * @return The builder.
         */
        public final T hostname(String hostname) {
            this.hostname = hostname;
            return self();
        }

        /**
         * Sets the port.
         *
         * @param port The port.
         * @return The builder.
         */
        public final T port(int port) {
            this.port = port;
            return self();
        }

        /**
         * Sets the proxy, e.g. if you are behind a HTTP proxy and use a BOSH connection.
         *
         * @param proxy The proxy.
         * @return The builder.
         */
        public final T proxy(Proxy proxy) {
            this.proxy = proxy;
            return self();
        }

        /**
         * Sets whether the connection is secured via SSL.
         *
         * @param secure If the connection is secured via SSL.
         * @return The builder.
         */
        public final T secure(boolean secure) {
            this.secure = secure;
            return self();
        }

        /**
         * Sets a custom SSL context, used to secure the connection.
         *
         * @param sslContext The SSL context.
         * @return The builder.
         */
        public final T sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return self();
        }

        /**
         * Sets an optional hostname verifier, used to verify the hostname in the certificate presented by the server.
         * If no verifier is set, the hostname is verified nonetheless using the default.
         *
         * @param hostnameVerifier The hostname verifier.
         * @return The builder.
         */
        public final T hostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return self();
        }

        /**
         * Builds the connection configuration.
         *
         * @return The concrete connection configuration.
         */
        public abstract ConnectionConfiguration build();
    }
}
