package org.xmpp.extension.ibb;

import org.xmpp.Connection;
import org.xmpp.stanza.IQ;

import java.util.EventObject;

/**
 * @author Christian Schudt
 */
public final class IbbEvent extends EventObject {

    private final Connection connection;

    private final IQ iq;

    private final Open open;

    public IbbEvent(Object source, Connection connection, IQ iq, Open open) {
        super(source);
        this.connection = connection;
        this.iq = iq;
        this.open = open;
    }

    public IbbSession accept() {
        connection.send(iq.createResult());
        return connection.getExtensionManager(InBandByteStreamManager.class).createInBandByteStream(iq.getFrom(), open.getBlockSize(), open.getSessionId());
    }

    public void reject() {

    }
}
