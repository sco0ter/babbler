/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.time;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.time.model.EntityTime;

import java.time.OffsetDateTime;

/**
 * This manager implements <a href="http://xmpp.org/extensions/xep-0202.html">XEP-0202: Entity Time</a>.
 * <p>
 * It automatically responds to entity time requests, with the system's current date and timezone information and allows to retrieve another entity's time.
 * </p>
 *
 * @author Christian Schudt
 */
public final class EntityTimeManager extends ExtensionManager {

    private EntityTimeManager(final XmppSession xmppSession) {
        super(xmppSession, EntityTime.NAMESPACE);
    }

    @Override
    protected void initialize() {
        xmppSession.addIQHandler(EntityTime.class, new AbstractIQHandler(this, AbstractIQ.Type.GET) {
            @Override
            protected IQ processRequest(IQ iq) {
                return iq.createResult(new EntityTime(OffsetDateTime.now()));
            }
        });
    }

    /**
     * Gets the time information (e.g. time zone) of another XMPP entity.
     *
     * @param jid The entity's JID.
     * @return The entity time or null if this protocol is not supported by the entity.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public OffsetDateTime getEntityTime(Jid jid) throws XmppException {
        IQ result = xmppSession.query(new IQ(jid, IQ.Type.GET, new EntityTime()));
        EntityTime entityTime = result.getExtension(EntityTime.class);
        return entityTime != null ? entityTime.getDateTime() : null;
    }
}
