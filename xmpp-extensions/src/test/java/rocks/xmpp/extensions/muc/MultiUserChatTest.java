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

package rocks.xmpp.extensions.muc;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.extensions.muc.model.History;
import rocks.xmpp.extensions.muc.model.Muc;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.time.Instant;

/**
 * @author Christian Schudt
 */
public class MultiUserChatTest extends XmlTest {

    protected MultiUserChatTest() throws JAXBException, XMLStreamException {
        super(Presence.class, Muc.class);
    }

    @Test
    public void testEnterRoom() throws JAXBException, XMLStreamException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc());
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"></x></presence>");
    }

    @Test
    public void testEnterRoomWithPassword() throws JAXBException, XMLStreamException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc("cauldronburn"));
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><password>cauldronburn</password></x></presence>");
    }

    @Test
    public void testEnterRoomWithHistoryMaxChars() throws JAXBException, XMLStreamException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc(History.forMaxChars(65000)));
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><history maxchars=\"65000\"></history></x></presence>");
    }

    @Test
    public void testEnterRoomWithHistoryMaxStanzas() throws JAXBException, XMLStreamException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc(History.forMaxMessages(20)));
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><history maxstanzas=\"20\"></history></x></presence>");
    }

    @Test
    public void testEnterRoomWithHistorySeconds() throws JAXBException, XMLStreamException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc(History.forSeconds(180)));
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><history seconds=\"180\"></history></x></presence>");
    }

    @Test
    public void testEnterRoomWithHistorySince() throws JAXBException, XMLStreamException {
        Instant now = Instant.now();
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc(History.since(now)));
        String xml = marshal(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><history since=\"" + now.toString() + "\"></history></x></presence>");
    }
}
