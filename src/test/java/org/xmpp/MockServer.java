package org.xmpp;

import org.xmpp.stanza.Stanza;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Schudt
 */
public class MockServer {

    private Map<Jid, Connection> connections = new HashMap<>();

    public void registerConnection(Connection connection) {
        connections.put(connection.getConnectedResource(), connection);
    }

    public void receive(Stanza stanza) {

        Connection toConnection = connections.get(stanza.getTo());
        if (toConnection != null) {
            try {
                toConnection.handleElement(stanza);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
