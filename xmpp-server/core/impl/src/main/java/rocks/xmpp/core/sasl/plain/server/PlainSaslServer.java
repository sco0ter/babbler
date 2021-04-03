/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.core.sasl.plain.server;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

import rocks.xmpp.core.sasl.model.Failure;
import rocks.xmpp.core.sasl.server.CredentialValidationCallback;
import rocks.xmpp.core.sasl.server.SaslFailureException;

/**
 * @author Christian Schudt
 */
public final class PlainSaslServer implements SaslServer {

    private static final String PLAIN_MECHANISM = "PLAIN";

    private final CallbackHandler handler;

    private String authzId;

    private boolean complete;

    private boolean initialResponseReceived;

    PlainSaslServer(final CallbackHandler handler) {
        this.handler = handler;
    }

    @Override
    public final String getMechanismName() {
        return PLAIN_MECHANISM;
    }

    @Override
    public final byte[] evaluateResponse(final byte[] response) throws SaslException {
        try {
            if (response.length != 0) {
                // message   = [authzid] UTF8NUL authcId UTF8NUL passwd'
                Deque<String> tokens = new ArrayDeque<>();
                StringBuilder messageToken = new StringBuilder();

                for (byte b : response) {
                    if (b == 0) {
                        tokens.add(messageToken.toString());
                        messageToken.setLength(0);
                    } else {
                        messageToken.append((char) b);
                    }
                }
                tokens.add(messageToken.toString());
                // validate response
                if (tokens.size() != 3) {
                    throw new SaslFailureException(new Failure(Failure.Condition.MALFORMED_REQUEST, "Invalid PLAIN message format", Locale.US));
                }

                final String passwd = tokens.removeLast();
                final String authcId = tokens.removeLast();

                if (authcId == null || authcId.isEmpty()) {
                    throw new SaslFailureException(new Failure(Failure.Condition.MALFORMED_REQUEST), "No username provided");
                }
                if (passwd == null || passwd.isEmpty()) {
                    throw new SaslException("No password provided");
                }

                authzId = tokens.removeLast();
                if (authzId.isEmpty()) {
                    authzId = authcId;
                }

                final UsernamePasswordCredential credential = new UsernamePasswordCredential(authcId, passwd);
                final CredentialValidationCallback credentialValidationCallback = new CredentialValidationCallback(credential);

                handler.handle(new Callback[]{credentialValidationCallback});

                final CredentialValidationResult credentialValidationResult = credentialValidationCallback.getCredentialValidationResult();

                complete = true;
                if (credentialValidationResult == null || credentialValidationResult.getStatus() != CredentialValidationResult.Status.VALID) {
                    throw new SaslException("Authentication failed");
                }
            } else {
                // no initial response, allow one more response
                if (initialResponseReceived) {
                    throw new SaslException("No response");
                }
                return null;
            }
        } catch (IOException | UnsupportedCallbackException e) {
            throw new SaslException(e.getMessage(), e);
        } finally {
            initialResponseReceived = true;
        }
        return null;
    }

    @Override
    public final boolean isComplete() {
        return complete;
    }

    @Override
    public final String getAuthorizationID() {
        if (isComplete()) {
            return authzId;
        } else {
            throw new IllegalStateException("PLAIN authentication not completed");
        }
    }

    @Override
    public final byte[] unwrap(final byte[] incoming, final int offset, final int len) {
        if (isComplete()) {
            throw new IllegalStateException("PLAIN does not support integrity or privacy");
        } else {
            throw new IllegalStateException("PLAIN authentication not completed");
        }
    }

    @Override
    public final byte[] wrap(final byte[] outgoing, final int offset, final int len) {
        if (isComplete()) {
            throw new IllegalStateException("PLAIN does not support integrity or privacy");
        } else {
            throw new IllegalStateException("PLAIN authentication not completed");
        }
    }

    @Override
    public final Object getNegotiatedProperty(final String propName) {
        if (isComplete()) {
            return null;
        } else {
            throw new IllegalStateException("PLAIN authentication not completed");
        }
    }

    @Override
    public final void dispose() {
    }

    public static final class PlainSaslServerFactory implements SaslServerFactory {

        @Override
        public SaslServer createSaslServer(String mechanism, String protocol, String serverName, Map<String, ?> props, CallbackHandler cbh) {
            if (PLAIN_MECHANISM.equals(mechanism)) {
                return new PlainSaslServer(cbh);
            }
            return null;
        }

        @Override
        public String[] getMechanismNames(Map<String, ?> props) {
            return new String[]{PLAIN_MECHANISM};
        }
    }
}
