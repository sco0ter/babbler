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
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.xml.XMLConstants;

import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.net.ReaderInterceptor;
import rocks.xmpp.core.net.WriterInterceptor;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.extensions.sm.client.ClientStreamManager;
import rocks.xmpp.util.XmppStreamDecoder;
import rocks.xmpp.util.XmppStreamEncoder;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.QueuedScheduledExecutorService;
import rocks.xmpp.websocket.net.WebSocketConnection;

/**
 * Abstract base class for client-initiated WebSocket connections.
 *
 * <p>Client connections periodically send ping messages to check if the connection is alive.</p>
 */
abstract class AbstractWebSocketClientConnection extends WebSocketConnection {

    private static final ExecutorService EXECUTOR_SERVICE =
            Executors.newCachedThreadPool(XmppUtils.createNamedThreadFactory("WebSocket Ping Scheduler"));

    private final StreamFeaturesManager streamFeaturesManager;

    private final ClientStreamManager streamManager;

    private final Set<String> pings = new CopyOnWriteArraySet<>();

    private final URI uri;

    protected final XmppSession xmppSession;

    protected final List<WriterInterceptor> writerInterceptors;

    protected final List<ReaderInterceptor> readerInterceptors;

    protected ScheduledExecutorService executorService;

    /**
     * Guarded by "this".
     */
    protected Future<?> pingFuture;

    /**
     * Guarded by "this".
     */
    private Future<?> pongFuture;

    protected AbstractWebSocketClientConnection(final WebSocketConnectionConfiguration connectionConfiguration,
                                                final URI uri,
                                                final XmppSession xmppSession,
                                                final CompletionStage<Void> closeFuture) {
        super(connectionConfiguration, xmppSession, xmppSession::notifyException, closeFuture);
        this.uri = uri;
        this.xmppSession = xmppSession;
        this.writerInterceptors = new ArrayList<>(xmppSession.getWriterInterceptors());
        this.writerInterceptors.add(new XmppStreamEncoder(xmppSession.getConfiguration().getXmlOutputFactory(),
                xmppSession::createMarshaller, streamElement -> streamElement instanceof StreamFeatures
                || streamElement instanceof StreamError));
        this.readerInterceptors = new ArrayList<>(xmppSession.getReaderInterceptors());
        this.readerInterceptors.add(new XmppStreamDecoder(xmppSession.getConfiguration().getXmlInputFactory(),
                xmppSession::createUnmarshaller, XMLConstants.NULL_NS_URI));
        this.streamFeaturesManager = xmppSession.getManager(StreamFeaturesManager.class);
        this.streamManager = xmppSession.getManager(ClientStreamManager.class);
        this.streamManager.reset();
        this.executorService = new QueuedScheduledExecutorService(EXECUTOR_SERVICE);

        if (connectionConfiguration.getPingInterval() != null && !connectionConfiguration.getPingInterval().isNegative()
                && !connectionConfiguration.getPingInterval().isZero()) {
            pingFuture = this.executorService.scheduleAtFixedRate(() -> {
                // Send a WebSocket ping in an interval.
                synchronized (this) {
                    try {
                        if (!isClosed()) {
                            String uuid = UUID.randomUUID().toString();
                            if (pings.add(uuid)) {
                                // Send the ping with the UUID as application data, so that we can match it to the pong.
                                sendPing(ByteBuffer.wrap(uuid.getBytes(StandardCharsets.UTF_8)));

                                // Later check if the ping has been answered by a pong.
                                pongFuture = this.executorService.schedule(() -> {
                                            if (pings.remove(uuid)) {
                                                // Ping has not been removed by a corresponding pong (still unanswered).
                                                // Notify the session with an exception.
                                                xmppSession.notifyException(
                                                        new XmppException(
                                                                "No WebSocket pong received in time."));
                                            }
                                        }, xmppSession.getConfiguration().getDefaultResponseTimeout().toMillis(),
                                        TimeUnit.MILLISECONDS);
                            }
                        }
                    } catch (IOException e) {
                        xmppSession.notifyException(e);
                    }
                }
            }, 0, connectionConfiguration.getPingInterval().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public final InetSocketAddress getRemoteAddress() {
        return InetSocketAddress.createUnresolved(uri.getHost(), uri.getPort());
    }

    @Override
    public final boolean isUsingAcknowledgements() {
        return streamManager.isActive();
    }

    @Override
    protected final void restartStream() {
        open(sessionOpen);
    }

    /**
     * Sends a ping message.
     *
     * @param message The ping message.
     * @throws IOException If sending failed.
     */
    protected abstract void sendPing(ByteBuffer message) throws IOException;

    /**
     * This method must be called when a pong is received.
     *
     * @param message The pong message.
     */
    protected void pongReceived(ByteBuffer message) {
        // We received a pong from the server.
        // We can now remove the corresponding ping.
        final byte[] bytes = new byte[message.limit()];
        message.get(bytes);
        pings.remove(new String(bytes, StandardCharsets.UTF_8));
    }

    /**
     * Cancels any ping messages and executors. This method must be called after closing the WebSocket.
     */
    protected void doCloseConnection() {
        streamFeaturesManager.removeFeatureNegotiator(streamManager);

        synchronized (this) {
            if (pingFuture != null) {
                pingFuture.cancel(false);
                pingFuture = null;
            }
            if (pongFuture != null) {
                pongFuture.cancel(false);
                pongFuture = null;
            }
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    // (Re-)Cancel if current thread also interrupted
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                executorService = null;
            }
        }
    }
}
