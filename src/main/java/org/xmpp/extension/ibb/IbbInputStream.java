package org.xmpp.extension.ibb;

import org.xmpp.Connection;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Christian Schudt
 */
final class IbbInputStream extends InputStream {

    private final Connection connection;

    public IbbInputStream(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int read() throws IOException {
        return 0;
    }
}
