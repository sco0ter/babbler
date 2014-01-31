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

package org.xmpp.extension.servicediscovery;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.dataforms.DataForm;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.extension.servicediscovery.info.Identity;
import org.xmpp.extension.servicediscovery.info.InfoDiscovery;
import org.xmpp.extension.servicediscovery.info.InfoNode;
import org.xmpp.extension.servicediscovery.items.Item;
import org.xmpp.extension.servicediscovery.items.ItemDiscovery;
import org.xmpp.extension.servicediscovery.items.ItemNode;
import org.xmpp.stanza.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;

/**
 * Manages <a href="http://xmpp.org/extensions/xep-0030.html">XEP-0030: Service Discovery</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0030.html#intro">1. Introduction</a></cite></p>
 * <p>The ability to discover information about entities on the Jabber network is extremely valuable. Such information might include features offered or protocols supported by the entity, the entity's type or identity, and additional entities that are associated with the original entity in some way (often thought of as "children" of the "parent" entity).</p>
 * </blockquote>
 * <p>Enabled extensions are automatically added to the list of features by their respective manager class.
 * Disabled extensions are removed.</p>
 * <p>
 * This class automatically manages incoming service discovery requests by responding with a list of enabled extensions (features).
 * </p>
 *
 * @author Christian Schudt
 */
public final class ServiceDiscoveryManager extends ExtensionManager implements InfoNode, ItemNode {

    private static final String FEATURE_INFO = "http://jabber.org/protocol/disco#info";

    private static final String FEATURE_ITEMS = "http://jabber.org/protocol/disco#items";

    private static Identity defaultIdentity = new Identity("client", "pc");

    private final Set<Identity> identities = new CopyOnWriteArraySet<>();

    private final Set<Feature> features = new CopyOnWriteArraySet<>();

    private final List<DataForm> extensions = new CopyOnWriteArrayList<>();

    private final List<Item> items = new CopyOnWriteArrayList<>();

    private final Map<String, InfoNode> infoNodeMap = new ConcurrentHashMap<>();

    private final Map<String, ItemNode> itemNodeMap = new ConcurrentHashMap<>();

