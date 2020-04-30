/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

import rocks.xmpp.core.sasl.scram.ScramClient;
import rocks.xmpp.im.roster.server.spi.ScramCredential;
import rocks.xmpp.im.roster.server.spi.ScramIdentityStore;

import javax.enterprise.context.ApplicationScoped;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory identity store for testing and illustration purposes.
 * <p>
 * It validates the user "admin/admin".
 *
 * @author Christian Schudt
 */
@ApplicationScoped
public class InMemoryIdentityStore implements ScramIdentityStore {

    private static final Random RANDOM = new SecureRandom();

    private final Map<String, CredentialInfo> credentialInfoMap = new ConcurrentHashMap<>();

    public InMemoryIdentityStore() {
        credentialInfoMap.put("admin", createAccount("admin", "admin"));
        credentialInfoMap.put("111", createAccount("111", "111"));
        credentialInfoMap.put("222", createAccount("222", "222"));
    }

    private static CredentialInfo createAccount(String username, String password) {
        // TODO clean this up
        byte[] salt = new byte[32];
        RANDOM.nextBytes(salt);
        int iterationCount = 4096;
        ScramClient scramClient = new ScramClient("SHA-1", null, callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.toCharArray());
                }
            }
        });
        try {
            byte[] saltedPassword = scramClient.computeSaltedPassword(password.toCharArray(), salt, iterationCount);
            byte[] clientKey = scramClient.computeClientKey(saltedPassword);
            byte[] storedKey = scramClient.computeStoredKey(clientKey);
            byte[] serverKey = scramClient.computeServerKey(saltedPassword);
            return new CredentialInfo(username, iterationCount, salt, storedKey, serverKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return EnumSet.of(ValidationType.VALIDATE);
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;
            CredentialInfo credentialInfo = credentialInfoMap.get(usernamePasswordCredential.getCaller());

            ScramClient scramClient = new ScramClient("SHA-1", null, callbacks -> {
                for (Callback callback : callbacks) {
                    if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(usernamePasswordCredential.getPassword().getValue());
                    }
                }
            });
            try {
                byte[] saltedPassword = scramClient.computeSaltedPassword(usernamePasswordCredential.getPassword().getValue(), credentialInfo.salt, credentialInfo.iterationCount);
                byte[] clientKey = scramClient.computeClientKey(saltedPassword);
                byte[] storedKey = scramClient.computeStoredKey(clientKey);
                if (Arrays.equals(credentialInfo.storedKey, storedKey)) {
                    return new CredentialValidationResult(usernamePasswordCredential.getCaller());
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }

            return CredentialValidationResult.INVALID_RESULT;
        }
        return CredentialValidationResult.NOT_VALIDATED_RESULT;
    }

    @Override
    public ScramCredential getScramCredential(String username) {
        CredentialInfo credentialInfo = credentialInfoMap.get(username);
        if (credentialInfo != null) {
            return new ScramCredential(credentialInfo.username, credentialInfo.iterationCount, credentialInfo.salt, credentialInfo.storedKey, credentialInfo.serverKey);
        }
        return null;
    }

    private static final class CredentialInfo {

        private String username;

        private int iterationCount;

        private byte[] salt;

        private byte[] storedKey;

        private byte[] serverKey;

        private CredentialInfo(String username, int iterationCount, byte[] salt, byte[] storedKey, byte[] serverKey) {
            this.username = username;
            this.iterationCount = iterationCount;
            this.salt = salt;
            this.storedKey = storedKey;
            this.serverKey = serverKey;
        }
    }
}
