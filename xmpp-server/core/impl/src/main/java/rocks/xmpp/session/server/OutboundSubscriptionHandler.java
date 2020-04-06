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

import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.im.roster.model.DefinedState;
import rocks.xmpp.im.roster.model.RosterItem;

import javax.inject.Inject;

/**
 * Handles outbound presence subscription requests, approvals, cancellations and unsubscriptions.
 *
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#sub-request-outbound">3.1.2.  Server Processing of Outbound Subscription Request</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#sub-request-approvalout">3.1.5.  Server Processing of Outbound Subscription Approval</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#sub-cancel-outbound">3.2.2.  Server Processing of Outbound Subscription Cancellation</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#sub-unsub-outbound">3.3.2.  Server Processing of Outbound Unsubscribe</a>
 */
public class OutboundSubscriptionHandler extends AbstractSubscriptionHandler {

    @Inject
    private SessionManager sessionManager;

    @Inject
    private ServerConfiguration serverConfiguration;

    public void process(final Presence presence) {

        if (presence.isSubscription()) {
            // RFC 6121 § 3.  Managing Presence Subscriptions
            // When a server processes or generates an outbound presence stanza of type "subscribe", "subscribed", "unsubscribe", or "unsubscribed",
            // the server MUST stamp the outgoing presence stanza with the bare JID <localpart@domainpart> of the sending entity, not the full JID <localpart@domainpart/resourcepart>.
            if (presence.getFrom().isFullJid()) {
                presence.setFrom(presence.getFrom().asBareJid());
            }

            // RFC 6121 § 3.1.2.  Server Processing of Outbound Subscription Request
            // If the JID is of the form <contact@domainpart/resourcepart> instead of <contact@domainpart>,
            // the user's server SHOULD treat it as if the request had been directed to the contact's bare JID and modify the 'to' address accordingly.
            if (presence.getTo().isFullJid()) {
                presence.setTo(presence.getTo().asBareJid());
            }

            final String username = presence.getFrom().getLocal();
            final RosterItem rosterItem = rosterManager.getRosterItem(username, presence.getTo());
            switch (presence.getType()) {
                case SUBSCRIBE:
                case SUBSCRIBED:
                case UNSUBSCRIBE:
                    updateRosterAndPush(username, presence, rosterItem, DefinedState::onOutboundSubscriptionChange);
                    break;
                case UNSUBSCRIBED:
                    if (rosterItem != null) {
                        DefinedState definedState = DefinedState.valueOf(rosterItem);
                        switch (definedState) {
                            case NONE:
                            case NONE_PENDING_OUT:
                            case TO:
                                // If the user's bare JID is not yet in the contact's roster or is in the contact's roster with a state
                                // of "None", "None + Pending Out", or "To", the contact's server SHOULD NOT route or deliver the presence stanza
                                // of type "unsubscribed"to the user and MUST NOT send presence notifications of type "unavailable" to the user.
                                break;
                            default:
                                // While the user is still subscribed to the contact's presence (i.e., before the contact's server routes or delivers the presence stanza
                                // of type "unsubscribed" to the user), the contact's server MUST send a presence stanza of type "unavailable" from all of the contact's online resources to the user.
                                sessionManager.getUserSessions(serverConfiguration.getDomain().withLocal(username)).forEach(session -> {
                                            Presence unavailablePresence = new Presence(Presence.Type.UNAVAILABLE);
                                            unavailablePresence.setFrom(session.getRemoteXmppAddress());
                                            unavailablePresence.setTo(presence.getTo());
                                            // TODO send
                                        }
                                );
                                updateRosterAndPush(username, presence, rosterItem, DefinedState::onOutboundSubscriptionChange);
                                break;
                        }
                    }
                    break;
                default:
                    break;
            }
            // TODO deliver or route
        }
    }
}
