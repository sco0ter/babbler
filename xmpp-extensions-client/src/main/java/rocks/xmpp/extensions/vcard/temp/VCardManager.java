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

package rocks.xmpp.extensions.vcard.temp;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.AbstractPresence;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.subscription.PresenceManager;
import rocks.xmpp.extensions.avatar.AvatarManager;
import rocks.xmpp.extensions.vcard.temp.model.VCard;

import java.util.Objects;

/**
 * This manager allows to retrieve or save one owns vCard or retrieve another user's vCard.
 * <p>
 * The use cases are also described in <a href="http://xmpp.org/extensions/xep-0054.html">XEP-0054: vcard-temp</a> in more detail.
 * </p>
 *
 * @author Christian Schudt
 */
public final class VCardManager extends Manager {

    private VCardManager(final XmppSession xmppSession) {
        super(xmppSession);
    }

    /**
     * Gets the vCard of the current user.
     *
     * @return The vCard.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public VCard getVCard() throws XmppException {
        AbstractIQ result = xmppSession.query(new IQ(IQ.Type.GET, new VCard()));
        return result.getExtension(VCard.class);
    }

    /**
     * Saves or updates a vCard.
     *
     * @param vCard The vCard.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public void setVCard(VCard vCard) throws XmppException {
        // Update the vCard
        xmppSession.query(new IQ(IQ.Type.SET, vCard));

        // Then inform about the update by sending a presence. The avatar manager will add the update extension.
        AvatarManager avatarManager = xmppSession.getManager(AvatarManager.class);
        if (isEnabled() && avatarManager.isEnabled()) {
            AbstractPresence presence = xmppSession.getManager(PresenceManager.class).getLastSentPresence();
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
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public VCard getVCard(Jid jid) throws XmppException {
        Objects.requireNonNull(jid, "jid must not be null.");
        AbstractIQ result = xmppSession.query(new IQ(jid.asBareJid(), IQ.Type.GET, new VCard()));
        return result.getExtension(VCard.class);
    }
}
