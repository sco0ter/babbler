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
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.extension.pubsub.owner.PubSubOwner;
import org.xmpp.stanza.client.IQ;

import java.net.URI;
import java.util.*;

/**
 * @author Christian Schudt
 */
public final class PubSubNode {

    private final String name;

    private final Type type;

    private final Jid pubSubServiceAddress;

    private final XmppSession xmppSession;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private String nodeId;

    PubSubNode(String node, String name, Jid pubSubServiceAddress, XmppSession xmppSession) {
        // If a nodeId is a leaf nodeId rather than a collection nodeId and items have been published to the nodeId, the service MAY return one <item/> element for each published item as described in the Discover Items for a Node section of this document, however such items MUST NOT include a 'nodeId' attribute (since they are published items, not nodes).
        this(node, name, node == null ? Type.LEAF : Type.COLLECTION, pubSubServiceAddress, xmppSession);
    }

    PubSubNode(String node, String name, Type type, Jid pubSubServiceAddress, XmppSession xmppSession) {
        this.nodeId = node;
        this.name = name;
        this.type = type;
        this.pubSubServiceAddress = pubSubServiceAddress;
        this.xmppSession = xmppSession;
        this.serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);

    }

    /**
     * Gets nodeId info.
     *
     * @return The nodeId info.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     */
    public NodeInfo getNodeInfo() throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(pubSubServiceAddress);
        Identity identity = null;
        Set<PubSubFeature> features = new HashSet<>();
        PubSubMetaDataForm metaDataForm = null;

        if (infoNode != null) {
            Set<Identity> identities = infoNode.getIdentities();
            Iterator<Identity> iterator = identities.iterator();
            if (iterator.hasNext()) {
                identity = iterator.next();
            }
            for (DataForm dataForm : infoNode.getExtensions()) {
                String formType = dataForm.getFormType();
                if (PubSubMetaDataForm.FORM_TYPE.equals(formType)) {
                    metaDataForm = new PubSubMetaDataForm(dataForm);
                    break;
                }
            }
        }

        return new NodeInfo(identity, features, metaDataForm);
    }

    /**
     * Gets the items for this nodeId.
     *
     * @return The items.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-discoveritems">5.5 Discover Items for a Node</a>
     */
    public List<Item> discoverItems() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(pubSubServiceAddress, nodeId);
        List<Item> result = new ArrayList<>();
        for (org.xmpp.extension.disco.items.Item item : itemNode.getItems()) {
            // The 'name' attribute of each Service Discovery item MUST contain its ItemID
            result.add(new PubSub.ItemElement(item.getName()));
        }
        return result;
    }

    /**
     * Gets the subscriptions for this nodeId.
     *
     * @return The subscriptions for the nodeId.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-subscriptions">5.6 Retrieve Subscriptions</a>
     */
    public List<Subscription> getSubscriptions() throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSub.withSubscriptions(nodeId)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getSubscriptions();
    }


    /**
     * Gets the affiliations for this nodeId.
     *
     * @return The affiliations for all nodes.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-affiliations">5.7 Retrieve Affiliations</a>
     */
    public List<Affiliation> getAffiliations() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, PubSub.withAffiliations(nodeId)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getAffiliations();
    }

    /**
     * Subscribes to this nodeId.
     *
     * @return The subscription.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-subscribe">6.1 Subscribe to a Node</a>
     */
    public Subscription subscribe() throws XmppException {
        return subscribe(null);
    }

    /**
     * Subscribes to and configures this nodeId.
     *
     * @param dataForm The configuration form.
     * @return The subscription.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-subandconfig">6.3.7 Subscribe and Configure</a>
     */
    public Subscription subscribe(DataForm dataForm) throws XmppException {
        if (nodeId == null) {
            throw new IllegalArgumentException("nodeId must not be null");
        }
        IQ result = xmppSession.query(new IQ(IQ.Type.SET, PubSub.withSubscribe(nodeId, xmppSession.getConnectedResource().asBareJid(), dataForm)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getSubscription();
    }

    /**
     * Unsubscribes from this nodeId.
     *
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-unsubscribe">6.2 Unsubscribe from a Node</a>
     */
    public void unsubscribe() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, PubSub.withUnsubscribe(nodeId, xmppSession.getConnectedResource().asBareJid())));
    }

    /**
     * Requests the subscription options for this nodeId.
     *
     * @return The data form.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-request">6.3.2 Request</a>
     */
    public DataForm getSubscriptionOptions() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, PubSub.withOptions(nodeId, xmppSession.getConnectedResource().asBareJid())));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getOptions().getDataForm();
    }

    /**
     * Submits subscription options for this nodeId.
     *
     * @param dataForm The subscription options form.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.3.5 Form Submission</a>
     */
    public void submitSubscriptionOptions(DataForm dataForm) throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, PubSub.withOptions(nodeId, xmppSession.getConnectedResource().asBareJid())));
    }

    /**
     * Gets the default subscription options for a specific nodeId.
     *
     * @return The default subscription options.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.4 Request Default Subscription Configuration Options</a>
     */
    public DataForm getDefaultSubscriptionOptions() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, PubSub.withDefault(nodeId)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getDefault().getDataForm();
    }

    /**
     * Gets all items for this nodeId.
     *
     * @return The items.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestall">6.5.2 Requesting All Items</a>
     */
    public List<Item> getItems() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, PubSub.withItems(nodeId)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getItems();
    }

    /**
     * Gets one or more items with a given item id for a specific nodeId.
     *
     * @param ids The item ids.
     * @return The items.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-returnnotify">6.5.6 Returning Notifications Only</a>
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestone">6.5.8 Requesting a Particular Item</a>
     */
    public List<Item> getItems(String... ids) throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, PubSub.withItems(nodeId, ids)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getItems();
    }


    /**
     * Gets the most recent items.
     *
     * @param maxItems The maximal number of items.
     * @return The items.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestrecent">6.5.7 Requesting the Most Recent Items</a>
     */
    public List<Item> getItems(int maxItems) throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, PubSub.withItems(nodeId, maxItems)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getItems();
    }

    /**
     * Publishes an item to this nodeId.
     *
     * @param item The item to be published. Note that this item must be known to the {@link org.xmpp.XmppContext}, so that it can be marshalled into XML.
     * @return The item id, generated by the pubsub service.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public String publish(Object item) throws XmppException {
        return publish(null, item);
    }

    /**
     * Publishes an item to this nodeId.
     *
     * @param id   The item's id.
     * @param item The item to be published. Note that this item must be known to the {@link org.xmpp.XmppContext}, so that it can be marshalled into XML.
     * @return The item id.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public String publish(String id, Object item) throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.SET, PubSub.withPublish(nodeId, id, item)));
        PubSub pubSub = result.getExtension(PubSub.class);
        if (pubSub != null && pubSub.getPublish() != null && pubSub.getPublish().getItem() != null) {
            return pubSub.getPublish().getItem().getId();
        }
        return id;
    }

    /**
     * Deletes an item from this nodeId.
     *
     * @param id     The item id.
     * @param notify If the pubsub service shall notify the subscribers about the deletion.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-delete">7.2 Delete an Item from a Node</a>
     */
    private void deleteItem(String id, boolean notify) throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, PubSub.withRetract(nodeId, id, notify)));
    }

    /**
     * Creates a nodeId.
     *
     * @return The nodeId id.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create">8.1 Create a Node</a>
     */
    public String create() throws XmppException {
        return create(null);
    }

    /**
     * Creates and configures this nodeId.
     *
     * @param dataForm The configuration form.
     * @return The nodeId id.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create-and-configure">8.1.3 Create and Configure a Node</a>
     */
    public String create(DataForm dataForm) throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.SET, PubSub.withCreate(nodeId, dataForm)));
        if (nodeId != null) {
            return nodeId;
        }
        PubSub pubSub = result.getExtension(PubSub.class);
        if (pubSub != null) {
            String generatedNodeId = pubSub.getNode();
            if (generatedNodeId != null) {
                this.nodeId = generatedNodeId;
            }
        }
        return nodeId;
    }

    /**
     * Gets the nodeId configuration form.
     *
     * @return The configuration form.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure-request">8.2.1 Request</a>
     */
    public DataForm getNodeConfiguration() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, PubSubOwner.withConfigure(nodeId)));
        PubSubOwner pubSubOwner = result.getExtension(PubSubOwner.class);
        return pubSubOwner.getConfigurationForm();
    }

    /**
     * Submits the nodeId configuration form.
     *
     * @param dataForm The configuration form.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure-submit">8.2.4 Form Submission</a>
     */
    public void submitNodeConfiguration(DataForm dataForm) throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, PubSubOwner.withConfigure(nodeId, dataForm)));
    }

    /**
     * Deletes a nodeId on the pubsub service.
     *
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
     */
    public void delete() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, PubSubOwner.withDelete(nodeId)));
    }

    /**
     * Deletes this nodeId and specifies a replacement nodeId.
     *
     * @param uri The replacement nodeId.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
     */
    public void delete(URI uri) throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, PubSubOwner.withDelete(nodeId, uri)));
    }

    /**
     * Purges this nodeId of all published items.
     *
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-purge">8.5 Purge All Node Items</a>
     */
    public void purge() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, PubSubOwner.withPurge(nodeId)));
    }

    /**
     * Gets the (sub-)nodes, which hierarchically reside under this nodeId, e.g. the "second-level" nodes.
     *
     * @return The list of nodes.
     * @throws org.xmpp.stanza.StanzaException If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException    If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-nodes">5.2 Discover Nodes</a>
     */
    public List<PubSubNode> getNodes() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(pubSubServiceAddress, nodeId);
        List<PubSubNode> nodes = new ArrayList<>();
        for (org.xmpp.extension.disco.items.Item item : itemNode.getItems()) {
            PubSubNode n = new PubSubNode(item.getNode(), item.getName(), pubSubServiceAddress, xmppSession);
            nodes.add(n);
        }
        return nodes;
    }

    /**
     * Gets the node id.
     *
     * @return The node id.
     */
    public String getId() {
        return nodeId;
    }

    /**
     * Gets the name of the nodeId.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of the node.
     *
     * @return The node.
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return nodeId;
    }

    /**
     * The nodeId type.
     */
    public enum Type {
        LEAF,
        COLLECTION,
    }
}
