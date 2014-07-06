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

package org.xmpp.extension.invisible;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.caps.EntityCapabilitiesManager;
import org.xmpp.stanza.client.IQ;

/**
 * @author Christian Schudt
 */
public final class InvisibilityManager extends ExtensionManager {

    private volatile boolean invisible;

    private InvisibilityManager(XmppSession xmppSession) {
        super(xmppSession);
    }

    /**
     * Becomes invisible.
     *
     * @throws XmppException
     */
    public void becomeInvisible() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, new Invisible()));
        invisible = true;
    }

    /**
     * Becomes visible.
     *
     * @throws XmppException
     */
    public void becomeVisible() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, new Visible()));
        invisible = false;
    }

    /**
     * Indicates, whether the current session is invisible.
     *
     * @return True, of the current session is invisible.
     */
    public boolean isInvisible() {
        return invisible;
    }

    /**
     * Checks, whether invisibility is supported by the server.
     *
     * @return True, if invisibility is supported.
     * @throws XmppException
     */
    public boolean isSupported() throws XmppException {
        EntityCapabilitiesManager entityCapabilitiesManager = xmppSession.getExtensionManager(EntityCapabilitiesManager.class);
        return entityCapabilitiesManager.isSupported(Invisible.NAMESPACE, Jid.valueOf(xmppSession.getXmppServiceDomain()));
    }
}
