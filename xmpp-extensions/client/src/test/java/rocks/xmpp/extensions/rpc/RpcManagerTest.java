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

package rocks.xmpp.extensions.rpc;

import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.rpc.client.ClientRpcManager;
import rocks.xmpp.extensions.rpc.model.Value;

/**
 * Tests for {@link ClientRpcManager}.
 */
public class RpcManagerTest extends BaseTest {

    @Test
    public void testServiceDiscoveryEntry() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());
        ClientRpcManager rpcManager = xmppSession.getManager(ClientRpcManager.class);
        Assert.assertFalse(rpcManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        String feature = "jabber:iq:rpc";
        Assert.assertFalse(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures().contains(feature));
        rpcManager.setRpcHandler((requester, methodName, parameters) -> null);
        Assert.assertTrue(rpcManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures().contains(feature));
    }

    @Test
    public void testCall() throws ExecutionException, InterruptedException {
        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        RpcManager rpcManager = xmppSession1.getManager(RpcManager.class);
        rpcManager.setRpcHandler((requester, methodName, parameters) -> {
            if (methodName.equals("square")) {
                return Value.of(parameters.get(0).getAsInteger() * parameters.get(0).getAsInteger());
            }
            return null;
        });

        Value result = xmppSession2.getManager(RpcManager.class).call(ROMEO, "square", Value.of(2)).get();
        Assert.assertEquals(result.getAsInteger().intValue(), 4);
    }

    @Test
    public void testRpcException() {
        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        RpcManager rpcManager = xmppSession1.getManager(RpcManager.class);

        rpcManager.setRpcHandler((requester, methodName, parameters) -> {
            if (methodName.equals("fault")) {
                throw new RpcException(2, "faulty");
            }
            return null;
        });

        try {
            xmppSession2.getManager(RpcManager.class).call(ROMEO, "fault", Value.of(2)).get();
        } catch (Exception e) {
            Assert.assertEquals(((RpcException) e.getCause()).getFaultCode(), 2);
            Assert.assertEquals(((RpcException) e.getCause()).getFaultString(), "faulty");
            return;
        }
        Assert.fail("RpcException expected.");
    }

    @Test
    public void testStanzaException() {
        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        RpcManager rpcManager = xmppSession1.getManager(RpcManager.class);
        rpcManager.setRpcHandler((requester, methodName, parameters) -> {
            if (methodName.equals("fault")) {
                IQ iq = new IQ(IQ.Type.ERROR, null);
                iq.setError(new StanzaError(Condition.FORBIDDEN));
                throw new StanzaErrorException(iq);
            }
            return null;
        });

        try {
            xmppSession2.getManager(RpcManager.class).call(ROMEO, "fault", Value.of(2)).getResult();
        } catch (StanzaErrorException e) {
            Assert.assertSame(e.getStanza().getError().getCondition(), Condition.FORBIDDEN);
            return;
        } catch (XmppException e) {
            Assert.fail("StanzaErrorException expected.");
        }
        Assert.fail("StanzaErrorException expected.");
    }
}
