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

package org.xmpp.extension.pubsub;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.stanza.StanzaException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class PubSubManager extends ExtensionManager {

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private PubSubManager(Connection connection) {
        super(connection);
        serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
    }

    /**
     * Discovers the publish-subscribe services for the current connection.
     *
     * @return The list of publish-subscribe services.
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     */
    public List<Jid> discoverPubSubServices() throws XmppException {
        List<Jid> result = new ArrayList<>();
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(null);
        for (Item item : itemNode.getItems()) {
            InfoNode infoNode = serviceDiscoveryManager.discoverInformation(item.getJid());
            if (infoNode.getFeatures().contains(new Feature(PubSub.NAMESPACE))) {
                result.add(item.getJid());
            }
        }
        return result;
    }

    /**
     * @param service The pubsub service address.
     * @return The pubsub service.
     */
    public PubSubService createPubSubService(Jid service) {
        return new PubSubService(service, connection, serviceDiscoveryManager);
    }

    /**
     * @return The personal eventing service.
     * @see <a href="http://xmpp.org/extensions/xep-0163.html">XEP-0163: Personal Eventing Protocol</a>
     */
    public PubSubService createPersonalEventingService() {
        return new PubSubService(connection.getConnectedResource().toBareJid(), connection, serviceDiscoveryManager);
    }
}
