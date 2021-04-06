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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;

import rocks.xmpp.extensions.caps.model.EntityCapabilities1;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;

/**
 * Abstract base class for the original Entity Capabilities protocol (XEP-0115: Entity Capabilities).
 *
 * <p>It provides the Service Discovery feature <code>{@value EntityCapabilities1#NAMESPACE}</code> and creates the entity capabilities extension
 * for either inclusion in presence (applicable for client entities) or inclusion as stream feature (for server entities).</p>
 *
 * @author Christian Schudt
 */
public abstract class AbstractEntityCapabilities1Protocol extends AbstractEntityCapabilitiesProtocol<EntityCapabilities1> {

    private static final Set<String> FEATURES = Collections.singleton(EntityCapabilities1.NAMESPACE);

    private static final String DEFAULT_NODE = "http://xmpp.rocks";

    /**
     * Guarded by "this".
     */
    private String node;

    protected AbstractEntityCapabilities1Protocol(ServiceDiscoveryManager serviceDiscoveryManager, EntityCapabilitiesCache entityCapabilitiesCache) {
        super(EntityCapabilities1.class, serviceDiscoveryManager, entityCapabilitiesCache);
    }

    @Override
    protected final EntityCapabilities1 produceEntityCapabilities(DiscoverableInfo discoverableInfo) {
        try {
            return new EntityCapabilities1(getNode(), discoverableInfo, MessageDigest.getInstance("sha-1"));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Gets the node. If no node was set, a default node is returned.
     *
     * @return The node.
     * @see #setNode(String)
     */
    public final synchronized String getNode() {
        return node != null ? node : DEFAULT_NODE;
    }

    /**
     * Sets the node.
     *
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/extensions/xep-0115.html#protocol">4. Protocol</a></cite></p>
     * <p>It is RECOMMENDED for the value of the 'node' attribute to be an HTTP URL at which a user could find further information about the software product, such as "http://psi-im.org" for the Psi client;</p>
     * </blockquote>
     *
     * @param node The node.
     * @see #getNode()
     */
    public final synchronized void setNode(String node) {
        this.node = node;
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
