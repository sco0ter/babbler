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

        RoomRegistrationForm roomRegistrationForm = RoomRegistrationForm.builder()
                .allowRegister(true)
                .email("hag66@witchesonline")
                .familyName("Entwhistle-Throckmorton")
                .givenName("Brunhilde")
                .faqEntry("Just another witch.")
                .nickname("thirdwitch")
                .webPage(new URL("http://witchesonline/~hag66/"))
                .build();

        String xml = marshal(roomRegistrationForm.getDataForm());

        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"submit\">" +
                "<field type=\"hidden\" var=\"FORM_TYPE\"><value>http://jabber.org/protocol/muc#register</value></field>" +
                "<field type=\"boolean\" var=\"muc#register_allow\"><value>1</value></field>" +
                "<field type=\"text-single\" var=\"muc#register_email\"><value>hag66@witchesonline</value></field>" +
                "<field type=\"text-multi\" var=\"muc#register_faqentry\"><value>Just another witch.</value></field>" +
                "<field type=\"text-single\" var=\"muc#register_first\"><value>Brunhilde</value></field>" +
                "<field type=\"text-single\" var=\"muc#register_last\"><value>Entwhistle-Throckmorton</value></field>" +
                "<field type=\"text-single\" var=\"muc#register_roomnick\"><value>thirdwitch</value></field>" +
                "<field type=\"text-single\" var=\"muc#register_url\"><value>http://witchesonline/~hag66/</value></field>" +
                "</x>");

        DataForm dataForm = unmarshal(xml, DataForm.class);

        RoomRegistrationForm roomRegistrationForm2 = new RoomRegistrationForm(dataForm);
        Assert.assertEquals(roomRegistrationForm2.getGivenName(), "Brunhilde");
        Assert.assertEquals(roomRegistrationForm2.getFamilyName(), "Entwhistle-Throckmorton");
        Assert.assertEquals(roomRegistrationForm2.getRoomNick(), "thirdwitch");
        Assert.assertEquals(roomRegistrationForm2.getWebPage(), new URL("http://witchesonline/~hag66/"));
        Assert.assertEquals(roomRegistrationForm2.getEmail(), "hag66@witchesonline");
        Assert.assertEquals(roomRegistrationForm2.getFaqEntry(), "Just another witch.");
        Assert.assertTrue(roomRegistrationForm2.isRegisterAllowed());
    }
}
