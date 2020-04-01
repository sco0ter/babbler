/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.sm.server;

import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.server.StreamFeatureProvider;
import rocks.xmpp.extensions.sm.AbstractStreamManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;

/**
 * @author Christian Schudt
 */
public class ServerStreamManager extends AbstractStreamManager implements StreamFeatureProvider<StreamManagement> {

    private final Session session;

    public ServerStreamManager(Session session) {
        super(session);
        this.session = session;
    }

    @Override
    public final StreamManagement createStreamFeature() {
        return new StreamManagement();
    }

    @Override
    public StreamNegotiationResult processNegotiation(Object element) throws StreamNegotiationException {
        StreamNegotiationResult result = super.processNegotiation(element);
        if (result != StreamNegotiationResult.IGNORE) {
            return result;
        }
        if (element instanceof StreamManagement.Enable) {
            // TODO check if authenticated and resource is bound.

            if (enabledByClient.compareAndSet(false, true)) {
                session.send(new StreamManagement.Enabled());
            } else {
                session.send(new StreamManagement.Failed(Condition.UNEXPECTED_REQUEST));
            }
            return StreamNegotiationResult.SUCCESS;
        }

        return StreamNegotiationResult.IGNORE;
    }
}
