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

package org.xmpp.extension.bytestreams.s5b;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.bytestreams.ByteStreamListener;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.extension.bytestreams.ibb.IbbSession;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.client.IQ;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public class Socks5ByteStreamManager extends ExtensionManager {

    public static final String NAMESPACE = "http://jabber.org/protocol/bytestreams";

    private static final Logger logger = Logger.getLogger(Socks5ByteStreamManager.class.getName());

    final Set<ByteStreamListener> byteStreamListeners = new CopyOnWriteArraySet<>();

    private final Map<String, IbbSession> ibbSessionMap = new ConcurrentHashMap<>();

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private Socks5ByteStreamManager(final XmppSession xmppSession) {
        super(xmppSession, NAMESPACE);
        this.serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {

                    Socks5ByteStream socks5ByteStream = iq.getExtension(Socks5ByteStream.class);
                    if (socks5ByteStream != null) {

                        for (ByteStreamListener byteStreamListener : byteStreamListeners) {
                            try {
                                byteStreamListener.byteStreamRequested(new S5bEvent(Socks5ByteStreamManager.this, socks5ByteStream.getSessionId(), xmppSession, iq, socks5ByteStream.getStreamHosts()));
                            } catch (Exception exc) {
                                logger.log(Level.WARNING, exc.getMessage(), exc);
                            }
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    /**
     * Establishes the SOCKS5 connection.
     *
     * @param socket    The socket.
     * @param sessionId The session id.
     * @param requester The requester.
     * @param target    The target.
     * @throws IOException If the SOCKS5 connection could not be established.
     */
    private static void establish(Socket socket, String sessionId, Jid requester, Jid target) throws IOException {

        DataInputStream in = new DataInputStream(socket.getInputStream());
        OutputStream out = socket.getOutputStream();

        /*
            The client connects to the server, and sends a version
            identifier/method selection message:

                   +----+----------+----------+
                   |VER | NMETHODS | METHODS  |
                   +----+----------+----------+
                   | 1  |    1     | 1 to 255 |
                   +----+----------+----------+
         */

        out.write(new byte[]{(byte) 0x05, (byte) 0x01, (byte) 0x00}); // 0x00 == NO AUTHENTICATION REQUIRED
        out.flush();

        /*
            The server selects from one of the methods given in METHODS, and
            sends a METHOD selection message:

                         +----+--------+
                         |VER | METHOD |
                         +----+--------+
                         | 1  |   1    |
                         +----+--------+
         */
        byte[] response = new byte[2];
        in.readFully(response);

        // If the server supports version 5 and has accepted the no-authentication method
        if (response[0] == (byte) 0x05 && response[1] == (byte) 0x00) {

            /*
                The SOCKS request is formed as follows:

                  +----+-----+-------+------+----------+----------+
                  |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
                  +----+-----+-------+------+----------+----------+
                  | 1  |  1  | X'00' |  1   | Variable |    2     |
                  +----+-----+-------+------+----------+----------+

             */

            byte[] dstAddr = Socks5ByteStream.hash(sessionId, requester, target).getBytes();
            byte[] dstPort = new byte[]{0x00, 0x00}; // The port MUST be 0 (zero).
            byte[] requestDetails = new byte[]{
                    (byte) 0x05, // protocol version: X'05'
                    (byte) 0x01, // CMD: CONNECT X'01'
                    (byte) 0x00, // RESERVED
                    (byte) 0x03, // ATYP, Hardcoded to 3 (DOMAINNAME) in this usage
                    (byte) dstAddr.length // The first octet of the address field contains the number of octets of name that follow
            };

            out.write(requestDetails);
            out.write(dstAddr); // DST.ADDR
            out.write(dstPort); // DST.PORT
            out.flush();


            /*
                The server evaluates the request, and returns a reply formed as follows:

                    +----+-----+-------+------+----------+----------+
                    |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
                    +----+-----+-------+------+----------+----------+
                    | 1  |  1  | X'00' |  1   | Variable |    2     |
                    +----+-----+-------+------+----------+----------+
             */

            byte[] halfReply = new byte[5];
            in.readFully(halfReply, 0, 5);

            if (halfReply[1] == 0) {  // X'00' succeeded
                byte[] hash = new byte[halfReply[4]];
                in.readFully(hash);
                byte[] bndPort = new byte[2];
                in.readFully(bndPort);
                // When replying to the Target in accordance with Section 6 of RFC 1928, the Proxy MUST set the BND.ADDR and BND.PORT to the DST.ADDR and DST.PORT values provided by the client in the connection request.
                if (!(Arrays.equals(dstAddr, hash) && Arrays.equals(dstPort, bndPort))) {
                    throw new IOException("Verification failed.");
                }
            } else {
                throw new IOException("SOCKS5 server returned error code " + halfReply[1]);
            }
        } else {
            throw new IOException("Unable to connect to SOCKS5 server.");
        }
    }

    /**
     * Discovers the SOCKS5 proxies.
     *
     * @return The proxies.
     * @throws XmppException
     * @see <a href="http://xmpp.org/extensions/xep-0065.html#disco">4. Discovering Proxies</a>
     */
    public List<StreamHost> discoverProxies() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(null);
        for (Item item : itemNode.getItems()) {
            InfoNode infoNode = serviceDiscoveryManager.discoverInformation(item.getJid());
            if (infoNode.getFeatures().contains(new Feature(NAMESPACE))) {
                IQ result = xmppSession.query(new IQ(item.getJid(), IQ.Type.GET, new Socks5ByteStream()));
                Socks5ByteStream socks5ByteStream = result.getExtension(Socks5ByteStream.class);
                if (socks5ByteStream != null) {
                    return socks5ByteStream.getStreamHosts();
                }
            }
        }
        return Collections.emptyList();
    }

    public StreamHost connect(List<StreamHost> streamHosts) {
        StreamHost streamHostUsed = null;
        for (StreamHost streamHost : streamHosts) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(streamHost.getHost(), streamHost.getPort()));
                establish(socket, "1234", Jid.valueOf("juliet@example.net"), Jid.valueOf("romeo@example.net"));
                streamHostUsed = streamHost;
                break;
            } catch (IOException e) {
                // ignore, try next.
            }
        }
        return streamHostUsed;
    }

    /**
     * Initiates a SOCKS5 session with a target.
     *
     * @param target      The target.
     * @param sessionId   The session id.
     * @param streamHosts The stream hosts.
     * @return The SOCKS5 byte stream session.
     * @throws XmppException
     * @throws IOException
     */
    public ByteStreamSession initiateSession(Jid target, String sessionId, List<StreamHost> streamHosts) throws XmppException, IOException {
        // 6.3.1 Requester Initiates S5B Negotiation
        IQ result = xmppSession.query(new IQ(target, IQ.Type.SET, new Socks5ByteStream(sessionId, streamHosts)));

        // 6.3.3 Target Acknowledges Bytestream
        Socks5ByteStream socks5ByteStream = result.getExtension(Socks5ByteStream.class);
        StreamHost usedStreamHost = null;
        for (StreamHost streamHost : streamHosts) {
            if (socks5ByteStream.getStreamHostUsed() != null && socks5ByteStream.getStreamHostUsed().equals(streamHost.getJid())) {
                usedStreamHost = streamHost;
                break;
            }
        }

        if (usedStreamHost == null) {
            throw new IllegalStateException("Target did not respond with a stream host.");
        }

        // 6.3.4 Requester Establishes SOCKS5 Connection with StreamHost
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(usedStreamHost.getHost(), usedStreamHost.getPort()));
        establish(socket, sessionId, result.getTo(), target);

        // 6.3.5 Activation of Bytestream
        xmppSession.query(new IQ(usedStreamHost.getJid(), IQ.Type.SET, Socks5ByteStream.activate(sessionId, target)));

        return new S5bSession(sessionId, socket, usedStreamHost.getJid());
    }

    /**
     * Adds a byte stream listener, which allows to listen for incoming byte stream requests.
     *
     * @param byteStreamListener The listener.
     * @see #removeByteStreamListener(org.xmpp.extension.bytestreams.ByteStreamListener)
     */
    public void addByteStreamListener(ByteStreamListener byteStreamListener) {
        byteStreamListeners.add(byteStreamListener);
    }

    /**
     * Removes a previously added byte stream listener.
     *
     * @param ibbListener The listener.
     * @see #addByteStreamListener(org.xmpp.extension.bytestreams.ByteStreamListener)
     */
    public void removeByteStreamListener(ByteStreamListener ibbListener) {
        byteStreamListeners.remove(ibbListener);
    }

    public S5bSession createS5bSession(Jid requester, Jid target, String sessionId, List<StreamHost> streamHosts) {
        Socket socketUsed = null;
        Jid streamHostUsed = null;
        for (StreamHost streamHost : streamHosts) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(streamHost.getHost(), streamHost.getPort()));
                establish(socket, sessionId, requester, target);
                socketUsed = socket;
                streamHostUsed = streamHost.getJid();
                break;
            } catch (IOException e) {
                // ignore, try next.
            }
        }
        return new S5bSession(sessionId, socketUsed, streamHostUsed);
    }
}
