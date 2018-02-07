/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.core.tls.server;

import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.ConnectionConfiguration;
import rocks.xmpp.core.net.TcpBinding;
import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.server.ServerStreamFeatureNegotiator;
import rocks.xmpp.core.tls.model.Failure;
import rocks.xmpp.core.tls.model.Proceed;
import rocks.xmpp.core.tls.model.StartTls;

/**
 * Negotiates STARTTLS for a TCP connection by advertising support for STARTTLS and later securing the connection.
 *
 * @author Christian Schudt
 */
public final class StartTlsNegotiator extends ServerStreamFeatureNegotiator<StartTls> {

    private final TcpBinding tcpBinding;

    private final ConnectionConfiguration connectionConfiguration;

    public StartTlsNegotiator(final TcpBinding tcpBinding, final ConnectionConfiguration connectionConfiguration) {
        super(StartTls.class);
        this.tcpBinding = tcpBinding;
        this.connectionConfiguration = connectionConfiguration;
    }

    @Override
    public final StartTls createStreamFeature() {
        return new StartTls(connectionConfiguration.isSecure());
    }

    @Override
    public final StreamNegotiationResult processNegotiation(final Object element) {
        if (element instanceof StartTls) {
            try {
                tcpBinding.secureConnection();
                ((Connection) tcpBinding).send((Proceed.INSTANCE));
                return StreamNegotiationResult.RESTART;
            } catch (Exception e) {
                ((Connection) tcpBinding).write(Failure.INSTANCE);
                ((Connection) tcpBinding).closeAsync();
            }
        }
        return StreamNegotiationResult.IGNORE;
    }

    @Override
    public boolean canProcess(final Object element) {
        return element instanceof StartTls;
    }
}