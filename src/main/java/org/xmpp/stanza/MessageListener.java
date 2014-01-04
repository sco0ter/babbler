package org.xmpp.stanza;

/**
 * A listener interface, which allows to listen for received or sent messages.
 *
 * @author Christian Schudt
 * @see org.xmpp.Connection#addMessageListener(MessageListener)
 */
public interface MessageListener extends StanzaListener<MessageEvent> {
}
