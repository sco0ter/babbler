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

import java.util.EventObject;

/**
 * A feature event notifies listeners about the negotiation process of a feature. It is fired, whenever the status of the feature negotiation changed.
 * <p>
 * This event is only fired upon a server response during feature negotiation, e.g. with a {@code <proceed/>} element during the STARTTLS negotiation.
 * It is not fired, if the client has sent a feature protocol element, e.g. {@code <auth/>}, since it does not result in a status change.
 * </p>
 *
 * @author Christian Schudt
 * @see FeatureNegotiator
 * @see FeatureListener
 */
public final class FeatureEvent extends EventObject {
    private final FeatureNegotiator.Status status;

    private final Object element;

    /**
     * Constructs a feature event.
     *
     * @param source The feature negotiator on which the event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    FeatureEvent(FeatureNegotiator source, FeatureNegotiator.Status status, Object element) {
        super(source);
        this.status = status;
        this.element = element;
    }

    /**
     * Gets the status of the feature negotiation.
     *
     * @return The negotiation status.
     */
    public FeatureNegotiator.Status getStatus() {
        return status;
    }

    /**
     * Gets the XML element, which fired the feature event.
     *
     * @return The element.
     */
    public Object getElement() {
        return element;
    }
}
