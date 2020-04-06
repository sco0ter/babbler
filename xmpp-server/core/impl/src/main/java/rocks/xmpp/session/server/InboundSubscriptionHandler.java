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

package rocks.xmpp.session.server;

import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.im.roster.model.DefinedState;
import rocks.xmpp.im.roster.model.RosterItem;

import javax.inject.Inject;
import java.util.stream.Stream;

/**
 * Handles inbound presence subscription requests, approvals, cancellations and unsubscriptions.
 *
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#sub-request-inbound">3.1.3.  Server Processing of Inbound Subscription Request</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#sub-request-approvalin">3.1.6.  Server Processing of Inbound Subscription Approval</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#sub-cancel-inbound">3.2.3.  Server Processing of Inbound Subscription Cancellation</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#sub-unsub-inbound">3.3.3.  Server Processing of Inbound Unsubscribe</a>
 */
public class InboundSubscriptionHandler extends AbstractSubscriptionHandler {

    @Inject
    private SessionManager sessionManager;

    public void process(Presence presence) {

        if (presence.isSubscription()) {

            // RFC 6121 3.1.3.  Server Processing of Inbound Subscription Request
            // If the JID is of the form <contact@domainpart/resourcepart> instead of <contact@domainpart>,
            // the user's server SHOULD treat it as if the request had been directed to the contact's bare JID and modify the 'to' address accordingly.
            if (presence.getTo().isFullJid()) {
                presence.setTo(presence.getTo().asBareJid());
            }
            String username = presence.getTo().getLocal();
            RosterItem rosterItem = rosterManager.getRosterItem(username, presence.getFrom());
            switch (presence.getType()) {
                case SUBSCRIBE:
                    if (rosterItem != null && rosterItem.getSubscription().contactHasSubscriptionToUser()) {
                        // If the contact exists and the user already has a subscription to the contact's presence,
                        // then the contact's server MUST auto-reply on behalf of the contact by sending a presence stanza of type "subscribed" from the contact's bare JID to the user's bare JID.
                        Presence subscribed = new Presence(presence.getFrom(), Presence.Type.SUBSCRIBED, null, presence.getId());
                        subscribed.setFrom(presence.getTo());
                        // TODO send
                    } else {
                        Stream<Session> userSessions = sessionManager.getUserSessions(presence.getTo());
                        userSessions.forEach(session -> session.send(presence));

                        updateRosterAndPush(username, presence, rosterItem, DefinedState::onInboundSubscriptionChange);
                    }
                    break;
                case SUBSCRIBED:
                    if (rosterItem != null) {
                        DefinedState definedState = DefinedState.valueOf(rosterItem);
                        if (definedState == DefinedState.NONE_PENDING_OUT
                                || definedState == DefinedState.NONE_PENDING_OUT_IN
                                || definedState == DefinedState.FROM_PENDING_OUT) {
                            // Deliver the inbound subscription approval to all of the user's interested resources
                            Stream<Session> userSessions = sessionManager.getUserSessions(presence.getTo());
                            userSessions.forEach(session -> session.send(presence));

                            // Initiate a roster push to all of the user's interested resources
                            updateRosterAndPush(username, presence, rosterItem, DefinedState::onInboundSubscriptionChange);

                            // The user's server MUST also deliver the available presence stanza received from each of the contact's available resources to each of the user's available resources.
                            Stream<Session> contactSessions = sessionManager.getUserSessions(presence.getFrom());
                            contactSessions.forEach(session -> {
                                Presence availablePresence = new Presence(presence.getTo());
                                availablePresence.setFrom(session.getRemoteXmppAddress());
                                // TODO send
                            });
                        }
                        // TODO optionally store presence
                        break;
                    }
                case UNSUBSCRIBED:
                    if (rosterItem != null && rosterItem.getSubscription().userHasSubscriptionToContact()) {
                        // Deliver the inbound subscription cancellation to all of the user's interested resources
                        Stream<Session> userSessions = sessionManager.getUserSessions(presence.getTo());
                        userSessions.forEach(session -> session.send(presence));

                        // Initiate a roster push to all of the user's interested resources
                        updateRosterAndPush(username, presence, rosterItem, DefinedState::onInboundSubscriptionChange);

                        // The user's server MUST also deliver the inbound presence stanzas of type "unavailable".
                        Stream<Session> contactSessions = sessionManager.getUserSessions(presence.getFrom());
                        contactSessions.forEach(session -> {
                            Presence unavailablePresence = new Presence(presence.getTo(), Presence.Type.UNAVAILABLE, null);
                            unavailablePresence.setFrom(session.getRemoteXmppAddress());
                            // TODO send
                        });
                    }
                    break;
                case UNSUBSCRIBE:
                    if (rosterItem != null && rosterItem.getSubscription().contactHasSubscriptionToUser()) {
                        // Deliver the inbound unsubscribe to all of the user's interested resources
                        // TODO send

                        // Initiate a roster push to all of the user's interested resources
                        updateRosterAndPush(username, presence, rosterItem, DefinedState::onInboundSubscriptionChange);

                        // Generate an outbound presence stanza of type "unavailable" from each of the contact's available resources to the user.
                        Stream<Session> contactSessions = sessionManager.getUserSessions(presence.getTo());
                        contactSessions.forEach(session -> {
                            Presence unavailablePresence = new Presence(presence.getFrom(), Presence.Type.UNAVAILABLE, null);
                            unavailablePresence.setFrom(session.getRemoteXmppAddress());
                            // TODO send
                        });
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
