/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
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

package org.xmpp.extension.offline;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@code <offline/>} element in the {@code http://jabber.org/protocol/offline} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0013.html">XEP-0013: Flexible Offline Message Retrieval</a>
 * @see <a href="http://xmpp.org/extensions/xep-0013.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "offline")
public final class OfflineMessage {

    static final String NAMESPACE = "http://jabber.org/protocol/offline";

    @XmlElement(name = "item")
    private final List<Item> items = new ArrayList<>();

    @XmlElement(name = "fetch")
    private String fetch;

    @XmlElement(name = "purge")
    private String purge;

    OfflineMessage() {
    }

    OfflineMessage(Item item) {
        items.add(item);
    }

    OfflineMessage(boolean fetch, boolean purge) {
        this.fetch = fetch ? "" : null;
        this.purge = purge ? "" : null;
    }

    /**
     * Gets the offline message id.
     *
     * @return The offline message id.
     */
    public String getId() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getId();
        }
        return null;
    }

    /**
     * Gets the items.
     *
     * @return The items.
     */
    List<Item> getItems() {
        return items;
    }

    static final class Item {
        @XmlAttribute(name = "node")
        private String id;

        @XmlAttribute(name = "action")
        private Action action;

        private Item() {
        }

        Item(String id, Action action) {
            this.id = id;
            this.action = action;
        }

        /**
         * Gets the offline message id.
         *
         * @return The id.
         */
        public String getId() {
            return id;
        }

        @XmlEnum
        enum Action {
            @XmlEnumValue("remove")
            REMOVE,
            @XmlEnumValue("view")
            VIEW
        }
    }
}
