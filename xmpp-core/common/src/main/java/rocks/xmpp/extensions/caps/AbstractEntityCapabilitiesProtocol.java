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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.InboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.extensions.caps.model.EntityCapabilities;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoProvider;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.extensions.hashes.model.Hashed;
import rocks.xmpp.util.cache.LruCache;

/**
 * Base class for the Entity Capabilities protocols.
 *
 * <p>As of today, there are two versions of Entity Capabilities:</p>
 *
 * <ul>
 * <li>XEP-0115: Entity Capabilities</li>
 * <li>XEP-0390: Entity Capabilities 2.0</li>
 * </ul>
 *
 * <p>This class provides the common aspects of both protocols, both from a client and a server point of view.
 * It handles inbound presence and processes any entity capabilities extension (caching, associating capabilities to the
 * sending entity, discovering capabilities if necessary).
 * It also provides the Service Discovery node, so that other entities can discover our capabilities.</p>
 *
 * <p>Subclasses for the client should deal with outbound presence and attach Entity Capabilities to the presence,
 * while server implementations should include them as stream feature.</p>
 *
 * @param <T> The Entity Capabilities implementation.
 * @author Christian Schudt
 */
public abstract class AbstractEntityCapabilitiesProtocol<T extends EntityCapabilities>
        implements InboundPresenceHandler, InfoProvider, ExtensionProtocol {

    private static final System.Logger logger = System.getLogger(AbstractEntityCapabilitiesProtocol.class.getName());

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Map<Collection<InfoDiscovery>, EntityCapabilities> publishedNodes;

    private final Class<T> entityCapabilitiesClass;

    private final EntityCapabilitiesCache entityCapabilitiesCache;

    protected AbstractEntityCapabilitiesProtocol(final Class<T> entityCapabilitiesClass,
                                                 final ServiceDiscoveryManager serviceDiscoveryManager,
                                                 final EntityCapabilitiesCache entityCapabilitiesCache) {
        this.serviceDiscoveryManager = Objects.requireNonNull(serviceDiscoveryManager);
        this.entityCapabilitiesClass = Objects.requireNonNull(entityCapabilitiesClass);
        this.entityCapabilitiesCache = Objects.requireNonNull(entityCapabilitiesCache);
        this.publishedNodes = new LruCache<>(10);
    }

    /**
     * Gets the Entity Capabilities class, which represent different versions of caps.
     *
     * @return The class.
     * @see rocks.xmpp.extensions.caps.model.EntityCapabilities1
     * @see rocks.xmpp.extensions.caps2.model.EntityCapabilities2
     */
    public final Class<T> getEntityCapabilitiesClass() {
        return entityCapabilitiesClass;
    }

    /**
     * Gets the published nodes.
     *
     * @return The published nodes.
     */
    public final Map<Collection<InfoDiscovery>, EntityCapabilities> getPublishedNodes() {
        return Collections.unmodifiableMap(publishedNodes);
    }

    /**
     * Publishes this entity's capabilities as Service Discovery node. On the client side this can used, when sending
     * presence. On the server side this is used when advertising stream features and include the server's
     * capabilities.
     *
     * <p>The published node can be discovered by another entity.</p>
     *
     * @return The published information.
     */
    public final InfoDiscovery publishCapsNode() {

        final InfoDiscovery infoDiscovery = new InfoDiscovery(serviceDiscoveryManager.getDefaultInfo().getIdentities(),
                serviceDiscoveryManager.getDefaultInfo().getFeatures(),
                serviceDiscoveryManager.getDefaultInfo().getExtensions());

        EntityCapabilities entityCapabilities = produceEntityCapabilities(infoDiscovery);

        List<InfoDiscovery> infoDiscoveries = new ArrayList<>();
        Set<Hashed> capabilityHashSet = entityCapabilities.getCapabilityHashSet();
        for (Hashed hashed : capabilityHashSet) {

            // Cache our own capabilities.
            entityCapabilitiesCache.writeCapabilities(Hash.from(hashed), infoDiscovery);

            final String node = entityCapabilities.createCapabilityHashNode(hashed);
            infoDiscoveries.add(new InfoDiscovery(node, infoDiscovery.getIdentities(), infoDiscovery.getFeatures(),
                    infoDiscovery.getExtensions()));
        }
        publishedNodes.put(infoDiscoveries, entityCapabilities);
        return infoDiscovery;
    }

    /**
     * Handles entity capabilities by associating them to the entity or discovering the entity's capabilities if they
     * are not cached.
     *
     * <p>Entity Capabilities can either be announced in presence or in stream features.</p>
     *
     * @param entityCapabilities The entity capabilities.
     * @param entity             The entity which generated the capabilities.
     */
    public void handleEntityCapabilities(final EntityCapabilities entityCapabilities, final Jid entity) {
        processCapabilitiesHashSet(entityCapabilities.getCapabilityHashSet().iterator(), entity, entityCapabilities);
    }

    /**
     * Processes the capability hash set by using the following rules:
     *
     * <ul>
     * <li>Check if the first hash is cached</li>
     * <li>If true, associate the entity with the hash.</li>
     * <li>If false, request the capabilities from the entity by using Service Discovery</li>
     * <li>Reconstruct the hash for the retrieved capabilities, cache them (if hash matches) and associate
     * it to the entity.</li>
     * </ul>
     *
     * @param capabilityHashSet The iterator over the capability hash set.
     * @param entity            The entity to which the capabilities belong.
     * @param caps              The capabilities.
     */
    private void processCapabilitiesHashSet(final Iterator<Hashed> capabilityHashSet, final Jid entity,
                                            final EntityCapabilities caps) {
        if (capabilityHashSet.hasNext()) {
            // 1. Verify that the <c/> element includes a 'hash' attribute. If it does not, ignore the 'ver'
            final Hashed hashed = capabilityHashSet.next();
            if (hashed.getHashAlgorithm() == null) {
                return;
            }
            final Hash hash = Hash.from(hashed);
            // Check if the hash is already known.
            final DiscoverableInfo discoverableInfo = entityCapabilitiesCache.readCapabilities(hash);

            if (discoverableInfo != null) {
                // If its known, just update the information for this entity.
                entityCapabilitiesCache.writeEntityCapabilities(entity, discoverableInfo);
            } else {
                final String nodeToDiscover = caps.createCapabilityHashNode(hash);
                try {
                    // 3. If the value of the 'hash' attribute matches one of the processing application's supported
                    // hash functions, validate the verification string by doing the following:
                    final MessageDigest messageDigest = MessageDigest.getInstance(hash.getHashAlgorithm());

                    // 3.1 Send a service discovery information request to the generating entity.
                    // 3.2 Receive a service discovery information response from the generating entity.
                    logger.log(System.Logger.Level.DEBUG, "Discovering capabilities for ''{0}'' at node {1}", entity,
                            nodeToDiscover);
                    serviceDiscoveryManager.discoverInformation(entity, nodeToDiscover)
                            .whenComplete((infoDiscovery, e1) -> {
                                if (e1 != null) {
                                    processCapabilitiesHashSet(capabilityHashSet, entity, caps);
                                    logger.log(System.Logger.Level.WARNING,
                                            () -> "Failed to discover information for entity '" + entity
                                                    + "' for node '" + nodeToDiscover + "'", e1);
                                } else {
                                    // 3.3 If the response includes more than one service discovery identity with the
                                    // same category/type/lang/name, consider the entire response to be ill-formed.
                                    // 3.4 If the response includes more than one service discovery feature with the
                                    // same XML character data, consider the entire response to be ill-formed.
                                    // => not possible due to java.util.Set semantics and equals method.
                                    // If the response had duplicates, just check the hash.

                                    // 3.5 If the response includes more than one extended service discovery information
                                    // form with the same FORM_TYPE or the FORM_TYPE field contains more than
                                    // one <value/> element with different XML character data, consider the entire
                                    // response to be ill-formed.
                                    Collection<String> ftValues = new ArrayDeque<>();
                                    for (DataForm dataForm : infoDiscovery.getExtensions()) {
                                        DataForm.Field formType = dataForm.findField(DataForm.FORM_TYPE);
                                        // 3.6 If the response includes an extended service discovery information form
                                        // where the FORM_TYPE field is not of type "hidden" or the form does not
                                        // include a FORM_TYPE field, ignore the form but continue processing.
                                        if (formType != null && formType.getType() == DataForm.Field.Type.HIDDEN
                                                && !formType.getValues().isEmpty()) {
                                            Collection<String> values = new ArrayDeque<>();
                                            for (String value : formType.getValues()) {
                                                if (values.contains(value)) {
                                                    // ill-formed
                                                    return;
                                                }
                                                values.add(value);
                                            }
                                            String value = formType.getValues().get(0);
                                            if (ftValues.contains(value)) {
                                                // ill-formed
                                                return;
                                            }
                                            ftValues.add(value);
                                        }
                                    }

                                    // 3.7 If the response is considered well-formed, reconstruct the hash by using the
                                    // service discovery information response to generate a local hash in accordance
                                    // with the Generation Method).
                                    final byte[] verificationString = caps.createVerificationString(infoDiscovery);
                                    final byte[] computedHash = messageDigest.digest(verificationString);
                                    // 3.8 If the values of the received and reconstructed hashes match, the processing
                                    // application MUST consider the result to be valid and SHOULD globally cache the
                                    // result for all JabberIDs with which it communicates.
                                    if (Arrays.equals(computedHash, hash.getHashValue())) {
                                        entityCapabilitiesCache.writeCapabilities(hash, infoDiscovery);
                                    } else {
                                        processCapabilitiesHashSet(capabilityHashSet, entity, caps);
                                    }
                                    entityCapabilitiesCache.writeEntityCapabilities(entity, infoDiscovery);
                                }
                            });
                    // 3.9 If the values of the received and reconstructed hashes do not match, the processing
                    // application MUST consider the result to be invalid and MUST NOT globally cache the
                    // verification string;
                } catch (NoSuchAlgorithmException e1) {
                    // 2. If the value of the 'hash' attribute does not match one of the processing application's
                    // supported hash functions, do the following:
                    // 2.1 Send a service discovery information request to the generating entity.
                    // 2.2 Receive a service discovery information response from the generating entity.
                    // 2.3 Do not validate or globally cache the verification string as described below; instead,
                    // the processing application SHOULD associate the discovered identity+features only with the
                    // JabberID of the generating entity.
                    serviceDiscoveryManager.discoverInformation(entity, nodeToDiscover).whenComplete((result, e2) -> {
                        if (e2 != null) {
                            logger.log(System.Logger.Level.WARNING,
                                    "Failed to discover information for entity '{0}' for node '{1}'", entity,
                                    nodeToDiscover);
                        } else {
                            entityCapabilitiesCache.writeEntityCapabilities(entity, result);
                        }
                    });

                    // Additionally try next hash.
                    processCapabilitiesHashSet(capabilityHashSet, entity, caps);
                }
            }
        }
    }

    @Override
    public final void handleInboundPresence(PresenceEvent e) {
        final Presence presence = e.getPresence();
        if (!presence.getFrom().equals(((Session) e.getSource()).getLocalXmppAddress())) {
            final EntityCapabilities caps = presence.getExtension(entityCapabilitiesClass);
            if (caps != null) {
                logger.log(System.Logger.Level.DEBUG, "Processing {0}", caps);
                handleEntityCapabilities(caps, presence.getFrom());
            }
        }
    }

    @Override
    public final DiscoverableInfo getInfo(Jid to, Jid from, String node, Locale locale) {
        return publishedNodes.keySet()
                .stream()
                .flatMap(Collection::stream)
                .filter(infoNode -> Objects.equals(infoNode.getNode(), node))
                .findFirst().orElse(null);
    }

    protected abstract T produceEntityCapabilities(DiscoverableInfo discoverableInfo);
}