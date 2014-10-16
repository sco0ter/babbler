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

package rocks.xmpp.extensions.disco;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;

/**
 * @author Christian Schudt
 */
public class ServiceDiscoveryManagerTest extends BaseTest {

    @Test
    public void testFeatureEquals() {
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addFeature(new Feature("http://jabber.org/protocol/muc"));
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(new Feature("http://jabber.org/protocol/muc")));
    }

    @Test
    public void testItemsEquals() {
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addIdentity(new Identity("conference", "text", "name1", "en"));
        Assert.assertTrue(serviceDiscoveryManager.getIdentities().contains(new Identity("conference", "text", "name2", "en")));
    }

    @Test
    public void testInfoDiscovery() throws XmppException {
        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        InfoNode result = serviceDiscoveryManager.discoverInformation(JULIET);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getFeatures().size() > 1);
        //  Every entity MUST have at least one identity
        Assert.assertTrue(result.getIdentities().size() > 0);
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestXmppSession connection1 = new TestXmppSession();
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(serviceDiscoveryManager.isEnabled());
        Feature featureInfo = new Feature("http://jabber.org/protocol/disco#info");
        Feature featureItems = new Feature("http://jabber.org/protocol/disco#items");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(featureInfo));
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(featureItems));
        serviceDiscoveryManager.setEnabled(false);
        Assert.assertFalse(serviceDiscoveryManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(featureInfo));
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(featureItems));
    }
}
