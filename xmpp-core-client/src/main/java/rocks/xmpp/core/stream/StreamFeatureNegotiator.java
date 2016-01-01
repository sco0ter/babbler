/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.model.StreamFeature;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A base class to negotiate features.
 * <p>
 * Classes which extend from this class are responsible to negotiate a single feature.
 * </p>
 * Feature negotiators are registered with the {@link StreamFeaturesManager#addFeatureNegotiator(StreamFeatureNegotiator)} method.
 *
 * @author Christian Schudt
 */
public abstract class StreamFeatureNegotiator extends Manager {

    private final Set<StreamFeatureListener> streamFeatureListeners = new CopyOnWriteArraySet<>();

    private final Class<? extends StreamFeature> featureClass;

    /**
     * Constructs a feature negotiator.
     *
     * @param xmppSession  The XMPP session.
     * @param featureClass The feature class, which represents the feature, which will be negotiated.
     */
    public StreamFeatureNegotiator(XmppSession xmppSession, Class<? extends StreamFeature> featureClass) {
        super(xmppSession, false);
        this.featureClass = featureClass;
    }

    /**
     * Adds a feature listener, which will get notified about feature negotiation status changes.
     *
     * @param streamFeatureListener The feature listener.
     */
    public final void addFeatureListener(StreamFeatureListener streamFeatureListener) {
        streamFeatureListeners.add(streamFeatureListener);
    }

    /**
     * Notifies the listener, if a feature negotiation has completed.
     *
     * @throws StreamNegotiationException If an exception occurred during feature negotiation.
     */
    protected void notifyFeatureNegotiated() throws StreamNegotiationException {
        for (StreamFeatureListener streamFeatureListener : streamFeatureListeners) {
            streamFeatureListener.featureSuccessfullyNegotiated();
        }
    }

    /**
     * Processes a feature protocol element or the feature element itself.
     *
     * @param element The XML element, which belongs to the feature negotiation, e.g. {@code <challenge/>} for SASL negotiation or the feature element itself, e.g. {@code <mechanisms/>}.
     * @return The status of the feature negotiation.
     * @throws StreamNegotiationException Any exception which might be thrown during a feature negotiation. Note that any exception thrown during the feature negotiation process is thrown by the {@link rocks.xmpp.core.session.XmppSession#connect()} method and therefore will abort the connection process.
     */
    public abstract Status processNegotiation(Object element) throws StreamNegotiationException;

    /**
     * Checks, if the feature needs a stream restart after it has been successfully negotiated.
     * Override this method (returning true), if the feature requires a stream restart after negotiation.
     * <p>
     * By default this method returns false.
     * </p>
     *
     * @return True, if the stream requires a restart after feature negotiation.
     */
    public boolean needsRestart() {
        return false;
    }

    /**
     * Checks, whether the element can be processed by the feature negotiator.
     *
     * @param element The feature protocol element, e.g. {@code <challenge/>}. The element is never the feature element itself, e.g. {@code <mechanisms/>}, which is advertised in the {@code <stream:features/>} element.
     * @return True, if the element can be processed by the feature negotiator.
     */
    public abstract boolean canProcess(Object element);

    /**
     * Gets the feature class, this negotiator is responsible for.
     *
     * @return The feature class.
     */
    public final Class<? extends StreamFeature> getFeatureClass() {
        return featureClass;
    }

    /**
     * Represents the status of the feature negotiation.
     */
    public enum Status {
        /**
         * If the feature negotiation has been successful.
         */
        SUCCESS,
        /**
         * If the feature negotiation is in progress and has not yet completed.
         */
        INCOMPLETE,
        /**
         * If the feature has been ignored.
         */
        IGNORE
    }
}
