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

package rocks.xmpp.extensions.bytestreams.s5b;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.bytestreams.ByteStreamManager;
import rocks.xmpp.extensions.bytestreams.ByteStreamSession;
import rocks.xmpp.extensions.bytestreams.s5b.model.Socks5ByteStream;
import rocks.xmpp.extensions.bytestreams.s5b.model.StreamHost;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.items.Item;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A manager for <a href="http://xmpp.org/extensions/xep-0065.html">XEP-0065: SOCKS5 Bytestreams</a>.
 * <p>
 * This class starts a local SOCKS5 server to support direct connections between two entities.
 * You can {@linkplain #setPort(int) set a port} of this local server, if you don't set a port, the default port 1080 is used.
 * <p>
 * It also allows you to {@linkplain #initiateSession(rocks.xmpp.core.Jid, String) initiate a byte stream session} with another entity.
 *
 * @author Christian Schudt
 */
public final class Socks5ByteStreamManager extends ByteStreamManager {

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final LocalSocks5Server localSocks5Server;

    /**
     * Guarded by "this".
     */
    private boolean localHostEnabled;

    private Socks5ByteStreamManager(final XmppSession xmppSession) {
        super(xmppSession);
        this.serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);

        this.localSocks5Server = new LocalSocks5Server();
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
                Socks5Protocol.establishClientConnection(socket, XmppUtils.hash((sessionId + requester.toEscapedString() + target.toEscapedString()).getBytes()), 0);
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
    protected void initialize() {
        super.initialize();
        xmppSession.addIQHandler(Socks5ByteStream.class, new AbstractIQHandler(this, AbstractIQ.Type.SET) {
            @Override
            protected IQ processRequest(IQ iq) {
                Socks5ByteStream socks5ByteStream = iq.getExtension(Socks5ByteStream.class);

                if (socks5ByteStream.getSessionId() == null) {
                    // If the request is malformed (e.g., the <query/> element does not include the 'sid' attribute), the Target MUST return an error of <bad-request/>.
                    return iq.createError(Condition.BAD_REQUEST);
                } else {
                    XmppUtils.notifyEventListeners(byteStreamListeners, new S5bEvent(Socks5ByteStreamManager.this, socks5ByteStream.getSessionId(), xmppSession, iq, socks5ByteStream.getStreamHosts()));
                    return null;
                }
            }
        });
    }

    /**
     * Indicates whether the local host is enabled.
     *
     * @return If enabled.
     */
    public synchronized boolean isLocalHostEnabled() {
        return localHostEnabled;
    }

    /**
     * Enables or disables the use of a local SOCKS5 host.
     *
     * @param enabled If enabled.
     */
    public synchronized void setLocalHostEnabled(boolean enabled) {
        this.localHostEnabled = enabled;
        if (!enabled) {
            localSocks5Server.stop();
        }
    }

    /**
     * Gets the port of the local host.
     *
     * @return The port.
     */
    public int getPort() {
        return localSocks5Server.getPort();
    }

    /**
     * Sets the port of the local host.
     *
     * @param port The port.
     */
    public void setPort(int port) {
        this.localSocks5Server.setPort(port);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled || !isLocalHostEnabled()) {
            // Only stop the server here, if we disable support.
            // It will be enabled, when needed.
            localSocks5Server.stop();
        }
    }

    /**
     * Discovers the SOCKS5 proxies.
     *
     * @return The proxies.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0065.html#disco">4. Discovering Proxies</a>
     */
    public List<StreamHost> discoverProxies() throws XmppException {
        Collection<Item> services = serviceDiscoveryManager.discoverServices(Socks5ByteStream.NAMESPACE);
        for (Item service : services) {
            IQ result = xmppSession.query(new IQ(service.getJid(), IQ.Type.GET, new Socks5ByteStream()));
            Socks5ByteStream socks5ByteStream = result.getExtension(Socks5ByteStream.class);
            if (socks5ByteStream != null) {
                return socks5ByteStream.getStreamHosts();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Gets a list of available stream hosts, including the discovered proxies and the local host.
     *
     * @return The stream hosts.
     * @throws java.io.IOException If not stream hosts could be found.
     */
    public List<StreamHost> getAvailableStreamHosts() throws IOException {

        List<StreamHost> streamHosts = new ArrayList<>();
        if (isLocalHostEnabled()) {
            // First add the local SOCKS5 server to the list of stream hosts.
            Jid requester = xmppSession.getConnectedResource();
            streamHosts.add(new StreamHost(requester, localSocks5Server.getAddress(), localSocks5Server.getPort()));
        }

        // Then discover proxies as alternative stream hosts.
        XmppException xmppException = null;
        try {
            streamHosts.addAll(discoverProxies());
        } catch (XmppException e) {
            // If no proxies are found, ignore the exception.
            xmppException = e;
        }
        if (streamHosts.isEmpty()) {
            throw new IOException("No stream hosts found.", xmppException);
        }
        return streamHosts;
    }

    /**
     * Initiates a SOCKS5 session with a target.
     *
     * @param target    The target.
     * @param sessionId The session id.
     * @return The SOCKS5 byte stream session.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @throws java.io.IOException                         If the byte stream session could not be established.
     */
    public ByteStreamSession initiateSession(Jid target, String sessionId) throws XmppException, IOException {

        if (isLocalHostEnabled()) {
            localSocks5Server.start();
        }
        List<StreamHost> streamHosts = getAvailableStreamHosts();

        Jid requester = xmppSession.getConnectedResource();

        // Create the hash, which will identify the socket connection.
        String hash = XmppUtils.hash((sessionId + requester.toEscapedString() + target.toEscapedString()).getBytes());
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
                Socks5Protocol.establishClientConnection(socket, hash, 0);

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

    @Override
    protected void dispose() {
        super.dispose();
        localSocks5Server.stop();
    }
}
