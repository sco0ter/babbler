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

package org.xmpp;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * The default TCP socket connection as described in <a href="http://xmpp.org/rfcs/rfc6120.html#tcp">TCP Binding</a>.
 *
 * @author Christian Schudt
 */
public final class TcpConnection extends Connection {

    /**
     * The stream id, which is assigned by the server.
     */
    volatile String streamId;

    private volatile Socket socket;

    private XmppStreamWriter xmppStreamWriter;

    private XmppStreamReader xmppStreamReader;

    /**
     * Creates a default connection to a XMPP server by only using a XMPP service domain.
     * The fully qualified domain name and port of the server is looked up via a <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">SRV lookup</a>.
     * If the lookup fails the fallback mechanism is used, i.e. it is tried to resolve the domain and connect on port 5222.
     *
     * @param xmppServiceDomain The XMPP service domain.
     */
    public TcpConnection(String xmppServiceDomain) {
        super(xmppServiceDomain, Proxy.NO_PROXY);
    }

    /**
     * Creates a default connection through a proxy to a XMPP server by only using a XMPP service domain.
     *
     * @param xmppServiceDomain The XMPP service domain.
     * @param proxy             The proxy, whose type should be {@link Proxy.Type#SOCKS}
     * @see #TcpConnection(String)
     */
    public TcpConnection(String xmppServiceDomain, Proxy proxy) {
        super(xmppServiceDomain, proxy);
    }

    /**
     * Creates a default connection to a given hostname and port.
     *
     * @param xmppServiceDomain The XMPP service domain, which will only be used in the opening XMPP stream as the value in the 'to' attribute.
     * @param hostname          The hostname.
     * @param port              The port.
     */
    public TcpConnection(String xmppServiceDomain, String hostname, int port) {
        super(xmppServiceDomain, hostname, port, Proxy.NO_PROXY);
    }

    /**
     * Creates a default connection to a given hostname and port through a proxy.
     *
     * @param xmppServiceDomain The XMPP service domain, which will only be used in the opening XMPP stream as the value in the 'to' attribute.
     * @param hostname          The hostname.
     * @param port              The port.
     * @param proxy             The proxy, whose type should be {@link Proxy.Type#SOCKS}
     * @param xmppContext       The XMPP context.
     * @see #TcpConnection(String, String, int)
     */
    public TcpConnection(String xmppServiceDomain, String hostname, int port, Proxy proxy, XmppContext xmppContext) {
        super(xmppServiceDomain, hostname, port, proxy, xmppContext);
    }

    /**
     * Creates a default connection to a given hostname and port.
     *
     * @param hostname The hostname.
     * @param port     The port.
     */
    public TcpConnection(String hostname, int port) {
        super(null, hostname, port, Proxy.NO_PROXY);
    }

    /**
     * Creates a default connection to a given hostname and port.
     *
     * @param hostname The hostname.
     * @param port     The port.
     * @param proxy    The proxy, whose type should be {@link Proxy.Type#SOCKS}
     * @see #TcpConnection(String, int)
     */
    public TcpConnection(String hostname, int port, Proxy proxy) {
        super(null, hostname, port, proxy);
    }

    /**
     * Connects to the specified XMPP server using a socket connection.
     * Stream features are negotiated until SASL negotiation, which will be negotiated separately in the {@link #login(String, String)} method.
     * <p>If only a XMPP service domain has been specified, it is tried to resolve the FQDN via SRV lookup.<br>
     * If that fails, it is tried to connect directly the XMPP service domain on port 5222.<br>
     * If a hostname and port have been specified, these are used to establish the connection.<br>
     * If a proxy has been specified, the connection is established through this proxy.<br>
     * </p>
     *
     * @throws IOException      If the underlying socket threw an exception.
     * @throws TimeoutException If the connection timed out.
     */
    @Override
    public synchronized void connect() throws IOException, TimeoutException {
        super.connect();
        int port = getPort() == 0 ? 5222 : getPort();

        this.socket = new Socket(proxy);

        if (getHostname() != null) {
            socket.connect(new InetSocketAddress(getHostname(), getPort()));
        } else if (xmppServiceDomain != null) {
            try {
                connectWithXmppServiceDomain(xmppServiceDomain);
            } catch (NamingException e) {
                // 9. If the initiating entity does not receive a response to its SRV query, it SHOULD attempt the fallback process described in the next section.
                socket.connect(new InetSocketAddress(InetAddress.getByName(xmppServiceDomain), port));
            }
        } else {
            throw new IllegalStateException("Neither 'xmppServiceDomain' nor 'host' is set.");
        }


        // Start writing to the output stream.
        try {
            xmppStreamWriter = new XmppStreamWriter(socket.getOutputStream(), this, this.xmlOutputFactory, this.marshaller);
        } catch (JAXBException | XMLStreamException e) {
            throw new IOException(e);
        }
        xmppStreamWriter.openStream(null);

        // Start reading from the input stream.
        try {
            xmppStreamReader = new XmppStreamReader(this);
        } catch (JAXBException e) {
            throw new IOException(e);
        }
        xmppStreamReader.startReading(socket.getInputStream());

        // Wait until the reader thread signals, that we are connected. That is after TLS negotiation and before SASL negotiation.
        waitUntilSaslNegotiationStarted();

        if (!isSecure && getSecurityManager().isTlsEnabled()) {
            throw new IllegalStateException("Connection could not be secured.");
        }
        updateStatus(Status.CONNECTED);
    }

