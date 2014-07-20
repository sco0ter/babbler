package org.xmpp.extension.jingle;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class JingleSession {

    private static final Logger logger = Logger.getLogger(JingleManager.class.getName());

    private final Set<JingleListener> jingleListeners = new CopyOnWriteArraySet<>();

    private final String sessionId;

    public JingleSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
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
    void notifyJingleListeners(JingleEvent jingleEvent) {
        for (JingleListener jingleListener : jingleListeners) {
            try {
                jingleListener.jingleReceived(jingleEvent);
            } catch (Exception exc) {
                logger.log(Level.WARNING, exc.getMessage(), exc);
            }
        }
    }
}
