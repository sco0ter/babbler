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

package rocks.xmpp.extensions.bytestreams.ibb;

import rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An input stream implementation for in-band bytestreams.
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
final class IbbInputStream extends InputStream {
    final BlockingQueue<InBandByteStream.Data> queue = new LinkedBlockingQueue<>();

    private final IbbSession ibbSession;

    private final long readTimeout;

    /**
     * Guarded by "this"
     */
    private byte[] buffer;

    /**
     * Guarded by "this"
     */
    private int n = 0;

    /**
     * Guarded by "this"
     */
    private boolean closed;

    IbbInputStream(IbbSession ibbSession, long readTimeout) {
        this.ibbSession = ibbSession;
        this.readTimeout = readTimeout;
    }

    @Override
    public int read() throws IOException {

        // If the buffer is empty, retrieve the next data packet and load it into the buffer.
        if (n == 0) {
            try {
                long timeout;
                synchronized (this) {
                    if (closed && queue.isEmpty()) {
                        return -1;
                    }
                    timeout = readTimeout;
                }
                InBandByteStream.Data data = null;
                if (timeout <= 0) {
                    while (data == null || data.getSequence() == -1) {
                        synchronized (this) {
                            // If the stream has been closed and there's no more data to process, return -1.
                            if (closed && queue.isEmpty()) {
                                return -1;
                            }
                        }
                        // Let's see, if there's some data for me.
                        data = queue.poll(1, TimeUnit.SECONDS);
                    }
                } else {
                    data = queue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (data == null || data.getSequence() == -1) {
                        synchronized (this) {
                            if (closed && queue.isEmpty()) {
                                return -1;
                            } else {
                                throw new SocketTimeoutException();
                            }
                        }
                    }
                }
                synchronized (this) {
                    // Assign the new buffer.
                    buffer = data.getBytes();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                InterruptedIOException ie = new InterruptedIOException();
                ie.initCause(e);
                throw ie;
            }
        }

        synchronized (this) {
            // Store the nth byte (as int) of the buffer. Then increment n.
            // Also convert the signed int value into an unsigned int by applying the & 0xFF bit operator.
            int b = (int) buffer[n++] & 0xFF;
            // If n is bigger than the buffer, reset n to 0 so that a new packet will be retrieved during the next read.
            if (n >= buffer.length) {
                n = 0;
            }
            return b;
        }
    }

    @Override
    public final void close() throws IOException {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        super.close();
        try {
            ibbSession.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
        // Add a "poison object" to indicate the end of stream and to release the blocking read() method.

        // From BlockingQueue JavaDoc:
        // A BlockingQueue does not intrinsically support any kind of "close" or "shutdown" operation
        // to indicate that no more items will be added.
        // The needs and usage of such features tend to be implementation-dependent.
        // For example, a common tactic is for producers to insert special end-of-stream or poison objects,
        // that are interpreted accordingly when taken by consumers.
        queue.add(new InBandByteStream.Data(new byte[0], ibbSession.getSessionId(), -1));
    }
}
