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

package rocks.xmpp.extensions.bytestreams.ibb;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.bytestreams.ByteStreamEvent;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;

import java.util.UUID;

/**
 * @author Christian Schudt
 */
public class IbbTest extends ExtensionTest {

    @Test
    public void testServiceDiscoveryEntry() {
        TestXmppSession connection1 = new TestXmppSession();
        InBandByteStreamManager inBandBytestreamManager = connection1.getManager(InBandByteStreamManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(inBandBytestreamManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("http://jabber.org/protocol/ibb");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
        inBandBytestreamManager.setEnabled(false);
        Assert.assertFalse(inBandBytestreamManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
    }

    @Test
    public void testIbbSessionRejection() {
        MockServer mockServer = new MockServer();
        final XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        final XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);
        InBandByteStreamManager inBandBytestreamManager2 = xmppSession2.getManager(InBandByteStreamManager.class);
        inBandBytestreamManager2.addByteStreamListener(ByteStreamEvent::reject);

        InBandByteStreamManager inBandBytestreamManager1 = xmppSession1.getManager(InBandByteStreamManager.class);
        boolean rejected = false;
        try {
            inBandBytestreamManager1.initiateSession(JULIET, UUID.randomUUID().toString(), 4096);
        } catch (XmppException e) {
            if (e instanceof StanzaException) {
                if (((StanzaException) e).getStanza().getError().getCondition() == rocks.xmpp.core.stanza.model.errors.Condition.NOT_ACCEPTABLE) {
                    rejected = true;
                }
            }
        }

        if (!rejected) {
            Assert.fail("Should have been rejected");
        }
    }
}
