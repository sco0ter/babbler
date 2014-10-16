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

package rocks.xmpp.extensions.disco.model.items;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@code <query/>} element in the {@code http://jabber.org/protocol/disco#item} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0030.html">XEP-0030: Service Discovery</a>
 * @see <a href="http://xmpp.org/extensions/xep-0030.html#schemas-items">XML Schema</a>
 */
@XmlRootElement(name = "query")
public final class ItemDiscovery implements ItemNode {

    @XmlAttribute(name = "node")
    private String node;

    @XmlElement(name = "item")
    private List<Item> items = new ArrayList<>();

    /**
     * Creates an empty element, used for item discovery requests.
     */
    public ItemDiscovery() {
    }

    /**
     * Creates an item discovery element with a node attribute.
     *
     * @param node The node.
     */
    public ItemDiscovery(String node) {
        this.node = node;
    }

    /**
     * Creates an item discovery element with nodes.
     *
     * @param items The items.
     */
    public ItemDiscovery(List<Item> items) {
        this.items = items;
    }

    /**
     * Creates an item discovery element with a node attribute.
     *
     * @param node  The node.
     * @param items The items.
     */
    public ItemDiscovery(String node, List<Item> items) {
        this.node = node;
        this.items = items;
    }

    @Override
    public List<Item> getItems() {
        return items;
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (node != null) {
            sb.append(node);
        }
        if (items != null) {
            if (!sb.toString().isEmpty()) {
                sb.append(": ");
            }
            sb.append(items.toString());
        }
        return sb.toString();
    }
}
