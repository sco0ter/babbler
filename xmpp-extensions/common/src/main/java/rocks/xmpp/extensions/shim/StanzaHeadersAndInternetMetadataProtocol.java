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

package rocks.xmpp.extensions.shim;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.shim.model.Headers;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Implementation of <a href="https://xmpp.org/extensions/xep-0131.html">XEP-0131: Stanza Headers and Internet Metadata</a>.
 *
 * @author Christian Schudt
 */
public class StanzaHeadersAndInternetMetadataProtocol implements HeaderManager {

    private static final Set<String> FEATURES = Collections.singleton(Headers.NAMESPACE);

    private final Set<String> supportedHeaders = new CopyOnWriteArraySet<>();

    private final DiscoverableInfo discoverableInfo = new HeaderInfo();

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    public StanzaHeadersAndInternetMetadataProtocol(ServiceDiscoveryManager serviceDiscoveryManager) {
        this.serviceDiscoveryManager = serviceDiscoveryManager;
    }

    @Override
    public final Set<String> getSupportedHeaders() {
        return supportedHeaders;
    }

    @Override
    public final AsyncResult<List<String>> discoverSupportedHeaders(Jid jid) {
        return serviceDiscoveryManager.discoverInformation(jid, Headers.NAMESPACE).thenApply(infoNode ->
                infoNode.getFeatures()
                        .stream()
                        .map(feature -> feature.substring(feature.indexOf('#') + 1))
                        .collect(Collectors.toList()));
    }

    @Override
    public final boolean isEnabled() {
        return !supportedHeaders.isEmpty();
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }

    @Override
    public final DiscoverableInfo getInfo(Jid to, Jid from, String node, Locale locale) {
        if (isEnabled() && Headers.NAMESPACE.equals(node)) {
            return discoverableInfo;
        }
        return null;
    }

    private final class HeaderInfo implements DiscoverableInfo {

        @Override
        public final Set<Identity> getIdentities() {
            return Collections.emptySet();
        }

        @Override
        public final Set<String> getFeatures() {
            // https://xmpp.org/extensions/xep-0131.html#disco-header
            return supportedHeaders.stream().map(supportedHeader -> Headers.NAMESPACE + '#' + supportedHeader).collect(Collectors.toSet());
        }

        @Override
        public final List<DataForm> getExtensions() {
            return Collections.emptyList();
        }
    }
}
