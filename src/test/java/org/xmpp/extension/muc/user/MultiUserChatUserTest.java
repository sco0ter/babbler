package org.xmpp.extension.muc.user;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.muc.Affiliation;
import org.xmpp.extension.muc.Role;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.Presence;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class MultiUserChatUserTest extends BaseTest {

    @Test
    public void testPresenceBroadCast() throws JAXBException, XMLStreamException, IOException {
        String xml = "<presence\n" +
                "    from='coven@chat.shakespeare.lit/firstwitch'\n" +
                "    id='3DCB0401-D7CF-4E31-BE05-EDF8D057BFBD'\n" +
                "    to='hag66@shakespeare.lit/pda'>\n" +
                "  <x xmlns='http://jabber.org/protocol/muc#user'>\n" +
                "    <item affiliation='owner' role='moderator'/>\n" +
                "  </x>\n" +
                "</presence>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Presence presence = (Presence) unmarshaller.unmarshal(xmlEventReader);
        MucUser mucUser = presence.getExtension(MucUser.class);
        Assert.assertNotNull(mucUser);
        Assert.assertNotNull(mucUser.getItem());
        Assert.assertEquals(mucUser.getItem().getAffiliation(), Affiliation.OWNER);
        Assert.assertEquals(mucUser.getItem().getRole(), Role.MODERATOR);
    }

    @Test
    public void testAffiliationAndRole1() throws XMLStreamException, JAXBException {
        String xml = "<x xmlns='http://jabber.org/protocol/muc#user'>" +
                "<item affiliation='member' role='moderator'/>" +
                "</x>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        MucUser mucUser = (MucUser) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertEquals(mucUser.getItem().getAffiliation(), Affiliation.MEMBER);
        Assert.assertEquals(mucUser.getItem().getRole(), Role.MODERATOR);
    }

    @Test
    public void testAffiliationAndRole2() throws XMLStreamException, JAXBException {
        String xml = "<x xmlns='http://jabber.org/protocol/muc#user'>" +
                "<item affiliation='admin' role='none'/>" +
                "</x>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        MucUser mucUser = (MucUser) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertEquals(mucUser.getItem().getAffiliation(), Affiliation.ADMIN);
        Assert.assertEquals(mucUser.getItem().getRole(), Role.NONE);
    }

    @Test
    public void testAffiliationAndRole3() throws XMLStreamException, JAXBException {
        String xml = "<x xmlns='http://jabber.org/protocol/muc#user'>" +
                "<item affiliation='none' role='participant'/>" +
                "</x>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        MucUser mucUser = (MucUser) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertEquals(mucUser.getItem().getAffiliation(), Affiliation.NONE);
        Assert.assertEquals(mucUser.getItem().getRole(), Role.PARTICIPANT);
    }

    @Test
    public void testAffiliationAndRole4() throws XMLStreamException, JAXBException {
        String xml = "<x xmlns='http://jabber.org/protocol/muc#user'>" +
                "<item affiliation='outcast' role='visitor'/>" +
                "</x>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        MucUser mucUser = (MucUser) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertEquals(mucUser.getItem().getAffiliation(), Affiliation.OUTCAST);
        Assert.assertEquals(mucUser.getItem().getRole(), Role.VISITOR);
    }

    @Test
    public void testStatusCodes() throws XMLStreamException, JAXBException {
        String xml = "<presence\n" +
                "    from='coven@chat.shakespeare.lit/thirdwitch'\n" +
                "    id='n13mt3l'\n" +
                "    to='hag66@shakespeare.lit/pda'>\n" +
                "  <x xmlns='http://jabber.org/protocol/muc#user'>\n" +
                "    <item affiliation='member' role='participant'/>\n" +
                "    <status code='110'/>\n" +
                "    <status code='210'/>\n" +
                "  </x>\n" +
                "</presence>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Presence presence = (Presence) unmarshaller.unmarshal(xmlEventReader);
        MucUser mucUser = presence.getExtension(MucUser.class);
        Assert.assertNotNull(mucUser);
        Assert.assertNotNull(mucUser.getItem());
        Assert.assertEquals(mucUser.getStatusCodes().size(), 2);
        Assert.assertEquals(mucUser.getStatusCodes().get(0).getCode(), (Integer) 110);
        Assert.assertEquals(mucUser.getStatusCodes().get(1).getCode(), (Integer) 210);
    }

    @Test
    public void testFullJidAndNick() throws XMLStreamException, JAXBException {
        String xml = "<presence\n" +
                "    from='coven@chat.shakespeare.lit/thirdwitch'\n" +
                "    id='17232D15-134F-43C8-9A29-61C20A64B236'\n" +
                "    to='crone1@shakespeare.lit/desktop'>\n" +
                "  <x xmlns='http://jabber.org/protocol/muc#user'>\n" +
                "    <item affiliation='none'\n" +
                "          jid='hag66@shakespeare.lit/pda'\n" +
                "          nick='oldhag'\n" +
                "          role='participant'/>\n" +
                "  </x>\n" +
                "</presence>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Presence presence = (Presence) unmarshaller.unmarshal(xmlEventReader);
        MucUser mucUser = presence.getExtension(MucUser.class);
        Assert.assertNotNull(mucUser);
        Assert.assertNotNull(mucUser.getItem());
        Assert.assertEquals(mucUser.getItem().getJid(), Jid.fromString("hag66@shakespeare.lit/pda"));
        Assert.assertEquals(mucUser.getItem().getNick(), "oldhag");
    }

    @Test
    public void testMediatedInvitation() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='crone1@shakespeare.lit/desktop'\n" +
                "    id='nzd143v8'\n" +
                "    to='coven@chat.shakespeare.lit'>\n" +
                "  <x xmlns='http://jabber.org/protocol/muc#user'>\n" +
                "    <invite to='hecate@shakespeare.lit'>\n" +
                "      <reason>Hey Hecate, this is the place for all good witches!</reason>\n" +
                "    </invite>\n" +
                "  </x>\n" +
                "</message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        MucUser mucUser = message.getExtension(MucUser.class);
        Assert.assertNotNull(mucUser);
        Assert.assertNotNull(mucUser.getInvitations());
        Assert.assertEquals(mucUser.getInvitations().get(0).getTo(), Jid.fromString("hecate@shakespeare.lit"));
        Assert.assertEquals(mucUser.getInvitations().get(0).getReason(), "Hey Hecate, this is the place for all good witches!");
    }

    @Test
    public void testDecline() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='hecate@shakespeare.lit/broom'\n" +
                "    id='jk2vs61v'\n" +
                "    to='coven@chat.shakespeare.lit'>\n" +
                "  <x xmlns='http://jabber.org/protocol/muc#user'>\n" +
                "    <decline to='crone1@shakespeare.lit'>\n" +
                "      <reason>Sorry, I'm too busy right now.</reason>\n" +
                "    </decline>\n" +
                "  </x>\n" +
                "</message>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        MucUser mucUser = message.getExtension(MucUser.class);
        Assert.assertNotNull(mucUser);
        Assert.assertNotNull(mucUser.getDecline());
        Assert.assertEquals(mucUser.getDecline().getTo(), Jid.fromString("crone1@shakespeare.lit"));
        Assert.assertEquals(mucUser.getDecline().getReason(), "Sorry, I'm too busy right now.");
    }

    @Test
    public void testContinueFlagInInvitations() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='crone1@shakespeare.lit/desktop'\n" +
                "    id='gl3s85n7'\n" +
                "    to='coven@chat.shakespeare.lit'>\n" +
                "  <x xmlns='http://jabber.org/protocol/muc#user'>\n" +
                "    <invite to='wiccarocks@shakespeare.lit/laptop'>\n" +
                "      <reason>This coven needs both wiccarocks and hag66.</reason>\n" +
                "      <continue thread='e0ffe42b28561960c6b12b944a092794b9683a38'/>\n" +
                "    </invite>\n" +
                "    <invite to='hag66@shakespeare.lit'>\n" +
                "      <reason>This coven needs both wiccarocks and hag66.</reason>\n" +
                "      <continue thread='e0ffe42b28561960c6b12b944a092794b9683a38'/>\n" +
                "    </invite>\n" +
                "  </x>\n" +
                "</message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        MucUser mucUser = message.getExtension(MucUser.class);
        Assert.assertNotNull(mucUser);
        Assert.assertNotNull(mucUser.getInvitations());
        Assert.assertEquals(mucUser.getInvitations().size(), 2);
        Assert.assertEquals(mucUser.getInvitations().get(0).getTo(), Jid.fromString("wiccarocks@shakespeare.lit/laptop"));
        Assert.assertEquals(mucUser.getInvitations().get(0).getContinue().getThread(), "e0ffe42b28561960c6b12b944a092794b9683a38");
        Assert.assertEquals(mucUser.getInvitations().get(1).getTo(), Jid.fromString("hag66@shakespeare.lit"));
        Assert.assertEquals(mucUser.getInvitations().get(1).getContinue().getThread(), "e0ffe42b28561960c6b12b944a092794b9683a38");
    }
}
