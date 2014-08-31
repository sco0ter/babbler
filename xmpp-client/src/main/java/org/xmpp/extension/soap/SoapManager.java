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

package org.xmpp.extension.soap;

import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.caps.EntityCapabilitiesManager;
import org.xmpp.stanza.StanzaException;

/**
 * Manages XEP-0072: SOAP Over XMPP.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0072.html">XEP-0072: SOAP Over XMPP</a>
 */
public class SoapManager extends ExtensionManager {

    private static final String NAMESPACE = "http://jabber.org/protocol/soap";

    private final EntityCapabilitiesManager entityCapabilitiesManager;

    private SoapManager(XmppSession xmppSession) {
        super(xmppSession, NAMESPACE);
        entityCapabilitiesManager = xmppSession.getExtensionManager(EntityCapabilitiesManager.class);
    }

    /**
     * Indicates whether an entity supports XEP-0072: SOAP Over XMPP.
     *
     * @param jid The entity.
     * @return True, if it support XEP-0072: SOAP Over XMPP.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public boolean isSupported(Jid jid) throws XmppException {
        return entityCapabilitiesManager.isSupported(NAMESPACE, jid);
    }
}
