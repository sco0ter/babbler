package org.xmpp.extension.time;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.Stanza;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

/**
 * This manager implements <a href="http://xmpp.org/extensions/xep-0202.html">XEP-0202: Entity Time</a>.
 * <p>
 * It automatically responds to entity time requests, with the system's current date and timezone information and allows to retrieve another entity's time.
 * </p>
 *
 * @author Christian Schudt
 */
public final class EntityTimeManager extends ExtensionManager {

    private static final Feature FEATURE = new Feature("urn:xmpp:time");

    public EntityTimeManager(final Connection connection) {
        super(connection);
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (iq.getExtension(EntityTime.class) != null && e.isIncoming() && iq.getType() == IQ.Type.GET) {
                    if (isEnabled()) {
                        IQ result = iq.createResult();
                        result.setExtension(new EntityTime(TimeZone.getDefault(), new Date()));
                        connection.send(result);
                    } else {
                        IQ error = iq.createError(new Stanza.Error(new Stanza.Error.ServiceUnavailable()));
                        connection.send(error);
                    }
                }
            }
        });
        setEnabled(true);
    }

    /**
     * Gets the time information (e.g. time zone) of another XMPP entity.
     *
     * @param jid The entity's JID.
     * @return The entity time or null if this protocol is not supported by the entity.
     * @throws TimeoutException If the operation timed out.
     */
    public EntityTime getEntityTime(Jid jid) throws TimeoutException {
        IQ iq = new IQ(IQ.Type.GET, new EntityTime());
        iq.setTo(jid);
        IQ result = connection.query(iq);
        if (result.getType() == IQ.Type.RESULT) {
            return result.getExtension(EntityTime.class);
        }
        return null;
    }

    @Override
    protected Collection<Feature> getFeatures() {
        return Arrays.asList(FEATURE);
    }
}
