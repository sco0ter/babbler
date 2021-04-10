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

package rocks.xmpp.extensions.disco.server;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.disco.AbstractServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoProvider;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.disco.model.items.ItemProvider;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;
import rocks.xmpp.session.server.InboundClientSession;
import rocks.xmpp.session.server.SessionManager;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class ServerServiceDiscoveryManager extends AbstractServiceDiscoveryManager {

    @Inject
    private SessionManager sessionManager;

    @Inject
    private Instance<InfoProvider> infoProviders;

    @Inject
    private Instance<ItemProvider> itemProviders;

    @Inject
    private ServerInfoNode serverInfoNode;

    @PostConstruct
    public void init() {
        infoProviders.stream().forEach(this::addInfoProvider);
        itemProviders.stream().forEach(this::addItemProvider);
    }

    @Override
    public AsyncResult<DiscoverableInfo> discoverInformation(Jid jid, String node) {
        InboundClientSession session = (InboundClientSession) sessionManager.getSession(jid);
        return session.query(IQ.get(new InfoDiscovery(node)))
                .thenApply(result -> result.getExtension(DiscoverableInfo.class));
    }

    @Override
    public AsyncResult<ItemNode> discoverItems(Jid jid, String node, ResultSetManagement resultSetManagement) {
        return null;
    }

    @Override
    public void addIdentity(Identity identity) {

    }

    @Override
    public void removeIdentity(Identity identity) {

    }

    @Override
    public void addFeature(String feature) {

    }

    @Override
    public void removeFeature(String feature) {

    }

    @Override
    public DiscoverableInfo getDefaultInfo() {
        return serverInfoNode;
    }
}
