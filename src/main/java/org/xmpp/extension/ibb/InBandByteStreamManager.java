package org.xmpp.extension.ibb;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.Stanza;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public class InBandByteStreamManager extends ExtensionManager {
    private static final Feature FEATURE = new Feature("http://jabber.org/protocol/ibb");

    private static final Logger logger = Logger.getLogger(InBandByteStreamManager.class.getName());

    final Set<IbbListener> ibbListeners = new CopyOnWriteArraySet<>();


    public InBandByteStreamManager(final Connection connection) {
        super(connection);

        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    // If someone wants to create an IBB session with me.
                    if (iq.getType() == IQ.Type.SET && iq.getExtension(Open.class) != null) {
                        if (isEnabled()) {
                            // Notify the listeners.
                            for (IbbListener ibbListener : ibbListeners) {
                                try {
                                    ibbListener.streamRequested(new IbbEvent(InBandByteStreamManager.this));
                                } catch (Exception exc) {
                                    logger.log(Level.WARNING, exc.getMessage(), exc);
                                }
                            }
                        } else {
                            IQ result = iq.createError(new Stanza.Error(new Stanza.Error.ServiceUnavailable()));
                            connection.send(result);
                        }
                    }
                }
            }
        });
    }

    public IbbSession createInBandByteStream(Jid jid, short blockSize) {
        IQ iq = new IQ(IQ.Type.SET);

        iq.setTo(jid);
        iq.setExtension(new Open(blockSize, UUID.randomUUID().toString()));
        connection.send(iq);

        return null;
    }

    /**
     * Adds a IBB listener, which allows to listen for incoming IBB requests.
     *
     * @param ibbListener The listener.
     * @see #removeIbbListener(IbbListener)
     */
    public void addIbbListener(IbbListener ibbListener) {
        ibbListeners.add(ibbListener);
    }

    /**
     * Removes a previously added IBB listener.
     *
     * @param ibbListener The listener.
     * @see #addIbbListener(IbbListener)
     */
    public void removeIbbListener(IbbListener ibbListener) {
        ibbListeners.remove(ibbListener);
    }

    @Override
    protected Collection<Feature> getFeatures() {
        return Arrays.asList(FEATURE);
    }
}
