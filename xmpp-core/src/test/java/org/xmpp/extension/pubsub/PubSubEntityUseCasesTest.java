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

import org.xmpp.UnmarshalTest;

/**
 * @author Christian Schudt
 */
public class PubSubEntityUseCasesTest extends UnmarshalTest {

//    @Test
//    public void unmarshalPubSub() throws XMLStreamException, JAXBException {
//        String xml = "<iq type='set'\n" +
//                "    from='hamlet@denmark.lit/blogbot'\n" +
//                "    to='pubsub.shakespeare.lit'\n" +
//                "    id='pub1'>\n" +
//                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
//                "    <publish node='princely_musings'>\n" +
//                "      <item>\n" +
//                "        <entry xmlns='http://www.w3.org/2005/Atom'>\n" +
//                "          <title>Soliloquy</title>\n" +
//                "          <summary>\n" +
//                "To be, or not to be: that is the question:\n" +
//                "Whether 'tis nobler in the mind to suffer\n" +
//                "The slings and arrows of outrageous fortune,\n" +
//                "Or to take arms against a sea of troubles,\n" +
//                "And by opposing end them?\n" +
//                "          </summary>\n" +
//                "          <link rel='alternate' type='text/html'\n" +
//                "                href='http://denmark.lit/2003/12/13/atom03'/>\n" +
//                "          <id>tag:denmark.lit,2003:entry-32397</id>\n" +
//                "          <published>2003-12-13T18:30:02Z</published>\n" +
//                "          <updated>2003-12-13T18:30:02Z</updated>\n" +
//                "        </entry>\n" +
//                "      </item>\n" +
//                "    </publish>\n" +
//                "  </pubsub>\n" +
//                "</iq>\n";
//        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
//        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
//
//        PubSub pubSub = iq.getExtension(PubSub.class);
//        Assert.assertNotNull(pubSub);
//    }
//
//    @Test
//    public void marshalSubscriptionsRequest() throws JAXBException, XMLStreamException, IOException {
//        PubSub pubSub = PubSub.withSubscriptions();
//        String xml = marshall(pubSub);
//        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><subscriptions></subscriptions></pubsub>");
//    }
//
//    @Test
//    public void marshalSubscriptionsRequestWithNode() throws JAXBException, XMLStreamException, IOException {
//        PubSub pubSub = PubSub.withSubscriptions("princely_musings");
//        String xml = marshall(pubSub);
//        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><subscriptions node=\"princely_musings\"></subscriptions></pubsub>");
//    }
//
//    @Test
//    public void unmarshalPubSubSubscriptions() throws XMLStreamException, JAXBException {
//        String xml = "<iq type='result'\n" +
//                "    from='pubsub.shakespeare.lit'\n" +
//                "    to='francisco@denmark.lit'\n" +
//                "    id='subscriptions1'>\n" +
//                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
//                "    <subscriptions>\n" +
//                "      <subscription node='node1' jid='francisco@denmark.lit' subscription='subscribed'/>\n" +
//                "      <subscription node='node2' jid='francisco@denmark.lit' subscription='subscribed'/>\n" +
//                "      <subscription node='node5' jid='francisco@denmark.lit' subscription='unconfigured'/>\n" +
//                "      <subscription node='node6' jid='francisco@denmark.lit' subscription='subscribed' subid='123-abc'/>\n" +
//                "      <subscription node='node6' jid='francisco@denmark.lit' subscription='subscribed' subid='004-yyy'/>\n" +
//                "    </subscriptions>\n" +
//                "  </pubsub>\n" +
//                "</iq>\n";
//        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
//        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
//        PubSub pubSub = iq.getExtension(PubSub.class);
//        Assert.assertNotNull(pubSub);
//        Assert.assertNotNull(pubSub.getSubscriptions());
//        Assert.assertEquals(pubSub.getSubscriptions().size(), 5);
//        Assert.assertEquals(pubSub.getSubscriptions().get(0).getNode(), "node1");
//        Assert.assertEquals(pubSub.getSubscriptions().get(0).getJid(), Jid.valueOf("francisco@denmark.lit"));
//        Assert.assertEquals(pubSub.getSubscriptions().get(0).getSubscriptionState(), SubscriptionState.SUBSCRIBED);
//
//        Assert.assertEquals(pubSub.getSubscriptions().get(2).getSubscriptionState(), SubscriptionState.UNCONFIGURED);
//        Assert.assertEquals(pubSub.getSubscriptions().get(3).getSubId(), "123-abc");
//    }
//
//    @Test
//    public void unmarshalPubSubNoSubscriptions() throws XMLStreamException, JAXBException {
//        String xml = "<iq type='result'\n" +
//                "    from='pubsub.shakespeare.lit'\n" +
//                "    to='francisco@denmark.lit/barracks'\n" +
//                "    id='subscriptions1'>\n" +
//                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
//                "    <subscriptions/>\n" +
//                "  </pubsub>\n" +
//                "</iq>\n";
//        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
//        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
//        PubSub pubSub = iq.getExtension(PubSub.class);
//        Assert.assertNotNull(pubSub);
//        Assert.assertNotNull(pubSub.getSubscriptions());
//        Assert.assertEquals(pubSub.getSubscriptions().size(), 0);
//    }
//
//    @Test
//    public void marshalAffiliations() throws JAXBException, XMLStreamException, IOException {
//        PubSub pubSub = PubSub.withAffiliations();
//        String xml = marshall(pubSub);
//        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><affiliations></affiliations></pubsub>");
//    }
//
//    @Test
//    public void marshalAffiliationsWithNode() throws JAXBException, XMLStreamException, IOException {
//        PubSub pubSub = PubSub.withAffiliations("node6");
//        String xml = marshall(pubSub);
//        Assert.assertEquals(xml, "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\"><affiliations node=\"node6\"></affiliations></pubsub>");
//    }
//
//    @Test
//    public void unmarshalPubSubAffiliations() throws XMLStreamException, JAXBException {
//        String xml = "<iq type='result'\n" +
//                "    from='pubsub.shakespeare.lit'\n" +
//                "    to='francisco@denmark.lit'\n" +
//                "    id='affil1'>\n" +
//                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
//                "    <affiliations>\n" +
//                "      <affiliation node='node1' affiliation='owner'/>\n" +
//                "      <affiliation node='node2' affiliation='publisher'/>\n" +
//                "      <affiliation node='node5' affiliation='outcast'/>\n" +
//                "      <affiliation node='node6' affiliation='owner'/>\n" +
//                "    </affiliations>\n" +
//                "  </pubsub>\n" +
//                "</iq>";
//        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
//        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
//        PubSub pubSub = iq.getExtension(PubSub.class);
//        Assert.assertNotNull(pubSub);
//        Assert.assertNotNull(pubSub.getAffiliations());
//        Assert.assertEquals(pubSub.getAffiliations().size(), 4);
//        Assert.assertEquals(pubSub.getAffiliations().get(0).getNode(), "node1");
//        Assert.assertEquals(pubSub.getAffiliations().get(0).getAffiliation(), Affiliation.OWNER);
//
//        Assert.assertEquals(pubSub.getAffiliations().get(1).getAffiliation(), Affiliation.PUBLISHER);
//        Assert.assertEquals(pubSub.getAffiliations().get(2).getAffiliation(), Affiliation.OUTCAST);
//    }
//
//    @Test
//    public void unmarshalPubSubNoAffiliations() throws XMLStreamException, JAXBException {
//        String xml = "<iq type='result'\n" +
//                "    from='pubsub.shakespeare.lit'\n" +
//                "    to='francisco@denmark.lit/barracks'\n" +
//                "    id='affil1'>\n" +
//                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
//                "    <affiliations/>\n" +
//                "  </pubsub>\n" +
//                "</iq>";
//        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
//        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
//        PubSub pubSub = iq.getExtension(PubSub.class);
//        Assert.assertNotNull(pubSub);
//        Assert.assertNotNull(pubSub.getAffiliations());
//        Assert.assertTrue(pubSub.getAffiliations().isEmpty());
//    }

//    @Test
//    public void testFeatures() {
//        InfoDiscovery infoDiscovery = new InfoDiscovery();
//        infoDiscovery.getFeatures().add(new Feature("http://jabber.org/protocol/pubsub#collections"));
//        infoDiscovery.getFeatures().add(new Feature("http://jabber.org/protocol/pubsub#config-node"));
//        infoDiscovery.getFeatures().add(new Feature("http://jabber.org/protocol/disco#info"));
//        PubSubManager pubSubManager = connection.getExtensionManager(PubSubManager.class);
//        Set<PubSubFeature> pubSubFeatures = pubSubManager.createPubSubService(null).getFeatures(infoDiscovery);
//
//        Assert.assertEquals(pubSubFeatures.size(), 2);
//        Assert.assertTrue(pubSubFeatures.contains(PubSubFeature.COLLECTIONS));
//        Assert.assertTrue(pubSubFeatures.contains(PubSubFeature.CONFIG_NODE));
//    }

}
