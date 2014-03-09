package org.xmpp.extension.pubsub;

import org.xmpp.Jid;

/**
 * Represents the {@code <affiliation/>} element, which is used in both 'pubsub' and 'pubsub#owner' namespace.
 *
 * @author Christian Schudt
 */
public interface AffiliationNode {

    Affiliation getAffiliation();

    String getNode();

    Jid getJid();
}
