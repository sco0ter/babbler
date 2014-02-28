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

package org.xmpp.extension.time;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaException;

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
public final class EntityTimeManager extends ExtensionManager {

    private EntityTimeManager(final Connection connection) {
        super(connection, "urn:xmpp:time");
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (iq.getExtension(EntityTime.class) != null && e.isIncoming() && iq.getType() == IQ.Type.GET) {
                    if (isEnabled()) {
                        IQ result = iq.createResult();
                        result.setExtension(new EntityTime(TimeZone.getDefault(), new Date()));
                        connection.send(result);
                    } else {
                        sendServiceUnavailable(iq);
                    }
                }
            }
        });
        setEnabled(true);
    }

    /**
     * Gets the time information (e.g. time zone) of another XMPP entity.
     *
     * @param jid The entity's JID.
     * @return The entity time or null if this protocol is not supported by the entity.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public EntityTime getEntityTime(Jid jid) throws XmppException {
        IQ result = connection.query(new IQ(jid, IQ.Type.GET, new EntityTime()));
        return result.getExtension(EntityTime.class);
    }
}
