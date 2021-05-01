/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.TcpConnection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.dns.DnsResolver;
import rocks.xmpp.dns.SrvRecord;
import rocks.xmpp.util.concurrent.CompletionStages;

/**
 * An abstract transport connector which binds XMPP to TCP using the preferred TCP resolution process.
 *
 * <p>Implementations must implement {@link #connect(String, int, TcpConnectionConfiguration)} which establishes the
 * TCP connection.</p>
 *
 * @param <T> The concrete TCP transport implementation, such as {@link java.net.Socket}.
 * @see <a href="https://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">3.2.1.  Preferred Process: SRV Lookup</a>
 */
public abstract class AbstractTcpConnector<T> implements TransportConnector<TcpConnectionConfiguration> {

    private static final System.Logger logger = System.getLogger(AbstractTcpConnector.class.getName());

    /**
     * Creates the connection.
     *
     * @param xmppSession The XMPP session.
     * @param creator     The creation function using the return value of {@link #connect(String, int,
     *                    TcpConnectionConfiguration)} as input.
     * @param sessionOpen The session open element.
     * @return A future which is complete, when the connection is established.
     */
    protected final CompletableFuture<Connection> createConnection(XmppSession xmppSession,
                                                                   TcpConnectionConfiguration configuration,
                                                                   BiFunction<T, TcpConnectionConfiguration,
                                                                           TcpConnection> creator,
                                                                   SessionOpen sessionOpen) {
        CompletableFuture<T> socket;
        final AtomicBoolean useDirectTls =
                new AtomicBoolean(configuration.getChannelEncryption() == ChannelEncryption.DIRECT);
        if (configuration.getHostname() != null && !configuration.getHostname().isEmpty()) {
            socket = connect(configuration.getHostname(), configuration.getPort(), configuration);
        } else if (xmppSession.getDomain() != null) {
            if ((socket = connectWithXmppServiceDomain(xmppSession.getDomain(), configuration,
                    xmppSession.getConfiguration().getNameServer(), useDirectTls::set)) == null) {
                // 9. If the initiating entity does not receive a response to its SRV query, it SHOULD attempt the
                // fallback process described in the next section.
                socket = connect(xmppSession.getDomain().toString(), configuration.getPort(), configuration);
            }
        } else {
            throw new IllegalStateException("Neither 'xmppServiceDomain' nor 'host' is set.");
        }
        return socket.thenApply(s -> {
            TcpConnection tcpConnection = creator.apply(s, configuration);
            if (useDirectTls.get()) {
                try {
                    tcpConnection.secureConnection();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }
            return tcpConnection;
        }).thenCompose(connection -> connection.open(sessionOpen).thenApply(aVoid -> connection));
    }

    /**
     * Connects to a host and port.
     *
     * @param hostname      The host.
     * @param configuration The configuration.
     * @return A future with the connected transport.
     */
    protected abstract CompletableFuture<T> connect(String hostname, int port,
                                                    TcpConnectionConfiguration configuration);

    /**
     * This is the preferred way to resolve the FQDN.
     *
     * @param xmppServiceDomain The fully qualified domain name.
     * @param nameServer        The name server used for DNS resolution.
     * @param isDirectTls       The consumer which gets notified, if direct TLS is used.
     * @return The future which completes with a connected socket or null, if the connection could not be established
     * and the fallback (use domain as hostname) should be tried.
     * @see <a href="https://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">3.2.1.  Preferred Process: SRV Lookup</a>
     */
    CompletableFuture<T> connectWithXmppServiceDomain(final Jid xmppServiceDomain,
                                                      final TcpConnectionConfiguration configuration,
                                                      final String nameServer,
                                                      final Consumer<Boolean> isDirectTls) {

        // 1. The initiating entity constructs a DNS SRV query whose inputs are:
        //
        //   * Service of "xmpp-client" (for client-to-server connections) or "xmpp-server"
        //   (for server-to-server connections)
        try {

            final List<SrvRecord> srvRecords = new ArrayList<>();
            if (configuration.getChannelEncryption() != ChannelEncryption.DIRECT) {
                // Don't lookup unencrypted end points, if only direct TLS is allowed.
                srvRecords.addAll(DnsResolver
                        .resolveSRV("xmpp-client", xmppServiceDomain, nameServer, configuration.getConnectTimeout()));
            }
            final List<SrvRecord> srvRecordsXmpps = new ArrayList<>();
            if (configuration.getChannelEncryption() != ChannelEncryption.DISABLED) {
                // Only resolve SRV if TLS is not disabled.
                srvRecordsXmpps.addAll(DnsResolver
                        .resolveSRV("xmpps-client", xmppServiceDomain, nameServer, configuration.getConnectTimeout()));
            }
            srvRecords.addAll(srvRecordsXmpps);
            // 3. If a response is received, it will contain one or more combinations of a port and FDQN,
            // each of which is weighted and prioritized as described in [DNS-SRV].
            // Sort the entries, so that the best one is tried first.
            srvRecords.sort(null);

            if (!srvRecords.isEmpty()) {
                return connectToNextHost(srvRecords.iterator(), srvRecordsXmpps, configuration, isDirectTls);
            }
        } catch (Exception e) {
            // Unable to resolve the domain, try fallback.
            return null;
        }
        return null;
    }

    private CompletableFuture<T> connectToNextHost(Iterator<SrvRecord> iterator,
                                                   List<SrvRecord> srvRecordsXmpps,
                                                   TcpConnectionConfiguration configuration,
                                                   Consumer<Boolean> isDirectTls) {
        if (iterator.hasNext()) {
            SrvRecord srvRecord = iterator.next();

            // If the result of the SRV lookup is a single resource record with a Target of ".",
            // i.e., the root domain, then the initiating entity MUST abort SRV processing at this point
            // because according to [DNS-SRV] such a Target "means that the service is decidedly
            // not available at this domain".
            if (".".equals(srvRecord.getTarget())) {
                if (srvRecordsXmpps.contains(srvRecord)) {
                    // Direct TLS is not supported, in this case try the non-direct-TLS targets.
                    return connectToNextHost(iterator, srvRecordsXmpps, configuration, isDirectTls);
                } else {
                    return null;
                }
            }

            // 4. The initiating entity chooses at least one of the returned FQDNs to resolve
            // (following the rules in [DNS-SRV]), which it does by performing DNS "A" or "AAAA" lookups
            // on the FDQN; this will result in an IPv4 or IPv6 address.
            // 5. The initiating entity uses the IP address(es) from the successfully resolved FDQN
            // (with the corresponding port number returned by the SRV lookup) as the connection address
            // for the receiving entity.
            // 6. If the initiating entity fails to connect using that IP address but the "A" or "AAAA"
            // lookups returned more than one IP address, then the initiating entity uses the next resolved
            // IP address for that FDQN as the connection address.
            // 7. If the initiating entity fails to connect using all resolved IP addresses for a given
            // FDQN, then it repeats the process of resolution and connection for the next FQDN returned by
            // the SRV lookup based on the priority and weight as defined in [DNS-SRV].
            // 8. If the initiating entity receives a response to its SRV query but it is not able to establish an XMPP
            // connection using the data received in the response, it SHOULD NOT attempt the fallback process described
            // in the next section (this helps to prevent a state mismatch between inbound and outbound connections).

            logger.log(System.Logger.Level.DEBUG, "Trying to connect to {0}:{1}", srvRecord.getTarget(),
                    String.valueOf(srvRecord.getPort()));
            CompletableFuture<T> socket = connect(srvRecord.getTarget(), srvRecord.getPort(), configuration);
            return CompletionStages
                    .withFallback(socket,
                            (failedStage, exc) -> connectToNextHost(iterator, srvRecordsXmpps, configuration,
                                    isDirectTls))
                    .thenApply(s -> {
                        if (srvRecordsXmpps.contains(srvRecord)) {
                            isDirectTls.accept(true);
                        }
                        return s;
                    }).toCompletableFuture();

        }

        // 8. If the initiating entity receives a response to its SRV query but it is not able to establish an XMPP
        // connection using the data received in the response, it SHOULD NOT attempt the fallback process described
        // in the next section (this helps to prevent a state mismatch between inbound and outbound connections).
        return CompletableFuture.failedFuture(new IOException("Could not connect to any host"));
    }
}
