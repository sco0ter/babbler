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

package org.xmpp.extension.servicediscovery;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.Stanza;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class ServiceDiscoveryManager extends ExtensionManager {

    static final Feature feature = new Feature("http://jabber.org/protocol/disco#info");

    private final Set<Identity> identities = new HashSet<>();

    private final Set<Feature> features = new HashSet<>();

    public ServiceDiscoveryManager(final Connection connection) {
        super(connection);
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && iq.getType() == IQ.Type.GET && iq.getExtension(ServiceDiscovery.class) != null) {
                    if (isEnabled()) {
                        IQ result = iq.createResult();
                        ServiceDiscovery serviceDiscovery = new ServiceDiscovery(identities, features);
                        result.setExtension(serviceDiscovery);
                        connection.send(result);
                    } else {
                        IQ error = iq.createError(new Stanza.Error(new Stanza.Error.ServiceUnavailable()));
                        connection.send(error);
                    }
                }
            }
        });
        features.add(feature);
    }

    @Override
    protected Feature getFeature() {
        return feature;
    }

    /**
     * Adds an identity.
     *
     * @param identity The identity.
     */
    public void addIdentity(Identity identity) {
        identities.add(identity);
    }

    public Set<Identity> getIdentities() {
        return Collections.unmodifiableSet(identities);
    }

    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    /**
     * Adds a feature.
     *
     * @param feature The feature.
     */
    public void addFeature(Feature feature) {
        features.add(feature);
    }

    public void removeFeature(Feature feature) {
        features.remove(feature);
    }

    /**
     * Discovers information about another XMPP entity.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0030.html#info">3. Discovering Information About a Jabber Entity</a></cite></p>
     * <p>A requesting entity may want to discover information about another entity on the network. The information desired generally is of two kinds:</p>
     * <ol>
     * <li>The target entity's identity.</li>
     * <li>The features offered and protocols supported by the target entity.</li>
     * </ol>
     * </blockquote>
     *
     * @param jid The entity's JID.
     * @return The service discovery result.
     * @throws TimeoutException
     */
    public ServiceDiscovery discoverInformation(Jid jid) throws TimeoutException {
        IQ iq = new IQ(IQ.Type.GET, new ServiceDiscovery());
        iq.setTo(jid);
        IQ result = connection.query(iq);
        return result.getExtension(ServiceDiscovery.class);
    }

    public ItemDiscovery discoverItems(Jid jid) throws TimeoutException {
        IQ iq = new IQ(IQ.Type.GET, new ServiceDiscovery());
        iq.setTo(jid);
        IQ result = connection.query(iq);

        return result.getExtension(ItemDiscovery.class);
    }
}
