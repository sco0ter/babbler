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

package rocks.xmpp.websocket.net;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import rocks.xmpp.core.net.ConnectionConfiguration;
import rocks.xmpp.core.stream.StreamHandler;
import rocks.xmpp.core.stream.model.StreamElement;

/**
 * Generic WebSocket connection which is based on Jakarta WebSocket API.
 *
 * <p>The connection is implemented from a server point of view, i.e. during stream restarts no "open" element is
 * sent.</p>
 *
 * <p>Client implementations may use this class and pass their own message handler which restarts the stream.</p>
 */
public final class JakartaWebSocketConnection extends WebSocketConnection {

    private final Session session;

    /**
     * Creates
     *
     * @param session                 The session.
     * @param connectionConfiguration The configuration.
     * @param streamHandler           Handles inbound elements.
     * @param onException             Handles exceptions.
     * @param closeFuture             The close future, which is completed when the session is closed.
     * @param messageHandler          Optional message handler; if null the default handling of this connection is
     *                                used.
     */
    public JakartaWebSocketConnection(
            Session session,
            ConnectionConfiguration connectionConfiguration,
            StreamHandler streamHandler,
            Consumer<Throwable> onException,
            CompletionStage<Void> closeFuture,
            MessageHandler.Whole<StreamElement> messageHandler) {
        super(connectionConfiguration, streamHandler, onException, closeFuture);
        this.session = session;
        session.addMessageHandler(StreamElement.class, messageHandler != null ? messageHandler : this::handleElement);
    }

    @Override
    protected void restartStream() {
    }

    @Override
    public final CompletionStage<Void> closeConnection() {
        return CompletableFuture.runAsync(() -> {
            try {
                session.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public final InetSocketAddress getRemoteAddress() {
        return InetSocketAddress.createUnresolved(session.getRequestURI().getHost(), session.getRequestURI().getPort());
    }

    @Override
    public final CompletableFuture<Void> write(final StreamElement streamElement) {
        final CompletableFuture<Void> sendFuture = new CompletableFuture<>();
        session.getAsyncRemote().sendObject(streamElement, result -> {
            if (result.isOK()) {
                sendFuture.complete(null);
            } else {
                sendFuture.completeExceptionally(result.getException());
            }
        });
        return sendFuture;
    }

    @Override
    public final void flush() {
        try {
            session.getAsyncRemote().flushBatch();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder("WebSocket connection at ").append(session.getRequestURI());
        final String streamId = getStreamId();
        if (streamId != null) {
            sb.append(" (").append(streamId).append(')');
        }
        return sb.toString();
    }
}
