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

package rocks.xmpp.extensions.caps;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.PresenceListener;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.subscription.PresenceManager;
import rocks.xmpp.core.util.cache.DirectoryCache;
import rocks.xmpp.extensions.caps.model.EntityCapabilities;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages <a href="http://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a>.
 * <p>
 * If this manager is enabled (default), entity capabilities are automatically included in every presence notification being sent.
 * </p>
 * <p>
 * You can check for an entity's capabilities by using {@link #getCapabilities(rocks.xmpp.core.Jid)}, which will either return cached capabilities or ask the entity.
 * </p>
 * Similarly you can ask if an entity supports a particular feature via {@link #isSupported(String, rocks.xmpp.core.Jid)}.
 *
 * @author Christian Schudt
 */
public final class EntityCapabilitiesManager extends ExtensionManager implements SessionStatusListener, PresenceListener, PropertyChangeListener {

    private static final Logger logger = Logger.getLogger(EntityCapabilitiesManager.class.getName());

    private static final String DEFAULT_NODE = "http://xmpp.rocks";

    private static final String HASH_ALGORITHM = "sha-1";

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Map<Jid, InfoNode> jidInfos = new ConcurrentHashMap<>();

    private final ExecutorService serviceDiscoverer;

    private final Map<String, Verification> publishedNodes;

    private final DirectoryCache directoryCapsCache;

    private boolean capsSent;

    private String node;

    private EntityCapabilitiesManager(final XmppSession xmppSession) {
        super(xmppSession, EntityCapabilities.NAMESPACE);
        serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addPropertyChangeListener(this);
        xmppSession.addSessionStatusListener(this);
        xmppSession.addPresenceListener(this);
        directoryCapsCache = new DirectoryCache(new File(xmppSession.getConfiguration().getCacheDirectory(), "caps"));
        serviceDiscoverer = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Automatic Service Discovery Thread");
                thread.setDaemon(true);
                return thread;
            }
        });
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

        setEnabled(true);
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
                public Set<Feature> getFeatures() {
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0115.html#discover">6.2 Discovering Capabilities</a>
     */
    public InfoNode discoverCapabilities(Jid jid) throws XmppException {
        InfoNode infoNode = jidInfos.get(jid);
        if (infoNode == null) {
            synchronized (jidInfos) {
                infoNode = serviceDiscoveryManager.discoverInformation(jid);
                jidInfos.put(jid, infoNode);
            }
        }
        return infoNode;
    }

    /**
     * Gets the capabilities of another XMPP entity.
     *
     * @param jid The JID, which should usually be a full JID.
     * @return The capabilities in form of a info node, which contains the identities, the features and service discovery extensions.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @deprecated Use {@link #discoverCapabilities(rocks.xmpp.core.Jid)}
     */
    @Deprecated
    public InfoNode getCapabilities(Jid jid) throws XmppException {
        return discoverCapabilities(jid);
    }

    /**
     * Checks whether the entity supports the given feature. If the features are already known and cached
     *
     * @param feature The feature.
     * @param jid     The JID, which should usually be a full JID.
     * @return True, if this entity supports the feature.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public boolean isSupported(String feature, Jid jid) throws XmppException {
        InfoNode infoNode = discoverCapabilities(jid);
        return infoNode.getFeatures().contains(new Feature(feature));
    }

    private void writeToCache(Verification verification, InfoNode infoNode) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(byteArrayOutputStream);
            XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, true);
            xmppSession.getMarshaller().marshal(infoNode, xmppStreamWriter);
            directoryCapsCache.put(XmppUtils.hash(verification.toString().getBytes()) + ".caps", byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not write entity capabilities to persistent cache. Reason: " + e.getMessage());
        }
    }

    private InfoNode readFromCache(Verification verification) {
        String fileName = XmppUtils.hash(verification.toString().getBytes()) + ".caps";
        byte[] bytes = directoryCapsCache.get(fileName);
        if (bytes != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try {
                return (InfoNode) xmppSession.getUnmarshaller().unmarshal(byteArrayInputStream);
            } catch (JAXBException e) {
                logger.log(Level.WARNING, "Could not read e entity capabilities from persistent cache (file: " + fileName + ")");
            }
        }
        return null;
    }

    @Override
    public void handlePresence(PresenceEvent e) {
        final Presence presence = e.getPresence();

        if (!e.isIncoming()) {
            if (isEnabled() && presence.isAvailable() && presence.getTo() == null) {
                // Synchronize on sdm, to make sure no features/identities are added removed, while computing the hash.
                synchronized (serviceDiscoveryManager) {
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
        } else {
            final EntityCapabilities entityCapabilities = presence.getExtension(EntityCapabilities.class);
            if (entityCapabilities != null) {

                Verification verification = new Verification(entityCapabilities.getHashingAlgorithm(), entityCapabilities.getVerificationString());
                // Check if the verification string is already known.
                InfoNode infoNode = readFromCache(verification);
                if (entityCapabilities.getHashingAlgorithm() != null && infoNode != null) {
                    // If its known, just update the information for this entity.
                    jidInfos.put(presence.getFrom(), infoNode);
                } else {
                    // 1. Verify that the <c/> element includes a 'hash' attribute. If it does not, ignore the 'ver'
                    final String hashAlgorithm = entityCapabilities.getHashingAlgorithm();
                    if (hashAlgorithm != null) {
                        synchronized (serviceDiscoverer) {
                            // Ask for being shutdown to prevent possible RejectedExecutionException.
                            // Need to synchronize this.
                            if (!serviceDiscoverer.isShutdown()) {
                                serviceDiscoverer.execute(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            // 3. If the value of the 'hash' attribute matches one of the processing application's supported hash functions, validate the verification string by doing the following:
                                            final MessageDigest messageDigest = MessageDigest.getInstance(entityCapabilities.getHashingAlgorithm());

                                            // 3.1 Send a service discovery information request to the generating entity.
                                            // 3.2 Receive a service discovery information response from the generating entity.
                                            InfoNode infoDiscovery = serviceDiscoveryManager.discoverInformation(presence.getFrom(), entityCapabilities.getNode() + "#" + entityCapabilities.getVerificationString());
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
                                            jidInfos.put(presence.getFrom(), infoDiscovery);

                                            // 3.9 If the values of the received and reconstructed hashes do not match, the processing application MUST consider the result to be invalid and MUST NOT globally cache the verification string;
                                        } catch (XmppException e1) {
                                            logger.log(Level.WARNING, e1.getMessage(), e1);
                                        } catch (NoSuchAlgorithmException e1) {
                                            // 2. If the value of the 'hash' attribute does not match one of the processing application's supported hash functions, do the following:
                                            try {
                                                // 2.1 Send a service discovery information request to the generating entity.
                                                // 2.2 Receive a service discovery information response from the generating entity.
                                                InfoNode infoNode = serviceDiscoveryManager.discoverInformation(presence.getFrom(), entityCapabilities.getNode());
                                                // 2.3 Do not validate or globally cache the verification string as described below; instead, the processing application SHOULD associate the discovered identity+features only with the JabberID of the generating entity.
                                                jidInfos.put(presence.getFrom(), infoNode);
                                            } catch (XmppException e2) {
                                                logger.log(Level.WARNING, e2.getMessage(), e2);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            jidInfos.clear();
            synchronized (serviceDiscoverer) {
                serviceDiscoverer.shutdown();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // If we haven't established a presence session yet, don't care about changes in service discovery.
        // If we change features during a presence session, update the verification string and resend presence.

        // http://xmpp.org/extensions/xep-0115.html#advertise:
        // "If the supported features change during a generating entity's presence session (e.g., a user installs an updated version of a client plugin), the application MUST recompute the verification string and SHOULD send a new presence broadcast."
        if (capsSent) {
            // Whenever the verification string has changed, publish the info node.
            publishCapsNode();

            // Resend presence. This manager will add the caps extension later.
            PresenceManager presenceManager = xmppSession.getPresenceManager();
            Presence lastPresence = presenceManager.getLastSentPresence();
            xmppSession.send(new Presence(null, lastPresence.getType(), lastPresence.getShow(), lastPresence.getStatuses(), lastPresence.getPriority(), null, null, lastPresence.getLanguage(), null, null));
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
