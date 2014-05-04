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
import org.xmpp.UnmarshalTest;
import org.xmpp.extension.data.DataForm;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class RoomConfigurationFormTest extends UnmarshalTest {
    protected RoomConfigurationFormTest() throws JAXBException, XMLStreamException {
        super(DataForm.class);
    }

    @Test
    public void testRoomConfigurationForm() throws JAXBException, XMLStreamException {
        String xml = "<x xmlns='jabber:x:data' type='form'>\n" +
                "      <title>Configuration for \"coven\" Room</title>\n" +
                "      <instructions>\n" +
                "        Complete this form to modify the\n" +
                "        configuration of your room.\n" +
                "      </instructions>\n" +
                "      <field\n" +
                "          type='hidden'\n" +
                "          var='FORM_TYPE'>\n" +
                "        <value>http://jabber.org/protocol/muc#roomconfig</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Natural-Language Room Name'\n" +
                "          type='text-single'\n" +
                "          var='muc#roomconfig_roomname'>\n" +
                "        <value>A Dark Cave</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Short Description of Room'\n" +
                "          type='text-single'\n" +
                "          var='muc#roomconfig_roomdesc'>\n" +
                "        <value>The place for all good witches!</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Enable Public Logging?'\n" +
                "          type='boolean'\n" +
                "          var='muc#roomconfig_enablelogging'>\n" +
                "        <value>1</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Allow Occupants to Change Subject?'\n" +
                "          type='boolean'\n" +
                "          var='muc#roomconfig_changesubject'>\n" +
                "        <value>0</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Allow Occupants to Invite Others?'\n" +
                "          type='boolean'\n" +
                "          var='muc#roomconfig_allowinvites'>\n" +
                "        <value>0</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Who Can Send Private Messages?'\n" +
                "          type='list-single'\n" +
                "          var='muc#roomconfig_allowpm'>\n" +
                "        <value>anyone</value>\n" +
                "        <option label='Anyone'>\n" +
                "          <value>anyone</value>\n" +
                "        </option>\n" +
                "        <option label='Anyone with Voice'>\n" +
                "          <value>participants</value>\n" +
                "          </option>\n" +
                "        <option label='Moderators Only'>\n" +
                "          <value>moderators</value>\n" +
                "        </option>\n" +
                "        <option label='Nobody'>\n" +
                "          <value>none</value>\n" +
                "        </option>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Maximum Number of Occupants'\n" +
                "          type='list-single'\n" +
                "          var='muc#roomconfig_maxusers'>\n" +
                "        <value>10</value>\n" +
                "        <option label='10'><value>10</value></option>\n" +
                "        <option label='20'><value>20</value></option>\n" +
                "        <option label='30'><value>30</value></option>\n" +
                "        <option label='50'><value>50</value></option>\n" +
                "        <option label='100'><value>100</value></option>\n" +
                "        <option label='None'><value>none</value></option>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Roles for which Presence is Broadcasted'\n" +
                "          type='list-multi'\n" +
                "          var='muc#roomconfig_presencebroadcast'>\n" +
                "        <value>moderator</value>\n" +
                "        <value>participant</value>\n" +
                "        <value>visitor</value>\n" +
                "        <option label='Moderator'><value>moderator</value></option>\n" +
                "        <option label='Participant'><value>participant</value></option>\n" +
                "        <option label='Visitor'><value>visitor</value></option>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Roles and Affiliations that May Retrieve Member List'\n" +
                "          type='list-multi'\n" +
                "          var='muc#roomconfig_getmemberlist'>\n" +
                "        <value>moderator</value>\n" +
                "        <value>participant</value>\n" +
                "        <value>visitor</value>\n" +
                "        <option label='Moderator'><value>moderator</value></option>\n" +
                "        <option label='Participant'><value>participant</value></option>\n" +
                "        <option label='Visitor'><value>visitor</value></option>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Make Room Publicly Searchable?'\n" +
                "          type='boolean'\n" +
                "          var='muc#roomconfig_publicroom'>\n" +
                "        <value>0</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Make Room Persistent?'\n" +
                "          type='boolean'\n" +
                "          var='muc#roomconfig_persistentroom'>\n" +
                "        <value>0</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Make Room Moderated?'\n" +
                "          type='boolean'\n" +
                "          var='muc#roomconfig_moderatedroom'>\n" +
                "        <value>0</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Make Room Members Only?'\n" +
                "          type='boolean'\n" +
                "          var='muc#roomconfig_membersonly'>\n" +
                "        <value>0</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Password Required for Entry?'\n" +
                "          type='boolean'\n" +
                "          var='muc#roomconfig_passwordprotectedroom'>\n" +
                "        <value>1</value>\n" +
                "      </field>\n" +
                "      <field type='fixed'>\n" +
                "        <value>\n" +
                "          If a password is required to enter this room,\n" +
                "          you must specify the password below.\n" +
                "        </value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Password'\n" +
                "          type='text-private'\n" +
                "          var='muc#roomconfig_roomsecret'>\n" +
                "        <value>cauldronburn</value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Who May Discover Real JIDs?'\n" +
                "          type='list-single'\n" +
                "          var='muc#roomconfig_whois'>\n" +
                "        <value>moderators</value>\n" +
                "        <option label='Moderators Only'>\n" +
                "          <value>moderators</value>\n" +
                "        </option>\n" +
                "        <option label='Anyone'>\n" +
                "          <value>anyone</value>\n" +
                "        </option>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Maximum Number of History Messages Returned by Room'\n" +
                "          type='text-single'\n" +
                "          var='muc#maxhistoryfetch'>\n" +
                "        <value>50</value>\n" +
                "      </field>\n" +
                "      <field type='fixed'>\n" +
                "        <value>\n" +
                "          You may specify additional people who have\n" +
                "          admin status in the room. Please\n" +
                "          provide one Jabber ID per line.\n" +
                "        </value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Room Admins'\n" +
                "          type='jid-multi'\n" +
                "          var='muc#roomconfig_roomadmins'>\n" +
                "        <value>wiccarocks@shakespeare.lit</value>\n" +
                "        <value>hecate@shakespeare.lit</value>\n" +
                "      </field>\n" +
                "      <field type='fixed'>\n" +
                "        <value>\n" +
                "          You may specify additional owners for this\n" +
                "          room. Please provide one Jabber ID per line.\n" +
                "        </value>\n" +
                "      </field>\n" +
                "      <field\n" +
                "          label='Room Owners'\n" +
                "          type='jid-multi'\n" +
                "          var='muc#roomconfig_roomowners'/>\n" +
                "    </x>\n";

        DataForm dataForm = unmarshal(xml, DataForm.class);

        RoomConfigurationForm roomConfigurationForm = new RoomConfigurationForm(dataForm);

        Assert.assertEquals(roomConfigurationForm.getName(), "A Dark Cave");
        Assert.assertEquals(roomConfigurationForm.getDescription(), "The place for all good witches!");
        Assert.assertTrue(roomConfigurationForm.isPublicLoggingEnabled());
        Assert.assertFalse(roomConfigurationForm.isChangeSubjectAllowed());
        Assert.assertFalse(roomConfigurationForm.isInviteAllowed());
        Assert.assertEquals(roomConfigurationForm.getMaxOccupants(), 10);
        Assert.assertFalse(roomConfigurationForm.isPublic());
        Assert.assertFalse(roomConfigurationForm.isPersistent());
        Assert.assertFalse(roomConfigurationForm.isModerated());
        Assert.assertFalse(roomConfigurationForm.isMembersOnly());
        Assert.assertTrue(roomConfigurationForm.isPasswordProtected());
        Assert.assertEquals(roomConfigurationForm.getPassword(), "cauldronburn");
        Assert.assertEquals(roomConfigurationForm.getMaxHistoryMessages(), 50);
        Assert.assertEquals(roomConfigurationForm.getAdministrators().size(), 2);
        Assert.assertEquals(roomConfigurationForm.getAdministrators().get(0), Jid.valueOf("wiccarocks@shakespeare.lit"));
        Assert.assertEquals(roomConfigurationForm.getAdministrators().get(1), Jid.valueOf("hecate@shakespeare.lit"));
    }
}
