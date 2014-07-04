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

package org.xmpp.extension.privacy;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.IQ;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class manages privacy lists, which allow users to block communications from other users as described in <a href="http://xmpp.org/extensions/xep-0016.html">XEP-0016: Privacy Lists</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0016.html#protocol">2. Protocol</a></cite></p>
 * <p>Server-side privacy lists enable successful completion of the following use cases:</p>
 * <ul>
 * <li>Retrieving one's privacy lists.</li>
 * <li>Adding, removing, and editing one's privacy lists.</li>
 * <li>Setting, changing, or declining active lists.</li>
 * <li>Setting, changing, or declining the default list (i.e., the list that is active by default).</li>
 * <li>Allowing or blocking messages based on JID, group, or subscription type (or globally).</li>
 * <li>Allowing or blocking inbound presence notifications based on JID, group, or subscription type (or globally).</li>
 * <li>Allowing or blocking outbound presence notifications based on JID, group, or subscription type (or globally).</li>
 * <li>Allowing or blocking IQ stanzas based on JID, group, or subscription type (or globally).</li>
 * <li>Allowing or blocking all communications based on JID, group, or subscription type (or globally).</li>
 * </ul>
 * </blockquote>
 *
 * @author Christian Schudt
 */
public final class PrivacyListManager extends ExtensionManager {
    private final Set<PrivacyListListener> privacyListListeners = new CopyOnWriteArraySet<>();

    private PrivacyListManager(final XmppSession xmppSession) {
        super(xmppSession);
        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    privacyListListeners.clear();
                }
            }
        });
        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {
                    // In accordance with the semantics of IQ stanzas defined in XMPP Core [7], each connected resource MUST return an IQ result to the server as well.
                    xmppSession.send(iq.createResult());
                    e.consume();

                    Privacy privacy = iq.getExtension(Privacy.class);
                    if (privacy != null) {
                        List<PrivacyList> privacyLists = privacy.getPrivacyLists();
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

    /**
     * Adds a privacy list listener.
     *
     * @param privacyListListener The listener.
     * @see #removePrivacyListListener(PrivacyListListener)
     */
    public void addPrivacyListListener(PrivacyListListener privacyListListener) {
        privacyListListeners.add(privacyListListener);
    }

    /**
     * Removes a previously added privacy list listener.
     *
     * @param privacyListListener The listener.
     * @see #addPrivacyListListener(PrivacyListListener)
     */
    public void removePrivacyListListener(PrivacyListListener privacyListListener) {
        privacyListListeners.remove(privacyListListener);
    }

    /**
     * Gets the privacy lists.
     *
     * @return The privacy lists.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0016.html#protocol-retrieve">2.3 Retrieving One's Privacy Lists</a>
     */
    public Privacy getPrivacyLists() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, new Privacy()));
        return result.getExtension(Privacy.class);
    }

    /**
     * Gets a privacy list.
     *
     * @param name The privacy list name.
     * @return The privacy list.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0016.html#protocol-retrieve">2.3 Retrieving One's Privacy Lists</a>
     */
    public PrivacyList getPrivacyList(String name) throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, new Privacy(new PrivacyList(name))));
        Privacy privacy = result.getExtension(Privacy.class);
        if (privacy != null) {
            return privacy.getPrivacyLists().get(0);
        }
        return null;
    }

    /**
     * Changes the active list currently being applied.
     *
     * @param name The active list name.
     * @throws StanzaException     If the user attempts to set an active list but a list by that name does not exist, the server MUST return an {@code <item-not-found/>} stanza error to the user.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0016.html#protocol-active">2.4 Managing Active Lists</a>
     */
    public void setActiveList(String name) throws XmppException {
        Privacy privacy = new Privacy();
        privacy.setActiveName(name);
        setPrivacy(privacy);
    }

    /**
     * Declines the use of any active list.
     *
     * @throws StanzaException     If the request returned with an error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0016.html#protocol-active">2.4 Managing Active Lists</a>
     */
    public void declineActiveList() throws XmppException {
        setActiveList("");
    }

    /**
     * Change the default list (which applies to the user as a whole, not only the sending resource).
     *
     * @param name The list name.
     * @throws StanzaException     <ul>
     *                             <li>If the user attempts to change which list is the default list but the default list is in use by at least one connected resource other than the sending resource, the server MUST return a {@code <conflict/>} stanza error to the sending resource</li>
     *                             <li>If the user attempts to set a default list but a list by that name does not exist, the server MUST return an {@code <item-not-found/>} stanza error to the user</li>
     *                             </ul>
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0016.html#protocol-default">2.5 Managing the Default List</a>
     */
    public void setDefaultList(String name) throws XmppException {
        Privacy privacy = new Privacy();
        privacy.setDefaultName(name);
        setPrivacy(privacy);
    }

    /**
     * Declines the use of any default list.
     *
     * @throws StanzaException     If one connected resource attempts to decline the use of a default list for the user as a whole but the default list currently applies to at least one other connected resource, the server MUST return a {@code <conflict/>} error to the sending resource.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0016.html#protocol-default">2.5 Managing the Default List</a>
     */
    public void declineDefaultList() throws XmppException {
        setDefaultList("");
    }

    /**
     * Creates or edits a privacy list.
     *
     * @param privacyList The privacy list.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0016.html#protocol-edit">2.6 Editing a Privacy List</a>
     */
    public void updateList(PrivacyList privacyList) throws XmppException {
        setPrivacy(new Privacy(privacyList));
    }

    /**
     * Removes a privacy list.
     *
     * @param name The privacy list.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0016.html#protocol-remove">2.8 Removing a Privacy List</a>
     */
    public void removePrivacyList(String name) throws XmppException {
        setPrivacy(new Privacy(new PrivacyList(name)));
    }

    private void setPrivacy(Privacy privacy) throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, privacy));
    }
}
