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

package org.xmpp.extension.messagedeliveryreceipts;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.*;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class MessageDeliveryReceiptsTest extends BaseTest {

    @Test
    public void unmarshalMessageDeliveryRequest() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='northumberland@shakespeare.lit/westminster'\n" +
                "    id='richard2-4.1.247'\n" +
                "    to='kingrichard@royalty.england.lit/throne'>\n" +
                "  <body>My lord, dispatch; read o'er these articles.</body>\n" +
                "  <request xmlns='urn:xmpp:receipts'/>\n" +
                "</message>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Request request = message.getExtension(Request.class);
        Assert.assertNotNull(request);
    }

    @Test
    public void unmarshalMessageDeliveryReceived() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='kingrichard@royalty.england.lit/throne'\n" +
                "    id='bi29sg183b4v'\n" +
                "    to='northumberland@shakespeare.lit/westminster'>\n" +
                "  <received xmlns='urn:xmpp:receipts' id='richard2-4.1.247'/>\n" +
                "</message>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Received received = message.getExtension(Received.class);
        Assert.assertNotNull(received);
        Assert.assertEquals(received.getId(), "richard2-4.1.247");
    }

    @Test
    public void testManager() {
        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        Connection connection2 = new TestConnection(JULIET, mockServer);

        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager1 = connection1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        // Test if the manager is disabled by default.
        Assert.assertFalse(messageDeliveryReceiptsManager1.isEnabled());

        messageDeliveryReceiptsManager1.setEnabled(true);
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager2 = connection2.getExtensionManager(MessageDeliveryReceiptsManager.class);
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

        Message message = new Message(JULIET);
        message.setId("123");
        connection1.send(message);

        Assert.assertTrue(messageReceived[0]);
        Assert.assertEquals(receivedId[0], "123");
    }

    @Test
    public void testManagerIfContactDoesNotSupportReceipts() {
        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        Connection connection2 = new TestConnection(JULIET, mockServer);

        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager1 = connection1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager1.setEnabled(true);
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager2 = connection2.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager2.setEnabled(false);

        final boolean[] messageReceived = {false};
        messageDeliveryReceiptsManager1.setEnabled(true);
        messageDeliveryReceiptsManager1.addMessageDeliveredListener(new MessageDeliveredListener() {
            @Override
            public void messageDelivered(MessageDeliveredEvent e) {
                messageReceived[0] = true;
            }
        });

        Message message = new Message(JULIET);
        message.setId("123");
        connection1.send(message);

        Assert.assertFalse(messageReceived[0]);
    }

    @Test
    public void testManagerIfNoMessageId() {
        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager1 = connection1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager1.setEnabled(true);
        connection1.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                Assert.assertNull(e.getMessage().getExtension(Request.class));
            }
        });

        Message message = new Message(JULIET);
        connection1.send(message);
    }

    /**
     * A sender could request receipts on any non-error content message
     */
    @Test
    public void testManagerIfErrorType() {
        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager1 = connection1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        messageDeliveryReceiptsManager1.setEnabled(true);
        connection1.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                Assert.assertNull(e.getMessage().getExtension(Request.class));
            }
        });

        Message message = new Message(JULIET);
        message.setType(Message.Type.ERROR);
        message.setId("123");
        connection1.send(message);
    }

    @Test
    public void testDisabledManager() {
        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);

        connection1.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                Assert.assertNull(e.getMessage().getExtension(Request.class));
            }
        });

        Message message = new Message(JULIET);
        message.setId("123");
        connection1.send(message);
    }

    @Test
    public void testEnablingManager() {
        TestConnection connection1 = new TestConnection();
        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = connection1.getExtensionManager(MessageDeliveryReceiptsManager.class);
        Assert.assertFalse(messageDeliveryReceiptsManager.isEnabled());
    }

    @Test
    public void testListeners() throws IOException, TimeoutException {
        TestConnection connection1 = new TestConnection();
        connection1.connect();

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
        TestConnection connection1 = new TestConnection();
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
