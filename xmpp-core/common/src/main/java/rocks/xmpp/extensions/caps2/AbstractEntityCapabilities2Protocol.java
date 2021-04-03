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

package rocks.xmpp.extensions.caps2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;

import rocks.xmpp.extensions.caps.AbstractEntityCapabilitiesProtocol;
import rocks.xmpp.extensions.caps.EntityCapabilitiesCache;
import rocks.xmpp.extensions.caps2.model.EntityCapabilities2;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;

/**
 * Abstract base class for the second Entity Capabilities protocol (XEP-0390: Entity Capabilities 2.0).
 * <p>
 * It provides the Service Discovery feature <code>{@value EntityCapabilities2#NAMESPACE}</code> and creates the entity capabilities extension
 * for either inclusion in presence (applicable for client entities) or inclusion as stream feature (for server entities).
 *
 * @author Christian Schudt
 */
public abstract class AbstractEntityCapabilities2Protocol extends AbstractEntityCapabilitiesProtocol<EntityCapabilities2> {

    private static final Set<String> FEATURES = Collections.singleton(EntityCapabilities2.NAMESPACE);

    protected AbstractEntityCapabilities2Protocol(ServiceDiscoveryManager serviceDiscoveryManager, EntityCapabilitiesCache entityCapabilitiesCache) {
        super(EntityCapabilities2.class, serviceDiscoveryManager, entityCapabilitiesCache);
    }

    @Override
    protected final EntityCapabilities2 produceEntityCapabilities(DiscoverableInfo discoverableInfo) {
        try {
            return new EntityCapabilities2(discoverableInfo, MessageDigest.getInstance("sha-256"));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public final boolean isEnabled() {
        return true;
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }
}
