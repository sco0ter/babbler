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

package rocks.xmpp.extensions.time;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQListener;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.time.model.EntityTime;

import java.util.Date;
import java.util.TimeZone;

/**
 * This manager implements <a href="http://xmpp.org/extensions/xep-0202.html">XEP-0202: Entity Time</a>.
 * <p>
 * It automatically responds to entity time requests, with the system's current date and timezone information and allows to retrieve another entity's time.
 * </p>
 *
 * @author Christian Schudt
 */
public final class EntityTimeManager extends ExtensionManager implements IQListener {

    private EntityTimeManager(final XmppSession xmppSession) {
        super(xmppSession, EntityTime.NAMESPACE);
        xmppSession.addIQListener(this);
        setEnabled(true);
    }

    /**
     * Gets the time information (e.g. time zone) of another XMPP entity.
     *
     * @param jid The entity's JID.
     * @return The entity time or null if this protocol is not supported by the entity.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public EntityTime getEntityTime(Jid jid) throws XmppException {
        IQ result = xmppSession.query(new IQ(jid, IQ.Type.GET, new EntityTime()));
        return result.getExtension(EntityTime.class);
    }

    @Override
    public void handleIQ(IQEvent e) {
        IQ iq = e.getIQ();
        if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.GET && iq.getExtension(EntityTime.class) != null) {
            xmppSession.send(iq.createResult(new EntityTime(TimeZone.getDefault(), new Date())));
            e.consume();
        }
    }
}
