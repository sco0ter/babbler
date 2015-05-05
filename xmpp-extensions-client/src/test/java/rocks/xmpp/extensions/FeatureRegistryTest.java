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

package rocks.xmpp.extensions;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.ping.PingManager;
import rocks.xmpp.extensions.ping.model.Ping;

/**
 * @author Christian Schudt
 */
public class FeatureRegistryTest {

    @Test
    public void test() {
        XmppSession xmppSession = new TestXmppSession();
        // By default ping is enabled.
        Assert.assertTrue(xmppSession.getManager(ServiceDiscoveryManager.class).getFeatures().contains(Ping.NAMESPACE));
        Assert.assertTrue(xmppSession.getManager(PingManager.class).isEnabled());

        // Then remove the feature from service discovery
        xmppSession.getManager(ServiceDiscoveryManager.class).removeFeature(Ping.NAMESPACE);

        // As a consequence PingManager should be disabled.
        Assert.assertFalse(xmppSession.getManager(PingManager.class).isEnabled());
        Assert.assertFalse(xmppSession.getManager(ServiceDiscoveryManager.class).getFeatures().contains(Ping.NAMESPACE));
        Assert.assertFalse(xmppSession.isFeatureEnabled(Ping.NAMESPACE));

        // Enable by namespace.
        xmppSession.enableFeature(Ping.NAMESPACE);

        Assert.assertTrue(xmppSession.getManager(PingManager.class).isEnabled());
        Assert.assertTrue(xmppSession.getManager(ServiceDiscoveryManager.class).getFeatures().contains(Ping.NAMESPACE));
    }
}