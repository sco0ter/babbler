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

package org.xmpp.extension.muc.admin;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.muc.Affiliation;
import org.xmpp.extension.muc.MucElementFactory;
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

    @Test
    public void testBanUser() throws JAXBException, XMLStreamException, IOException {
        String xml = "<iq from='kinghenryv@shakespeare.lit/throne'\n" +
                "    id='ban1'\n" +
                "    to='southampton@chat.shakespeare.lit'\n" +
                "    type='set'>\n" +
                "  <query xmlns='http://jabber.org/protocol/muc#admin'>\n" +
                "    <item affiliation='outcast'\n" +
                "          jid='earlofcambridge@shakespeare.lit'>\n" +
                "      <reason>Treason</reason>\n" +
                "    </item>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        MucAdmin mucAdmin = iq.getExtension(MucAdmin.class);
        Assert.assertNotNull(mucAdmin);
        Assert.assertNotNull(mucAdmin.getItems());
        Assert.assertEquals(mucAdmin.getItems().get(0).getJid(), Jid.valueOf("earlofcambridge@shakespeare.lit"));
        Assert.assertEquals(mucAdmin.getItems().get(0).getAffiliation(), Affiliation.OUTCAST);
        Assert.assertEquals(mucAdmin.getItems().get(0).getReason(), "Treason");
    }

    @Test
    public void marshalBanUser() throws JAXBException, XMLStreamException, IOException {
        MucAdmin mucAdmin = new MucAdmin(MucElementFactory.createItem(Affiliation.OUTCAST, Jid.valueOf("earlofcambridge@shakespeare.lit")));
        String xml = marshall(mucAdmin);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#admin\"><item affiliation=\"outcast\" jid=\"earlofcambridge@shakespeare.lit\"></item></query>");
    }

    @Test
    public void marshalRequestBanlist() throws JAXBException, XMLStreamException, IOException {
        MucAdmin mucAdmin = new MucAdmin(MucElementFactory.createItem(Affiliation.OUTCAST));
        String xml = marshall(mucAdmin);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#admin\"><item affiliation=\"outcast\"></item></query>");
    }

    @Test
    public void marshalModifiedBanlist() throws JAXBException, XMLStreamException, IOException {
        MucAdmin mucAdmin = new MucAdmin(MucElementFactory.createItem(Affiliation.OUTCAST, Jid.valueOf("lordscroop@shakespeare.lit"), "Treason"), MucElementFactory.createItem(Affiliation.OUTCAST, Jid.valueOf("sirthomasgrey@shakespeare.lit"), "Treason"));
        String xml = marshall(mucAdmin);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#admin\"><item affiliation=\"outcast\" jid=\"lordscroop@shakespeare.lit\"><reason>Treason</reason></item><item affiliation=\"outcast\" jid=\"sirthomasgrey@shakespeare.lit\"><reason>Treason</reason></item></query>");
    }

    @Test
    public void marshalGrantMembership() throws JAXBException, XMLStreamException, IOException {
        MucAdmin mucAdmin = new MucAdmin(MucElementFactory.createItem(Affiliation.MEMBER, Jid.valueOf("hag66@shakespeare.lit"), "A worthy witch indeed!"));
        String xml = marshall(mucAdmin);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#admin\"><item affiliation=\"member\" jid=\"hag66@shakespeare.lit\"><reason>A worthy witch indeed!</reason></item></query>");
    }

    @Test
    public void marshalGrantModerator() throws JAXBException, XMLStreamException, IOException {
        MucAdmin mucAdmin = new MucAdmin(MucElementFactory.createItem(Role.MODERATOR, "thirdwitch", "A worthy witch indeed!"));
        String xml = marshall(mucAdmin);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#admin\"><item nick=\"thirdwitch\" role=\"moderator\"><reason>A worthy witch indeed!</reason></item></query>");
    }

    @Test
    public void marshalRevokeModerator() throws JAXBException, XMLStreamException, IOException {
        MucAdmin mucAdmin = new MucAdmin(MucElementFactory.createItem(Role.PARTICIPANT, "thirdwitch", "Not so worthy after all!"));
        String xml = marshall(mucAdmin);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#admin\"><item nick=\"thirdwitch\" role=\"participant\"><reason>Not so worthy after all!</reason></item></query>");
    }

    @Test
    public void marshalModifyModeratorList() throws JAXBException, XMLStreamException, IOException {
        MucAdmin mucAdmin = new MucAdmin(MucElementFactory.createItem(Role.PARTICIPANT, "thirdwitch"), MucElementFactory.createItem(Role.MODERATOR, "Hecate"));
        String xml = marshall(mucAdmin);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#admin\"><item nick=\"thirdwitch\" role=\"participant\"></item><item nick=\"Hecate\" role=\"moderator\"></item></query>");
    }
}
