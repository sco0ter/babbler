package org.xmpp.extension.rosterx;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.im.Roster;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.Presence;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class RosterExchangeManager extends ExtensionManager {
    public RosterExchangeManager(Connection connection) {
        super(connection);
    }

    public void suggestContactAddition(Jid jid, List<Roster.Contact> contacts) throws TimeoutException {
        RosterExchange rosterExchange = new RosterExchange();
        for (Roster.Contact contact : contacts) {
            RosterExchange.Item rosterItem = new RosterExchange.Item(contact.getJid(), contact.getName(), contact.getGroups(), RosterExchange.Item.Action.ADD);
            rosterExchange.getItems().add(rosterItem);
        }
        // http://xmpp.org/extensions/xep-0144.html#stanza
        Presence presence = connection.getPresenceManager().getPresence(jid);
        if (presence.isAvailable()) {
            IQ iq = new IQ(IQ.Type.SET, rosterExchange);
            iq.setTo(presence.getFrom());
            connection.query(iq);
        } else {
            Message message = new Message(jid, Message.Type.NORMAL);
            connection.send(message);
        }
    }

    @Override
    protected Collection<Feature> getFeatures() {
        return Arrays.asList();
    }
}
