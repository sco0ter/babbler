/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.extensions.caps.model;

import java.util.Set;

import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.hashes.model.Hashed;

/**
 * An interface for different Entity Capabilities implementations as described in the following two specifications.
 *
 * <ul>
 * <li><a href="https://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a></li>
 * <li><a href="https://xmpp.org/extensions/xep-0390.html">XEP-0390: Entity Capabilities 2.0</a></li>
 * </ul>
 *
 * @author Christian Schudt
 */
public interface EntityCapabilities {

    /**
     * Gets the capability hash set.
     *
     * @return The capability hash set.
     */
    Set<Hashed> getCapabilityHashSet();

    /**
     * Creates an UTF-8 encoded verification string from a disco#info query.
     *
     * <p>The returned byte array is neither hashed nor base64 encoded.
     * It should be used as input for generating the capability hash set.</p>
     *
     * @param discoverableInfo The query.
     * @return The caps string, aka. verification string.
     */
    byte[] createVerificationString(DiscoverableInfo discoverableInfo);

    /**
     * Creates the Capability Hash Node.
     *
     * <p>This node is used to query the generating entity via a Service Discovery Info query (disco#info).</p>
     *
     * @param hashed The pair of hash algorithm and value.
     * @return The node.
     */
    String createCapabilityHashNode(Hashed hashed);
}
