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

package rocks.xmpp.extensions.privacy;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.roster.model.Contact;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.privacy.model.Privacy;
import rocks.xmpp.extensions.privacy.model.PrivacyList;
import rocks.xmpp.extensions.privacy.model.PrivacyRule;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class PrivacyListTest extends XmlTest {

    protected PrivacyListTest() throws JAXBException, XMLStreamException {
        super(IQ.class, Privacy.class);
    }


    @Test
    public void marshalPrivacyListsRequest() throws XMLStreamException, JAXBException {
        IQ iq = new IQ(null, IQ.Type.GET, new Privacy(), "getlist1", Jid.valueOf("romeo@example.net/orchard"), null, null);
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq from=\"romeo@example.net/orchard\" id=\"getlist1\" type=\"get\"><query xmlns=\"jabber:iq:privacy\"></query></iq>");
    }

    @Test
    public void unmarshalPrivacyServerResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result' id='getlist1' to='romeo@example.net/orchard'>\n" +
                "<query xmlns='jabber:iq:privacy'>\n" +
                "  <active name='private'/>\n" +
                "  <default name='public'/>\n" +
                "  <list name='public'/>\n" +
                "  <list name='private'/>\n" +
                "  <list name='special'/>\n" +
                "</query>\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        Privacy privacy = iq.getExtension(Privacy.class);
        Assert.assertNotNull(privacy);
        Assert.assertEquals(privacy.getActiveName(), "private");
        Assert.assertEquals(privacy.getDefaultName(), "public");
        Assert.assertEquals(privacy.getPrivacyLists().size(), 3);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getName(), "public");
        Assert.assertEquals(privacy.getPrivacyLists().get(1).getName(), "private");
        Assert.assertEquals(privacy.getPrivacyLists().get(2).getName(), "special");
    }

    @Test
    public void marshalPrivacyListRequest() throws XMLStreamException, JAXBException {
        IQ iq = new IQ(null, IQ.Type.GET, new Privacy(new PrivacyList("public")), "getlist1", Jid.valueOf("romeo@example.net/orchard"), null, null);
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq from=\"romeo@example.net/orchard\" id=\"getlist1\" type=\"get\"><query xmlns=\"jabber:iq:privacy\"><list name=\"public\"></list></query></iq>");
    }

    @Test
    public void unmarshalPrivacyListResponse() throws JAXBException, XMLStreamException {
        String xml = "<iq type='result' id='getlist2' to='romeo@example.net/orchard'>\n" +
                "<query xmlns='jabber:iq:privacy'>\n" +
                "  <list name='public'>\n" +
                "    <item type='jid'\n" +
                "          value='tybalt@example.com'\n" +
                "          action='deny'\n" +
                "          order='1'/>\n" +
                "    <item action='allow' order='2'/>\n" +
                "  </list>\n" +
                "</query>\n" +
                "</iq>";

        IQ iq = unmarshal(xml, IQ.class);
        Privacy privacy = iq.getExtension(Privacy.class);
        Assert.assertNotNull(privacy);
        Assert.assertEquals(privacy.getPrivacyLists().size(), 1);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getName(), "public");
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().size(), 2);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getType(), PrivacyRule.Type.JID);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getValue(), "tybalt@example.com");
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getAction(), PrivacyRule.Action.DENY);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getOrder(), 1);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(1).getAction(), PrivacyRule.Action.ALLOW);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(1).getOrder(), 2);
    }

    @Test
    public void unmarshalPrivacyListResponse2() throws JAXBException, XMLStreamException {
        String xml = "<iq type='result' id='getlist3' to='romeo@example.net/orchard'>\n" +
                "<query xmlns='jabber:iq:privacy'>\n" +
                "  <list name='private'>\n" +
                "    <item type='subscription'\n" +
                "          value='both'\n" +
                "          action='allow'\n" +
                "          order='10'/>\n" +
                "    <item action='deny' order='15'/>\n" +
                "  </list>\n" +
                "</query>\n" +
                "</iq>\n";
        IQ iq = unmarshal(xml, IQ.class);
        Privacy privacy = iq.getExtension(Privacy.class);
        Assert.assertNotNull(privacy);
        Assert.assertEquals(privacy.getPrivacyLists().size(), 1);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getName(), "private");
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().size(), 2);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getType(), PrivacyRule.Type.SUBSCRIPTION);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getValue(), "both");
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getAction(), PrivacyRule.Action.ALLOW);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getOrder(), 10);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(1).getAction(), PrivacyRule.Action.DENY);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(1).getOrder(), 15);
    }

    @Test
    public void unmarshalPrivacyListResponse3() throws JAXBException, XMLStreamException {
        String xml = "<iq from='romeo@example.net/orchard' type='set' id='msg2'>\n" +
                "<query xmlns='jabber:iq:privacy'>\n" +
                "  <list name='message-group-example'>\n" +
                "    <item type='group'\n" +
                "          value='Enemies'\n" +
                "          action='deny'\n" +
                "          order='4'>\n" +
                "      <message/>\n" +
                "    </item>\n" +
                "  </list>\n" +
                "</query>\n" +
                "</iq>\n";
        IQ iq = unmarshal(xml, IQ.class);
        Privacy privacy = iq.getExtension(Privacy.class);
        Assert.assertNotNull(privacy);
        Assert.assertEquals(privacy.getPrivacyLists().size(), 1);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getName(), "message-group-example");
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().size(), 1);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getType(), PrivacyRule.Type.GROUP);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getValue(), "Enemies");
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getAction(), PrivacyRule.Action.DENY);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getOrder(), 4);
    }

    @Test
    public void unmarshalPrivacyListResponse4() throws JAXBException, XMLStreamException {
        String xml = "<iq from='romeo@example.net/orchard' type='set' id='msg1'>\n" +
                "<query xmlns='jabber:iq:privacy'>\n" +
                "  <list name='message-jid-example'>\n" +
                "    <item type='jid'\n" +
                "          value='tybalt@example.com'\n" +
                "          action='deny'\n" +
                "          order='3'>\n" +
                "      <message/>\n" +
                "    </item>\n" +
                "  </list>\n" +
                "</query>\n" +
                "</iq>\n";

        IQ iq = unmarshal(xml, IQ.class);
        Privacy privacy = iq.getExtension(Privacy.class);
        Assert.assertNotNull(privacy);
        Assert.assertEquals(privacy.getPrivacyLists().size(), 1);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getName(), "message-jid-example");
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().size(), 1);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getType(), PrivacyRule.Type.JID);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getValue(), "tybalt@example.com");
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getAction(), PrivacyRule.Action.DENY);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getOrder(), 3);
        Assert.assertEquals(privacy.getPrivacyLists().get(0).getPrivacyRules().get(0).getOrder(), 3);
    }

    @Test
    public void marshalChangeActiveListRequest() throws XMLStreamException, JAXBException {
        Privacy privacy = new Privacy();
        privacy.setActiveName("special");
        IQ iq = new IQ(null, IQ.Type.GET, privacy, "getlist1", Jid.valueOf("romeo@example.net/orchard"), null, null);
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq from=\"romeo@example.net/orchard\" id=\"getlist1\" type=\"get\"><query xmlns=\"jabber:iq:privacy\"><active name=\"special\"></active></query></iq>");
    }

    @Test
    public void marshalDeclineActiveListRequest() throws XMLStreamException, JAXBException {
        Privacy privacy = new Privacy();
        privacy.setActiveName("");
        IQ iq = new IQ(null, IQ.Type.GET, privacy, "getlist1", Jid.valueOf("romeo@example.net/orchard"), null, null);
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq from=\"romeo@example.net/orchard\" id=\"getlist1\" type=\"get\"><query xmlns=\"jabber:iq:privacy\"><active></active></query></iq>");
    }

    @Test
    public void marshalChangeDefaultListRequest() throws XMLStreamException, JAXBException {
        Privacy privacy = new Privacy();
        privacy.setDefaultName("special");
        IQ iq = new IQ(null, IQ.Type.GET, privacy, "getlist1", Jid.valueOf("romeo@example.net/orchard"), null, null);
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq from=\"romeo@example.net/orchard\" id=\"getlist1\" type=\"get\"><query xmlns=\"jabber:iq:privacy\"><default name=\"special\"></default></query></iq>");
    }

    @Test
    public void marshalDeclineDefaultListRequest() throws XMLStreamException, JAXBException {
        Privacy privacy = new Privacy();
        privacy.setDefaultName("");
        IQ iq = new IQ(null, IQ.Type.GET, privacy, "getlist1", Jid.valueOf("romeo@example.net/orchard"), null, null);
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq from=\"romeo@example.net/orchard\" id=\"getlist1\" type=\"get\"><query xmlns=\"jabber:iq:privacy\"><default></default></query></iq>");
    }

    @Test
    public void marshalPrivacyRule() throws XMLStreamException, JAXBException {
        PrivacyList privacyList = new PrivacyList("test", Arrays.asList(new PrivacyRule(PrivacyRule.Action.ALLOW, 0, Contact.Subscription.BOTH)));
        String xml = marshal(new Privacy(privacyList));
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:privacy\"><list name=\"test\"><item type=\"subscription\" value=\"both\" action=\"allow\" order=\"0\"></item></list></query>");
    }

    @Test
    public void marshalInvisibilityList() throws XMLStreamException, JAXBException {
        PrivacyList privacyList = PrivacyList.createInvisibilityList();
        String xml = marshal(new Privacy(privacyList));
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:privacy\"><list name=\"invisible\"><item action=\"deny\" order=\"1\"><presence-out></presence-out></item></list></query>");
    }

    @Test
    public void marshalInvisibilityListForUsers() throws XMLStreamException, JAXBException {
        PrivacyList privacyList = PrivacyList.createInvisibilityListForUsers("invisible-to-Gandalf", Jid.valueOf("gandalf@tolkien.lit"));
        String xml = marshal(new Privacy(privacyList));
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:privacy\"><list name=\"invisible-to-Gandalf\"><item type=\"jid\" value=\"gandalf@tolkien.lit\" action=\"deny\" order=\"1\"><presence-out></presence-out></item><item action=\"allow\" order=\"2\"><presence-out></presence-out></item></list></query>");
    }

    @Test
    public void marshalInvisibilityListForGroups() throws XMLStreamException, JAXBException {
        PrivacyList privacyList = PrivacyList.createInvisibilityListForGroups("invisible-to-Wizards", "Wizards");
        String xml = marshal(new Privacy(privacyList));
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:privacy\"><list name=\"invisible-to-Wizards\"><item type=\"group\" value=\"Wizards\" action=\"deny\" order=\"1\"><presence-out></presence-out></item><item action=\"allow\" order=\"2\"><presence-out></presence-out></item></list></query>");
    }

    @Test
    public void marshalInvisibilityListExceptForUsers() throws XMLStreamException, JAXBException {
        PrivacyList privacyList = PrivacyList.createInvisibilityListExceptForUsers("visible-to-Frodo", Jid.valueOf("frodo@tolkien.lit"));
        String xml = marshal(new Privacy(privacyList));
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:privacy\"><list name=\"visible-to-Frodo\"><item type=\"jid\" value=\"frodo@tolkien.lit\" action=\"allow\" order=\"1\"><presence-out></presence-out></item><item action=\"deny\" order=\"2\"><presence-out></presence-out></item></list></query>");
    }

    @Test
    public void marshalInvisibilityListExceptForGroups() throws XMLStreamException, JAXBException {
        PrivacyList privacyList = PrivacyList.createInvisibilityListExceptForGroups("visible-to-Bagginses", "Bagginses");
        String xml = marshal(new Privacy(privacyList));
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:privacy\"><list name=\"visible-to-Bagginses\"><item type=\"group\" value=\"Bagginses\" action=\"allow\" order=\"1\"><presence-out></presence-out></item><item action=\"deny\" order=\"2\"><presence-out></presence-out></item></list></query>");
    }

    @Test
    public void testComparePrivacyLists() {

        PrivacyList privacyList1 = new PrivacyList("zzz");
        privacyList1 = privacyList1.asDefault();

        PrivacyList privacyList2 = new PrivacyList("yyy");
        privacyList2 = privacyList2.asActive();

        PrivacyList privacyList3 = new PrivacyList("aaa");
        PrivacyList privacyList4 = new PrivacyList("bbb");
        PrivacyList privacyList5 = new PrivacyList("ccc");

        List<PrivacyList> list = new ArrayList<>();
        list.add(privacyList1);
        list.add(privacyList2);
        list.add(privacyList3);
        list.add(privacyList4);
        list.add(privacyList5);

        Collections.shuffle(list);

        Collections.sort(list);

        Assert.assertEquals(list.get(0), privacyList1);
        Assert.assertEquals(list.get(1), privacyList2);
        Assert.assertEquals(list.get(2), privacyList3);
        Assert.assertEquals(list.get(3), privacyList4);
        Assert.assertEquals(list.get(4), privacyList5);
    }

    @Test
    public void testComparePrivacyRules() {

        PrivacyRule privacyRule1 = new PrivacyRule(PrivacyRule.Action.DENY, 0);
        PrivacyRule privacyRule2 = new PrivacyRule(PrivacyRule.Action.DENY, 1);
        PrivacyRule privacyRule3 = new PrivacyRule(PrivacyRule.Action.DENY, 2);
        PrivacyRule privacyRule4 = new PrivacyRule(PrivacyRule.Action.DENY, 3);
        PrivacyRule privacyRule5 = new PrivacyRule(PrivacyRule.Action.DENY, 4);


        List<PrivacyRule> list = new ArrayList<>();
        list.add(privacyRule1);
        list.add(privacyRule2);
        list.add(privacyRule3);
        list.add(privacyRule4);
        list.add(privacyRule5);

        Collections.shuffle(list);

        Collections.sort(list);

        Assert.assertEquals(list.get(0), privacyRule1);
        Assert.assertEquals(list.get(1), privacyRule2);
        Assert.assertEquals(list.get(2), privacyRule3);
        Assert.assertEquals(list.get(3), privacyRule4);
        Assert.assertEquals(list.get(4), privacyRule5);
    }
}
