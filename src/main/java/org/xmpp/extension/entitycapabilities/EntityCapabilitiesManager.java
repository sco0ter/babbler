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

package org.xmpp.extension.entitycapabilities;

import org.xmpp.Connection;
import org.xmpp.ConnectionEvent;
import org.xmpp.ConnectionListener;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.dataforms.DataForm;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.extension.servicediscovery.info.Identity;
import org.xmpp.extension.servicediscovery.info.InfoDiscovery;
import org.xmpp.extension.servicediscovery.info.InfoNode;
import org.xmpp.stanza.Presence;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;
import org.xmpp.stanza.StanzaException;

import javax.xml.bind.DatatypeConverter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class EntityCapabilitiesManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(EntityCapabilitiesManager.class.getName());

    // It is RECOMMENDED for the value of the 'node' attribute to be an HTTP URL at which a user could find further information about the software product, such as "http://psi-im.org" for the Psi client;
    private static final String NODE = "http://babbler-xmpp.blogspot.de/";

    private static final String HASH_ALGORITHM = "sha-1";

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Map<Verification, InfoNode> cache = new ConcurrentHashMap<>();

    private final Map<Jid, InfoNode> jidInfos = new ConcurrentHashMap<>();

    private boolean capsSent;

    private String currentVerificationString;

    private EntityCapabilitiesManager(final Connection connection) {
        super(connection, "http://jabber.org/protocol/caps");
        serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                synchronized (EntityCapabilitiesManager.this) {
                    if (capsSent) {
                        try {
                            updateVerificationString(HASH_ALGORITHM);
                        } catch (NoSuchAlgorithmException e1) {
                            logger.log(Level.WARNING, e1.getMessage(), e1);
                        }
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
                }
            }
        });

        connection.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (isEnabled()) {
                    Presence presence = e.getPresence();
                    // a client SHOULD include entity capabilities with every presence notification it sends.
                    if (!e.isIncoming()) {
                        if (presence.isAvailable()) {
                            try {
                                synchronized (EntityCapabilitiesManager.this) {
                                    if (currentVerificationString == null) {
                                        updateVerificationString(HASH_ALGORITHM);
                                    }
                                    presence.getExtensions().add(new EntityCapabilities(NODE, HASH_ALGORITHM, currentVerificationString));
                                    capsSent = true;
                                }
                            } catch (NoSuchAlgorithmException e1) {
                                logger.log(Level.WARNING, e1.getMessage(), e1);
                            }
                        }
                    } else {
                        EntityCapabilities entityCapabilities = presence.getExtension(EntityCapabilities.class);
                        if (entityCapabilities != null) {
                            if (entityCapabilities.getHashingAlgorithm() != null && cache.containsKey(new Verification(entityCapabilities.getHashingAlgorithm(), entityCapabilities.getVerificationString()))) {
                                InfoNode infoNode = cache.get(new Verification(entityCapabilities.getHashingAlgorithm(), entityCapabilities.getVerificationString()));
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
                                            InfoNode infoDiscovery = serviceDiscoveryManager.discoverInformation(presence.getFrom(), entityCapabilities.getNode());
                                            // 3.3 If the response includes more than one service discovery identity with the same category/type/lang/name, consider the entire response to be ill-formed.
                                            // 3.4 If the response includes more than one service discovery feature with the same XML character data, consider the entire response to be ill-formed.
                                            // => not possible due to java.util.Set semantics and equals method.

                                            // 3.5 If the response includes more than one extended service discovery information form with the same FORM_TYPE or the FORM_TYPE field contains more than one <value/> element with different XML character data, consider the entire response to be ill-formed.
                                            // => TODO
                                            // 3.6 If the response includes an extended service discovery information form where the FORM_TYPE field is not of type "hidden" or the form does not include a FORM_TYPE field, ignore the form but continue processing.
                                            // => TODO

                                            // 3.7 If the response is considered well-formed, reconstruct the hash by using the service discovery information response to generate a local hash in accordance with the Generation Method).
                                            String verificationString = getVerificationString(infoDiscovery, messageDigest);

                                            // 3.8 If the values of the received and reconstructed hashes match, the processing application MUST consider the result to be valid and SHOULD globally cache the result for all JabberIDs with which it communicates.
                                            if (verificationString.equals(entityCapabilities.getVerificationString())) {
                                                cache.put(new Verification(hashAlgorithm, verificationString), infoDiscovery);
                                            }
                                            jidInfos.put(presence.getFrom(), infoDiscovery);

                                            // 3.9 If the values of the received and reconstructed hashes do not match, the processing application MUST consider the result to be invalid and MUST NOT globally cache the verification string;

                                        } catch (TimeoutException | StanzaException e1) {
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
                                        } catch (TimeoutException | StanzaException e2) {
                                            logger.log(Level.WARNING, e2.getMessage(), e2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    private void updateVerificationString(String hashAlgorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
        InfoDiscovery infoDiscovery = new InfoDiscovery();
        infoDiscovery.getFeatures().addAll(serviceDiscoveryManager.getFeatures());
        infoDiscovery.getIdentities().addAll(serviceDiscoveryManager.getIdentities());
        infoDiscovery.getExtensions().addAll(serviceDiscoveryManager.getExtensions());
        currentVerificationString = getVerificationString(infoDiscovery, messageDigest);
        cache.put(new Verification(HASH_ALGORITHM, currentVerificationString), infoDiscovery);
    }

    String getVerificationString(InfoNode infoNode, MessageDigest messageDigest) {

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
            Collections.sort(dataForm.getFields());

            if (!dataForm.getFields().isEmpty()) {

                if (!"FORM_TYPE".equals(dataForm.getFields().get(0).getVar())) {
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

    private static class Verification {
        private String hashAlgorithm;

        private String verificationString;

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
