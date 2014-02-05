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

package org.xmpp.extension.pubsub;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.stanza.Message;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class PubSubEventTest extends BaseTest {

    @Test
    public void unmarshalPubSubEventItems() throws XMLStreamException, JAXBException {
        String xml = "<message from='pubsub.shakespeare.lit' to='francisco@denmark.lit' id='foo'>\n" +
                "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <items node='princely_musings'>\n" +
                "      <item id='ae890ac52d0df67ed7cfdf51b644e901'>\n" +
                "      </item>\n" +
                "    </items>\n" +
                "  </event>\n" +
                "</message>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(message);
        PubSubEvent pubSubEvent = message.getExtension(PubSubEvent.class);
        Assert.assertNotNull(pubSubEvent);
        Assert.assertNotNull(pubSubEvent.getItems());
        Assert.assertEquals(pubSubEvent.getItems().getNode(), "princely_musings");
        Assert.assertEquals(pubSubEvent.getItems().getItems().size(), 1);
    }

    @Test
    public void unmarshalPubSubEventRetract() throws XMLStreamException, JAXBException {
        String xml = "<message from='pubsub.shakespeare.lit' to='francisco@denmark.lit' id='foo'>\n" +
                "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <items node='princely_musings'>\n" +
                "      <retract id='ae890ac52d0df67ed7cfdf51b644e901'/>\n" +
                "    </items>\n" +
                "  </event>\n" +
                "</message>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(message);
        PubSubEvent pubSubEvent = message.getExtension(PubSubEvent.class);
        Assert.assertNotNull(pubSubEvent);
        Assert.assertNotNull(pubSubEvent.getItems());
        Assert.assertNotNull(pubSubEvent.getItems().getRetract());
        Assert.assertEquals(pubSubEvent.getItems().getRetract().getId(), "ae890ac52d0df67ed7cfdf51b644e901");
    }

    @Test
    public void unmarshalPubSubEventPurge() throws XMLStreamException, JAXBException {
        String xml = "<message from='pubsub.shakespeare.lit' to='francisco@denmark.lit' id='foo'>\n" +
                "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <purge node='princely_musings'/>\n" +
                "  </event>\n" +
                "</message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(message);
        PubSubEvent pubSubEvent = message.getExtension(PubSubEvent.class);
        Assert.assertNotNull(pubSubEvent);
        Assert.assertNotNull(pubSubEvent.getPurge());
        Assert.assertNotNull(pubSubEvent.getPurge().getNode(), "princely_musings");
    }
}
