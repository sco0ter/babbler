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
import javax.security.sasl.SaslException;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Christian Schudt
 */
public class ScramTest {

    @Test
    public void testHiFunction() throws InvalidKeyException, NoSuchAlgorithmException, SaslException {
        ScramClient scramSaslClient = new ScramClient("SHA-1", "server", new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName("Test");
                    }
                    if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword("pencil".toCharArray());
                    }
                }
            }
        });

        scramSaslClient.evaluateChallenge(new byte[0]);
        Assert.assertEquals(DatatypeConverter.printBase64Binary(scramSaslClient.hi("test".getBytes(), "salt".getBytes(), 4096)), "suIjHg0e14CDoom6wmHKz3naWOc=");
    }

    @Test
    public void testServerResponse() throws SaslException {

        ScramClient scramSha1SaslClient = new ScramClient("SHA-1", "server", new CallbackHandler() {
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

        Assert.assertTrue(new String(result).startsWith("c=biws,r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,p="));
    }

    @Test
    public void testStringPreparation() {
        String normalized = ScramBase.prepare(new String(new char[]{0x00AA}));
        Assert.assertEquals(normalized, "a");

        String normalized2 = ScramBase.prepare(new String(new char[]{0x2168}));
        Assert.assertEquals(normalized2, "IX");
    }

    @Test
    public void testReplacement() {
        String username = ",=,==";
        Assert.assertEquals("=2C=3D=2C=3D=3D", ScramClient.replaceUsername(username));
    }

    @Test
    public void testGetAttributes1() {
        String attributes = "n,,n==2C=3D=2C=3D=3D,r=fyko+d2lbbFgONRv9qkxdawL";
        Map<Character, String> map = ScramBase.getAttributes(attributes);
        Assert.assertEquals(map.size(), 2);
        Assert.assertEquals(map.get('n'), "=2C=3D=2C=3D=3D");
        Assert.assertEquals(map.get('r'), "fyko+d2lbbFgONRv9qkxdawL");
    }

    @Test
    public void testGetAttributes2() {
        String attributes = "r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,s=QSXCR+Q6sek8bf92,i=4096";
        Map<Character, String> map = ScramBase.getAttributes(attributes);
        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.get('r'), "fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j");
        Assert.assertEquals(map.get('s'), "QSXCR+Q6sek8bf92");
        Assert.assertEquals(map.get('i'), "4096");
    }

    @Test
    public void testGetAttributes3() {
        String attributes = "c=biws,r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,p=v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=";
        Map<Character, String> map = ScramBase.getAttributes(attributes);
        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.get('c'), "biws");
        Assert.assertEquals(map.get('r'), "fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j");
        Assert.assertEquals(map.get('p'), "v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=");
    }

    @Test
    public void testGetAttributes4() {
        String attributes = "v=rmF9pqV8S7suAoZWja4dJRkFsKQ=";
        Map<Character, String> map = ScramBase.getAttributes(attributes);
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.get('v'), "rmF9pqV8S7suAoZWja4dJRkFsKQ=");
    }

//    @Test
//    public void testClientServer() throws SaslException {
//        Security.addProvider(new SaslProvider());
//
//        SaslClient saslClient = Sasl.createSaslClient(new String[]{"SCRAM-SHA-1"}, "authzid", "xmpp", "servername", null, new CallbackHandler() {
//            @Override
//            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
//                for (Callback callback : callbacks) {
//                    if (callback instanceof NameCallback) {
//                        ((NameCallback) callback).setName("Test");
//                    }
//                    if (callback instanceof PasswordCallback) {
//                        ((PasswordCallback) callback).setPassword("==".toCharArray());
//                    }
//                }
//            }
//        });
//
//        byte[] initialResponse = saslClient.evaluateChallenge(new byte[0]);
//
//        ScramServer scramServer = new ScramServer("SHA-1", null, new CallbackHandler() {
//            @Override
//            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
//                for (Callback callback : callbacks) {
//                    if (callback instanceof PasswordCallback) {
//                        ((PasswordCallback) callback).setPassword("==".toCharArray());
//                    }
//                }
//            }
//        });
//
//        byte[] challenge = scramServer.evaluateResponse(initialResponse);
//
//        byte[] response = saslClient.evaluateChallenge(challenge);
//
//        byte[] serverFinalMessage = scramServer.evaluateResponse(response);
//        Assert.assertTrue(new String(serverFinalMessage).startsWith("v="));
//    }
//
//    @Test(expectedExceptions = SaslException.class)
//    public void testScramServerInvalidUsername() throws SaslException {
//        ScramServer.validateAndGetUsername("=dw");
//    }
//
//    @Test
//    public void testScramServerValidUsername() throws SaslException {
//        Assert.assertEquals(ScramServer.validateAndGetUsername("test=2C"), "test,");
//        Assert.assertEquals(ScramServer.validateAndGetUsername("aaa=3D"), "aaa=");
//        Assert.assertEquals(ScramServer.validateAndGetUsername("aaa=3D=2C=2C=3D"), "aaa=,,=");
//    }
}
