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
import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.sasl.model.Auth;
import rocks.xmpp.core.sasl.model.Response;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.util.XmppUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Christian Schudt
 */
public class PrefixFreeCanonicalizationWriterTest {

    @Test
    public void testSimpleStanza() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XmlTest.OUTPUT_FACTORY.createXMLStreamWriter(writer);

        XMLStreamWriter prefixFreeWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, "jabber:client");

        JAXBContext jaxbContext = JAXBContext.newInstance(ClientIQ.class, Roster.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);


        Collection<Contact> contacts = new ArrayDeque<>();
        contacts.add(new Contact(Jid.ofDomain("domain")));
        Roster roster = new Roster(contacts);
        IQ iq = ClientIQ.from(new IQ(IQ.Type.GET, roster, "1"));

        marshaller.marshal(iq, prefixFreeWriter);
        prefixFreeWriter.flush();
        Assert.assertEquals(writer.toString(), "<iq id=\"1\" type=\"get\"><query xmlns=\"jabber:iq:roster\"><item jid=\"domain\"></item></query></iq>");
    }

    @Test
    public void testElementWithPrefixedAttribute() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XmlTest.OUTPUT_FACTORY.createXMLStreamWriter(writer);
        XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter);

        JAXBContext jaxbContext = JAXBContext.newInstance(Body.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        Body body = Body.builder().xmppVersion("1.0").restart(true).requestId(1L).build();

        marshaller.marshal(body, xmppStreamWriter);
        xmppStreamWriter.flush();
        Assert.assertEquals(writer.toString(), "<body xmlns=\"http://jabber.org/protocol/httpbind\" rid=\"1\" xmlns:xmpp=\"urn:xmpp:xbosh\" xmpp:version=\"1.0\" xmpp:restart=\"true\"></body>");
    }

    @Test
    public void testTwoElementsWithSameNamespace() throws XMLStreamException, JAXBException, IOException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XmlTest.OUTPUT_FACTORY.createXMLStreamWriter(writer);
        XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter);
        xmppStreamWriter.writeStartElement("stream");
        JAXBContext jaxbContext = JAXBContext.newInstance(Auth.class, Response.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        Auth auth = new Auth("PLAIN", null);
        marshaller.marshal(auth, xmppStreamWriter);

        Response response = new Response(null);
        marshaller.marshal(response, xmppStreamWriter);
        xmppStreamWriter.flush();
        Assert.assertEquals(writer.toString(), "<stream><auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\"></auth><response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\"></response>");
    }

    @Test
    public void testStreamFeatures() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XmlTest.OUTPUT_FACTORY.createXMLStreamWriter(writer);
        XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter);

        JAXBContext jaxbContext = JAXBContext.newInstance(Body.class, StreamFeatures.class, Bind.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        StreamFeatures streamFeatures = new StreamFeatures(Collections.singleton(new Bind("res")));

        marshaller.marshal(streamFeatures, xmppStreamWriter);
        xmppStreamWriter.flush();
        Assert.assertEquals(writer.toString(), "<stream:features><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><resource>res</resource></bind></stream:features>");
    }

    @Test
    public void testStreamFeaturesInBody() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XmlTest.OUTPUT_FACTORY.createXMLStreamWriter(writer);
        XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, "jabber:client", true);

        JAXBContext jaxbContext = JAXBContext.newInstance(Body.class, StreamFeatures.class, Bind.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        StreamFeatures streamFeatures = new StreamFeatures(Collections.singleton(new Bind("res")));

        marshaller.marshal(Body.builder().xmppVersion("1.0").wrappedObjects(Collections.singleton(streamFeatures)).build(), xmppStreamWriter);
        xmppStreamWriter.flush();
        Assert.assertEquals(writer.toString(), "<body xmlns=\"http://jabber.org/protocol/httpbind\" xmlns:stream=\"http://etherx.jabber.org/streams\" xmlns:xmpp=\"urn:xmpp:xbosh\" xmpp:version=\"1.0\"><stream:features><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><resource>res</resource></bind></stream:features></body>");
    }

    @Test
    public void testStreamFeaturesNotInBody() throws XMLStreamException, JAXBException {

        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XmlTest.OUTPUT_FACTORY.createXMLStreamWriter(writer);
        XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, "jabber:client", false);

        JAXBContext jaxbContext = JAXBContext.newInstance(Body.class, StreamFeatures.class, Auth.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        Auth auth = new Auth("PLAIN", null);

        marshaller.marshal(Body.builder().xmppVersion("1.0").wrappedObjects(Collections.singleton(auth)).build(), xmppStreamWriter);
        xmppStreamWriter.flush();
        Assert.assertEquals(writer.toString(), "<body xmlns=\"http://jabber.org/protocol/httpbind\" xmlns:xmpp=\"urn:xmpp:xbosh\" xmpp:version=\"1.0\"><auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\"></auth></body>");
    }
}
