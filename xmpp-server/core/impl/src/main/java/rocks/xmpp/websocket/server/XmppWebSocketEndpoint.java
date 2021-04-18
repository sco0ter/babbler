/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.websocket.server;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.enterprise.inject.spi.CDI;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.session.server.InboundClientSession;
import rocks.xmpp.websocket.codec.XmppWebSocketEncoder;
import rocks.xmpp.websocket.net.JakartaWebSocketConnection;

/**
 * The WebSocket endpoint for XMPP. It listens for new inbound WebSocket connections, creates XMPP sessions from them
 * and also handles the closing of the sessions.
 *
 * @author Christian Schudt
 */
public final class XmppWebSocketEndpoint extends Endpoint {

    @Override
    public void onOpen(Session session, EndpointConfig config) {

        CompletableFuture<Void> closeFuture = new CompletableFuture<>();
        // A new WebSocket session has been created.
        // Associate it with a WebSocketConnection.

        InboundClientSession xmppSession = CDI.current().select(InboundClientSession.class).get();
        JakartaWebSocketConnection connection =
                new JakartaWebSocketConnection(session, null, xmppSession::handleElement, null, closeFuture, null);

        xmppSession.setConnection(connection);
        config.getUserProperties().put(XmppWebSocketEncoder.UserProperties.SESSION, xmppSession);
        config.getUserProperties().put(XmppWebSocketEncoder.UserProperties.CONNECTION, connection);

        // Store the XMPP session in the WebSocket's properties in order to close it again.
        session.getUserProperties().put(InboundClientSession.class.getName(), xmppSession);
        session.getUserProperties().put("closeFuture", closeFuture);
    }

    @Override
    public final void onClose(final Session session, final CloseReason closeReason) {
        // Retrieve the associated XMPP session from the WebSocket session and close it.
        InboundClientSession xmppSession =
                (InboundClientSession) session.getUserProperties().get(InboundClientSession.class.getName());
        @SuppressWarnings("unchecked")
        CompletableFuture<Void> closeFuture = (CompletableFuture<Void>) session.getUserProperties().get("closeFuture");
        closeFuture.complete(null);
        xmppSession.close();
    }

    @Override
    public final void onError(final Session session, final Throwable throwable) {
        // TODO: use a Logger.
        throwable.printStackTrace();
        // An error occurred, close the XMPP session with an internal server error.
        InboundClientSession xmppSession =
                (InboundClientSession) session.getUserProperties().get(InboundClientSession.class.getName());
        xmppSession.closeAsync(new StreamError(Condition.INTERNAL_SERVER_ERROR));
        // Then close the WebSocket session.
        try {
            session.close();
        } catch (IOException e) {
            // TODO: use a Logger.
            e.printStackTrace();
        }
    }
}
