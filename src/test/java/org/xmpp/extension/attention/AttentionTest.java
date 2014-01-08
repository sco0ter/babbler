package org.xmpp.extension.attention;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.*;
import org.xmpp.extension.messagedeliveryreceipts.MessageDeliveryReceiptsManager;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
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
