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

package rocks.xmpp.im.roster.server;

import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.model.SubscriptionState;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Christian Schudt
 */
@Dependent
public class RosterHandler extends AbstractIQHandler {

    @Inject
    private ServerRosterManager rosterManager;

    public RosterHandler() {
        super(Roster.class, IQ.Type.GET, IQ.Type.SET);
    }

    @Override
    protected IQ processRequest(IQ iq) {

        if (iq.getTo() == null || iq.getTo().equals(iq.getFrom().asBareJid())) {
            Roster roster = iq.getExtension(Roster.class);
            if (iq.getType() == IQ.Type.GET) {
                if (!roster.getContacts().isEmpty()) {
                    // the <query/> element MUST NOT contain any <item/> child elements.
                    return iq.createError(Condition.BAD_REQUEST);
                }
                Collection<? extends RosterItem> rosterItems = rosterManager.getRosterItems(iq.getFrom().getLocal());
                return iq.createResult(new Roster(rosterItems.stream().map(Contact::new).collect(Collectors.toList())));
            } else if (roster.getContacts().size() == 1) {
                RosterItem rosterItem = roster.getContacts().get(0);

                if (rosterItem.getSubscription() == SubscriptionState.Subscription.REMOVE) {
                    RosterItem deletedItem = rosterManager.delete(iq.getFrom().getLocal(), rosterItem.getJid());
                    if (deletedItem != null) {
                        return iq.createResult();
                    } else {
                        // RFC 6121 2.5.3.  Error Cases
                        // If the value of the 'jid' attribute specifies an item that is not in the roster, then the server MUST return an <item-not-found/> stanza error.
                        return iq.createError(Condition.ITEM_NOT_FOUND);
                    }
                } else {

                    // Check the roster groups.
                    StanzaError stanzaError = checkGroups(rosterItem.getGroups());
                    if (stanzaError != null) {
                        return iq.createError(stanzaError);
                    }
                    rosterManager.setRosterItem(iq.getFrom().getLocal(), rosterItem);

                    return iq.createResult();
                }
            }
            // The <query/> element MUST contain one and only one <item/> element.
            return iq.createError(Condition.BAD_REQUEST);
        } else {
            return iq.createError(Condition.FORBIDDEN);
        }
    }

    /**
     * Checks if the roster groups conform to the specification.
     *
     * @param groups The roster groups.
     * @return A stanza error if a violation has been detected or null if everything is fine.
     * @see <a href="https://xmpp.org/rfcs/rfc6121.html#roster-add-errors">2.3.3.  Error Cases</a>
     */
    private static StanzaError checkGroups(final Iterable<String> groups) {
        final Set<String> set = new HashSet<>();
        for (String group : groups) {
            if (!set.add(group)) {
                // Duplicate group found.
                // RFC 6121 2.3.3.  Error Cases
                // 2. The <item/> element contains more than one <group/> element, but there are duplicate groups
                return new StanzaError(Condition.BAD_REQUEST, "Item contains duplicate groups");
            }
            if (group.isEmpty()) {
                // The server MUST return a <not-acceptable/> stanza error to the client if the roster set contains any of the following violations:
                // 2. The XML character data of the <group/> element is of zero length.
                return new StanzaError(Condition.NOT_ACCEPTABLE, "Group is of zero length");
            }
        }
        return null;
    }
}
