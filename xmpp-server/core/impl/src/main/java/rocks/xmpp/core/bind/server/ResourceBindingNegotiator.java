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

package rocks.xmpp.core.bind.server;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.server.ServerStreamFeatureNegotiator;
import rocks.xmpp.session.server.InboundClientSession;
import rocks.xmpp.im.roster.server.spi.IdGenerator;
import rocks.xmpp.session.server.SessionManager;

import javax.enterprise.inject.spi.CDI;
import java.security.Principal;
import java.util.Optional;

/**
 * Negotiates resource binding with the client.
 *
 * @author Christian Schudt
 */
public final class ResourceBindingNegotiator extends ServerStreamFeatureNegotiator<Bind> {

    private final InboundClientSession session;

    private IdGenerator resourceIdentifierGenerator;

    public ResourceBindingNegotiator(InboundClientSession session) {
        super(Bind.class);
        this.session = session;
        this.resourceIdentifierGenerator = CDI.current().select(IdGenerator.class).get();
    }

    @Override
    public final Bind createStreamFeature() {
        return new Bind();
    }

    @Override
    public final StreamNegotiationResult processNegotiation(Object element) {
        if (element instanceof IQ) {
            IQ iq = (IQ) element;
            Bind bind = iq.getExtension(Bind.class);
            final String resource;
            if (bind != null) {
                if (bind.getResource() == null || bind.getResource().equals("")) {
                    resource = resourceIdentifierGenerator.generateId();
                } else {
                    resource = bind.getResource();
                }
                Optional<Principal> principal = session.getPrincipal();
                if (principal.isPresent()) {
                    try {
                        Jid jid = Jid.of(principal.get().getName(), "domain", resource);
                        session.setAddress(jid);
                        CDI.current().select(SessionManager.class).get().addSession(jid, session);
                        session.send(iq.createResult(new Bind(jid)));
                        return StreamNegotiationResult.SUCCESS;
                    } catch (IllegalArgumentException e) {
                        // If the client submitted a bad resource identifier, send a bad request error in accordance with:
                        // https://xmpp.org/rfcs/rfc6120.html#bind-clientsubmit-error-badrequest
                        session.send(iq.createError(Condition.BAD_REQUEST));
                    }
                } else {
                    session.send(iq.createError(Condition.NOT_AUTHORIZED));
                }
                return StreamNegotiationResult.SUCCESS;
            }
        }
        return StreamNegotiationResult.IGNORE;
    }

    @Override
    public boolean canProcess(Object element) {
        return false;
    }
}
