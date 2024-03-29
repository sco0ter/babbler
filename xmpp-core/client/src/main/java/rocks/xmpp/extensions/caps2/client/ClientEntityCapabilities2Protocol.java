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

package rocks.xmpp.extensions.caps2.client;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.OutboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.caps.client.ClientEntityCapabilitiesSupport;
import rocks.xmpp.extensions.caps2.AbstractEntityCapabilities2Protocol;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;

/**
 * The Entity Caps 2.0 protocol implementation from a client perspective.
 *
 * <p>Clients attach the Entity Caps extension to outbound presence.</p>
 */
public final class ClientEntityCapabilities2Protocol extends AbstractEntityCapabilities2Protocol
        implements OutboundPresenceHandler {

    private final ClientEntityCapabilitiesSupport capsSupport;

    public ClientEntityCapabilities2Protocol(XmppSession xmppSession) {
        super(xmppSession.getManager(ServiceDiscoveryManager.class),
                xmppSession.getManager(EntityCapabilitiesManager.class));
        this.capsSupport = new ClientEntityCapabilitiesSupport(xmppSession, this);
    }

    @Override
    public final void handleOutboundPresence(PresenceEvent e) {
        capsSupport.handleOutboundPresence(e);
    }
}
