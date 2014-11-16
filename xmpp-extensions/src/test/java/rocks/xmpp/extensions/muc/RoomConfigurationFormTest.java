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

package rocks.xmpp.extensions.muc;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.muc.model.Role;
import rocks.xmpp.extensions.muc.model.RoomConfigurationForm;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author Christian Schudt
 */
public class RoomConfigurationFormTest extends XmlTest {
    protected RoomConfigurationFormTest() throws JAXBException, XMLStreamException {
        super(DataForm.class);
    }

    @Test
    public void testRoomConfigurationForm() throws JAXBException, XMLStreamException {

        RoomConfigurationForm roomConfigurationForm = RoomConfigurationForm.builder()
                .maxHistoryMessages(4)
                .rolesThatMaySendPrivateMessages(EnumSet.of(Role.MODERATOR, Role.PARTICIPANT))
                .allowInvites(true)
                .allowChangeSubject(true)
                .enableLogging(true)
                .rolesThatMayRetrieveMemberList(EnumSet.of(Role.PARTICIPANT))
                .language("en")
                .pubSubNode(URI.create("xmpp:pubsub.shakespeare.lit?;node=princely_musings"))
                .maxUsers(30)
                .membersOnly(true)
                .moderated(true)
                .passwordProtected(true)
                .persistent(true)
                .rolesForWhichPresenceIsBroadcast(EnumSet.of(Role.MODERATOR, Role.PARTICIPANT))
                .publicRoom(true)
                .administrators(Arrays.asList(Jid.valueOf("admin1"), Jid.valueOf("admin2")))
                .description("description")
                .name("name")
                .owners(Arrays.asList(Jid.valueOf("owner1"), Jid.valueOf("owner2")))
                .password("pass")
                .rolesThatMayDiscoverRealJids(EnumSet.of(Role.MODERATOR))
                .build();

        String xml = marshal(roomConfigurationForm.getDataForm());

        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"submit\">" +
                "<field type=\"hidden\" var=\"FORM_TYPE\"><value>http://jabber.org/protocol/muc#roomconfig</value></field>" +
                "<field type=\"text-single\" var=\"muc#maxhistoryfetch\"><value>4</value></field>" +
                "<field type=\"list-single\" var=\"muc#roomconfig_allowpm\"><value>anyone</value></field>" +
                "<field type=\"boolean\" var=\"muc#roomconfig_allowinvites\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"muc#roomconfig_changesubject\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"muc#roomconfig_enablelogging\"><value>1</value></field>" +
                "<field type=\"list-multi\" var=\"muc#roomconfig_getmemberlist\"><value>participant</value></field>" +
                "<field type=\"text-single\" var=\"muc#roomconfig_lang\"><value>en</value></field>" +
                "<field type=\"text-single\" var=\"muc#roomconfig_pubsub\"><value>xmpp:pubsub.shakespeare.lit?;node=princely_musings</value></field>" +
                "<field type=\"list-single\" var=\"muc#roomconfig_maxusers\"><value>30</value></field>" +
                "<field type=\"boolean\" var=\"muc#roomconfig_membersonly\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"muc#roomconfig_moderatedroom\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"muc#roomconfig_passwordprotectedroom\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"muc#roomconfig_persistentroom\"><value>1</value></field>" +
                "<field type=\"list-multi\" var=\"muc#roomconfig_presencebroadcast\"><value>moderator</value><value>participant</value></field>" +
                "<field type=\"boolean\" var=\"muc#roomconfig_publicroom\"><value>1</value></field>" +
                "<field type=\"jid-multi\" var=\"muc#roomconfig_roomadmins\"><value>admin1</value><value>admin2</value></field>" +
                "<field type=\"text-single\" var=\"muc#roomconfig_roomdesc\"><value>description</value></field>" +
                "<field type=\"text-single\" var=\"muc#roomconfig_roomname\"><value>name</value></field>" +
                "<field type=\"jid-multi\" var=\"muc#roomconfig_roomowners\"><value>owner1</value><value>owner2</value></field>" +
                "<field type=\"text-single\" var=\"muc#roomconfig_roomsecret\"><value>pass</value></field>" +
                "<field type=\"list-single\" var=\"muc#roomconfig_whois\"><value>moderators</value></field>" +
                "</x>");


        DataForm dataForm = unmarshal(xml, DataForm.class);

        RoomConfigurationForm roomConfigurationForm1 = new RoomConfigurationForm(dataForm);

        Assert.assertEquals(roomConfigurationForm1.getMaxHistoryMessages(), Integer.valueOf(4));
        Assert.assertEquals(roomConfigurationForm1.getRolesThatMaySendPrivateMessages(), Arrays.asList(Role.MODERATOR, Role.PARTICIPANT));
        Assert.assertTrue(roomConfigurationForm1.isAllowInvites());
        Assert.assertTrue(roomConfigurationForm1.isAllowChangeSubject());
        Assert.assertTrue(roomConfigurationForm1.isPublicLoggingEnabled());
        Assert.assertEquals(roomConfigurationForm1.getRolesThatMayRetrieveMemberList(), Arrays.asList(Role.PARTICIPANT));
        Assert.assertEquals(roomConfigurationForm1.getLanguage(), "en");
        Assert.assertEquals(roomConfigurationForm1.getPubSubNode(), URI.create("xmpp:pubsub.shakespeare.lit?;node=princely_musings"));
        Assert.assertEquals(roomConfigurationForm1.getMaxUsers(), Integer.valueOf(30));
        Assert.assertTrue(roomConfigurationForm1.isMembersOnly());
        Assert.assertTrue(roomConfigurationForm1.isModerated());
        Assert.assertTrue(roomConfigurationForm1.isPasswordProtected());
        Assert.assertTrue(roomConfigurationForm1.isPersistent());
        Assert.assertEquals(roomConfigurationForm1.getRolesForWhichPresenceIsBroadcast(), Arrays.asList(Role.MODERATOR, Role.PARTICIPANT));
        Assert.assertTrue(roomConfigurationForm1.isPublicRoom());
        Assert.assertEquals(roomConfigurationForm1.getAdministrators(), Arrays.asList(Jid.valueOf("admin1"), Jid.valueOf("admin2")));
        Assert.assertEquals(roomConfigurationForm1.getDescription(), "description");
        Assert.assertEquals(roomConfigurationForm1.getName(), "name");
        Assert.assertEquals(roomConfigurationForm1.getOwners(), Arrays.asList(Jid.valueOf("owner1"), Jid.valueOf("owner2")));
        Assert.assertEquals(roomConfigurationForm1.getPassword(), "pass");
        Assert.assertEquals(roomConfigurationForm1.getRolesThatMayDiscoverRealJids(), Arrays.asList(Role.MODERATOR));
    }
}
