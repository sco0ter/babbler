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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.*;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

/**
 * The implementation of the SCRAM-SHA-1 SASL mechanism.
 *
 * @author Christian Schudt
 * @see <a href="http://tools.ietf.org/search/rfc5802">Salted Challenge Response Authentication Mechanism (SCRAM)</a>
 */
public class ScramSha1SaslClient implements SaslClient {

    private static final String HMAC_SHA1 = "HmacSHA1";

    private static final String GS2_CBIND_FLAG = "n";

    private static final byte[] INT1 = new byte[]{0, 0, 0, 1};

    private final CallbackHandler callbackHandler;

    private final String gs2Header;

    private String authorizationId;

    private char[] passwd;

    private String username;

    private String cnonce;

    private String clientFirstMessageBare;

    public ScramSha1SaslClient(String authorizationId, String protocol, String serverName, Map<String, ?> props, CallbackHandler callbackHandler) throws SaslException {
        // authzID can only be encoded in UTF8 - RFC 2222
        if (authorizationId != null) {
            this.authorizationId = authorizationId;
            try {
                authorizationId.getBytes("UTF8");
            } catch (UnsupportedEncodingException e) {
                throw new SaslException("SCRAM-SHA-1: Error encoding authzid value into UTF-8", e);
            }
        }
        this.gs2Header = GS2_CBIND_FLAG + "," + (authorizationId != null ? "a=" + authorizationId : "") + ",";
        this.callbackHandler = callbackHandler;
    }

    /**
     * Apply the exclusive-or operation to combine the octet string
     * on the left of this operator with the octet string on the right of
     * this operator.  The length of the output and each of the two
     * inputs will be the same for this use.
     *
     * @param a The first byte array.
     * @param b The second byte array.
     * @return The XOR combined byte array.
     */
    private static byte[] xor(byte[] a, byte[] b) {
        byte[] c = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }

    /**
     * Apply the HMAC keyed hash algorithm (defined in
     * [RFC2104]) using the octet string represented by "key" as the key
     * and the octet string "str" as the input string.  The size of the
     * result is the hash result size for the hash function in use.  For
     * example, it is 20 octets for SHA-1 (see [RFC3174]).
     *
     * @param key The key.
     * @param str The input.
     * @return The HMAC keyed hash.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static byte[] hmac(byte[] key, byte[] str) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA1);
        Key k = new SecretKeySpec(key, HMAC_SHA1);
        mac.init(k);
        mac.update(str);
        return mac.doFinal();
    }

    /**
     * Calculates the salted password.
     *
     * @param str  The input.
     * @param salt The salt.
     * @param i    The iteration count.
     * @return The salted password.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    static byte[] hi(byte[] str, byte[] salt, int i) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(HMAC_SHA1);
        hmac.init(new SecretKeySpec(str, HMAC_SHA1));

        hmac.update(salt);
        hmac.update(INT1);

        byte[] uPrev = hmac.doFinal();
        byte[] result = uPrev;

        for (int c = 1; c < i; c++) {
            hmac.update(uPrev);
            uPrev = hmac.doFinal();
            result = xor(result, uPrev);
        }

        return result;
    }

    /**
     * Apply the cryptographic hash function to the octet string
     * "str", producing an octet string as a result.  The size of the
     * result depends on the hash result size for the hash function in
     * use.
     *
     * @param str The byte array.
     */
    private static byte[] h(byte[] str) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(str);
        return digest.digest();
    }

    @Override
    public String getMechanismName() {
        return "SCRAM-SHA-1";
    }

    @Override
    public boolean hasInitialResponse() {
        // Nothing in SCRAM prevents either sending
        // the client-first message with the SASL authentication request defined
        // by an application protocol ("initial client response")
        return true;
    }

    @Override
    public byte[] evaluateChallenge(byte[] challenge) throws SaslException {

        // Initial response
        if (challenge.length == 0) {
            NameCallback ncb = authorizationId == null ?
                    new NameCallback("SCRAM-SHA-1 username: ") :
                    new NameCallback("SCRAM-SHA-1 username: ", authorizationId);
            PasswordCallback pcb = new PasswordCallback("SCRAM-SHA-1 password: ", false);

            try {
                callbackHandler.handle(new Callback[]{ncb, pcb});
                passwd = pcb.getPassword();
                pcb.clearPassword();
                username = ncb.getName();
            } catch (IOException e) {
                throw new SaslException("SCRAM-SHA-1: Error acquiring user name or password.", e);
            } catch (UnsupportedCallbackException e) {
                throw new SaslException("SCRAM-SHA-1: Cannot perform callback to acquire username or password", e);
            }

            cnonce = UUID.randomUUID().toString();
            clientFirstMessageBare = "n=" + username + ",r=" + cnonce;
            String clientFirstMessage = gs2Header + clientFirstMessageBare;
            return clientFirstMessage.getBytes();
        } else {

            // The server sends the salt and the iteration count to the client, which then computes
            // the following values and sends a ClientProof to the server

            String serverFirstMessage = new String(challenge);
            String[] parts = serverFirstMessage.split(",");
            String nonce = null;
            String saltBase64 = null;
            Integer iterationCount = null;
            for (String part : parts) {
                String[] pair = part.split("=");
                if (pair[0].equals("r")) {
                    nonce = pair[1];
                }
                if (pair[0].equals("s")) {
                    saltBase64 = pair[1];
                }
                if (pair[0].equals("i")) {
                    try {
                        iterationCount = Integer.parseInt(pair[1]);
                    } catch (NumberFormatException e) {
                        throw new SaslException("iterationCount could not be parsed.");
                    }
                }
            }
            if (nonce == null) {
                throw new SaslException("nonce was null in the server response.");
            }
            if (saltBase64 == null) {
                throw new SaslException("salt was null in the server response.");
            }
            if (iterationCount == null) {
                throw new SaslException("iterationCount was null in the server response.");
            }

            byte[] salt = DatatypeConverter.parseBase64Binary(saltBase64);

            try {
                // SaltedPassword  := Hi(Normalize(password), salt, i)
                byte[] saltedPassword = hi(new String(passwd).getBytes(), salt, iterationCount);
                // ClientKey       := HMAC(SaltedPassword, "Client Key")
                byte[] clientKey = hmac(saltedPassword, "Client Key".getBytes());
                // StoredKey       := H(ClientKey)
                byte[] storedKey = h(clientKey);
                // AuthMessage     := client-first-message-bare + "," +
                //                    server-first-message + "," +
                //                    client-final-message-without-proof
                String clientFinalMessageWithoutProof = "c=" + DatatypeConverter.printBase64Binary(gs2Header.getBytes()) + ",r=" + nonce;
                String authMessage = clientFirstMessageBare + "," + serverFirstMessage + "," + clientFinalMessageWithoutProof;
                // ClientSignature := HMAC(StoredKey, AuthMessage)
                byte[] clientSignature = hmac(storedKey, authMessage.getBytes());
                // ClientProof     := ClientKey XOR ClientSignature
                byte[] clientProof = xor(clientKey, clientSignature);

                // The client then responds by sending a "client-final-message" with the
                // same nonce and a ClientProof computed using the selected hash
                // function as explained earlier.
                String clientFinalMessage = clientFinalMessageWithoutProof + ",p=" + DatatypeConverter.printBase64Binary(clientProof);
                return clientFinalMessage.getBytes();

            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new SaslException(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
        return new byte[0];
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
        return new byte[0];
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        return null;
    }

    @Override
    public void dispose() throws SaslException {

    }
}
