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
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.pubsub.owner.PubSubOwner;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URI;

/**
 * @author Christian Schudt
 */
public class PubSubOwnerUseCasesTest extends BaseTest {

    @Test
    public void marshalCreate() throws JAXBException, XMLStreamException, IOException {
        PubSub pubSub = new PubSub(new PubSub.Create("princely_musings"), null);
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><create node=\"princely_musings\"></create></pubsub>");
    }

    @Test
    public void marshalCreateInstantNode() throws JAXBException, XMLStreamException, IOException {
        PubSub pubSub = new PubSub(new PubSub.Create(null), null);
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><create></create></pubsub>");
    }

    @Test
    public void marshalConfigure() throws JAXBException, XMLStreamException, IOException {
        PubSub pubSub = new PubSub(new Configure("princely_musings"));
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><configure node=\"princely_musings\"></configure></pubsub>");
    }

    @Test
    public void marshalDefault() throws JAXBException, XMLStreamException, IOException {
        PubSub pubSub = PubSub.forRequestDefault();
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><default></default></pubsub>");
    }

    @Test
    public void marshalDeleteNode() throws JAXBException, XMLStreamException, IOException {
        PubSubOwner pubSub = PubSubOwner.forDelete("princely_musings");
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub#owner\"><delete node=\"princely_musings\"></delete></pubsub>");
    }

    @Test
    public void marshalDeleteNodeWithRedirect() throws JAXBException, XMLStreamException, IOException {
        PubSubOwner pubSub = PubSubOwner.forDelete("princely_musings", URI.create("xmpp:hamlet@denmark.lit?;node=blog"));
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub#owner\"><delete node=\"princely_musings\"><redirect uri=\"xmpp:hamlet@denmark.lit?;node=blog\"></redirect></delete></pubsub>");
    }

    @Test
    public void marshalPurgeNodes() throws JAXBException, XMLStreamException, IOException {
        PubSubOwner pubSub = PubSubOwner.forPurge("princely_musings");
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub#owner\"><purge node=\"princely_musings\"></purge></pubsub>");
    }

    @Test
    public void marshalPubSubOwnerSubscriptions() throws JAXBException, XMLStreamException, IOException {
        PubSubOwner pubSub = PubSubOwner.forSubscriptions("princely_musings");
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub#owner\"><subscriptions node=\"princely_musings\"></subscriptions></pubsub>");
    }

    @Test
    public void unmarshalPubSubOwnerSubscriptions() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='subman1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>\n" +
                "    <subscriptions node='princely_musings'>\n" +
                "      <subscription jid='hamlet@denmark.lit' subscription='subscribed'/>\n" +
                "      <subscription jid='polonius@denmark.lit' subscription='unconfigured'/>\n" +
                "      <subscription jid='bernardo@denmark.lit' subscription='subscribed' subid='123-abc'/>\n" +
                "      <subscription jid='bernardo@denmark.lit' subscription='subscribed' subid='004-yyy'/>\n" +
                "    </subscriptions>\n" +
                "  </pubsub>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        PubSubOwner pubSubOwner = iq.getExtension(PubSubOwner.class);
        Assert.assertNotNull(pubSubOwner);
        Assert.assertTrue(pubSubOwner.isSubscriptions());
        Assert.assertEquals(pubSubOwner.getNode(), "princely_musings");
        Assert.assertEquals(pubSubOwner.getSubscriptions().size(), 4);
    }

    @Test
    public void unmarshalPubSubPublishSuccess() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/blogbot'\n" +
                "    id='publish1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <publish node='princely_musings'>\n" +
                "      <item id='ae890ac52d0df67ed7cfdf51b644e901'/>\n" +
                "    </publish>\n" +
                "  </pubsub>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        PubSub pubSub = iq.getExtension(PubSub.class);
        Assert.assertNotNull(pubSub);
        Assert.assertNotNull(pubSub.getPublish());
        Assert.assertEquals(pubSub.getPublish().getNode(), "princely_musings");
        Assert.assertNotNull(pubSub.getPublish().getItem());
    }

    @Test
    public void marshalDeleteItem() throws JAXBException, XMLStreamException, IOException {
        PubSub pubSub = new PubSub(new RetractElement("princely_musings", new ItemElement("ae890ac52d0df67ed7cfdf51b644e901"), false));
        String xml = marshall(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><retract notify=\"false\" node=\"princely_musings\"><item id=\"ae890ac52d0df67ed7cfdf51b644e901\"></item></retract></pubsub>");
    }

    @Test
    public void unmarshalPubSubOwnerConfigure() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='config1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>\n" +
                "    <configure node='princely_musings'>\n" +
                "      <x xmlns='jabber:x:data' type='form'>\n" +
                "        <field var='FORM_TYPE' type='hidden'>\n" +
                "          <value>http://jabber.org/protocol/pubsub#node_config</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#title' type='text-single'\n" +
                "               label='A friendly name for the node'/>\n" +
                "        <field var='pubsub#deliver_notifications' type='boolean'\n" +
                "               label='Whether to deliver event notifications'>\n" +
                "          <value>true</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#deliver_payloads' type='boolean'\n" +
                "               label='Whether to deliver payloads with event notifications'>\n" +
                "          <value>true</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notify_config' type='boolean'\n" +
                "               label='Notify subscribers when the node configuration changes'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notify_delete' type='boolean'\n" +
                "               label='Notify subscribers when the node is deleted'>\n" +
                "          <value>false</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notify_retract' type='boolean'\n" +
                "               label='Notify subscribers when items are removed from the node'>\n" +
                "          <value>false</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notify_sub' type='boolean'\n" +
                "               label='Notify owners about new subscribers and unsubscribes'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#persist_items' type='boolean'\n" +
                "               label='Persist items to storage'>\n" +
                "          <value>1</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#max_items' type='text-single'\n" +
                "               label='Max # of items to persist'>\n" +
                "          <value>10</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#item_expire' type='text-single'\n" +
                "               label='Time after which to automatically purge items'>\n" +
                "          <value>604800</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#subscribe' type='boolean'\n" +
                "               label='Whether to allow subscriptions'>\n" +
                "          <value>1</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#access_model' type='list-single'\n" +
                "               label='Specify the subscriber model'>\n" +
                "          <option><value>authorize</value></option>\n" +
                "          <option><value>open</value></option>\n" +
                "          <option><value>presence</value></option>\n" +
                "          <option><value>roster</value></option>\n" +
                "          <option><value>whitelist</value></option>\n" +
                "          <value>open</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#roster_groups_allowed' type='list-multi'\n" +
                "               label='Roster groups allowed to subscribe'>\n" +
                "          <option><value>friends</value></option>\n" +
                "          <option><value>courtiers</value></option>\n" +
                "          <option><value>servants</value></option>\n" +
                "          <option><value>enemies</value></option>\n" +
                "        </field>\n" +
                "        <field var='pubsub#publish_model' type='list-single'\n" +
                "               label='Specify the publisher model'>\n" +
                "          <option><value>publishers</value></option>\n" +
                "          <option><value>subscribers</value></option>\n" +
                "          <option><value>open</value></option>\n" +
                "          <value>publishers</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#purge_offline' type='boolean'\n" +
                "               label='Purge all items when the relevant publisher goes offline?'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#max_payload_size' type='text-single'\n" +
                "               label='Max Payload size in bytes'>\n" +
                "          <value>1028</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#send_last_published_item' type='list-single'\n" +
                "               label='When to send the last published item'>\n" +
                "          <option label='Never'><value>never</value></option>\n" +
                "          <option label='When a new subscription is processed'><value>on_sub</value></option>\n" +
                "          <option label='When a new subscription is processed and whenever a subscriber comes online'>\n" +
                "            <value>on_sub_and_presence</value>\n" +
                "          </option>\n" +
                "          <value>never</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#presence_based_delivery' type='boolean'\n" +
                "               label='Deliver event notifications only to available users'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notification_type' type='list-single'\n" +
                "               label='Specify the delivery style for event notifications'>\n" +
                "          <option><value>normal</value></option>\n" +
                "          <option><value>headline</value></option>\n" +
                "          <value>headline</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#type' type='text-single'\n" +
                "               label='Specify the type of payload data to be provided at this node'>\n" +
                "          <value>http://www.w3.org/2005/Atom</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#dataform_xslt' type='text-single'\n" +
                "               label='Payload XSLT'/>\n" +
                "      </x>\n" +
                "    </configure>\n" +
                "  </pubsub>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        PubSubOwner pubSubOwner = iq.getExtension(PubSubOwner.class);
        Assert.assertNotNull(pubSubOwner);
        Assert.assertTrue(pubSubOwner.isConfigure());
        Assert.assertNotNull(pubSubOwner.getConfiguration());
        Assert.assertEquals(pubSubOwner.getNode(), "princely_musings");
    }

    @Test
    public void unmarshalPubSubOwnerDefault() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='def1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>\n" +
                "    <default>\n" +
                "      <x xmlns='jabber:x:data' type='form'>\n" +
                "        <field var='FORM_TYPE' type='hidden'>\n" +
                "           <value>http://jabber.org/protocol/pubsub#node_config</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#title' type='text-single'\n" +
                "               label='A friendly name for the node'/>\n" +
                "        <field var='pubsub#deliver_notifications' type='boolean'\n" +
                "               label='Deliver event notifications'>\n" +
                "          <value>true</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#deliver_payloads' type='boolean'\n" +
                "             label='Deliver payloads with event notifications'>\n" +
                "          <value>1</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#description' type='text-single'\n" +
                "             label='A description of the node'/>\n" +
                "        <field var='pubsub#notify_config' type='boolean'\n" +
                "               label='Notify subscribers when the node configuration changes'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notify_delete' type='boolean'\n" +
                "               label='Notify subscribers when the node is deleted'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notify_retract' type='boolean'\n" +
                "               label='Notify subscribers when items are removed from the node'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notify_sub' type='boolean'\n" +
                "               label='Notify owners about new subscribers and unsubscribes'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#persist_items' type='boolean'\n" +
                "               label='Persist items to storage'>\n" +
                "          <value>1</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#max_items' type='text-single'\n" +
                "               label='Max # of items to persist'>\n" +
                "          <value>10</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#item_expire' type='text-single'\n" +
                "               label='Time after which to automatically purge items'>\n" +
                "          <value>604800</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#subscribe' type='boolean'\n" +
                "               label='Whether to allow subscriptions'>\n" +
                "          <value>1</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#access_model' type='list-single'\n" +
                "               label='Specify the subscriber model'>\n" +
                "          <option><value>authorize</value></option>\n" +
                "          <option><value>open</value></option>\n" +
                "          <option><value>presence</value></option>\n" +
                "          <option><value>roster</value></option>\n" +
                "          <option><value>whitelist</value></option>\n" +
                "          <value>open</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#roster_groups_allowed' type='list-multi'\n" +
                "               label='Roster groups allowed to subscribe'>\n" +
                "          <option><value>friends</value></option>\n" +
                "          <option><value>courtiers</value></option>\n" +
                "          <option><value>servants</value></option>\n" +
                "          <option><value>enemies</value></option>\n" +
                "        </field>\n" +
                "        <field var='pubsub#publish_model' type='list-single'\n" +
                "               label='Specify the publisher model'>\n" +
                "          <option><value>publishers</value></option>\n" +
                "          <option><value>subscribers</value></option>\n" +
                "          <option><value>open</value></option>\n" +
                "          <value>publishers</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#purge_offline' type='boolean'\n" +
                "               label='Purge all items when the relevant publisher goes offline?'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#max_payload_size' type='text-single'\n" +
                "               label='Max payload size in bytes'>\n" +
                "          <value>9216</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#send_last_published_item' type='list-single'\n" +
                "               label='When to send the last published item'>\n" +
                "          <option label='Never'><value>never</value></option>\n" +
                "          <option label='When a new subscription is processed'><value>on_sub</value></option>\n" +
                "          <option label='When a new subscription is processed and whenever a subscriber comes online'>\n" +
                "            <value>on_sub_and_presence</value>\n" +
                "          </option>\n" +
                "          <value>never</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#presence_based_delivery' type='boolean'\n" +
                "               label='Deliver notifications only to available users'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#notification_type' type='list-single'\n" +
                "               label='Specify the delivery style for notifications'>\n" +
                "          <option><value>normal</value></option>\n" +
                "          <option><value>headline</value></option>\n" +
                "          <value>headline</value>\n" +
                "        </field>\n" +
                "      </x>\n" +
                "    </default>\n" +
                "  </pubsub>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        PubSubOwner pubSubOwner = iq.getExtension(PubSubOwner.class);
        Assert.assertNotNull(pubSubOwner);
        Assert.assertTrue(pubSubOwner.isDefault());
        Assert.assertNotNull(pubSubOwner.getDefaultConfiguration());

    }

    @Test
    public void unmarshalPubSubOwnerDelete() throws XMLStreamException, JAXBException {
        String xml = "<iq type='set'\n" +
                "    from='hamlet@denmark.lit/elsinore'\n" +
                "    to='pubsub.shakespeare.lit'\n" +
                "    id='delete1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>\n" +
                "    <delete node='princely_musings'>\n" +
                "      <redirect uri='xmpp:hamlet@denmark.lit?;node=blog'/>\n" +
                "    </delete>\n" +
                "  </pubsub>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        PubSubOwner pubSubOwner = iq.getExtension(PubSubOwner.class);
        Assert.assertNotNull(pubSubOwner);
        Assert.assertTrue(pubSubOwner.isDelete());
        Assert.assertEquals(pubSubOwner.getNode(), "princely_musings");
        Assert.assertEquals(pubSubOwner.getRedirectUri(), URI.create("xmpp:hamlet@denmark.lit?;node=blog"));
    }

    @Test
    public void unmarshalPubSubOwnerPurge() throws XMLStreamException, JAXBException {
        String xml = "<iq type='set'\n" +
                "    from='hamlet@denmark.lit/elsinore'\n" +
                "    to='pubsub.shakespeare.lit'\n" +
                "    id='purge1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>\n" +
                "    <purge node='princely_musings'/>\n" +
                "  </pubsub>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        PubSubOwner pubSubOwner = iq.getExtension(PubSubOwner.class);
        Assert.assertNotNull(pubSubOwner);
        Assert.assertTrue(pubSubOwner.isPurge());
        Assert.assertEquals(pubSubOwner.getNode(), "princely_musings");
    }

    @Test
    public void unmarshalPubSubOwnerAffiliations() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='ent1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>\n" +
                "    <affiliations node='princely_musings'>\n" +
                "      <affiliation jid='hamlet@denmark.lit' affiliation='owner'/>\n" +
                "      <affiliation jid='polonius@denmark.lit' affiliation='outcast'/>\n" +
                "    </affiliations>\n" +
                "  </pubsub>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        PubSubOwner pubSubOwner = iq.getExtension(PubSubOwner.class);
        Assert.assertNotNull(pubSubOwner);
        Assert.assertNotNull(pubSubOwner.getAffiliations());
        Assert.assertEquals(pubSubOwner.getNode(), "princely_musings");
        Assert.assertEquals(pubSubOwner.getAffiliations().size(), 2);
        Assert.assertEquals(pubSubOwner.getAffiliations().get(0).getJid(), Jid.fromString("hamlet@denmark.lit"));
        Assert.assertEquals(pubSubOwner.getAffiliations().get(0).getAffiliation(), Affiliation.OWNER);

        Assert.assertEquals(pubSubOwner.getAffiliations().get(1).getJid(), Jid.fromString("polonius@denmark.lit"));
        Assert.assertEquals(pubSubOwner.getAffiliations().get(1).getAffiliation(), Affiliation.OUTCAST);
    }

    @Test
    public void marshalPubSubOwnerDelete() throws JAXBException, XMLStreamException, IOException {
        PubSubOwner pubSubOwner = PubSubOwner.forDelete("test");
        String xml = marshall(pubSubOwner);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub#owner\"><delete node=\"test\"></delete></pubsub>");
    }
}
