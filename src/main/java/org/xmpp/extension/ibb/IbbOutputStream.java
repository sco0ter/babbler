package org.xmpp.extension.ibb;

import org.xmpp.stanza.StanzaException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

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
        } catch (TimeoutException | StanzaException e) {
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
