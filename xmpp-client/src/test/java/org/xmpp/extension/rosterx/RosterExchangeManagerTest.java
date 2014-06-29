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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.im.Contact;
import org.xmpp.im.Roster;
import org.xmpp.stanza.AbstractIQ;
import org.xmpp.stanza.client.IQ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class RosterExchangeManagerTest extends BaseTest {

    @BeforeClass
    public void prepareRoster() throws Exception {
        Roster roster = new Roster();
        roster.getContacts().add(new Contact(Jid.valueOf("juliet@example.net"), "juliet", "friends", "friends2"));
        roster.getContacts().add(new Contact(Jid.valueOf("romeo@example.net"), "romeo", "friends"));
        IQ iq = new IQ(AbstractIQ.Type.SET, roster);
        // Simulate a roster push in order to fill the roster.
        xmppSession.handleElement(iq);
    }

    @Test
    public void testRosterItemAdditionWhenContactDoesNotExist() {
        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        List<RosterExchange.Item> suggestedContacts = new ArrayList<>();
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("contact@example.net"), "juliet", Arrays.asList("friends"), RosterExchange.Item.Action.ADD));

        List<RosterExchange.Item> items = rosterExchangeManager.getItemsToProcess(suggestedContacts);

        Assert.assertEquals(items.size(), 1);
        RosterExchange.Item item = items.get(0);
        Assert.assertEquals(item.getAction(), RosterExchange.Item.Action.ADD);
        Assert.assertEquals(item.getGroups().size(), 1);
        Assert.assertEquals(item.getGroups().get(0), "friends");
    }

    @Test
    public void testRosterItemAdditionWhenContactAlreadyExists() throws Exception {

        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        List<RosterExchange.Item> suggestedContacts = new ArrayList<>();
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Arrays.asList("friends"), RosterExchange.Item.Action.ADD));

        List<RosterExchange.Item> items = rosterExchangeManager.getItemsToProcess(suggestedContacts);

        // Contact already exists in roster (and in same group), therefore do nothing.
        Assert.assertEquals(items.size(), 0);

        // Contact already exists, but not in the suggested group
        List<RosterExchange.Item> suggestedContacts2 = new ArrayList<>();
        suggestedContacts2.add(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Arrays.asList("newGroup"), RosterExchange.Item.Action.ADD));

        List<RosterExchange.Item> items2 = rosterExchangeManager.getItemsToProcess(suggestedContacts2);
        Assert.assertEquals(items2.size(), 1);
        RosterExchange.Item item = items2.get(0);
        Assert.assertEquals(item.getAction(), RosterExchange.Item.Action.ADD);
        Assert.assertEquals(item.getGroups().size(), 1);
        Assert.assertEquals(item.getGroups().get(0), "newGroup");
    }

    @Test
    public void testRosterItemDeletionWhenContactDoesNotExist() throws Exception {
        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        List<RosterExchange.Item> suggestedContacts = new ArrayList<>();
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("contact@example.net"), "juliet", Arrays.asList("friends"), RosterExchange.Item.Action.DELETE));

        List<RosterExchange.Item> items = rosterExchangeManager.getItemsToProcess(suggestedContacts);

        // Contact does not exist in roster, therefore do nothing.
        Assert.assertEquals(items.size(), 0);
    }

    @Test
    public void testRosterItemDeletionWhenContactDoesExistButNotInSpecifiedGroup() throws Exception {
        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        List<RosterExchange.Item> suggestedContacts = new ArrayList<>();
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Arrays.asList("otherGroup"), RosterExchange.Item.Action.DELETE));

        List<RosterExchange.Item> items = rosterExchangeManager.getItemsToProcess(suggestedContacts);

        // Contact does exist in roster, but not in the specified group, therefore do nothing.
        Assert.assertEquals(items.size(), 0);
    }

    @Test
    public void testRosterItemDeletionWhenContactDoesExistInSpecifiedGroup() throws Exception {

        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        List<RosterExchange.Item> suggestedContacts = new ArrayList<>();
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Arrays.asList("friends", "unknownGroup"), RosterExchange.Item.Action.DELETE));

        List<RosterExchange.Item> items = rosterExchangeManager.getItemsToProcess(suggestedContacts);

        // Contact does exist in roster, but not in the specified group, therefore do nothing.
        Assert.assertEquals(items.size(), 1);
        RosterExchange.Item item = items.get(0);
        Assert.assertEquals(item.getGroups().size(), 1);
        Assert.assertEquals(item.getGroups().get(0), "friends");
    }

    @Test
    public void testRosterItemDeletionWhenNoGroupSpecified() throws Exception {

        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        List<RosterExchange.Item> suggestedContacts = new ArrayList<>();
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Collections.<String>emptyList(), RosterExchange.Item.Action.DELETE));

        List<RosterExchange.Item> items = rosterExchangeManager.getItemsToProcess(suggestedContacts);

        // Contact does exist in roster, but not in the specified group, therefore do nothing.
        Assert.assertEquals(items.size(), 0);
    }

    @Test
    public void testRosterItemModificationIfContactDoesExist() throws Exception {

        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        List<RosterExchange.Item> suggestedContacts = new ArrayList<>();
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "Juliet", Arrays.asList("newGroup1"), RosterExchange.Item.Action.MODIFY));

        List<RosterExchange.Item> items = rosterExchangeManager.getItemsToProcess(suggestedContacts);

        // Only one contact in roster.
        Assert.assertEquals(items.size(), 1);
        RosterExchange.Item item = items.get(0);
        Assert.assertEquals(item.getGroups().size(), 1);
        Assert.assertEquals(item.getGroups().get(0), "newGroup1");
        Assert.assertEquals(item.getName(), "Juliet");
    }

    @Test
    public void testRosterItemModification() throws Exception {

        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        List<RosterExchange.Item> suggestedContacts = new ArrayList<>();
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("contact@example.net"), "contact", Collections.<String>emptyList(), RosterExchange.Item.Action.MODIFY));
        suggestedContacts.add(new RosterExchange.Item(Jid.valueOf("romeo@example.net"), "romeo", Collections.<String>emptyList(), RosterExchange.Item.Action.MODIFY));

        List<RosterExchange.Item> items = rosterExchangeManager.getItemsToProcess(suggestedContacts);

        // Only one contact in roster.
        Assert.assertEquals(items.size(), 1);
    }

    @Test
    public void testRosterItemApproval() throws Exception {

        RosterExchangeManager rosterExchangeManager = xmppSession.getExtensionManager(RosterExchangeManager.class);

        // Does already exist, therefore return null.
        Assert.assertNull(rosterExchangeManager.approve(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Arrays.asList("friends"), RosterExchange.Item.Action.ADD)));

        // Does already exist, but not in all groups, therefore return modify.
        Assert.assertEquals(rosterExchangeManager.approve(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Arrays.asList("friends", "newGroup"), RosterExchange.Item.Action.ADD)), RosterExchange.Item.Action.MODIFY);

        // Does not yet exist
        Assert.assertEquals(rosterExchangeManager.approve(new RosterExchange.Item(Jid.valueOf("contact1@example.net"), "contact1", Arrays.asList("friends", "newGroup"), RosterExchange.Item.Action.ADD)), RosterExchange.Item.Action.ADD);

        // Does not exist, therefore return null.
        Assert.assertNull(rosterExchangeManager.approve(new RosterExchange.Item(Jid.valueOf("contact2@example.net"), "contact2", Collections.<String>emptyList(), RosterExchange.Item.Action.DELETE)));

        // Is only removed from group "friends", but still in group "friends2".
        Assert.assertEquals(rosterExchangeManager.approve(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Arrays.asList("friends"), RosterExchange.Item.Action.DELETE)), RosterExchange.Item.Action.MODIFY);

        // Is removed completely.
        Assert.assertEquals(rosterExchangeManager.approve(new RosterExchange.Item(Jid.valueOf("juliet@example.net"), "juliet", Arrays.asList("friends", "friends2"), RosterExchange.Item.Action.DELETE)), RosterExchange.Item.Action.DELETE);

        // Does not exist, therefore return null.
        Assert.assertNull(rosterExchangeManager.approve(new RosterExchange.Item(Jid.valueOf("contact3@example.net"), "contact3", Collections.<String>emptyList(), RosterExchange.Item.Action.MODIFY)));

        // Does not exist, therefore return null.
        Assert.assertEquals(rosterExchangeManager.approve(new RosterExchange.Item(Jid.valueOf("romeo@example.net"), "Romeo", Collections.<String>emptyList(), RosterExchange.Item.Action.MODIFY)), RosterExchange.Item.Action.MODIFY);
    }
}
