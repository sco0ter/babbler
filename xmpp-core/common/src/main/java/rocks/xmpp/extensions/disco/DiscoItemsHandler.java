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

package rocks.xmpp.extensions.disco;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.disco.model.items.DiscoverableItem;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemProvider;
import rocks.xmpp.extensions.rsm.ResultSet;
import rocks.xmpp.extensions.rsm.ResultSetProvider;

/**
 * Handles 'disco#items' request by responding with the items at the specified address and/or node.
 *
 * @author Christian Schudt
 */
final class DiscoItemsHandler extends AbstractIQHandler {

    private final Set<ItemProvider> itemProviders = new CopyOnWriteArraySet<>();

    DiscoItemsHandler() {
        super(ItemDiscovery.class, IQ.Type.GET);
    }

    @Override
    protected IQ processRequest(IQ iq) {
        ItemDiscovery itemDiscovery = iq.getExtension(ItemDiscovery.class);

        List<ResultSetProvider<DiscoverableItem>> providers = new ArrayList<>();
        for (ItemProvider itemProvider : itemProviders) {
            try {
                ResultSetProvider<DiscoverableItem> itemResultSetProvider =
                        itemProvider.getItems(iq.getTo(), iq.getFrom(), itemDiscovery.getNode(), iq.getLanguage());
                if (itemResultSetProvider != null) {
                    providers.add(itemResultSetProvider);
                }
            } catch (StanzaErrorException e) {
                return iq.createError(e.getError());
            }
        }

        if (!providers.isEmpty()) {
            ResultSetProvider<DiscoverableItem> combinedResultSetProvider = ResultSetProvider.combine(providers);
            ResultSet<DiscoverableItem> resultSet =
                    ResultSet.create(combinedResultSetProvider, itemDiscovery.getResultSetManagement());
            return iq.createResult(new ItemDiscovery(itemDiscovery.getNode(), resultSet.getItems(),
                    resultSet.getResultSetManagement()));
        } else {
            // No providers have been found to handle JID or JID+NodeID
            // <item-not-found/>: The JID or JID+NodeID of the specified target entity does not exist.
            return iq.createError(Condition.ITEM_NOT_FOUND);
        }
    }

    /**
     * Adds an item provider. Requests to this handler will return items returned by the provider if appropriate.
     *
     * @param itemProvider The item provider.
     * @return If the provider could be added.
     * @see #removeItemProvider(ItemProvider)
     */
    final boolean addItemProvider(ItemProvider itemProvider) {
        return itemProviders.add(itemProvider);
    }

    /**
     * Removes an item provider.
     *
     * @param itemProvider The item provider.
     * @return If the provider could be removed .
     * @see #addItemProvider(ItemProvider)
     */
    final boolean removeItemProvider(ItemProvider itemProvider) {
        return itemProviders.remove(itemProvider);
    }
}
