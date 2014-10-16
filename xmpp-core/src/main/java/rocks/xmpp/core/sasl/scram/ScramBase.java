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

package rocks.xmpp.core.sasl.scram;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslException;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A base SCRAM implementation.
 *
 * @author Christian Schudt
 */
abstract class ScramBase {

    private static final byte[] INT1 = new byte[]{0, 0, 0, 1};

    private static final byte[] CLIENT_KEY = "Client Key".getBytes();

    protected final CallbackHandler callbackHandler;

    private final String hmacAlgorithm;

    private final String hashAlgorithm;

    protected boolean isComplete;

    protected String clientFirstMessageBare;

    protected String serverFirstMessage;

    protected String nonce;

    protected String channelBinding;

    private String mechanism;

    public ScramBase(String hashAlgorithm, CallbackHandler callbackHandler) throws SaslException {
        mechanism = "SCRAM-";
        hashAlgorithm = hashAlgorithm.toUpperCase();

        if ("SHA-1".equals(hashAlgorithm)) {
            hmacAlgorithm = "HmacSHA1";
        } else {
            throw new UnsupportedOperationException("Hash algorithm not supported.");
        }
        mechanism += hashAlgorithm;
        this.hashAlgorithm = hashAlgorithm;
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
    public static byte[] xor(byte[] a, byte[] b) {
        byte[] c = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }

    /**
     * Generates a nonce.
     *
     * @return The nonce.
     * @throws NoSuchAlgorithmException If the generation algorithm does not exist.
     */
    public static String generateNonce() throws NoSuchAlgorithmException {
        byte[] nonce = new byte[16];
        Random rand = SecureRandom.getInstance("SHA1PRNG");
        rand.nextBytes(nonce);
        return DatatypeConverter.printBase64Binary(nonce);
    }

    /**
     * Gets the attributes from a SCRAM string.
     *
     * @param str The string.
     * @return The attributes.
     */
    public static Map<Character, String> getAttributes(String str) {
        Map<Character, String> map = new HashMap<Character, String>();
        String[] parts = str.split(",");
        for (String part : parts) {
            if (part.length() > 1) {
                map.put(part.charAt(0), part.substring(2));
            }
        }
        return map;
    }

    /**
     * Creates the client-first-message-bare.
     *
     * @param username The user name.
     * @param nonce    The nonce.
     * @return The client-first-message-bare.
     */
    public static String createClientFirstMessageBare(String username, String nonce) {
        return "n=" + username + ",r=" + nonce;
    }

    /**
     * Computes the client signature.
     *
     * @param clientKey   The client key.
     * @param authMessage The auth message.
     * @return The client signature.
     * @throws InvalidKeyException      If the key is invalid.
     * @throws NoSuchAlgorithmException If the mac algorithm does not exist.
     */
    public byte[] computeClientSignature(byte[] clientKey, String authMessage) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] storedKey = computeStoredKey(clientKey);
        // ClientSignature := HMAC(StoredKey, AuthMessage)
        return hmac(storedKey, authMessage.getBytes());
    }

    /**
     * Computes the auth message.
     *
     * @return The auth message.
     */
    public String computeAuthMessage() {
        // AuthMessage     := client-first-message-bare + "," +
        //                    server-first-message + "," +
        //                    client-final-message-without-proof
        String clientFinalMessageWithoutProof = "c=" + channelBinding + ",r=" + nonce;
        return clientFirstMessageBare + "," + serverFirstMessage + "," + clientFinalMessageWithoutProof;
    }

    /**
     * Computes the salted password.
     *
     * @param password       The password.
     * @param salt           The salt.
     * @param iterationCount The iteration count.
     * @return The salted password.
     * @throws InvalidKeyException      If the key is invalid.
     * @throws NoSuchAlgorithmException If the hash algorithm does not exist.
     */
    public byte[] computeSaltedPassword(char[] password, byte[] salt, int iterationCount) throws InvalidKeyException, NoSuchAlgorithmException {
        // SaltedPassword  := Hi(Normalize(password), salt, i)
        return hi(SaslPrep.prepare(new String(password)).getBytes(), salt, iterationCount);
    }

    /**
     * Computes the client key.
     *
     * @param saltedPassword The salted password.
     * @return The client key.
     * @throws InvalidKeyException      If the key is invalid.
     * @throws NoSuchAlgorithmException If the mac algorithm does not exist.
     */
    public byte[] computeClientKey(byte[] saltedPassword) throws InvalidKeyException, NoSuchAlgorithmException {
        // ClientKey       := HMAC(SaltedPassword, "Client Key")
        return hmac(saltedPassword, CLIENT_KEY);
    }

    /**
     * Computes the stored key.
     *
     * @param clientKey The client key.
     * @return The stored key.
     * @throws NoSuchAlgorithmException If the hash algorithm does not exist.
     */
    public byte[] computeStoredKey(byte[] clientKey) throws NoSuchAlgorithmException {
        // StoredKey       := H(ClientKey)
        return h(clientKey);
    }

    /**
     * Apply the cryptographic hash function to the octet string
     * "str", producing an octet string as a result.  The size of the
     * result depends on the hash result size for the hash function in
     * use.
     *
     * @param str The byte array.
     * @return The hash value.
     * @throws NoSuchAlgorithmException If the hash algorithm does not exist.
     */
    public byte[] h(byte[] str) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
        digest.update(str);
        return digest.digest();
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
     * @throws NoSuchAlgorithmException If the MAC algorithm does not exist.
     * @throws InvalidKeyException      If the key does not exist.
     */
    public byte[] hmac(byte[] key, byte[] str) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(hmacAlgorithm);
        mac.init(new SecretKeySpec(key, hmacAlgorithm));
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
     * @throws InvalidKeyException      If the key is invalid.
     * @throws NoSuchAlgorithmException If the mac algorithm does not exist.
     */
    public byte[] hi(byte[] str, byte[] salt, int i) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(hmacAlgorithm);
        hmac.init(new SecretKeySpec(str, hmacAlgorithm));

        // U1   := HMAC(str, salt + INT(1))
        // U2   := HMAC(str, U1)
        // ...
        // Ui-1 := HMAC(str, Ui-2)
        // Ui   := HMAC(str, Ui-1)
        //
        // Hi := U1 XOR U2 XOR ... XOR Ui
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
     * Gets the mechanism name, i.e "SCRAM-" + the hash algorithm name.
     *
     * @return The mechanism name.
     */
    public String getMechanismName() {
        return mechanism;
    }
}

