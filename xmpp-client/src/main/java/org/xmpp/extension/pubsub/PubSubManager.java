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

import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.extension.pubsub.event.Event;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Christian Schudt
 */
public final class PubSubManager extends ExtensionManager {

    final Set<PubSubListener> pubSubListeners = new CopyOnWriteArraySet<>();

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private PubSubManager(XmppSession xmppSession) {
        super(xmppSession);
        serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);

        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    Message message = e.getMessage();
                    Event event = message.getExtension(Event.class);
                    if (event != null) {
                        int i = 0;
                    }
                }
            }
        });
    }

    /**
     * Discovers the publish-subscribe services for the current connection.
     *
     * @return The list of publish-subscribe services.
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     */
    public Collection<PubSubService> getPubSubServices() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(null);
        Collection<PubSubService> pubSubServices = new ArrayList<>();

        for (Item item : itemNode.getItems()) {
            InfoNode infoNode = serviceDiscoveryManager.discoverInformation(item.getJid());
            if (infoNode.getFeatures().contains(new Feature(PubSub.NAMESPACE))) {
                pubSubServices.add(new PubSubService(item.getJid(), xmppSession, serviceDiscoveryManager));
            }
        }
        return pubSubServices;
    }

    /**
     * Creates a pubsub service.
     *
     * @param service The pubsub service address.
     * @return The pubsub service.
     */
    public PubSubService createPubSubService(Jid service) {
        return new PubSubService(service, xmppSession, serviceDiscoveryManager);
    }

    /**
     * Creates a personal eventing service.
     *
     * @return The personal eventing service.
     * @see <a href="http://xmpp.org/extensions/xep-0163.html">XEP-0163: Personal Eventing Protocol</a>
     */
    public PubSubService createPersonalEventingService() {
        return new PubSubService(xmppSession.getConnectedResource().asBareJid(), xmppSession, serviceDiscoveryManager);
    }

    /**
     * Adds a pubsub listener, which allows to listen for pubsub notifications.
     *
     * @param pubSubListener The listener.
     * @see #removePubSubListener(PubSubListener)
     */
    public void addPubSubListener(PubSubListener pubSubListener) {
        pubSubListeners.add(pubSubListener);
    }

    /**
     * Removes a previously added pubsub listener.
     *
     * @param pubSubListener The listener.
     * @see #addPubSubListener(PubSubListener)
     */
    public void removePubSubListener(PubSubListener pubSubListener) {
        pubSubListeners.remove(pubSubListener);
    }
}
