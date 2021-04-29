/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.disco.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.AbstractServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * Manages <a href="https://xmpp.org/extensions/xep-0030.html">XEP-0030: Service Discovery</a>.
 * <blockquote>
 * <p><cite><a href="https://xmpp.org/extensions/xep-0030.html#intro">1. Introduction</a></cite></p>
 * <p>The ability to discover information about entities on the Jabber network is extremely valuable. Such information
 * might include features offered or protocols supported by the entity, the entity's type or identity, and additional
 * entities that are associated with the original entity in some way (often thought of as "children" of the "parent"
 * entity).</p>
 * </blockquote>
 * <p>Enabled extensions are automatically added to the list of features by their respective manager class.
 * Disabled extensions are removed.</p>
 *
 * <p>This class automatically manages inbound service discovery requests by responding with a list of enabled
 * extensions (features).</p>
 *
 * @author Christian Schudt
 */
public final class ClientServiceDiscoveryManager extends AbstractServiceDiscoveryManager {

    private static final Identity DEFAULT_IDENTITY = Identity.clientPc();

    private final Set<Consumer<EventObject>> capabilitiesChangeListeners = new CopyOnWriteArraySet<>();

    private final ClientInfo clientInfo = new ClientInfo();

    private final List<DiscoverableInfo> discoverableInfos = new ArrayList<>();

    private final DiscoverableInfo rootNode;

    private final XmppSession xmppSession;

    private ClientServiceDiscoveryManager(final XmppSession xmppSession) {
        this.xmppSession = xmppSession;
        this.discoverableInfos.add(clientInfo);
        this.rootNode = new CombinedRootNode();
        addInfoProvider((to, from, node, locale) -> {
            if (node == null) {
                return rootNode;
            }
            return null;
        });
    }

