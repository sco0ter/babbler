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

package rocks.xmpp.extensions.pubsub;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.pubsub.model.*;
import rocks.xmpp.extensions.pubsub.model.owner.PubSubOwner;

import java.net.URI;
import java.util.*;

/**
 * @author Christian Schudt
 */
public final class PubSubNode {

    private final String name;

    private final NodeType type;

    private final Jid pubSubServiceAddress;

    private final XmppSession xmppSession;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private String nodeId;

    PubSubNode(String nodeId, String name, Jid pubSubServiceAddress, XmppSession xmppSession) {
        // If a node is a leaf node rather than a collection node and items have been published to the node, the service MAY return one <item/> element for each published item as described in the Discover Items for a Node section of this document, however such items MUST NOT include a 'node' attribute (since they are published items, not nodes).
        this(nodeId, name, nodeId == null ? NodeType.LEAF : NodeType.COLLECTION, pubSubServiceAddress, xmppSession);
    }

    PubSubNode(String nodeId, String name, NodeType type, Jid pubSubServiceAddress, XmppSession xmppSession) {
        this.nodeId = nodeId;
        this.name = name;
        this.type = type;
        this.pubSubServiceAddress = pubSubServiceAddress;
        this.xmppSession = xmppSession;
        this.serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);

    }

    /**
     * Gets node info, which consists of a node name, type and meta info.
     *
     * @return The node info.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-info">5.3 Discover Node Information</a>
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-metadata">5.4 Discover Node Metadata</a>
     */
    public NodeInfo getNodeInfo() throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(pubSubServiceAddress);
        Identity identity = null;
        Set<PubSubFeature> features = new HashSet<>();
        NodeMetaData metaDataForm = null;

        if (infoNode != null) {
            Set<Identity> identities = infoNode.getIdentities();
            Iterator<Identity> iterator = identities.iterator();
            if (iterator.hasNext()) {
                identity = iterator.next();
            }
            for (DataForm dataForm : infoNode.getExtensions()) {
                String formType = dataForm.getFormType();
                if (NodeMetaData.FORM_TYPE.equals(formType)) {
                    metaDataForm = new NodeMetaData(dataForm);
                    break;
                }
            }
        }

        return new NodeInfo(identity, features, metaDataForm);
    }

    /**
     * Gets the items for this node.
     *
     * @return The items.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-discoveritems">5.5 Discover Items for a Node</a>
     */
    public List<Item> discoverItems() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(pubSubServiceAddress, nodeId);
        List<Item> result = new ArrayList<>();
        for (rocks.xmpp.extensions.disco.model.items.Item item : itemNode.getItems()) {
            // The 'name' attribute of each Service Discovery item MUST contain its ItemID
            result.add(new PubSub.ItemElement(item.getName()));
        }
        return result;
    }

    /**
     * Gets the subscriptions for this node.
     *
     * @return The subscriptions for the node.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-subscriptions">5.6 Retrieve Subscriptions</a>
     */
    public List<Subscription> getSubscriptions() throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSub.withSubscriptions(nodeId)));
        return result.getExtension(PubSub.class).getSubscriptions();
    }


    /**
     * Gets the affiliations for this node.
     *
     * @return The affiliations for all nodes.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-affiliations">5.7 Retrieve Affiliations</a>
     */
    public List<Affiliation> getAffiliations() throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSub.withAffiliations(nodeId)));
        return result.getExtension(PubSub.class).getAffiliations();
    }

    /**
     * Subscribes to this node.
     *
     * @return The subscription.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-subscribe">6.1 Subscribe to a Node</a>
     */
    public Subscription subscribe() throws XmppException {
        return subscribe(null);
    }

    /**
     * Subscribes to and configures this node.
     *
     * @param subscribeOptions The configuration form.
     * @return The subscription.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-subandconfig">6.3.7 Subscribe and Configure</a>
     */
    public Subscription subscribe(SubscribeOptions subscribeOptions) throws XmppException {
        if (nodeId == null) {
            throw new IllegalArgumentException("nodeId must not be null");
        }
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSub.withSubscribe(nodeId, xmppSession.getConnectedResource().asBareJid(), subscribeOptions != null ? subscribeOptions.getDataForm() : null)));
        return result.getExtension(PubSub.class).getSubscription();
    }

    /**
     * Unsubscribes from this node.
     *
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-unsubscribe">6.2 Unsubscribe from a Node</a>
     */
    public void unsubscribe() throws XmppException {
        this.unsubscribe(null);
    }

    /**
     * Unsubscribes from this node.
     *
     * @param subscriptionId The subscription id.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-unsubscribe">6.2 Unsubscribe from a Node</a>
     */
    public void unsubscribe(String subscriptionId) throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSub.withUnsubscribe(nodeId, xmppSession.getConnectedResource().asBareJid(), subscriptionId)));
    }

    /**
     * Requests the subscription options for this node.
     *
     * @return The data form.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-request">6.3.2 Request</a>
     * @see #configureSubscription(rocks.xmpp.extensions.pubsub.model.SubscribeOptions)
     */
    public SubscribeOptions getSubscriptionOptions() throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSub.withOptions(nodeId, xmppSession.getConnectedResource().asBareJid(), null)));
        return new SubscribeOptions(result.getExtension(PubSub.class).getOptions().getDataForm());
    }

    /**
     * Submits subscription options for this node.
     *
     * @param dataForm The subscription options form.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.3.5 Form Submission</a>
     * @deprecated Use {@link #configureSubscription(rocks.xmpp.extensions.pubsub.model.SubscribeOptions)}
     */
    @Deprecated
    public void submitSubscriptionOptions(DataForm dataForm) throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSub.withOptions(nodeId, xmppSession.getConnectedResource().asBareJid(), dataForm)));
    }

    /**
     * Submits subscription options for this node.
     *
     * @param subscribeOptions The subscription options form.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.3.5 Form Submission</a>
     */
    public void configureSubscription(SubscribeOptions subscribeOptions) throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSub.withOptions(nodeId, xmppSession.getConnectedResource().asBareJid(), subscribeOptions.getDataForm())));
    }

    /**
     * Gets the default subscription options for a specific node.
     *
     * @return The default subscription options.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.4 Request Default Subscription Configuration Options</a>
     */
    public SubscribeOptions getDefaultSubscriptionOptions() throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSub.withDefault(nodeId)));
        return new SubscribeOptions(result.getExtension(PubSub.class).getDefault().getDataForm());
    }

    /**
     * Gets all items for this node.
     *
     * @return The items.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestall">6.5.2 Requesting All Items</a>
     */
    public List<Item> getItems() throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSub.withItems(nodeId)));
        return result.getExtension(PubSub.class).getItems();
    }

    /**
     * Gets one or more items with a given item id for a specific node.
     *
     * @param ids The item ids.
     * @return The items.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-returnnotify">6.5.6 Returning Notifications Only</a>
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestone">6.5.8 Requesting a Particular Item</a>
     */
    public List<Item> getItems(String... ids) throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSub.withItems(nodeId, ids)));
        return result.getExtension(PubSub.class).getItems();
    }


    /**
     * Gets the most recent items.
     *
     * @param maxItems The maximal number of items.
     * @return The items.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestrecent">6.5.7 Requesting the Most Recent Items</a>
     */
    public List<Item> getItems(int maxItems) throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSub.withItems(nodeId, maxItems)));
        return result.getExtension(PubSub.class).getItems();
    }

    /**
     * Publishes an item to this node.
     *
     * @param item The item to be published. Note that this item must be known to the {@link rocks.xmpp.core.session.context.CoreContext}, so that it can be marshalled into XML.
     * @return The item id, generated by the pubsub service.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public String publish(Object item) throws XmppException {
        return publish(null, item);
    }

    /**
     * Publishes an item to this node.
     *
     * @param item           The item to be published. Note that this item must be known to the {@link rocks.xmpp.core.session.context.CoreContext}, so that it can be marshalled into XML.
     * @param publishOptions The optional publish options.
     * @return The item id, generated by the pubsub service.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public String publish(Object item, PublishOptions publishOptions) throws XmppException {
        return publish(null, item, publishOptions);
    }


    /**
     * Publishes an item to this node.
     *
     * @param id   The item's id.
     * @param item The item to be published. Note that this item must be known to the {@link rocks.xmpp.core.session.context.CoreContext}, so that it can be marshalled into XML.
     * @return The item id.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public String publish(String id, Object item) throws XmppException {
        return publish(id, item, null);
    }

    /**
     * Publishes an item to this node.
     *
     * @param id             The item's id.
     * @param item           The item to be published. Note that this item must be known to the {@link rocks.xmpp.core.session.context.CoreContext}, so that it can be marshalled into XML.
     * @param publishOptions The optional publish options.
     * @return The item id.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish-options">7.1.5 Publishing Options</a>
     */
    public String publish(String id, Object item, PublishOptions publishOptions) throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSub.withPublish(nodeId, id, item, publishOptions != null ? publishOptions.getDataForm() : null)));
        PubSub pubSub = result.getExtension(PubSub.class);
        if (pubSub != null && pubSub.getPublish() != null && pubSub.getPublish().getItem() != null) {
            return pubSub.getPublish().getItem().getId();
        }
        return id;
    }

    /**
     * Deletes an item from this node.
     *
     * @param id     The item id.
     * @param notify If the pubsub service shall notify the subscribers about the deletion.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-delete">7.2 Delete an Item from a Node</a>
     */
    private void deleteItem(String id, boolean notify) throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSub.withRetract(nodeId, id, notify)));
    }

    /**
     * Creates a node.
     *
     * @return The node id.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create">8.1 Create a Node</a>
     */
    public String create() throws XmppException {
        return create(null);
    }

    /**
     * Creates and configures this node.
     *
     * @param nodeConfiguration The configuration form.
     * @return The node id.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create-and-configure">8.1.3 Create and Configure a Node</a>
     */
    public String create(NodeConfiguration nodeConfiguration) throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSub.withCreate(nodeId, nodeConfiguration.getDataForm())));
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
     * Gets the node configuration form.
     *
     * @return The configuration form.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure-request">8.2.1 Request</a>
     */
    public NodeConfiguration getNodeConfiguration() throws XmppException {
        IQ result = xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.GET, PubSubOwner.withConfigure(nodeId)));
        PubSubOwner pubSubOwner = result.getExtension(PubSubOwner.class);
        return new NodeConfiguration(pubSubOwner.getConfigurationForm());
    }

    /**
     * Submits the node configuration form.
     *
     * @param dataForm The configuration form.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure-submit">8.2.4 Form Submission</a>
     * @deprecated Use {@link #configureNode(rocks.xmpp.extensions.pubsub.model.NodeConfiguration)}
     */
    @Deprecated
    public void submitNodeConfiguration(DataForm dataForm) throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSubOwner.withConfigure(nodeId, dataForm)));
    }

    /**
     * Submits the node configuration form.
     *
     * @param nodeConfiguration The configuration form.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure-submit">8.2.4 Form Submission</a>
     */
    public void configureNode(NodeConfiguration nodeConfiguration) throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSubOwner.withConfigure(nodeId, nodeConfiguration.getDataForm())));
    }

    /**
     * Deletes a node on the pubsub service.
     *
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
     */
    public void delete() throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSubOwner.withDelete(nodeId)));
    }

    /**
     * Deletes this node and specifies a replacement node.
     *
     * @param uri The replacement node.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
     */
    public void delete(URI uri) throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSubOwner.withDelete(nodeId, uri)));
    }

    /**
     * Purges this node of all published items.
     *
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-purge">8.5 Purge All Node Items</a>
     */
    public void purge() throws XmppException {
        xmppSession.query(new IQ(pubSubServiceAddress, IQ.Type.SET, PubSubOwner.withPurge(nodeId)));
    }

    /**
     * Gets the (sub-)nodes, which hierarchically reside under this node, e.g. the "second-level" nodes.
     *
     * @return The list of nodes.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-nodes">5.2 Discover Nodes</a>
     */
    public List<PubSubNode> getNodes() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(pubSubServiceAddress, nodeId);
        List<PubSubNode> nodes = new ArrayList<>();
        for (rocks.xmpp.extensions.disco.model.items.Item item : itemNode.getItems()) {
            PubSubNode n = new PubSubNode(item.getNode(), item.getName(), pubSubServiceAddress, xmppSession);
            nodes.add(n);
        }
        return nodes;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
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
     * Gets the type of this node.
     *
     * @return The type.
     */
    public NodeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return nodeId;
    }
}
