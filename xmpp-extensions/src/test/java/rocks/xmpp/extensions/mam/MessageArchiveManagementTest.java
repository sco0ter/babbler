/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.extensions.mam;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.extensions.mam.model.ArchiveConfiguration;
import rocks.xmpp.extensions.mam.model.MessageArchive;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

/**
 * @author Christian Schudt
 */
public class MessageArchiveManagementTest extends XmlTest {
    protected MessageArchiveManagementTest() throws JAXBException, XMLStreamException {
        super(ClientMessage.class, ClientIQ.class, MessageArchive.class);
    }

    @Test
    public void unmarshalMamQuery() throws XMLStreamException, JAXBException {
        String xml = "<iq type='set' id='juliet1'>\n" +
                "  <query xmlns='urn:xmpp:mam:2' queryid='f27' />\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        MessageArchive.Query mam = iq.getExtension(MessageArchive.Query.class);
        Assert.assertNotNull(mam);
        Assert.assertEquals(mam.getQueryId(), "f27");
    }

    @Test
    public void unmarshalMamResult() throws XMLStreamException, JAXBException {
        String xml = "<message id='aeb213' to='juliet@capulet.lit/chamber'>\n" +
                "  <result xmlns='urn:xmpp:mam:2' queryid='f27' id='28482-98726-73623'>\n" +
                "    <forwarded xmlns='urn:xmpp:forward:0'>\n" +
                "      <delay xmlns='urn:xmpp:delay' stamp='2010-07-10T23:08:25Z'/>\n" +
                "      <message xmlns='jabber:client' from=\"witch@shakespeare.lit\" to=\"macbeth@shakespeare.lit\">\n" +
                "        <body>Hail to thee</body>\n" +
                "      </message>\n" +
                "    </forwarded>\n" +
                "  </result>\n" +
                "</message>\n";
        Message message = unmarshal(xml, Message.class);
        MessageArchive.Result mam = message.getExtension(MessageArchive.Result.class);
        Assert.assertNotNull(mam);
        Assert.assertEquals(mam.getId(), "28482-98726-73623");
        Assert.assertEquals(mam.getQueryId(), "f27");;
        Assert.assertNotNull(mam.getForwarded());
        Assert.assertNotNull(mam.getForwarded().getDelayedDelivery());
        Assert.assertEquals(mam.getForwarded().getDelayedDelivery().getTimeStamp(), OffsetDateTime.of(2010, 7, 10, 23, 8, 25, 0, ZoneOffset.UTC).toInstant());
        Assert.assertEquals(((Message) mam.getForwarded().getStanza()).getBody(), "Hail to thee");
    }

    @Test
    public void unmarshalQueryWithDataForm() throws XMLStreamException, JAXBException {
        String xml = "<iq type='set' id='juliet1'>\n" +
                "  <query xmlns='urn:xmpp:mam:2'>\n" +
                "    <x xmlns='jabber:x:data' type='submit'>\n" +
                "      <field var='FORM_TYPE' type='hidden'>\n" +
                "        <value>urn:xmpp:mam:1</value>\n" +
                "      </field>\n" +
                "      <field var='start'>\n" +
                "        <value>2010-06-07T00:00:00Z</value>\n" +
                "      </field>\n" +
                "      <field var='end'>\n" +
                "        <value>2010-07-07T13:23:54Z</value>\n" +
                "      </field>\n" +
                "    </x>\n" +
                "  </query>\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        MessageArchive.Query mam = iq.getExtension(MessageArchive.Query.class);
        Assert.assertNotNull(mam);
        Assert.assertNotNull(mam.getDataForm());
        Assert.assertEquals(mam.getDataForm().findValueAsInstant("start"), OffsetDateTime.of(2010, 6, 7, 0, 0, 0, 0, ZoneOffset.UTC).toInstant());
    }

    @Test
    public void unmarshalFin() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result' id='u29303'>\n" +
                "  <fin xmlns='urn:xmpp:mam:2' complete='true'>\n" +
                "    <set xmlns='http://jabber.org/protocol/rsm'>\n" +
                "      <first index='0'>23452-4534-1</first>\n" +
                "      <last>390-2342-22</last>\n" +
                "      <count>16</count>\n" +
                "    </set>\n" +
                "  </fin>\n" +
                "</iq>\n";
        IQ iq = unmarshal(xml, IQ.class);
        MessageArchive.Fin fin = iq.getExtension(MessageArchive.Fin.class);
        Assert.assertNotNull(fin);
        Assert.assertNotNull(fin.getResultSet());
        Assert.assertEquals(fin.getResultSet().getLastItem(), "390-2342-22");
        Assert.assertTrue(fin.isComplete());
    }

    @Test
    public void marshalFin() throws XMLStreamException, JAXBException {
        MessageArchive.Fin fin = new MessageArchive.Fin(ResultSetManagement.forCount(1), true);
        String xml = marshal(fin);
        Assert.assertEquals(xml, "<fin xmlns=\"urn:xmpp:mam:2\" complete=\"true\"><set xmlns=\"http://jabber.org/protocol/rsm\"><count>1</count></set></fin>");
    }

    @Test
    public void marshalPrefs() throws XMLStreamException, JAXBException {
        MessageArchive.Preferences preferences = new MessageArchive.Preferences();
        Assert.assertEquals(marshal(preferences), "<prefs xmlns=\"urn:xmpp:mam:2\"></prefs>");
        Assert.assertTrue(preferences.getAlways().isEmpty());
        Assert.assertTrue(preferences.getNever().isEmpty());

        MessageArchive.Preferences preferences2 = new MessageArchive.Preferences(MessageArchive.Preferences.Default.ALWAYS, Collections.singleton(Jid.of("romeo@montague.lit")), Collections.emptyList());
        Assert.assertEquals(marshal(preferences2), "<prefs xmlns=\"urn:xmpp:mam:2\" default=\"always\"><always><jid>romeo@montague.lit</jid></always><never></never></prefs>");
    }

    @Test
    public void unmarshalPrefs() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result' id='juliet3'>\n" +
                "  <prefs xmlns='urn:xmpp:mam:2' default='roster'>\n" +
                "    <always>\n" +
                "      <jid>romeo@montague.lit</jid>\n" +
                "      <jid>juliet@montague.lit</jid>\n" +
                "    </always>\n" +
                "    <never>\n" +
                "      <jid>montague@montague.lit</jid>\n" +
                "    </never>\n" +
                "  </prefs>\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        MessageArchive.Preferences prefs = iq.getExtension(MessageArchive.Preferences.class);
        Assert.assertNotNull(prefs);
        Assert.assertEquals(prefs.getDefault(), MessageArchive.Preferences.Default.ROSTER);
        Assert.assertEquals(prefs.getAlways().size(), 2);
        Assert.assertEquals(prefs.getNever().size(), 1);
    }

    @Test
    public void testConfiguration() throws XMLStreamException, JAXBException {
        Instant start = Instant.now();
        ArchiveConfiguration configuration = ArchiveConfiguration.builder()
                .with(Jid.of("test"))
                .start(start)
                .end(start)
                .build();
        String xml = marshal(configuration.getDataForm());
        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"submit\"><field type=\"hidden\" var=\"FORM_TYPE\"><value>urn:xmpp:mam:2</value></field><field type=\"jid-single\" var=\"with\"><value>test</value></field><field type=\"text-single\" var=\"start\"><value>" + start + "</value></field><field type=\"text-single\" var=\"end\"><value>" + start + "</value></field></x>");
    }
}
