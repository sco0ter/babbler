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

package rocks.xmpp.extensions.disco.handler;

import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.rsm.ResultSet;
import rocks.xmpp.extensions.rsm.ResultSetProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles 'disco#items' request by responding with the items at the specified node.
 *
 * @author Christian Schudt
 */
public final class DiscoItemsHandler extends AbstractIQHandler implements ExtensionProtocol {

    private static final Set<String> FEATURES = Collections.singleton(ItemDiscovery.NAMESPACE);

    private final Map<String, ResultSetProvider<Item>> itemProviders = new ConcurrentHashMap<>();

    public DiscoItemsHandler() {
        super(ItemDiscovery.class, IQ.Type.GET);
    }

    @Override
    protected IQ processRequest(IQ iq) {
        ItemDiscovery itemDiscovery = iq.getExtension(ItemDiscovery.class);
        ResultSetProvider<Item> itemProvider = itemProviders.get(itemDiscovery.getNode() == null ? "" : itemDiscovery.getNode());
        if (itemProvider != null) {
            ResultSet<Item> resultSet = ResultSet.create(itemProvider, itemDiscovery.getResultSetManagement());
            return iq.createResult(new ItemDiscovery(itemDiscovery.getNode(), resultSet.getItems(), resultSet.getResultSetManagement()));
        } else {
            if (itemDiscovery.getNode() == null) {
                // If there are no items associated with an entity (or if those items are not publicly available), the target entity MUST return an empty query element to the requesting entity.
                return iq.createResult(new ItemDiscovery());
            } else {
                // <item-not-found/>: The JID or JID+NodeID of the specified target entity does not exist.
                return iq.createError(Condition.ITEM_NOT_FOUND);
            }
        }
    }

    /**
     * Gets an unmodifiable list of items at the root node.
     *
     * @return The items.
     */
    public final List<Item> getItems() {
        ResultSetProvider<Item> rootItemProvider = itemProviders.get("");
        if (rootItemProvider != null) {
            return Collections.unmodifiableList(rootItemProvider.getItems());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Sets an item provider for the root node.
     * <p>
     * If you want to manage items in memory, you can use {@link ResultSetProvider#forItems(Collection)}}.
     *
     * @param itemProvider The item provider.
     */
    public final void setItemProvider(ResultSetProvider<Item> itemProvider) {
        setItemProvider("", itemProvider);
    }

    /**
     * Sets an item provider for a node.
     * <p>
     * If you want to manage items in memory, you can use {@link ResultSetProvider#forItems(Collection)}}.
     *
     * @param node         The node name.
     * @param itemProvider The item provider.
     */
    public final void setItemProvider(String node, ResultSetProvider<Item> itemProvider) {
        if (itemProvider == null) {
            itemProviders.remove(node);
        } else {
            itemProviders.put(node, itemProvider);
        }
    }

    /**
     * Clears all items.
     */
    public void clear() {
        itemProviders.clear();
    }

    @Override
    public final boolean isEnabled() {
        return true;
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }
}
