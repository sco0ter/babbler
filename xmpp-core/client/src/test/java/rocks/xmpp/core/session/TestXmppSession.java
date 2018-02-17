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
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;

import java.util.concurrent.CompletableFuture;


/**
 * @author Christian Schudt
 */
public final class TestXmppSession extends XmppSession {

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

        addSendSucceededListener(element -> {
            if (mockServer != null && element instanceof Stanza) {
                ((Stanza) element).setFrom(connectedResource);
                mockServer.receive((Stanza) element);
            }
        });

        activeConnection = new Connection(null, TcpConnectionConfiguration.builder().build()) {

            @Override
            protected void restartStream() {
            }

            @Override
            public CompletableFuture<Void> send(StreamElement clientStreamElement) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void connect() {
            }

            @Override
            public void open(SessionOpen sessionOpen) {
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
            public CompletableFuture<Void> closeAsync() {
                return CompletableFuture.completedFuture(null);
            }
        };
        stanzaListenerExecutor = iqHandlerExecutor = new SameThreadExecutorService();
        mockServer.registerConnection(this);

        // Auto-connect
        updateStatus(Status.AUTHENTICATED);
    }

    @Override
    public void connect(Jid from) {

    }

    @Override
    public Jid getConnectedResource() {
        return connectedResource;
    }
}
