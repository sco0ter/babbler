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

import org.xmpp.Jid;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class Node {
    private String node;

    private String name;

    private Type type;

    // http://xmpp.org/extensions/xep-0060.html#registrar-formtypes-metadata

    private List<Jid> contacts;

    private Date creationDate;

    private Jid creator;

    private String description;

    private List<String> language;

    private int numberOfSubscribers;

    private List<Jid> owners;

    private List<Jid> publishers;

    private String title;

    private String payloadType;

    public Node() {
    }

    public Node(String node, String name) {
        // If a node is a leaf node rather than a collection node and items have been published to the node, the service MAY return one <item/> element for each published item as described in the Discover Items for a Node section of this document, however such items MUST NOT include a 'node' attribute (since they are published items, not nodes).
        this(node, name, node == null ? Type.LEAF : Type.COLLECTION);
    }

    public Node(String node, String name, Type type) {
        this.node = node;
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the JIDs of those to contact with questions.
     *
     * @return The contacts.
     */
    public List<Jid> getContacts() {
        return contacts;
    }

    /**
     * Gets the datetime when the node was created.
     *
     * @return The creation date.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Gets the JID of the node creator.
     *
     * @return The creator.
     */
    public Jid getCreator() {
        return creator;
    }

    /**
     * Gets a description of the node.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the languages of the node.
     *
     * @return The node.
     */
    public List<String> getLanguage() {
        return language;
    }

    /**
     * Gets the number of subscribers to the node.
     *
     * @return The number of subscribers.
     */
    public int getNumberOfSubscribers() {
        return numberOfSubscribers;
    }

    /**
     * Gets the owners of the node.
     *
     * @return The owners.
     */
    public List<Jid> getOwners() {
        return owners;
    }

    /**
     * The publishers for this node.
     *
     * @return The publishers.
     */
    public List<Jid> getPublishers() {
        return publishers;
    }

    /**
     * Gets the payload type for the node.
     *
     * @return The payload type.
     */
    public String getPayloadType() {
        return payloadType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the name of the node.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the node.
     *
     * @return The node.
     */
    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    /**
     * The node type.
     */
    public enum Type {
        LEAF,
        COLLECTION,
    }
}
