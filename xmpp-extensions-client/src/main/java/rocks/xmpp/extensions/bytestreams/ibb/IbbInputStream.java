/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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
 * @author Christian Schudt
 */
final class IbbInputStream extends InputStream {
    final BlockingQueue<InBandByteStream.Data> queue = new LinkedBlockingQueue<>();

    private final IbbSession ibbSession;

    int readTimeout;

    private byte[] buffer;

    private int n = 0;

    private volatile boolean closed;

    IbbInputStream(IbbSession ibbSession) {
        this.ibbSession = ibbSession;
    }

    @Override
    public synchronized int read() throws IOException {

        // If the buffer is empty, retrieve the next data packet and load it into the buffer.
        if (n == 0) {
            try {
                if (closed) {
                    return -1;
                }
                InBandByteStream.Data data = null;
                if (readTimeout <= 0) {
                    while (data == null) {
                        // If the stream has been closed and there's no more data to process, return -1.
                        if (closed && queue.isEmpty()) {
                            return -1;
                        }
                        // Let's see, if there's some data for me.
                        data = queue.poll(1, TimeUnit.SECONDS);
                    }
                } else {
                    data = queue.poll(readTimeout, TimeUnit.MILLISECONDS);
                    if (data == null) {
                        if (closed) {
                            return -1;
                        } else {
                            throw new SocketTimeoutException();
                        }
                    }
                }
                // Assign the new buffer.
                buffer = data.getBytes();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                InterruptedIOException ie = new InterruptedIOException();
                ie.initCause(e);
                throw ie;
            }
        }

        // Store the nth byte (as int) of the buffer. Then increment n.
        // Also convert the signed int value into an unsigned int by applying the & 0xFF bit operator.
        int b = (int) buffer[n++] & 0xFF;

        // If n is bigger than the buffer, reset n to 0 so that a new packet will be retrieved during the next read.
        if (n >= buffer.length) {
            n = 0;
        }
        return b;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            super.close();
            closed = true;
            try {
                ibbSession.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}
