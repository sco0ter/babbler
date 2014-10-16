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

package rocks.xmpp.extensions.disco;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQListener;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.ItemNotFound;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemNode;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

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

    private static Identity defaultIdentity = new Identity("client", "pc");

    private final Set<Identity> identities = new CopyOnWriteArraySet<>();

    private final Set<Feature> features = new CopyOnWriteArraySet<>();

    private final List<DataForm> extensions = new CopyOnWriteArrayList<>();

    private final List<Item> items = new CopyOnWriteArrayList<>();

    private final Map<String, InfoNode> infoNodeMap = new ConcurrentHashMap<>();

    private final Map<String, ItemNode> itemNodeMap = new ConcurrentHashMap<>();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private ServiceDiscoveryManager(final XmppSession xmppSession) {
        super(xmppSession, "http://jabber.org/protocol/disco#info", "http://jabber.org/protocol/disco#items");

        xmppSession.addSessionStatusListener(new SessionStatusListener() {
            @Override
            public void sessionStatusChanged(SessionStatusEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    for (PropertyChangeListener propertyChangeListener : pcs.getPropertyChangeListeners()) {
                        pcs.removePropertyChangeListener(propertyChangeListener);
                    }
                }
            }
        });

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.GET) {
                    InfoDiscovery infoDiscovery = iq.getExtension(InfoDiscovery.class);
                    if (infoDiscovery != null) {
                        if (infoDiscovery.getNode() == null) {
                            IQ result = iq.createResult();
                            result.setExtension(new InfoDiscovery(getIdentities(), getFeatures(), getExtensions()));
                            xmppSession.send(result);
                            e.consume();
                        } else {
                            InfoNode infoNode = infoNodeMap.get(infoDiscovery.getNode());
                            if (infoNode != null) {
                                IQ result = iq.createResult();
                                result.setExtension(new InfoDiscovery(infoNode.getNode(), infoNode.getIdentities(), infoNode.getFeatures(), infoNode.getExtensions()));
                                xmppSession.send(result);
                                e.consume();
                            } else {
                                xmppSession.send(iq.createError(new StanzaError(new ItemNotFound())));
                                e.consume();
                            }
                        }
                    } else {
                        ItemDiscovery itemDiscovery = iq.getExtension(ItemDiscovery.class);
                        if (itemDiscovery != null) {
                            if (itemDiscovery.getNode() == null) {
                                IQ result = iq.createResult();
                                result.setExtension(new ItemDiscovery(items));
                                xmppSession.send(result);
                                e.consume();
                            } else {
                                ItemNode itemNode = itemNodeMap.get(itemDiscovery.getNode());
                                if (itemNode != null) {
                                    IQ result = iq.createResult();
                                    result.setExtension(new ItemDiscovery(itemNode.getNode(), items));
                                    xmppSession.send(result);
                                    e.consume();
                                } else {
                                    xmppSession.send(iq.createError(new StanzaError(new ItemNotFound())));
                                    e.consume();
                                }
                            }
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    /**
     * Adds a property change listener, which listens for changes in the {@linkplain #getIdentities() identities}, {@linkplain #getFeatures() features}, {@linkplain #getExtensions() extensions} and {@linkplain #getItems() items} collections.
     *
     * @param listener The listener.
     * @see #removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param listener The listener.
     * @see #addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    /**
     * Since this is the "root" node, it returns null.
     *
     * @return null
     */
    @Override
    public String getNode() {
        return null;
    }

    /**
     * Gets an unmodifiable list of items.
     *
     * @return The items.
     * @see #addItem(rocks.xmpp.extensions.disco.model.items.Item)
     * @see #removeItem(rocks.xmpp.extensions.disco.model.items.Item)
     */
    @Override
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Gets an unmodifiable set of identities.
     *
     * @return The identities.
     * @see #addIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     */
    @Override
    public synchronized Set<Identity> getIdentities() {
        Set<Identity> ids;
        //  Every entity MUST have at least one identity
        if (!identities.isEmpty()) {
            ids = new HashSet<>(identities);
        } else {
            ids = new HashSet<>();
            ids.add(defaultIdentity);
        }
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Gets an unmodifiable set of features.
     *
     * @return The features.
     * @see #addFeature(rocks.xmpp.extensions.disco.model.info.Feature)
     * @see #removeFeature(rocks.xmpp.extensions.disco.model.info.Feature)
     */
    @Override
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    /**
     * Gets an unmodifiable list of extensions.
     *
     * @return The extensions.
     * @see #addExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #removeExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see <a href="http://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    @Override
    public List<DataForm> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

    /**
     * Adds an item.
     *
     * @param item The item.
     * @see #removeItem(rocks.xmpp.extensions.disco.model.items.Item)
     * @see #getItems() ()
     */
    public synchronized void addItem(Item item) {
        List<Item> oldList = Collections.unmodifiableList(new ArrayList<>(items));
        items.add(item);
        this.pcs.firePropertyChange("items", oldList, getItems());
    }

    /**
     * Removes an item.
     *
     * @param item The item.
     * @see #addItem(rocks.xmpp.extensions.disco.model.items.Item)
     * @see #getItems()
     */
    public synchronized void removeItem(Item item) {
        List<Item> oldList = Collections.unmodifiableList(new ArrayList<>(items));
        items.remove(item);
        this.pcs.firePropertyChange("items", oldList, getItems());
    }

    /**
     * Adds an identity.
     *
     * @param identity The identity.
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #getIdentities()
     */
    public synchronized void addIdentity(Identity identity) {
        Set<Identity> oldList = Collections.unmodifiableSet(new HashSet<>(identities));
        identities.add(identity);
        this.pcs.firePropertyChange("identities", oldList, getIdentities());
    }

    /**
     * Removes an identity.
     *
     * @param identity The identity.
     * @see #addIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #getIdentities()
     */
    public synchronized void removeIdentity(Identity identity) {
        Set<Identity> oldList = Collections.unmodifiableSet(new HashSet<>(identities));
        identities.remove(identity);
        this.pcs.firePropertyChange("identities", oldList, getIdentities());
    }

    /**
     * Adds a feature. Features should not be added or removed directly. Instead enable or disable the respective extension manager, which will then add or remove the feature.
     * That way, supported features are consistent with enabled extension managers and service discovery won't reveal features, that are in fact not supported.
     *
     * @param feature The feature.
     * @see #removeFeature(rocks.xmpp.extensions.disco.model.info.Feature)
     * @see #getFeatures()
     */
    public synchronized void addFeature(Feature feature) {
        Set<Feature> oldList = Collections.unmodifiableSet(new HashSet<>(features));
        features.add(feature);
        this.pcs.firePropertyChange("features", oldList, getFeatures());
    }

    /**
     * Removes a feature.
     *
     * @param feature The feature.
     * @see #addFeature(rocks.xmpp.extensions.disco.model.info.Feature)
     * @see #getFeatures()
     */
    public synchronized void removeFeature(Feature feature) {
        Set<Feature> oldList = Collections.unmodifiableSet(new HashSet<>(features));
        features.remove(feature);
        this.pcs.firePropertyChange("features", oldList, getFeatures());
    }

    /**
     * Adds an extension.
     *
     * @param extension The extension.
     * @see #removeExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #getExtensions()
     * @see <a href="http://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    public synchronized void addExtension(DataForm extension) {
        List<DataForm> oldList = Collections.unmodifiableList(new ArrayList<>(extensions));
        extensions.add(extension);
        this.pcs.firePropertyChange("extensions", oldList, getExtensions());
    }

    /**
     * Removes an extension.
     *
     * @param extension The extension.
     * @see #addExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #getExtensions()
     * @see <a href="http://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    public synchronized void removeExtension(DataForm extension) {
        List<DataForm> oldList = Collections.unmodifiableList(new ArrayList<>(extensions));
        extensions.remove(extension);
        this.pcs.firePropertyChange("extensions", oldList, getExtensions());
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public InfoNode discoverInformation(Jid jid) throws XmppException {
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see #discoverInformation(rocks.xmpp.core.Jid)
     */
    public InfoNode discoverInformation(Jid jid, String node) throws XmppException {
        IQ iq = new IQ(jid, IQ.Type.GET, new InfoDiscovery(node));
        IQ result = xmppSession.query(iq);
        return result.getExtension(InfoDiscovery.class);
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid The JID.
     * @return The discovered items.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public ItemNode discoverItems(Jid jid) throws XmppException {
        return discoverItems(jid, null);
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid  The JID.
     * @param node The node.
     * @return The discovered items.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public ItemNode discoverItems(Jid jid, String node) throws XmppException {
        IQ iq = new IQ(IQ.Type.GET, new ItemDiscovery(node));
        iq.setTo(jid);
        IQ result = xmppSession.query(iq);
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
