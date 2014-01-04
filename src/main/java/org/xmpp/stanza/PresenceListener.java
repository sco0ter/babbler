package org.xmpp.stanza;

/**
 * A listener interface, which allows to listen for received or sent presence stanzas.
 *
 * @author Christian Schudt
 * @see org.xmpp.Connection#addPresenceListener(PresenceListener)
 */
public interface PresenceListener extends StanzaListener<PresenceEvent> {
}
