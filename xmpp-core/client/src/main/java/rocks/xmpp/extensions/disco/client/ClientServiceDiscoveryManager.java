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

import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.Manager;
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
 * <p>The ability to discover information about entities on the Jabber network is extremely valuable. Such information might include features offered or protocols supported by the entity, the entity's type or identity, and additional entities that are associated with the original entity in some way (often thought of as "children" of the "parent" entity).</p>
 * </blockquote>
 * <p>Enabled extensions are automatically added to the list of features by their respective manager class.
 * Disabled extensions are removed.</p>
 * <p>
 * This class automatically manages inbound service discovery requests by responding with a list of enabled extensions (features).
 * </p>
 *
 * @author Christian Schudt
 */
public final class ClientServiceDiscoveryManager extends AbstractServiceDiscoveryManager {

    private static final Identity DEFAULT_IDENTITY = Identity.clientPc();

    private final Set<Consumer<EventObject>> capabilitiesChangeListeners = new CopyOnWriteArraySet<>();

    private final Map<String, Extension> featureToExtension = new HashMap<>();

    private final Map<Class<?>, Set<Extension>> managersToExtensions = new HashMap<>();

    private final Set<ExtensionProtocol> extensions = new HashSet<>();

    private final ClientInfo clientInfo = new ClientInfo();

    private final XmppSession xmppSession;

    private ClientServiceDiscoveryManager(final XmppSession xmppSession) {
        this.xmppSession = xmppSession;
        addInfoProvider((to, from, node, locale) -> {
            if (node == null) {
                return clientInfo;
            }
            return null;
        });
    }

    /**
     * Adds a property change listener, which listens for changes in the {@linkplain #getIdentities() identities}, {@linkplain #getFeatures() features} and {@linkplain #getExtensions() extensions} collections.
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
     * Adds a feature. Features should not be added or removed directly. Instead enable or disable the respective extension manager, which will then add or remove the feature.
     * That way, supported features are consistent with enabled extension managers and service discovery won't reveal features, that are in fact not supported.
     *
     * @param feature The feature.
     * @see #removeFeature(String)
     * @see #getFeatures()
     */
    @Override
    public final void addFeature(String feature) {
        if (clientInfo.features.add(feature)) {
            Extension extension = featureToExtension.get(feature);
            setEnabled(extension != null ? Collections.singleton(extension) : null, feature, true);
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
            Extension extension = featureToExtension.get(feature);
            setEnabled(extension != null ? Collections.singleton(extension) : null, feature, false);
            if (xmppSession.isConnected()) {
                XmppUtils.notifyEventListeners(capabilitiesChangeListeners, new EventObject(this));
            }
        }
    }

    @Override
    public final DiscoverableInfo getDefaultInfo() {
        return clientInfo;
    }

    /**
     * Adds a feature by its manager class.
     *
     * @param managerClass The manager class.
     * @see #addFeature(String)
     * @see #getFeatures()
     */
    public final void addFeature(Class<?> managerClass) {
        // This will eventually call addFeature(String)
        setEnabled(managersToExtensions.get(managerClass), null, true);
    }

    /**
     * Removes a feature by its manager class.
     *
     * @param managerClass The feature.
     * @see #addFeature(String)
     * @see #getFeatures()
     */
    public final void removeFeature(Class<?> managerClass) {
        // This will eventually call addFeature(String)
        setEnabled(managersToExtensions.get(managerClass), null, false);
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
        if (clientInfo.getExtensions().add(extension) && xmppSession.isConnected()) {
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

    private void setEnabled(Iterable<Extension> extensions, String feature, boolean enabled) {
        if (extensions != null) {
            for (Extension extension : extensions) {
                // Check if the extension has an associated manager class, which we need to enable/disable.
                Class<?> managerClass = extension.getManager();
                if (managerClass != null) {
                    Object manager = xmppSession.getManager(managerClass);
                    // A manager can manage multiple features (e.g. ServiceDiscoveryManager manages disco#items and disco#info feature)
                    // If we disable one feature, but not the other one, the manager should still be enabled.
                    boolean mayDisable = true;
                    if (feature != null && !enabled) {
                        Set<Extension> ex = managersToExtensions.get(managerClass);
                        for (Extension e : ex) {
                            if (clientInfo.getFeatures().contains(e.getNamespace())) {
                                mayDisable = false;
                                break;
                            }
                        }
                    }
                    if (manager instanceof Manager && mayDisable && enabled != ((Manager) manager).isEnabled()) {
                        ((Manager) manager).setEnabled(enabled);
                    }
                }

                // Enable the feature (by adding it to the Service Discovery list)
                enableFeature(extension.getNamespace(), enabled);

                // Do the same for each sub-feature.
                for (String subFeature : extension.getFeatures()) {
                    enableFeature(subFeature, enabled);
                }
            }
        } else {
            enableFeature(feature, enabled);
        }
    }

    private void enableFeature(String feature, boolean enabled) {
        if (feature != null) {
            if (enabled) {
                addFeature(feature);
            } else {
                removeFeature(feature);
            }
        }
    }

    /**
     * Registers a feature / extension.
     *
     * @param extension The extension.
     */
    public final void registerFeature(Extension extension) {
        if (extension.getNamespace() != null) {
            featureToExtension.put(extension.getNamespace(), extension);
        }
        if (extension.getManager() != null) {
            Set<Extension> extensions = managersToExtensions.computeIfAbsent(extension.getManager(), key -> new HashSet<>());
            extensions.add(extension);
        }
        if (extension.isEnabled()) {
            setEnabled(Collections.singleton(extension), null, true);
        }
    }

    public final void registerFeature(ExtensionProtocol extension) {
        extensions.add(extension);
    }

    private final class ClientInfo implements DiscoverableInfo {

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
            // Concat manually added features, with enabled known extensions
            return Stream.concat(features.stream(),
                    ClientServiceDiscoveryManager.this.extensions
                            .stream()
                            // Extensions with manager currently get excluded, because they are manually added to the feature set.
                            // This shall change in the future
                            .filter(ExtensionProtocol::isEnabled)
                            .flatMap(extension -> extension.getFeatures().stream()))
                    .collect(Collectors.toSet());
        }

        @Override
        public List<DataForm> getExtensions() {
            return extensions;
        }
    }
}
