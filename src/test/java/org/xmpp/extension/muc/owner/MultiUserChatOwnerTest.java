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

package org.xmpp.extension.muc.owner;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.muc.Affiliation;
import org.xmpp.extension.muc.Destroy;
import org.xmpp.extension.muc.MucElementFactory;
import org.xmpp.extension.muc.Role;
import org.xmpp.extension.muc.admin.MucAdmin;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class MultiUserChatOwnerTest extends BaseTest {

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
        Assert.assertEquals(mucAdmin.getItems().get(0).getJid(), Jid.fromString("earlofcambridge@shakespeare.lit"));
        Assert.assertEquals(mucAdmin.getItems().get(0).getAffiliation(), Affiliation.OUTCAST);
        Assert.assertEquals(mucAdmin.getItems().get(0).getReason(), "Treason");
    }

    @Test
    public void marshalCreateInstantRoom() throws JAXBException, XMLStreamException, IOException {
        MucOwner mucOwner = new MucOwner(new DataForm(DataForm.Type.SUBMIT));
        String xml = marshall(mucOwner);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#owner\"><x xmlns=\"jabber:x:data\" type=\"submit\"></x></query>");
    }

    @Test
    public void marshalRequestConfigurationForm() throws JAXBException, XMLStreamException, IOException {
        MucOwner mucOwner = new MucOwner();
        String xml = marshall(mucOwner);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#owner\"></query>");
    }

    @Test
    public void unmarshalConfigurationForm() throws JAXBException, XMLStreamException, IOException {
        String xml = "<iq from='coven@chat.shakespeare.lit'\n" +
                "    id='config1'\n" +
                "    to='crone1@shakespeare.lit/desktop'\n" +
                "    type='result'>\n" +
                "  <query xmlns='http://jabber.org/protocol/muc#owner'>\n" +
                "    <x xmlns='jabber:x:data' type='form'>\n" +
                "      <title>Configuration for \"coven\" Room</title>\n" +
                "    </x>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        MucOwner mucOwner = iq.getExtension(MucOwner.class);
        Assert.assertNotNull(mucOwner);
        Assert.assertNotNull(mucOwner.getConfigurationForm());
    }

    @Test
    public void marshalDestroyRoom() throws JAXBException, XMLStreamException, IOException {
        MucOwner mucOwner = new MucOwner(MucElementFactory.createDestroy(Jid.fromString("coven@chat.shakespeare.lit"), "Macbeth doth come."));
        String xml = marshall(mucOwner);
        Assert.assertEquals(xml, "<query xmlns=\"http://jabber.org/protocol/muc#owner\"><destroy jid=\"coven@chat.shakespeare.lit\"><reason>Macbeth doth come.</reason></destroy></query>");
    }
}
