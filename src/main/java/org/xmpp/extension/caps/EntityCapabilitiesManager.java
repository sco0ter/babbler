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

package org.xmpp.extension.caps;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoDiscovery;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.im.PresenceManager;
import org.xmpp.stanza.Presence;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;
import org.xmpp.stanza.StanzaException;

import javax.xml.bind.DatatypeConverter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages <a href="http://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a>.
 * <p>
 * By enabling this manager, entity capabilities are automatically included in every presence notification being sent.
 * Further more it caches entity capabilities per presence session. By default support for entity capabilities is not enabled.
 * </p>
 * <p>
 * You can check for an entity's capabilities by using {@link #getCapabilities(Jid)}, which will either return cached capabilities or ask the entity.
 * </p>
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

    private boolean capsSent;

    private String currentVerificationString;

    private String node;

    private EntityCapabilitiesManager(final Connection connection) {
        super(connection, EntityCapabilities.NAMESPACE);
        serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                synchronized (serviceDiscoveryManager) {
                    // If we haven't established a presence session yet, don't care about changes in service discovery.
                    // If we change features during a presence session, update the verification string and resent presence.

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
                        PresenceManager presenceManager = connection.getPresenceManager();
                        Presence lastPresence = presenceManager.getLastSentPresence();
                        lastPresence.getExtensions().clear();
                        connection.send(lastPresence);
                    }
                }
            }
        });

        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == Connection.Status.CLOSED) {
                    jidInfos.clear();
                    cache.clear();
                    capsSent = false;
                    currentVerificationString = null;
                }
            }
        });

        connection.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (isEnabled()) {
                    Presence presence = e.getPresence();

                    if (!e.isIncoming()) {
                        if (presence.isAvailable()) {
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
                        EntityCapabilities entityCapabilities = presence.getExtension(EntityCapabilities.class);
                        if (entityCapabilities != null) {
                            synchronized (EntityCapabilitiesManager.this) {

                                Verification verification = new Verification(entityCapabilities.getHashingAlgorithm(), entityCapabilities.getVerificationString());
                                // Check if the verification string is already known.
                                if (entityCapabilities.getHashingAlgorithm() != null && cache.containsKey(verification)) {
                                    InfoNode infoNode = cache.get(verification);
                                    jidInfos.put(presence.getFrom(), infoNode);
                                } else {
                                    // 1. Verify that the <c/> element includes a 'hash' attribute. If it does not, ignore the 'ver'
                                    String hashAlgorithm = entityCapabilities.getHashingAlgorithm();
                                    if (hashAlgorithm != null) {

                                        try {
                                            // 3. If the value of the 'hash' attribute matches one of the processing application's supported hash functions, validate the verification string by doing the following:
                                            MessageDigest messageDigest = MessageDigest.getInstance(entityCapabilities.getHashingAlgorithm());

                                            try {
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
                                                String verificationString = getVerificationString(infoDiscovery, messageDigest);

                                                // 3.8 If the values of the received and reconstructed hashes match, the processing application MUST consider the result to be valid and SHOULD globally cache the result for all JabberIDs with which it communicates.
                                                if (verificationString.equals(entityCapabilities.getVerificationString())) {
                                                    cache.put(new Verification(hashAlgorithm, verificationString), infoDiscovery);
                                                }
                                                jidInfos.put(presence.getFrom(), infoDiscovery);

                                                // 3.9 If the values of the received and reconstructed hashes do not match, the processing application MUST consider the result to be invalid and MUST NOT globally cache the verification string;

                                            } catch (XmppException e1) {
                                                logger.log(Level.WARNING, e1.getMessage(), e1);
                                            }
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
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    static String getVerificationString(InfoNode infoNode, MessageDigest messageDigest) {

        List<Identity> identities = new ArrayList<>(infoNode.getIdentities());
        List<Feature> features = new ArrayList<>(infoNode.getFeatures());
        List<DataForm> dataForms = new ArrayList<>(infoNode.getExtensions());

        // 1. Initialize an empty string S.
        StringBuilder sb = new StringBuilder();

        // 2. Sort the service discovery identities [15] by category and then by type and then by xml:lang (if it exists), formatted as CATEGORY '/' [TYPE] '/' [LANG] '/' [NAME]. [16] Note that each slash is included even if the LANG or NAME is not included (in accordance with XEP-0030, the category and type MUST be included.
        Collections.sort(identities);

        // 3. For each identity, append the 'category/type/lang/name' to S, followed by the '<' character.
        for (Identity identity : identities) {
            if (identity.getCategory() != null) {
                sb.append(identity.getCategory());
            }
            sb.append("/");
            if (identity.getType() != null) {
                sb.append(identity.getType());
            }
            sb.append("/");
            if (identity.getLanguage() != null) {
                sb.append(identity.getLanguage());
            }
            sb.append("/");
            if (identity.getName() != null) {
                sb.append(identity.getName());
            }
            sb.append("<");
        }

        // 4. Sort the supported service discovery features.
        Collections.sort(features);

        // 5. For each feature, append the feature to S, followed by the '<' character.
        for (Feature feature : features) {
            if (feature.getVar() != null) {
                sb.append(feature.getVar());
            }
            sb.append("<");
        }

        // 6. If the service discovery information response includes XEP-0128 data forms, sort the forms by the FORM_TYPE (i.e., by the XML character data of the <value/> element).
        Collections.sort(dataForms);

        // 7. For each extended service discovery information form:
        for (DataForm dataForm : dataForms) {

            // 7.2. Sort the fields by the value of the "var" attribute.
            // This makes sure, that FORM_TYPE fields are always on zero position.
            Collections.sort(dataForm.getFields());

            if (!dataForm.getFields().isEmpty()) {

                // Also make sure, that we don't send an ill-formed verification string.
                // 3.6 If the response includes an extended service discovery information form where the FORM_TYPE field is not of type "hidden" or the form does not include a FORM_TYPE field, ignore the form but continue processing.
                if (!"FORM_TYPE".equals(dataForm.getFields().get(0).getVar()) || dataForm.getFields().get(0).getType() != DataForm.Field.Type.HIDDEN) {
                    // => Don't include this form in the verification string.
                    continue;
                }

                for (DataForm.Field field : dataForm.getFields()) {
                    // 7.3. For each field other than FORM_TYPE:
                    if (!"FORM_TYPE".equals(field.getVar())) {
                        // 7.3.1. Append the value of the "var" attribute, followed by the '<' character.
                        sb.append(field.getVar());
                        sb.append("<");

                        // 7.3.2. Sort values by the XML character data of the <value/> element.
                        Collections.sort(field.getValues());
                    }
                    // 7.1. Append the XML character data of the FORM_TYPE field's <value/> element, followed by the '<' character.
                    // 7.3.3. For each <value/> element, append the XML character data, followed by the '<' character.
                    for (String value : field.getValues()) {
                        sb.append(value);
                        sb.append("<");
                    }
                }

            }
        }

        // 8. Ensure that S is encoded according to the UTF-8 encoding
        String plainString = sb.toString();

        // 9. Compute the verification string by hashing S using the algorithm specified in the 'hash' attribute.
        messageDigest.reset();
        return DatatypeConverter.printBase64Binary(messageDigest.digest(plainString.getBytes()));
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
        currentVerificationString = getVerificationString(infoDiscovery, messageDigest);
        cache.put(new Verification(HASH_ALGORITHM, currentVerificationString), infoDiscovery);
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
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public synchronized InfoNode getCapabilities(Jid jid) throws XmppException {
        InfoNode infoNode = jidInfos.get(jid);
        if (infoNode == null) {
            infoNode = serviceDiscoveryManager.discoverInformation(jid);
            jidInfos.put(jid, infoNode);
        }
        return infoNode;
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
    }
}
