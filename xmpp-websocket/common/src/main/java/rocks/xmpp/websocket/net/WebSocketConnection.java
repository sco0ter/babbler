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

package rocks.xmpp.websocket.net;

import rocks.xmpp.core.net.AbstractConnection;
import rocks.xmpp.core.net.ConnectionConfiguration;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.websocket.model.Close;
import rocks.xmpp.websocket.model.Open;

import javax.websocket.Session;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * An XMPP WebSocket connection.
 *
 * @author Christian Schudt
 * @see <a href="https://tools.ietf.org/html/rfc7395">XMPP Subprotocol for WebSocket</a>
 * @since 0.7.0
 */
public class WebSocketConnection extends AbstractConnection {

    protected final Session session;

    private final CompletionStage<Void> closeFuture;

    protected SessionOpen sessionOpen;

    public WebSocketConnection(Session session, CompletableFuture<Void> closeFuture, ConnectionConfiguration connectionConfiguration) {
        super(connectionConfiguration);
        this.closeFuture = closeFuture;
        this.session = session;
        session.addMessageHandler(StreamElement.class, this::onRead);
    }

    protected void onRead(final StreamElement streamElement) {
        if (streamElement instanceof Open) {
            openedByPeer((Open) streamElement);
        } else if (streamElement instanceof Close) {
            closedByPeer();
        }
    }

    @Override
    protected final void restartStream() {
        open(sessionOpen);
    }

    @Override
    protected final CompletionStage<Void> closeStream() {
        return send(new Close());
    }

    @Override
    protected CompletionStage<Void> closeConnection() {
        return CompletableFuture.runAsync(() -> {
            try {
                session.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public final CompletionStage<Void> closeFuture() {
        return closeFuture;
    }

    @Override
    public CompletableFuture<Void> send(final StreamElement streamElement) {
        final CompletableFuture<Void> future = write(streamElement);
        flush();
        return future;
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
    public final CompletionStage<Void> open(final SessionOpen sessionOpen) {
        this.sessionOpen = sessionOpen;
        // Opens the stream
        return send(new Open(sessionOpen.getTo(), sessionOpen.getFrom(), sessionOpen.getId(), sessionOpen.getLanguage()));
    }

    @Override
    public final boolean isSecure() {
        return session.isSecure();
    }


}
