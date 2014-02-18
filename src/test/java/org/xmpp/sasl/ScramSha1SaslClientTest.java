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

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.security.auth.callback.*;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Christian Schudt
 */
public class ScramSha1SaslClientTest {

    @Test
    public void test() throws InvalidKeyException, NoSuchAlgorithmException, SaslException {
        ScramSha1SaslClient scramSha1SaslClient = new ScramSha1SaslClient(null, "xmpp", "server", null, new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

            }
        });

        scramSha1SaslClient.evaluateChallenge(new byte[0]);
        Assert.assertEquals(DatatypeConverter.printBase64Binary(ScramSha1SaslClient.hi("test".getBytes(), "salt".getBytes(), 4096)), "suIjHg0e14CDoom6wmHKz3naWOc=");
    }

    @Test
    public void test2() throws SaslException {
//        SaslClient sasl = Sasl.createSaslClient(new String[]{"DIGEST-MD5"}, "frerf", "xmpp", "test", null, new CallbackHandler() {
//            @Override
//            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
//                int i = 0;
//            }
//        });
//        sasl.evaluateChallenge(new byte[0]);
    }

    @Test
    public void test3() throws SaslException {

        ScramSha1SaslClient scramSha1SaslClient = new ScramSha1SaslClient(null, "xmpp", "server", null, new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName("user");
                    }
                    if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword("pencil".toCharArray());
                    }
                }
            }
        });
        String serverResponse = "r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,s=QSXCR+Q6sek8bf92,i=4096";

        scramSha1SaslClient.evaluateChallenge(new byte[0]);
        byte[] result = scramSha1SaslClient.evaluateChallenge(serverResponse.getBytes());

        System.out.print(new String(result));
    }

}
