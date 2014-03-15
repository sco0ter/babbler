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

package org.xmpp.extension.pubsub.errors;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.pubsub.PubSubFeature;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class PubSubErrorTest extends BaseTest {

    @Test
    public void testUnsupportedErrorRetrieveSubscriptions() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='subscriptions1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='retrieve-subscriptions'/>\n" +
                "  </error>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.RETRIEVE_SUBSCRIPTIONS);
    }

    @Test
    public void testUnsupportedErrorRetrieveAffiliations() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='affil1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='retrieve-affiliations'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.RETRIEVE_AFFILIATIONS);
    }

    @Test
    public void testInvalidJidError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <invalid-jid xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof InvalidJid);
    }

    @Test
    public void testPresenceSubscriptionRequiredError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <error type='auth'>\n" +
                "    <not-authorized xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <presence-subscription-required xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof PresenceSubscriptionRequired);
    }

    @Test
    public void testNotInRosterGroupError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <error type='auth'>\n" +
                "    <not-authorized xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <not-in-roster-group xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof NotInRosterGroup);
    }

    @Test
    public void testClosedNodeError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <error type='cancel'>\n" +
                "    <not-allowed xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <closed-node xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof ClosedNode);
    }

    @Test
    public void testPendingSubscriptionError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <error type='auth'>\n" +
                "    <not-authorized xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <pending-subscription xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof PendingSubscription);
    }

    @Test
    public void testTooManySubscriptionsError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <error type='wait'>\n" +
                "    <policy-violation xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <too-many-subscriptions xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof TooManySubscriptions);
    }

    @Test
    public void testUnsupportedErrorSubscribe() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='subscribe'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.SUBSCRIBE);
    }

    @Test
    public void testConfigurationRequiredError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='sub1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <subscribe\n" +
                "        node='princely_musings'\n" +
                "        jid='francisco@denmark.lit'/>\n" +
                "    <options node='princely_musings' jid='francisco@denmark.lit'>\n" +
                "      <x xmlns='jabber:x:data' type='submit'>\n" +
                "        <field var='FORM_TYPE' type='hidden'>\n" +
                "          <value>http://jabber.org/protocol/pubsub#subscribe_options</value>\n" +
                "        </field>\n" +
                "        <field var='pubsub#deliver'><value>1</value></field>\n" +
                "        <field var='pubsub#digest'><value>0</value></field>\n" +
                "        <field var='pubsub#include_body'><value>false</value></field>\n" +
                "        <field var='pubsub#show-values'>\n" +
                "          <value>chat</value>\n" +
                "          <value>online</value>\n" +
                "          <value>away</value>\n" +
                "        </field>\n" +
                "      </x>\n" +
                "    </options>\n" +
                "  </pubsub>\n" +
                "  <error type='modify'>\n" +
                "    <not-acceptable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <configuration-required xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof ConfigurationRequired);
    }

    @Test
    public void testSubIdRequiredError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='unsub1'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <subid-required xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof SubIdRequired);
    }

    @Test
    public void testNotSubscribedError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='unsub1'>\n" +
                "  <error type='cancel'>\n" +
                "    <unexpected-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <not-subscribed xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof NotSubscribed);
    }

    @Test
    public void testInvalidSubIdError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='unsub1'>\n" +
                "  <error type='modify'>\n" +
                "    <not-acceptable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <invalid-subid xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof InvalidSubId);
    }

    @Test
    public void testJidRequiredError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='options1'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <jid-required xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof JidRequired);
    }

    @Test
    public void testUnsupportedErrorSubscriptionOptions() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='options1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='subscription-options'/>\n" +
                "  </error>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.SUBSCRIPTION_OPTIONS);
    }

    @Test
    public void testInvalidOptionsError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='options2'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <invalid-options xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof InvalidOptions);
    }

    @Test
    public void testUnsupportedErrorRetrieveDefaultSub() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='francisco@denmark.lit/barracks'\n" +
                "    id='def1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='retrieve-default-sub'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.RETRIEVE_DEFAULT_SUB);
    }

    @Test
    public void testUnsupportedErrorPublish() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='publish1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='publish'/>\n" +
                "  </error>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.PUBLISH);
    }

    @Test
    public void testPayloadTooBigError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='publish1'>\n" +
                "  <error type='modify'>\n" +
                "    <not-acceptable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <payload-too-big xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof PayloadTooBig);
    }

    @Test
    public void testInvalidPayloadError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='publish1'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <invalid-payload xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof InvalidPayload);
    }

    @Test
    public void testItemRequiredError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='publish1'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <item-required xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof ItemRequired);
    }

    @Test
    public void testPayloadRequiredError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='publish1'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <payload-required xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof PayloadRequired);
    }

    @Test
    public void testItemForbiddenError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='publish1'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <item-forbidden xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof ItemForbidden);
    }

    @Test
    public void testNodeIdRequiredError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='retract1'>\n" +
                "  <error type='modify'>\n" +
                "    <bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <nodeid-required xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof NodeIdRequired);
    }

    @Test
    public void testUnsupportedErrorDeleteItems() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='retract1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='delete-items'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.DELETE_ITEMS);
    }

    @Test
    public void testUnsupportedErrorCreateNodes() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='create1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='create-nodes'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.CREATE_NODES);
    }

    @Test
    public void testUnsupportedErrorConfigNode() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='hamlet@denmark.lit/elsinore'\n" +
                "    to='pubsub.shakespeare.lit'\n" +
                "    id='config1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='config-node'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.CONFIG_NODE);
    }

    @Test
    public void testUnsupportedErrorRetrieveDefault() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='def1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='retrieve-default'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.RETRIEVE_DEFAULT);
    }

    @Test
    public void testUnsupportedErrorPurgeNodes() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='purge1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='purge-nodes'/>\n" +
                "  </error>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.PURGE_NODES);
    }

    @Test
    public void testUnsupportedErrorGetPending() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='pending1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='get-pending'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.GET_PENDING);
    }

    @Test
    public void testUnsupportedErrorManageSubscriptions() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    id='purge1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='manage-subscriptions'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.MANAGE_SUBSCRIPTIONS);
    }

    @Test
    public void testUnsupportedErrorModifyAffiliations() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    id='ent1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='modify-affiliations'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.MODIFY_AFFILIATIONS);
    }

    @Test
    public void testUnsupportedErrorMemberAffiliations() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    id='ent1'>\n" +
                "  <error type='cancel'>\n" +
                "    <feature-not-implemented xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <unsupported xmlns='http://jabber.org/protocol/pubsub#errors'\n" +
                "                 feature='member-affiliation'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof Unsupported);
        Assert.assertEquals(((Unsupported) iq.getError().getExtension()).getFeature(), PubSubFeature.MEMBER_AFFILIATION);
    }

    @Test
    public void testMaxItemsExceededError() throws JAXBException, XMLStreamException {
        String xml = "<iq type='error'\n" +
                "    from='pubsub.shakespeare.lit'\n" +
                "    to='hamlet@denmark.lit/elsinore'\n" +
                "    id='publish1'>\n" +
                "  <error type='modify'>\n" +
                "    <not-allowed xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "    <max-items-exceeded xmlns='http://jabber.org/protocol/pubsub#errors'/>\n" +
                "  </error>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(iq.getError().getExtension() instanceof MaxItemsExceeded);
    }
}
