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

package rocks.xmpp.extensions.pubsub;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.pubsub.model.PubSubMetaDataForm;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public class PubSubConfigurationFormsTest extends XmlTest {

    protected PubSubConfigurationFormsTest() throws JAXBException, XMLStreamException {
        super(DataForm.class);
    }

    @Test
    public void testMetaData() throws JAXBException, XMLStreamException {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        PubSubMetaDataForm pubSubMetaDataForm = PubSubMetaDataForm.builder()
                .contacts(Arrays.asList(Jid.valueOf("contact")))
                .creationDate(date)
                .creator(Jid.valueOf("creator"))
                .description("desc")
                .language("de")
                .numberOfSubscribers(2)
                .owners(Arrays.asList(Jid.valueOf("owner")))
                .publishers(Arrays.asList(Jid.valueOf("publisher")))
                .title("title")
                .payloadType("namespace")
                .build();

        String xml = marshal(pubSubMetaDataForm.getDataForm());
        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"result\">" +
                "<field type=\"hidden\" var=\"FORM_TYPE\"><value>http://jabber.org/protocol/pubsub#meta-data</value></field>" +
                "<field type=\"jid-multi\" var=\"pubsub#contact\"><value>contact</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#creation_date\"><value>" + DatatypeConverter.printDateTime(calendar) + "</value></field>" +
                "<field type=\"jid-single\" var=\"pubsub#creator\"><value>creator</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#description\"><value>desc</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#language\"><value>de</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#num_subscribers\"><value>2</value></field>" +
                "<field type=\"jid-multi\" var=\"pubsub#owner\"><value>owner</value></field>" +
                "<field type=\"jid-multi\" var=\"pubsub#publisher\"><value>publisher</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#title\"><value>title</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#type\"><value>namespace</value></field>" +
                "</x>");
        DataForm dataForm = unmarshal(xml, DataForm.class);
        PubSubMetaDataForm pubSubMetaDataForm1 = new PubSubMetaDataForm(dataForm);
        Assert.assertEquals(pubSubMetaDataForm1.getCreationDate(), date);
        Assert.assertEquals(pubSubMetaDataForm1.getCreator(), Jid.valueOf("creator"));
        Assert.assertEquals(pubSubMetaDataForm1.getDescription(), "desc");
        Assert.assertEquals(pubSubMetaDataForm1.getLanguage(), "de");
        Assert.assertEquals(pubSubMetaDataForm1.getNumberOfSubscribers(), new Integer(2));
        Assert.assertEquals(pubSubMetaDataForm1.getOwners(), Arrays.asList(Jid.valueOf("owner")));
        Assert.assertEquals(pubSubMetaDataForm1.getPublishers(), Arrays.asList(Jid.valueOf("publisher")));
        Assert.assertEquals(pubSubMetaDataForm1.getTitle(), "title");
        Assert.assertEquals(pubSubMetaDataForm1.getPayloadType(), "namespace");
    }
}
