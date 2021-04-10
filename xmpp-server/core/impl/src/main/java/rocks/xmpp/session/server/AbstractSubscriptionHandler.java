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

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import javax.inject.Inject;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.im.roster.model.DefinedState;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.server.ServerRosterManager;

public abstract class AbstractSubscriptionHandler {

    @Inject
    protected ServerRosterManager rosterManager;

    /**
     * Updates the roster and initiates a roster push to the user's interested resources.
     *
     * @param username The user.
     * @param presence The presence.
     * @param item     The roster item to update. If null, a new item is created.
     * @return True, if the subscription state has changed; false otherwise.
     */
    protected boolean updateRosterAndPush(String username, Presence presence, Runnable onStateChange, RosterItem item,
                                          BiFunction<DefinedState, Presence.Type, DefinedState> function,
                                          boolean approved) {

        if (item != null) {
            DefinedState oldState = DefinedState.valueOf(item);
            DefinedState newState = function.apply(oldState, presence.getType());
            final boolean stateChanged = newState != oldState;
            if (stateChanged || item.isApproved() != approved) {
                if (stateChanged && onStateChange != null) {
                    onStateChange.run();
                }
                rosterManager.setRosterItem(username, new RosterItem() {
                    @Override
                    public Jid getJid() {
                        return item.getJid();
                    }

                    @Override
                    public String getName() {
                        return item.getName();
                    }

                    @Override
                    public boolean isApproved() {
                        return approved;
                    }

                    @Override
                    public List<String> getGroups() {
                        return item.getGroups();
                    }

                    @Override
                    public Subscription getSubscription() {
                        return newState.getSubscription();
                    }

                    @Override
                    public boolean isPendingOut() {
                        return newState.isPendingOut();
                    }

                    @Override
                    public boolean isPendingIn() {
                        return newState.isPendingIn();
                    }
                });
            }
            return stateChanged;
        } else {
            if (onStateChange != null) {
                onStateChange.run();
            }
            rosterManager.setRosterItem(username, new RosterItem() {
                @Override
                public Jid getJid() {
                    return presence.getTo();
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public boolean isApproved() {
                    return approved;
                }

                @Override
                public List<String> getGroups() {
                    return Collections.emptyList();
                }

                @Override
                public Subscription getSubscription() {
                    return Subscription.NONE;
                }

                @Override
                public boolean isPendingOut() {
                    return true;
                }

                @Override
                public boolean isPendingIn() {
                    return false;
                }
            });
        }
        return true;
    }
}
