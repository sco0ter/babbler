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
import org.xmpp.Jid;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.extension.servicediscovery.Identity;
import org.xmpp.extension.servicediscovery.ServiceDiscovery;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author Christian Schudt
 */
public class EntityCapabilitiesManager {

    private Map<Jid, ServiceDiscovery> entityCapabilities;

    private Map<String, ServiceDiscovery> cache = new HashMap<>();

    private Connection connection;

    public EntityCapabilitiesManager() {
        List<Identity> identityList = ServiceDiscoveryManager.INSTANCE.getIdentities();
        /*
        connection.addStanzaListener(new StanzaListener() {
            @Override
            public void handle(Stanza stanza) {
                if (stanza instanceof Presence) {
                    Presence presence = (Presence) stanza;
                    EntityCapabilities entityCapabilities = presence.getExtension(EntityCapabilities.class);

                    if (entityCapabilities != null) {
                        // 1. Verify that the <c/> element includes a 'hash' attribute. If it does not, ignore the 'ver' or treat it as generated in accordance with the Legacy Format (if supported).
                        if (entityCapabilities.getHash() == null) {
                            return;
                        }

                        MessageDigest messageDigest;
                        try {
                            messageDigest = MessageDigest.getInstance(entityCapabilities.getHash());
                            // 3. If the value of the 'hash' attribute matches one of the processing application's supported hash functions, validate the verification string by doing the following:

                            // 3.1 Send a service discovery information request to the generating entity.
                            // 3.2 Receive a service discovery information response from the generating entity.

                            // 3.7 If the response is considered well-formed, reconstruct the hash by using the service discovery information response to generate a local hash in accordance with the Generation Method).
                            // 3.8 If the values of the received and reconstructed hashes match, the processing application MUST consider the result to be valid and SHOULD globally cache the result for all JabberIDs with which it communicates.


                        } catch (NoSuchAlgorithmException e) {
                            // 2. If the value of the 'hash' attribute does not match one of the processing application's supported hash functions, do the following:
                            // 2.1 Send a service discovery information request to the generating entity.
                            // 2.2 Receive a service discovery information response from the generating entity.
                            // 2.3 Do not validate or globally cache the verification string as described below; instead, the processing application SHOULD associate the discovered identity+features only with the JabberID of the generating entity.
                        }

                        IQ iq = new IQ(IQ.Type.GET, new ServiceDiscovery(entityCapabilities.getNode() + "#" + entityCapabilities.getVerificationString()));
                        iq.setTo(presence.getFrom());

                    }
                }
            }
        });
        */
    }

    String getVerificationString(List<Identity> identities, List<Feature> features) {

        StringBuilder sb = new StringBuilder();

        sortIdentities(identities);
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

        sortFeatures(features);
        for (Feature feature : features) {
            if (feature.getVar() != null) {
                sb.append(feature.getVar());
            }
            sb.append("<");
        }

        String plainString = sb.toString();

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("sha-1");
            messageDigest.reset();
            messageDigest.update(plainString.getBytes());
            return DatatypeConverter.printBase64Binary(messageDigest.digest());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    void sortIdentities(List<Identity> identities) {
        // Sort the service discovery identities [15] by category and then by type and then by xml:lang (if it exists), formatted as CATEGORY '/' [TYPE] '/' [LANG] '/' [NAME]. [16] Note that each slash is included even if the LANG or NAME is not included (in accordance with XEP-0030, the category and type MUST be included.
        Collections.sort(identities);
    }

    void sortFeatures(List<Feature> features) {
        Collections.sort(features);
    }

    public boolean supportsProtocol(Jid jid, String protocolNamespace) {
        return true;
    }
}
