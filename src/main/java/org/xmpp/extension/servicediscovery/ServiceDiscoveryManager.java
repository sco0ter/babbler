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
import org.xmpp.extension.ExtensionManager;

import java.util.*;

/**
 * @author Christian Schudt
 */
public class ServiceDiscoveryManager extends ExtensionManager {

    static final Feature feature = new Feature("http://jabber.org/protocol/disco#info");

    private final List<Identity> identities = new ArrayList<>();

    private final Set<Feature> features = new HashSet<>();

    public ServiceDiscoveryManager(Connection connection) {
        super(connection);
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
        // the <query/> element MAY include multiple <identity/> elements with the same category+type but with different 'xml:lang' values, however the <query/> element MUST NOT include multiple <identity/> elements with the same category+type+xml:lang but with different 'name' values
        if (identities.contains(identity)) {
            throw new IllegalArgumentException("An identity with that category, type and language already exists.");
        }

        identities.add(identity);
    }

    public List<Identity> getIdentities() {
        return Collections.unmodifiableList(identities);
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
}
