/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.session.server;

import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.InboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.im.roster.model.DefinedState;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.server.spi.RosterItemProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class PresenceHandler {

    @Inject
    private SessionManager sessionManager;

    @Inject
    private RosterItemProvider rosterItemProvider;

    @Inject
    private Instance<InboundPresenceHandler> inboundPresenceHandlers;

    public boolean process(Presence presence) {

        inboundPresenceHandlers.forEach(inboundPresenceHandler -> inboundPresenceHandler.handleInboundPresence(new PresenceEvent(sessionManager.getSession(presence.getFrom()), presence, true)));

        // RFC 6120 § 10.3.2.  Presence
        // If the server receives a presence stanza with no 'to' attribute,
        // it MUST broadcast it to the entities that are subscribed to the sending entity's presence
        if (presence.getTo() == null) {
            // The user's server MUST also broadcast initial presence from the user's newly available resource to all of the user's available resources,
            // including the resource that generated the presence notification in the first place (i.e., an entity is implicitly subscribed to its own presence)
            Stream<Session> userSessions = sessionManager.getUserSessions(presence.getFrom().asBareJid());
            userSessions.forEach(session -> {
                Presence copy = new Presence(session.getRemoteXmppAddress().asBareJid(), presence.getType(), presence.getShow(), presence.getStatuses(), presence.getPriority(), presence.getId(), presence.getFrom(), presence.getLanguage(), presence.getExtensions(), presence.getError());
                session.send(copy);
            });
        } else if (presence.getTo().isBareJid()) {
            // RFC 6121 § 8.5.2.1.2.  Presence
            // For a presence stanza with no type or of type "unavailable", the server MUST deliver it to all available resources.
            if (presence.getType() == null || presence.getType() == Presence.Type.UNAVAILABLE) {
                Stream<Session> userSessions = sessionManager.getUserSessions(presence.getFrom().asBareJid());
                userSessions.forEach(session -> {
                    Presence copy = new Presence(session.getRemoteXmppAddress().asBareJid(), presence.getType(), presence.getShow(), presence.getStatuses(), presence.getPriority(), presence.getId(), presence.getFrom(), presence.getLanguage(), presence.getExtensions(), presence.getError());
                    session.send(copy);
                });
            }
            // In all cases, the server MUST NOT rewrite the 'to' attribute
            // (i.e., it MUST leave it as <localpart@domainpart>rather than change it to <localpart@domainpart/resourcepart>).
        } else if (presence.getTo().isFullJid()) {
            // RFC 6121 § 8.5.3.2.2.  Presence
            if (presence.getType() == null || presence.getType() == Presence.Type.UNAVAILABLE) {
                // For a presence stanza with no 'type' attribute or a 'type' attribute of "unavailable", the server MUST silently ignore the stanza.
            }
        }

        if (presence.getType() == Presence.Type.SUBSCRIBE) {
            // RFC 6121 § 3.1.2.  Server Processing of Outbound Subscription Request
            // If the JID is of the form <contact@domainpart/resourcepart> instead of <contact@domainpart>,
            // the user's server SHOULD treat it as if the request had been directed to the contact's bare JID and modify the 'to' address accordingly.
            if (presence.getTo().isFullJid()) {
                presence.setTo(presence.getTo().asBareJid());
            }
            String username = presence.getFrom().getLocal();

            RosterItem rosterItem = rosterItemProvider.get(username, presence.getTo());
            if (rosterItem != null && (rosterItem.getSubscription() == Contact.Subscription.TO || rosterItem.getSubscription() == Contact.Subscription.BOTH)) {
                // If the contact exists and the user already has a subscription to the contact's presence, then the contact's server MUST auto-reply on behalf of the contact by sending a presence stanza of type "subscribed" from the contact's bare JID to the user's bare JID.
                Presence presence1 = new Presence(presence.getFrom(), Presence.Type.SUBSCRIBED, null, presence.getId());
                presence1.setFrom(presence.getTo());
            } else {
                DefinedState definedState = DefinedState.valueOf(rosterItem);
                DefinedState newState = definedState.onOutboundSubscriptionChange(presence.getType());
                if (rosterItem == null) {
                    RosterItem rosterItem1 = new Contact(presence.getTo(), null, false, false, Contact.Subscription.NONE, Collections.emptyList());
                    rosterItemProvider.create(username, rosterItem1);
                } else {
//                    rosterItem.setSubscription(newState.getSubscription());
//                    rosterItem.setPendingOut(newState.isPendingOut());
//                    rosterItem.setPendingIn(newState.isPendingIn());
                    rosterItemProvider.update(username, rosterItem);
                }
                Stream<Session> userSessions = sessionManager.getUserSessions(presence.getFrom().asBareJid());
                userSessions.forEach(session -> session.send(IQ.set(session.getRemoteXmppAddress(), new Roster(new Contact(presence.getTo(), null, true, null, Contact.Subscription.NONE, Collections.emptyList())))));
            }

            //rosterDao.create(new RosterItem(username, presence.getTo()));

        }

        return true;
    }
}
