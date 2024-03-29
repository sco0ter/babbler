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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoProvider;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class ServerInfoNode implements DiscoverableInfo, InfoProvider {

    @Inject
    private Instance<ExtensionProtocol> extensionProtocols;

    @Inject
    private ServerConfiguration serverConfiguration;

    @Override
    public Set<Identity> getIdentities() {
        return Collections.singleton(Identity.serverInstantMessaging());
    }

    @Override
    public Set<String> getFeatures() {
        return extensionProtocols.stream()
                .filter(extensionProtocol -> extensionProtocol.isEnabled()
                        && extensionProtocol instanceof DiscoverableInfo)
                .flatMap(extensionProtocol -> ((DiscoverableInfo) extensionProtocol).getFeatures().stream())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public List<DataForm> getExtensions() {
        return Collections.emptyList();
    }

    @Override
    public DiscoverableInfo getInfo(Jid to, Jid from, String node, Locale locale) {
        if (serverConfiguration.getDomain().equals(to) && node == null) {
            return this;
        }
        return null;
    }
}
