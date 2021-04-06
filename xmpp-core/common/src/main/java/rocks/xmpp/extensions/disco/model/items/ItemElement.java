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
import javax.xml.bind.annotation.XmlAttribute;

import rocks.xmpp.addr.Jid;

/**
 * The implementation of the {@code <item/>} element in the {@code http://jabber.org/protocol/disco#item} namespace, used for item discovery.
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 */
public final class ItemElement implements Item {

    @XmlAttribute
    private final Jid jid;

    @XmlAttribute
    private final String name;

    @XmlAttribute
    private final String node;

    private ItemElement() {
        this.jid = null;
        this.node = null;
        this.name = null;
    }

    public ItemElement(Jid jid, String node, String name) {
        this.jid = Objects.requireNonNull(jid);
        this.node = node;
        this.name = name;
    }

    public ItemElement(Item item) {
        this(item.getJid(), item.getNode(), item.getName());
    }

    /**
     * Gets the JID.
     *
     * @return The JID.
     */
    @Override
    public final Jid getJid() {
        return jid;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    @Override
    public final String getName() {
        return name;
    }

    /**
     * Gets the node.
     *
     * @return The node.
     */
    @Override
    public final String getNode() {
        return node;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder(jid);
        if (node != null) {
            sb.append(" / ").append(node);
        }
        if (name != null) {
            sb.append(" (").append(name).append(')');
        }
        return sb.toString();
    }
}
