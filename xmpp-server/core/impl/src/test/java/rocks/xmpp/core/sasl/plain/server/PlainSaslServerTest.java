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

package rocks.xmpp.core.sasl.plain.server;

import java.nio.charset.StandardCharsets;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.sasl.server.CredentialValidationCallback;

/**
 * @author Christian Schudt
 */
public class PlainSaslServerTest {

    private static final CallbackHandler CALLBACK_HANDLER = callbacks -> {
        for (Callback callback : callbacks) {
            if (callback instanceof CredentialValidationCallback) {
                CredentialValidationCallback credentialValidationCallback = (CredentialValidationCallback) callback;
                if (credentialValidationCallback.getCredential() instanceof UsernamePasswordCredential) {
                    UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credentialValidationCallback.getCredential();
                    if ("admin".equals(usernamePasswordCredential.getCaller()) && "pwd".equals(usernamePasswordCredential.getPasswordAsString())) {
                        credentialValidationCallback.setCredentialValidationResult(new CredentialValidationResult("admin"));
                    } else {
                        credentialValidationCallback.setCredentialValidationResult(CredentialValidationResult.INVALID_RESULT);
                    }
                }
            }
        }
    };

    @Test(expectedExceptions = SaslException.class)
    public void testPlainSaslServerNoResponse() throws SaslException {
        SaslServer saslServer = new PlainSaslServer(CALLBACK_HANDLER);
        saslServer.evaluateResponse(new byte[0]);
        saslServer.evaluateResponse(new byte[0]);
    }

    @Test
    public void testPlainSaslServer() throws SaslException {
        SaslServer saslServer = new PlainSaslServer(CALLBACK_HANDLER);
        saslServer.evaluateResponse("\0admin\0pwd".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(saslServer.getAuthorizationID(), "admin");
    }

    @Test(expectedExceptions = SaslException.class)
    public void testPlainSaslServerMalFormed() throws SaslException {
        SaslServer saslServer = new PlainSaslServer(CALLBACK_HANDLER);
        saslServer.evaluateResponse("admin\0pwd".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(saslServer.getAuthorizationID(), "admin");
    }
}
