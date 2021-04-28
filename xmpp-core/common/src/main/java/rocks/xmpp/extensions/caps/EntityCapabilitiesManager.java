/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

import java.util.concurrent.CompletionException;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * A manager for Entity Capabilities whose primary task is to cache capabilities.
 *
 * <p>Capabilities are the identities, features and extensions discovered by Service Discovery request and consolidated
 * as "capability set" in {@link DiscoverableInfo}. Each capability set is hashed using a protocol specific
 * algorithm and then cached.</p>
 *
 * <p>Capabilities can be shared among multiple entities, e.g. if they use the same client software. To avoid multiple
 * service discovery requests to each entity, entities provide a hash value and capabilities can be read from a cache
 * instead.</p>
 */
public interface EntityCapabilitiesManager {

    /**
     * Reads capabilities from a cache.
     *
     * @param hash The hash which identifies the capabilities.
     * @return The discovered info (capabilities) associated with the hash.
     * @see #writeCapabilities(Hash, DiscoverableInfo)
     */
    DiscoverableInfo readCapabilities(Hash hash);

    /**
     * Writes capabilities to the cache.
     *
     * <p>A capability is identified by a hash value and can be shared among multiple entities, e.g. if multiple
     * entities use the same client software with the same features.</p>
     *
     * <p>Capabilities should be cached permanently, so that they can be reused.</p>
     *
     * @param hash             The hash which identifies the capabilities.
     * @param discoverableInfo The discovered info (capabilities) to cache.
     * @see #readCapabilities(Hash)
     */
    void writeCapabilities(Hash hash, DiscoverableInfo discoverableInfo);

    /**
     * Reads entity capabilities for an XMPP entity (JID).
     *
     * @param entity The XMPP entity.
     * @return The entity capabilities.
     * @see #writeEntityCapabilities(Jid, DiscoverableInfo)
     */
    DiscoverableInfo readEntityCapabilities(Jid entity);

    /**
     * Writes entity capabilities for an XMPP entity (JID).
     *
     * <p>Usually it is sufficient to cache capabilities associated with an entity (JID) in-memory.</p>
     *
     * @param entity           The XMPP entity.
     * @param discoverableInfo The entity capabilities.
     * @see #readEntityCapabilities(Jid)
     */
    void writeEntityCapabilities(Jid entity, DiscoverableInfo discoverableInfo);

    /**
     * Discovers capabilities for the given entity. If there are cached capabilities for the given entity, they should
     * be returned. Otherwise a service discovery request must be made, which should then be cached.
     *
     * @param jid The entity.
     * @return The result containing the capabilities.
     */
    AsyncResult<DiscoverableInfo> discoverCapabilities(Jid jid);

    /**
     * Checks whether a feature is supported by checking the capabilities set of the entity. If the capabilities are
     * unknown a service discovery request is made.
     *
     * @param feature The feature.
     * @param jid     The entity, which should usually be a full JID.
     * @return The result indicating, if the entity supports the feature.
     * @see DiscoverableInfo#getFeatures()
     * @see #discoverCapabilities(Jid)
     */
    default AsyncResult<Boolean> isSupported(String feature, Jid jid) {
        return discoverCapabilities(jid)
                .handle((infoNode, e) -> {
                    if (e == null) {
                        return infoNode.getFeatures().contains(feature);
                    } else {
                        if (e.getCause() instanceof StanzaErrorException) {
                            return false;
                        }
                        throw (CompletionException) e;
                    }
                });
    }
}
