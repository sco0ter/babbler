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

package rocks.xmpp.extensions.disco;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.IQExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.rsm.ResultSetManager;
import rocks.xmpp.extensions.rsm.ResultSetProvider;
import rocks.xmpp.extensions.rsm.model.ResultSet;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

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
public final class ServiceDiscoveryManager extends IQExtensionManager implements SessionStatusListener {

    private static Identity defaultIdentity = new Identity("client", "pc");

    private final Set<Identity> identities = new ConcurrentSkipListSet<>();

    private final Set<Feature> features = new ConcurrentSkipListSet<>();

    private final List<DataForm> extensions = new CopyOnWriteArrayList<>();

    private final Map<String, InfoNode> infoNodeMap = new ConcurrentHashMap<>();

    private final Map<String, ResultSetProvider<Item>> itemProviders = new ConcurrentHashMap<>();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private ServiceDiscoveryManager(final XmppSession xmppSession) {
        super(xmppSession, AbstractIQ.Type.GET, InfoDiscovery.NAMESPACE, ItemDiscovery.NAMESPACE);

        xmppSession.addSessionStatusListener(this);

        xmppSession.addIQHandler(InfoDiscovery.class, this);
        xmppSession.addIQHandler(ItemDiscovery.class, this);
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
     * Gets an unmodifiable list of items.
     *
     * @return The items.
     */
    public List<Item> getItems() {
        ResultSetProvider<Item> rootItemProvider = itemProviders.get("");
        if (rootItemProvider != null) {
            return Collections.unmodifiableList(rootItemProvider.getItems());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Gets an unmodifiable set of identities.
     *
     * @return The identities.
     * @see #addIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     */
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
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(new HashSet<>(features));
    }

    /**
     * Gets an unmodifiable list of extensions.
     *
     * @return The extensions.
     * @see #addExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #removeExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see <a href="http://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    public List<DataForm> getExtensions() {
        return Collections.unmodifiableList(new ArrayList<>(extensions));
    }

    /**
     * Adds an identity.
     *
     * @param identity The identity.
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #getIdentities()
     */
    public synchronized void addIdentity(Identity identity) {
        Set<Identity> oldList = getIdentities();
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
        Set<Identity> oldList = getIdentities();
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
        Set<Feature> oldList = getFeatures();
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
        Set<Feature> oldList = getFeatures();
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
        List<DataForm> oldList = getExtensions();
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
        List<DataForm> oldList = getExtensions();
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
     * @throws rocks.xmpp.core.stanza.StanzaException If the entity returned a stanza error.
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
     * @throws rocks.xmpp.core.stanza.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see #discoverInformation(rocks.xmpp.core.Jid)
     */
    public InfoNode discoverInformation(Jid jid, String node) throws XmppException {
        IQ result = xmppSession.query(new IQ(jid, IQ.Type.GET, new InfoDiscovery(node)));
        return result.getExtension(InfoDiscovery.class);
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid The JID.
     * @return The discovered items.
     * @throws rocks.xmpp.core.stanza.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public ItemNode discoverItems(Jid jid) throws XmppException {
        return discoverItems(jid, null, null);
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid       The JID.
     * @param resultSet The result set management.
     * @return The discovered items.
     * @throws rocks.xmpp.core.stanza.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public ItemNode discoverItems(Jid jid, ResultSetManagement resultSet) throws XmppException {
        return discoverItems(jid, null, resultSet);
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid  The JID.
     * @param node The node.
     * @return The discovered items.
     * @throws rocks.xmpp.core.stanza.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public ItemNode discoverItems(Jid jid, String node) throws XmppException {
        return discoverItems(jid, node, null);
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid                 The JID.
     * @param node                The node.
     * @param resultSetManagement The result set management.
     * @return The discovered items.
     * @throws rocks.xmpp.core.stanza.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public ItemNode discoverItems(Jid jid, String node, ResultSetManagement resultSetManagement) throws XmppException {
        IQ result = xmppSession.query(new IQ(jid, IQ.Type.GET, new ItemDiscovery(node, resultSetManagement)));
        return result.getExtension(ItemDiscovery.class);
    }

    /**
     * Discovers a service on the connected server by its feature namespace.
     *
     * @param feature The feature namespace.
     * @return The services, that belong to the namespace.
     * @throws rocks.xmpp.core.stanza.StanzaException If the server returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the server did not respond.
     */
    public Collection<Item> discoverServices(String feature) throws XmppException {
        ItemNode itemDiscovery = discoverItems(Jid.valueOf(xmppSession.getDomain()));
        Collection<Item> services = new ArrayList<>();
        XmppException exception = null;
        for (Item item : itemDiscovery.getItems()) {
            try {
                InfoNode infoDiscovery = discoverInformation(item.getJid());
                if (infoDiscovery.getFeatures().contains(new Feature(feature))) {
                    services.add(item);
                }
            } catch (XmppException e) {
                // If a disco#info request returns with an error, ignore it for now and try the next item.
                exception = e;
            }
        }
        // If an exception occurred and no service could be discovered, rethrow the original exception.
        if (exception != null && services.isEmpty()) {
            throw exception;
        }
        // Otherwise return the successfully discovered services.
        return services;
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
     * Sets an item provider for the root node.
     * <p>
     * If you want to manage items in memory, you can use {@link DefaultItemProvider}.
     *
     * @param itemProvider The item provider.
     */
    public void setItemProvider(ResultSetProvider<Item> itemProvider) {
        if (itemProvider == null) {
            itemProviders.remove("");
        } else {
            itemProviders.put("", itemProvider);
        }
    }

    /**
     * Sets an item provider for a node.
     * <p>
     * If you want to manage items in memory, you can use {@link DefaultItemProvider}.
     *
     * @param node         The node name.
     * @param itemProvider The item provider.
     */
    public void setItemProvider(String node, ResultSetProvider<Item> itemProvider) {
        if (itemProvider == null) {
            itemProviders.remove(node);
        } else {
            itemProviders.put(node, itemProvider);
        }
    }

    @Override
    protected IQ processRequest(final IQ iq) {
        InfoDiscovery infoDiscovery = iq.getExtension(InfoDiscovery.class);
        if (infoDiscovery != null) {
            if (infoDiscovery.getNode() == null) {
                return iq.createResult(new InfoDiscovery(getIdentities(), getFeatures(), getExtensions()));
            } else {
                InfoNode infoNode = infoNodeMap.get(infoDiscovery.getNode());
                if (infoNode != null) {
                    return iq.createResult(new InfoDiscovery(infoNode.getNode(), infoNode.getIdentities(), infoNode.getFeatures(), infoNode.getExtensions()));
                } else {
                    // Returns <feature-not-implemented/> here.
                    // XEP-0030 is not clear on that, but XEP-0045 and XEP-0079 specify to return a <feature-not-implemented/> on unknown nodes.
                    return iq.createError(Condition.FEATURE_NOT_IMPLEMENTED);
                }
            }
        } else {
            ItemDiscovery itemDiscovery = iq.getExtension(ItemDiscovery.class);
            ResultSetProvider<Item> itemProvider = itemProviders.get(itemDiscovery.getNode() == null ? "" : itemDiscovery.getNode());
            if (itemProvider != null) {
                ResultSet<Item> resultSet = ResultSetManager.createResultSet(itemProvider, itemDiscovery.getResultSetManagement());
                return iq.createResult(new ItemDiscovery(itemDiscovery.getNode(), resultSet.getItems(), resultSet.getResultSetManagement()));
            } else {
                if (itemDiscovery.getNode() == null) {
                    // If there are no items associated with an entity (or if those items are not publicly available), the target entity MUST return an empty query element to the requesting entity.
                    return iq.createResult(new ItemDiscovery(itemDiscovery.getNode()));
                } else {
                    // <item-not-found/>: The JID or JID+NodeID of the specified target entity does not exist.
                    return iq.createError(Condition.ITEM_NOT_FOUND);
                }
            }
        }
    }

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            for (PropertyChangeListener propertyChangeListener : pcs.getPropertyChangeListeners()) {
                pcs.removePropertyChangeListener(propertyChangeListener);
            }
        }
        infoNodeMap.clear();
        itemProviders.clear();
    }
}
