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

package rocks.xmpp.core.stream;

import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.NoResponseException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.model.StreamFeature;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.tls.model.StartTls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the various features, which are advertised during stream negotiation.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-negotiation">4.3.  Stream Negotiation</a></cite></p>
 * <p>Because the receiving entity for a stream acts as a gatekeeper to the domains it services, it imposes certain conditions for connecting as a client or as a peer server. At a minimum, the initiating entity needs to authenticate with the receiving entity before it is allowed to send stanzas to the receiving entity (for client-to-server streams this means using SASL as described under Section 6). However, the receiving entity can consider conditions other than authentication to be mandatory-to-negotiate, such as encryption using TLS as described under Section 5. The receiving entity informs the initiating entity about such conditions by communicating "stream features": the set of particular protocol interactions that the initiating entity needs to complete before the receiving entity will accept XML stanzas from the initiating entity, as well as any protocol interactions that are voluntary-to-negotiate but that might improve the handling of an XML stream (e.g., establishment of application-layer compression as described in [XEP-0138]).</p>
 * </blockquote>
 * <p>Each feature is associated with a {@linkplain StreamFeatureNegotiator feature negotiator}, which negotiates the particular feature.</p>
 * <p>This class manages these negotiators, receives XML elements and delegates them to the responsible feature negotiator for further processing.</p>
 * <p>It negotiates the stream by sequentially negotiating each stream feature.</p>
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
public final class StreamFeaturesManager extends Manager {

    private static final EnumSet<StreamFeatureNegotiator.Status> NEGOTIATION_COMPLETED = EnumSet.of(StreamFeatureNegotiator.Status.SUCCESS, StreamFeatureNegotiator.Status.IGNORE);

    private final Lock lock = new ReentrantLock();

    private final Map<Class<? extends StreamFeature>, Condition> featureNegotiationStartedConditions = new ConcurrentHashMap<>();

    /**
     * The features which have been advertised by the server.
     */
    private final HashMap<Class<? extends StreamFeature>, StreamFeature> advertisedFeatures = new HashMap<>();

    /**
     * The list of features, which the server advertised and have not yet been negotiated.
     */
    private final List<StreamFeature> featuresToNegotiate = new ArrayList<>();

    /**
     * The feature for which negotiation has started.
     */
    private final Set<Class<? extends StreamFeature>> negotiatingFeatures = new HashSet<>();

    /**
     * The feature negotiators, which are responsible to negotiate each individual feature.
     */
    private final Set<StreamFeatureNegotiator> streamFeatureNegotiators = new CopyOnWriteArraySet<>();

    private final Condition negotiationCompleted = lock.newCondition();

    private StreamFeaturesManager(XmppSession xmppSession) {
        super(xmppSession, false);
    }

    @Override
    protected final void initialize() {
        xmppSession.addSessionStatusListener(e -> {
            switch (e.getStatus()) {
                // If we're (re)connecting, make sure any previous features are forgotten.
                case CONNECTING:
                    synchronized (this) {
                        featureNegotiationStartedConditions.clear();
                        advertisedFeatures.clear();
                        negotiatingFeatures.clear();
                    }
                    break;
                // If the connection is closed, clear everything.
                case CLOSED:
                    synchronized (this) {
                        featureNegotiationStartedConditions.clear();
                        advertisedFeatures.clear();
                        featuresToNegotiate.clear();
                        negotiatingFeatures.clear();
                        streamFeatureNegotiators.clear();
                    }
                    break;
            }
        });
    }

    /**
     * Gets the available features, which the server has advertised.
     *
     * @return The features.
     */
    @SuppressWarnings("unchecked")
    public final Map<Class<? extends StreamFeature>, StreamFeature> getFeatures() {
        // return defensive copies of mutable internal fields
        return Collections.unmodifiableMap((HashMap<Class<? extends StreamFeature>, StreamFeature>) advertisedFeatures.clone());
    }

    /**
     * Adds a new feature negotiator, which is responsible for negotiating an individual feature.
     *
     * @param streamFeatureNegotiator The feature negotiator, which is responsible for the feature.
     */
    public final void addFeatureNegotiator(StreamFeatureNegotiator streamFeatureNegotiator) {
        streamFeatureNegotiators.add(streamFeatureNegotiator);
    }

