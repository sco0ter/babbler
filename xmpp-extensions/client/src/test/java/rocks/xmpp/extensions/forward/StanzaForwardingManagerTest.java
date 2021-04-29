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

package rocks.xmpp.extensions.forward;

import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.forward.model.Forwarded;

/**
 * Tests for {@link StanzaForwardingManager}.
 */
public class StanzaForwardingManagerTest extends BaseTest {

    @Test
    public void testServiceDiscoveryEntry() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());
        StanzaForwardingManager stanzaForwardingManager = xmppSession.getManager(StanzaForwardingManager.class);
        // By default, the manager should be NOT enabled.
        Assert.assertFalse(stanzaForwardingManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        Assert.assertFalse(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures().contains(Forwarded.NAMESPACE));
        stanzaForwardingManager.setEnabled(true);
        Assert.assertTrue(stanzaForwardingManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures().contains(Forwarded.NAMESPACE));
    }
}
