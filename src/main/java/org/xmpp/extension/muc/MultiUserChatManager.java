package org.xmpp.extension.muc;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.stanza.Presence;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Schudt
 */
public final class MultiUserChatManager extends ExtensionManager {

    private static final String FEATURE = "http://jabber.org/protocol/muc";

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    protected MultiUserChatManager(Connection connection) {
        super(connection, "http://jabber.org/protocol/muc");
        connection.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {

            }
        });
        this.serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
    }

    public Set<Identity> getMucServices() throws XmppException {
        ItemNode itemDiscovery = serviceDiscoveryManager.discoverItems(null);
        Set<Identity> identities = new HashSet<>();

        for (Item item : itemDiscovery.getItems()) {
            InfoNode infoDiscovery = serviceDiscoveryManager.discoverInformation(item.getJid());
            if (infoDiscovery.getFeatures().contains(new Feature(FEATURE))) {
                identities.addAll(infoDiscovery.getIdentities());
            }
        }
        return identities;
    }

    public List<Item> getPublicRooms(Jid service) throws XmppException {
        ItemNode itemDiscovery = serviceDiscoveryManager.discoverItems(service);
        return itemDiscovery.getItems();
    }

    public void enterRoom(String room, String service, String nick) {
        Presence presence = new Presence();
        presence.setTo(new Jid(room, service, nick));
        presence.getExtensions().add(new Muc());
        connection.send(presence);
    }
}