    /**
     * Adds a property change listener, which listens for changes in the {@linkplain #getIdentities() identities},
     * {@linkplain #getFeatures() features} and {@linkplain #getExtensions() extensions} collections.
     *
     * @param listener The listener.
     * @see #removeCapabilitiesChangeListener(Consumer)
     */
    public final void addCapabilitiesChangeListener(Consumer<EventObject> listener) {
        this.capabilitiesChangeListeners.add(listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param listener The listener.
     * @see #addCapabilitiesChangeListener(Consumer)
     */
    public final void removeCapabilitiesChangeListener(Consumer<EventObject> listener) {
        this.capabilitiesChangeListeners.remove(listener);
    }

    /**
     * Gets an unmodifiable set of identities.
     *
     * @return The identities.
     * @see #addIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     */
    public final Set<Identity> getIdentities() {
        return Collections.unmodifiableSet(clientInfo.getIdentities());
    }

    /**
     * Gets an unmodifiable list of extensions.
     *
     * @return The extensions.
     * @see #addExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #removeExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see <a href="https://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    public final List<DataForm> getExtensions() {
        return Collections.unmodifiableList(clientInfo.getExtensions());
    }

    /**
     * Adds an identity.
     *
     * @param identity The identity.
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #getIdentities()
     */
    @Override
    public final void addIdentity(Identity identity) {
        if (clientInfo.getIdentities().add(identity) && xmppSession.isConnected()) {
            XmppUtils.notifyEventListeners(capabilitiesChangeListeners, new EventObject(this));
        }
    }

    /**
     * Removes an identity.
     *
     * @param identity The identity.
     * @see #addIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #getIdentities()
     */
    @Override
    public final void removeIdentity(Identity identity) {
        if (clientInfo.getIdentities().remove(identity) && xmppSession.isConnected()) {
            XmppUtils.notifyEventListeners(capabilitiesChangeListeners, new EventObject(this));
        }
    }

    /**
     * Adds a feature. Features should not be added or removed directly. Instead enable or disable the respective
     * extension manager, which will then add or remove the feature. That way, supported features are consistent with
     * enabled extension managers and service discovery won't reveal features, that are in fact not supported.
     *
     * @param feature The feature.
     * @see #removeFeature(String)
     * @see #getFeatures()
     */
    @Override
    public final void addFeature(String feature) {
        if (clientInfo.features.add(feature)) {
            if (xmppSession.isConnected()) {
                XmppUtils.notifyEventListeners(capabilitiesChangeListeners, new EventObject(this));
            }
        }
    }

    /**
     * Removes a feature.
     *
     * @param feature The feature.
     * @see #addFeature(String)
     * @see #getFeatures()
     */
    @Override
    public final void removeFeature(String feature) {
        if (clientInfo.features.remove(feature)) {
            if (xmppSession.isConnected()) {
                XmppUtils.notifyEventListeners(capabilitiesChangeListeners, new EventObject(this));
            }
        }
    }

    @Override
    public final DiscoverableInfo getDefaultInfo() {
        return rootNode;
    }

    /**
     * Adds an extension.
     *
     * @param extension The extension.
     * @see #removeExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #getExtensions()
     * @see <a href="https://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    public final void addExtension(DataForm extension) {
        clientInfo.getExtensions().add(extension);
        if (xmppSession.isConnected()) {
            XmppUtils.notifyEventListeners(capabilitiesChangeListeners, new EventObject(this));
        }
    }

    /**
     * Removes an extension.
     *
     * @param extension The extension.
     * @see #addExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #getExtensions()
     * @see <a href="https://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    public final void removeExtension(DataForm extension) {
        if (clientInfo.getExtensions().remove(extension) && xmppSession.isConnected()) {
            XmppUtils.notifyEventListeners(capabilitiesChangeListeners, new EventObject(this));
        }
    }

    /**
     * Adds discoverable information to the root node.
     *
     * @param discoverableInfo The info.
     */
    public final void addInfo(DiscoverableInfo discoverableInfo) {
        discoverableInfos.add(discoverableInfo);
    }

    /**
     * Discovers information about another XMPP entity targeted at a specific node.
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/extensions/xep-0030.html#info-nodes">3.2 Info Nodes</a></cite></p>
     * <p>A disco#info query MAY also be directed to a specific node identifier associated with a JID.</p>
     * </blockquote>
     *
     * @param jid  The entity's JID.
     * @param node The node.
     * @return The async service discovery result.
     * @see #discoverInformation(Jid)
     */
    @Override
    public final AsyncResult<DiscoverableInfo> discoverInformation(Jid jid, String node) {
        return xmppSession.query(IQ.get(jid, new InfoDiscovery(node)), DiscoverableInfo.class);
    }

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid                 The JID.
     * @param node                The node.
     * @param resultSetManagement The result set management.
     * @return The async result with the discovered items.
     */
    @Override
    public final AsyncResult<ItemNode> discoverItems(Jid jid, String node, ResultSetManagement resultSetManagement) {
        return xmppSession.query(IQ.get(jid, new ItemDiscovery(node, resultSetManagement)), ItemNode.class);
    }

    private final class CombinedRootNode implements DiscoverableInfo {

        @Override
        public final Set<Identity> getIdentities() {
            return discoverableInfos.stream().flatMap(discoverableInfo -> discoverableInfo.getIdentities().stream())
                    .collect(Collectors.toSet());
        }

        @Override
        public final Set<String> getFeatures() {
            return discoverableInfos.stream().flatMap(discoverableInfo -> discoverableInfo.getFeatures().stream())
                    .collect(Collectors.toSet());
        }

        @Override
        public final List<DataForm> getExtensions() {
            return discoverableInfos.stream().flatMap(discoverableInfo -> discoverableInfo.getExtensions().stream())
                    .collect(Collectors.toList());
        }
    }

    private static final class ClientInfo implements DiscoverableInfo {

        private final Set<Identity> identities = Collections.newSetFromMap(new ConcurrentHashMap<>());

        private final Set<String> features = Collections.newSetFromMap(new ConcurrentHashMap<>());

        private final CopyOnWriteArrayList<DataForm> extensions = new CopyOnWriteArrayList<>();

        private ClientInfo() {
            identities.add(DEFAULT_IDENTITY);
        }

        @Override
        public Set<Identity> getIdentities() {
            return identities;
        }

        @Override
        public Set<String> getFeatures() {
            return features;
        }

        @Override
        public List<DataForm> getExtensions() {
            return extensions;
        }
    }
}
