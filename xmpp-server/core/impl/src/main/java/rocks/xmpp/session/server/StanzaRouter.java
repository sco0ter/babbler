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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import rocks.xmpp.core.Session;
import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stanza.model.errors.Condition;

@ApplicationScoped
public class StanzaRouter {

    @Inject
    private ServerConfiguration serverConfiguration;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private InboundStanzaProcessor inboundStanzaProcessor;

    public void route(Stanza stanza) {
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
    }
}
