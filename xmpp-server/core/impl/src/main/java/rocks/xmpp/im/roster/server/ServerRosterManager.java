/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.im.roster.server;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.model.SubscriptionState;
import rocks.xmpp.im.roster.server.spi.RosterItemProvider;
import rocks.xmpp.session.server.SessionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Manages the roster by reading and persisting roster items from/to the underlying provider and emits roster pushes.
 *
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#roster">Managing the Roster</a>
 */
@ApplicationScoped
public class ServerRosterManager {

    @Inject
    private RosterItemProvider rosterItemProvider;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private ServerConfiguration serverConfiguration;

    /**
     * Adds or updates a roster item in the user's roster.
     *
     * @param username   The username.
     * @param rosterItem The roster item to add or update.
     */
    public void setRosterItem(final String username, final RosterItem rosterItem) {
        RosterItem item = rosterItemProvider.get(username, rosterItem.getJid());
        if (item != null) {
            //existingRosterItem.setName(rosterItem.getName());
            item.getGroups().clear();
            item.getGroups().addAll(rosterItem.getGroups());
            rosterItemProvider.update(username, item);
        } else {
            item = new Contact(rosterItem.getJid(), rosterItem.getName(), false, false, SubscriptionState.Subscription.NONE, rosterItem.getGroups());
            rosterItemProvider.create(username, item);
        }
        rosterPush(username, rosterItem, item.isPendingOut(), item.getSubscription());
    }

    /**
     * Gets the roster item.
     *
     * @param username The user.
     * @param contact  The contact.
     * @return The roster item or null, if it does not exist.
     */
    public RosterItem getRosterItem(String username, Jid contact) {
        return rosterItemProvider.get(username, contact);
    }

    /**
     * Gets all roster items in the user's roster.
     *
     * @param username The user.
     * @return The roster items.
     */
    public Collection<? extends RosterItem> getRosterItems(String username) {
        return rosterItemProvider.getRosterItems(username);
    }

    /**
     * Deletes a roster item in the user's roster.
     *
     * @param username The username.
     * @param jid      The contact's JID.
     * @return The deleted roster item or {@code null} if it does not exist.
     */
    public RosterItem delete(String username, Jid jid) {
        RosterItem rosterItem = rosterItemProvider.delete(username, jid);
        if (rosterItem != null) {
            rosterPush(username, rosterItem, false, SubscriptionState.Subscription.REMOVE);

            // In addition, the user's server might need to generate one or more subscription-related presence stanzas, as follows:
            if (rosterItem.getSubscription().userHasSubscriptionToContact()) {
                // If the user has a presence subscription to the contact, then the user's server MUST send a presence stanza
                // of type "unsubscribe" to the contact (in order to unsubscribe from the contact's presence).
                Presence presence = new Presence();
                presence.setFrom(serverConfiguration.getDomain().withLocal(username));
                presence.setTo(jid);
                presence.setType(Presence.Type.UNSUBSCRIBE);
                // TODO send
            }
            if (rosterItem.getSubscription().contactHasSubscriptionToUser()) {
                // If the contact has a presence subscription to the user, then the user's server MUST send a presence stanza
                // of type "unsubscribed"to the contact (in order to cancel the contact's subscription to the user).
                Presence presence = new Presence();
                presence.setFrom(serverConfiguration.getDomain().withLocal(username));
                presence.setTo(jid);
                presence.setType(Presence.Type.UNSUBSCRIBED);
                // TODO send
            }
        }
        return rosterItem;
    }

    private void rosterPush(String username, RosterItem rosterItem, boolean isPendingOut, SubscriptionState.Subscription subscriptionState) {
        Contact contact = new Contact(rosterItem.getJid(), rosterItem.getName(), isPendingOut, false, subscriptionState, rosterItem.getGroups());
        sessionManager.getUserSessions(serverConfiguration.getDomain().withLocal(username)).forEach(session -> session.send(IQ.set(session.getRemoteXmppAddress(), new Roster(contact))));
    }
}
