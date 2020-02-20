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

package rocks.xmpp.extensions.disco.handler;

import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles 'disco#info' IQ requests by responding with appropriate identities, features and extensions.
 *
 * @author Christian Schudt
 */
public final class DiscoInfoHandler extends AbstractIQHandler {

    private final Map<String, InfoNode> infoNodeMap = new ConcurrentHashMap<>();

    private Identity defaultIdentity;

    public DiscoInfoHandler() {
        super(InfoDiscovery.class, IQ.Type.GET);
    }

    @Override
    protected final IQ processRequest(IQ iq) {
        InfoDiscovery infoDiscovery = iq.getExtension(InfoDiscovery.class);
        if (infoDiscovery.getNode() == null) {
            return iq.createResult(new InfoDiscovery(null, getIdentities(), getFeatures(), getExtensions()));
        }
        InfoNode infoNode = infoNodeMap.get(infoDiscovery.getNode());
        if (infoNode != null) {
            return iq.createResult(new InfoDiscovery(infoNode.getNode(), infoNode.getIdentities(), infoNode.getFeatures(), infoNode.getExtensions()));
        } else {
            // Returns <feature-not-implemented/> here.
            // XEP-0030 is not clear on that, but XEP-0045 and XEP-0079 specify to return a <feature-not-implemented/> on unknown nodes.
            return iq.createError(Condition.FEATURE_NOT_IMPLEMENTED);
        }
    }

    /**
     * Adds an info node.
     *
     * @param infoNode The info node.
     */
    public final void addInfoNode(InfoNode infoNode) {
        String key = infoNode.getNode() == null ? "" : infoNode.getNode();
        infoNodeMap.put(key, infoNode);
    }

    /**
     * Removes an info node.
     *
     * @param node The node name.
     */
    public final void removeInfoNode(String node) {
        infoNodeMap.remove(node == null ? "" : node);
    }

    /**
     * Gets an unmodifiable set of identities.
     *
     * @return The identities.
     * @see #addIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     */
    public final Set<Identity> getIdentities() {
        Set<Identity> identities = getRootNode().getIdentities();
        return Collections.unmodifiableSet(identities.isEmpty() ? Collections.singleton(getDefaultIdentity()) : identities);
    }

    /**
     * Gets an unmodifiable set of features.
     *
     * @return The features.
     * @see #addFeature(String)
     * @see #removeFeature(String)
     */
    public final Set<String> getFeatures() {
        return Collections.unmodifiableSet(getRootNode().getFeatures());
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
        return Collections.unmodifiableList(getRootNode().getExtensions());
    }

    /**
     * Adds an identity.
     *
     * @param identity The identity.
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #getIdentities()
     */
    public final boolean addIdentity(Identity identity) {
        return getRootNode().getIdentities().add(identity);
    }

    /**
     * Removes an identity.
     *
     * @param identity The identity.
     * @see #addIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     * @see #getIdentities()
     */
    public final boolean removeIdentity(Identity identity) {
        return getRootNode().getIdentities().remove(identity);
    }

    /**
     * Adds a feature.
     *
     * @param feature The feature.
     * @see #removeFeature(String)
     * @see #getFeatures()
     */
    public final boolean addFeature(String feature) {
        return getRootNode().getFeatures().add(feature);
    }

    /**
     * Removes a feature.
     *
     * @param feature The feature.
     * @see #addFeature(String)
     * @see #getFeatures()
     */
    public final boolean removeFeature(String feature) {
        return getRootNode().getFeatures().remove(feature);
    }

    /**
     * Adds an extension.
     *
     * @param extension The extension.
     * @see #removeExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #getExtensions()
     * @see <a href="https://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    public final boolean addExtension(DataForm extension) {
        return getRootNode().getExtensions().add(extension);
    }

    /**
     * Removes an extension.
     *
     * @param extension The extension.
     * @see #addExtension(rocks.xmpp.extensions.data.model.DataForm)
     * @see #getExtensions()
     * @see <a href="https://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
     */
    public final boolean removeExtension(DataForm extension) {
        return getRootNode().getExtensions().remove(extension);
    }

    /**
     * Gets the default identity.
     *
     * @return The default identity.
     */
    public final Identity getDefaultIdentity() {
        return this.defaultIdentity;
    }

    /**
     * Sets the default identity.
     *
     * @param identity The default identity.
     */
    public final void setDefaultIdentity(Identity identity) {
        this.defaultIdentity = identity;
    }

    /**
     * Clears all nodes except the root node.
     */
    public final void clear() {
        infoNodeMap.clear();
    }

    private InfoNode getRootNode() {
        return infoNodeMap.computeIfAbsent("", key -> new RootNode());
    }

    /**
     * The default root node.
     */
    private static final class RootNode implements InfoNode {

        private final Set<Identity> identities = Collections.newSetFromMap(new ConcurrentHashMap<>());

        private final Set<String> features = Collections.newSetFromMap(new ConcurrentHashMap<>());

        private final CopyOnWriteArrayList<DataForm> extensions = new CopyOnWriteArrayList<>();

        @Override
        public String getNode() {
            return null;
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
