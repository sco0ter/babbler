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
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.PresenceListener;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.subscription.PresenceManager;
import rocks.xmpp.extensions.caps.model.EntityCapabilities;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public final class EntityCapabilitiesManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(EntityCapabilitiesManager.class.getName());

    private static final String DEFAULT_NODE = "http://babbler-xmpp.blogspot.de/";

    private static final String HASH_ALGORITHM = "sha-1";

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Map<Verification, InfoNode> cache = new ConcurrentHashMap<>();

    private final Map<Jid, InfoNode> jidInfos = new ConcurrentHashMap<>();

    private final ExecutorService serviceDiscoverer;

    private boolean capsSent;

    private String currentVerificationString;

    private String node;

    private EntityCapabilitiesManager(final XmppSession xmppSession) {
        super(xmppSession, EntityCapabilities.NAMESPACE);
        serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                synchronized (serviceDiscoveryManager) {
                    // If we haven't established a presence session yet, don't care about changes in service discovery.
                    // If we change features during a presence session, update the verification string and resend presence.

                    // http://xmpp.org/extensions/xep-0115.html#advertise:
                    // "If the supported features change during a generating entity's presence session (e.g., a user installs an updated version of a client plugin), the application MUST recompute the verification string and SHOULD send a new presence broadcast."
                    if (capsSent) {
                        try {
                            recomputeVerificationString(HASH_ALGORITHM);
                        } catch (NoSuchAlgorithmException e1) {
                            logger.log(Level.WARNING, e1.getMessage(), e1);
                        }
                        // Whenever the verification string has changed, publish the info node.
                        publishCapsNode();

                        // Resend presence. This manager will add the caps extension later.
                        PresenceManager presenceManager = xmppSession.getPresenceManager();
                        Presence lastPresence = presenceManager.getLastSentPresence();
                        Presence presence = new Presence();
                        presence.setError(lastPresence.getError());
                        presence.setFrom(lastPresence.getFrom());
                        presence.setPriority(lastPresence.getPriority());
                        presence.setLanguage(lastPresence.getLanguage());
                        presence.setShow(lastPresence.getShow());
                        presence.setStatus(lastPresence.getStatus());
                        presence.setTo(lastPresence.getTo());
                        xmppSession.send(presence);
                    }
                }
            }
        });

        xmppSession.addSessionStatusListener(new SessionStatusListener() {
            @Override
            public void sessionStatusChanged(SessionStatusEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    jidInfos.clear();
                    cache.clear();
                    capsSent = false;
                    currentVerificationString = null;
                    serviceDiscoverer.shutdown();
                }
            }
        });

        xmppSession.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                final Presence presence = e.getPresence();

                if (!e.isIncoming()) {
                    if (isEnabled() && presence.isAvailable() && presence.getTo() == null) {
                        try {
                            // Synchronize on sdm, to make sure no features/identities are added removed, while computing the hash.
                            synchronized (serviceDiscoveryManager) {
                                if (currentVerificationString == null) {
                                    recomputeVerificationString(HASH_ALGORITHM);
                                    publishCapsNode();
                                }
                                // a client SHOULD include entity capabilities with every presence notification it sends.
                                presence.getExtensions().add(new EntityCapabilities(getNode(), HASH_ALGORITHM, currentVerificationString));
                                capsSent = true;
                            }
                        } catch (NoSuchAlgorithmException e1) {
                            logger.log(Level.WARNING, e1.getMessage(), e1);
                        }
                    }
                } else {
                    final EntityCapabilities entityCapabilities = presence.getExtension(EntityCapabilities.class);
                    if (entityCapabilities != null) {
                        synchronized (EntityCapabilitiesManager.this) {

                            Verification verification = new Verification(entityCapabilities.getHashingAlgorithm(), entityCapabilities.getVerificationString());
                            // Check if the verification string is already known.
                            if (entityCapabilities.getHashingAlgorithm() != null && cache.containsKey(verification)) {
                                InfoNode infoNode = cache.get(verification);
                                // If its known, just update the information for this entity.
                                jidInfos.put(presence.getFrom(), infoNode);
                            } else {
                                // 1. Verify that the <c/> element includes a 'hash' attribute. If it does not, ignore the 'ver'
                                final String hashAlgorithm = entityCapabilities.getHashingAlgorithm();
                                if (hashAlgorithm != null) {

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
                                                    DataForm.Field formType = dataForm.findField("FORM_TYPE");
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
                                                    cache(new Verification(hashAlgorithm, verificationString), infoDiscovery);
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
        });
        serviceDiscoverer = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Automatic Service Discovery Thread");
                thread.setDaemon(true);
                return thread;
            }
        });
        setEnabled(true);
    }

    private void publishCapsNode() {
        final Set<Identity> identities = new HashSet<>(serviceDiscoveryManager.getIdentities());
        final Set<Feature> features = new HashSet<>(serviceDiscoveryManager.getFeatures());
        final List<DataForm> extensions = new ArrayList<>(serviceDiscoveryManager.getExtensions());
        final String node = getNode() + "#" + currentVerificationString;

        serviceDiscoveryManager.addInfoNode(new InfoNode() {
            @Override
            public String getNode() {
                return node;
            }

            @Override
            public Set<Identity> getIdentities() {
                return identities;
            }

            @Override
            public Set<Feature> getFeatures() {
                return features;
            }

            @Override
            public List<DataForm> getExtensions() {
                return extensions;
            }
        });
    }

    private void recomputeVerificationString(String hashAlgorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
        InfoDiscovery infoDiscovery = new InfoDiscovery();

        infoDiscovery.getFeatures().addAll(serviceDiscoveryManager.getFeatures());
        infoDiscovery.getIdentities().addAll(serviceDiscoveryManager.getIdentities());
        infoDiscovery.getExtensions().addAll(serviceDiscoveryManager.getExtensions());
        currentVerificationString = EntityCapabilities.getVerificationString(infoDiscovery, messageDigest);
        cache(new Verification(HASH_ALGORITHM, currentVerificationString), infoDiscovery);
    }

    /**
     * Gets the node. If no node was set, a default node is returned.
     *
     * @return The node.
     * @see #setNode(String)
     */
    public String getNode() {
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
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * Gets the capabilities of another XMPP entity.
     *
     * @param jid The JID, which should usually be a full JID.
     * @return The capabilities in form of a info node, which contains the identities, the features and service discovery extensions.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public InfoNode getCapabilities(Jid jid) throws XmppException {
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
     * Checks whether the entity supports the given feature. If the features are already known and cached
     *
     * @param feature The feature.
     * @param jid     The JID, which should usually be a full JID.
     * @return True, if this entity supports the feature.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public boolean isSupported(String feature, Jid jid) throws XmppException {
        InfoNode infoNode = getCapabilities(jid);
        return infoNode.getFeatures().contains(new Feature(feature));
    }

    private void cache(Verification verification, InfoNode infoNode) {
        cache.put(verification, infoNode);
        /*
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(verification.toString() + ".xml");
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(fileOutputStream);
            XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, true);
            xmppSession.getMarshaller().marshal(infoNode, xmppStreamWriter);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to write cache.", e);
        }
        */
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

            return (hashAlgorithm == null ? other.hashAlgorithm == null : hashAlgorithm.equals(other.hashAlgorithm))
                    && (verificationString == null ? other.verificationString == null : verificationString.equals(other.verificationString));

        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + ((hashAlgorithm == null) ? 0 : hashAlgorithm.hashCode());
            result = 31 * result + ((verificationString == null) ? 0 : verificationString.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return hashAlgorithm + "_" + verificationString;
        }
    }
}
