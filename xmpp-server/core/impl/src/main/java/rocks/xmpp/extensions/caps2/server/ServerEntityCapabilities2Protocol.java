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

package rocks.xmpp.extensions.caps2.server;

import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.server.StreamFeatureProvider;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.caps2.AbstractEntityCapabilities2Protocol;
import rocks.xmpp.extensions.caps2.model.EntityCapabilities2;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;

/**
 * @author Christian Schudt
 */
public class ServerEntityCapabilities2Protocol extends AbstractEntityCapabilities2Protocol
        implements StreamFeatureProvider<EntityCapabilities2> {

    public ServerEntityCapabilities2Protocol(ServiceDiscoveryManager serviceDiscoveryManager,
                                             EntityCapabilitiesManager entityCapabilitiesManager) {
        super(serviceDiscoveryManager, entityCapabilitiesManager);
    }

    @Override
    public EntityCapabilities2 createStreamFeature() {
        return produceEntityCapabilities(publishCapsNode());
    }

    @Override
    public StreamNegotiationResult processNegotiation(Object element) {
        return StreamNegotiationResult.IGNORE;
    }
}
