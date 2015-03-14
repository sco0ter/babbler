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

package rocks.xmpp.extensions.reach;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.model.AbstractMessage;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.attention.AttentionManager;
import rocks.xmpp.extensions.attention.model.Attention;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;

/**
 * @author Christian Schudt
 */
public class ReachabilityManagerTest extends ExtensionTest {

    @Test
    public void testReachabilityManager() {

        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        final boolean[] attentionReceived = {false};
        xmppSession2.addInboundMessageListener(new MessageListener() {
            @Override
            public void handleMessage(MessageEvent e) {
                if (e.getMessage().getExtension(Attention.class) != null && e.getMessage().getType() == AbstractMessage.Type.HEADLINE) {
                    attentionReceived[0] = true;
                    Assert.assertEquals(e.getMessage().getType(), AbstractMessage.Type.HEADLINE);
                }
            }
        });

        AttentionManager attentionManager = xmppSession1.getManager(AttentionManager.class);
        attentionManager.captureAttention(JULIET);

        Assert.assertTrue(attentionReceived[0]);
    }

    @Test
    public void testServiceDiscoveryEntry() {

        TestXmppSession connection1 = new TestXmppSession();
        ReachabilityManager reachabilityManager = connection1.getManager(ReachabilityManager.class);
        Assert.assertFalse(reachabilityManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:reach:0");
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        reachabilityManager.setEnabled(true);
        Assert.assertTrue(reachabilityManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
