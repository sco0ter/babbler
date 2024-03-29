/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.core.net.client;

import java.io.InputStreamReader;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rocks.xmpp.core.net.ReaderInterceptor;
import rocks.xmpp.core.net.ReaderInterceptorChain;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.util.XmppStreamDecoder;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.QueuedExecutorService;

/**
 * This class is responsible for reading the inbound XMPP stream. It starts one "reader thread", which keeps reading the
 * XMPP document from the stream until the stream is closed or disconnected.
 *
 * <p>This class is thread-safe.</p>
 *
 * @author Christian Schudt
 */
final class XmppStreamReader {

    private static final System.Logger logger = System.getLogger(XmppStreamWriter.class.getName());

    private static final ExecutorService EXECUTOR_SERVICE =
            Executors.newCachedThreadPool(XmppUtils.createNamedThreadFactory("Reader Thread"));

    private final SocketConnection connection;

    private final XmppSession xmppSession;

    private final ExecutorService executorService;

    private final List<ReaderInterceptor> readerInterceptors = new ArrayList<>();

    private final XmppStreamDecoder xmppStreamDecoder;

    XmppStreamReader(final Iterable<ReaderInterceptor> readerInterceptors, String namespace,
                     final SocketConnection connection, XmppSession xmppSession) {
        this.connection = connection;
        this.xmppSession = xmppSession;
        this.executorService = new QueuedExecutorService(EXECUTOR_SERVICE);
        this.xmppStreamDecoder = new XmppStreamDecoder(xmppSession.getConfiguration().getXmlInputFactory(),
                xmppSession::createUnmarshaller, namespace);
        readerInterceptors.forEach(this.readerInterceptors::add);
        this.readerInterceptors.add(xmppStreamDecoder);
    }

    void startReading() {
        executorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ReaderInterceptorChain context = new ReaderInterceptorChain(readerInterceptors, xmppSession,
                                    connection);
                            context.proceed(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8),
                                    streamElement -> handle(streamElement, this));
                        } catch (Exception e) {
                            // shutdown the service, but don't await termination,
                            // in order to not block the reader thread.
                            executorService.shutdown();

                            // Recheck if there was a stream error. In this case don't report the original exception,
                            // but the stream error. This may happen, if the server doesn't gracefully close the stream
                            // after sending a stream error.
                            if (!connection.isClosed()) {
                                xmppSession.notifyException(e);
                            }
                        }
                    }
                }
        );
    }

    private void handle(StreamElement streamElement, final Runnable reader) {

        boolean wasClosed = connection.isClosed();
        if (connection.handleElement(streamElement)) {
            xmppStreamDecoder.restart();
            reader.run();
        }

        if (streamElement == StreamHeader.CLOSING_STREAM_TAG && !wasClosed) {
            // The server initiated a graceful disconnect by sending <stream:stream/> without a stream error.
            // In this case we want to reconnect, therefore throw an exception as if a stream error has occurred
            if (logger.isLoggable(Level.DEBUG)) {
                logger.log(Level.DEBUG, "Stream closed by server");
            }
            xmppSession.notifyException(new StreamErrorException(
                    new StreamError(Condition.UNDEFINED_CONDITION, "Stream closed by server", Locale.ENGLISH,
                            null)));
        }
    }

    /**
     * Shuts down the executor and waits maximal 0.5 seconds for the reader thread to finish, i.e. when the server sends
     * a {@code </stream:stream>} response.
     */
    void shutdown() {
        executorService.shutdown();
        // Wait for the closing </stream> element to be received.
        try {
            if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
