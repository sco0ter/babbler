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

package org.xmpp.extension.vcard;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * This manager allows to retrieve or save one owns vCard or retrieve another user's vCard.
 * <p>
 * The use cases are also described in <a href="http://xmpp.org/extensions/xep-0054.html">XEP-0054: vcard-temp</a> in more detail.
 * </p>
 *
 * @author Christian Schudt
 */
public final class VCardManager extends ExtensionManager {

    public VCardManager(Connection connection) {
        super(connection);
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return new ArrayList<>();
    }

    /**
     * Gets the vCard of the current user.
     *
     * @return The vCard.
     * @throws TimeoutException If the server did not answer in time.
     */
    public VCard getVCard() throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new VCard()));
        if (result.getType() == IQ.Type.RESULT) {
            return result.getExtension(VCard.class);
        }
        return null;
    }

    /**
     * Gets the vCard of another user.
     *
     * @param jid The user's JID.
     * @return The vCard of the other user or null, if it does not exist.
     * @throws TimeoutException If the server did not answer in time.
     */
    public VCard getVCard(Jid jid) throws TimeoutException {
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }
        IQ result = connection.query(new IQ(jid.toBareJid(), IQ.Type.GET, new VCard()));
        if (result.getType() == IQ.Type.RESULT) {
            return result.getExtension(VCard.class);
        }
        return null;
    }

    /**
     * Saves or updates a vCard.
     *
     * @param vCard The vCard.
     * @throws TimeoutException If the server did not answer in time.
     */
    public void setVCard(VCard vCard) throws TimeoutException {
        connection.query(new IQ(IQ.Type.SET, vCard));
    }
}
