/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.extensions.sid;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.extensions.sid.model.OriginId;
import rocks.xmpp.extensions.sid.model.StanzaId;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class StanzaIdTest extends XmlTest {

    protected StanzaIdTest() throws JAXBException, XMLStreamException {
        super(ClientMessage.class, StanzaId.class);
    }

    @Test
    public void unmarshalStanzaId() throws XMLStreamException, JAXBException {
        String xml = "<message xmlns='jabber:client'\n" +
                "         to='room@muc.example.org'\n" +
                "         type='groupchat'>\n" +
                "  <body>Typical body text</body>\n" +
                "  <stanza-id xmlns='urn:xmpp:sid:0'\n" +
                "           id='de305d54-75b4-431b-adb2-eb6b9e546013'\n" +
                "           by='room@muc.xmpp.org'/>\n" +
                "</message>";

        Message message = unmarshal(xml, Message.class);
        StanzaId stanzaId = message.getExtension(StanzaId.class);
        Assert.assertNotNull(stanzaId);
        Assert.assertEquals(stanzaId.getId(), "de305d54-75b4-431b-adb2-eb6b9e546013");
        Assert.assertEquals(stanzaId.getBy(), Jid.of("room@muc.xmpp.org"));
    }

    @Test
    public void unmarshalOriginId() throws XMLStreamException, JAXBException {
        String xml = "<message xmlns='jabber:client'\n" +
                "         to='room@muc.example.org'\n" +
                "         type='groupchat'>\n" +
                "  <body>Typical body text</body>\n" +
                "  <origin-id xmlns='urn:xmpp:sid:0' id='de305d54-75b4-431b-adb2-eb6b9e546013'/>\n" +
                "</message>";

        Message message = unmarshal(xml, Message.class);
        OriginId stanzaId = message.getExtension(OriginId.class);
        Assert.assertNotNull(stanzaId);
        Assert.assertEquals(stanzaId.getId(), "de305d54-75b4-431b-adb2-eb6b9e546013");
    }
}