/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
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

package org.xmpp.extension.stream.ibb;

import org.xmpp.XmppException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @author Christian Schudt
 */
final class IbbOutputStream extends OutputStream {

    private final IbbSession ibbSession;

    private final byte[] buffer;

    private int n;

    public IbbOutputStream(IbbSession ibbSession, int blockSize) {
        this.ibbSession = ibbSession;
        this.buffer = new byte[blockSize];
    }

    @Override
    public synchronized void write(int b) throws IOException {
        buffer[n++] = (byte) b;
        if (n == buffer.length) {
            flush();
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        super.flush();

        // If the buffer is empty, there's nothing to do.
        if (n == 0) {
            return;
        }
        try {
            ibbSession.send(Arrays.copyOf(buffer, n));
        } catch (XmppException e) {
            throw new IOException(e);
        } finally {
            n = 0;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        flush();
        ibbSession.close();
    }
}
