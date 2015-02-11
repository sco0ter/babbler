/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stream.StreamFeatureListener;
import rocks.xmpp.core.stream.model.ClientStreamElement;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.extensions.compress.CompressionManager;
import rocks.xmpp.extensions.compress.CompressionMethod;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.xml.stream.XMLOutputFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

/**
 * The default TCP socket connection as described in <a href="http://xmpp.org/rfcs/rfc6120.html#tcp">TCP Binding</a>.
 * <p>
 * If no hostname is set (null or empty) the connection tries to resolve the hostname via an <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">SRV DNS lookup</a>.
 * <p>
 * This class is unconditionally thread-safe.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/rfcs/rfc6120.html#tcp">3.  TCP Binding</a>
 */
public final class TcpConnection extends Connection {

    private final TcpConnectionConfiguration tcpConnectionConfiguration;

    /**
     * The stream id, which is assigned by the server.
     * guarded by "this"
     */
    String streamId;

    /**
     * guarded by "this"
     */
    private Socket socket;

    /**
     * guarded by "this"
     */
    private XmppStreamWriter xmppStreamWriter;

    /**
     * guarded by "this"
     */
    private XmppStreamReader xmppStreamReader;

    /**
     * guarded by "this"
     */
    private InputStream inputStream;

    /**
     * guarded by "this"
     */
    private OutputStream outputStream;

    TcpConnection(XmppSession xmppSession, TcpConnectionConfiguration configuration) {
        super(xmppSession, configuration);
        this.tcpConnectionConfiguration = configuration;

        xmppSession.getStreamFeaturesManager().addFeatureNegotiator(new SecurityManager(xmppSession, new StreamFeatureListener() {
            @Override
            public void featureSuccessfullyNegotiated() throws StreamNegotiationException {
                try {
                    secureConnection();
                } catch (Exception e) {
                    throw new StreamNegotiationException(e);
                }
            }
        }, configuration.isSecure()));

        final CompressionManager compressionManager = new CompressionManager(xmppSession, configuration.getCompressionMethods());
        compressionManager.addFeatureListener(new StreamFeatureListener() {
            @Override
            public void featureSuccessfullyNegotiated() {
                CompressionMethod compressionMethod = compressionManager.getNegotiatedCompressionMethod();
                // We are in the reader thread here. Make sure it sees the streams assigned by the application thread in the connect() method by using synchronized.
                // The following might look overly verbose, but it follows the rule to "never call an alien method from within a synchronized region".
                InputStream iStream;
                OutputStream oStream;
                synchronized (TcpConnection.this) {
                    iStream = inputStream;
                    oStream = outputStream;
                }
                iStream = compressionMethod.decompress(iStream);
                oStream = compressionMethod.compress(oStream);
                synchronized (TcpConnection.this) {
                    inputStream = iStream;
                    outputStream = oStream;
                }
            }
        });
        xmppSession.getStreamFeaturesManager().addFeatureNegotiator(compressionManager);
    }

    @Override
    @Deprecated
    public final synchronized void connect() throws IOException {
        connect(null);
    }

    /**
     * Connects to the specified XMPP server using a socket connection.
     * Stream features are negotiated until SASL negotiation, which will be negotiated separately in the {@link XmppSession#login(String, String)} method.
     * <p>If only a XMPP service domain has been specified, it is tried to resolve the FQDN via SRV lookup.<br>
     * If that fails, it is tried to connect directly the XMPP service domain on port 5222.<br>
     * If a hostname and port have been specified, these are used to establish the connection.<br>
     * If a proxy has been specified, the connection is established through this proxy.<br>
     * </p>
     *
     * @param from The optional 'from' attribute in the stream header.
     * @throws IOException If the underlying socket throws an exception.
     */
    @Override
    public final synchronized void connect(Jid from) throws IOException {

        if (getXmppSession() == null) {
            throw new IllegalStateException("Can't connect without XmppSession. Use XmppSession to connect.");
        }

        if (getHostname() != null && !getHostname().isEmpty()) {
            connectToSocket(InetAddress.getByName(getHostname()), getPort(), getProxy());
        } else if (getXmppSession().getDomain() != null) {
            if (!connectWithXmppServiceDomain(getXmppSession().getDomain())) {
                // 9. If the initiating entity does not receive a response to its SRV query, it SHOULD attempt the fallback process described in the next section.
                connectToSocket(InetAddress.getByName(getXmppSession().getDomain()), getPort(), getProxy());
            }
        } else {
            throw new IllegalStateException("Neither 'xmppServiceDomain' nor 'host' is set.");
        }

        this.from = from;
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = new BufferedInputStream(socket.getInputStream());
        // Start writing to the output stream.
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
        xmppStreamWriter = new XmppStreamWriter(this.getXmppSession(), this, xmlOutputFactory);
        xmppStreamWriter.initialize(tcpConnectionConfiguration.getKeepAliveInterval());
        xmppStreamWriter.openStream(outputStream, from);

        // Start reading from the input stream.
        xmppStreamReader = new XmppStreamReader(this, this.getXmppSession(), xmlOutputFactory);
        xmppStreamReader.startReading(inputStream);
    }

