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

package org.xmpp.extension.reach;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.XmppSession;
import org.xmpp.MockServer;
import org.xmpp.TestConnection;
import org.xmpp.XmppSession;
import org.xmpp.extension.attention.Attention;
import org.xmpp.extension.attention.AttentionManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.stanza.AbstractMessage;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;

/**
 * @author Christian Schudt
 */
public class ReachabilityManagerTest extends BaseTest {

    @Test
    public void testReachabilityManager() {

        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestConnection(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestConnection(JULIET, mockServer);

        final boolean[] attentionReceived = {false};
        xmppSession2.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming() && e.getMessage().getExtension(Attention.class) != null && e.getMessage().getType() == AbstractMessage.Type.HEADLINE) {
                    attentionReceived[0] = true;
                    Assert.assertEquals(e.getMessage().getType(), AbstractMessage.Type.HEADLINE);
                }
            }
        });

        AttentionManager attentionManager = xmppSession1.getExtensionManager(AttentionManager.class);
        attentionManager.captureAttention(JULIET);

        Assert.assertTrue(attentionReceived[0]);
    }

    @Test
    public void testServiceDiscoveryEntry() {

        TestConnection connection1 = new TestConnection();
        ReachabilityManager reachabilityManager = connection1.getExtensionManager(ReachabilityManager.class);
        Assert.assertFalse(reachabilityManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:reach:0");
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        reachabilityManager.setEnabled(true);
        Assert.assertTrue(reachabilityManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
