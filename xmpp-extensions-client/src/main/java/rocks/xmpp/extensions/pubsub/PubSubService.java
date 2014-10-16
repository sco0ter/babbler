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
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.pubsub.model.Affiliation;
import rocks.xmpp.extensions.pubsub.model.PubSub;
import rocks.xmpp.extensions.pubsub.model.PubSubFeature;
import rocks.xmpp.extensions.pubsub.model.Subscription;
import rocks.xmpp.extensions.pubsub.model.owner.PubSubOwner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class PubSubService {

    private static final Logger logger = Logger.getLogger(PubSubService.class.getName());

    private final Jid service;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final XmppSession xmppSession;

    PubSubService(Jid service, XmppSession xmppSession, ServiceDiscoveryManager serviceDiscoveryManager) {
        this.service = service;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.xmppSession = xmppSession;
    }

    /**
     * Gets the features, which are supported by the pubsub service.
     *
     * @return The set of supported features.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-features">5.1 Discover Features</a>
     */
    public Collection<PubSubFeature> getFeatures() throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(service);
        return getFeatures(infoNode);
    }

    Collection<PubSubFeature> getFeatures(InfoNode infoNode) {
        Collection<PubSubFeature> features = EnumSet.noneOf(PubSubFeature.class);
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
     * Gets the first-level nodes of this pubsub service.
     *
     * @return The list of nodes.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-nodes">5.2 Discover Nodes</a>
     */
    public List<PubSubNode> getNodes() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(service);
        List<PubSubNode> nodes = new ArrayList<>();
        for (Item item : itemNode.getItems()) {
            PubSubNode n = new PubSubNode(item.getNode(), item.getName(), service, xmppSession);
            nodes.add(n);
        }
        return nodes;
    }

    /**
     * Gets a node.
     *
     * @param node The node.
     * @return The node.
     */
    public PubSubNode getNode(String node) {
        return new PubSubNode(node, null, service, xmppSession);
    }

    /**
     * Gets the subscriptions for all nodes.
     *
     * @return The subscriptions for all nodes.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
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

    @Override
    public String toString() {
        return service.toString();
    }
}
