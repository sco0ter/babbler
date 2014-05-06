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
import org.xmpp.Jid;
import org.xmpp.XmlTest;
import org.xmpp.stanza.client.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class PubSubSubscriberUseCasesTest extends XmlTest {

    protected PubSubSubscriberUseCasesTest() throws JAXBException, XMLStreamException {
        super(IQ.class, PubSub.class);
    }

    @Test
    public void marshalSubscriptions() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withSubscribe("princely_musings", Jid.valueOf("francisco@denmark.lit"));
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><subscribe jid=\"francisco@denmark.lit\" node=\"princely_musings\"></subscribe></pubsub>");
    }

    @Test
    public void unmarshalPubSubSubscriptionSuccess() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <subscription\n" +
                "        node='princely_musings'\n" +
                "        jid='francisco@denmark.lit'\n" +
                "        subid='ba49252aaa4f5d320c24d3766f0bdcade78c78d3'\n" +
                "        subscription='subscribed'/>\n" +
                "  </pubsub>\n" +
                "</iq>\n";
        IQ iq = unmarshal(xml, IQ.class);
        PubSub pubSub = iq.getExtension(PubSub.class);
        Assert.assertNotNull(pubSub);
        Assert.assertNotNull(pubSub.getSubscription());
        Assert.assertEquals(pubSub.getSubscription().getNode(), "princely_musings");
        Assert.assertEquals(pubSub.getSubscription().getJid(), Jid.valueOf("francisco@denmark.lit"));
        Assert.assertEquals(pubSub.getSubscription().getSubId(), "ba49252aaa4f5d320c24d3766f0bdcade78c78d3");
        Assert.assertEquals(pubSub.getSubscription().getSubscriptionState(), SubscriptionState.SUBSCRIBED);
    }

    @Test
    public void unmarshalPubSubPending() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <subscription\n" +
                "        node='princely_musings'\n" +
                "        jid='francisco@denmark.lit'\n" +
                "        subscription='pending'/>\n" +
                "  </pubsub>\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        PubSub pubSub = iq.getExtension(PubSub.class);
        Assert.assertNotNull(pubSub);
        Assert.assertNotNull(pubSub.getSubscription());
        Assert.assertEquals(pubSub.getSubscription().getSubscriptionState(), SubscriptionState.PENDING);
    }

    @Test
    public void unmarshalPubSubConfigurationRequired() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <subscription\n" +
                "        node='princely_musings'\n" +
                "        jid='francisco@denmark.lit'\n" +
                "        subscription='unconfigured'>\n" +
                "      <subscribe-options>\n" +
                "        <required/>\n" +
                "      </subscribe-options>\n" +
                "    </subscription>\n" +
                "  </pubsub>\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        PubSub pubSub = iq.getExtension(PubSub.class);
        Assert.assertNotNull(pubSub);
        Assert.assertNotNull(pubSub.getSubscription());
        Assert.assertTrue(pubSub.getSubscription().isConfigurationRequired());
    }

    @Test
    public void unmarshalPubSubConfigurationSupported() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <subscription\n" +
                "        node='princely_musings'\n" +
                "        jid='francisco@denmark.lit'\n" +
                "        subscription='unconfigured'>\n" +
                "      <subscribe-options/>\n" +
                "    </subscription>\n" +
                "  </pubsub>\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        PubSub pubSub = iq.getExtension(PubSub.class);
        Assert.assertNotNull(pubSub);
        Assert.assertNotNull(pubSub.getSubscription());
        Assert.assertFalse(pubSub.getSubscription().isConfigurationRequired());
    }

    // TODO Example 48. Service returns error specifying that subscription configuration is required

    @Test
    public void marshalUnsubscribe() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withUnsubscribe("node6", Jid.valueOf("francisco@denmark.lit"));
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><unsubscribe jid=\"francisco@denmark.lit\" node=\"node6\"></unsubscribe></pubsub>");
    }

    @Test
    public void marshalRequestOptions() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withOptions("node6", Jid.valueOf("francisco@denmark.lit"));
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><options node=\"node6\" jid=\"francisco@denmark.lit\"></options></pubsub>");
    }

    // TODO  Example 68. Subscriber submits completed options form
    @Test
    public void marshalSubmitOptions() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withOptions("node6", Jid.valueOf("francisco@denmark.lit"));
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><options node=\"node6\" jid=\"francisco@denmark.lit\"></options></pubsub>");
    }

    @Test
    public void unmarshalPubSubOptions() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='options1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <options node='princely_musings' jid='francisco@denmark.lit'>\n" +
                "      <x xmlns='jabber:x:data' type='form'>\n" +
                "        <field var='FORM_TYPE' type='hidden'>\n" +
                "          <value>http://jabber.org/protocol/pubsub#subscribe_options</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#deliver' type='boolean'\n" +
                "               label='Enable delivery?'>\n" +
                "          <value>1</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#digest' type='boolean'\n" +
                "               label='Receive digest notifications (approx. one per day)?'>\n" +
                "          <value>0</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#include_body' type='boolean'\n" +
                "               label='Receive message body in addition to payload?'>\n" +
                "          <value>false</value>\n" +
                "        </field>\n" +
                "        <field\n" +
                "            var='pubsub#show-values'\n" +
                "            type='list-multi'\n" +
                "            label='Select the presence types which are\n" +
                "                   allowed to receive event notifications'>\n" +
                "          <option label='Want to Chat'><value>chat</value></option>\n" +
                "          <option label='Available'><value>online</value></option>\n" +
                "          <option label='Away'><value>away</value></option>\n" +
                "          <option label='Extended Away'><value>xa</value></option>\n" +
                "          <option label='Do Not Disturb'><value>dnd</value></option>\n" +
                "          <value>chat</value>\n" +
                "          <value>online</value>\n" +
                "        </field>\n" +
                "      </x>\n" +
                "    </options>\n" +
                "  </pubsub>\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        PubSub pubSub = iq.getExtension(PubSub.class);
        Assert.assertNotNull(pubSub);
        Assert.assertNotNull(pubSub.getOptions());
        Assert.assertNotNull(pubSub.getOptions().getDataForm());
    }

    @Test
    public void marshalDefault() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withDefault("node6");
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><default node=\"node6\"></default></pubsub>");
    }

    @Test
    public void marshalDefaultAllNodes() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withDefault();
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><default></default></pubsub>");
    }

    @Test
    public void marshalRequestAllItems() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withItems("princely_musings");
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><items node=\"princely_musings\"></items></pubsub>");
    }

    @Test
    public void unmarshalPubSubItems() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='items1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <items node='princely_musings'>\n" +
                "      <item id='368866411b877c30064a5f62b917cffe'>\n" +
                "        <entry xmlns='http://www.w3.org/2005/Atom'>\n" +
                "          <title>The Uses of This World</title>\n" +
                "          <summary>\n" +
                "O, that this too too solid flesh would melt\n" +
                "Thaw and resolve itself into a dew!\n" +
                "          </summary>\n" +
                "          <link rel='alternate' type='text/html'\n" +
                "                href='http://denmark.lit/2003/12/13/atom03'/>\n" +
                "          <id>tag:denmark.lit,2003:entry-32396</id>\n" +
                "          <published>2003-12-12T17:47:23Z</published>\n" +
                "          <updated>2003-12-12T17:47:23Z</updated>\n" +
                "        </entry>\n" +
                "      </item>\n" +
                "      <item id='3300659945416e274474e469a1f0154c'>\n" +
                "        <entry xmlns='http://www.w3.org/2005/Atom'>\n" +
                "          <title>Ghostly Encounters</title>\n" +
                "          <summary>\n" +
                "O all you host of heaven! O earth! what else?\n" +
                "And shall I couple hell? O, fie! Hold, hold, my heart;\n" +
                "And you, my sinews, grow not instant old,\n" +
                "But bear me stiffly up. Remember thee!\n" +
                "          </summary>\n" +
                "          <link rel='alternate' type='text/html'\n" +
                "                href='http://denmark.lit/2003/12/13/atom03'/>\n" +
                "          <id>tag:denmark.lit,2003:entry-32396</id>\n" +
                "          <published>2003-12-12T23:21:34Z</published>\n" +
                "          <updated>2003-12-12T23:21:34Z</updated>\n" +
                "        </entry>\n" +
                "      </item>\n" +
                "      <item id='4e30f35051b7b8b42abe083742187228'>\n" +
                "        <entry xmlns='http://www.w3.org/2005/Atom'>\n" +
                "          <title>Alone</title>\n" +
                "          <summary>\n" +
                "Now I am alone.\n" +
                "O, what a rogue and peasant slave am I!\n" +
                "          </summary>\n" +
                "          <link rel='alternate' type='text/html'\n" +
                "                href='http://denmark.lit/2003/12/13/atom03'/>\n" +
                "          <id>tag:denmark.lit,2003:entry-32396</id>\n" +
                "          <published>2003-12-13T11:09:53Z</published>\n" +
                "          <updated>2003-12-13T11:09:53Z</updated>\n" +
                "        </entry>\n" +
                "      </item>\n" +
                "      <item id='ae890ac52d0df67ed7cfdf51b644e901'>\n" +
                "        <entry xmlns='http://www.w3.org/2005/Atom'>\n" +
                "          <title>Soliloquy</title>\n" +
                "          <summary>\n" +
                "To be, or not to be: that is the question:\n" +
                "Whether 'tis nobler in the mind to suffer\n" +
                "The slings and arrows of outrageous fortune,\n" +
                "Or to take arms against a sea of troubles,\n" +
                "And by opposing end them?\n" +
                "          </summary>\n" +
                "          <link rel='alternate' type='text/html'\n" +
                "                href='http://denmark.lit/2003/12/13/atom03'/>\n" +
                "          <id>tag:denmark.lit,2003:entry-32397</id>\n" +
                "          <published>2003-12-13T18:30:02Z</published>\n" +
                "          <updated>2003-12-13T18:30:02Z</updated>\n" +
                "        </entry>\n" +
                "      </item>\n" +
                "    </items>\n" +
                "  </pubsub>\n" +
                "</iq>\n";
        IQ iq = unmarshal(xml, IQ.class);
        PubSub pubSub = iq.getExtension(PubSub.class);
        Assert.assertNotNull(pubSub);
        Assert.assertNotNull(pubSub.getItems());
        Assert.assertEquals(pubSub.getItems().size(), 4);
        Assert.assertEquals(pubSub.getItems().get(0).getId(), "368866411b877c30064a5f62b917cffe");
    }

    @Test
    public void marshalRequestMostRecentItems() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withItems("princely_musings", 2);
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><items max_items=\"2\" node=\"princely_musings\"></items></pubsub>");
    }

    @Test
    public void marshalRequestParticularItem() throws JAXBException, XMLStreamException {
        PubSub pubSub = PubSub.withItems("princely_musings", "ae890ac52d0df67ed7cfdf51b644e901");
        String xml = marshal(pubSub);
        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><items node=\"princely_musings\"><item id=\"ae890ac52d0df67ed7cfdf51b644e901\"></item></items></pubsub>");
    }
}
