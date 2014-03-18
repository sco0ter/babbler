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

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.extension.pubsub.owner.PubSubOwner;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class PubSubService {

    private static final Logger logger = Logger.getLogger(PubSubService.class.getName());

    private final Jid service;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Connection connection;

    PubSubService(Jid service, Connection connection, ServiceDiscoveryManager serviceDiscoveryManager) {
        this.service = service;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.connection = connection;
    }

    // ENTITY USE CASES START

    /**
     * Gets the features, which are supported by the pubsub service.
     *
     * @return The set of supported features.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-features">5.1 Discover Features</a>
     */
    public Set<PubSubFeature> getFeatures() throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(service);
        return getFeatures(infoNode);
    }

    Set<PubSubFeature> getFeatures(InfoNode infoNode) {
        EnumSet<PubSubFeature> features = EnumSet.noneOf(PubSubFeature.class);
        for (Feature feature : infoNode.getFeatures()) {
            if (feature.getVar().startsWith(PubSub.NAMESPACE + "#")) {
                String f = feature.getVar().substring(feature.getVar().indexOf("#") + 1);
                try {
                    PubSubFeature pubSubFeature = PubSubFeature.valueOf(f.toUpperCase().replace("-", "_"));
                    if (pubSubFeature != null) {
                        features.add(pubSubFeature);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Server advertised unknown pubsub feature: " + f);
                }
            }
        }
        return features;
    }

    /**
     * Gets the first-level nodes of the pubsub service.
     *
     * @return The list of nodes.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-nodes">5.2 Discover Nodes</a>
     */
    private List<Node> getNodes() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(service);
        List<Node> nodes = new ArrayList<>();
        for (org.xmpp.extension.disco.items.Item item : itemNode.getItems()) {
            Node node = new Node(item.getNode(), item.getName());
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * Gets the (sub-)nodes, which hierarchically reside under the given node, e.g. the "second-level" nodes.
     *
     * @param node The node.
     * @return The list of nodes.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-nodes">5.2 Discover Nodes</a>
     */
    private List<Node> getNodes(String node) throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(service);
        List<Node> nodes = new ArrayList<>();
        for (org.xmpp.extension.disco.items.Item item : itemNode.getItems()) {
            Node n = new Node(item.getNode(), item.getName());
            nodes.add(n);
        }
        return nodes;
    }

    /**
     * Gets a node.
     *
     * @param node The node.
     * @return The node.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-info">5.3 Discover Node Information</a>
     */
    private Node getNode(String node) throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(service, node);
        Node result = new Node();
        result.setNode(node);
        if (infoNode.getIdentities() != null && infoNode.getIdentities().iterator().hasNext()) {
            Identity identity = infoNode.getIdentities().iterator().next();
            result.setName(identity.getName());
            result.setType("leaf".equals(identity.getType()) ? Node.Type.LEAF : Node.Type.COLLECTION);
        }
        // TODO node meta information.
        return result;
    }

    /**
     * Gets the items for a node.
     *
     * @param node The node.
     * @return The items.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-discoveritems">5.5 Discover Items for a Node</a>
     */
    private List<Item> getItems(String node) throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(service, node);
        List<Item> result = new ArrayList<>();
        for (org.xmpp.extension.disco.items.Item item : itemNode.getItems()) {
            // The 'name' attribute of each Service Discovery item MUST contain its ItemID
            result.add(new PubSub.ItemElement(item.getName()));
        }
        return result;
    }

    /**
     * Gets the subscriptions for all nodes.
     *
     * @return The subscriptions for all nodes.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-subscriptions">5.6 Retrieve Subscriptions</a>
     */
    private List<Subscription> getSubscriptions() throws XmppException {
        IQ result = connection.query(new IQ(service, IQ.Type.GET, PubSub.withSubscriptions()));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getSubscriptions();
    }

    /**
     * Gets the subscriptions for a specific node.
     *
     * @param node The node.
     * @return The subscriptions for the node.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-subscriptions">5.6 Retrieve Subscriptions</a>
     */
    private List<Subscription> getSubscriptions(String node) throws XmppException {
        IQ result = connection.query(new IQ(service, IQ.Type.GET, PubSub.withSubscriptions(node)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getSubscriptions();
    }

    /**
     * Gets the affiliations for all nodes.
     *
     * @return The affiliations for all nodes.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-affiliations">5.7 Retrieve Affiliations</a>
     */
    private List<AffiliationNode> getAffiliations() throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSub.withAffiliations()));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getAffiliations();
    }

    /**
     * Gets the affiliations for a specific node.
     *
     * @param node The node.
     * @return The affiliations for all nodes.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-affiliations">5.7 Retrieve Affiliations</a>
     */
    private List<AffiliationNode> getAffiliations(String node) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSub.withAffiliations(node)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getAffiliations();
    }

    // ENTITY USE CASES END

    // SUBSCRIBER USE CASES START

    /**
     * Subscribes to a node.
     *
     * @param node The node to subscribe to.
     * @return The subscription.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-subscribe">6.1 Subscribe to a Node</a>
     */
    public Subscription subscribe(String node) throws XmppException {
        return subscribe(node, null);
    }

    /**
     * Subscribes to and configures a node.
     *
     * @param node     The node to subscribe to.
     * @param dataForm The configuration form.
     * @return The subscription.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-subandconfig">6.3.7 Subscribe and Configure</a>
     */
    public Subscription subscribe(String node, DataForm dataForm) throws XmppException {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        IQ result = connection.query(new IQ(IQ.Type.SET, PubSub.withSubscribe(node, connection.getConnectedResource().toBareJid(), dataForm)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getSubscription();
    }

    /**
     * Unsubscribes from a node.
     *
     * @param node The node to unsubscribe from.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-unsubscribe">6.2 Unsubscribe from a Node</a>
     */
    public void unsubscribe(String node) throws XmppException {
        connection.query(new IQ(IQ.Type.SET, PubSub.withUnsubscribe(node, connection.getConnectedResource().toBareJid())));
    }

    /**
     * Requests the subscription options for a node.
     *
     * @param node The node.
     * @return The data form.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-request">6.3.2 Request</a>
     */
    private DataForm getSubscriptionOptions(String node) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSub.withOptions(node, connection.getConnectedResource().toBareJid())));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getOptions().getDataForm();
    }

    /**
     * Submits subscription options.
     *
     * @param node     The node.
     * @param dataForm The configuration form.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.3.5 Form Submission</a>
     */
    private void submitSubscriptionOptions(String node, DataForm dataForm) throws XmppException {
        connection.query(new IQ(IQ.Type.SET, PubSub.withOptions(node, connection.getConnectedResource().toBareJid())));
    }

    /**
     * Gets the default subscription options for the pubsub service.
     *
     * @return The default subscription options.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.4 Request Default Subscription Configuration Options</a>
     */
    private DataForm getDefaultSubscriptionOptions() throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSub.withDefault()));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getOptions().getDataForm();
    }

    /**
     * Gets the default subscription options for a specific node.
     *
     * @param node The node.
     * @return The default subscription options.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.4 Request Default Subscription Configuration Options</a>
     */
    private DataForm getDefaultSubscriptionOptions(String node) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSub.withDefault(node)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getOptions().getDataForm();
    }

    /**
     * Gets all items for a specific node.
     *
     * @param node The node.
     * @return The items.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestall">6.5.2 Requesting All Items</a>
     */
    private List<Item> getAllItems(String node) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSub.withItems(node)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getItems();
    }

    /**
     * Gets one or more items with a given item id for a specific node.
     *
     * @param node The node.
     * @param ids  The item ids.
     * @return The items.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-returnnotify">6.5.6 Returning Notifications Only</a>
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestone">6.5.8 Requesting a Particular Item</a>
     */
    private List<Item> getItems(String node, String... ids) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSub.withItems(node, ids)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getItems();
    }

    /**
     * Gets the most recent items.
     *
     * @param node     The node.
     * @param maxItems The maximal number of items.
     * @return The items.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestrecent">6.5.7 Requesting the Most Recent Items</a>
     */
    private List<Item> getItems(String node, int maxItems) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSub.withItems(node, maxItems)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getItems();
    }

    // SUBSCRIBER USE CASES END

    // PUBLISHER USE CASES START

    /**
     * Publishes an item to a node.
     *
     * @param node The node.
     * @param item The item to be published. Note that this item must be known to the {@link org.xmpp.XmppContext}, so that it can be marshalled into XML.
     * @return The item id, generated by the pubsub service.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public String publish(String node, Object item) throws XmppException {
        return publish(node, null, item);
    }

    /**
     * Publishes an item to a node.
     *
     * @param node The node.
     * @param id   The item's id.
     * @param item The item to be published. Note that this item must be known to the {@link org.xmpp.XmppContext}, so that it can be marshalled into XML.
     * @return The item id.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public String publish(String node, String id, Object item) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.SET, PubSub.withPublish(node, id, item)));
        PubSub pubSub = result.getExtension(PubSub.class);
        if (pubSub != null && pubSub.getPublish() != null && pubSub.getPublish().getItem() != null) {
            return pubSub.getPublish().getItem().getId();
        }
        return id;
    }

    /**
     * Deletes an item.
     *
     * @param node   The node.
     * @param id     The item id.
     * @param notify If the pubsub service shall notify the subscribers about the deletion.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-delete">7.2 Delete an Item from a Node</a>
     */
    private void deleteItem(String node, String id, boolean notify) throws XmppException {
        connection.query(new IQ(IQ.Type.SET, PubSub.withRetract(node, id, notify)));
    }

    // PUBLISHER USE CASES END

    /**
     * Creates an instant node.
     *
     * @return The node id, which is generated by the pubsub service.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create">8.1 Create a Node</a>
     */
    private String createNode() throws XmppException {
        return createNode(null, null);
    }

    /**
     * Creates a node.
     *
     * @param node The node.
     * @return The node id.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create">8.1 Create a Node</a>
     */
    private String createNode(String node) throws XmppException {
        return createNode(node, null);
    }

    /**
     * Creates and configures a node.
     *
     * @param node     The node.
     * @param dataForm The configuration form.
     * @return The node id.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create-and-configure">8.1.3 Create and Configure a Node</a>
     */
    private String createNode(String node, DataForm dataForm) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.SET, PubSub.withCreate(node, dataForm)));
        if (node != null) {
            return node;
        }
        PubSub pubSub = result.getExtension(PubSub.class);
        if (pubSub != null) {
            return pubSub.getNode();
        }
        return null;
    }

    /**
     * Gets the node configuration form.
     *
     * @param node The node.
     * @return The configuration form.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure-request">8.2.1 Request</a>
     */
    private DataForm getNodeConfiguration(String node) throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSubOwner.withConfigure(node)));
        PubSubOwner pubSubOwner = result.getExtension(PubSubOwner.class);
        return pubSubOwner.getConfigurationForm();
    }

    /**
     * Submits the node configuration form.
     *
     * @param node     The node.
     * @param dataForm The configuration form.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-configure-submit">8.2.4 Form Submission</a>
     */
    private void submitNodeConfiguration(String node, DataForm dataForm) throws XmppException {
        connection.query(new IQ(IQ.Type.SET, PubSubOwner.withConfigure(node, dataForm)));
    }

    /**
     * Gets the default node configuration form.
     *
     * @return The configuration form.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-default">8.3 Request Default Node Configuration Options</a>
     */
    private DataForm getDefaultNodeConfiguration() throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, PubSubOwner.withDefault()));
        PubSubOwner pubSubOwner = result.getExtension(PubSubOwner.class);
        return pubSubOwner.getConfigurationForm();
    }

    /**
     * Deletes a node.
     *
     * @param node The node.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
     */
    private void deleteNode(String node) throws XmppException {
        connection.query(new IQ(IQ.Type.SET, PubSubOwner.withDelete(node)));
    }

    /**
     * Deletes a node and specifies a replacement node.
     *
     * @param node The node.
     * @param uri  The replacement node.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-delete">8.4 Delete a Node</a>
     */
    private void deleteNode(String node, URI uri) throws XmppException {
        connection.query(new IQ(IQ.Type.SET, PubSubOwner.withDelete(node, uri)));
    }

    /**
     * Purges a node of all published items.
     *
     * @param node The node.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-purge">8.5 Purge All Node Items</a>
     */
    private void purgeNode(String node) throws XmppException {
        connection.query(new IQ(IQ.Type.SET, PubSubOwner.withPurge(node)));
    }
}
