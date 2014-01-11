package org.xmpp.extension.ibb;

import org.xmpp.Connection;
import org.xmpp.stanza.IQ;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Christian Schudt
 */
final class IbbOutputStream extends OutputStream {

    private final Connection connection;

    private final byte[] buffer;

    private short writtenBytes;

    public IbbOutputStream(Connection connection, short blockSize) {
        this.connection = connection;
        this.buffer = new byte[blockSize];
    }

    @Override
    public void write(int b) throws IOException {

        buffer[writtenBytes++] = (byte) b;
        if (writtenBytes == buffer.length) {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        String bas64 = DatatypeConverter.printBase64Binary(buffer);
        IQ iq = new IQ(IQ.Type.SET, new Data());
    }
}
