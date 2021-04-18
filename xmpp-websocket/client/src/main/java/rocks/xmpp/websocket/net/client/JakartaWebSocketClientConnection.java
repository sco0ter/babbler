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

package rocks.xmpp.websocket.net.client;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.websocket.net.JakartaWebSocketConnection;

/**
 * A WebSocket connection which is based on the Jakarta WebSocket API.
 */
final class JakartaWebSocketClientConnection extends AbstractWebSocketClientConnection {

    private final JakartaWebSocketConnection jakartaWebSocketConnection;

    private final Session session;

    JakartaWebSocketClientConnection(
            Session session,
            CompletableFuture<Void> closeFuture, XmppSession xmppSession,
            WebSocketConnectionConfiguration connectionConfiguration, URI uri) {
        super(connectionConfiguration, uri, xmppSession, closeFuture);
        this.session = session;
        this.jakartaWebSocketConnection = new JakartaWebSocketConnection(session, connectionConfiguration, xmppSession,
                xmppSession::notifyException, closeFuture, this::handleElement);
        session.addMessageHandler(new PongHandler());
    }

    @Override
    protected CompletionStage<Void> closeConnection() {
        return jakartaWebSocketConnection.closeConnection();
    }

    @Override
    public CompletionStage<Void> write(StreamElement streamElement) {
        return jakartaWebSocketConnection.write(streamElement);
    }

    @Override
    public void flush() {
        jakartaWebSocketConnection.flush();
    }

    @Override
    protected void sendPing(ByteBuffer message) throws IOException {
        session.getBasicRemote().sendPing(message);
    }

    private final class PongHandler implements MessageHandler.Whole<PongMessage> {

        @Override
        public final void onMessage(final PongMessage message) {
            pongReceived(message.getApplicationData());
        }
    }
}
