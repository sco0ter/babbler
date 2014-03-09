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

package org.xmpp.extension.pubsub.event;

import org.xmpp.extension.pubsub.Item;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Christian Schudt
 */
final class ItemElement implements Item {

    @XmlAnyElement
    private Object object;

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "node")
    private String node;

    @XmlAttribute(name = "publisher")
    private String publisher;

    private ItemElement() {
    }

    public ItemElement(String id) {
        this.id = id;
    }

    public ItemElement(Object object) {
        this.object = object;
    }

    @Override
    public Object getPayload() {
        return object;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public String getPublisher() {
        return publisher;
    }
}
