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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.SameThreadExecutorService;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;

import java.io.IOException;
import java.net.Proxy;
import java.util.function.Consumer;

/**
 * @author Christian Schudt
 */
public final class TestXmppSession extends XmppSession {

    private final MockServer mockServer;

    private final Jid connectedResource;

    public TestXmppSession() {
        this(Jid.of("test@domain/resource"), new MockServer());
    }

    public TestXmppSession(Jid jid, MockServer mockServer) {
        this(jid, mockServer, XmppSessionConfiguration.getDefault());
    }

    public TestXmppSession(Jid jid, MockServer mockServer, XmppSessionConfiguration configuration) {
        super(null, configuration);
        connectedResource = jid;

        activeConnection = new Connection("hostname", 5222, Proxy.NO_PROXY) {

            @Override
            protected void restartStream() {

            }

            @Override
            public void send(StreamElement clientStreamElement) {
            }

            @Override
            public void connect(Jid from, String namespace, Consumer<Jid> onConnected) throws IOException {

            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getStreamId() {
                return null;
            }

            @Override
            public void close() throws IOException {
            }
        };
        stanzaListenerExecutor = iqHandlerExecutor = new SameThreadExecutorService();
        this.mockServer = mockServer;
        mockServer.registerConnection(this);

        // Auto-connect
        updateStatus(Status.AUTHENTICATED);
    }

    @Override
    public StreamElement send(StreamElement element) {
        StreamElement sent = super.send(element);
        if (mockServer != null && sent instanceof Stanza) {
            ((Stanza) sent).setFrom(connectedResource);
            mockServer.receive((Stanza) sent);
        }
        return element;
    }

    @Override
    public void connect(Jid from) throws XmppException {
    }

    @Override
    public IQ query(final IQ iq) throws XmppException {
        final IQ[] result = new IQ[1];

        final Consumer<IQEvent> iqListener = e -> {
            if (e.getIQ().isResponse() && e.getIQ().getId() != null && e.getIQ().getId().equals(iq.getId())) {
                result[0] = e.getIQ();
            }
        };

        addInboundIQListener(iqListener);
        send(iq);

        removeInboundIQListener(iqListener);
        IQ response = result[0];
        if (response.getType() == IQ.Type.ERROR) {
            throw new StanzaException(response);
        }
        return response;
    }

    @Override
    public IQ query(final IQ iq, long timeout) throws XmppException {
        // Ignore timeout for tests.
        return query(iq);
    }

    @Override
    public Jid getConnectedResource() {
        return connectedResource;
    }
}
