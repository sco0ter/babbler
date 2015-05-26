/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.pubsub.model.Affiliation;
import rocks.xmpp.extensions.pubsub.model.NodeType;
import rocks.xmpp.extensions.pubsub.model.PubSub;
import rocks.xmpp.extensions.pubsub.model.PubSubFeature;
import rocks.xmpp.extensions.pubsub.model.Subscription;
import rocks.xmpp.extensions.pubsub.model.owner.PubSubOwner;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class acts a facade to deal with a remote pubsub service.
 * <p>
 * E.g. it lets you get all your subscriptions on the service, let's you work with nodes (e.g. subscribe to nodes)
 * or let's you discover the features provided by the remote service.
 * <p>
 * To work with pubsub nodes, {@linkplain #node(String) create a local node instance}, which can be used to work the remote node (e.g. subscribe to the node).
 *
 * @author Christian Schudt
 */
public final class PubSubService {

    private static final Logger logger = Logger.getLogger(PubSubService.class.getName());

    private final Jid service;

    private final String name;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final XmppSession xmppSession;

    PubSubService(Jid service, String name, XmppSession xmppSession, ServiceDiscoveryManager serviceDiscoveryManager) {
        this.service = service;
        this.name = name;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.xmppSession = xmppSession;
    }

    /**
     * Discovers the features, which are supported by the pubsub service.
     *
     * @return The set of supported features.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-features">5.1 Discover Features</a>
     */
    public Collection<PubSubFeature> discoverFeatures() throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(service);
        return getFeatures(infoNode);
    }

    Collection<PubSubFeature> getFeatures(InfoNode infoNode) {
        Collection<PubSubFeature> features = EnumSet.noneOf(PubSubFeature.class);
        infoNode.getFeatures().stream().filter(feature -> feature.startsWith(PubSub.NAMESPACE + "#")).forEach(feature -> {
            String f = feature.substring(feature.indexOf("#") + 1);
            try {
                PubSubFeature pubSubFeature = PubSubFeature.valueOf(f.toUpperCase().replace("-", "_"));
                if (pubSubFeature != null) {
                    features.add(pubSubFeature);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Server advertised unknown pubsub feature: {0}", f);
            }
        });
        return features;
    }

    /**
     * Discovers the first-level nodes of this pubsub service.
     *
     * @return The list of nodes.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-nodes">5.2 Discover Nodes</a>
     */
    public List<PubSubNode> discoverNodes() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(service);
        return itemNode.getItems().stream().map(item -> new PubSubNode(item.getNode(), service, xmppSession)).collect(Collectors.toList());
    }

    /**
     * Creates a pubsub node locally, which can be used to work with a node at the pubsub service.
     *
     * @param node The node.
     * @return The node.
     */
    public PubSubNode node(String node) {
        return new PubSubNode(node, NodeType.LEAF, service, xmppSession);
    }

    /**
     * Gets the subscriptions for all nodes.
     *
     * @return The subscriptions for all nodes.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-subscriptions">5.6 Retrieve Subscriptions</a>
     */
    private List<Subscription> getSubscriptions() throws XmppException {
        IQ result = xmppSession.query(new IQ(service, IQ.Type.GET, PubSub.withSubscriptions()));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getSubscriptions();
    }

    /**
     * Gets the affiliations for all nodes.
     *
     * @return The affiliations for all nodes.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-affiliations">5.7 Retrieve Affiliations</a>
     */
    private List<Affiliation> getAffiliations() throws XmppException {
        IQ result = xmppSession.query(new IQ(service, IQ.Type.GET, PubSub.withAffiliations()));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getAffiliations();
    }

    /**
     * Gets the default subscription options for this pubsub service.
     *
     * @return The default subscription options.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.4 Request Default Subscription Configuration Options</a>
     */
    private DataForm getDefaultSubscriptionOptions() throws XmppException {
        IQ result = xmppSession.query(new IQ(service, IQ.Type.GET, PubSub.withDefault()));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getDefault().getDataForm();
    }

    /**
     * Gets the default node configuration form for this pubsub service.
     *
     * @return The configuration form.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-default">8.3 Request Default Node Configuration Options</a>
     */
    private DataForm getDefaultNodeConfiguration() throws XmppException {
        IQ result = xmppSession.query(new IQ(service, IQ.Type.GET, PubSubOwner.withDefault()));
        PubSubOwner pubSubOwner = result.getExtension(PubSubOwner.class);
        return pubSubOwner.getConfigurationForm();
    }

    /**
     * Gets the service address.
     *
     * @return The service address.
     */
    public Jid getAddress() {
        return service;
    }

    /**
     * Returns the service address.
     *
     * @return The service address.
     */
    @Override
    public String toString() {
        return service.toString();
    }

    /**
     * Gets the name of this service.
     *
     * @return The name or null, if the name is unknown.
     */
    public String getName() {
        return name;
    }
}
