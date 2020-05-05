/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.session.server;

import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.OutboundIQHandler;
import rocks.xmpp.core.stanza.OutboundMessageHandler;
import rocks.xmpp.core.stanza.OutboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class OutboundStanzaProcessor {

    @Inject
    private Instance<OutboundMessageHandler> outboundMessageHandlers;

    @Inject
    private Instance<OutboundPresenceHandler> outboundPresenceHandlers;

    @Inject
    private Instance<OutboundIQHandler> outboundIQHandlers;

    @Inject
    private StanzaRouter stanzaRouter;

    public boolean process(Stanza stanza) {
        if (stanza instanceof Message) {
            MessageEvent messageEvent = new MessageEvent(this, (Message) stanza, false);
            outboundMessageHandlers.forEach(outboundMessageHandler -> outboundMessageHandler.handleOutboundMessage(messageEvent));
            stanzaRouter.route(stanza);
        } else if (stanza instanceof Presence) {
            PresenceEvent presenceEvent = new PresenceEvent(this, (Presence) stanza, false);
            outboundPresenceHandlers.forEach(outboundPresenceHandler -> outboundPresenceHandler.handleOutboundPresence(presenceEvent));
        } else if (stanza instanceof IQ) {
            IQEvent iqEvent = new IQEvent(this, (IQ) stanza, false);
            outboundIQHandlers.forEach(outboundIQHandler -> outboundIQHandler.handleOutboundIQ(iqEvent));
            stanzaRouter.route(stanza);
        }
        return true;
    }
}
