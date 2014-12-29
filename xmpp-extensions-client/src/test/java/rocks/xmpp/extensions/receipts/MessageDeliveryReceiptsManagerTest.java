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

package rocks.xmpp.extensions.receipts;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.receipts.model.Request;

import java.io.IOException;
import java.util.Collections;

/**
 * @author Christian Schudt
 */
public class MessageDeliveryReceiptsManagerTest extends ExtensionTest {

    @Test
    public void testManager() {
        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager1 = xmppSession1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        // Test if the manager is disabled by default.
        Assert.assertFalse(messageDeliveryReceiptsManager1.isEnabled());

        messageDeliveryReceiptsManager1.setEnabled(true);
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager2 = xmppSession2.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager2.setEnabled(true);

        final boolean[] messageReceived = {false};
        final String[] receivedId = {null};
        messageDeliveryReceiptsManager1.setEnabled(true);
        messageDeliveryReceiptsManager1.addMessageDeliveredListener(new MessageDeliveredListener() {
            @Override
            public void messageDelivered(MessageDeliveredEvent e) {
                messageReceived[0] = true;
                receivedId[0] = e.getMessageId();
            }
        });

        Message message = new Message(JULIET, null, Collections.<Message.Body>emptyList(), null, null, null, "123", null, null, null, null);
        xmppSession1.send(message);

        Assert.assertTrue(messageReceived[0]);
        Assert.assertEquals(receivedId[0], "123");
    }

    @Test
    public void testManagerIfContactDoesNotSupportReceipts() {
        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager1 = xmppSession1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager1.setEnabled(true);
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager2 = xmppSession2.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager2.setEnabled(false);

        final boolean[] messageReceived = {false};
        messageDeliveryReceiptsManager1.setEnabled(true);
        messageDeliveryReceiptsManager1.addMessageDeliveredListener(new MessageDeliveredListener() {
            @Override
            public void messageDelivered(MessageDeliveredEvent e) {
                messageReceived[0] = true;
            }
        });

        Message message = new Message(JULIET, null, Collections.<Message.Body>emptyList(), null, null, null, "123", null, null, null, null);
        xmppSession1.send(message);

        Assert.assertFalse(messageReceived[0]);
    }

    @Test
    public void testManagerIfNoMessageId() {
        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager1 = xmppSession1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager1.setEnabled(true);
        xmppSession1.addMessageListener(new MessageListener() {
            @Override
            public void handleMessage(MessageEvent e) {
                Assert.assertNull(e.getMessage().getExtension(Request.class));
            }
        });

        Message message = new Message(JULIET);
        xmppSession1.send(message);
    }

    /**
     * A sender could request receipts on any non-error content message
     */
    @Test
    public void testManagerIfErrorType() {
        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager1 = xmppSession1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager1.setEnabled(true);
        xmppSession1.addMessageListener(new MessageListener() {
            @Override
            public void handleMessage(MessageEvent e) {
                Assert.assertNull(e.getMessage().getExtension(Request.class));
            }
        });

        Message message = new Message(JULIET, Message.Type.ERROR, Collections.<Message.Body>emptyList(), null, null, null, "123", null, null, null, null);
        xmppSession1.send(message);
    }

    @Test
    public void testDisabledManager() {
        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);

        xmppSession1.addMessageListener(new MessageListener() {
            @Override
            public void handleMessage(MessageEvent e) {
                Assert.assertNull(e.getMessage().getExtension(Request.class));
            }
        });

        Message message = new Message(JULIET, null, Collections.<Message.Body>emptyList(), null, null, null, "123", null, null, null, null);
        xmppSession1.send(message);
    }

    @Test
    public void testEnablingManager() {
        TestXmppSession connection1 = new TestXmppSession();
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = connection1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        Assert.assertFalse(messageDeliveryReceiptsManager.isEnabled());
    }

    @Test
    public void testListeners() throws IOException {
        TestXmppSession connection1 = new TestXmppSession();

        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = connection1.getExtensionManager(MessageDeliveryReceiptsManager.class);

        MessageDeliveredListener messageDeliveredListener = new MessageDeliveredListener() {
            @Override
            public void messageDelivered(MessageDeliveredEvent e) {

            }
        };
        messageDeliveryReceiptsManager.addMessageDeliveredListener(messageDeliveredListener);
        Assert.assertEquals(messageDeliveryReceiptsManager.messageDeliveredListeners.size(), 1);
        messageDeliveryReceiptsManager.removeMessageDeliveredListener(messageDeliveredListener);
        Assert.assertEquals(messageDeliveryReceiptsManager.messageDeliveredListeners.size(), 0);
        messageDeliveryReceiptsManager.addMessageDeliveredListener(new MessageDeliveredListener() {
            @Override
            public void messageDelivered(MessageDeliveredEvent e) {

            }
        });

        connection1.close();
        // Listeners should be cleared now.
        Assert.assertEquals(messageDeliveryReceiptsManager.messageDeliveredListeners.size(), 0);
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestXmppSession connection1 = new TestXmppSession();
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = connection1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        Assert.assertFalse(messageDeliveryReceiptsManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:receipts");
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        messageDeliveryReceiptsManager.setEnabled(true);
        Assert.assertTrue(messageDeliveryReceiptsManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
