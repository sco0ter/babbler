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

package rocks.xmpp.session.server;

import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.InboundIQHandler;
import rocks.xmpp.core.stanza.InboundMessageHandler;
import rocks.xmpp.core.stanza.InboundPresenceHandler;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class InboundStanzaProcessor {

    @Inject
    private Instance<InboundMessageHandler> inboundMessageHandlers;

    @Inject
    private Instance<InboundPresenceHandler> inboundPresenceHandlers;

    @Inject
    private Instance<InboundIQHandler> inboundIQHandlers;

    @Inject
    private SessionManager sessionManager;

    public void process(Stanza stanza) {
        if (stanza instanceof Message) {
            MessageEvent messageEvent = new MessageEvent(sessionManager.getSession(stanza.getFrom()), (Message) stanza, true);
            inboundMessageHandlers.forEach(inboundPresenceHandler -> inboundPresenceHandler.handleInboundMessage(messageEvent));
        } else if (stanza instanceof Presence) {
            PresenceEvent presenceEvent = new PresenceEvent(sessionManager.getSession(stanza.getFrom()), (Presence) stanza, true);
            inboundPresenceHandlers.forEach(inboundPresenceHandler -> inboundPresenceHandler.handleInboundPresence(presenceEvent));
        } else if (stanza instanceof IQ) {
            IQEvent iqEvent = new IQEvent(sessionManager.getSession(stanza.getFrom()), (IQ) stanza, true);
            inboundIQHandlers.forEach(inboundPresenceHandler -> inboundPresenceHandler.handleInboundIQ(iqEvent));
        }
    }
}
