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

package org.xmpp.extension.muc;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.Jid;
import org.xmpp.XmlTest;
import org.xmpp.extension.data.DataForm;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Christian Schudt
 */
public class RoomInfoFormTest extends XmlTest {
    protected RoomInfoFormTest() throws JAXBException, XMLStreamException {
        super(DataForm.class);
    }

    @Test
    public void testRoomConfigurationForm() throws JAXBException, XMLStreamException, MalformedURLException {
        String xml = "<x xmlns='jabber:x:data' type='result'>\n" +
                "      <field var='FORM_TYPE' type='hidden'>\n" +
                "        <value>http://jabber.org/protocol/muc#roominfo</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_description' \n" +
                "             label='Description'>\n" +
                "        <value>The place for all good witches!</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_subjectmod' \n" +
                "             label='Occupants May Change the Subject'>\n" +
                "        <value>true</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_contactjid' \n" +
                "             label='Contact Addresses'>\n" +
                "        <value>crone1@shakespeare.lit</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_subject' \n" +
                "             label='Current Discussion Topic'>\n" +
                "        <value>Spells</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roomconfig_changesubject' \n" +
                "             label='Subject can be modified'>\n" +
                "        <value>true</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_occupants' \n" +
                "             label='Number of occupants'>\n" +
                "        <value>3</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_ldapgroup' \n" +
                "             label='Associated LDAP Group'>\n" +
                "        <value>cn=witches,dc=shakespeare,dc=lit</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_lang' \n" +
                "             label='Language of discussion'>\n" +
                "        <value>en</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_logs' \n" +
                "             label='URL for discussion logs'>\n" +
                "        <value>http://www.shakespeare.lit/chatlogs/coven/</value>\n" +
                "      </field>\n" +
                "      <field var='muc#maxhistoryfetch'\n" +
                "             label='Maximum Number of History Messages Returned by Room'>\n" +
                "        <value>50</value>\n" +
                "      </field>\n" +
                "      <field var='muc#roominfo_pubsub' \n" +
                "             label='Associated pubsub node'>\n" +
                "        <value>xmpp:pubsub.shakespeare.lit?;node=the-coven-node</value>\n" +
                "      </field>\n" +
                "    </x>\n";

        DataForm dataForm = unmarshal(xml, DataForm.class);

        RoomInfoForm roomInfoForm = new RoomInfoForm(dataForm);

        Assert.assertEquals(roomInfoForm.getDescription(), "The place for all good witches!");
        Assert.assertTrue(roomInfoForm.isChangeSubjectAllowed());
        Assert.assertEquals(roomInfoForm.getContacts().get(0), Jid.valueOf("crone1@shakespeare.lit"));
        Assert.assertEquals(roomInfoForm.getSubject(), "Spells");
        Assert.assertEquals(roomInfoForm.getCurrentNumberOfOccupants(), 3);
        Assert.assertEquals(roomInfoForm.getLdapGroup(), "cn=witches,dc=shakespeare,dc=lit");
        Assert.assertEquals(roomInfoForm.getLanguage(), "en");
        Assert.assertEquals(roomInfoForm.getLogs(), new URL("http://www.shakespeare.lit/chatlogs/coven/"));
        Assert.assertEquals(roomInfoForm.getMaxHistoryMessages(), 50);
    }

