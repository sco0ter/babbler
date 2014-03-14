/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    protected MultiUserChatManager(Connection connection) {
        super(connection, Muc.NAMESPACE);
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
            if (infoDiscovery.getFeatures().contains(new Feature(Muc.NAMESPACE))) {
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
