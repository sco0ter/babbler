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

import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.disco.handler.DiscoInfoHandler;
import rocks.xmpp.extensions.disco.handler.DiscoItemsHandler;
import rocks.xmpp.extensions.disco.model.ServiceDiscoveryNode;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.rsm.ResultSetProvider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    protected final DiscoInfoHandler discoInfoHandler;

    protected final DiscoItemsHandler discoItemHandler;

    protected AbstractServiceDiscoveryManager() {
        this.discoInfoHandler = new DiscoInfoHandler();
        this.discoItemHandler = new DiscoItemsHandler();
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
        if (iq.hasExtension(InfoNode.class)) {
            return discoInfoHandler.handleRequest(iq);
        } else if (iq.hasExtension(ItemNode.class)) {
            return discoItemHandler.handleRequest(iq);
        }
        return iq.createError(Condition.SERVICE_UNAVAILABLE);
    }

    @Override
    public final void addInfoNode(InfoNode infoNode) {
        discoInfoHandler.addInfoNode(infoNode);
    }

    @Override
    public final void removeInfoNode(String node) {
        discoInfoHandler.removeInfoNode(node);
    }

    @Override
    public final InfoNode getRootNode() {
        return discoInfoHandler.getRootNode();
    }

    @Override
    public final void setItemProvider(ResultSetProvider<Item> itemProvider) {
        discoItemHandler.setItemProvider(itemProvider);
    }

    @Override
    public final void setItemProvider(String node, ResultSetProvider<Item> itemProvider) {
        discoItemHandler.setItemProvider(node, itemProvider);
    }
}
