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

        RequestVoiceForm requestVoiceForm = RequestVoiceForm.builder()
                .jid(Jid.valueOf("hag66@shakespeare.lit/pda"))
                .role(Role.MODERATOR)
                .roomNick("thirdwitch")
                .allowRequest(true)
                .build();

        String xml = marshal(requestVoiceForm.getDataForm());
        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"submit\">" +
                "<field type=\"hidden\" var=\"FORM_TYPE\"><value>http://jabber.org/protocol/muc#request</value></field>" +
                "<field type=\"text-single\" var=\"muc#role\"><value>moderator</value></field>" +
                "<field type=\"jid-single\" var=\"muc#jid\"><value>hag66@shakespeare.lit/pda</value></field>" +
                "<field type=\"text-single\" var=\"muc#roomnick\"><value>thirdwitch</value></field>" +
                "<field type=\"boolean\" var=\"muc#request_allow\"><value>1</value></field>" +
                "</x>");

        DataForm dataForm = unmarshal(xml, DataForm.class);
        RequestVoiceForm requestVoiceForm1 = new RequestVoiceForm(dataForm);

        Assert.assertEquals(requestVoiceForm1.getRoomNick(), "thirdwitch");
        Assert.assertEquals(requestVoiceForm1.getRole(), Role.MODERATOR);
        Assert.assertEquals(requestVoiceForm1.getJid(), Jid.valueOf("hag66@shakespeare.lit/pda"));
        Assert.assertTrue(requestVoiceForm1.isRequestAllowed());
    }
}
