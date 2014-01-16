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
