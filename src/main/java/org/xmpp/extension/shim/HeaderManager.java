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

package org.xmpp.extension.shim;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.stanza.StanzaException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manages support for <a href="http://xmpp.org/extensions/xep-0131.html">XEP-0131: Stanza Headers and Internet Metadata</a>.
 * <p>
 * By default support is disabled, so that service discovery won't reveal the 'http://jabber.org/protocol/shim' feature.
 * </p>
 *
 * @author Christian Schudt
 */
public final class HeaderManager extends ExtensionManager implements InfoNode {

    private final Set<String> supportedHeaders;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private HeaderManager(Connection connection) {
        super(connection, Headers.NAMESPACE);
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
    public String getNode() {
        return Headers.NAMESPACE;
    }

    /**
     * Discovers the supported headers of another entity.
     *
     * @param jid The JID.
     * @return The list of supported headers.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public List<String> discoverSupportedHeaders(Jid jid) throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(jid, Headers.NAMESPACE);
        List<String> result = new ArrayList<>();
        for (Feature feature : infoNode.getFeatures()) {
            String var = feature.getVar();
            result.add(var.substring(var.indexOf("#") + 1));
        }
        return result;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            serviceDiscoveryManager.removeInfoNode(Headers.NAMESPACE);
        } else {
            serviceDiscoveryManager.addInfoNode(this);
        }
    }

    @Override
    public Set<Feature> getFeatures() {
        Set<Feature> features = new HashSet<>();
        // http://xmpp.org/extensions/xep-0131.html#disco-header
        for (String supportedHeader : supportedHeaders) {
            features.add(new Feature(Headers.NAMESPACE + "#" + supportedHeader));
        }
        return features;
    }

    @Override
    public Set<Identity> getIdentities() {
        return null;
    }

    @Override
    public List<DataForm> getExtensions() {
        return null;
    }
}
