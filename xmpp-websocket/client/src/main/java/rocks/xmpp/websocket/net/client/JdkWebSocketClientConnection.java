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

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import rocks.xmpp.core.net.ReaderInterceptorChain;
import rocks.xmpp.core.net.WriterInterceptorChain;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.model.StreamElement;

/**
 * A WebSocket connection which is based on JDK's {@link java.net.http.WebSocket}.
 */
final class JdkWebSocketClientConnection extends AbstractWebSocketClientConnection
        implements WebSocket.Listener {

    private final StringBuilder receivedMessaged = new StringBuilder();

    private WebSocket webSocket;

    private CompletableFuture<?> accumulatedMessage = new CompletableFuture<>();

    JdkWebSocketClientConnection(
            WebSocketConnectionConfiguration connectionConfiguration, URI uri,
            XmppSession xmppSession, CompletableFuture<Void> closeFuture) {
        super(connectionConfiguration, uri, xmppSession, closeFuture);
    }

    @Override
    public final CompletionStage<Void> write(StreamElement streamElement) {

        WriterInterceptorChain writerInterceptorChain =
                new WriterInterceptorChain(writerInterceptors, xmppSession, this);
        try (Writer writer = new StringWriter()) {
            writerInterceptorChain.proceed(streamElement, writer);
            return webSocket.sendText(writer.toString(), true).thenRun(() -> {
            });
        } catch (Exception e) {
            return CompletableFuture.failedStage(e);
        }
    }

    @Override
    public final void flush() {
    }

    @Override
    public final void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        webSocket.request(1);
    }

    @Override
    public final CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {

        receivedMessaged.append(data);
        webSocket.request(1);

        if (last) {
            ReaderInterceptorChain readerInterceptorChain =
                    new ReaderInterceptorChain(readerInterceptors, xmppSession, this);
            try {
                readerInterceptorChain.proceed(new StringReader(receivedMessaged.toString()), this::handleElement);
            } catch (Exception e) {
                xmppSession.notifyException(e);
            }
            receivedMessaged.setLength(0);
            receivedMessaged.trimToSize();
            accumulatedMessage.complete(null);
            CompletionStage<?> cf = accumulatedMessage;
            accumulatedMessage = new CompletableFuture<>();
            return cf;
        }
        return accumulatedMessage;
    }

    @Override
    public final CompletionStage<?> onPong(WebSocket webSocket,
                                           ByteBuffer message) {
        pongReceived(message);
        webSocket.request(1);
        return null;
    }

    @Override
    protected final CompletionStage<Void> closeConnection() {
        return webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").thenRun(this::doCloseConnection);
    }

    @Override
    public final void sendPing(ByteBuffer message) {
        webSocket.sendPing(message);
    }
}
