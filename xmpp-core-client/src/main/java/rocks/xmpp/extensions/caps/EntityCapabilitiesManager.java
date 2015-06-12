/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.core.subscription.PresenceManager;
import rocks.xmpp.core.util.cache.DirectoryCache;
import rocks.xmpp.core.util.cache.LruCache;
import rocks.xmpp.extensions.caps.model.EntityCapabilities;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages <a href="http://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a>.
 * <p>
 * If this manager is enabled (default), entity capabilities are automatically included in every presence notification being sent.
 * </p>
 * <p>
 * You can check for an entity's capabilities by using {@link #discoverCapabilities(rocks.xmpp.core.Jid)}, which will either return cached capabilities or ask the entity.
 * </p>
 * Similarly you can ask if an entity supports a particular feature via {@link #isSupported(String, rocks.xmpp.core.Jid)}.
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a>
 */
public final class EntityCapabilitiesManager extends Manager {

    private static final Logger logger = Logger.getLogger(EntityCapabilitiesManager.class.getName());

    private static final String DEFAULT_NODE = "http://xmpp.rocks";

    private static final String HASH_ALGORITHM = "sha-1";

    // Cache up to 100 verification strings in memory.
    private static final Map<Verification, InfoNode> CAPS_CACHE = new LruCache<>(100);

    // Cache the capabilities of an entity.
    private static final Map<Jid, InfoNode> ENTITY_CAPABILITIES = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Jid, Lock> REQUESTING_LOCKS = new ConcurrentHashMap<>();

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final ExecutorService serviceDiscoverer;

    private final Map<String, Verification> publishedNodes;

    private final DirectoryCache directoryCapsCache;

    private final Consumer<PresenceEvent> inboundPresenceListener;

    private final Consumer<PresenceEvent> outboundPresenceListener;

    /**
     * Guarded by "serviceDiscoveryManager".
     */
    private boolean capsSent;

    /**
     * Guarded by "this".
     */
    private String node;

    private EntityCapabilitiesManager(final XmppSession xmppSession) {
        super(xmppSession);
        serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);

        directoryCapsCache = xmppSession.getConfiguration().getCacheDirectory() != null ? new DirectoryCache(xmppSession.getConfiguration().getCacheDirectory().resolve("caps")) : null;
        serviceDiscoverer = Executors.newSingleThreadExecutor(XmppUtils.createNamedThreadFactory("Automatic Service Discovery Thread"));
        // no need for a synchronized map, since access to this is already synchronized by this class.

        publishedNodes = new LinkedHashMap<String, Verification>(10, 0.75F, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Verification> eldest) {
                if (size() > 10) {
                    // Remove old published nodes as well, they are no longer needed.
                    serviceDiscoveryManager.removeInfoNode(eldest.getKey());
                    return true;
                }
                return false;
            }
        };

        this.inboundPresenceListener = e -> {
            final Presence presence = e.getPresence();
            if (!presence.getFrom().equals(xmppSession.getConnectedResource())) {
                final EntityCapabilities entityCapabilities = presence.getExtension(EntityCapabilities.class);
                if (entityCapabilities != null) {
                    handleEntityCaps(entityCapabilities, presence.getFrom());
                }
            }
        };

        this.outboundPresenceListener = e -> {
            final Presence presence = e.getPresence();
            if (presence.isAvailable() && presence.getTo() == null) {
                // Synchronize on sdm, to make sure no features/identities are added removed, while computing the hash.
                synchronized (publishedNodes) {
                    if (publishedNodes.isEmpty()) {
                        publishCapsNode();
                    }
                    // a client SHOULD include entity capabilities with every presence notification it sends.
                    // Get the last generated verification string here.
                    List<Verification> verifications = new ArrayList<>(publishedNodes.values());
                    Verification verification = verifications.get(verifications.size() - 1);
                    presence.getExtensions().add(new EntityCapabilities(getNode(), verification.hashAlgorithm, verification.verificationString));
                    capsSent = true;
                }
            }
        };
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        xmppSession.addInboundPresenceListener(inboundPresenceListener);
        xmppSession.addOutboundPresenceListener(outboundPresenceListener);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        xmppSession.removeInboundPresenceListener(inboundPresenceListener);
        xmppSession.removeOutboundPresenceListener(outboundPresenceListener);
    }

    @Override
    protected void initialize() {
        serviceDiscoveryManager.addCapabilitiesChangeListener(evt -> {
            // If we haven't established a presence session yet, don't care about changes in service discovery.
            // If we change features during a presence session, update the verification string and resend presence.

            // http://xmpp.org/extensions/xep-0115.html#advertise:
            // "If the supported features change during a generating entity's presence session (e.g., a user installs an updated version of a client plugin), the application MUST recompute the verification string and SHOULD send a new presence broadcast."
            synchronized (publishedNodes) {
                if (capsSent) {
                    // Whenever the verification string has changed, publish the info node.
                    publishCapsNode();

                    // Resend presence. This manager will add the caps extension later.
                    PresenceManager presenceManager = xmppSession.getManager(PresenceManager.class);
                    Presence lastPresence = presenceManager.getLastSentPresence();
                    xmppSession.send(new Presence(null, lastPresence.getType(), lastPresence.getShow(), lastPresence.getStatuses(), lastPresence.getPriority(), null, null, lastPresence.getLanguage(), null, null));
                }
            }
        });
        xmppSession.addSessionStatusListener(e -> {
            switch (e.getStatus()) {
                case AUTHENTICATED:
                    // As soon as we are authenticated, check if the server has advertised Entity Capabilities in its stream features.
                    EntityCapabilities serverCapabilities = (EntityCapabilities) xmppSession.getManager(StreamFeaturesManager.class).getFeatures().get(EntityCapabilities.class);
                    // If yes, treat it as other caps.
                    if (serverCapabilities != null) {
                        handleEntityCaps(serverCapabilities, Jid.valueOf(xmppSession.getDomain()));
                    }
                    break;
                case CLOSED:
                    synchronized (serviceDiscoverer) {
                        serviceDiscoverer.shutdown();
                    }
                    break;
            }
        });
    }

    private void publishCapsNode() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);

            final InfoDiscovery infoDiscovery = new InfoDiscovery(serviceDiscoveryManager.getIdentities(), serviceDiscoveryManager.getFeatures(), serviceDiscoveryManager.getExtensions());
            Verification verification = new Verification(HASH_ALGORITHM, EntityCapabilities.getVerificationString(infoDiscovery, messageDigest));
            // Cache our own capabilities.
            writeToCache(verification, infoDiscovery);

            final String node = getNode() + "#" + verification.verificationString;

            publishedNodes.put(node, verification);
            serviceDiscoveryManager.addInfoNode(new InfoNode() {
                @Override
                public String getNode() {
                    return node;
                }

                @Override
                public Set<Identity> getIdentities() {
                    return infoDiscovery.getIdentities();
                }

                @Override
                public Set<String> getFeatures() {
                    return infoDiscovery.getFeatures();
                }

                @Override
                public List<DataForm> getExtensions() {
                    return infoDiscovery.getExtensions();
                }
            });
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Gets the node. If no node was set, a default node is returned.
     *
     * @return The node.
     * @see #setNode(String)
     */
    public synchronized String getNode() {
        return node != null ? node : DEFAULT_NODE;
    }

    /**
     * Sets the node.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0115.html#protocol">4. Protocol</a></cite></p>
     * <p>It is RECOMMENDED for the value of the 'node' attribute to be an HTTP URL at which a user could find further information about the software product, such as "http://psi-im.org" for the Psi client;</p>
     * </blockquote>
     *
     * @param node The node.
     * @see #getNode()
     */
    public synchronized void setNode(String node) {
        this.node = node;
    }

    /**
     * Discovers the capabilities of another XMPP entity.
     *
     * @param jid The JID, which should usually be a full JID.
     * @return The capabilities in form of a info node, which contains the identities, the features and service discovery extensions.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0115.html#discover">6.2 Discovering Capabilities</a>
     */
    public final InfoNode discoverCapabilities(Jid jid) throws XmppException {
        InfoNode infoNode = ENTITY_CAPABILITIES.get(jid);
        if (infoNode == null) {
            // Make sure, that for the same JID no multiple concurrent queries are sent. One is enough.
            // Use the double-checked locking idiom.

            // Acquire the lock for the JID.
            Lock lock = new ReentrantLock();
            Lock existingLock = REQUESTING_LOCKS.putIfAbsent(jid, lock);
            if (existingLock != null) {
                lock = existingLock;
            }

            lock.lock();
            // Recheck the cache (this is the double-check), maybe it has been inserted by another thread.
            infoNode = ENTITY_CAPABILITIES.get(jid);
            if (infoNode != null) {
                return infoNode;
            }
            try {
                infoNode = serviceDiscoveryManager.discoverInformation(jid);
                ENTITY_CAPABILITIES.put(jid, infoNode);
            } finally {
                lock.unlock();
                REQUESTING_LOCKS.remove(jid);
            }
        }
        return infoNode;
    }

    /**
     * Checks whether the entity supports the given feature. If the features are already known and cached
     *
     * @param feature The feature.
     * @param jid     The JID, which should usually be a full JID.
     * @return True, if this entity supports the feature.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final boolean isSupported(String feature, Jid jid) throws XmppException {
        try {
            InfoNode infoNode = discoverCapabilities(jid);
            return infoNode.getFeatures().contains(feature);
        } catch (StanzaException e) {
            return false;
        }
    }

    private void writeToCache(Verification verification, InfoNode infoNode) {
        if (directoryCapsCache != null) {
            // Write to in-memory cache.
            CAPS_CACHE.put(verification, infoNode);

            // Write to persistent cache.
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                XMLStreamWriter xmppStreamWriter = null;
                try {
                    xmppStreamWriter = XmppUtils.createXmppStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(byteArrayOutputStream), true);
                    xmppStreamWriter.flush();
                    xmppSession.createMarshaller().marshal(infoNode, xmppStreamWriter);
                } finally {
                    if (xmppStreamWriter != null) {
                        xmppStreamWriter.close();
                    }
                }
                directoryCapsCache.put(XmppUtils.hash(verification.toString().getBytes()) + ".caps", byteArrayOutputStream.toByteArray());
            } catch (Exception e) {
                logger.log(Level.WARNING, e, () -> "Could not write entity capabilities to persistent cache. Reason: " + e.getMessage());
            }
        }
    }

    private InfoNode readFromCache(Verification verification) {
        if (directoryCapsCache != null) {
            // First check the in-memory cache.
            InfoNode infoNode = CAPS_CACHE.get(verification);
            if (infoNode != null) {
                return infoNode;
            }
            // If it's not present, check the persistent cache.
            String fileName = XmppUtils.hash(verification.toString().getBytes()) + ".caps";
            try {
                byte[] bytes = directoryCapsCache.get(fileName);
                if (bytes != null) {
                    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
                        infoNode = (InfoNode) xmppSession.createUnmarshaller().unmarshal(byteArrayInputStream);
                        CAPS_CACHE.put(verification, infoNode);
                        return infoNode;
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, e, () -> "Could not read entity capabilities from persistent cache (file: " + fileName + ")");
            }
        }
        // The verification string is unknown, Service Discovery needs to be done.
        return null;
    }

    private void handleEntityCaps(final EntityCapabilities entityCapabilities, final Jid entity) {
        Verification verification = new Verification(entityCapabilities.getHashingAlgorithm(), entityCapabilities.getVerificationString());
        // Check if the verification string is already known.
        InfoNode infoNode = readFromCache(verification);
        if (entityCapabilities.getHashingAlgorithm() != null && infoNode != null) {
            // If its known, just update the information for this entity.
            ENTITY_CAPABILITIES.put(entity, infoNode);
        } else {
            // 1. Verify that the <c/> element includes a 'hash' attribute. If it does not, ignore the 'ver'
            final String hashAlgorithm = entityCapabilities.getHashingAlgorithm();
            if (hashAlgorithm != null) {
                synchronized (serviceDiscoverer) {
                    // Ask for being shutdown to prevent possible RejectedExecutionException.
                    // Need to synchronize this.
                    if (!serviceDiscoverer.isShutdown()) {
                        serviceDiscoverer.execute(() -> {
                            String nodeToDiscover = entityCapabilities.getNode() + "#" + entityCapabilities.getVerificationString();
                            try {
                                // 3. If the value of the 'hash' attribute matches one of the processing application's supported hash functions, validate the verification string by doing the following:
                                final MessageDigest messageDigest = MessageDigest.getInstance(entityCapabilities.getHashingAlgorithm());

                                // 3.1 Send a service discovery information request to the generating entity.
                                // 3.2 Receive a service discovery information response from the generating entity.
                                InfoNode infoDiscovery = serviceDiscoveryManager.discoverInformation(entity, nodeToDiscover);
                                // 3.3 If the response includes more than one service discovery identity with the same category/type/lang/name, consider the entire response to be ill-formed.
                                // 3.4 If the response includes more than one service discovery feature with the same XML character data, consider the entire response to be ill-formed.
                                // => not possible due to java.util.Set semantics and equals method.
                                // If the response had duplicates, just check the hash.

                                // 3.5 If the response includes more than one extended service discovery information form with the same FORM_TYPE or the FORM_TYPE field contains more than one <value/> element with different XML character data, consider the entire response to be ill-formed.
                                List<String> ftValues = new ArrayList<>();
                                for (DataForm dataForm : infoDiscovery.getExtensions()) {
                                    DataForm.Field formType = dataForm.findField(DataForm.FORM_TYPE);
                                    // 3.6 If the response includes an extended service discovery information form where the FORM_TYPE field is not of type "hidden" or the form does not include a FORM_TYPE field, ignore the form but continue processing.
                                    if (formType != null && formType.getType() == DataForm.Field.Type.HIDDEN && !formType.getValues().isEmpty()) {
                                        List<String> values = new ArrayList<>();
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
                                String verificationString = EntityCapabilities.getVerificationString(infoDiscovery, messageDigest);

                                // 3.8 If the values of the received and reconstructed hashes match, the processing application MUST consider the result to be valid and SHOULD globally cache the result for all JabberIDs with which it communicates.
                                if (verificationString.equals(entityCapabilities.getVerificationString())) {
                                    writeToCache(new Verification(hashAlgorithm, verificationString), infoDiscovery);
                                }
                                ENTITY_CAPABILITIES.put(entity, infoDiscovery);

                                // 3.9 If the values of the received and reconstructed hashes do not match, the processing application MUST consider the result to be invalid and MUST NOT globally cache the verification string;
                            } catch (XmppException e1) {
                                logger.log(Level.WARNING, "Failed to discover information for entity '{0}' for node '{1}'", new Object[]{entity, nodeToDiscover});
                            } catch (NoSuchAlgorithmException e1) {
                                // 2. If the value of the 'hash' attribute does not match one of the processing application's supported hash functions, do the following:
                                try {
                                    // 2.1 Send a service discovery information request to the generating entity.
                                    // 2.2 Receive a service discovery information response from the generating entity.
                                    // 2.3 Do not validate or globally cache the verification string as described below; instead, the processing application SHOULD associate the discovered identity+features only with the JabberID of the generating entity.
                                    ENTITY_CAPABILITIES.put(entity, serviceDiscoveryManager.discoverInformation(entity, nodeToDiscover));
                                } catch (XmppException e2) {
                                    logger.log(Level.WARNING, "Failed to discover information for entity '{0}' for node '{1}'", new Object[]{entity, nodeToDiscover});
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * A key for the cache (consisting of hash algorithm and verification string).
     */
    private static final class Verification {
        private final String hashAlgorithm;

        private final String verificationString;

        private Verification(String hashAlgorithm, String verificationString) {
            this.hashAlgorithm = hashAlgorithm;
            this.verificationString = verificationString;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Verification)) {
                return false;
            }
            Verification other = (Verification) o;

            return Objects.equals(hashAlgorithm, other.hashAlgorithm)
                    && Objects.equals(verificationString, other.verificationString);

        }

        @Override
        public int hashCode() {
            return Objects.hash(hashAlgorithm, verificationString);
        }

        @Override
        public String toString() {
            return hashAlgorithm + "+" + verificationString;
        }
    }
}
