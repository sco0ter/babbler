/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.caps.client;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.extensions.caps.AbstractEntityCapabilitiesProtocol;
import rocks.xmpp.extensions.caps.EntityCapabilities1Protocol;
import rocks.xmpp.extensions.caps.EntityCapabilitiesCache;
import rocks.xmpp.extensions.caps.model.EntityCapabilities1;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.client.ClientServiceDiscoveryManager;
import rocks.xmpp.im.subscription.PresenceManager;

import java.util.List;

/**
 * @author Christian Schudt
 */
public class ClientEntityCapabilities1Protocol extends EntityCapabilities1Protocol {

    public ClientEntityCapabilities1Protocol(XmppSession xmppSession) {
        super(xmppSession.getManager(ServiceDiscoveryManager.class), xmppSession.getManager(EntityCapabilitiesCache.class));
        init(xmppSession, this);
    }

    public static void init(XmppSession xmppSession, AbstractEntityCapabilitiesProtocol entityCapabilitiesProtocol) {
        xmppSession.getManager(ClientServiceDiscoveryManager.class).addCapabilitiesChangeListener(evt -> {
            // If we haven't established a presence session yet, don't care about changes in service discovery.
            // If we change features during a presence session, update the verification string and resend presence.

            // https://xmpp.org/extensions/xep-0115.html#advertise:
            // "If the supported features change during a generating entity's presence session (e.g., a user installs an updated version of a client plugin), the application MUST recompute the verification string and SHOULD send a new presence broadcast."

            // Resend presence. This manager will add the caps extension later.
            PresenceManager presenceManager = xmppSession.getManager(PresenceManager.class);
            Presence lastPresence = presenceManager.getLastSentPresence();

            if (lastPresence != null) {
                // Whenever the verification string has changed, publish the info node.
                entityCapabilitiesProtocol.publishCapsNode();

                xmppSession.send(new Presence(null, lastPresence.getType(), lastPresence.getShow(), lastPresence.getStatuses(), lastPresence.getPriority(), null, null, lastPresence.getLanguage(), null, null));
            }
        });
        xmppSession.addSessionStatusListener(e -> {
            switch (e.getStatus()) {
                case AUTHENTICATED:
                    // As soon as we are authenticated, check if the server has advertised Entity Capabilities in its stream features.
                    List<EntityCapabilities1> serverCapabilities1 = xmppSession.getManager(StreamFeaturesManager.class).getFeatures(EntityCapabilities1.class);
                    // If yes, treat it as other caps.
                    if (!serverCapabilities1.isEmpty()) {
                        entityCapabilitiesProtocol.processCapabilitiesHashSet(serverCapabilities1.get(0).getCapabilityHashSet().iterator(), xmppSession.getDomain(), serverCapabilities1.get(0));
                    }
                    break;
                default:
                    break;
            }
        });
    }


}