    @Override
    public synchronized boolean isSecure() {
        return socket instanceof SSLSocket;
    }

    private void connectToSocket(InetAddress inetAddress, int port, Proxy proxy) throws IOException {
        if (tcpConnectionConfiguration.getSocketFactory() == null) {
            if (proxy != null) {
                socket = new Socket(proxy);
            } else {
                socket = new Socket();
            }
        } else {
            socket = tcpConnectionConfiguration.getSocketFactory().createSocket();
        }
        socket.connect(new InetSocketAddress(inetAddress, port), tcpConnectionConfiguration.getConnectTimeout());
        this.port = port;
        this.hostname = inetAddress.getHostName();
    }

    /**
     * This method is called from the reader thread. Because it accesses shared data (socket, outputStream, inputStream) it should be synchronized.
     */
    private void secureConnection() throws IOException, CertificateException, NoSuchAlgorithmException {

        SSLContext sslContext = tcpConnectionConfiguration.getSSLContext();
        if (sslContext == null) {
            sslContext = SSLContext.getDefault();
        }
        SSLSocket sslSocket;

        // synchronize socket because it's also used by the isSecure() method.
        synchronized (this) {
            socket = sslContext.getSocketFactory().createSocket(
                    socket,
                    getXmppSession().getDomain(),
                    socket.getPort(),
                    true);
            sslSocket = (SSLSocket) socket;
        }

        HostnameVerifier verifier = tcpConnectionConfiguration.getHostnameVerifier();

        // See
        // http://op-co.de/blog/posts/java_sslsocket_mitm/
        // http://tersesystems.com/2014/03/23/fixing-hostname-verification/

        // If no hostname verifier has been set, use the default one, which is used by HTTPS, too.
        if (verifier == null) {
            SSLParameters sslParameters = sslSocket.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            sslSocket.setSSLParameters(sslParameters);
        } else {
            sslSocket.startHandshake();
            // We are calling an "alien" method here, i.e. code we don't control.
            // Don't call alien methods from within synchronized regions, that's why the regions are split.
            if (!verifier.verify(getXmppSession().getDomain(), sslSocket.getSession())) {
                throw new CertificateException("Server failed to authenticate as " + getXmppSession().getDomain());
            }
        }

        synchronized (this) {
            outputStream = new BufferedOutputStream(socket.getOutputStream());
            inputStream = new BufferedInputStream(socket.getInputStream());
        }
    }

    @Override
    public final synchronized void send(ClientStreamElement element) {
        xmppStreamWriter.send(element);
    }

    @Override
    protected final synchronized void restartStream() {
        xmppStreamWriter.openStream(outputStream, from);
        xmppStreamReader.startReading(inputStream);
    }

    /**
     * Closes the TCP connection.
     * It first sends a {@code </stream:stream>}, then shuts down the writer so that no more stanzas can be sent.
     * After that it shuts down the reader and awaits shortly for any stanzas from the server and the server gracefully closing the stream with {@code </stream:stream>}.
     * Eventually the socket is closed.
     *
     * @throws IOException If the socket throws an I/O exception.
     */
    @Override
    public final synchronized void close() throws Exception {
        // This call closes the stream and waits until everything has been sent to the server.
        if (xmppStreamWriter != null) {
            xmppStreamWriter.shutdown();
            xmppStreamWriter = null;
        }
        // This call shuts down the reader and waits for a </stream> response from the server, if it hasn't already shut down before by the server.
        if (xmppStreamReader != null) {
            xmppStreamReader.shutdown();
            xmppStreamReader = null;
        }
        // We have sent a </stream:stream> to close the stream and waited for a server response, which also closes the stream by sending </stream:stream>.
        // Now close the socket.
        if (socket != null) {
            socket.close();
            socket = null;
        }
        inputStream = null;
        outputStream = null;
    }

