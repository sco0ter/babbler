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

package org.xmpp.sasl;

import org.xmpp.Connection;
import org.xmpp.stream.FeatureNegotiator;

import javax.security.auth.callback.*;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Manages SASL authentication as described in <a href="http://xmpp.org/rfcs/rfc6120.html#sasl">SASL Negotiation</a>.
 *
 * @author Christian Schudt
 */
public final class AuthenticationManager extends FeatureNegotiator {

    static {
        // The SunSASL Provider only supports: "PLAIN", "CRAM-MD5", "DIGEST-MD5", "GSSAPI", "EXTERNAL".
        // http://download.java.net/jdk8/docs/technotes/guides/security/sasl/sasl-refguide.html

        // Add the "ANONYMOUS" SASL mechanism.
        Security.addProvider(new AnonymousSaslProvider());
    }

    private final Connection connection;

    /**
     * The condition, which is triggered, when the authentication either failed or succeeded.
     */
    private final Condition authenticationComplete;

    /**
     * Stores the supported and preferred SASL mechanisms of the server.
     */
    private final LinkedHashSet<String> supportedMechanisms;

    /**
     * The lock, which used to create new waiting conditions.
     */
    private final Lock lock;

    /**
     * Stores the preferred SASL mechanisms of the client.
     */
    private LinkedHashSet<String> preferredMechanisms;

    /**
     * The SASL client which is used during an authentication process.
     */
    private SaslClient saslClient;

    /**
     * If the authentication failed, this variable is set (by the reader thread) and later read by the application thread.
     */
    private volatile Failure authenticationFailure = null;

    /**
     * Set to true if the authentication succeeded; or false, if it failed or not yet completed.
     */
    private volatile boolean authenticated;

    private String lastPassword;

    private String lastUsername;

    private String lastAuthorizationId;

    private String[] lastMechanisms;

    private CallbackHandler lastCallbackHandler;

    /**
     * Creates the authentication manager. Usually only the {@link Connection} should create it implicitly.
     *
     * @param connection The connection.
     * @param lock       The lock object, which is used to make the current thread wait during authentication.
     */
    public AuthenticationManager(final Connection connection, Lock lock) {
        super(Mechanisms.class);

        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null.");
        }

        this.connection = connection;
        this.lock = lock;
        this.authenticationComplete = lock.newCondition();
        this.supportedMechanisms = new LinkedHashSet<>();
        this.preferredMechanisms = new LinkedHashSet<>();

