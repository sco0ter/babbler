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

package rocks.xmpp.extensions.invisible;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.invisible.model.InvisibleCommand;

/**
 * @author Christian Schudt
 */
public final class InvisibilityManager extends ExtensionManager {

    private boolean invisible;

    private InvisibilityManager(XmppSession xmppSession) {
        super(xmppSession);
    }

    /**
     * Becomes invisible.
     *
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public synchronized void becomeInvisible() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, InvisibleCommand.INVISIBLE));
        invisible = true;
    }

    /**
     * Becomes visible.
     *
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public synchronized void becomeVisible() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, InvisibleCommand.VISIBLE));
        invisible = false;
    }

    /**
     * Indicates, whether the current session is invisible.
     *
     * @return True, of the current session is invisible.
     */
    public synchronized boolean isInvisible() {
        return invisible;
    }

    /**
     * Checks, whether invisibility is supported by the server.
     *
     * @return True, if invisibility is supported.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public boolean isSupported() throws XmppException {
        EntityCapabilitiesManager entityCapabilitiesManager = xmppSession.getExtensionManager(EntityCapabilitiesManager.class);
        return entityCapabilitiesManager.isSupported(InvisibleCommand.NAMESPACE, Jid.valueOf(xmppSession.getDomain()));
    }
}
