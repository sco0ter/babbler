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

package org.xmpp.tls;

import org.xmpp.Connection;
import org.xmpp.stream.FeatureListener;
import org.xmpp.stream.FeatureNegotiator;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

/**
 * @author Christian Schudt
 */
public final class SecurityManager extends FeatureNegotiator {

    private final Connection connection;

    private volatile SSLContext sslContext;

    private boolean tlsEnabled = true;

    public SecurityManager(Connection connection, FeatureListener featureListener) {
        super(StartTls.class);
        addFeatureListener(featureListener);
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null.");
        }
        try {
            // Assign the default TLS context.
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        this.connection = connection;
    }

    @Override
    public Status processNegotiation(Object element) throws Exception {

        Status status = Status.INCOMPLETE;
        try {
            if (element instanceof StartTls) {
                StartTls startTls = (StartTls) element;
                if (startTls.isMandatory() && !tlsEnabled) {
                    throw new Exception("The server requires TLS, but you disabled it.");
                }
                if (tlsEnabled) {
                    connection.send(new StartTls());
                } else {
                    status = Status.SUCCESS;
                }
            } else if (element instanceof Proceed) {
                status = Status.SUCCESS;
            } else if (element instanceof Failure) {
                status = Status.FAILURE;
                throw new Exception("Failure during TLS negotiation.");
            }
        } finally {
            notifyFeatureNegotiated(status, element);
        }
        return status;
    }

    @Override
    public boolean needsRestart() {
        return true;
    }

    @Override
    public boolean canProcess(Object element) {
        return element instanceof Proceed || element instanceof Failure;
    }

    public SSLContext getSSLContext() {
        return sslContext;
    }

    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }
}
