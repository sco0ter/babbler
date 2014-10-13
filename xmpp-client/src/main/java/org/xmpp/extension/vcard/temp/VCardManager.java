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

package org.xmpp.extension.vcard.temp;

import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.avatar.AvatarManager;
import org.xmpp.extension.vcard.temp.VCard;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.client.Presence;

/**
 * This manager allows to retrieve or save one owns vCard or retrieve another user's vCard.
 * <p>
 * The use cases are also described in <a href="http://xmpp.org/extensions/xep-0054.html">XEP-0054: vcard-temp</a> in more detail.
 * </p>
 *
 * @author Christian Schudt
 */
public final class VCardManager extends ExtensionManager {

    private VCardManager(final XmppSession xmppSession) {
        super(xmppSession, VCard.NAMESPACE);
        setEnabled(true);
    }

    /**
     * Gets the vCard of the current user.
     *
     * @return The vCard.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public VCard getVCard() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, new VCard()));
        return result.getExtension(VCard.class);
    }

    /**
     * Saves or updates a vCard.
     *
     * @param vCard The vCard.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void setVCard(VCard vCard) throws XmppException {
        if (vCard == null) {
            throw new IllegalArgumentException("vCard must not be null.");
        }
        // Update the vCard
        xmppSession.query(new IQ(IQ.Type.SET, vCard));

        // Then inform about the update by sending a presence. The avatar manager will add the update extension.
        AvatarManager avatarManager = xmppSession.getExtensionManager(AvatarManager.class);
        if (isEnabled() && avatarManager.isEnabled()) {
            Presence presence = xmppSession.getPresenceManager().getLastSentPresence();
            if (presence == null) {
                presence = new Presence();
            }
            presence.getExtensions().clear();
            xmppSession.send(presence);
        }
    }

    /**
     * Gets the vCard of another user.
     *
     * @param jid The user's JID.
     * @return The vCard of the other user or null, if it does not exist.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public VCard getVCard(Jid jid) throws XmppException {
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }
        IQ result = xmppSession.query(new IQ(jid.asBareJid(), IQ.Type.GET, new VCard()));
        return result.getExtension(VCard.class);
    }
}
