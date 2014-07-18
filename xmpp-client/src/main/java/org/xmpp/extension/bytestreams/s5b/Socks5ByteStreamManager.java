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
import org.xmpp.extension.bytestreams.ByteStreamManager;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.BadRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class Socks5ByteStreamManager extends ByteStreamManager {

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final LocalSocks5Server localSocks5Server;

    private Socks5ByteStreamManager(final XmppSession xmppSession) {
        super(xmppSession, Socks5ByteStream.NAMESPACE);
        this.serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);

        this.localSocks5Server = LocalSocks5Server.INSTANCE;

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {

                    Socks5ByteStream socks5ByteStream = iq.getExtension(Socks5ByteStream.class);
                    if (socks5ByteStream != null) {
                        if (socks5ByteStream.getSessionId() == null) {
                            // If the request is malformed (e.g., the <query/> element does not include the 'sid' attribute), the Target MUST return an error of <bad-request/>.
                            xmppSession.send(iq.createError(new StanzaError(new BadRequest())));
                        } else {
                            notifyByteStreamEvent(new S5bEvent(Socks5ByteStreamManager.this, socks5ByteStream.getSessionId(), xmppSession, iq, socks5ByteStream.getStreamHosts()));
                        }
                        e.consume();
                    }
                }
            }
        });
        setEnabled(true);
    }

    static S5bSession createS5bSession(Jid requester, Jid target, String sessionId, List<StreamHost> streamHosts) throws IOException {
        Socket socketUsed = null;
        Jid streamHostUsed = null;
        IOException ioException = null;
        // If the Requester provides more than one StreamHost, the Target SHOULD try to connect to them in the order of the <streamhost/> children within the <query/> element.
        for (StreamHost streamHost : streamHosts) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(streamHost.getHost(), streamHost.getPort()));
                // If the Target is able to open a TCP socket on a StreamHost/Requester, it MUST use the SOCKS5 protocol to establish a SOCKS5 connection.
                Socks5Protocol.establishClientConnection(socket, sessionId, requester, target);
                socketUsed = socket;
                streamHostUsed = streamHost.getJid();
                break;
            } catch (IOException e) {
                // ignore, try next.
                ioException = e;
            }
        }
        if (streamHostUsed == null) {
            throw new IOException("Unable to connect to any stream host.", ioException);
        }
        return new S5bSession(sessionId, socketUsed, streamHostUsed);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            localSocks5Server.start();
        } else {
            localSocks5Server.stop();
        }
    }

    /**
     * Discovers the SOCKS5 proxies.
     *
     * @return The proxies.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0065.html#disco">4. Discovering Proxies</a>
     */
    public List<StreamHost> discoverProxies() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(null);
        for (Item item : itemNode.getItems()) {
            InfoNode infoNode = serviceDiscoveryManager.discoverInformation(item.getJid());
            if (infoNode.getFeatures().contains(new Feature(Socks5ByteStream.NAMESPACE))) {
                IQ result = xmppSession.query(new IQ(item.getJid(), IQ.Type.GET, new Socks5ByteStream()));
                Socks5ByteStream socks5ByteStream = result.getExtension(Socks5ByteStream.class);
                if (socks5ByteStream != null) {
                    return socks5ByteStream.getStreamHosts();
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Initiates a SOCKS5 session with a target.
     *
     * @param target    The target.
     * @param sessionId The session id.
     * @return The SOCKS5 byte stream session.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @throws IOException                     If the byte stream session could not be established.
     */
    public ByteStreamSession initiateSession(Jid target, String sessionId) throws XmppException, IOException {

        List<StreamHost> streamHosts = new ArrayList<>();

        localSocks5Server.start();

        Jid requester = xmppSession.getConnectedResource();

        // First add the local SOCKS5 server to the list of stream hosts.
        streamHosts.add(new StreamHost(requester, localSocks5Server.getAddress(), localSocks5Server.getPort()));

        // Then discover proxies as alternative stream hosts.
        XmppException proxyDiscoveryException = null;
        try {
            streamHosts.addAll(discoverProxies());
        } catch (XmppException e) {
            // If no proxies are found, ignore the exception.
            proxyDiscoveryException = e;
        }

        if (streamHosts.isEmpty()) {
            throw new IOException("No stream hosts found.", proxyDiscoveryException);
        }

        // Create the hash, which will identify the socket connection.
        String hash = Socks5ByteStream.hash(sessionId, requester, target);
        localSocks5Server.allowedAddresses.add(hash);

        try {
            // 5.3.1 Requester Initiates S5B Negotiation
            // 6.3.1 Requester Initiates S5B Negotiation
            IQ result = xmppSession.query(new IQ(target, IQ.Type.SET, new Socks5ByteStream(sessionId, streamHosts, hash)));

            // 5.3.3 Target Acknowledges Bytestream
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
                throw new IOException("Target did not respond with a stream host.");
            }

            Socket socket;
            if (!usedStreamHost.getJid().equals(requester)) {
                // 6.3.4 Requester Establishes SOCKS5 Connection with StreamHost
                socket = new Socket();
                socket.connect(new InetSocketAddress(usedStreamHost.getHost(), usedStreamHost.getPort()));
                Socks5Protocol.establishClientConnection(socket, sessionId, result.getTo(), target);

                // 6.3.5 Activation of Bytestream
                xmppSession.query(new IQ(usedStreamHost.getJid(), IQ.Type.SET, Socks5ByteStream.activate(sessionId, target)));
            } else {
                socket = localSocks5Server.getSocket(hash);
            }
            if (socket == null) {
                throw new IOException("Not connected to stream host");
            }
            return new S5bSession(sessionId, socket, usedStreamHost.getJid());
        } finally {
            localSocks5Server.removeConnection(hash);
        }
    }
}
