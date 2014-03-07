package org.xmpp.extension.muc.admin;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.muc.Role;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class MultiUserChatAdminTest extends BaseTest {

    @Test
    public void testKickOccupant() throws JAXBException, XMLStreamException, IOException {
        String xml = "<iq from='fluellen@shakespeare.lit/pda'\n" +
                "    id='kick1'\n" +
                "    to='harfleur@chat.shakespeare.lit'\n" +
                "    type='set'>\n" +
                "  <query xmlns='http://jabber.org/protocol/muc#admin'>\n" +
                "    <item nick='pistol' role='none'>\n" +
                "      <reason>Avaunt, you cullion!</reason>\n" +
                "    </item>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        MucAdmin mucAdmin = iq.getExtension(MucAdmin.class);
        Assert.assertNotNull(mucAdmin);
        Assert.assertNotNull(mucAdmin.getItems());
        Assert.assertEquals(mucAdmin.getItems().get(0).getNick(), "pistol");
        Assert.assertEquals(mucAdmin.getItems().get(0).getRole(), Role.NONE);
    }

    @Test
    public void testGrantVoice() throws JAXBException, XMLStreamException, IOException {
        String xml = "<iq from='crone1@shakespeare.lit/desktop'\n" +
                "    id='voice1'\n" +
                "    to='coven@chat.shakespeare.lit'\n" +
                "    type='set'>\n" +
                "  <query xmlns='http://jabber.org/protocol/muc#admin'>\n" +
                "    <item nick='thirdwitch'\n" +
                "          role='participant'/>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        MucAdmin mucAdmin = iq.getExtension(MucAdmin.class);
        Assert.assertNotNull(mucAdmin);
        Assert.assertNotNull(mucAdmin.getItems());
        Assert.assertEquals(mucAdmin.getItems().get(0).getNick(), "thirdwitch");
        Assert.assertEquals(mucAdmin.getItems().get(0).getRole(), Role.PARTICIPANT);
    }

    @Test
    public void testRevokeVoice() throws JAXBException, XMLStreamException, IOException {
        String xml = "<iq from='crone1@shakespeare.lit/desktop'\n" +
                "    id='voice2'\n" +
                "    to='coven@chat.shakespeare.lit'\n" +
                "    type='set'>\n" +
                "  <query xmlns='http://jabber.org/protocol/muc#admin'>\n" +
                "    <item nick='thirdwitch'\n" +
                "          role='visitor'>\n" +
                "      <reason>Not so worthy after all!</reason>\n" +
                "    </item>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        MucAdmin mucAdmin = iq.getExtension(MucAdmin.class);
        Assert.assertNotNull(mucAdmin);
        Assert.assertNotNull(mucAdmin.getItems());
        Assert.assertEquals(mucAdmin.getItems().get(0).getNick(), "thirdwitch");
        Assert.assertEquals(mucAdmin.getItems().get(0).getRole(), Role.VISITOR);
        Assert.assertEquals(mucAdmin.getItems().get(0).getReason(), "Not so worthy after all!");
    }
}
