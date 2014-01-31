/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
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

package org.xmpp.extension.headers;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.extension.servicediscovery.info.Identity;
import org.xmpp.extension.servicediscovery.info.InfoDiscovery;
import org.xmpp.extension.servicediscovery.info.InfoNode;
import org.xmpp.stanza.StanzaException;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;

/**
 * Manages support for <a href="http://xmpp.org/extensions/xep-0131.html">XEP-0131: Stanza Headers and Internet Metadata</a>.
 * <p>
 * By default support is disabled, so that service discovery won't reveal the 'http://jabber.org/protocol/shim' feature.
 * </p>
 *
 * @author Christian Schudt
 */
public final class HeaderManager extends ExtensionManager implements InfoNode {

    private static final String FEATURE = "http://jabber.org/protocol/shim";

    private final Set<String> supportedHeaders;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private HeaderManager(Connection connection) {
        super(connection);
        this.supportedHeaders = new CopyOnWriteArraySet<>();
        serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        setEnabled(false);
    }

    /**
     * Gets the supported headers.
     * <p>
     * If you want to advertise support for a specific header, add it to this set.
     * Service discovery requests to the 'header' node will then reveal supported headers.
     * </p>
     *
     * @return The supported headers.
     */
    public Set<String> getSupportedHeaders() {
        return supportedHeaders;
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList(FEATURE);
    }

    @Override
    public String getNode() {
        return FEATURE;
    }

    /**
     * Discovers the supported headers of another entity.
     *
     * @param jid The JID.
     * @return The list of supported headers.
     * @throws StanzaException  If entity returns an error.
     * @throws TimeoutException If the operation timed out.
     */
    public List<String> discoverSupportedHeaders(Jid jid) throws StanzaException, TimeoutException {
        InfoDiscovery infoDiscovery = serviceDiscoveryManager.discoverInformation(jid, FEATURE);
        List<String> result = new ArrayList<>();
        for (Feature feature : infoDiscovery.getFeatures()) {
            String var = feature.getVar();
            result.add(var.substring(var.indexOf("#") + 1));
        }
        return result;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            serviceDiscoveryManager.removeInfoNode(FEATURE);
        } else {
            serviceDiscoveryManager.addInfoNode(this);
        }
    }

    @Override
    public Set<Feature> getFeatures() {
        Set<Feature> features = new HashSet<>();
        // http://xmpp.org/extensions/xep-0131.html#disco-header
        for (String supportedHeader : supportedHeaders) {
            features.add(new Feature(FEATURE + "#" + supportedHeader));
        }
        return features;
    }

    @Override
    public Set<Identity> getIdentities() {
        return null;
    }
}
