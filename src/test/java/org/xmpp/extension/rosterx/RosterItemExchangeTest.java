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

package org.xmpp.extension.rosterx;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.stanza.Message;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class RosterItemExchangeTest extends BaseTest {

    @Test
    public void unmarshalRosterItemExchange() throws XMLStreamException, JAXBException {
        String xml = "<message from='horatio@denmark.lit' to='hamlet@denmark.lit'>\n" +
                "  <body>Some visitors, m'lord!</body>\n" +
                "  <x xmlns='http://jabber.org/protocol/rosterx'> \n" +
                "    <item action='add'\n" +
                "          jid='rosencrantz@denmark.lit'\n" +
                "          name='Rosencrantz'>\n" +
                "      <group>Visitors</group>\n" +
                "    </item>\n" +
                "    <item action='delete'\n" +
                "          jid='guildenstern@denmark.lit'\n" +
                "          name='Guildenstern'>\n" +
                "      <group>Visitors</group>\n" +
                "    </item>\n" +
                "    <item action='modify'\n" +
                "          jid='rosencrantz@denmark.lit'\n" +
                "          name='Rosencrantz'>\n" +
                "      <group>Retinue</group>\n" +
                "    </item>\n" +
                "  </x>\n" +
                "</message>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        RosterExchange rosterExchange = message.getExtension(RosterExchange.class);
        Assert.assertNotNull(rosterExchange);
        Assert.assertEquals(rosterExchange.getItems().size(), 3);
        Assert.assertEquals(rosterExchange.getItems().get(0).getJid(), Jid.valueOf("rosencrantz@denmark.lit"));
        Assert.assertEquals(rosterExchange.getItems().get(0).getAction(), RosterExchange.Item.Action.ADD);
        Assert.assertEquals(rosterExchange.getItems().get(0).getName(), "Rosencrantz");
        Assert.assertEquals(rosterExchange.getItems().get(0).getGroups().size(), 1);
        Assert.assertEquals(rosterExchange.getItems().get(0).getGroups().get(0), "Visitors");
        Assert.assertEquals(rosterExchange.getItems().get(1).getJid(), Jid.valueOf("guildenstern@denmark.lit"));
        Assert.assertEquals(rosterExchange.getItems().get(1).getAction(), RosterExchange.Item.Action.DELETE);
        Assert.assertEquals(rosterExchange.getItems().get(1).getName(), "Guildenstern");
        Assert.assertEquals(rosterExchange.getItems().get(1).getGroups().size(), 1);
        Assert.assertEquals(rosterExchange.getItems().get(1).getGroups().get(0), "Visitors");
        Assert.assertEquals(rosterExchange.getItems().get(2).getJid(), Jid.valueOf("rosencrantz@denmark.lit"));
        Assert.assertEquals(rosterExchange.getItems().get(2).getAction(), RosterExchange.Item.Action.MODIFY);
        Assert.assertEquals(rosterExchange.getItems().get(2).getName(), "Rosencrantz");
        Assert.assertEquals(rosterExchange.getItems().get(2).getGroups().size(), 1);
        Assert.assertEquals(rosterExchange.getItems().get(2).getGroups().get(0), "Retinue");
    }

}
