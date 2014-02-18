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

package org.xmpp.extension.forward;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.TestConnection;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.stanza.Message;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class StanzaForwardingTest extends BaseTest {

    @Test
    public void unmarshalForwarded() throws XMLStreamException, JAXBException {
        String xml = "<message to='mercutio@verona.lit' from='romeo@montague.lit/orchard' type='chat' id='28gs'>\n" +
                "        <body>A most courteous exposition!</body>\n" +
                "        <forwarded xmlns='urn:xmpp:forward:0'>\n" +
                "          <delay xmlns='urn:xmpp:delay' stamp='2010-07-10T23:08:25Z'/>\n" +
                "          <message from='juliet@capulet.lit/orchard'\n" +
                "                   id='0202197'\n" +
                "                   to='romeo@montague.lit'\n" +
                "                   type='chat'\n" +
                "                   xmlns='jabber:client'>\n" +
                "              <body>Yet I should kill thee with much cherishing.</body>\n" +
                "              <mood xmlns='http://jabber.org/protocol/mood'>\n" +
                "                  <amorous/>\n" +
                "              </mood>\n" +
                "          </message>\n" +
                "        </forwarded>\n" +
                "      </message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        Forwarded forwarded = message.getExtension(Forwarded.class);
        Assert.assertNotNull(forwarded);
        Assert.assertTrue(forwarded.getStanza() instanceof Message);
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestConnection connection1 = new TestConnection();
        StanzaForwardingManager stanzaForwardingManager = connection1.getExtensionManager(StanzaForwardingManager.class);
        // By default, the manager should be NOT enabled.
        Assert.assertFalse(stanzaForwardingManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:forward:0");
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        stanzaForwardingManager.setEnabled(true);
        Assert.assertTrue(stanzaForwardingManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
