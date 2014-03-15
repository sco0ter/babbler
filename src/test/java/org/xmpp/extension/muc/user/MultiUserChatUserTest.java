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

package org.xmpp.extension.muc.user;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.muc.Affiliation;
import org.xmpp.extension.muc.MucElementFactory;
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
        Assert.assertEquals(mucUser.getStatusCodes().get(0).getCode(), 110);
        Assert.assertEquals(mucUser.getStatusCodes().get(1).getCode(), 210);
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
        Assert.assertEquals(mucUser.getItem().getJid(), Jid.valueOf("hag66@shakespeare.lit/pda"));
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
        Assert.assertNotNull(mucUser.getInvites());
        Assert.assertEquals(mucUser.getInvites().get(0).getTo(), Jid.valueOf("hecate@shakespeare.lit"));
        Assert.assertEquals(mucUser.getInvites().get(0).getReason(), "Hey Hecate, this is the place for all good witches!");
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
        Assert.assertEquals(mucUser.getDecline().getTo(), Jid.valueOf("crone1@shakespeare.lit"));
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
        Assert.assertNotNull(mucUser.getInvites());
        Assert.assertEquals(mucUser.getInvites().size(), 2);
        Assert.assertEquals(mucUser.getInvites().get(0).getTo(), Jid.valueOf("wiccarocks@shakespeare.lit/laptop"));
        Assert.assertEquals(mucUser.getInvites().get(0).getContinue().getThread(), "e0ffe42b28561960c6b12b944a092794b9683a38");
        Assert.assertEquals(mucUser.getInvites().get(1).getTo(), Jid.valueOf("hag66@shakespeare.lit"));
        Assert.assertEquals(mucUser.getInvites().get(1).getContinue().getThread(), "e0ffe42b28561960c6b12b944a092794b9683a38");
    }

    @Test
    public void unmarshalKicked() throws XMLStreamException, JAXBException {
        String xml = "<presence\n" +
                "    from='harfleur@chat.shakespeare.lit/pistol'\n" +
                "    to='pistol@shakespeare.lit/harfleur'\n" +
                "    type='unavailable'>\n" +
                "  <x xmlns='http://jabber.org/protocol/muc#user'>\n" +
                "    <item affiliation='none' role='none'>\n" +
                "      <actor nick='Fluellen'/>\n" +
                "      <reason>Avaunt, you cullion!</reason>\n" +
                "    </item>\n" +
                "    <status code='307'/>\n" +
                "  </x>\n" +
                "</presence>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Presence presence = (Presence) unmarshaller.unmarshal(xmlEventReader);
        MucUser mucUser = presence.getExtension(MucUser.class);
        Assert.assertNotNull(mucUser);
        Assert.assertNotNull(mucUser.getItem());
        Assert.assertNotNull(mucUser.getItem().getActor());
        Assert.assertEquals(mucUser.getItem().getActor().getNick(), "Fluellen");
    }

    @Test
    public void unmarshalDestroyed() throws XMLStreamException, JAXBException {
        String xml = "<presence\n" +
                "    from='heath@chat.shakespeare.lit/firstwitch'\n" +
                "    to='crone1@shakespeare.lit/desktop'\n" +
                "    type='unavailable'>\n" +
                "  <x xmlns='http://jabber.org/protocol/muc#user'>\n" +
                "    <item affiliation='none' role='none'/>\n" +
                "    <destroy jid='coven@chat.shakespeare.lit'>\n" +
                "      <reason>Macbeth doth come.</reason>\n" +
                "    </destroy>\n" +
                "  </x>\n" +
                "</presence>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Presence presence = (Presence) unmarshaller.unmarshal(xmlEventReader);
        MucUser mucUser = presence.getExtension(MucUser.class);
        Assert.assertNotNull(mucUser);
        Assert.assertNotNull(mucUser.getDestroy());
        Assert.assertEquals(mucUser.getDestroy().getJid(), Jid.valueOf("coven@chat.shakespeare.lit"));
        Assert.assertEquals(mucUser.getDestroy().getReason(), "Macbeth doth come.");
    }

    @Test
    public void marshalDestroyRoom() throws JAXBException, XMLStreamException, IOException {
        MucUser mucOwner = new MucUser(MucElementFactory.createDestroy(Jid.valueOf("coven@chat.shakespeare.lit"), "Macbeth doth come."));
        String xml = marshall(mucOwner);
        Assert.assertEquals(xml, "<x xmlns=\"http://jabber.org/protocol/muc#user\"><destroy jid=\"coven@chat.shakespeare.lit\"><reason>Macbeth doth come.</reason></destroy></x>");
    }
}
