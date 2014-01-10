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

package org.xmpp.extension.servicediscovery;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class ServiceDiscoveryTest extends BaseTest {

    @Test
    public void unmarshalServiceDiscoveryResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='plays.shakespeare.lit'\n" +
                "    to='romeo@montague.net/orchard'\n" +
                "    id='info1'>\n" +
                "  <query xmlns='http://jabber.org/protocol/disco#info'>\n" +
                "    <identity\n" +
                "        category='conference'\n" +
                "        type='text'\n" +
                "        name='Play-Specific Chatrooms'/>\n" +
                "    <identity\n" +
                "        category='directory'\n" +
                "        type='chatroom'\n" +
                "        name='Play-Specific Chatrooms'/>\n" +
                "    <feature var='http://jabber.org/protocol/disco#info'/>\n" +
                "    <feature var='http://jabber.org/protocol/disco#items'/>\n" +
                "    <feature var='http://jabber.org/protocol/muc'/>\n" +
                "    <feature var='jabber:iq:register'/>\n" +
                "    <feature var='jabber:iq:search'/>\n" +
                "    <feature var='jabber:iq:time'/>\n" +
                "    <feature var='jabber:iq:version'/>\n" +
                "  </query>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        ServiceDiscovery serviceDiscovery = iq.getExtension(ServiceDiscovery.class);
        Assert.assertNotNull(serviceDiscovery);
        Assert.assertEquals(serviceDiscovery.getIdentities().size(), 2);
        Assert.assertEquals(serviceDiscovery.getFeatures().size(), 7);

        Identity identity1 = new Identity("conference", "text", "Play-Specific Chatrooms");
        Identity identity2 = new Identity("directory", "chatroom", "Play-Specific Chatrooms");

        Assert.assertTrue(serviceDiscovery.getIdentities().contains(identity1));
        Assert.assertTrue(serviceDiscovery.getIdentities().contains(identity2));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("http://jabber.org/protocol/disco#info")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("http://jabber.org/protocol/disco#items")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("http://jabber.org/protocol/muc")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("jabber:iq:register")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("jabber:iq:search")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("jabber:iq:time")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("jabber:iq:version")));
    }

    @Test
    public void unmarshalServiceDiscoveryItemResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='catalog.shakespeare.lit'\n" +
                "    to='romeo@montague.net/orchard'\n" +
                "    id='items2'>\n" +
                "  <query xmlns='http://jabber.org/protocol/disco#items'>\n" +
                "    <item jid='catalog.shakespeare.lit'\n" +
                "          node='books'\n" +
                "          name='Books by and about Shakespeare'/>\n" +
                "    <item jid='catalog.shakespeare.lit'\n" +
                "          node='clothing'\n" +
                "          name='Wear your literary taste with pride'/>\n" +
                "    <item jid='catalog.shakespeare.lit'\n" +
                "          node='music'\n" +
                "          name='Music from the time of Shakespeare'/>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        ItemDiscovery itemDiscovery = iq.getExtension(ItemDiscovery.class);
        Assert.assertNotNull(itemDiscovery);
        Assert.assertEquals(itemDiscovery.getItems().size(), 3);
        Assert.assertEquals(itemDiscovery.getItems().get(0).getJid(), Jid.fromString("catalog.shakespeare.lit"));
        Assert.assertEquals(itemDiscovery.getItems().get(0).getNode(), "books");
        Assert.assertEquals(itemDiscovery.getItems().get(0).getName(), "Books by and about Shakespeare");
        Assert.assertEquals(itemDiscovery.getItems().get(1).getJid(), Jid.fromString("catalog.shakespeare.lit"));
        Assert.assertEquals(itemDiscovery.getItems().get(1).getNode(), "clothing");
        Assert.assertEquals(itemDiscovery.getItems().get(1).getName(), "Wear your literary taste with pride");
        Assert.assertEquals(itemDiscovery.getItems().get(2).getJid(), Jid.fromString("catalog.shakespeare.lit"));
        Assert.assertEquals(itemDiscovery.getItems().get(2).getNode(), "music");
        Assert.assertEquals(itemDiscovery.getItems().get(2).getName(), "Music from the time of Shakespeare");
    }

    @Test
    public void testAddFeatureTwice() {
        ServiceDiscoveryManager serviceDiscoveryManager = new ServiceDiscoveryManager(connection);
        serviceDiscoveryManager.getFeatures().add(new Feature("http://jabber.org/protocol/muc"));
        serviceDiscoveryManager.getFeatures().add(new Feature("http://jabber.org/protocol/muc"));
    }

    @Test
    public void testAddIdentityTwice() {
        ServiceDiscoveryManager serviceDiscoveryManager = new ServiceDiscoveryManager(connection);
        serviceDiscoveryManager.getIdentities().add(new Identity("conference", "text", "name1", "en"));
        serviceDiscoveryManager.getIdentities().add(new Identity("conference", "text", "name2", "en"));
    }
}
