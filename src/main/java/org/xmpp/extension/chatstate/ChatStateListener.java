package org.xmpp.extension.chatstate;

/**
 * @author Christian Schudt
 */
public interface ChatStateListener {

    void chatStateUpdated(ChatStateEvent e);
}
