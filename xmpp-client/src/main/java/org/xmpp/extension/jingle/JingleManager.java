package org.xmpp.extension.jingle;

import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.jingle.transports.TransportMethod;
import org.xmpp.extension.jingle.transports.ibb.InBandBytestreamsTransportMethod;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.client.IQ;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class JingleManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(JingleManager.class.getName());

    private final Set<JingleListener> jingleListeners = new CopyOnWriteArraySet<>();

    private JingleManager(final XmppSession xmppSession) {
        super(xmppSession);

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {
                    Jingle jingle = iq.getExtension(Jingle.class);
                    if (jingle != null) {
                        if (jingle.getAction() == Jingle.Action.SESSION_INITIATE) {
                            xmppSession.send(iq.createResult());

                            for (Jingle.Content content : jingle.getContents()) {
                                boolean hasUnsupportedTransport = true;
                                TransportMethod transportMethod = content.getTransportMethod();
                                if (transportMethod instanceof InBandBytestreamsTransportMethod) {
                                    hasUnsupportedTransport = false;
                                }
                                if (hasUnsupportedTransport) {
                                    Jingle jingleResponse = new Jingle(jingle.getSessionId(), Jingle.Action.SESSION_TERMINATE, new Jingle.Reason(new Jingle.Reason.UnsupportedTransports()));
                                    IQ response = new IQ(iq.getFrom(), IQ.Type.SET, jingleResponse);
                                    xmppSession.send(response);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public JingleSession initiateSession(Jid responder, long timeout, Jingle.Content... contents) throws Exception {

        if (responder == null) {
            throw new IllegalArgumentException("responder must not be null.");
        }
        if (contents.length == 0) {
            throw new IllegalArgumentException("no content provided.");
        }

        final String sessionId = UUID.randomUUID().toString();
        final Lock lock = new ReentrantLock();
        final Condition jingleAccepted = lock.newCondition();
        final JingleSession[] jingleSessions = new JingleSession[1];
        final Exception[] negotiationExceptions = new Exception[1];
        JingleListener jingleListener = new JingleListener() {
            @Override
            public void jingleReceived(JingleEvent e) {
                if (sessionId.equals(e.getSessionId())) {
                    lock.lock();
                    try {
                        // Auto-accept the incoming session
                        jingleSessions[0] = e.accept();
                        jingleAccepted.signal();
                    } catch (Exception e1) {
                        negotiationExceptions[0] = e1;
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };
        try {
            addJingleListener(jingleListener);
            // 6.2 Initiation
            IQ iq = new IQ(responder, IQ.Type.SET, new Jingle(sessionId, Jingle.Action.SESSION_INITIATE, xmppSession.getConnectedResource(), contents));
            xmppSession.query(iq);

            // 6.5 Acceptance
            // Wait until the responder accepted or declined the session.
            lock.lock();
            try {
                if (!jingleAccepted.await(timeout, TimeUnit.MILLISECONDS)) {
                    throw new NoResponseException("Responder did not accepted the session in time.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
            if (negotiationExceptions[0] != null) {
                throw negotiationExceptions[0];
            }

            return jingleSessions[0];
        } finally {
            removeJingleListener(jingleListener);
        }
    }

    /**
     * Adds a Jingle listener, which allows to listen for Jingle events.
     *
     * @param jingleListener The listener.
     * @see #removeJingleListener(JingleListener)
     */
    public final void addJingleListener(JingleListener jingleListener) {
        jingleListeners.add(jingleListener);
    }

    /**
     * Removes a previously added Jingle listener.
     *
     * @param jingleListener The listener.
     * @see #addJingleListener(JingleListener)
     */
    public final void removeJingleListener(JingleListener jingleListener) {
        jingleListeners.remove(jingleListener);
    }

    /**
     * Notifies the Jingle listeners.
     *
     * @param jingleEvent The Jingle event.
     */
    private void notifyJingleListeners(JingleEvent jingleEvent) {
        for (JingleListener jingleListener : jingleListeners) {
            try {
                jingleListener.jingleReceived(jingleEvent);
            } catch (Exception exc) {
                logger.log(Level.WARNING, exc.getMessage(), exc);
            }
        }
    }
}