        // Add default preferred SASL mechanisms.
        preferredMechanisms.add("SCRAM-SHA-1"); // TODO: implement SaslClient for this.
        preferredMechanisms.add("DIGEST-MD5");
        preferredMechanisms.add("GSSAPI");
        preferredMechanisms.add("CRAM-MD5");
        preferredMechanisms.add("PLAIN");
        preferredMechanisms.add("ANONYMOUS");
    }

    /**
     * Gets the preferred mechanisms used for this connection.
     *
     * @return The preferred mechanisms.
     * @see #setPreferredMechanisms(java.util.LinkedHashSet)
     */
    public LinkedHashSet<String> getPreferredMechanisms() {
        return preferredMechanisms;
    }

    /**
     * Sets the preferred mechanisms used for this connection.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#sasl-rules-preferences">6.3.3.  Mechanism Preferences</a></cite></p>
     * <p>Any entity that will act as a SASL client or a SASL server MUST maintain an ordered list of its preferred SASL mechanisms according to the client or server, where the list is ordered according to local policy or user configuration (which SHOULD be in order of perceived strength to enable the strongest authentication possible). The initiating entity MUST maintain its own preference order independent of the preference order of the receiving entity. A client MUST try SASL mechanisms in its preference order. For example, if the server offers the ordered list "PLAIN SCRAM-SHA-1 GSSAPI" or "SCRAM-SHA-1 GSSAPI PLAIN" but the client's ordered list is "GSSAPI SCRAM-SHA-1", the client MUST try GSSAPI first and then SCRAM-SHA-1 but MUST NOT try PLAIN (since PLAIN is not on its list).</p>
     * </blockquote>
     *
     * @param preferredMechanisms The preferred mechanisms.
     * @see #getPreferredMechanisms()
     */
    public void setPreferredMechanisms(LinkedHashSet<String> preferredMechanisms) {
        this.preferredMechanisms = preferredMechanisms;
    }

    /**
     * Authenticates a user by choosing the "best" SASL mechanism available to both client and server.
     *
     * @param authorizationId The authorization identity.
     *                        <blockquote>
     *                        <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#sasl-rules-authzid">6.3.8.  Authorization Identity</a></cite></p>
     *                        <p>An authorization identity is an OPTIONAL identity included by the initiating entity to specify an identity to act as (see Section 2 of [SASL]). In client-to-server streams, it would most likely be used by an administrator to perform some management task on behalf of another user, whereas in server-to-server streams it would most likely be used to specify a particular add-on service at an XMPP service (e.g., a multi-user chat server at conference.example.com that is hosted by the example.com XMPP service). If the initiating entity wishes to act on behalf of another entity and the selected SASL mechanism supports transmission of an authorization identity, the initiating entity MUST provide an authorization identity during SASL negotiation. If the initiating entity does not wish to act on behalf of another entity, it MUST NOT provide an authorization identity.</p>
     *                        <p>In the case of client-to-server communication, the value of an authorization identity MUST be a bare JID ({@code <localpart@domainpart>}) rather than a full JID ({@code <localpart@domainpart/resourcepart>}).</p>
     *                        </blockquote>
     * @param user            The user.
     *                        <blockquote>
     *                        <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#sasl-rules-username">6.3.7.  Simple User Name</a></cite></p>
     *                        <p>Some SASL mechanisms (e.g., CRAM-MD5, DIGEST-MD5, and SCRAM) specify that the authentication identity used in the context of such mechanisms is a "simple user name" (see Section 2 of [SASL] as well as [SASLPREP]). The exact form of the simple user name in any particular mechanism or deployment thereof is a local matter, and a simple user name does not necessarily map to an application identifier such as a JID or JID component (e.g., a localpart). However, in the absence of local information provided by the server, an XMPP client SHOULD assume that the authentication identity for such a SASL mechanism is a simple user name equal to the localpart of the user's JID.</p>
     *                        </blockquote>
     * @param password        The user's password.
     * @param callbackHandler An optional callback handler. Should be null in most cases.
     * @throws SaslException              If a {@link SaslClient} could not be created.
     * @throws LoginException             If the login failed, due to a SASL error reported by the server.
     * @throws FailedLoginException       If the login failed, due to a wrong username or password. It is thrown if the server reports a {@code <not-authorized/>} SASL error.
     * @throws AccountLockedException     If the login failed, because the account has been disabled.  It is thrown if the server reports a {@code <account-disabled/>} SASL error.
     * @throws CredentialExpiredException If the login failed, because the credentials have expired. It is thrown if the server reports a {@code <credentials-expired/>} SASL error.
     */
    public void authenticate(String authorizationId, String user, String password, CallbackHandler callbackHandler) throws LoginException, SaslException {
        authenticate(getCommonMechanisms(), authorizationId, user, password, callbackHandler);
    }

    /**
     * Authenticates anonymously, if the server supports anonymous authentication.
     *
     * @throws LoginException If the anonymous login failed. See {@link #authenticate(String, String, String, javax.security.auth.callback.CallbackHandler)} for more a detailed description.
     */
    public void authenticateAnonymously() throws LoginException {
        try {
            authenticate(new String[]{"ANONYMOUS"}, null, null, null, null);
        } catch (SaslException e) {
            // Should actually never happen, because the AnonymousSaslClient does not throw an exception.
            throw new LoginException(e.getMessage());
        }
    }

    /**
     * @param mechanisms      The mechanisms to use.
     * @param authorizationId The authorization identity.
     * @param user            The user.
     * @param password        The password.
     * @param callbackHandler The callback handler.
     * @throws SaslException              If a {@link SaslClient} could not be created.
     * @throws LoginException             If the login failed, due to a SASL error reported by the server.
     * @throws FailedLoginException       If the login failed, due to a wrong username or password. It is thrown if the server reports a {@code <not-authorized/>} SASL error.
     * @throws AccountLockedException     If the login failed, because the account has been disabled.  It is thrown if the server reports a {@code <account-disabled/>} SASL error.
     * @throws CredentialExpiredException If the login failed, because the credentials have expired. It is thrown if the server reports a {@code <credentials-expired/>} SASL error.
     * @see #authenticate(String, String, String, javax.security.auth.callback.CallbackHandler)
     */
    private void authenticate(String[] mechanisms, String authorizationId, String user, String password, CallbackHandler callbackHandler) throws SaslException, LoginException {
        if (connection.getStatus() != Connection.Status.CONNECTED) {
            throw new IllegalStateException("You must be connected to the server before trying to authenticate.");
        }
        // Reset variables.
        authenticationFailure = null;
        authenticated = false;

        lastMechanisms = mechanisms;
        lastAuthorizationId = authorizationId;
        lastUsername = user;
        lastPassword = password;
        lastCallbackHandler = callbackHandler;

        saslClient = createSaslClient(mechanisms, authorizationId, user, password, callbackHandler);

        if (saslClient == null) {
            throw new SaslException("No SASL client found.");
        }

        byte[] initialResponse = new byte[0];
        if (saslClient.hasInitialResponse()) {
            initialResponse = saslClient.evaluateChallenge(new byte[0]);
        }

        connection.send(new Auth(saslClient.getMechanismName(), initialResponse));

        // Wait until the authentication succeeded or failed, but max. 10 seconds.
        lock.lock();
        try {
            authenticationComplete.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }

        // At this point we should be authenticated. If not, throw an exception.
        if (!authenticated) {
            if (authenticationFailure != null) {
                String failureText;
                if (authenticationFailure.getText() != null) {
                    failureText = saslClient.getMechanismName() + " authentication failed: " + authenticationFailure.getText();
                } else {
                    failureText = saslClient.getMechanismName() + " authentication failed.";
                }
                if (authenticationFailure.getCondition() instanceof Failure.NotAuthorized) {
                    throw new FailedLoginException(failureText);
                } else if (authenticationFailure.getCondition() instanceof Failure.AccountDisabled) {
                    throw new AccountLockedException(failureText);
                } else if (authenticationFailure.getCondition() instanceof Failure.CredentialsExpired) {
                    throw new CredentialExpiredException(failureText);
                } else {
                    throw new LoginException(saslClient.getMechanismName() + " authentication failed with condition: " + authenticationFailure.getCondition().getClass().getSimpleName());
                }
            } else {
                throw new LoginException(saslClient.getMechanismName() + " authentication failed for an unknown reason, but probably due to timeout.");
            }
        }
    }

    /**
     * Re-authenticates after a connection has disconnected and reconnected. The parameters from the last authentication process is used to re-authenticate.
     *
     * @throws SaslException  If the SASL mechanism could not be created.
     * @throws LoginException If the login failed.
     */
    public void reAuthenticate() throws SaslException, LoginException {
        authenticate(lastMechanisms, lastAuthorizationId, lastUsername, lastPassword, lastCallbackHandler);
    }

    /**
     * Gets the preferred mechanisms, which are also supported by the server.
     *
     * @return The common mechanisms.
     */
    private String[] getCommonMechanisms() {
        LinkedHashSet<String> mechanisms = new LinkedHashSet<>();
        for (String preferredMechanism : preferredMechanisms) {
            if (supportedMechanisms.contains(preferredMechanism)) {
                mechanisms.add(preferredMechanism);
            }
        }
        return mechanisms.toArray(new String[mechanisms.size()]);
    }

    @Override
    public Status processNegotiation(Object element) throws Exception {
        Status status = Status.INCOMPLETE;
        try {
            if (element instanceof Mechanisms) {
                supportedMechanisms.clear();
                supportedMechanisms.addAll(((Mechanisms) element).getMechanisms());
            } else if (element instanceof Challenge) {
                sendResponse((Challenge) element);
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
            authenticationComplete.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean needsRestart() {
        return true;
    }

    @Override
    public boolean canProcess(Object element) {
        return element instanceof Challenge || element instanceof Failure || element instanceof Success;
    }

    /**
     * Creates the SASL client which is used during the authentication process.
     *
     * @param authorizationId The authorization identity.
     * @param user            The user.
     * @param password        The password.
     * @param callbackHandler The optional callback handler. May be null.
     * @throws SaslException
     */
    private SaslClient createSaslClient(String[] mechanisms, String authorizationId, final String user, final String password, CallbackHandler callbackHandler) throws SaslException {

        // If the callbackHandler is null, assign a default callback handler.
        if (callbackHandler == null) {
            callbackHandler = new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (Callback callback : callbacks) {
                        if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(user);
                        }
                        if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword(password.toCharArray());
                        }
                        if (callback instanceof RealmCallback) {
                            ((RealmCallback) callback).setText(((RealmCallback) callback).getDefaultText());
                        }
                    }
                }
            };
        }

        return Sasl.createSaslClient(mechanisms, authorizationId, "xmpp", connection.getDomain(), new HashMap<String, Object>(), callbackHandler);
    }

    /**
     * Sends a response to a challenge.
     *
     * @param challenge The challenge.
     * @throws SaslException
     * @see Challenge
     * @see Response
     */
    private void sendResponse(Challenge challenge) throws SaslException {
        byte[] responseArray = saslClient.evaluateChallenge(challenge.getValue());
        Response response = new Response(responseArray);
        connection.send(response);
    }
}
