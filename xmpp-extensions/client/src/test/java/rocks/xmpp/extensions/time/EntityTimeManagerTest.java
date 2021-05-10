/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.extensions.time;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.time.model.EntityTime;

/**
 * Tests for {@link EntityTimeManager}.
 */
public class EntityTimeManagerTest extends BaseTest {

    @Test
    public void testEntityTimeManager() throws ExecutionException, InterruptedException {
        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        new TestXmppSession(JULIET, mockServer);
        EntityTimeManager entityTimeManager = connection1.getManager(EntityTimeManager.class);
        OffsetDateTime entityTime = entityTimeManager.getEntityTime(JULIET).get();
        Assert.assertNotNull(entityTime);
    }

    @Test
    public void testEntityTimeIfDisabled() throws InterruptedException {
        MockServer mockServer = new MockServer();
        TestXmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        TestXmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);
        xmppSession2.disableFeature(EntityTime.NAMESPACE);
        EntityTimeManager entityTimeManager = xmppSession1.getManager(EntityTimeManager.class);
        try {
            entityTimeManager.getEntityTime(JULIET).get();
        } catch (ExecutionException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testServiceDiscoveryEntry() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());
        // By default, the manager should be enabled.
        Assert.assertTrue(xmppSession.getEnabledFeatures().contains(EntityTime.NAMESPACE));
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        DiscoverableInfo discoverableInfo = serviceDiscoveryManager.discoverInformation(JULIET).get();
        Assert.assertTrue(discoverableInfo.getFeatures().contains(EntityTime.NAMESPACE));
        xmppSession.disableFeature(EntityTime.NAMESPACE);
        Assert.assertFalse(xmppSession.getEnabledFeatures().contains(EntityTime.NAMESPACE));
        discoverableInfo = serviceDiscoveryManager.discoverInformation(JULIET).get();
        Assert.assertFalse(discoverableInfo.getFeatures().contains(EntityTime.NAMESPACE));
    }
}
