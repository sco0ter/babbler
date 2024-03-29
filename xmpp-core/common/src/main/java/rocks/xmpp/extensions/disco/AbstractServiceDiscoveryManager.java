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

package rocks.xmpp.extensions.disco;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.disco.model.ServiceDiscoveryNode;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoProvider;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.disco.model.items.ItemProvider;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;
import rocks.xmpp.util.concurrent.AsyncResult;
import rocks.xmpp.util.concurrent.CompletionStages;

/**
 * @author Christian Schudt
 */
public abstract class AbstractServiceDiscoveryManager implements ServiceDiscoveryManager {

    private static final Set<String> FEATURES;

    static {
        Set<String> features = new HashSet<>();
        features.add(ItemDiscovery.NAMESPACE);
        features.add(InfoDiscovery.NAMESPACE);
        FEATURES = Collections.unmodifiableSet(features);
    }

    private final DiscoInfoHandler discoInfoHandler;

    private final DiscoItemsHandler discoItemHandler;

    protected AbstractServiceDiscoveryManager() {
        this.discoInfoHandler = new DiscoInfoHandler();
        this.discoItemHandler = new DiscoItemsHandler();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@value InfoDiscovery#NAMESPACE}
     */
    @Override
    public final String getNamespace() {
        return InfoDiscovery.NAMESPACE;
    }

    @Override
    public final boolean isEnabled() {
        return true;
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }

    @Override
    public final Class<?> getPayloadClass() {
        return ServiceDiscoveryNode.class;
    }

    @Override
    public final IQ handleRequest(IQ iq) {
        if (iq.hasExtension(InfoDiscovery.class)) {
            return discoInfoHandler.handleRequest(iq);
        } else if (iq.hasExtension(ItemNode.class)) {
            return discoItemHandler.handleRequest(iq);
        }
        return iq.createError(Condition.SERVICE_UNAVAILABLE);
    }

    @Override
    public final AsyncResult<DiscoverableInfo> discoverInformation(Jid jid) {
        return discoverInformation(jid, null);
    }

    @Override
    public final AsyncResult<ItemNode> discoverItems(Jid jid) {
        return discoverItems(jid, null, null);
    }

    @Override
    public final AsyncResult<ItemNode> discoverItems(Jid jid, ResultSetManagement resultSet) {
        return discoverItems(jid, null, resultSet);
    }

    @Override
    public final AsyncResult<ItemNode> discoverItems(Jid jid, String node) {
        return discoverItems(jid, node, null);
    }

    @Override
    public final boolean addInfoProvider(final InfoProvider infoProvider) {
        return discoInfoHandler.addInfoProvider(infoProvider);
    }

    @Override
    public final boolean removeInfoProvider(final InfoProvider infoNodeProvider) {
        return discoInfoHandler.removeInfoProvider(infoNodeProvider);
    }

    @Override
    public final boolean addItemProvider(ItemProvider itemProvider) {
        return discoItemHandler.addItemProvider(itemProvider);
    }

    @Override
    public final boolean removeItemProvider(ItemProvider itemProvider) {
        return discoItemHandler.removeItemProvider(itemProvider);
    }

    @Override
    public final AsyncResult<List<Item>> discoverServices(Jid jid, Identity identity) {
        return discoverServices(jid, infoNode -> {
            for (Identity id : infoNode.getIdentities()) {
                if (id.getCategory().equals(identity.getCategory()) && id.getType().equals(identity.getType())) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public final AsyncResult<List<Item>> discoverServices(Jid jid, String feature) {
        return discoverServices(jid, infoNode -> infoNode.getFeatures().contains(feature));
    }

    private AsyncResult<List<Item>> discoverServices(Jid jid, Predicate<DiscoverableInfo> predicate) {
        // First discover the items of the server.
        // Then, for each item, discover the features of the item, but ignore any exceptions.
        return discoverItems(jid).thenCompose(itemDiscovery -> {
            Collection<CompletionStage<List<Item>>> stages = itemDiscovery.getItems().stream()
                    .map(item -> discoverInformation(item.getJid()).thenApply(infoDiscovery -> {
                        if (predicate.test(infoDiscovery)) {
                            return Collections.singletonList(item);
                        }
                        return Collections.<Item>emptyList();
                    }).handle((items, throwable) -> {
                        // If one disco#info fails, don't let the whole discoverServices() method fail.
                        // Instead of failing, return an empty list, other services can hopefully
                        // be discovered successfully.
                        if (throwable != null) {
                            return Collections.<Item>emptyList();
                        } else {
                            return items;
                        }
                    }))
                    .collect(Collectors.toList());
            return CompletionStages.allOf(stages);
        });
    }
}
