package org.xmpp.stanza;

/**
 * A listener interface, which allows to listen for received or sent IQ stanzas.
 *
 * @author Christian Schudt
 * @see org.xmpp.Connection#addIQListener(IQListener)
 */
public interface IQListener extends StanzaListener<IQEvent> {
}
