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

package org.xmpp.extension.attention;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.*;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class AttentionTest extends BaseTest {

    @Test
    public void unmarshalAttentionInMessage() throws XMLStreamException, JAXBException {
        String xml = "<message from='calvin@usrobots.lit/lab'\n" +
                "         to='herbie@usrobots.lit/home'\n" +
                "         type='headline'>\n" +
                "  <attention xmlns='urn:xmpp:attention:0'/>\n" +
                "  <body>Why don't you answer, Herbie?</body>\n" +
                "</message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Attention attention = message.getExtension(Attention.class);
        Assert.assertNotNull(attention);
    }

    @Test
    public void testAttentionManager() {

        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        Connection connection2 = new TestConnection(JULIET, mockServer);

        final boolean[] attentionReceived = {false};
        connection2.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming() && e.getMessage().getExtension(Attention.class) != null && e.getMessage().getType() == Message.Type.HEADLINE) {
                    attentionReceived[0] = true;
                    Assert.assertEquals(e.getMessage().getType(), Message.Type.HEADLINE);
                }
            }
        });

        AttentionManager attentionManager = connection1.getExtensionManager(AttentionManager.class);
        attentionManager.captureAttention(JULIET);

        Assert.assertTrue(attentionReceived[0]);

    }

    @Test
    public void testServiceDiscoveryEntry() {

        TestConnection connection1 = new TestConnection();
        AttentionManager attentionManager = connection1.getExtensionManager(AttentionManager.class);
        Assert.assertFalse(attentionManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:attention:0");
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        attentionManager.setEnabled(true);
        Assert.assertTrue(attentionManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
