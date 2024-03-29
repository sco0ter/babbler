/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.extensions.reach;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.InboundMessageHandler;
import rocks.xmpp.core.stanza.InboundPresenceHandler;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.OutboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.pubsub.model.Item;
import rocks.xmpp.extensions.pubsub.model.event.Event;
import rocks.xmpp.extensions.reach.model.Address;
import rocks.xmpp.extensions.reach.model.Reachability;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * Allows to query for reachability addresses of another contact, automatically responds to reachability queries and
 * notifies {@linkplain Consumer}s, when the reachability of a contact has changed either via presence or PEP.
 */
public final class ReachabilityManager extends AbstractIQHandler
        implements InboundPresenceHandler, OutboundPresenceHandler, InboundMessageHandler, ExtensionProtocol,
        DiscoverableInfo {

    private static final Set<String> FEATURES = Collections.singleton(Reachability.NAMESPACE);

    private final Set<Consumer<ReachabilityEvent>> reachabilityListeners = new CopyOnWriteArraySet<>();

    private final Map<Jid, List<Address>> reachabilities = new ConcurrentHashMap<>();

    private final List<Address> addresses = new CopyOnWriteArrayList<>();

    private final XmppSession xmppSession;

    ReachabilityManager(final XmppSession xmppSession) {
        super(Reachability.class, IQ.Type.GET);
        this.xmppSession = xmppSession;
    }

    /**
     * Gets the reachability address as unmodifiable list.
     *
     * @return The reachability addresses.
     */
    public List<Address> getReachabilityAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    /**
     * Adds a reachability address.
     *
     * @param address The reachability address.
     * @return True, if the address was added.
     * @see #removeReachabilityAddress(Address)
     */
    public boolean addReachabilityAddress(Address address) {
        boolean result = addresses.add(address);
        xmppSession.enableFeature(getNamespace());
        return result;
    }

    /**
     * Removes a reachability address.
     *
     * @param address The reachability address.
     * @return True, if the address was removed.
     * @see #addReachabilityAddress(Address)
     */
    public boolean removeReachabilityAddress(Address address) {
        boolean result = addresses.remove(address);
        if (addresses.isEmpty()) {
            xmppSession.disableFeature(getNamespace());
        }
        return result;
    }

    /**
     * Adds a reachability listener, which allows to listen for reachability updates.
     *
     * @param reachabilityListener The listener.
     * @see #removeReachabilityListener(Consumer)
     */
    public void addReachabilityListener(Consumer<ReachabilityEvent> reachabilityListener) {
        reachabilityListeners.add(reachabilityListener);
    }

    /**
     * Removes a previously added reachability listener.
     *
     * @param reachabilityListener The listener.
     * @see #addReachabilityListener(Consumer)
     */
    public void removeReachabilityListener(Consumer<ReachabilityEvent> reachabilityListener) {
        reachabilityListeners.remove(reachabilityListener);
    }

    /**
     * Requests the reachability addresses of a contact.
     *
     * @param contact The contact.
     * @return The async result with the reachability addresses.
     */
    public AsyncResult<List<Address>> requestReachabilityAddresses(Jid contact) {
        // In addition, a contact MAY request a user's reachability addresses in an XMPP <iq/> stanza of type "get".
        return xmppSession.query(IQ.get(contact, new Reachability())).thenApply(result -> {
            Reachability reachability = result.getExtension(Reachability.class);
            if (reachability != null) {
                return reachability.getAddresses();
            }
            return null;
        });
    }

    @Override
    protected final IQ processRequest(IQ iq) {
        // In addition, a contact MAY request a user's reachability addresses in an XMPP <iq/> stanza of type "get"
        return iq.createResult(new Reachability(addresses));
    }

    @Override
    public final void handleInboundPresence(PresenceEvent e) {
        Presence presence = e.getPresence();
        boolean hasReachability = checkStanzaForReachabilityAndNotify(presence);
        Jid contact = presence.getFrom().asBareJid();
        if (!hasReachability && reachabilities.remove(contact) != null) {
            // If no reachability was found in presence, check, if the contact has previously sent any reachability
            // via presence.
            XmppUtils.notifyEventListeners(reachabilityListeners,
                    new ReachabilityEvent(this, contact, Collections.emptyList()));
        }
    }

    @Override
    public final void handleInboundMessage(MessageEvent e) {
        // a user MAY send reachability addresses in an XMPP <message/> stanza.
        boolean hasReachability = checkStanzaForReachabilityAndNotify(e.getMessage());

        if (!hasReachability) {
            Event event = e.getMessage().getExtension(Event.class);
            if (event != null) {
                for (final Item item : event.getItems()) {
                    if (item.getPayload() instanceof Reachability) {
                        handleReachability(e.getMessage().getFrom(), (Reachability) item.getPayload());
                    }
                }
            }
        }
    }

    private boolean checkStanzaForReachabilityAndNotify(Stanza stanza) {
        Reachability reachability = stanza.getExtension(Reachability.class);
        if (reachability != null && stanza.getFrom() != null) {
            Jid contact = stanza.getFrom().asBareJid();
            handleReachability(contact, reachability);
        }
        return reachability != null;
    }

    private void handleReachability(Jid contact, Reachability reachability) {
        List<Address> oldReachabilityAddresses = reachabilities.put(contact, reachability.getAddresses());
        if (oldReachabilityAddresses == null || !oldReachabilityAddresses.equals(reachability.getAddresses())) {
            XmppUtils.notifyEventListeners(reachabilityListeners,
                    new ReachabilityEvent(this, contact, reachability.getAddresses()));
        }
    }

    @Override
    public final void handleOutboundPresence(PresenceEvent e) {
        Presence presence = e.getPresence();
        if (presence.isAvailable()) {
            synchronized (addresses) {
                if (!addresses.isEmpty()) {
                    presence.putExtension(new Reachability(new ArrayDeque<>(addresses)));
                }
            }
        }
    }

    @Override
    public final String getNamespace() {
        return Reachability.NAMESPACE;
    }

    @Override
    public final boolean isEnabled() {
        return !addresses.isEmpty();
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }
}
