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

package rocks.xmpp.extensions.disco.server;

import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.disco.model.items.DiscoverableItem;
import rocks.xmpp.extensions.disco.model.items.ItemProvider;
import rocks.xmpp.extensions.rsm.ResultSetProvider;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.server.ServerRosterManager;
import rocks.xmpp.session.server.SessionManager;

/**
 * Returns a user's available resources as disco#items response if allowed.
 *
 * <blockquote>
 * <p>In response to a disco#items request sent to a bare JID with no node, if access is not denied the server SHOULD return the associated items including connected or available resources as appropriate</p>
 * <cite><a href="https://xmpp.org/extensions/xep-0030.html">XEP-0030: Service Discovery</a></cite>
 * </blockquote>
 *
 * @see <a href="https://xmpp.org/extensions/xep-0030.html#example-15">XEP-0030: Service Discovery, Example 15.</a>
 */
@ApplicationScoped
public class AvailableResourcesItemProvider implements ItemProvider {

    @Inject
    private SessionManager sessionManager;

    @Inject
    private ServerRosterManager rosterManager;

    @Override
    public ResultSetProvider<DiscoverableItem> getItems(Jid to, Jid from, String node, Locale locale) {
        if (to.getLocal() != null && node == null) {
            // The following rules apply to the handling of service discovery requests sent to bare JIDs:
            // In response to a disco#items request, the server MUST return an empty result set if:
            // 2a. The target entity does not exist (no matter if the request specifies a node or not).
            // 2b. The request did not specify a node, the only items are available resources
            //     and the requesting entity is not authorized to receive presence from the target entity
            //     (i.e., via the target having a presence subscription to the requesting entity of type "both" or "from")

            if (to.asBareJid().equals(from.asBareJid())) {
                // A user is always subscribed to its own presence.
                return getAvailableResourcesAsItems(to);
            } else {
                RosterItem rosterItem = rosterManager.getRosterItem(to.getLocal(), from.asBareJid());
                if (rosterItem != null && rosterItem.getSubscription().contactHasSubscriptionToUser()) {
                    return getAvailableResourcesAsItems(to);
                }
            }
            return ResultSetProvider.forItems(Collections.emptyList());
        }
        return null;
    }

    private ResultSetProvider<DiscoverableItem> getAvailableResourcesAsItems(Jid user) {
        return ResultSetProvider.forItems(sessionManager.getUserSessions(user.asBareJid())
                .map(session -> new DiscoverableItem() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public Jid getJid() {
                        return session.getRemoteXmppAddress();
                    }

                    @Override
                    public String getNode() {
                        return null;
                    }

                    @Override
                    public String getId() {
                        return session.getRemoteXmppAddress().toEscapedString();
                    }
                })
                .collect(Collectors.toList()));
    }
}
