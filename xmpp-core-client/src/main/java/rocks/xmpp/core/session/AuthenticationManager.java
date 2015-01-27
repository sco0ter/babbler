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

package rocks.xmpp.core.session;

import rocks.xmpp.core.sasl.XmppSaslClientFactory;
import rocks.xmpp.core.sasl.model.Auth;
import rocks.xmpp.core.sasl.model.AuthenticationException;
import rocks.xmpp.core.sasl.model.Challenge;
import rocks.xmpp.core.sasl.model.Failure;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.sasl.model.Response;
import rocks.xmpp.core.sasl.model.Success;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Manages SASL authentication as described in <a href="http://xmpp.org/rfcs/rfc6120.html#sasl">SASL Negotiation</a>.
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
final class AuthenticationManager extends StreamFeatureNegotiator {

    static {
        // The SunSASL Provider only supports: "PLAIN", "CRAM-MD5", "DIGEST-MD5", "GSSAPI", "EXTERNAL".
        // http://download.java.net/jdk8/docs/technotes/guides/security/sasl/sasl-refguide.html

        // Add the "ANONYMOUS" and "SCRAM-SHA-1" SASL mechanism.
        Security.addProvider(new Provider("XMPP Sasl Provider", 1.0, "Provides additional SASL mechanisms, which are required for XMPP.") {
            {
                put("SaslClientFactory.ANONYMOUS", XmppSaslClientFactory.class.getName());
                put("SaslClientFactory.SCRAM-SHA-1", XmppSaslClientFactory.class.getName());
            }
        });
    }

    private final XmppSession xmppSession;

    /**
     * The condition, which is triggered, when the authentication either failed or succeeded.
     */
    private final Condition authenticationComplete;

    /**
     * Stores the supported and preferred SASL mechanisms of the server.
     */
    private final List<String> supportedMechanisms;

    /**
     * The lock, which used to create new waiting conditions.
     */
    private final Lock lock;

    /**
     * Stores the preferred SASL mechanisms of the client.
     */
    private final List<String> preferredMechanisms;

    /**
     * The SASL client which is used during an authentication process.
     * Guarded by "this".
     */
    private SaslClient saslClient;

    /**
     * If the authentication failed, this variable is set (by the reader thread) and later read by the application thread.
     * Guarded by "this".
     */
    private Failure authenticationFailure;

    /**
     * Set to true if the authentication succeeded; or false, if it failed or not yet completed.
     * Guarded by "this".
     */
    private boolean authenticated;

    /**
     * Guarded by "this".
     */
    private String lastAuthorizationId;

    /**
     * Guarded by "this".
     */
    private String[] lastMechanisms;

    /**
     * Guarded by "this".
     */
    private CallbackHandler lastCallbackHandler;

    /**
     * Creates the authentication manager. Usually only the {@link rocks.xmpp.core.session.XmppSession} should create it implicitly.
     *
     * @param xmppSession The connection.
     * @param lock        The lock object, which is used to make the current thread wait during authentication.
     */
    public AuthenticationManager(final XmppSession xmppSession, Lock lock, List<String> mechanisms) {
        super(Mechanisms.class);
        this.xmppSession = Objects.requireNonNull(xmppSession, "xmppSession must not be null.");
        this.lock = lock;
        this.authenticationComplete = lock.newCondition();
        this.supportedMechanisms = new ArrayList<>();
        this.preferredMechanisms = new ArrayList<>(mechanisms);
    }

    /**
     * @param mechanisms      The mechanisms to use.
     * @param authorizationId The authorization identity.
     * @param callbackHandler The callback handler.
     * @throws SaslException           If a {@link SaslClient} could not be created.
     * @throws AuthenticationException If the login failed, due to a SASL error reported by the server.
     */
    public final void authenticate(String[] mechanisms, String authorizationId, CallbackHandler callbackHandler) throws SaslException, AuthenticationException {
        synchronized (this) {
            Collection<String> clientMechanisms;
            if (mechanisms == null) {
                clientMechanisms = new ArrayList<>(preferredMechanisms);
            } else {
                clientMechanisms = new ArrayList<>(Arrays.asList(mechanisms));
            }

            // Retain only the server-supported mechanisms.
            clientMechanisms.retainAll(supportedMechanisms);

            // Reset variables.
            authenticationFailure = null;
            authenticated = false;

            lastMechanisms = mechanisms;
            lastAuthorizationId = authorizationId;
            lastCallbackHandler = callbackHandler;
            saslClient = Sasl.createSaslClient(clientMechanisms.toArray(new String[clientMechanisms.size()]), authorizationId, "xmpp", xmppSession.getDomain(), new HashMap<String, Object>(), callbackHandler);

            if (saslClient == null) {
                throw new SaslException("No SASL client found for mechanisms: " + clientMechanisms);
            }

            byte[] initialResponse = new byte[0];
            if (saslClient.hasInitialResponse()) {
                initialResponse = saslClient.evaluateChallenge(new byte[0]);
            }

            xmppSession.send(new Auth(saslClient.getMechanismName(), initialResponse));
        }
        // Wait until the authentication succeeded or failed, but max. 10 seconds.
        lock.lock();
        try {
            authenticationComplete.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
        synchronized (this) {
            // At this point we should be authenticated. If not, throw an exception.
            if (!authenticated) {
                if (authenticationFailure != null) {
                    String failureText = saslClient.getMechanismName() + " authentication failed with condition " + authenticationFailure.toString();
                    if (authenticationFailure.getText() != null) {
                        failureText += " (" + authenticationFailure.getText() + ")";
                    }
                    throw new AuthenticationException(failureText, authenticationFailure);
                } else {
                    throw new AuthenticationException(saslClient.getMechanismName() + " authentication failed for an unknown reason, but probably due to timeout.");
                }
            }
        }
    }

    /**
     * Re-authenticates after a connection has disconnected and reconnected. The parameters from the last authentication process is used to re-authenticate.
     *
     * @throws SaslException           If the SASL mechanism could not be created.
     * @throws AuthenticationException If the login failed.
     */
    public final synchronized void reAuthenticate() throws SaslException, AuthenticationException {
        authenticate(lastMechanisms, lastAuthorizationId, lastCallbackHandler);
    }

    @Override
    public final Status processNegotiation(Object element) throws Exception {
        Status status = Status.INCOMPLETE;
        try {
            synchronized (this) {
                if (element instanceof Mechanisms) {
                    supportedMechanisms.clear();
                    supportedMechanisms.addAll(((Mechanisms) element).getMechanisms());
                } else if (element instanceof Challenge) {
                    xmppSession.send(new Response(saslClient.evaluateChallenge(((Challenge) element).getValue())));
                } else if (element instanceof Failure) {
                    authenticationFailure = (Failure) element;
                    authenticated = false;
                    releaseLock();
                    status = Status.FAILURE;
                } else if (element instanceof Success) {
                    authenticated = true;
                    releaseLock();
                    status = Status.SUCCESS;
                }
            }
        } catch (Exception e) {
            releaseLock();
            throw e;
        } finally {
            notifyFeatureNegotiated(status, element);
        }
        return status;
    }

    /**
     * Releases the lock after SASL authentication either failed or succeeded.
     */
    private void releaseLock() {
        lock.lock();
        try {
            authenticationComplete.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final boolean needsRestart() {
        return true;
    }

    @Override
    public final boolean canProcess(Object element) {
        return element instanceof Challenge || element instanceof Failure || element instanceof Success;
    }
}
