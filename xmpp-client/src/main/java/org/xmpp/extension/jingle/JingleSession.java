package org.xmpp.extension.jingle;

/**
 * @author Christian Schudt
 */
public final class JingleSession {

    private final String sessionId;

    public JingleSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
