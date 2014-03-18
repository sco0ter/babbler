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

package org.xmpp.extension.pubsub.event;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.pubsub.SubscriptionState;
import org.xmpp.stanza.Message;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.net.URI;

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
        Event pubSubEvent = message.getExtension(Event.class);
        Assert.assertNotNull(pubSubEvent);
        Assert.assertNotNull(pubSubEvent.getItems());
        Assert.assertEquals(pubSubEvent.getNode(), "princely_musings");
        Assert.assertEquals(pubSubEvent.getItems().size(), 1);
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
        Event event = message.getExtension(Event.class);
        Assert.assertNotNull(event);
        Assert.assertNotNull(event.getItems());
        //Assert.assertNotNull(event.getItems().getRetract());
        //Assert.assertEquals(event.getItems().getRetract().getId(), "ae890ac52d0df67ed7cfdf51b644e901");
    }

    @Test
    public void unmarshalPubSubEventConfiguration() throws XMLStreamException, JAXBException {
        String xml = "<message from='pubsub.shakespeare.lit' to='francisco@denmark.lit' id='foo'>\n" +
                "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <configuration node='princely_musings'/>\n" +
                "  </event>\n" +
                "</message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(message);
        Event event = message.getExtension(Event.class);
        Assert.assertNotNull(event);
        Assert.assertTrue(event.isConfiguration());
        Assert.assertEquals(event.getNode(), "princely_musings");
    }

    @Test
    public void unmarshalPubSubEventConfigurationForm() throws XMLStreamException, JAXBException {
        String xml = "<message from='pubsub.shakespeare.lit' to='francisco@denmark.lit' id='foo'>\n" +
                "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <configuration node='princely_musings'>\n" +
                "      <x xmlns='jabber:x:data' type='result'>\n" +
                "        <field var='FORM_TYPE' type='hidden'>\n" +
                "          <value>http://jabber.org/protocol/pubsub#node_config</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#title'><value>Princely Musings (Atom)</value></field>\n" +
                "        <field var='pubsub#deliver_notifications'><value>1</value></field>\n" +
                "        <field var='pubsub#deliver_payloads'><value>1</value></field>\n" +
                "        <field var='pubsub#notify_config'><value>0</value></field>\n" +
                "        <field var='pubsub#notify_delete'><value>0</value></field>\n" +
                "        <field var='pubsub#notify_retract'><value>0</value></field>\n" +
                "        <field var='pubsub#notify_sub'><value>0</value></field>\n" +
                "        <field var='pubsub#persist_items'><value>1</value></field>\n" +
                "        <field var='pubsub#max_items'><value>10</value></field>\n" +
                "        <field var='pubsub#item_expire'><value>604800</value></field>\n" +
                "        <field var='pubsub#subscribe'><value>1</value></field>\n" +
                "        <field var='pubsub#access_model'><value>open</value></field>\n" +
                "        <field var='pubsub#publish_model'><value>publishers</value></field>\n" +
                "        <field var='pubsub#purge_offline'><value>0</value></field>\n" +
                "        <field var='pubsub#max_payload_size'><value>9216</value></field>\n" +
                "        <field var='pubsub#send_last_published_item'><value>never</value></field>\n" +
                "        <field var='pubsub#presence_based_delivery'><value>0</value></field>\n" +
                "        <field var='pubsub#notification_type'><value>headline</value></field>\n" +
                "        <field var='pubsub#type'><value>http://www.w3.org/2005/Atom</value></field>\n" +
                "        <field var='pubsub#body_xslt'>\n" +
                "          <value>http://jabxslt.jabberstudio.org/atom_body.xslt</value>\n" +
                "        </field>\n" +
                "      </x>\n" +
                "    </configuration>\n" +
                "  </event>\n" +
                "</message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(message);
        Event event = message.getExtension(Event.class);
        Assert.assertNotNull(event);
        Assert.assertTrue(event.isConfiguration());
        Assert.assertEquals(event.getNode(), "princely_musings");
        Assert.assertNotNull(event.getConfigurationForm());
    }

    @Test
    public void unmarshalPubSubEventDelete() throws XMLStreamException, JAXBException {
        String xml = "<message from='pubsub.shakespeare.lit' to='francisco@denmark.lit' id='foo'>\n" +
                "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <delete node='princely_musings'>\n" +
                "      <redirect uri='xmpp:hamlet@denmark.lit?;node=blog'/>\n" +
                "    </delete>\n" +
                "  </event>\n" +
                "</message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(message);
        Event event = message.getExtension(Event.class);
        Assert.assertNotNull(event);
        Assert.assertTrue(event.isDelete());
        Assert.assertEquals(event.getNode(), "princely_musings");
        Assert.assertNotNull(event.getRedirectUri());
        Assert.assertEquals(event.getRedirectUri(), URI.create("xmpp:hamlet@denmark.lit?;node=blog"));
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
        Event event = message.getExtension(Event.class);
        Assert.assertNotNull(event);
        Assert.assertNotNull(event.isPurge());
        Assert.assertEquals(event.getNode(), "princely_musings");
    }

    @Test
    public void unmarshalPubSubEventSubscription() throws XMLStreamException, JAXBException {
        String xml = "<message from='pubsub.shakespeare.lit' to='francisco@denmark.lit/barracks'>\n" +
                "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <subscription\n" +
                "        expiry='2006-02-28T23:59:00Z'\n" +
                "        jid='francisco@denmark.lit'\n" +
                "        node='princely_musings'\n" +
                "        subid='ba49252aaa4f5d320c24d3766f0bdcade78c78d3'\n" +
                "        subscription='subscribed'/>\n" +
                "  </event>\n" +
                "</message>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(message);
        Event event = message.getExtension(Event.class);
        Assert.assertNotNull(event);
        Assert.assertNotNull(event.getSubscription());
        Assert.assertNotNull(event.getSubscription().getExpiry());
        Assert.assertEquals(event.getSubscription().getNode(), "princely_musings");
        Assert.assertEquals(event.getSubscription().getJid(), Jid.valueOf("francisco@denmark.lit"));
        Assert.assertEquals(event.getSubscription().getSubscriptionState(), SubscriptionState.SUBSCRIBED);
        Assert.assertEquals(event.getSubscription().getSubId(), "ba49252aaa4f5d320c24d3766f0bdcade78c78d3");
    }
}
