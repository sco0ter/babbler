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

package rocks.xmpp.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.roster.model.Contact;
import rocks.xmpp.core.roster.model.Roster;
import rocks.xmpp.core.stanza.model.client.IQ;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class PrefixFreeCanonicalizationWriterTest {

    @Test
    public void testSimpleStanza() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);

        XMLStreamWriter prefixFreeWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, true);

        JAXBContext jaxbContext = JAXBContext.newInstance(IQ.class, Roster.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);


        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(new Jid("domain")));
        Roster roster = new Roster(contacts);
        IQ iq = new IQ(IQ.Type.GET, roster, "1");

        marshaller.marshal(iq, prefixFreeWriter);
        Assert.assertEquals("<iq id=\"1\" type=\"get\"><query xmlns=\"jabber:iq:roster\"><item jid=\"domain\"></item></query></iq>", writer.toString());
    }

    @Test
    public void testElementWithPrefixedAttribute() throws XMLStreamException, JAXBException {

//        Writer writer = new StringWriter();
//
//        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
//        XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, true);
//
//        JAXBContext jaxbContext = JAXBContext.newInstance(Body.class);
//        Marshaller marshaller = jaxbContext.createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
//        Body body = new Body();
//        body.setXmppVersion("1.0");
//        body.setRestart(true);
//        body.setRid(1L);
//
//        marshaller.marshal(body, xmppStreamWriter);
    }
}
