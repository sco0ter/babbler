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
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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

        activeConnection = new Connection(null, TcpConnectionConfiguration.builder().build()) {

            @Override
            protected void restartStream() {
            }

            @Override
            public Future<?> send(StreamElement clientStreamElement) {
                return CompletableFuture.completedFuture(null);
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
            public boolean isUsingAcknowledgements() {
                return false;
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
    public void connect(Jid from) throws XmppException {

    }

    @Override
    public Future<?> send(StreamElement element) {
        Future<?> future = super.send(element);
        if (mockServer != null && element instanceof Stanza) {
            ((Stanza) element).setFrom(connectedResource);
            mockServer.receive((Stanza) element);

        }
        return future;
    }

    @Override
    public final Trackable<IQ> sendIQ(final IQ stanza) {
        Future<?> future = super.send(stanza);
        if (mockServer != null) {
            stanza.setFrom(connectedResource);
            mockServer.receive(stanza);
        }
        return null;
    }

    @Override
    public final Trackable<Message> sendMessage(final Message stanza) {
        return trackAndSend(stanza);
    }

    @Override
    public final Trackable<Presence> sendPresence(final Presence stanza) {
        return trackAndSend(stanza);
    }

    @Override
    public Jid getConnectedResource() {
        return connectedResource;
    }
}
