package org.xmpp.extension.ping;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.Stanza;

import java.util.concurrent.TimeoutException;

/**
 * This class implements the application-level ping mechanism as specified in <a href="http://xmpp.org/extensions/xep-0199.html">XEP-0199: XMPP Ping</a>
 * <p>
 * For <a href="http://xmpp.org/extensions/xep-0199.html#s2c">Server-To-Client Pings</a> it automatically responds with a result (pong).
 * </p>
 * <p>
 * It also allows to ping the server (<a href="http://xmpp.org/extensions/xep-0199.html#c2s">Client-To-Server Pings</a>) or to ping other XMPP entities (<a href="http://xmpp.org/extensions/xep-0199.html#e2e">Client-to-Client Pings</a>).
 * </p>
 *
 * @author Christian Schudt
 */
public final class PingManager extends ExtensionManager {

    private static final Feature FEATURE = new Feature("urn:xmpp:ping");

    public PingManager(final Connection connection) {
        super(connection);
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {

                IQ iq = e.getIQ();

                if (iq.getType() == IQ.Type.GET && iq.getExtension(Ping.class) != null) {
                    if (isEnabled()) {
                        IQ result = iq.createResult();
                        connection.send(result);
                    } else {
                        IQ error = iq.createError(new Stanza.Error(new Stanza.Error.ServiceUnavailable()));
                        error.setExtension(new Ping());
                        connection.send(error);
                    }
                }
            }
        });
        setEnabled(false);
    }

    /**
     * Pings the given XMPP entity.
     *
     * @param jid The JID to ping.
     * @return True, if the entity responded with a result; or false if it does not support the ping protocol.
     * @throws TimeoutException If the ping timed out, i.e. no response has been received in time.
     */
    public boolean ping(Jid jid) throws TimeoutException {
        IQ iq = new IQ(IQ.Type.GET, new Ping());
        iq.setTo(jid);

        try {
            IQ result = connection.query(iq);
            return result.getError() == null;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Pings the connected server.
     *
     * @return True, if the server responded with a result; or false if it does not support the ping protocol.
     * @throws TimeoutException If the ping timed out, i.e. no response has been received in time.
     */
    public boolean pingServer() throws TimeoutException {
        return ping(null);
    }

    @Override
    protected Feature getFeature() {
        return FEATURE;
    }
}
