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

import rocks.xmpp.core.net.WriterInterceptor;
import rocks.xmpp.core.net.WriterInterceptorChain;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.extensions.sm.client.ClientStreamManager;
import rocks.xmpp.util.XmppStreamEncoder;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.QueuedScheduledExecutorService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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

/**
 * This class is responsible for opening and closing the XMPP stream as well as writing any XML elements to the stream.
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
final class XmppStreamWriter {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(XmppUtils.createNamedThreadFactory("Writer Thread"));

    private final XmppSession xmppSession;

    private final ScheduledExecutorService executor;

    private final XmppDebugger debugger;

    private final ClientStreamManager streamManager;

    private final XmppStreamEncoder streamEncoder;

    /**
     * Will be accessed only by the writer thread.
     */
    private OutputStreamWriter outputStreamWriter;

    /**
     * Indicates whether the stream has been opened. Will be accessed only by the writer thread.
     */
    private boolean streamOpened;

    XmppStreamWriter(ClientStreamManager streamManager, final XmppSession xmppSession) {
        this.xmppSession = xmppSession;
        this.debugger = xmppSession.getDebugger();
        this.executor = new QueuedScheduledExecutorService(EXECUTOR);
        this.streamManager = streamManager;
        this.streamEncoder = new XmppStreamEncoder(xmppSession.getConfiguration().getXmlOutputFactory(), xmppSession::createMarshaller, s -> false);
    }

    private WriterInterceptorChain newWriterChain() {
        List<WriterInterceptor> writerInterceptors = new ArrayList<>();
        if (debugger != null) {
            writerInterceptors.add(debugger);
        }
        writerInterceptors.add(streamEncoder);
        return new WriterInterceptorChain(writerInterceptors);
    }

    void initialize(int keepAliveInterval) {
        if (keepAliveInterval > 0) {
            executor.scheduleAtFixedRate(() -> {
                if (EnumSet.of(XmppSession.Status.CONNECTED, XmppSession.Status.AUTHENTICATED).contains(xmppSession.getStatus())) {
                    try {
                        outputStreamWriter.write(' ');
                        outputStreamWriter.flush();
                    } catch (Exception e) {
                        notifyException(e);
                    }
                }
            }, keepAliveInterval, keepAliveInterval, TimeUnit.SECONDS);
        }
    }

    CompletableFuture<Void> write(final StreamElement clientStreamElement, final boolean flush) {
        Objects.requireNonNull(clientStreamElement);
        return CompletableFuture.runAsync(() -> {
            try {
                WriterInterceptorChain writerInterceptorChain = newWriterChain();
                // When about to send a stanza, first put the stanza (paired with the current value of X) in an "unacknowledged" queue.
                if (clientStreamElement instanceof Stanza) {
                    streamManager.markUnacknowledged((Stanza) clientStreamElement);
                }
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
                WriterInterceptorChain writerInterceptorChain = newWriterChain();
                writerInterceptorChain.proceed(streamHeader, outputStreamWriter);
                outputStreamWriter.flush();
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
                    WriterInterceptorChain writerInterceptorChain = newWriterChain();
                    writerInterceptorChain.proceed(StreamHeader.CLOSING_STREAM_TAG, outputStreamWriter);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    outputStreamWriter = null;
                    streamOpened = false;
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

        xmppSession.notifyException(exception);
    }

    /**
     * Closes the stream by sending a closing {@code </stream:stream>} to the server.
     * This method waits until this task is completed, but not more than 0.25 seconds.
     */
    CompletableFuture<Void> shutdown() {
        return closeStream().whenCompleteAsync((aVoid, throwable) -> {
            executor.shutdown();
            try {
                // Wait for the closing stream element to be sent before we can close the socket.
                if (!executor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
    }
}
