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

package rocks.xmpp.core.session;

import rocks.xmpp.core.stream.StreamFeatureListener;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.tls.model.Failure;
import rocks.xmpp.core.tls.model.Proceed;
import rocks.xmpp.core.tls.model.StartTls;

/**
 * Negotiates transport layer security during stream negotiation.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/rfcs/rfc6120.html#tls">STARTTLS Negotiation</a>
 */
final class SecurityManager extends StreamFeatureNegotiator {

    private final boolean isSecure;

    public SecurityManager(XmppSession xmppSession, StreamFeatureListener streamFeatureListener, boolean isSecure) {
        super(xmppSession, StartTls.class);
        this.isSecure = isSecure;
        addFeatureListener(streamFeatureListener);
    }

    @Override
    public Status processNegotiation(Object element) throws StreamNegotiationException {
        if (element instanceof StartTls) {
            StartTls startTls = (StartTls) element;
            if (startTls.isMandatory() && !isSecure) {
                throw new StreamNegotiationException("The server requires TLS, but you disabled it.");
            }
            if (isSecure) {
                xmppSession.send(new StartTls());
            } else {
                return Status.IGNORE;
            }
        } else if (element instanceof Proceed) {
            notifyFeatureNegotiated();
            return Status.SUCCESS;
        } else if (element instanceof Failure) {
            throw new StreamNegotiationException("Failure during TLS negotiation.");
        }

        return Status.INCOMPLETE;
    }

    @Override
    public boolean needsRestart() {
        return true;
    }

    @Override
    public boolean canProcess(Object element) {
        return element instanceof Proceed || element instanceof Failure;
    }
}