    /**
     * Processes the {@code <stream:features/>} element and immediately starts negotiating the first feature.
     *
     * @param featuresElement The {@code <stream:features/>} element.
     * @throws StreamNegotiationException If an exception occurred during feature negotiation.
     */
    public final synchronized void processFeatures(StreamFeatures featuresElement) throws StreamNegotiationException {
        List<Object> featureList = featuresElement.getFeatures();
        List<StreamFeature> sortedFeatureList = new ArrayList<>();

        featuresToNegotiate.clear();

        // Check if a feature is known, that means it must implement Feature and be added to the context.
        featureList.stream().filter(feature -> feature instanceof StreamFeature).forEach(feature -> {
            StreamFeature f = (StreamFeature) feature;
            advertisedFeatures.put(f.getClass(), f);
            sortedFeatureList.add(f);
        });
        // If the receiving entity advertises only the STARTTLS feature [...] the parties MUST consider TLS as mandatory-to-negotiate.
        if (featureList.size() == 1 && featureList.get(0) instanceof StartTls) {
            ((StartTls) featureList.get(0)).setMandatory(true);
        }

        sortedFeatureList.sort(null);

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
     * @throws StreamNegotiationException If an exception occurred during feature negotiation.
     */
    public final synchronized boolean processElement(Object element) throws StreamNegotiationException {
        // Check if the element is known to any feature negotiator.
        for (StreamFeatureNegotiator streamFeatureNegotiator : streamFeatureNegotiators) {
            if (streamFeatureNegotiator.getFeatureClass() == element.getClass() || streamFeatureNegotiator.canProcess(element)) {
                StreamFeatureNegotiator.Status status = streamFeatureNegotiator.processNegotiation(element);
                // Mark the feature negotiation as started.
                negotiatingFeatures.add(streamFeatureNegotiator.getFeatureClass());
                // If the feature has been successfully negotiated.
                if (NEGOTIATION_COMPLETED.contains(status)){
                    // Check if the feature expects a restart now.
                    if (status == StreamFeatureNegotiator.Status.SUCCESS && streamFeatureNegotiator.needsRestart()) {
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
     * @return True, if there are more feature to negotiate; false, if feature negotiation has been completed.
     * @throws StreamNegotiationException If an exception occurred during feature negotiation.
     */
    private synchronized boolean negotiateNextFeature() throws StreamNegotiationException {
        if (!featuresToNegotiate.isEmpty()) {
            StreamFeature advertisedFeature = featuresToNegotiate.remove(0);
            if (!negotiatingFeatures.contains(advertisedFeature.getClass())) {
                StreamFeatureNegotiator.Status negotiationStatus = StreamFeatureNegotiator.Status.IGNORE;
                // See if there's a feature negotiator associated with the feature.
                for (StreamFeatureNegotiator streamFeatureNegotiator : streamFeatureNegotiators) {
                    if (streamFeatureNegotiator.getFeatureClass() == advertisedFeature.getClass()) {
                        // Start negotiating the feature.
                        negotiationStatus = streamFeatureNegotiator.processNegotiation(advertisedFeature);
                        break;
                    }
                }

                negotiatingFeatures.add(advertisedFeature.getClass());

                // Check if there's a condition waiting for that feature to be negotiated.
                Condition condition = featureNegotiationStartedConditions.remove(advertisedFeature.getClass());
                if (condition != null) {
                    lock.lock();
                    try {
                        condition.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
                // If feature negotiation is incomplete, return and wait until it is completed.
                if (negotiationStatus == StreamFeatureNegotiator.Status.INCOMPLETE) {
                    return true;
                }
            }
            // If no feature negotiator was found or if the feature has been successfully negotiated, immediately go on with the next feature.
            return negotiateNextFeature();
        } else {
            lock.lock();
            try {
                negotiationCompleted.signalAll();
            } finally {
                lock.unlock();
            }
            return false;
        }
    }

    /**
     * Waits until the given feature will be negotiated. If the feature has already been negotiated it immediately returns.
     *
     * @param streamFeature The stream feature class.
     * @param timeout       The timeout.
     * @throws InterruptedException If the current thread is interrupted.
     * @throws NoResponseException  If the server didn't respond.
     */
    public final void awaitNegotiation(Class<? extends StreamFeature> streamFeature, long timeout) throws InterruptedException, NoResponseException {

        synchronized (this) {
            // Check if the feature is already negotiated and if there's no condition yet registered.
            if (negotiatingFeatures.contains(streamFeature) || featureNegotiationStartedConditions.containsKey(streamFeature)) {
                return;
            }
        }
        Condition condition = lock.newCondition();
        // Register a new "wait" condition.
        featureNegotiationStartedConditions.put(streamFeature, condition);

        lock.lock();
        try {
            // Wait until the feature will be negotiated.
            if (!condition.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("No response while waiting on feature: " + streamFeature.getSimpleName());
            }
        } catch (InterruptedException e) {
            // Restore the initial state before this method was called.
            featureNegotiationStartedConditions.remove(streamFeature);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Negotiates all pending features, if any, and waits until all features have been negotiated.
     *
     * @param timeout The timeout.
     * @throws InterruptedException       If the current thread is interrupted.
     * @throws NoResponseException        If the server didn't respond.
     * @throws StreamNegotiationException If the stream negotiation failed.
     */
    public final void completeNegotiation(long timeout) throws InterruptedException, NoResponseException, StreamNegotiationException {
        if (!negotiateNextFeature()) {
            return;
        }

        lock.lock();
        try {
            if (!negotiationCompleted.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("No response while waiting during stream feature negotiation.");
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancels negotiation and releases any locks.
     */
    public void cancelNegotiation() {
        for (Condition condition : featureNegotiationStartedConditions.values()) {
            lock.lock();
            try {
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
        featureNegotiationStartedConditions.clear();
    }
}
