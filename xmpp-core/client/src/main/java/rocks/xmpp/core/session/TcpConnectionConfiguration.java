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

package rocks.xmpp.core.session;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.client.ClientConnectionConfiguration;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.dns.DnsResolver;
import rocks.xmpp.dns.SrvRecord;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * A configuration for a TCP connection.
 * It allows you to configure various connection settings for a TCP socket connection, most importantly the host address and port,
 * but also a whitespace keep-alive interval, a custom socket factory, a custom SSL context and compression methods.
 * <h3>Usage</h3>
 * In order to create an instance of this class you have to use the builder pattern as shown below.
 * ```java
 * TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(5222)
 *     .sslContext(sslContext)
 *     .channelEncryption(ChannelEncryption.DISABLED)
 *     .build();
 * ```
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration
 * @see TcpConnection
 */
public final class TcpConnectionConfiguration extends ClientConnectionConfiguration {

    private static volatile TcpConnectionConfiguration defaultConfiguration;

    private final int keepAliveInterval;

    private final SocketFactory socketFactory;

    private TcpConnectionConfiguration(Builder builder) {
        super(builder);
        this.keepAliveInterval = builder.keepAliveInterval;
        this.socketFactory = builder.socketFactory;
    }

    /**
     * Creates a new builder for this class.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the default configuration.
     *
     * @return The default configuration.
     */
    public static TcpConnectionConfiguration getDefault() {
        // Use double-checked locking idiom
        if (defaultConfiguration == null) {
            synchronized (TcpConnectionConfiguration.class) {
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
    public static void setDefault(TcpConnectionConfiguration configuration) {
        synchronized (TcpConnectionConfiguration.class) {
            defaultConfiguration = configuration;
        }
    }

    @Override
    public final Connection createConnection(XmppSession xmppSession) {

        try {
            Socket socket;
            if (getHostname() != null && !getHostname().isEmpty()) {
                socket = createAndConnectSocket(InetSocketAddress.createUnresolved(getHostname(), getPort()), getProxy());
            } else if (xmppSession.getDomain() != null) {
                if ((socket = connectWithXmppServiceDomain(xmppSession.getDomain(), xmppSession.getConfiguration().getNameServer())) == null) {
                    // 9. If the initiating entity does not receive a response to its SRV query, it SHOULD attempt the fallback process described in the next section.
                    socket = createAndConnectSocket(InetSocketAddress.createUnresolved(xmppSession.getDomain().toString(), getPort()), getProxy());
                }
            } else {
                throw new IllegalStateException("Neither 'xmppServiceDomain' nor 'host' is set.");
            }
            TcpConnection tcpConnection = new TcpConnection(socket, xmppSession, this);
            if (getChannelEncryption() == ChannelEncryption.DIRECT) {
                tcpConnection.secureConnection();
            }
            return tcpConnection;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Socket createAndConnectSocket(final InetSocketAddress unresolvedAddress, final Proxy proxy) throws IOException {
        final Socket socket;
        if (getSocketFactory() == null) {
            if (proxy != null) {
                socket = new Socket(proxy);
            } else {
                socket = new Socket();
            }
        } else {
            socket = getSocketFactory().createSocket();
        }
        // SocketFactory may return an already connected socket, so check the connected state to prevent SocketException.
        if (!socket.isConnected()) {
            socket.connect(new InetSocketAddress(unresolvedAddress.getHostName(), unresolvedAddress.getPort()), getConnectTimeout());
        }

        return socket;
    }

    /**
     * This is the preferred way to resolve the FQDN.
     *
     * @param xmppServiceDomain The fully qualified domain name.
     * @return If the connection could be established.
     * @see <a href="https://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">3.2.1.  Preferred Process: SRV Lookup</a>
     */
    private Socket connectWithXmppServiceDomain(final Jid xmppServiceDomain, final String nameServer) {

        // 1. The initiating entity constructs a DNS SRV query whose inputs are:
        //
        //   * Service of "xmpp-client" (for client-to-server connections) or "xmpp-server" (for server-to-server connections)
        try {
            final List<SrvRecord> srvRecords = DnsResolver.resolveSRV("xmpp-client", xmppServiceDomain, nameServer, getConnectTimeout());

            // 3. If a response is received, it will contain one or more combinations of a port and FDQN, each of which is weighted and prioritized as described in [DNS-SRV].
            // Sort the entries, so that the best one is tried first.
            srvRecords.sort(null);
            IOException ex = null;
            for (SrvRecord srvRecord : srvRecords) {
                if (srvRecord != null) {
                    // (However, if the result of the SRV lookup is a single resource record with a Target of ".", i.e., the root domain, then the initiating entity MUST abort SRV processing at this point because according to [DNS-SRV] such a Target "means that the service is decidedly not available at this domain".)
                    if (".".equals(srvRecord.getTarget())) {
                        return null;
                    }

                    try {
                        // 4. The initiating entity chooses at least one of the returned FQDNs to resolve (following the rules in [DNS-SRV]), which it does by performing DNS "A" or "AAAA" lookups on the FDQN; this will result in an IPv4 or IPv6 address.
                        // 5. The initiating entity uses the IP address(es) from the successfully resolved FDQN (with the corresponding port number returned by the SRV lookup) as the connection address for the receiving entity.
                        // 6. If the initiating entity fails to connect using that IP address but the "A" or "AAAA" lookups returned more than one IP address, then the initiating entity uses the next resolved IP address for that FDQN as the connection address.
                        return createAndConnectSocket(InetSocketAddress.createUnresolved(srvRecord.getTarget(), srvRecord.getPort()), getProxy());
                    } catch (IOException e) {
                        // 7. If the initiating entity fails to connect using all resolved IP addresses for a given FDQN, then it repeats the process of resolution and connection for the next FQDN returned by the SRV lookup based on the priority and weight as defined in [DNS-SRV].
                        ex = e;
                    }
                }
            }

            // 8. If the initiating entity receives a response to its SRV query but it is not able to establish an XMPP connection using the data received in the response, it SHOULD NOT attempt the fallback process described in the next section (this helps to prevent a state mismatch between inbound and outbound connections).
            if (!srvRecords.isEmpty()) {
                throw new IOException("Could not connect to any host.", ex);
            }
        } catch (Exception e) {
            // Unable to resolve the domain, try fallback.
            return null;
        }
        return null;
    }

    /**
     * Gets the whitespace keep-alive interval.
     *
     * @return The whitespace keep-alive interval.
     */
    public final int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    /**
     * Gets the socket factory.
     *
     * @return The socket factory.
     */
    public final SocketFactory getSocketFactory() {
        return socketFactory;
    }

    @Override
    public final String toString() {
        return "TCP connection configuration: " + super.toString();
    }

    /**
     * A builder to create a {@link TcpConnectionConfiguration} instance.
     */
    public static final class Builder extends ClientConnectionConfiguration.Builder<Builder> {

        private int keepAliveInterval;

        private SocketFactory socketFactory;

        private Builder() {
            // default values.
            channelEncryption(ChannelEncryption.OPTIONAL);
            port(5222);
            keepAliveInterval(30);
        }

        /**
         * Sets the whitespace keep-alive interval in seconds. If the interval is negative, no whitespace will be sent at all.
         *
         * @param keepAliveInterval The whitespace keep-alive interval.
         * @return The builder.
         */
        public Builder keepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        /**
         * Sets a socket factory which creates the socket.
         * This can be useful if you want connect to the legacy SSL port (usually 5223) and the connection is encrypted right from the beginning.
         * <p>
         * However, usually, there's no need to set a custom socket factory.
         *
         * @param socketFactory The socket factory.
         * @return The builder.
         * @see #secure(boolean)
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
        public TcpConnectionConfiguration build() {
            return new TcpConnectionConfiguration(this);
        }
    }
}