    private ServiceDiscoveryManager(final Connection connection) {
        super(connection);
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && iq.getType() == IQ.Type.GET) {
                    InfoDiscovery infoDiscovery = iq.getExtension(InfoDiscovery.class);
                    if (infoDiscovery != null) {
                        if (isEnabled()) {

                            if (infoDiscovery.getNode() == null) {
                                Set<Identity> ids;
                                //  Every entity MUST have at least one identity
                                if (!identities.isEmpty()) {
                                    ids = new HashSet<>(identities);
                                } else {
                                    ids = new HashSet<>();
                                    ids.add(defaultIdentity);
                                }
                                IQ result = iq.createResult();
                                result.setExtension(new InfoDiscovery(ids, features, extensions));
                                connection.send(result);
                            } else {
                                InfoNode infoNode = infoNodeMap.get(infoDiscovery.getNode());
                                if (infoNode != null) {
                                    IQ result = iq.createResult();
                                    result.setExtension(new InfoDiscovery(infoNode.getNode(), infoNode.getIdentities(), infoNode.getFeatures(), infoNode.getExtensions()));
                                    connection.send(result);
                                } else {
                                    connection.send(iq.createError(new Stanza.Error(new Stanza.Error.ItemNotFound())));
                                }
                            }

                        } else {
                            sendServiceUnavailable(iq);
                        }
                    } else {
                        ItemDiscovery itemDiscovery = iq.getExtension(ItemDiscovery.class);
                        if (itemDiscovery != null) {
                            if (isEnabled()) {
                                if (itemDiscovery.getNode() == null) {
                                    IQ result = iq.createResult();
                                    result.setExtension(new ItemDiscovery(items));
                                    connection.send(result);
                                } else {
                                    ItemNode itemNode = itemNodeMap.get(itemDiscovery.getNode());
                                    if (itemNode != null) {
                                        IQ result = iq.createResult();
                                        result.setExtension(new ItemDiscovery(itemNode.getNode(), items));
                                        connection.send(result);
                                    } else {
                                        connection.send(iq.createError(new Stanza.Error(new Stanza.Error.ItemNotFound())));
                                    }
                                }

                            } else {
                                sendServiceUnavailable(iq);
                            }
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList(FEATURE_INFO, FEATURE_ITEMS);
    }

    @Override
    public String getNode() {
        return null;
    }

    @Override
    public List<Item> getItems() {
        return items;
    }

    @Override
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    @Override
    public Set<Identity> getIdentities() {
        return Collections.unmodifiableSet(identities);
    }

    @Override
    public List<DataForm> getExtensions() {
        return extensions;
    }

    /**
     * Adds a feature. Features should not be added or removed directly. Instead enable or disable the respective extension manager, which will then add or remove the feature.
     * That way, supported features are consistent with enabled extension managers and service discovery won't reveal features, that are in fact not supported.
     *
     * @param feature The features.
     */
    public void addFeature(Feature feature) {
        features.add(feature);
    }

    /**
     * Removes a feature.
     *
     * @param feature The feature.
     */
    public void removeFeature(Feature feature) {
        features.remove(feature);
    }

    /**
     * Adds an identity.
     *
     * @param identity The identity.
     */
    public void addIdentity(Identity identity) {
        identities.add(identity);
    }

    /**
     * Removes an identity.
     *
     * @param identity The identity.
     */
    public void removeIdentity(Identity identity) {
        identities.remove(identity);
    }

    /**
     * Discovers information about another XMPP entity.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0030.html#info">3. Discovering Information About a Jabber Entity</a></cite></p>
     * <p>A requesting entity may want to discover information about another entity on the network. The information desired generally is of two kinds:</p>
     * <ol>
     * <li>The target entity's identity.</li>
     * <li>The features offered and protocols supported by the target entity.</li>
     * </ol>
     * </blockquote>
     *
     * @param jid The entity's JID.
     * @return The service discovery result.
     * @throws StanzaException  If the entity returned an error.
     * @throws TimeoutException If the operation timed out.
     */
    public InfoNode discoverInformation(Jid jid) throws TimeoutException, StanzaException {
        return discoverInformation(jid, null);
    }

    /**
     * Discovers information about another XMPP entity targeted at a specific node.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0030.html#info-nodes">3.2 Info Nodes</a></cite></p>
     * <p>A disco#info query MAY also be directed to a specific node identifier associated with a JID.</p>
     * </blockquote>
     *
     * @param jid  The entity's JID.
     * @param node The node.
     * @return The info discovery result or null, if info discovery is not supported.
     * @throws StanzaException  If the entity returned an error.
     * @throws TimeoutException If the operation timed out.
     * @see #discoverInformation(org.xmpp.Jid)
     */
    public InfoNode discoverInformation(Jid jid, String node) throws TimeoutException, StanzaException {
        IQ iq = new IQ(jid, IQ.Type.GET, new InfoDiscovery(node));
        IQ result = connection.query(iq);
        if (result.getType() == IQ.Type.RESULT) {
            return result.getExtension(InfoDiscovery.class);
        } else {
            throw new StanzaException(result.getError());
        }
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid The JID.
     * @return The discovered items.
     * @throws TimeoutException If the operation timed out.
     */
    public ItemNode discoverItems(Jid jid) throws TimeoutException {
        return discoverItems(jid, null);
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid  The JID.
     * @param node The node.
     * @return The discovered items.
     * @throws TimeoutException If the operation timed out.
     */
    public ItemNode discoverItems(Jid jid, String node) throws TimeoutException {
        IQ iq = new IQ(IQ.Type.GET, new ItemDiscovery(node));
        iq.setTo(jid);
        IQ result = connection.query(iq);
        return result.getExtension(ItemDiscovery.class);
    }

    /**
     * Adds an info node.
     *
     * @param infoNode The info node.
     */
    public void addInfoNode(InfoNode infoNode) {
        infoNodeMap.put(infoNode.getNode(), infoNode);
    }

    /**
     * Removes an info node.
     *
     * @param node The node name.
     */
    public void removeInfoNode(String node) {
        infoNodeMap.remove(node);
    }

    /**
     * Adds an item node.
     *
     * @param itemNode The item node.
     */
    public void addItemNode(ItemNode itemNode) {
        itemNodeMap.put(itemNode.getNode(), itemNode);
    }

    /**
     * Removes an item node.
     *
     * @param node The item node.
     */
    public void removeItemNode(String node) {
        itemNodeMap.remove(node);
    }
}
