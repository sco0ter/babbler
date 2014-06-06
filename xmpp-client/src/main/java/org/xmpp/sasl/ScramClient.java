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

import javax.security.auth.callback.*;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * The implementation of the SCRAM-SHA-1 SASL mechanism.
 *
 * @author Christian Schudt
 * @see <a href="http://tools.ietf.org/search/rfc5802">Salted Challenge Response Authentication Mechanism (SCRAM)</a>
 */
final class ScramClient extends ScramBase implements SaslClient {

    private static final String GS2_CBIND_FLAG = "n";

    private final String gs2Header;

    private String authorizationId;

    private char[] passwd;

    public ScramClient(String hashAlgorithm, String authorizationId, CallbackHandler callbackHandler) throws SaslException {
        super(hashAlgorithm, callbackHandler);

        // authzID can only be encoded in UTF8 - RFC 2222
        if (authorizationId != null) {
            this.authorizationId = authorizationId;
            try {
                authorizationId.getBytes("UTF8");
            } catch (UnsupportedEncodingException e) {
                throw new SaslException("SCRAM: Error encoding authzid value into UTF-8", e);
            }
        }
        this.gs2Header = GS2_CBIND_FLAG + "," + (authorizationId != null ? "a=" + authorizationId : "") + ",";
    }

    /**
     * The characters ',' or '=' in usernames are sent as '=2C' and
     * '=3D' respectively.
     *
     * @param username The username.
     * @return The replaced username.
     */
    static String replaceUsername(String username) {
        if (username != null) {
            return username.replace("=", "=3D").replace(",", "=2C");
        }
        return null;
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
                    new NameCallback("SCRAM username: ") :
                    new NameCallback("SCRAM username: ", authorizationId);
            PasswordCallback pcb = new PasswordCallback("SCRAM-SHA-1 password: ", false);

            String username;
            try {
                callbackHandler.handle(new Callback[]{ncb, pcb});
                passwd = pcb.getPassword();
                pcb.clearPassword();
                username = ncb.getName();
                if (passwd == null || username == null) {
                    throw new SaslException("SCRAM: Username and password must not be null.");
                }

                // Before sending the username to the server, the client SHOULD
                // prepare the username using the "SASLprep" profile [RFC4013] of
                // the "stringprep" algorithm [RFC3454] treating it as a query
                // string (i.e., unassigned Unicode code points are allowed).
                username = prepare(username);

                // If the preparation of the username fails or results in an empty
                // string, the client SHOULD abort the authentication exchange.
                if ("".equals(username)) {
                    throw new SaslException("SCRAM: Username must not be empty.");
                }
                username = replaceUsername(username);

            } catch (IOException e) {
                throw new SaslException("SCRAM: Error acquiring user name or password.", e);
            } catch (UnsupportedCallbackException e) {
                throw new SaslException("SCRAM: Cannot perform callback to acquire username or password", e);
            }

            String cnonce;
            try {
                cnonce = generateNonce();
            } catch (NoSuchAlgorithmException e) {
                throw new SaslException("SCRAM: Failed to generate nonce.", e);
            }
            clientFirstMessageBare = createClientFirstMessageBare(username, cnonce);
            // First, the client sends the "client-first-message"
            String clientFirstMessage = gs2Header + clientFirstMessageBare;
            return clientFirstMessage.getBytes();
        } else {

            // The server sends the salt and the iteration count to the client, which then computes
            // the following values and sends a ClientProof to the server

            serverFirstMessage = new String(challenge);
            Map<Character, String> attributes = getAttributes(serverFirstMessage);
            nonce = attributes.get('r');

            String saltBase64 = attributes.get('s');
            Integer iterationCount;
            try {
                iterationCount = Integer.parseInt(attributes.get('i'));
            } catch (NumberFormatException e) {
                throw new SaslException("iterationCount could not be parsed.");
            }

            if (nonce == null) {
                throw new SaslException("SCRAM: nonce was null in the server response.");
            }
            if (saltBase64 == null) {
                throw new SaslException("SCRAM: salt was null in the server response.");
            }

            byte[] salt = DatatypeConverter.parseBase64Binary(saltBase64);

            try {
                channelBinding = DatatypeConverter.printBase64Binary(gs2Header.getBytes());
                byte[] clientKey = computeClientKey(computeSaltedPassword(passwd, salt, iterationCount));
                byte[] clientSignature = computeClientSignature(clientKey, computeAuthMessage());
                // ClientProof     := ClientKey XOR ClientSignature
                byte[] clientProof = xor(clientKey, clientSignature);
                clientFinalMessageWithoutProof = "c=" + channelBinding + ",r=" + nonce;
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
        return isComplete;
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
