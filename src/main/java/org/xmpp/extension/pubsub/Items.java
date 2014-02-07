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

package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
final class Items {

    @XmlElements({
            @XmlElement(name = "item", namespace = PubSub.EVENT_NAMESPACE),
            @XmlElement(name = "item")})
    private List<Item> items = new ArrayList<>();

    @XmlAttribute(name = "node")
    private String node;

    @XmlAttribute(name = "max_items")
    private Long maxItems;

    @XmlAttribute(name = "subid")
    private String subid;

    @XmlElements({
            @XmlElement(name = "retract"),
            @XmlElement(name = "retract", namespace = PubSub.EVENT_NAMESPACE)})
    private Retract retract;

    private Items() {
    }

    public Items(String node) {
        this.node = node;
    }

    public Items(String node, long maxItems) {
        this.node = node;
        this.maxItems = maxItems;
    }

    public Items(String node, Item item) {
        this.node = node;
        this.items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public String getNode() {
        return node;
    }

    Retract getRetract() {
        return retract;
    }
}
