package org.xmpp.extension.rpc;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Connection;
import org.xmpp.MockServer;
import org.xmpp.TestConnection;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.stanza.Stanza;
import org.xmpp.stanza.StanzaException;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class RpcManagerTest extends BaseTest {

    @Test
    public void testServiceDiscoveryEntry() {
        TestConnection connection1 = new TestConnection();
        RpcManager rpcManager = connection1.getExtensionManager(RpcManager.class);
        Assert.assertFalse(rpcManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("jabber:iq:rpc");
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        rpcManager.setEnabled(true);
        Assert.assertTrue(rpcManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
    }

    @Test
    public void testCall() throws StanzaException, TimeoutException, RpcException {
        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        Connection connection2 = new TestConnection(JULIET, mockServer);

        RpcManager rpcManager = connection1.getExtensionManager(RpcManager.class);
        rpcManager.setEnabled(true);
        rpcManager.setRpcHandler(new RpcHandler() {
            @Override
            public Value process(String methodName, List<Value> parameters) throws StanzaException, RpcException {
                if (methodName.equals("square")) {
                    return new Value(parameters.get(0).getAsInteger() * parameters.get(0).getAsInteger());
                }
                return null;
            }
        });

        Value result = connection2.getExtensionManager(RpcManager.class).call(ROMEO, "square", new Value(2));
        Assert.assertEquals(result.getAsInteger().intValue(), 4);
    }


    @Test
    public void testRpcException() throws StanzaException, TimeoutException {
        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        Connection connection2 = new TestConnection(JULIET, mockServer);

        RpcManager rpcManager = connection1.getExtensionManager(RpcManager.class);
        rpcManager.setEnabled(true);
        rpcManager.setRpcHandler(new RpcHandler() {
            @Override
            public Value process(String methodName, List<Value> parameters) throws StanzaException, RpcException {
                if (methodName.equals("fault")) {
                    throw new RpcException(2, "faulty");
                }
                return null;
            }
        });

        try {
            connection2.getExtensionManager(RpcManager.class).call(ROMEO, "fault", new Value(2));
        } catch (RpcException e) {
            Assert.assertEquals(e.getFaultCode(), 2);
            Assert.assertEquals(e.getFaultString(), "faulty");
            return;
        }
        Assert.fail("RpcException expected.");
    }

    @Test
    public void testStanzaException() throws RpcException, TimeoutException {
        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        Connection connection2 = new TestConnection(JULIET, mockServer);

        RpcManager rpcManager = connection1.getExtensionManager(RpcManager.class);
        rpcManager.setEnabled(true);
        rpcManager.setRpcHandler(new RpcHandler() {
            @Override
            public Value process(String methodName, List<Value> parameters) throws StanzaException, RpcException {
                if (methodName.equals("fault")) {
                    throw new StanzaException(new Stanza.Error(new Stanza.Error.Forbidden()));
                }
                return null;
            }
        });

        try {
            connection2.getExtensionManager(RpcManager.class).call(ROMEO, "fault", new Value(2));
        } catch (StanzaException e) {
            Assert.assertTrue(e.getError().getCondition() instanceof Stanza.Error.Forbidden);
            return;
        }
        Assert.fail("StanzaException expected.");
    }
}
