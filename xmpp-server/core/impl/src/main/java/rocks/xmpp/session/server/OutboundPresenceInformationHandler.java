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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.OutboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.server.ServerRosterManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*
 * Handles outbound presence information.
 *
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-initial-outbound">4.2.2.  Server Processing of Outbound Initial Presence</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-probe-outbound">4.3.1.  Server Generation of Outbound Presence Probe</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-broadcast-outbound">4.4.2.  Server Processing of Subsequent Outbound Presence</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-unavailable-outbound">4.5.2.  Server Processing of Outbound Unavailable Presence</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-directed-outbound">4.6.3.  Server Processing of Outbound Directed Presence</a>
 */
@ApplicationScoped
public class OutboundPresenceInformationHandler implements OutboundPresenceHandler {

    @Inject
    private ServerRosterManager rosterManager;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private StanzaProcessor stanzaProcessor;

    final Map<Jid, Set<Jid>> directPresences = new ConcurrentHashMap<>();

    public void process(final Presence presence) {
        String username = presence.getFrom().getLocal();
        if (username != null) {
            if (presence.getTo() == null) {
                if (presence.isAvailable()) {

                    Session session = sessionManager.getSession(presence.getFrom());
                    if (session instanceof InboundClientSession) {
                        ((InboundClientSession) session).setPresence(presence);
                    }

                    broadcastToContacts(presence);

                    // The user's server MUST also broadcast initial presence from the user's newly available resource to all of the user's available resources, including the resource that generated the presence notification in the first place (i.e., an entity is implicitly subscribed to its own presence).
                    Presence selfPresence = new Presence(presence.getFrom().asBareJid(), presence.getType(), presence.getShow(), presence.getStatuses(), presence.getPriority(), presence.getId(), presence.getFrom(), presence.getLanguage(), presence.getExtensions(), presence.getError());
                    stanzaProcessor.process(selfPresence);

                } else if (presence.getType() == Presence.Type.UNAVAILABLE) {
                    Session session = sessionManager.getSession(presence.getFrom());
                    if (session instanceof InboundClientSession) {
                        ((InboundClientSession) session).setPresence(presence);
                    }

                    Set<Jid> contacts = broadcastToContacts(presence);

                    // Get the direct available presences sent by the user
                    Set<Jid> directAvailablePresences = directPresences.remove(presence.getFrom());
                    // In case a direct presence receive became a contact in the meanwhile, don't send an unavailable presence again.
                    directAvailablePresences.removeAll(contacts);
                    broadcast(presence, directAvailablePresences);

                    Presence selfPresence = new Presence(presence.getFrom().asBareJid(), presence.getType(), presence.getShow(), presence.getStatuses(), presence.getPriority(), presence.getId(), presence.getFrom(), presence.getLanguage(), presence.getExtensions(), presence.getError());
                    stanzaProcessor.process(selfPresence);
                }
            } else {
                // Handle direct presence sessions with unsubscribed recipients.
                RosterItem rosterItem = rosterManager.getRosterItem(username, presence.getTo().asBareJid());

                if (rosterItem == null || rosterItem.getSubscription() == null || !rosterItem.getSubscription().contactHasSubscriptionToUser()) {
                    // TODO or user has no presence session

                    if (presence.isAvailable()) {
                        // Store the direct available presences sent by an entity.
                        // This allows to sent unavailable presences to them if user becomes unavailable.
                        directPresences.compute(presence.getFrom(), (key, set) -> {
                            Set<Jid> recipients;
                            if (set == null) {
                                recipients = ConcurrentHashMap.newKeySet();
                            } else {
                                recipients = set;
                            }
                            recipients.add(presence.getTo());
                            return recipients;
                        });
                    } else if (presence.getType() == Presence.Type.UNAVAILABLE) {
                        // Direct unavailable presence has been sent.
                        // Remove the recipient again from the set, no need to notify him later.
                        Set<Jid> recipients = directPresences.get(presence.getFrom());
                        if (recipients != null) {
                            recipients.remove(presence.getTo());
                            if (recipients.isEmpty()) {
                                // The user has no direct presence sessions with anybody, let's remove its key then.
                                directPresences.remove(presence.getFrom());
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<Jid> broadcastToContacts(Presence presence) {
        Set<Jid> contacts = rosterManager.getRosterItems(presence.getFrom().getLocal())
                .stream()
                .filter(rosterItem -> rosterItem.getSubscription() != null && rosterItem.getSubscription().contactHasSubscriptionToUser())
                .map(RosterItem::getJid)
                .collect(Collectors.toSet());
        broadcast(presence, contacts);
        return contacts;
    }

    private void broadcast(Presence presence, Iterable<Jid> recipients) {
        recipients.forEach(contact -> {
            Presence p = new Presence(contact, presence.getType(), presence.getShow(), presence.getStatuses(), presence.getPriority(), presence.getId(), presence.getFrom(), presence.getLanguage(), presence.getExtensions(), presence.getError());
            stanzaProcessor.process(p);
        });
    }

    @Override
    public void handleOutboundPresence(PresenceEvent e) {
        process(e.getPresence());
    }
}
