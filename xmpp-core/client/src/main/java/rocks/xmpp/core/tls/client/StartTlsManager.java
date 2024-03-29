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

package rocks.xmpp.core.tls.client;

import rocks.xmpp.core.Session;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.TcpConnection;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.tls.model.Failure;
import rocks.xmpp.core.tls.model.Proceed;
import rocks.xmpp.core.tls.model.StartTls;

/**
 * Negotiates transport layer security during stream negotiation.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/rfcs/rfc6120.html#tls">STARTTLS Negotiation</a>
 */
public final class StartTlsManager implements StreamFeatureNegotiator<StartTls> {

    private final TcpConnection tcpConnection;

    private final ChannelEncryption channelEncryption;

    private final Session session;

    public StartTlsManager(Session session, TcpConnection tcpConnection, ChannelEncryption channelEncryption) {
        this.session = session;
        this.tcpConnection = tcpConnection;
        this.channelEncryption = channelEncryption;
    }

    @Override
    public StreamNegotiationResult processNegotiation(Object element) throws StreamNegotiationException {
        if (element instanceof StartTls) {
            StartTls startTls = (StartTls) element;
            if (startTls.isMandatory() && channelEncryption == ChannelEncryption.DISABLED) {
                throw new StreamNegotiationException("The server requires TLS, but you disabled it.");
            }
            if (channelEncryption == ChannelEncryption.OPTIONAL || channelEncryption == ChannelEncryption.REQUIRED) {
                session.send(new StartTls());
                return StreamNegotiationResult.INCOMPLETE;
            } else {
                return StreamNegotiationResult.IGNORE;
            }
        } else if (element instanceof Proceed) {
            try {
                tcpConnection.secureConnection();
            } catch (Exception e) {
                throw new StreamNegotiationException(e);
            }
            return StreamNegotiationResult.RESTART;
        } else if (element instanceof Failure) {
            throw new StreamNegotiationException("Failure during TLS negotiation.");
        }

        return StreamNegotiationResult.IGNORE;
    }
}
