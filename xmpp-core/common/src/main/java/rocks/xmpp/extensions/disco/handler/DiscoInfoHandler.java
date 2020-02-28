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
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.info.InfoNodeProvider;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Handles 'disco#info' IQ requests by responding with appropriate identities, features and extensions.
 *
 * @author Christian Schudt
 */
public final class DiscoInfoHandler extends AbstractIQHandler {

    private final Set<InfoNodeProvider> infoNodeProviders = new CopyOnWriteArraySet<>();

    private final Map<String, InfoNode> infoNodeMap = new ConcurrentHashMap<>();

    public DiscoInfoHandler() {
        super(InfoDiscovery.class, IQ.Type.GET);
    }

    @Override
    protected final IQ processRequest(IQ iq) {

        InfoDiscovery infoDiscovery = iq.getExtension(InfoDiscovery.class);
        Set<InfoNode> infoNodes = new HashSet<>();
        for (InfoNodeProvider infoNodeProvider : infoNodeProviders) {
            try {
                Set<InfoNode> nodes = infoNodeProvider.getInfoNodes(infoDiscovery.getNode());
                if (nodes != null) {
                    nodes.stream()
                            .filter(infoNode -> Objects.equals(infoNode.getNode(), infoDiscovery.getNode()))
                            .forEach(infoNodes::add);
                }
            } catch (StanzaErrorException e) {
                return iq.createError(e.getError());
            }
        }

        InfoNode infoNode = infoNodeMap.get(infoDiscovery.getNode() == null ? "" : infoDiscovery.getNode());
        if (infoNode != null) {
            infoNodes.add(infoNode);
        }
        Set<String> features = infoNodes.stream().flatMap(infoNode1 -> infoNode1.getFeatures().stream()).collect(Collectors.toSet());
        Set<Identity> identities = infoNodes.stream().flatMap(infoNode1 -> infoNode1.getIdentities().stream()).collect(Collectors.toSet());
        List<DataForm> extensions = infoNodes.stream().flatMap(infoNode1 -> infoNode1.getExtensions().stream()).collect(Collectors.toList());

        if (!infoNodes.isEmpty()) {
            return iq.createResult(new InfoDiscovery(infoDiscovery.getNode(), identities, features, extensions));
        } else {
            return iq.createError(Condition.ITEM_NOT_FOUND);
        }
    }

    /**
     * Adds an info node provider.
     *
     * @param infoNodeProvider The info node provider.
     * @return true, if it has been successfully added.
     * @see #removeInfoNodeProvider(InfoNodeProvider)
     */
    public final boolean addInfoNodeProvider(InfoNodeProvider infoNodeProvider) {
        return infoNodeProviders.add(infoNodeProvider);
    }

    /**
     * Removes an info node provider.
     *
     * @param infoNodeProvider The info node provider.
     * @return true, if it has been successfully remove.
     * @see #addInfoNodeProvider(InfoNodeProvider)
     */
    public final boolean removeInfoNodeProvider(InfoNodeProvider infoNodeProvider) {
        return infoNodeProviders.remove(infoNodeProvider);
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
     * Clears all nodes except the root node.
     */
    public final void clear() {
        infoNodeMap.clear();
    }

    /**
     * Gets the root node.
     *
     * @return The root node.
     */
    public InfoNode getRootNode() {
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
