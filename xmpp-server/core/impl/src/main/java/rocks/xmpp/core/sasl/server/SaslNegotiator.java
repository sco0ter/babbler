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

package rocks.xmpp.core.sasl.server;

import java.security.Principal;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import javax.enterprise.inject.spi.CDI;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

import rocks.xmpp.core.sasl.model.Auth;
import rocks.xmpp.core.sasl.model.Challenge;
import rocks.xmpp.core.sasl.model.Failure;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.sasl.model.Response;
import rocks.xmpp.core.sasl.model.Success;
import rocks.xmpp.core.sasl.plain.server.PlainSaslServer;
import rocks.xmpp.core.sasl.scram.server.ScramCredentialCallback;
import rocks.xmpp.core.sasl.scram.server.ScramSaslServer;
import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.server.StreamFeatureProvider;
import rocks.xmpp.im.roster.server.spi.ScramIdentityStore;
import rocks.xmpp.session.server.InboundClientSession;

/**
 * Negotiates SASL with the client.
 *
 * @author Christian Schudt
 */
public final class SaslNegotiator implements StreamFeatureProvider<Mechanisms> {

    static {
        Security.insertProviderAt(new XmppSaslProvider(), 1);
    }

    private final InboundClientSession session;

    /**
     * Guarded by "this".
     */
    private SaslServer saslServer;

    public SaslNegotiator(final InboundClientSession session) {
        this.session = session;
    }

    @Override
    public final Mechanisms createStreamFeature() {
        return new Mechanisms(Arrays.asList("PLAIN", "SCRAM-SHA-1"));
    }

    @Override
    public final StreamNegotiationResult processNegotiation(final Object element) {

        try {
            if (element instanceof Auth) {
                final Auth auth = (Auth) element;
                synchronized (this) {
                    saslServer = Sasl.createSaslServer(auth.getMechanism(), "xmpp", null, Collections.emptyMap(),
                            callbacks -> {
                                String username = null;
                                for (Callback callback : callbacks) {
                                    if (callback instanceof NameCallback) {
                                        username = ((NameCallback) callback).getName();
                                    }
                                    if (callback instanceof ScramCredentialCallback) {
                                        ScramIdentityStore scramIdentityStore =
                                                CDI.current().select(ScramIdentityStore.class).get();
                                        ((ScramCredentialCallback) callback)
                                                .setScramCredential(scramIdentityStore.getScramCredential(username));
                                    }
                                    if (callback instanceof CredentialValidationCallback) {
                                        CredentialValidationCallback credentialValidationCallback =
                                                (CredentialValidationCallback) callback;
                                        IdentityStoreHandler identityStoreHandler =
                                                CDI.current().select(IdentityStoreHandler.class).get();
                                        CredentialValidationResult credentialValidationResult = identityStoreHandler
                                                .validate(credentialValidationCallback.getCredential());
                                        credentialValidationCallback
                                                .setCredentialValidationResult(credentialValidationResult);
                                    }
                                }
                            });

                    if (saslServer == null) {
                        throw new SaslFailureException(new Failure(Failure.Condition.INVALID_MECHANISM));
                    }

                    return evaluateResponse(auth.getInitialResponse());
                }
            } else if (element instanceof Response) {
                synchronized (this) {
                    return evaluateResponse(((Response) element).getValue());
                }
            }
        } catch (SaslFailureException e) {
            session.send(e.getFailure());
        } catch (SaslException e) {
            session.send(new Failure(Failure.Condition.NOT_AUTHORIZED));
        } catch (Exception e) {
            e.printStackTrace();
            session.send(new Failure(Failure.Condition.NOT_AUTHORIZED));
        }
        return StreamNegotiationResult.IGNORE;
    }

    /**
     * Evaluates a (possibly initial) response from a client and sends an answer to the client.
     *
     * @param response The client response.
     * @return The negotiation result.
     * @throws SaslException If the evaluation failed.
     */
    private StreamNegotiationResult evaluateResponse(final byte[] response) throws SaslException {
        try {
            final byte[] challenge = saslServer.evaluateResponse(response);
            if (saslServer.isComplete()) {
                session.send(new Success(challenge));
                Principal principal = new CallerPrincipal(saslServer.getAuthorizationID());
                session.setPrincipal(principal);
                dispose();
                return StreamNegotiationResult.RESTART;
            } else {
                session.send(new Challenge(challenge));
                return StreamNegotiationResult.INCOMPLETE;
            }
        } catch (Exception e) {
            dispose();
            throw e;
        }
    }

    /**
     * Disposes the {@link SaslServer} instance and sets it to null.
     */
    private void dispose() {
        try {
            saslServer.dispose();
        } catch (SaslException e) {
            e.printStackTrace();
        }
        saslServer = null;
    }

    private static final class XmppSaslProvider extends Provider {
        private XmppSaslProvider() {
            super("XMPP Sasl Provider", "1.0", "Provides SASL mechanisms, which are required for XMPP.");
            put("SaslServerFactory.PLAIN", PlainSaslServer.PlainSaslServerFactory.class.getName());
            put("SaslServerFactory.SCRAM-SHA-1", ScramSaslServer.ScramSaslServerFactory.class.getName());
        }
    }
}
