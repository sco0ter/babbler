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

package org.xmpp.stanza;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.Jid;
import org.xmpp.XmlTest;
import org.xmpp.stanza.client.Presence;
import org.xmpp.stanza.errors.RemoteServerNotFound;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class PresenceTest extends XmlTest {
    protected PresenceTest() throws JAXBException, XMLStreamException {
        super(Presence.class, RemoteServerNotFound.class);
    }

    @Test
    public void unmarshalPresence() throws XMLStreamException, JAXBException {
        String xml = "<presence from='romeo@example.net'" +
                "              id='xk3h1v69'\n" +
                "              to='juliet@example.com'\n" +
                "              type='subscribe'/>";

        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getTo().toString(), "juliet@example.com");
        Assert.assertEquals(presence.getFrom().toString(), "romeo@example.net");
        Assert.assertEquals(presence.getType(), Presence.Type.SUBSCRIBE);
        Assert.assertEquals(presence.getId(), "xk3h1v69");
    }

    @Test
    public void unmarshalPresenceWithError() throws XMLStreamException, JAXBException {
        String xml = "<presence from='juliet@example.com'\n" +
                "              id='xk3h1v69'\n" +
                "              to='romeo@example.net'\n" +
                "              type='error'>\n" +
                "      <error type='modify'>\n" +
                "        <remote-server-not-found\n" +
                "            xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "      </error>\n" +
                "    </presence>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(presence.getFrom().toString(), "juliet@example.com");
        Assert.assertEquals(presence.getType(), Presence.Type.ERROR);
        Assert.assertNotNull(presence.getError());
        Assert.assertNotNull(presence.getError().getCondition() instanceof RemoteServerNotFound);
    }

    @Test
    public void unmarshalPresenceTypeSubscribed() throws XMLStreamException, JAXBException {
        String xml = "<presence from='juliet@example.com'\n" +
                "              id='xk3h1v69'\n" +
                "              to='romeo@example.net'\n" +
                "              type='subscribed'/>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getFrom().toString(), "juliet@example.com");
        Assert.assertEquals(presence.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(presence.getType(), Presence.Type.SUBSCRIBED);
        Assert.assertEquals(presence.getId(), "xk3h1v69");
    }

    @Test
    public void unmarshalPresenceTypeUnsubscribed() throws XMLStreamException, JAXBException {
        String xml = "<presence id='tb2m1b59'\n" +
                "              to='romeo@example.net'\n" +
                "              type='unsubscribed'/>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(presence.getType(), Presence.Type.UNSUBSCRIBED);
        Assert.assertEquals(presence.getId(), "tb2m1b59");
    }

    @Test
    public void unmarshalPresenceTypeUnsubscribe() throws XMLStreamException, JAXBException {
        String xml = "<presence id='tb2m1b59'\n" +
                "              to='romeo@example.net'\n" +
                "              type='unsubscribe'/>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(presence.getType(), Presence.Type.UNSUBSCRIBE);
        Assert.assertEquals(presence.getId(), "tb2m1b59");
    }

    @Test
    public void unmarshalPresenceTypeProbe() throws XMLStreamException, JAXBException {
        String xml = "<presence id='tb2m1b59'\n" +
                "              to='romeo@example.net'\n" +
                "              type='probe'/>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(presence.getType(), Presence.Type.PROBE);
        Assert.assertEquals(presence.getId(), "tb2m1b59");
    }

    @Test
    public void unmarshalPresenceTypeUnavailable() throws XMLStreamException, JAXBException {
        String xml = "<presence id='tb2m1b59'\n" +
                "              to='romeo@example.net'\n" +
                "              type='unavailable'/>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(presence.getType(), Presence.Type.UNAVAILABLE);
        Assert.assertEquals(presence.getId(), "tb2m1b59");
    }

    @Test
    public void unmarshalPresenceStatus() throws XMLStreamException, JAXBException {
        String xml = "<presence from='romeo@example.net/orchard'\n" +
                "          xml:lang='en'>\n" +
                "  <show>dnd</show>\n" +
                "  <status>Wooing Juliet</status>\n" +
                "</presence>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getFrom().toString(), "romeo@example.net/orchard");
        Assert.assertEquals(presence.getShow(), Presence.Show.DND);
        Assert.assertEquals(presence.getLanguage(), "en");
        Assert.assertEquals(presence.getStatus(), "Wooing Juliet");
    }

    @Test
    public void unmarshalMultiplePresenceStatus() throws XMLStreamException, JAXBException {
        String xml = "<presence from='romeo@example.net/orchard'\n" +
                "          id='jx62vs97'\n" +
                "          xml:lang='en'>\n" +
                "  <show>dnd</show>\n" +
                "  <status>Wooing Juliet</status>\n" +
                "  <status xml:lang='de'>Julia</status>\n" +
                "</presence>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getFrom().toString(), "romeo@example.net/orchard");
        Assert.assertEquals(presence.getShow(), Presence.Show.DND);
        Assert.assertEquals(presence.getLanguage(), "en");
        Assert.assertEquals(presence.getStatus(), "Wooing Juliet");
    }

    @Test
    public void unmarshalPresenceShowAway() throws XMLStreamException, JAXBException {
        String xml = "<presence from='romeo@example.net/orchard'\n" +
                "          xml:lang='en'>\n" +
                "  <show>away</show>\n" +
                "</presence>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getShow(), Presence.Show.AWAY);
    }

    @Test
    public void unmarshalPresenceShowXA() throws XMLStreamException, JAXBException {
        String xml = "<presence from='romeo@example.net/orchard'\n" +
                "          xml:lang='en'>\n" +
                "  <show>xa</show>\n" +
                "</presence>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getShow(), Presence.Show.XA);
    }

    @Test
    public void unmarshalPresenceShowChat() throws XMLStreamException, JAXBException {
        String xml = "<presence from='romeo@example.net/orchard'\n" +
                "          xml:lang='en'>\n" +
                "  <show>chat</show>\n" +
                "</presence>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getShow(), Presence.Show.CHAT);
    }

    @Test
    public void unmarshalPresenceShowDnd() throws XMLStreamException, JAXBException {
        String xml = "<presence from='romeo@example.net/orchard'\n" +
                "          xml:lang='en'>\n" +
                "  <show>dnd</show>\n" +
                "</presence>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getShow(), Presence.Show.DND);
    }

    @Test
    public void unmarshalPriority() throws XMLStreamException, JAXBException {
        String xml = "<presence xml:lang='en'>\n" +
                "  <show>dnd</show>\n" +
                "  <status>Wooing Juliet</status>\n" +
                "  <status xml:lang='cs'>Dvo&#x0159;&#x00ED;m se Julii</status>\n" +
                "  <priority>1</priority>\n" +
                "</presence>";
        Presence presence = unmarshal(xml, Presence.class);
        Assert.assertEquals(presence.getPriority(), 1);
    }

    @Test
    public void marshalPresenceMultipleStatus() throws JAXBException, XMLStreamException {
        Presence presence = new Presence(Presence.Type.SUBSCRIBE);
        presence.setId("id");
        presence.setTo(new Jid("to", "domain"));
        presence.setFrom(new Jid("from", "domain"));
        presence.getStatuses().add(new Presence.Status("status", "de"));
        Presence.Status status = new Presence.Status("status2");
        status.setLanguage("en");
        presence.getStatuses().add(status);
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"subscribe\"><status xml:lang=\"de\">status</status><status xml:lang=\"en\">status2</status></presence>");
    }

    @Test
    public void marshalPresenceShowDnd() throws JAXBException, XMLStreamException {
        Presence presence = new Presence(Presence.Type.SUBSCRIBED);
        presence.setId("id");
        presence.setTo(new Jid("to", "domain"));
        presence.setFrom(new Jid("from", "domain"));
        presence.setShow(Presence.Show.DND);
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"subscribed\"><show>dnd</show></presence>");
    }

    @Test
    public void marshalPresenceShowAway() throws JAXBException, XMLStreamException {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBE);
        presence.setId("id");
        presence.setTo(new Jid("to", "domain"));
        presence.setFrom(new Jid("from", "domain"));
        presence.setShow(Presence.Show.AWAY);
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"unsubscribe\"><show>away</show></presence>");
    }

    @Test
    public void marshalPresenceShowXA() throws JAXBException, XMLStreamException {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBED);
        presence.setId("id");
        presence.setTo(new Jid("to", "domain"));
        presence.setFrom(new Jid("from", "domain"));
        presence.setShow(Presence.Show.XA);
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"unsubscribed\"><show>xa</show></presence>");
    }

    @Test
    public void marshalPresenceShowChat() throws JAXBException, XMLStreamException {
        Presence presence = new Presence(Presence.Type.UNAVAILABLE);
        presence.setId("id");
        presence.setTo(new Jid("to", "domain"));
        presence.setFrom(new Jid("from", "domain"));
        presence.setShow(Presence.Show.CHAT);
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"unavailable\"><show>chat</show></presence>");
    }

    @Test
    public void testIsAvailable() throws JAXBException, XMLStreamException {
        Presence presence1 = new Presence();
        Assert.assertTrue(presence1.isAvailable());

        Presence presence2 = new Presence(Presence.Type.SUBSCRIBE);
        Assert.assertFalse(presence2.isAvailable());

        Presence presence3 = new Presence(Presence.Type.UNSUBSCRIBED);
        Assert.assertFalse(presence3.isAvailable());

        Presence presence4 = new Presence(Presence.Type.UNSUBSCRIBE);
        Assert.assertFalse(presence4.isAvailable());

        Presence presence5 = new Presence(Presence.Type.SUBSCRIBED);
        Assert.assertFalse(presence5.isAvailable());

        Presence presence6 = new Presence(Presence.Type.ERROR);
        Assert.assertFalse(presence6.isAvailable());

        Presence presence7 = new Presence(Presence.Type.PROBE);
        Assert.assertFalse(presence7.isAvailable());

        Presence presence8 = new Presence(Presence.Type.UNAVAILABLE);
        Assert.assertFalse(presence8.isAvailable());
    }

    @Test
    public void testComparableImplementation() {
        Presence presenceUnavailable = new Presence(Presence.Type.UNAVAILABLE);
        Presence presenceDnd = new Presence(Presence.Show.DND);
        presenceDnd.setPriority((byte) 1);
        Presence presenceAway = new Presence(Presence.Show.AWAY);
        presenceAway.setPriority((byte) 1);
        Presence presenceXa = new Presence(Presence.Show.XA);
        presenceXa.setPriority((byte) 1);
        Presence presenceChat = new Presence(Presence.Show.CHAT);
        presenceChat.setPriority((byte) 1);

        Presence presencePrio1 = new Presence();
        presencePrio1.setPriority((byte) 1);
        Presence presencePrio2 = new Presence();
        presencePrio2.setPriority((byte) 2);
        Presence presencePrio1Unavailble = new Presence(Presence.Type.UNAVAILABLE);
        presencePrio1Unavailble.setPriority((byte) 1);

        List<Presence> list = new ArrayList<>();
        list.add(presenceAway);
        list.add(presenceDnd);
        list.add(presenceChat);
        list.add(presenceXa);
        list.add(presenceUnavailable);
        list.add(presencePrio1);
        list.add(presencePrio2);
        list.add(presencePrio1Unavailble);

        Collections.shuffle(list);
        Collections.sort(list);

        Assert.assertEquals(list.get(0), presencePrio1);
        Assert.assertEquals(list.get(1), presenceChat);
        Assert.assertEquals(list.get(2), presenceAway);
        Assert.assertEquals(list.get(3), presenceXa);
        Assert.assertEquals(list.get(4), presenceDnd);
        Assert.assertEquals(list.get(5), presencePrio2);
        Assert.assertEquals(list.get(6), presenceUnavailable);
        Assert.assertEquals(list.get(7), presencePrio1Unavailble);

    }
}
