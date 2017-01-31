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

package rocks.xmpp.extensions.chatmarkers;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.extensions.chatmarkers.model.ChatMarker;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class ChatMarkersTest extends XmlTest {

    protected ChatMarkersTest() throws JAXBException, XMLStreamException {
        super(ClientMessage.class, ChatMarker.class);
    }

    @Test
    public void unmarshalMarkable() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='northumberland@shakespeare.lit/westminster'\n" +
                "    id='message-1'\n" +
                "    to='kingrichard@royalty.england.lit/throne'>\n" +
                "  <thread>sleeping</thread>\n" +
                "  <body>My lord, dispatch; read o'er these articles.</body>\n" +
                "  <markable xmlns='urn:xmpp:chat-markers:0'/>\n" +
                "</message>\n";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getExtensions().size(), 1);
        Assert.assertTrue(message.getExtension(ChatMarker.class) == ChatMarker.MARKABLE);
    }

    @Test
    public void unmarshalReceived() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='kingrichard@royalty.england.lit/throne'\n" +
                "    id='message-2'\n" +
                "    to='northumberland@shakespeare.lit/westminster'>\n" +
                "  <thread>sleeping</thread>\n" +
                "  <received xmlns='urn:xmpp:chat-markers:0' \n" +
                "               id='message-1'/>\n" +
                "</message>";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getExtensions().size(), 1);
        ChatMarker chatMarker = message.getExtension(ChatMarker.class);
        Assert.assertTrue(chatMarker instanceof ChatMarker.Received);
        Assert.assertEquals(((ChatMarker.Received) chatMarker).getId(), "message-1");
    }

    @Test
    public void unmarshalDisplayed() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='kingrichard@royalty.england.lit/throne'\n" +
                "    id='message-2'\n" +
                "    to='northumberland@shakespeare.lit/westminster'>\n" +
                "  <thread>sleeping</thread>\n" +
                "  <displayed xmlns='urn:xmpp:chat-markers:0' \n" +
                "               id='message-1'/>\n" +
                "</message>";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getExtensions().size(), 1);
        ChatMarker chatMarker = message.getExtension(ChatMarker.class);
        Assert.assertTrue(chatMarker instanceof ChatMarker.Displayed);
        Assert.assertEquals(((ChatMarker.Displayed) chatMarker).getId(), "message-1");
    }

    @Test
    public void unmarshalAcknowledged() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='kingrichard@royalty.england.lit/throne'\n" +
                "    id='message-2'\n" +
                "    to='northumberland@shakespeare.lit/westminster'>\n" +
                "  <thread>sleeping</thread>\n" +
                "  <acknowledged xmlns='urn:xmpp:chat-markers:0' \n" +
                "               id='message-1'/>\n" +
                "</message>";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getExtensions().size(), 1);
        ChatMarker chatMarker = message.getExtension(ChatMarker.class);
        Assert.assertTrue(chatMarker instanceof ChatMarker.Acknowledged);
        Assert.assertEquals(((ChatMarker.Acknowledged) chatMarker).getId(), "message-1");
    }

    @Test
    public void marshalDisplayed() throws XMLStreamException, JAXBException {
        ChatMarker chatMarker = new ChatMarker.Displayed("msg-1");
        Assert.assertEquals(marshal(chatMarker), "<displayed xmlns=\"urn:xmpp:chat-markers:0\" id=\"msg-1\"></displayed>");
    }
}
