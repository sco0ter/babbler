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
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.muc.model.RoomRegistrationForm;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Christian Schudt
 */
public class RoomRegistrationFormTest extends XmlTest {
    protected RoomRegistrationFormTest() throws JAXBException, XMLStreamException {
        super(DataForm.class);
    }

    @Test
    public void testRoomRegistrationForm() throws JAXBException, XMLStreamException, MalformedURLException {
        String xml = "<x xmlns='jabber:x:data' type='submit'>\n" +
                "      <field var='FORM_TYPE'>\n" +
                "        <value>http://jabber.org/protocol/muc#register</value>\n" +
                "      </field>\n" +
                "      <field var='muc#register_first'>\n" +
                "        <value>Brunhilde</value>\n" +
                "      </field>\n" +
                "      <field var='muc#register_last'>\n" +
                "        <value>Entwhistle-Throckmorton</value>\n" +
                "      </field>\n" +
                "      <field var='muc#register_roomnick'>\n" +
                "        <value>thirdwitch</value>\n" +
                "      </field>\n" +
                "      <field var='muc#register_url'>\n" +
                "        <value>http://witchesonline/~hag66/</value>\n" +
                "      </field>\n" +
                "      <field var='muc#register_email'>\n" +
                "        <value>hag66@witchesonline</value>\n" +
                "      </field>\n" +
                "      <field var='muc#register_faqentry'>\n" +
                "        <value>Just another witch.</value>\n" +
                "      </field>\n" +
                "    </x>\n";

        DataForm dataForm = unmarshal(xml, DataForm.class);

        RoomRegistrationForm roomRegistrationForm = new RoomRegistrationForm(dataForm);

        Assert.assertEquals(roomRegistrationForm.getGivenName(), "Brunhilde");
        Assert.assertEquals(roomRegistrationForm.getFamilyName(), "Entwhistle-Throckmorton");
        Assert.assertEquals(roomRegistrationForm.getRoomNick(), "thirdwitch");
        Assert.assertEquals(roomRegistrationForm.getWebPage(), new URL("http://witchesonline/~hag66/"));
        Assert.assertEquals(roomRegistrationForm.getEmail(), "hag66@witchesonline");
        Assert.assertEquals(roomRegistrationForm.getFaqEntry(), "Just another witch.");
    }

    @Test
    public void testEmptyDataForm() throws MalformedURLException {
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        RoomRegistrationForm roomRegistrationForm = new RoomRegistrationForm(dataForm);
        roomRegistrationForm.setGivenName("Brunhilde");
        DataForm.Field fieldGivenName = dataForm.findField("muc#register_first");
        Assert.assertNotNull(fieldGivenName);
        Assert.assertEquals(fieldGivenName.getValues().size(), 1);
        Assert.assertEquals(fieldGivenName.getValues().get(0), "Brunhilde");
        Assert.assertEquals(roomRegistrationForm.getGivenName(), "Brunhilde");

        roomRegistrationForm.setFamilyName("Entwhistle-Throckmorton");
        DataForm.Field fieldFamilyName = dataForm.findField("muc#register_last");
        Assert.assertNotNull(fieldFamilyName);
        Assert.assertEquals(fieldFamilyName.getValues().size(), 1);
        Assert.assertEquals(fieldFamilyName.getValues().get(0), "Entwhistle-Throckmorton");
        Assert.assertEquals(roomRegistrationForm.getFamilyName(), "Entwhistle-Throckmorton");

        roomRegistrationForm.setRoomNick("thirdwitch");
        DataForm.Field fieldDescription = dataForm.findField("muc#register_roomnick");
        Assert.assertNotNull(fieldDescription);
        Assert.assertEquals(fieldDescription.getValues().size(), 1);
        Assert.assertEquals(fieldDescription.getValues().get(0), "thirdwitch");
        Assert.assertEquals(roomRegistrationForm.getRoomNick(), "thirdwitch");

        roomRegistrationForm.setWebPage(new URL("http://witchesonline/~hag66/"));
        DataForm.Field fieldLanguage = dataForm.findField("muc#register_url");
        Assert.assertNotNull(fieldLanguage);
        Assert.assertEquals(fieldLanguage.getValues().size(), 1);
        Assert.assertEquals(fieldLanguage.getValues().get(0), "http://witchesonline/~hag66/");
        Assert.assertEquals(roomRegistrationForm.getWebPage(), new URL("http://witchesonline/~hag66/"));

        roomRegistrationForm.setEmail("hag66@witchesonline");
        DataForm.Field fieldLdap = dataForm.findField("muc#register_email");
        Assert.assertNotNull(fieldLdap);
        Assert.assertEquals(fieldLdap.getValues().size(), 1);
        Assert.assertEquals(fieldLdap.getValues().get(0), "hag66@witchesonline");
        Assert.assertEquals(roomRegistrationForm.getEmail(), "hag66@witchesonline");

        roomRegistrationForm.setFaqEntry("Just another witch.");
        DataForm.Field fieldLogs = dataForm.findField("muc#register_faqentry");
        Assert.assertNotNull(fieldLogs);
        Assert.assertEquals(fieldLogs.getValues().size(), 1);
        Assert.assertEquals(fieldLogs.getValues().get(0), "Just another witch.");
        Assert.assertEquals(roomRegistrationForm.getFaqEntry(), "Just another witch.");

        roomRegistrationForm.setRegisterAllowed(true);
        DataForm.Field fieldAllow = dataForm.findField("muc#register_allow");
        Assert.assertNotNull(fieldAllow);
        Assert.assertEquals(fieldAllow.getValues().size(), 1);
        Assert.assertEquals(fieldAllow.getValues().get(0), "1");
        Assert.assertTrue(roomRegistrationForm.isRegisterAllowed());
    }
}
