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

import rocks.xmpp.core.Session;
import rocks.xmpp.core.server.ServerConfiguration;
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
import rocks.xmpp.core.stanza.model.errors.Condition;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class StanzaProcessor {

    @Inject
    private ServerConfiguration serverConfiguration;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private InboundStanzaProcessor inboundStanzaProcessor;

    @Inject
    private Instance<OutboundMessageHandler> outboundMessageHandlers;

    @Inject
    private Instance<OutboundPresenceHandler> outboundPresenceHandlers;

    @Inject
    private Instance<OutboundIQHandler> outboundIQHandlers;

    public boolean process(Stanza stanza) {
        if (stanza instanceof Message) {
            MessageEvent messageEvent = new MessageEvent(sessionManager.getSession(stanza.getFrom()), (Message) stanza, false);
            outboundMessageHandlers.forEach(outboundMessageHandler -> outboundMessageHandler.handleOutboundMessage(messageEvent));
        } else if (stanza instanceof Presence) {
            PresenceEvent presenceEvent = new PresenceEvent(sessionManager.getSession(stanza.getFrom()), (Presence) stanza, false);
            outboundPresenceHandlers.forEach(outboundPresenceHandler -> outboundPresenceHandler.handleOutboundPresence(presenceEvent));
        } else if (stanza instanceof IQ) {
            IQEvent iqEvent = new IQEvent(sessionManager.getSession(stanza.getFrom()), (IQ) stanza, false);
            outboundIQHandlers.forEach(outboundIQHandler -> outboundIQHandler.handleOutboundIQ(iqEvent));
        }

        if (stanza instanceof Message) {
            if (stanza.getTo() == null) {
                stanza.setTo(stanza.getFrom().asBareJid());
            }
        }
        if (stanza instanceof IQ) {
            if (stanza.getTo() == null) {
                stanza.setTo(stanza.getFrom().asBareJid());
            }
        }
        if (stanza.getTo() == null || stanza.getTo().getDomain().endsWith(serverConfiguration.getDomain().toString())) {
            inboundStanzaProcessor.process(stanza);
        } else {
            // s2s not implemented.
            Stanza errorResponse = stanza.createError(Condition.REMOTE_SERVER_NOT_FOUND);
            Session session = sessionManager.getSession(errorResponse.getTo());
            if (session != null) {
                session.send(errorResponse);
            }
        }
        return true;
    }
}
