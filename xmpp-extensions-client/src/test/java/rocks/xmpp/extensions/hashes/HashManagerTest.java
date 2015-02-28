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

package rocks.xmpp.extensions.hashes;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;

/**
 * @author Christian Schudt
 */
public class HashManagerTest extends ExtensionTest {

    @Test
    public void testServiceDiscoveryEntry() {
        TestXmppSession xmppSession = new TestXmppSession();
        HashManager hashManager = xmppSession.getManager(HashManager.class);
        Assert.assertTrue(hashManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:hashes:1");
        Feature featureSha256 = new Feature("urn:xmpp:hash-function-text-names:sha-256");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(featureSha256));
        hashManager.setEnabled(false);
        Assert.assertFalse(hashManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(featureSha256));
    }
}
