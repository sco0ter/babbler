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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.InboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.extensions.caps.model.EntityCapabilities;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.info.InfoNodeProvider;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.extensions.hashes.model.Hashed;
import rocks.xmpp.util.cache.LruCache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Christian Schudt
 */
public abstract class AbstractEntityCapabilitiesProtocol<T extends EntityCapabilities> implements InboundPresenceHandler, InfoNodeProvider, ExtensionProtocol {

    private static final Logger logger = Logger.getLogger(AbstractEntityCapabilitiesProtocol.class.getName());

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Map<Collection<InfoNode>, EntityCapabilities> publishedNodes;

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

    public Map<Collection<InfoNode>, EntityCapabilities> getPublishedNodes() {
        return Collections.unmodifiableMap(publishedNodes);
    }

    public void publishCapsNode() {

        final InfoDiscovery infoDiscovery = new InfoDiscovery(serviceDiscoveryManager.getRootNode().getIdentities(), serviceDiscoveryManager.getRootNode().getFeatures(), serviceDiscoveryManager.getRootNode().getExtensions());

        EntityCapabilities entityCapabilities = produceEntityCapabilities(infoDiscovery);

        List<InfoNode> infoNodes = new ArrayList<>();
        Set<Hashed> capabilityHashSet = entityCapabilities.getCapabilityHashSet();
        for (Hashed hashed : capabilityHashSet) {

            // Cache our own capabilities.
            entityCapabilitiesCache.writeCapabilities(Hash.from(hashed), infoDiscovery);

            final String node = entityCapabilities.createCapabilityHashNode(hashed);
            infoNodes.add(new InfoDiscovery(node, infoDiscovery.getIdentities(), infoDiscovery.getFeatures(), infoDiscovery.getExtensions()));
        }
        publishedNodes.put(infoNodes, entityCapabilities);
    }

    public void handleEntityCapabilities(final EntityCapabilities entityCapabilities, final Jid entity) {
        processCapabilitiesHashSet(entityCapabilities.getCapabilityHashSet().iterator(), entity, entityCapabilities);
    }

    private void processCapabilitiesHashSet(final Iterator<Hashed> hashedIterator, final Jid entity, final EntityCapabilities caps) {
        if (hashedIterator.hasNext()) {
            // 1. Verify that the <c/> element includes a 'hash' attribute. If it does not, ignore the 'ver'
            final Hashed hashed = hashedIterator.next();
            if (hashed.getHashAlgorithm() == null) {
                return;
            }
            final Hash hash = Hash.from(hashed);
            // Check if the hash is already known.
            final InfoNode infoNode = entityCapabilitiesCache.readCapabilities(hash);

            if (infoNode != null) {
                // If its known, just update the information for this entity.
                entityCapabilitiesCache.writeEntityCapabilities(entity, infoNode);
            } else {
                final String nodeToDiscover = caps.createCapabilityHashNode(hash);
                try {
                    // 3. If the value of the 'hash' attribute matches one of the processing application's supported hash functions, validate the verification string by doing the following:
                    final MessageDigest messageDigest = MessageDigest.getInstance(hash.getHashAlgorithm());

                    // 3.1 Send a service discovery information request to the generating entity.
                    // 3.2 Receive a service discovery information response from the generating entity.
                    logger.log(Level.FINE, "Discovering capabilities for '{0}' at node {1}", new Object[]{entity, nodeToDiscover});
                    serviceDiscoveryManager.discoverInformation(entity, nodeToDiscover).whenComplete((infoDiscovery, e1) -> {
                        if (e1 != null) {
                            processCapabilitiesHashSet(hashedIterator, entity, caps);
                            logger.log(Level.WARNING, e1, () -> "Failed to discover information for entity '" + entity + "' for node '" + nodeToDiscover + "'");
                        } else {
                            // 3.3 If the response includes more than one service discovery identity with the same category/type/lang/name, consider the entire response to be ill-formed.
                            // 3.4 If the response includes more than one service discovery feature with the same XML character data, consider the entire response to be ill-formed.
                            // => not possible due to java.util.Set semantics and equals method.
                            // If the response had duplicates, just check the hash.

                            // 3.5 If the response includes more than one extended service discovery information form with the same FORM_TYPE or the FORM_TYPE field contains more than one <value/> element with different XML character data, consider the entire response to be ill-formed.
                            Collection<String> ftValues = new ArrayDeque<>();
                            for (DataForm dataForm : infoDiscovery.getExtensions()) {
                                DataForm.Field formType = dataForm.findField(DataForm.FORM_TYPE);
                                // 3.6 If the response includes an extended service discovery information form where the FORM_TYPE field is not of type "hidden" or the form does not include a FORM_TYPE field, ignore the form but continue processing.
                                if (formType != null && formType.getType() == DataForm.Field.Type.HIDDEN && !formType.getValues().isEmpty()) {
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

                            // 3.7 If the response is considered well-formed, reconstruct the hash by using the service discovery information response to generate a local hash in accordance with the Generation Method).
                            final byte[] verificationString = caps.createVerificationString(infoDiscovery);
                            final byte[] computedHash = messageDigest.digest(verificationString);
                            // 3.8 If the values of the received and reconstructed hashes match, the processing application MUST consider the result to be valid and SHOULD globally cache the result for all JabberIDs with which it communicates.
                            if (Arrays.equals(computedHash, hash.getHashValue())) {
                                entityCapabilitiesCache.writeCapabilities(hash, infoDiscovery);
                            } else {
                                processCapabilitiesHashSet(hashedIterator, entity, caps);
                            }
                            entityCapabilitiesCache.writeEntityCapabilities(entity, infoDiscovery);
                        }
                    });
                    // 3.9 If the values of the received and reconstructed hashes do not match, the processing application MUST consider the result to be invalid and MUST NOT globally cache the verification string;
                } catch (NoSuchAlgorithmException e1) {
                    // 2. If the value of the 'hash' attribute does not match one of the processing application's supported hash functions, do the following:
                    // 2.1 Send a service discovery information request to the generating entity.
                    // 2.2 Receive a service discovery information response from the generating entity.
                    // 2.3 Do not validate or globally cache the verification string as described below; instead, the processing application SHOULD associate the discovered identity+features only with the JabberID of the generating entity.
                    serviceDiscoveryManager.discoverInformation(entity, nodeToDiscover).whenComplete((result, e2) -> {
                        if (e2 != null) {
                            logger.log(Level.WARNING, "Failed to discover information for entity '{0}' for node '{1}'", new Object[]{entity, nodeToDiscover});
                        } else {
                            entityCapabilitiesCache.writeEntityCapabilities(entity, result);
                        }
                    });

                    // Additionally try next hash.
                    processCapabilitiesHashSet(hashedIterator, entity, caps);
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
                logger.log(Level.FINE, "Processing {0}", caps);
                handleEntityCapabilities(caps, presence.getFrom());
            }
        }
    }

    @Override
    public final Set<InfoNode> getInfoNodes(final String node) {
        return publishedNodes.keySet()
                .stream()
                .flatMap(Collection::stream)
                .filter(infoNode -> Objects.equals(infoNode.getNode(), node))
                .collect(Collectors.toSet());
    }

    protected abstract T produceEntityCapabilities(InfoNode infoNode);
}