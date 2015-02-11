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

package rocks.xmpp.extensions.jingle.transports.s5b;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.bytestreams.s5b.Socks5ByteStreamManager;
import rocks.xmpp.extensions.bytestreams.s5b.model.StreamHost;
import rocks.xmpp.extensions.jingle.JingleSession;
import rocks.xmpp.extensions.jingle.transports.TransportNegotiator;
import rocks.xmpp.extensions.jingle.transports.s5b.model.S5bTransportMethod;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author Christian Schudt
 */
public final class S5bTransportNegotiator extends TransportNegotiator<S5bTransportMethod> {

    private final Socks5ByteStreamManager socks5ByteStreamManager;

    private XmppSession xmppSession;

    protected S5bTransportNegotiator(JingleSession jingleSession, XmppSession xmppSession) {
        super(jingleSession);
        this.xmppSession = xmppSession;
        this.socks5ByteStreamManager = xmppSession.getExtensionManager(Socks5ByteStreamManager.class);
    }


    private S5bTransportMethod createTransport() throws IOException {
        S5bTransportMethod s5bTransportMethod = new S5bTransportMethod();
        List<StreamHost> streamHosts = socks5ByteStreamManager.getAvailableStreamHosts();

        for (StreamHost streamHost : streamHosts) {
            s5bTransportMethod.getCandidates().add(new S5bTransportMethod.Candidate(UUID.randomUUID().toString(), streamHost.getHost(), streamHost.getJid(), 0, streamHost.getJid().equals(xmppSession.getConnectedResource()) ? S5bTransportMethod.Candidate.Type.DIRECT : S5bTransportMethod.Candidate.Type.PROXY, streamHost.getPort()));
        }
        return s5bTransportMethod;
    }
}
