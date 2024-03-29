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

package rocks.xmpp.extensions.version;

import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;

/**
 * @author Christian Schudt
 */
public class SoftwareVersionManagerTest extends BaseTest {

    @Test
    public void testSoftwareVersionManager() throws ExecutionException, InterruptedException {
        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        new TestXmppSession(JULIET, mockServer);
        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        connection2.getManager(SoftwareVersionManager.class).setSoftwareVersion(new SoftwareVersion("Name", "Version"));
        SoftwareVersionManager softwareVersionManager = connection1.getManager(SoftwareVersionManager.class);
        SoftwareVersion softwareVersion = softwareVersionManager.getSoftwareVersion(JULIET).get();
        Assert.assertNotNull(softwareVersion);
        Assert.assertEquals(softwareVersion.getSoftware(), "Name");
        Assert.assertEquals(softwareVersion.getSoftwareVersion(), "Version");
    }

    @Test
    public void testSoftwareVersionManagerIfDisabled() {
        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        connection2.getManager(SoftwareVersionManager.class).setSoftwareVersion(null);
        SoftwareVersionManager softwareVersionManager = connection1.getManager(SoftwareVersionManager.class);
        try {
            softwareVersionManager.getSoftwareVersion(JULIET).get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testServiceDiscoveryEntry() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());
        SoftwareVersionManager softwareVersionManager = xmppSession.getManager(SoftwareVersionManager.class);
        softwareVersionManager.setSoftwareVersion(new SoftwareVersion());
        // By default, the manager should be enabled.
        Assert.assertTrue(softwareVersionManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);

        Assert.assertTrue(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures().contains(SoftwareVersion.NAMESPACE));
        softwareVersionManager.setSoftwareVersion(null);
        Assert.assertFalse(softwareVersionManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures().contains(SoftwareVersion.NAMESPACE));
    }

}
