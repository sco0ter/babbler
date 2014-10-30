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

package rocks.xmpp.core.stream;

import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.model.StreamFeature;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.tls.model.StartTls;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the various features, which are advertised during stream negotiation.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-negotiation">4.3.  Stream Negotiation</a></cite></p>
 * <p>Because the receiving entity for a stream acts as a gatekeeper to the domains it services, it imposes certain conditions for connecting as a client or as a peer server. At a minimum, the initiating entity needs to authenticate with the receiving entity before it is allowed to send stanzas to the receiving entity (for client-to-server streams this means using SASL as described under Section 6). However, the receiving entity can consider conditions other than authentication to be mandatory-to-negotiate, such as encryption using TLS as described under Section 5. The receiving entity informs the initiating entity about such conditions by communicating "stream features": the set of particular protocol interactions that the initiating entity needs to complete before the receiving entity will accept XML stanzas from the initiating entity, as well as any protocol interactions that are voluntary-to-negotiate but that might improve the handling of an XML stream (e.g., establishment of application-layer compression as described in [XEP-0138]).</p>
 * </blockquote>
 * <p>Each feature is associated with a {@linkplain StreamFeatureNegotiator feature negotiator}, which negotiates the particular feature.</p>
 * <p>This class manages these negotiators, receives XML elements and delegates them to the responsible feature negotiator for further processing.</p>
 * <p>It negotiates the stream by sequentially negotiating each stream feature.</p>
 *
 * @author Christian Schudt
 */
public final class StreamFeaturesManager {

    /**
     * The features which have been advertised by the server.
     */
    private final Map<Class<? extends StreamFeature>, StreamFeature> features = new ConcurrentHashMap<>();

    /**
     * The list of features, which the server advertised and have not yet been negotiated.
     */
    private final List<StreamFeature> featuresToNegotiate = new ArrayList<>();

    private final Set<Class<? extends StreamFeature>> negotiatedFeatures = new HashSet<>();

    /**
     * The feature negotiators, which are responsible to negotiate each individual feature.
     */
    private final List<StreamFeatureNegotiator> streamFeatureNegotiators = new ArrayList<>();

    /**
     * Creates a feature manager.
     *
     * @param xmppSession The connection, features will be negotiated for.
     */
    public StreamFeaturesManager(XmppSession xmppSession) {

        xmppSession.addSessionStatusListener(new SessionStatusListener() {
            @Override
            public void sessionStatusChanged(SessionStatusEvent e) {
                switch (e.getStatus()) {
                    // If we're (re)connecting, make sure any previous features are forgotten.
                    case CONNECTING:
                        features.clear();
                        featuresToNegotiate.clear();
                        negotiatedFeatures.clear();
                        break;
                    // If the connection is closed, clear everything.
                    case CLOSED:
                        streamFeatureNegotiators.clear();
                        featuresToNegotiate.clear();
                        negotiatedFeatures.clear();
                        features.clear();
                        break;
                }
            }
        });
    }

    /**
     * Gets the available features, which the server has advertised.
     *
     * @return The features.
     */
    public Map<Class<? extends StreamFeature>, StreamFeature> getFeatures() {
        return Collections.unmodifiableMap(features);
    }

    /**
     * Adds a new feature negotiator, which is responsible for negotiating an individual feature.
     *
     * @param streamFeatureNegotiator The feature negotiator, which is responsible for the feature.
     */
    public void addFeatureNegotiator(StreamFeatureNegotiator streamFeatureNegotiator) {
        streamFeatureNegotiators.add(streamFeatureNegotiator);
    }

    /**
     * Processes the {@code <stream:features/>} element and immediately starts negotiating the first feature.
     *
     * @param featuresElement The {@code <stream:features/>} element.
     * @throws Exception If an exception occurred during feature negotiation.
     */
    public void processFeatures(StreamFeatures featuresElement) throws Exception {
        List<Object> featureList = featuresElement.getFeatures();
        List<StreamFeature> sortedFeatureList = new ArrayList<>();

        featuresToNegotiate.clear();

        // Check if a feature is known, that means it must implement Feature and be added to the context.
        for (Object feature : featureList) {
            if (feature instanceof StreamFeature) {
                StreamFeature f = (StreamFeature) feature;
                features.put(f.getClass(), f);
                sortedFeatureList.add(f);
            }
        }
        // If the receiving entity advertises only the STARTTLS feature [...] the parties MUST consider TLS as mandatory-to-negotiate.
        if (featureList.size() == 1 && featureList.get(0) instanceof StartTls) {
            ((StartTls) featureList.get(0)).setMandatory(true);
        }

        Collections.sort(sortedFeatureList);

        // Store the list of features. Each feature will be negotiated sequentially, if there is a corresponding feature negotiator.
        featuresToNegotiate.addAll(sortedFeatureList);

        // Immediately start negotiating the first feature.
        negotiateNextFeature();
    }

    /**
     * Tries to process an element, which is a feature or may belong to a feature protocol, e.g. the {@code <proceed/>} element from TLS negotiation.
     *
     * @param element The element.
     * @return True, if the stream needs restarted, after a feature has been negotiated.
     * @throws Exception If an exception occurred during feature negotiation.
     */
    public boolean processElement(Object element) throws Exception {
        // Check if the element is known to any feature negotiator.
        for (StreamFeatureNegotiator streamFeatureNegotiator : streamFeatureNegotiators) {
            if (streamFeatureNegotiator.getFeatureClass() == element || streamFeatureNegotiator.canProcess(element)) {
                StreamFeatureNegotiator.Status status = streamFeatureNegotiator.processNegotiation(element);
                negotiatedFeatures.add(streamFeatureNegotiator.getFeatureClass());
                // If the feature has been successfully negotiated.
                if (status == StreamFeatureNegotiator.Status.SUCCESS || status == StreamFeatureNegotiator.Status.IGNORE) {
                    // Check if the feature expects a restart now.
                    if (streamFeatureNegotiator.needsRestart()) {
                        return true;
                    } else {
                        // If no restart is required, negotiate the next feature.
                        negotiateNextFeature();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Negotiates the next feature. If the feature has been successfully negotiated, the next feature is automatically negotiated.
     *
     * @throws Exception If an exception occurred during feature negotiation.
     */
    public void negotiateNextFeature() throws Exception {
        if (featuresToNegotiate.size() > 0) {
            StreamFeature advertisedFeature = featuresToNegotiate.remove(0);

            if (!negotiatedFeatures.contains(advertisedFeature.getClass())) {
                // See if there's a feature negotiator associated with the feature.
                for (StreamFeatureNegotiator streamFeatureNegotiator : streamFeatureNegotiators) {
                    if (streamFeatureNegotiator.getFeatureClass() == advertisedFeature.getClass()) {
                        // If feature negotiation is incomplete, return and wait until it is completed.
                        if (streamFeatureNegotiator.processNegotiation(advertisedFeature) == StreamFeatureNegotiator.Status.INCOMPLETE) {
                            return;
                        }
                    }
                }
            }
            // If no feature negotiator was found or if the feature has been successfully negotiated, immediately go on with the next feature.
            negotiateNextFeature();
        }
    }
}