    @Test
    public void testEmptyDataForm() throws MalformedURLException {
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        RoomInfoForm roomInfoForm = new RoomInfoForm(dataForm);
        roomInfoForm.setMaxHistoryMessages(50);
        DataForm.Field fieldHistoryFetch = dataForm.findField("muc#maxhistoryfetch");
        Assert.assertNotNull(fieldHistoryFetch);
        Assert.assertEquals(fieldHistoryFetch.getValues().size(), 1);
        Assert.assertEquals(fieldHistoryFetch.getValues().get(0), "50");
        Assert.assertEquals(roomInfoForm.getMaxHistoryMessages(), 50);

        roomInfoForm.setContacts(Arrays.asList(Jid.valueOf("test1@domain"), Jid.valueOf("test2@domain")));
        DataForm.Field fieldAdministrators = dataForm.findField("muc#roominfo_contactjid");
        Assert.assertNotNull(fieldAdministrators);
        Assert.assertEquals(fieldAdministrators.getValues().size(), 2);
        Assert.assertEquals(fieldAdministrators.getValues().get(0), "test1@domain");
        Assert.assertEquals(fieldAdministrators.getValues().get(1), "test2@domain");
        Assert.assertEquals(roomInfoForm.getContacts(), Arrays.asList(Jid.valueOf("test1@domain"), Jid.valueOf("test2@domain")));

        roomInfoForm.setDescription("Description");
        DataForm.Field fieldDescription = dataForm.findField("muc#roominfo_description");
        Assert.assertNotNull(fieldDescription);
        Assert.assertEquals(fieldDescription.getValues().size(), 1);
        Assert.assertEquals(fieldDescription.getValues().get(0), "Description");
        Assert.assertEquals(roomInfoForm.getDescription(), "Description");

        roomInfoForm.setLanguage("en");
        DataForm.Field fieldLanguage = dataForm.findField("muc#roominfo_lang");
        Assert.assertNotNull(fieldLanguage);
        Assert.assertEquals(fieldLanguage.getValues().size(), 1);
        Assert.assertEquals(fieldLanguage.getValues().get(0), "en");
        Assert.assertEquals(roomInfoForm.getLanguage(), "en");

        roomInfoForm.setLdapGroup("ldap");
        DataForm.Field fieldLdap = dataForm.findField("muc#roominfo_ldapgroup");
        Assert.assertNotNull(fieldLdap);
        Assert.assertEquals(fieldLdap.getValues().size(), 1);
        Assert.assertEquals(fieldLdap.getValues().get(0), "ldap");
        Assert.assertEquals(roomInfoForm.getLdapGroup(), "ldap");

        roomInfoForm.setLogs(new URL("http://www.example.net"));
        DataForm.Field fieldLogs = dataForm.findField("muc#roominfo_logs");
        Assert.assertNotNull(fieldLogs);
        Assert.assertEquals(fieldLogs.getValues().size(), 1);
        Assert.assertEquals(fieldLogs.getValues().get(0), "http://www.example.net");
        Assert.assertEquals(roomInfoForm.getLogs(), new URL("http://www.example.net"));

        roomInfoForm.setCurrentNumberOfOccupants(4);
        DataForm.Field fieldOccupants = dataForm.findField("muc#roominfo_occupants");
        Assert.assertNotNull(fieldOccupants);
        Assert.assertEquals(fieldOccupants.getValues().size(), 1);
        Assert.assertEquals(fieldOccupants.getValues().get(0), "4");
        Assert.assertEquals(roomInfoForm.getCurrentNumberOfOccupants(), 4);

        roomInfoForm.setSubject("test");
        DataForm.Field fieldSubject = dataForm.findField("muc#roominfo_subject");
        Assert.assertNotNull(fieldSubject);
        Assert.assertEquals(fieldSubject.getValues().size(), 1);
        Assert.assertEquals(fieldSubject.getValues().get(0), "test");
        Assert.assertEquals(roomInfoForm.getSubject(), "test");

        roomInfoForm.setChangeSubjectAllowed(true);
        DataForm.Field fieldMembersOnly = dataForm.findField("muc#roominfo_subjectmod");
        Assert.assertNotNull(fieldMembersOnly);
        Assert.assertEquals(fieldMembersOnly.getValues().size(), 1);
        Assert.assertEquals(fieldMembersOnly.getValues().get(0), "1");
        Assert.assertTrue(roomInfoForm.isChangeSubjectAllowed());
    }
}
