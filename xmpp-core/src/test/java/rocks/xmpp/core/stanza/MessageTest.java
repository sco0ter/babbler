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

package rocks.xmpp.core.stanza;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.AbstractMessage;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.errors.Conflict;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Christian Schudt
 */
public class MessageTest extends XmlTest {

    protected MessageTest() throws JAXBException, XMLStreamException {
        super(Message.class);
    }

    @Test
    public void unmarshalSingleBody() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='juliet@example.com/balcony'\n" +
                "    id='b4vs9km4'\n" +
                "    to='romeo@example.net'\n" +
                "    type='chat'\n" +
                "    xml:lang='en'>\n" +
                "  <body>Wherefore art thou, Romeo?</body>\n" +
                "</message>";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getFrom().toString(), "juliet@example.com/balcony");
        Assert.assertEquals(message.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(message.getType(), Message.Type.CHAT);
        Assert.assertEquals(message.getId(), "b4vs9km4");
        Assert.assertEquals(message.getLanguage(), "en");
        Assert.assertEquals(message.getBody(), "Wherefore art thou, Romeo?");
    }

    @Test
    public void unmarshalMultipleBodies() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='juliet@example.com/balcony'\n" +
                "    id='z94nb37h'\n" +
                "    to='romeo@example.net'\n" +
                "    type='chat'\n" +
                "    xml:lang='en'>\n" +
                "  <body>Wherefore art thou, Romeo?</body>\n" +
                "  <body xml:lang='de'>Wo bist du, Romeo?</body>\n" +
                "</message>";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getFrom().toString(), "juliet@example.com/balcony");
        Assert.assertEquals(message.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(message.getType(), Message.Type.CHAT);
        Assert.assertEquals(message.getId(), "z94nb37h");
        Assert.assertEquals(message.getLanguage(), "en");
        Assert.assertEquals(message.getBody(), "Wherefore art thou, Romeo?");
        Assert.assertEquals(message.getBodies().get(1).getText(), "Wo bist du, Romeo?");
    }

    @Test
    public void unmarshalSingleSubject() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='juliet@example.com/balcony'\n" +
                "    id='c8xg3nf8'\n" +
                "    to='romeo@example.net'\n" +
                "    type='chat'\n" +
                "    xml:lang='en'>\n" +
                "  <subject>I implore you!</subject>\n" +
                "  <body>Wherefore art thou, Romeo?</body>\n" +
                "</message>";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getFrom().toString(), "juliet@example.com/balcony");
        Assert.assertEquals(message.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(message.getType(), Message.Type.CHAT);
        Assert.assertEquals(message.getId(), "c8xg3nf8");
        Assert.assertEquals(message.getLanguage(), "en");
        Assert.assertEquals(message.getSubject(), "I implore you!");
        Assert.assertEquals(message.getBody(), "Wherefore art thou, Romeo?");
    }

    @Test
    public void unmarshalMultipleSubjects() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='juliet@example.com/balcony'\n" +
                "    id='jk3v47gw'\n" +
                "    to='romeo@example.net'\n" +
                "    type='chat'\n" +
                "    xml:lang='en'>\n" +
                "  <subject>I implore you!</subject>\n" +
                "  <subject xml:lang='de'>Ich flehe dich an!</subject>\n" +
                "  <body>Wherefore art thou, Romeo?</body>\n" +
                "  <body xml:lang='cs'>Wo bist du, Romeo?</body>\n" +
                "</message>";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getFrom().toString(), "juliet@example.com/balcony");
        Assert.assertEquals(message.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(message.getType(), Message.Type.CHAT);
        Assert.assertEquals(message.getId(), "jk3v47gw");
        Assert.assertEquals(message.getLanguage(), "en");
        Assert.assertEquals(message.getSubject(), "I implore you!");
        Assert.assertEquals(message.getSubjects().get(1).getText(), "Ich flehe dich an!");
        Assert.assertEquals(message.getBody(), "Wherefore art thou, Romeo?");
        Assert.assertEquals(message.getBodies().get(1).getText(), "Wo bist du, Romeo?");
    }

    @Test
    public void unmarshalThread() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='juliet@example.com/balcony'\n" +
                "    to='romeo@example.net'\n" +
                "    type='chat'\n" +
                "    xml:lang='en'>\n" +
                "  <thread parent='e0ffe42b28561960c6b12b944a092794b9683a38'>0e3141cd80894871a68e6fe6b1ec56fa</thread>\n" +
                "</message>";
        Message message = unmarshal(xml, Message.class);
        Assert.assertEquals(message.getFrom().toString(), "juliet@example.com/balcony");
        Assert.assertEquals(message.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(message.getType(), Message.Type.CHAT);
        Assert.assertNull(message.getId());
        Assert.assertEquals(message.getLanguage(), "en");
        Assert.assertEquals(message.getThread(), "0e3141cd80894871a68e6fe6b1ec56fa");
        Assert.assertEquals(message.getParentThread(), "e0ffe42b28561960c6b12b944a092794b9683a38");
    }

    @Test
    public void marshalChatMessage() throws JAXBException, XMLStreamException {
        Message message = new Message(Jid.valueOf("juliet@example.com"), Message.Type.CHAT);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message to=\"juliet@example.com\" type=\"chat\"></message>");
    }

    @Test
    public void marshalGroupChatMessage() throws JAXBException, XMLStreamException {
        Message message = new Message(Jid.valueOf("juliet@example.com"), Message.Type.GROUPCHAT);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message to=\"juliet@example.com\" type=\"groupchat\"></message>");
    }

    @Test
    public void marshalNormalMessage() throws JAXBException, XMLStreamException {
        Message message = new Message(Jid.valueOf("juliet@example.com"), Message.Type.NORMAL);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message to=\"juliet@example.com\" type=\"normal\"></message>");
    }

    @Test
    public void marshalHeadlineMessage() throws JAXBException, XMLStreamException {
        Message message = new Message(Jid.valueOf("juliet@example.com"), Message.Type.HEADLINE);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message to=\"juliet@example.com\" type=\"headline\"></message>");
    }

    @Test
    public void marshalErrorMessage() throws JAXBException, XMLStreamException {
        Message message = new Message(Jid.valueOf("juliet@example.com"), Message.Type.ERROR, "test", null, null, null, null, null, null, new StanzaError(new Conflict()));
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message to=\"juliet@example.com\" type=\"error\"><body>test</body><error type=\"cancel\"><conflict xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"></conflict></error></message>");
    }

    @Test
    public void marshalMessage() throws JAXBException, XMLStreamException {
        Message message = new Message(new Jid("to", "domain"), Message.Type.CHAT, Collections.<AbstractMessage.Body>emptyList(), null, null, null, "id", new Jid("from", "domain"), null, null);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"chat\"></message>");
    }

    @Test
    public void marshalMessageThread() throws JAXBException, XMLStreamException {
        Message message = new Message(new Jid("to", "domain"), Message.Type.CHAT, Collections.<Message.Body>emptyList(), null, "thread", null, "id", new Jid("from", "domain"), null, null);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"chat\"><thread>thread</thread></message>");
    }

    @Test
    public void marshalMessageParentThread() throws JAXBException, XMLStreamException {
        Message message = new Message(new Jid("to", "domain"), Message.Type.CHAT, Collections.<Message.Body>emptyList(), null, "thread", "parentThread", "id", new Jid("from", "domain"), null, null);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"chat\"><thread parent=\"parentThread\">thread</thread></message>");
    }

    @Test
    public void marshalMessageParentThreadWithoutThread() throws JAXBException, XMLStreamException {
        Message message = new Message(new Jid("to", "domain"), Message.Type.CHAT, Collections.<Message.Body>emptyList(), null, null, "parentThread", "id", new Jid("from", "domain"), null, null);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"chat\"><thread parent=\"parentThread\"></thread></message>");
    }

    @Test
    public void marshalMessageBody() throws JAXBException, XMLStreamException {
        Message message = new Message(new Jid("to", "domain"), Message.Type.CHAT, Arrays.asList(new AbstractMessage.Body("body", "de"), new AbstractMessage.Body("body2", "fr")), null, null, null, "id", new Jid("from", "domain"), null, null);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"chat\"><body xml:lang=\"de\">body</body><body xml:lang=\"fr\">body2</body></message>");
    }

    @Test
    public void marshalMessageSubject() throws JAXBException, XMLStreamException {
        Message message = new Message(new Jid("to", "domain"), Message.Type.CHAT, null, Arrays.asList(new AbstractMessage.Subject("subject1", "de"), new AbstractMessage.Subject("subject2", "fr")), null, null, "id", new Jid("from", "domain"), null, null);
        String xml = marshal(message);
        Assert.assertEquals(xml, "<message from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"chat\"><subject xml:lang=\"de\">subject1</subject><subject xml:lang=\"fr\">subject2</subject></message>");
    }
}