    /**
     * This is the preferred way to resolve the FQDN.
     * See also <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">3.2.1.  Preferred Process: SRV Lookup</a>
     *
     * @param xmppServiceDomain The fully qualified domain name.
     * @return If the connection could be established.
     * @throws IOException If no connection could be established to a resolved host.
     */
    private boolean connectWithXmppServiceDomain(String xmppServiceDomain) throws IOException {

        // 1. The initiating entity constructs a DNS SRV query whose inputs are:
        //
        //   * Service of "xmpp-client" (for client-to-server connections) or "xmpp-server" (for server-to-server connections)
        //   * Proto of "tcp"
        //   * Name corresponding to the "origin domain" [TLS-CERTS] of the XMPP service to which the initiating entity wishes to connect (e.g., "example.net" or "im.example.com")
        //
        // 2. The result is a query such as "_xmpp-client._tcp.example.net." or "_xmpp-server._tcp.im.example.com.".
        String query = "_xmpp-client._tcp." + xmppServiceDomain;

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put(Context.PROVIDER_URL, "dns:");
        try {
            DirContext ctx = new InitialDirContext(env);

            Attributes attributes = ctx.getAttributes(query, new String[]{"SRV"});
            Attribute srvAttribute = attributes.get("SRV");
            List<DnsResourceRecord> dnsSrvRecords = new ArrayList<>();

            // 3. If a response is received, it will contain one or more combinations of a port and FDQN, each of which is weighted and prioritized as described in [DNS-SRV].
            if (srvAttribute != null) {
                NamingEnumeration<?> srvRecords = srvAttribute.getAll();
                while (srvRecords.hasMore()) {
                    String srvRecord = (String) srvRecords.next();
                    if (srvRecord != null) {
                        // (However, if the result of the SRV lookup is a single resource record with a Target of ".", i.e., the root domain, then the initiating entity MUST abort SRV processing at this point because according to [DNS-SRV] such a Target "means that the service is decidedly not available at this domain".)
                        if (srvRecord.equals(".")) {
                            return false;
                        }
                        dnsSrvRecords.add(new DnsResourceRecord(srvRecord));
                    }
                }

                // Sort the entries, so that the best one is tried first.
                Collections.sort(dnsSrvRecords, new Comparator<DnsResourceRecord>() {
                    @Override
                    public int compare(DnsResourceRecord o1, DnsResourceRecord o2) {
                        int result = Integer.compare(o1.priority, o2.priority);
                        if (result == 0) {
                            result = Integer.compare(o2.weight, o1.weight);
                        }
                        return result;
                    }
                });

                for (DnsResourceRecord dnsResourceRecord : dnsSrvRecords) {
                    try {
                        // 4. The initiating entity chooses at least one of the returned FQDNs to resolve (following the rules in [DNS-SRV]), which it does by performing DNS "A" or "AAAA" lookups on the FDQN; this will result in an IPv4 or IPv6 address.
                        InetAddress inetAddress = InetAddress.getByName(dnsResourceRecord.target);
                        // 5. The initiating entity uses the IP address(es) from the successfully resolved FDQN (with the corresponding port number returned by the SRV lookup) as the connection address for the receiving entity.
                        // 6. If the initiating entity fails to connect using that IP address but the "A" or "AAAA" lookups returned more than one IP address, then the initiating entity uses the next resolved IP address for that FDQN as the connection address.
                        connectToSocket(inetAddress, dnsResourceRecord.port, getProxy());
                        return true;
                    } catch (IOException e) {
                        // 7. If the initiating entity fails to connect using all resolved IP addresses for a given FDQN, then it repeats the process of resolution and connection for the next FQDN returned by the SRV lookup based on the priority and weight as defined in [DNS-SRV].
                    }
                }
                // 8. If the initiating entity receives a response to its SRV query but it is not able to establish an XMPP connection using the data received in the response, it SHOULD NOT attempt the fallback process described in the next section (this helps to prevent a state mismatch between inbound and outbound connections).
                if (dnsSrvRecords.size() > 0) {
                    throw new IOException("Could not connect to any host.");
                }
            }
        } catch (NamingException e) {
            return false;
        }
        return false;
    }

    @Override
    public final synchronized String getStreamId() {
        return streamId;
    }

    @Override
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder("TCP connection");
        if (hostname != null) {
            sb.append(String.format(" to %s:%s", hostname, port));
        }
        if (streamId != null) {
            sb.append(" (").append(streamId).append(")");
        }
        if (from != null) {
            sb.append(", from: ").append(from);
        }
        return sb.toString();
    }

    /**
     * http://tools.ietf.org/html/rfc2782
     */
    private static final class DnsResourceRecord {

        /**
         * The priority of this target host.  A client MUST attempt to
         * contact the target host with the lowest-numbered priority it can
         * reach; target hosts with the same priority SHOULD be tried in an
         * order defined by the weight field.  The range is 0-65535.  This
         * is a 16 bit unsigned integer in network byte order.
         */
        final int priority;

        /**
         * A server selection mechanism.  The weight field specifies a
         * relative weight for entries with the same priority. Larger
         * weights SHOULD be given a proportionately higher probability of
         * being selected. The range of this number is 0-65535.  This is a
         * 16 bit unsigned integer in network byte order.  Domain
         * administrators SHOULD use Weight 0 when there isn't any server
         * selection to do, to make the RR easier to read for humans (less
         * noisy).  In the presence of records containing weights greater
         * than 0, records with weight 0 should have a very small chance of
         * being selected.
         */
        final int weight;

        /**
         * The port on this target host of this service.
         */
        final int port;

        /**
         * The domain name of the target host.
         */
        final String target;

        DnsResourceRecord(String srvRecord) {
            String[] recordParts = srvRecord.split(" ");
            this.priority = Integer.parseInt(recordParts[recordParts.length - 4]);
            this.weight = Integer.parseInt(recordParts[recordParts.length - 3]);
            this.port = Integer.parseInt(recordParts[recordParts.length - 2]);
            String target = recordParts[recordParts.length - 1];
            this.target = target.substring(0, target.length() - 1);
        }
    }
}
