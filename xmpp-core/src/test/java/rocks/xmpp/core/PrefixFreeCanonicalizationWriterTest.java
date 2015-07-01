/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.roster.model.Contact;
import rocks.xmpp.core.roster.model.Roster;
import rocks.xmpp.core.sasl.model.Auth;
import rocks.xmpp.core.sasl.model.Response;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.util.XmppUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Collection;

/**
 * @author Christian Schudt
 */
public class PrefixFreeCanonicalizationWriterTest {

    @Test
    public void testSimpleStanza() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);

        XMLStreamWriter prefixFreeWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, "jabber:client");

        JAXBContext jaxbContext = JAXBContext.newInstance(ClientIQ.class, Roster.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);


        Collection<Contact> contacts = new ArrayDeque<>();
        contacts.add(new Contact(new Jid("domain")));
        Roster roster = new Roster(contacts);
        IQ iq = ClientIQ.from(new IQ(IQ.Type.GET, roster, "1"));

        marshaller.marshal(iq, prefixFreeWriter);
        Assert.assertEquals(writer.toString(), "<iq id=\"1\" type=\"get\"><query xmlns=\"jabber:iq:roster\"><item jid=\"domain\"></item></query></iq>");
    }

    @Test
    public void testElementWithPrefixedAttribute() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
        XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter);

        JAXBContext jaxbContext = JAXBContext.newInstance(Body.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        Body body = Body.builder().xmppVersion("1.0").restart(true).requestId(1L).build();

        marshaller.marshal(body, xmppStreamWriter);

        Assert.assertEquals(writer.toString(), "<body xmlns=\"http://jabber.org/protocol/httpbind\" rid=\"1\" xmlns:xmpp=\"urn:xmpp:xbosh\" xmpp:version=\"1.0\" xmpp:restart=\"true\"></body>");
    }

    @Test
    public void testTwoElementsWithSameNamespace() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
        XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter);

        JAXBContext jaxbContext = JAXBContext.newInstance(Auth.class, Response.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        Auth auth = new Auth("PLAIN", null);
        marshaller.marshal(auth, xmppStreamWriter);

        Response response = new Response(null);
        marshaller.marshal(response, xmppStreamWriter);

        Assert.assertEquals(writer.toString(), "<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\"></auth><response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\"></response>");
    }
}
