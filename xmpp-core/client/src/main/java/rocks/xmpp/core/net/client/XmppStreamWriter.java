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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.WriterInterceptor;
import rocks.xmpp.core.net.WriterInterceptorChain;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.util.XmppStreamEncoder;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.QueuedScheduledExecutorService;

/**
 * This class is responsible for opening and closing the XMPP stream as well as writing any XML elements to the stream.
 *
 * <p>This class is thread-safe.</p>
 *
 * @author Christian Schudt
 */
final class XmppStreamWriter {

    private static final System.Logger logger = System.getLogger(XmppStreamWriter.class.getName());

    static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool(XmppUtils.createNamedThreadFactory("Writer Thread"));

    private final XmppSession xmppSession;

    private final Connection connection;

    private final ScheduledExecutorService executor;

    private final List<WriterInterceptor> writerInterceptors = new ArrayList<>();

    /**
     * Will be accessed only by the writer thread.
     */
    private OutputStreamWriter outputStreamWriter;

    /**
     * Indicates whether the stream has been opened. Will be accessed only by the writer thread.
     */
    private boolean streamOpened;

    XmppStreamWriter(final Iterable<WriterInterceptor> writerInterceptors, final Connection connection,
                     final XmppSession xmppSession) {
        this.xmppSession = xmppSession;
        this.connection = connection;
        writerInterceptors.forEach(this.writerInterceptors::add);
        this.writerInterceptors.add(new XmppStreamEncoder(xmppSession.getConfiguration().getXmlOutputFactory(),
                xmppSession::createMarshaller, s -> false));
        this.executor = new QueuedScheduledExecutorService(EXECUTOR);
    }

    void initialize(Duration keepAliveInterval) {
        if (keepAliveInterval != null && !keepAliveInterval.isNegative() && !keepAliveInterval.isZero()) {
            executor.scheduleAtFixedRate(() -> {
                if (EnumSet.of(XmppSession.Status.CONNECTED, XmppSession.Status.AUTHENTICATED)
                        .contains(xmppSession.getStatus())) {
                    try {
                        if (logger.isLoggable(System.Logger.Level.TRACE)) {
                            logger.log(System.Logger.Level.TRACE,
                                    "Sending whitespace ping, connection " + System.identityHashCode(connection));
                        }
                        outputStreamWriter.write(' ');
                        outputStreamWriter.flush();
                    } catch (Exception e) {
                        notifyException(e);
                    }
                }
            }, keepAliveInterval.toSeconds(), keepAliveInterval.toSeconds(), TimeUnit.SECONDS);
        }
    }

    CompletableFuture<Void> write(final StreamElement clientStreamElement, final boolean flush) {
        Objects.requireNonNull(clientStreamElement);
        return CompletableFuture.runAsync(() -> {
            try {
                WriterInterceptorChain writerInterceptorChain =
                        new WriterInterceptorChain(writerInterceptors, xmppSession, connection);
                writerInterceptorChain.proceed(clientStreamElement, outputStreamWriter);
                if (flush) {
                    outputStreamWriter.flush();
                }
            } catch (Exception e) {
                notifyException(e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    CompletionStage<Void> openStream(final OutputStream outputStream, final StreamHeader streamHeader) {
        return CompletableFuture.runAsync(() -> {
            this.outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            try {
                WriterInterceptorChain
                        writerInterceptorChain =
                        new WriterInterceptorChain(writerInterceptors, xmppSession, connection);
                writerInterceptorChain.proceed(streamHeader, outputStreamWriter);
                outputStreamWriter.flush();
                if (logger.isLoggable(System.Logger.Level.TRACE)) {
                    logger.log(System.Logger.Level.TRACE,
                            "Stream opened for connection " + System.identityHashCode(connection));
                }
                streamOpened = true;
            } catch (Exception e) {
                notifyException(e);
            }
        }, executor);
    }

    private CompletableFuture<Void> closeStream() {
        return CompletableFuture.runAsync(() -> {
            if (streamOpened) {
                // Close the stream.
                try {
                    WriterInterceptorChain writerInterceptorChain =
                            new WriterInterceptorChain(writerInterceptors, xmppSession, connection);
                    writerInterceptorChain.proceed(StreamHeader.CLOSING_STREAM_TAG, outputStreamWriter);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    outputStreamWriter = null;
                    streamOpened = false;
                    if (logger.isLoggable(System.Logger.Level.TRACE)) {
                        logger.log(System.Logger.Level.TRACE,
                                "Sent closing stream tag, closed stream writer for connection "
                                        + System.identityHashCode(connection));
                    }
                } catch (Exception e) {
                    notifyException(e);
                }
            }
        }, executor);
    }

    void flush() {
        executor.execute(() -> {
            try {
                outputStreamWriter.flush();
            } catch (IOException e) {
                xmppSession.notifyException(e);
            }
        });
    }

    /**
     * Shuts down the executors, cleans up resources and notifies the session about the exception.
     *
     * @param exception The exception which occurred during writing.
     */
    private void notifyException(Exception exception) {

        // Shutdown the executors.
        synchronized (this) {
            executor.shutdown();
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                    outputStreamWriter = null;
                } catch (Exception e) {
                    exception.addSuppressed(e);
                }
            }
        }
        if (logger.isLoggable(System.Logger.Level.TRACE)) {
            logger.log(System.Logger.Level.TRACE,
                    "Got exception, shutdown writer for connection " + System.identityHashCode(connection), exception);
        }
        xmppSession.notifyException(exception);
    }

    /**
     * Closes the stream by sending a closing {@code </stream:stream>} to the server. This method waits until this task
     * is completed, but not more than 0.25 seconds.
     */
    CompletableFuture<Void> shutdown() {
        return closeStream().whenCompleteAsync((aVoid, throwable) -> {
            executor.shutdown();
            try {
                // Wait for the closing stream element to be sent before we can close the socket.
                if (!executor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                    if (logger.isLoggable(System.Logger.Level.TRACE)) {
                        logger.log(System.Logger.Level.TRACE,
                                "Couldn't shutdown writer executor, shutdown now, for connection "
                                        + System.identityHashCode(connection));
                    }
                }
            } catch (InterruptedException e) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            if (logger.isLoggable(System.Logger.Level.TRACE)) {
                logger.log(System.Logger.Level.TRACE,
                        "Shutdown writer executor for connection " + System.identityHashCode(connection));
            }
        });
    }
}
