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

package rocks.xmpp.extensions.caps;

import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.server.ServerStreamFeatureNegotiator;
import rocks.xmpp.extensions.caps.model.EntityCapabilities1;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class ServerEntityCapabilities extends EntityCapabilities1Protocol implements ServerStreamFeatureNegotiator<EntityCapabilities1> {

    @Inject
    private ServiceDiscoveryManager serviceDiscoveryManager;

    public ServerEntityCapabilities(ServiceDiscoveryManager serviceDiscoveryManager, EntityCapabilitiesCache entityCapabilitiesCache) {
        super(serviceDiscoveryManager, entityCapabilitiesCache);
    }

    @Override
    public EntityCapabilities1 createStreamFeature() {
        try {
            return new EntityCapabilities1("http://xmpp.rocks", serviceDiscoveryManager.getRootNode(), MessageDigest.getInstance("SHA-1"));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public StreamNegotiationResult processNegotiation(Object element) {
        return StreamNegotiationResult.IGNORE;
    }

    @Override
    public boolean canProcess(Object element) {
        return false;
    }

    @Override
    public Class<EntityCapabilities1> getFeatureClass() {
        return EntityCapabilities1.class;
    }
}
