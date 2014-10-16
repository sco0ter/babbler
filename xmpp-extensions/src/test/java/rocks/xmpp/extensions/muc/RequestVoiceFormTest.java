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

package rocks.xmpp.extensions.muc;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.muc.model.RequestVoiceForm;
import rocks.xmpp.extensions.muc.model.Role;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;

/**
 * @author Christian Schudt
 */
public class RequestVoiceFormTest extends XmlTest {
    protected RequestVoiceFormTest() throws JAXBException, XMLStreamException {
        super(DataForm.class);
    }

    @Test
    public void testRequestVoiceForm() throws JAXBException, XMLStreamException, MalformedURLException {
        String xml = "<x xmlns='jabber:x:data' type='submit'>\n" +
                "    <field var='FORM_TYPE' type='hidden'>\n" +
                "        <value>http://jabber.org/protocol/muc#request</value>\n" +
                "    </field>\n" +
                "    <field var='muc#role'>\n" +
                "      <value>participant</value>\n" +
                "    </field>\n" +
                "    <field var='muc#jid'>\n" +
                "      <value>hag66@shakespeare.lit/pda</value>\n" +
                "    </field>\n" +
                "    <field var='muc#roomnick'>\n" +
                "      <value>thirdwitch</value>\n" +
                "    </field>\n" +
                "    <field var='muc#request_allow'>\n" +
                "      <value>true</value>\n" +
                "    </field>\n" +
                "  </x>\n";

        DataForm dataForm = unmarshal(xml, DataForm.class);

        RequestVoiceForm requestVoiceForm = new RequestVoiceForm(dataForm);

        Assert.assertEquals(requestVoiceForm.getRoomNick(), "thirdwitch");
        Assert.assertEquals(requestVoiceForm.getRole(), Role.PARTICIPANT);
        Assert.assertEquals(requestVoiceForm.getJid(), Jid.valueOf("hag66@shakespeare.lit/pda"));
        Assert.assertTrue(requestVoiceForm.isRequestAllowed());
    }

    @Test
    public void testEmptyDataForm() throws MalformedURLException {
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);

        RequestVoiceForm requestVoiceForm = new RequestVoiceForm(dataForm);
        requestVoiceForm.setJid(Jid.valueOf("hag66@shakespeare.lit/pda"));
        DataForm.Field fieldJid = dataForm.findField("muc#jid");
        Assert.assertNotNull(fieldJid);
        Assert.assertEquals(fieldJid.getValues().size(), 1);
        Assert.assertEquals(fieldJid.getValues().get(0), "hag66@shakespeare.lit/pda");
        Assert.assertEquals(requestVoiceForm.getJid(), Jid.valueOf("hag66@shakespeare.lit/pda"));

        requestVoiceForm.setRoomNick("thirdwitch");
        DataForm.Field fieldRoomNick = dataForm.findField("muc#roomnick");
        Assert.assertNotNull(fieldRoomNick);
        Assert.assertEquals(fieldRoomNick.getValues().size(), 1);
        Assert.assertEquals(fieldRoomNick.getValues().get(0), "thirdwitch");
        Assert.assertEquals(requestVoiceForm.getRoomNick(), "thirdwitch");

        requestVoiceForm.setRole(Role.PARTICIPANT);
        DataForm.Field fieldRole = dataForm.findField("muc#role");
        Assert.assertNotNull(fieldRole);
        Assert.assertEquals(fieldRole.getValues().size(), 1);
        Assert.assertEquals(fieldRole.getValues().get(0), "participant");
        Assert.assertEquals(requestVoiceForm.getRole(), Role.PARTICIPANT);

        requestVoiceForm.setRequestAllowed(true);
        DataForm.Field fieldAllow = dataForm.findField("muc#request_allow");
        Assert.assertNotNull(fieldAllow);
        Assert.assertEquals(fieldAllow.getValues().size(), 1);
        Assert.assertEquals(fieldAllow.getValues().get(0), "1");
        Assert.assertTrue(requestVoiceForm.isRequestAllowed());
    }
}
