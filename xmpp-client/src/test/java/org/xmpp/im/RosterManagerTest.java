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

package org.xmpp.im;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.TestConnection;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class RosterManagerTest extends BaseTest {

    @Test
    public void testRosterListener() throws XMLStreamException, JAXBException {
        final int[] rosterPushCount = new int[1];

        RosterManager rosterManager = new RosterManager(new TestConnection());
        rosterManager.addRosterListener(new RosterListener() {
            @Override
            public void rosterChanged(RosterEvent e) {
                if (rosterPushCount[0] == 0) {
                    Assert.assertEquals(e.getAddedContacts().size(), 3);
                    Assert.assertEquals(e.getUpdatedContacts().size(), 0);
                    Assert.assertEquals(e.getRemovedContacts().size(), 0);
                } else if (rosterPushCount[0] == 1) {
                    Assert.assertEquals(e.getAddedContacts().size(), 1);
                    Assert.assertEquals(e.getUpdatedContacts().size(), 0);
                    Assert.assertEquals(e.getRemovedContacts().size(), 0);
                } else if (rosterPushCount[0] == 2) {
                    Assert.assertEquals(e.getAddedContacts().size(), 0);
                    Assert.assertEquals(e.getUpdatedContacts().size(), 0);
                    Assert.assertEquals(e.getRemovedContacts().size(), 1);
                    Assert.assertEquals(e.getRemovedContacts().get(0).getJid(), Jid.valueOf("contact2@domain"));
                } else if (rosterPushCount[0] == 3) {
                    Assert.assertEquals(e.getAddedContacts().size(), 0);
                    Assert.assertEquals(e.getUpdatedContacts().size(), 1);
                    Assert.assertEquals(e.getRemovedContacts().size(), 0);
                    Assert.assertEquals(e.getUpdatedContacts().get(0).getJid(), Jid.valueOf("contact1@domain"));
                    Assert.assertEquals(e.getUpdatedContacts().get(0).getName(), "Name");
                }
            }
        });

        Roster roster1 = new Roster();
        roster1.getContacts().add(new Contact(Jid.valueOf("contact1@domain")));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact2@domain")));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact3@domain")));
        rosterManager.updateRoster(roster1, false);
        rosterPushCount[0]++;

        Roster roster2 = new Roster();
        roster2.getContacts().add(new Contact(Jid.valueOf("contact4@domain")));
        rosterManager.updateRoster(roster2, true);

        rosterPushCount[0]++;
        Roster roster3 = new Roster();
        Contact contact = new Contact(Jid.valueOf("contact2@domain"));
        contact.setSubscription(Contact.Subscription.REMOVE);
        roster3.getContacts().add(contact);
        rosterManager.updateRoster(roster3, true);

        rosterPushCount[0]++;
        Roster roster4 = new Roster();
        Contact contact2 = new Contact(Jid.valueOf("contact1@domain"));
        contact2.setName("Name");
        roster4.getContacts().add(contact2);
        rosterManager.updateRoster(roster4, true);
    }

    @Test
    public void testRosterGroups() {
        RosterManager rosterManager = new RosterManager(new TestConnection());

        Roster roster1 = new Roster();
        roster1.getContacts().add(new Contact(Jid.valueOf("contact1@domain"), "contact1", "Group1"));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact2@domain"), "contact2", "Group2"));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact4@domain"), "contact4", "Group3"));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact3@domain"), "contact3", "Group3"));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact5@domain"), "contact5", "Group3"));
        rosterManager.updateRoster(roster1, false);

        List<ContactGroup> list = new ArrayList<>(rosterManager.getContactGroups());
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.get(0).getName(), "Group1");
        Assert.assertEquals(list.get(0).getContacts().size(), 1);
        Assert.assertEquals(list.get(0).getContacts().iterator().next().getJid(), Jid.valueOf("contact1@domain"));
        Assert.assertEquals(list.get(1).getName(), "Group2");
        Assert.assertEquals(list.get(1).getContacts().iterator().next().getJid(), Jid.valueOf("contact2@domain"));
        Assert.assertEquals(list.get(2).getName(), "Group3");
        Iterator<Contact> iterator = list.get(2).getContacts().iterator();
        Assert.assertEquals(iterator.next().getJid(), Jid.valueOf("contact3@domain"));
        Assert.assertEquals(iterator.next().getJid(), Jid.valueOf("contact4@domain"));
        Assert.assertEquals(iterator.next().getJid(), Jid.valueOf("contact5@domain"));
    }

    @Test
    public void testNestedRosterGroups() {
        RosterManager rosterManager = new RosterManager(new TestConnection());
        rosterManager.setGroupDelimiter("::");
        Roster roster1 = new Roster();
        roster1.getContacts().add(new Contact(Jid.valueOf("contact3@domain"), "contact3", "Group3::SubGroup"));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact4@domain"), "contact4", "Group3::SubGroup::3rdLevel"));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact5@domain"), "contact5", "Group3"));
        rosterManager.updateRoster(roster1, false);

        List<ContactGroup> list = new ArrayList<>(rosterManager.getContactGroups());
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0).getContacts().iterator().next().getJid(), Jid.valueOf("contact5@domain"));
        Assert.assertEquals(list.get(0).getName(), "Group3");
        Assert.assertEquals(list.get(0).getContacts().size(), 1);
        Assert.assertEquals(list.get(0).getGroups().size(), 1);

        ContactGroup contactGroup = list.get(0).getGroups().iterator().next();
        Assert.assertEquals(contactGroup.getName(), "SubGroup");
        Assert.assertEquals(contactGroup.getGroups().size(), 1);
        Assert.assertEquals(contactGroup.getContacts().size(), 1);
        Assert.assertEquals(contactGroup.getContacts().iterator().next().getJid(), Jid.valueOf("contact3@domain"));
        ContactGroup nestedGroup = contactGroup.getGroups().iterator().next();
        Assert.assertEquals(nestedGroup.getName(), "3rdLevel");
        Assert.assertEquals(nestedGroup.getContacts().size(), 1);
        Assert.assertEquals(nestedGroup.getContacts().iterator().next().getJid(), Jid.valueOf("contact4@domain"));
    }

    @Test
    public void testRosterIntegrity() {

        RosterManager rosterManager = new RosterManager(new TestConnection());

        // Initial roster
        Roster roster1 = new Roster();
        roster1.getContacts().add(new Contact(Jid.valueOf("contact1@domain"), "contact1", "group1"));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact2@domain"), "contact2", "group2"));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact3@domain"), "contact3", true, Contact.Subscription.FROM));
        roster1.getContacts().add(new Contact(Jid.valueOf("contact4@domain"), "contact4", true, Contact.Subscription.FROM, "group2"));
        rosterManager.updateRoster(roster1, false);

        Assert.assertEquals(rosterManager.getUnaffiliatedContacts().size(), 1);
        Assert.assertEquals(rosterManager.getUnaffiliatedContacts().iterator().next().getSubscription(), Contact.Subscription.FROM);
        List<ContactGroup> groups = new ArrayList<>(rosterManager.getContactGroups());
        Assert.assertEquals(groups.get(0).getContacts().size(), 1);
        Assert.assertEquals(groups.get(1).getContacts().size(), 2);

        Roster roster2 = new Roster();
        roster2.getContacts().add(new Contact(Jid.valueOf("contact3@domain"), "contact3", true, Contact.Subscription.BOTH));
        rosterManager.updateRoster(roster2, true);

        Assert.assertEquals(rosterManager.getUnaffiliatedContacts().size(), 1);
        Assert.assertEquals(rosterManager.getUnaffiliatedContacts().iterator().next().getSubscription(), Contact.Subscription.BOTH);

        Assert.assertEquals(rosterManager.getContactGroups().size(), 2);

        Roster roster3 = new Roster();
        roster3.getContacts().add(new Contact(Jid.valueOf("contact2@domain"), "contact2", true, Contact.Subscription.TO, "group1"));
        rosterManager.updateRoster(roster3, true);

        groups = new ArrayList<>(rosterManager.getContactGroups());
        List<Contact> contacts = new ArrayList<>(groups.get(0).getContacts());
        Assert.assertEquals(groups.get(0).getContacts().size(), 2);
        Assert.assertEquals(contacts.get(1).getSubscription(), Contact.Subscription.TO);
        Assert.assertTrue(contacts.get(1).isPending());
        Assert.assertEquals(groups.get(1).getContacts().size(), 1);

        Roster roster4 = new Roster();
        Contact contact2 = new Contact(Jid.valueOf("contact3@domain"), "", false, Contact.Subscription.REMOVE);
        roster4.getContacts().add(contact2);
        rosterManager.updateRoster(roster4, true);
        Assert.assertTrue(rosterManager.getUnaffiliatedContacts().isEmpty());
    }
}
