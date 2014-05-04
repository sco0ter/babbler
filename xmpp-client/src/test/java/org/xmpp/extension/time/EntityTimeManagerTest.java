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

package org.xmpp.extension.time;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.MockServer;
import org.xmpp.TestConnection;
import org.xmpp.XmppException;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.stanza.StanzaException;

/**
 * @author Christian Schudt
 */
public class EntityTimeManagerTest extends BaseTest {

    @Test
    public void testEntityTimeManager() throws XmppException {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        new TestConnection(JULIET, mockServer);
        EntityTimeManager entityTimeManager = connection1.getExtensionManager(EntityTimeManager.class);
        EntityTime entityTime = entityTimeManager.getEntityTime(JULIET);
        Assert.assertNotNull(entityTime);
        Assert.assertNotNull(entityTime.getDate());
        Assert.assertNotNull(entityTime.getTimezone());
    }

    @Test
    public void testEntityTimeIfDisabled() throws XmppException {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        TestConnection connection2 = new TestConnection(JULIET, mockServer);
        connection2.getExtensionManager(EntityTimeManager.class).setEnabled(false);
        EntityTimeManager entityTimeManager = connection1.getExtensionManager(EntityTimeManager.class);
        try {
            entityTimeManager.getEntityTime(JULIET);
        } catch (StanzaException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestConnection connection1 = new TestConnection();
        EntityTimeManager entityTimeManager = connection1.getExtensionManager(EntityTimeManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(entityTimeManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:time");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
        entityTimeManager.setEnabled(false);
        Assert.assertFalse(entityTimeManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
