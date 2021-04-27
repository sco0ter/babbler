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

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import rocks.xmpp.core.net.AbstractConnection;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.ConnectionConfiguration;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stream.StreamHandler;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.websocket.model.Close;
import rocks.xmpp.websocket.model.Open;

/**
 * Abstract base class for WebSocket connections.
 */
public abstract class WebSocketConnection extends AbstractConnection {

    private final CompletionStage<Void> closeFuture;

    protected SessionOpen sessionOpen;

    protected WebSocketConnection(ConnectionConfiguration connectionConfiguration,
                                  final StreamHandler streamHandler,
                                  final Consumer<Throwable> onException,
                                  final CompletionStage<Void> closeFuture) {
        super(connectionConfiguration, streamHandler, onException);
        this.closeFuture = closeFuture;
        closeFuture().whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                onException.accept(throwable);
            }
        });
    }

    @Override
    public final CompletionStage<Void> open(final SessionOpen sessionOpen) {
        this.sessionOpen = sessionOpen;
        // Opens the stream
        return send(
                new Open(sessionOpen.getTo(), sessionOpen.getFrom(), sessionOpen.getId(), sessionOpen.getLanguage()));
    }

    @Override
    public final CompletionStage<Void> send(final StreamElement streamElement) {
        return write(streamElement).thenRun(this::flush);
    }

    @Override
    public final boolean isSecure() {
        return getConfiguration().getChannelEncryption() == ChannelEncryption.DIRECT;
    }

    @Override
    public final CompletionStage<Void> closeFuture() {
        return closeFuture;
    }

    @Override
    protected final CompletionStage<Void> closeStream() {
        return send(new Close());
    }
}
