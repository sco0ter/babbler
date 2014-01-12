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

package org.xmpp.extension.privacylists;

import org.xmpp.Connection;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class PrivacyListManager extends ExtensionManager {
    private final Set<PrivacyListListener> privacyListListeners = new CopyOnWriteArraySet<>();

    public PrivacyListManager(final Connection connection) {
        super(connection);
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && iq.getType() == IQ.Type.SET) {
                    // In accordance with the semantics of IQ stanzas defined in XMPP Core [7], each connected resource MUST return an IQ result to the server as well.
                    IQ result = iq.createResult();
                    connection.send(result);

                    Privacy privacy = iq.getExtension(Privacy.class);
                    if (privacy != null) {
                        List<Privacy.PrivacyList> privacyLists = privacy.getPrivacyLists();
                        if (privacyLists.size() == 1) {
                            // Notify the listeners about the reception.
                            for (PrivacyListListener privacyListListener : privacyListListeners) {
                                privacyListListener.updated(new PrivacyListEvent(PrivacyListManager.this, privacyLists.get(0).getName()));
                            }
                        }
                    }
                }
            }
        });
    }

    public void addPrivacyListListener(PrivacyListListener privacyListListener) {
        privacyListListeners.add(privacyListListener);
    }

    public void removePrivacyListListener(PrivacyListListener privacyListListener) {
        privacyListListeners.remove(privacyListListener);
    }

    public Privacy.PrivacyList getPrivacyList(String name) throws TimeoutException, StanzaException {
        IQ iq = new IQ(IQ.Type.GET, new Privacy(new Privacy.PrivacyList(name)));
        IQ result = connection.query(iq);

        if (result.getError() != null) {
            throw new StanzaException(result.getError());
        }
        if (result.getType() == IQ.Type.RESULT) {
            Privacy privacy = result.getExtension(Privacy.class);
            if (privacy != null) {
                return privacy.getPrivacyLists().get(0);
            }
        }
        return null;
    }

    public void setActive(String name) throws TimeoutException, StanzaException {
        Privacy privacy = new Privacy();
        privacy.setActiveName(name);
        IQ iq = new IQ(IQ.Type.SET, privacy);
        IQ result = connection.query(iq);
        if (result.getError() != null) {
            throw new StanzaException(result.getError());
        }
    }

    public void setDefault(String name) throws TimeoutException, StanzaException {
        Privacy privacy = new Privacy();
        privacy.setDefaultName(name);
        setPrivacy(privacy);
    }

    public void updateList(Privacy.PrivacyList privacyList) throws TimeoutException, StanzaException {
        Privacy privacy = new Privacy(privacyList);
        setPrivacy(privacy);
    }

    public void removePrivacyList(String name) throws TimeoutException, StanzaException {
        Privacy privacy = new Privacy(new Privacy.PrivacyList(name));
        setPrivacy(privacy);
    }

    private void setPrivacy(Privacy privacy) throws TimeoutException, StanzaException {
        IQ iq = new IQ(IQ.Type.SET, privacy);
        IQ result = connection.query(iq);
        if (result.getError() != null) {
            throw new StanzaException(result.getError());
        }
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList();
    }
}