    @Override
    protected void secureConnection() {
        try {
            socket = getSecurityManager().getSSLContext().getSocketFactory().createSocket(
                    socket,
                    socket.getInetAddress().getHostAddress(),
                    socket.getPort(),
                    true);
            isSecure = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Object element) {
        super.send(element);
        xmppStreamWriter.send(element);
    }

    @Override
    protected void restartStream() {
        try {
            xmppStreamWriter.reset(socket.getOutputStream());
            xmppStreamWriter.openStream(null);
            xmppStreamReader.startReading(socket.getInputStream());
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();

        // This call closes the stream and waits until everything has been sent to the server.
        xmppStreamWriter.shutdown();
        // This call shuts down the reader and waits for a </stream> response from the server, if it hasn't already shut down before by the server.
        xmppStreamReader.shutdown();
        // We have sent a </stream:stream> to close the stream and waited for a server response, which also statusChanged the stream by sending </stream:stream>.
        // Now close the socket.
        socket.close();
        updateStatus(Status.CLOSED);
    }

    /**
     * This is the preferred way to resolve the FQDN.
     * See also <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-resolution-prefer">3.2.1.  Preferred Process: SRV Lookup</a>
     *
     * @param xmppServiceDomain The fully qualified domain name.
     * @throws NamingException
     */
    void connectWithXmppServiceDomain(String xmppServiceDomain) throws NamingException, IOException {

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

        DirContext ctx = new InitialDirContext(env);

        Attributes attributes = ctx.getAttributes(query, new String[]{"SRV"});
        Attribute srvAttribute = attributes.get("SRV");
        List<DnsResourceRecord> dnsSrvRecords = new ArrayList<>();

        // 3. If a response is received, it will contain one or more combinations of a port and FDQN, each of which is weighted and prioritized as described in [DNS-SRV].
        NamingEnumeration<?> srvRecords = srvAttribute.getAll();
        while (srvRecords.hasMore()) {
            String srvRecord = (String) srvRecords.next();
            if (srvRecord != null) {
                // (However, if the result of the SRV lookup is a single resource record with a Target of ".", i.e., the root domain, then the initiating entity MUST abort SRV processing at this point because according to [DNS-SRV] such a Target "means that the service is decidedly not available at this domain".)
                if (srvRecord.equals(".")) {
                    return;
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
                socket.connect(new InetSocketAddress(inetAddress, dnsResourceRecord.port));
                return;
            } catch (IOException e) {
                // 7. If the initiating entity fails to connect using all resolved IP addresses for a given FDQN, then it repeats the process of resolution and connection for the next FQDN returned by the SRV lookup based on the priority and weight as defined in [DNS-SRV].
            }
        }
        // 8. If the initiating entity receives a response to its SRV query but it is not able to establish an XMPP connection using the data received in the response, it SHOULD NOT attempt the fallback process described in the next section (this helps to prevent a state mismatch between inbound and outbound connections).
        if (dnsSrvRecords.size() > 0) {
            throw new IOException("Could not connect to any host.");
        }
    }

    /**
     * http://tools.ietf.org/html/rfc2782
     */
    static final class DnsResourceRecord {

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
