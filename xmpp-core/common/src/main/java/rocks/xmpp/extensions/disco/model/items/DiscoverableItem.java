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

package rocks.xmpp.extensions.disco.model.items;

import java.util.Objects;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.disco.AbstractServiceDiscoveryManager;
import rocks.xmpp.extensions.rsm.model.ResultSetItem;

/**
 * A Service Discovery item which has an id, in order to provide a limited result set.
 *
 * <p>Implementations of this class are provided by the {@link ItemProvider}, which in turn is used by the
 * {@link AbstractServiceDiscoveryManager} to returned a limited result set to the requesting entity
 * (only if a limited result set has been requested).</p>
 */
public interface DiscoverableItem extends Item, ResultSetItem {

    /**
     * Creates an anonymous instance of this interface from an {@link Item} and a result item id.
     *
     * @param item The item.
     * @param id   The id, which identifies the item.
     * @return A discoverable item.
     * @throws NullPointerException If item or id is null.
     */
    static DiscoverableItem from(Item item, String id) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(id);
        return new DiscoverableItem() {
            @Override
            public String getName() {
                return item.getName();
            }

            @Override
            public Jid getJid() {
                return item.getJid();
            }

            @Override
            public String getNode() {
                return item.getNode();
            }

            @Override
            public String getId() {
                return id;
            }
        };
    }
}
