/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.im.roster.server;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.model.SubscriptionState;
import rocks.xmpp.im.roster.server.spi.RosterItemProvider;
import rocks.xmpp.session.server.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ServerRosterManagerTest {

    @Mock
    private RosterItemProvider rosterItemProvider;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private ServerConfiguration serverConfiguration;

    @Mock
    private Session resource1;

    @Mock
    private Session resource2;

    @InjectMocks
    private ServerRosterManager rosterManager;

    @BeforeClass
    public void init() {
        rosterManager = new ServerRosterManager();
        MockitoAnnotations.initMocks(this);

        Mockito.when(serverConfiguration.getDomain()).thenReturn(Jid.of("domain"));
        Mockito.when(resource1.getRemoteXmppAddress()).thenReturn(Jid.of("a@server/resource1"));
        Mockito.when(resource2.getRemoteXmppAddress()).thenReturn(Jid.of("a@server/resource2"));
    }

    @BeforeMethod
    public void reset() {
        Mockito.when(sessionManager.getUserSessions(Jid.of("a@domain"))).thenReturn(Stream.of(resource1, resource2));
        Mockito.clearInvocations(resource1, resource2);
    }

    /**
     * Tests, that creating a roster item, does create it in the provider and does a roster push.
     */
    @Test
    public void testAddRosterItem() {
        Jid contact = Jid.of("contact@server");
        Mockito.when(rosterItemProvider.get(Mockito.any(), Mockito.eq(contact))).thenReturn(null);

        rosterManager.setRosterItem("a", new Contact(contact, "contact", "friends"));

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        Mockito.verify(rosterItemProvider).create(usernameCaptor.capture(), rosterItemCaptor.capture());
        Assert.assertEquals(usernameCaptor.getValue(), "a");
        Assert.assertEquals(rosterItemCaptor.getValue().getName(), "contact");
        Assert.assertEquals(rosterItemCaptor.getValue().getGroups(), Collections.singletonList("friends"));
        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), contact);

        // Testing roster push
        ArgumentCaptor<StreamElement> rosterCaptor = ArgumentCaptor.forClass(StreamElement.class);
        Mockito.verify(resource1).send(rosterCaptor.capture());
        Mockito.verify(resource2).send(rosterCaptor.capture());

        Assert.assertEquals(rosterCaptor.getAllValues().size(), 2);

        Assert.assertTrue(rosterCaptor.getAllValues().get(0) instanceof IQ);
        IQ iq1 = (IQ) rosterCaptor.getAllValues().get(0);
        Assert.assertEquals(iq1.getTo(), resource1.getRemoteXmppAddress());
        Roster roster1 = iq1.getExtension(Roster.class);
        Assert.assertNotNull(roster1);
        Assert.assertEquals(roster1.getContacts().size(), 1);
        Contact contact1 = roster1.getContacts().get(0);
        Assert.assertEquals(contact1.getJid(), contact);
        Assert.assertEquals(contact1.getSubscription(), SubscriptionState.Subscription.NONE);
        Assert.assertEquals(contact1.getName(), "contact");
        Assert.assertEquals(contact1.getGroups(), Collections.singletonList("friends"));
        Assert.assertFalse(contact1.isPendingOut());

        Assert.assertTrue(rosterCaptor.getAllValues().get(1) instanceof IQ);
        IQ iq2 = (IQ) rosterCaptor.getAllValues().get(1);
        Assert.assertEquals(iq2.getTo(), resource2.getRemoteXmppAddress());
        Roster roster2 = iq2.getExtension(Roster.class);
        Assert.assertNotNull(roster2);
        Assert.assertEquals(roster2.getContacts().size(), 1);
        Contact contact2 = roster2.getContacts().get(0);
        Assert.assertEquals(contact2.getJid(), contact);
        Assert.assertEquals(contact2.getSubscription(), SubscriptionState.Subscription.NONE);
        Assert.assertEquals(contact2.getName(), "contact");
        Assert.assertEquals(contact2.getGroups(), Collections.singletonList("friends"));
        Assert.assertFalse(contact2.isPendingOut());
    }

    /**
     * Tests, that updating a roster item, does update it in the provider and does a roster push.
     */
    @Test
    public void testUpdateRosterItem() {
        Jid contact = Jid.of("contact2@server");
        Mockito.when(rosterItemProvider.get(Mockito.any(), Mockito.eq(contact))).thenReturn(new RosterItem() {
            private List<String> groups = new ArrayList<>();

            @Override
            public Jid getJid() {
                return contact;
            }

            @Override
            public String getName() {
                return "contact";
            }

            @Override
            public boolean isApproved() {
                return false;
            }

            @Override
            public List<String> getGroups() {
                return groups;
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.FROM;
            }

            @Override
            public boolean isPendingOut() {
                return false;
            }

            @Override
            public boolean isPendingIn() {
                return false;
            }
        });

        rosterManager.setRosterItem("a", new Contact(contact, "contact", "friends"));

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        Mockito.verify(rosterItemProvider).update(usernameCaptor.capture(), rosterItemCaptor.capture());
        Assert.assertEquals(usernameCaptor.getValue(), "a");
        Assert.assertEquals(rosterItemCaptor.getValue().getName(), "contact");
        Assert.assertEquals(rosterItemCaptor.getValue().getGroups(), Collections.singletonList("friends"));
        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), contact);

        // Testing roster push
        ArgumentCaptor<StreamElement> rosterCaptor = ArgumentCaptor.forClass(StreamElement.class);
        Mockito.verify(resource1).send(rosterCaptor.capture());
        Mockito.verify(resource2).send(rosterCaptor.capture());

        Assert.assertEquals(rosterCaptor.getAllValues().size(), 2);

        Assert.assertTrue(rosterCaptor.getAllValues().get(0) instanceof IQ);
        IQ iq1 = (IQ) rosterCaptor.getAllValues().get(0);
        Assert.assertEquals(iq1.getTo(), resource1.getRemoteXmppAddress());
        Roster roster1 = iq1.getExtension(Roster.class);
        Assert.assertNotNull(roster1);
        Assert.assertEquals(roster1.getContacts().size(), 1);
        Contact contact1 = roster1.getContacts().get(0);
        Assert.assertEquals(contact1.getJid(), contact);
        Assert.assertEquals(contact1.getSubscription(), SubscriptionState.Subscription.FROM);
        Assert.assertEquals(contact1.getName(), "contact");
        Assert.assertEquals(contact1.getGroups(), Collections.singletonList("friends"));
        Assert.assertFalse(contact1.isPendingOut());

        Assert.assertTrue(rosterCaptor.getAllValues().get(1) instanceof IQ);
        IQ iq2 = (IQ) rosterCaptor.getAllValues().get(1);
        Assert.assertEquals(iq2.getTo(), resource2.getRemoteXmppAddress());
        Roster roster2 = iq2.getExtension(Roster.class);
        Assert.assertNotNull(roster2);
        Assert.assertEquals(roster2.getContacts().size(), 1);
        Contact contact2 = roster2.getContacts().get(0);
        Assert.assertEquals(contact2.getJid(), contact);
        Assert.assertEquals(contact2.getSubscription(), SubscriptionState.Subscription.FROM);
        Assert.assertEquals(contact2.getName(), "contact");
        Assert.assertEquals(contact2.getGroups(), Collections.singletonList("friends"));
        Assert.assertFalse(contact2.isPendingOut());
    }

    /**
     * Tests, that deleting an existing roster item does interact with the provider and does a roster push.
     */
    @Test
    public void testDeleteExistingRosterItem() {
        Jid contact = Jid.of("contact@server");
        Mockito.when(rosterItemProvider.delete(Mockito.any(), Mockito.eq(contact))).thenReturn(new RosterItem() {

            @Override
            public Jid getJid() {
                return contact;
            }

            @Override
            public String getName() {
                return "contact";
            }

            @Override
            public boolean isApproved() {
                return false;
            }

            @Override
            public List<String> getGroups() {
                return Collections.singletonList("friends");
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.FROM;
            }

            @Override
            public boolean isPendingOut() {
                return false;
            }

            @Override
            public boolean isPendingIn() {
                return false;
            }
        });

        rosterManager.delete("a", contact);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Jid> jidItemCaptor = ArgumentCaptor.forClass(Jid.class);
        Mockito.verify(rosterItemProvider).delete(usernameCaptor.capture(), jidItemCaptor.capture());
        Assert.assertEquals(usernameCaptor.getValue(), "a");
        Assert.assertEquals(jidItemCaptor.getValue(), contact);

        // Testing roster push
        ArgumentCaptor<StreamElement> rosterCaptor = ArgumentCaptor.forClass(StreamElement.class);
        Mockito.verify(resource1).send(rosterCaptor.capture());
        Mockito.verify(resource2).send(rosterCaptor.capture());

        Assert.assertEquals(rosterCaptor.getAllValues().size(), 2);

        Assert.assertTrue(rosterCaptor.getAllValues().get(0) instanceof IQ);
        IQ iq1 = (IQ) rosterCaptor.getAllValues().get(0);
        Assert.assertEquals(iq1.getTo(), resource1.getRemoteXmppAddress());
        Roster roster1 = iq1.getExtension(Roster.class);
        Assert.assertNotNull(roster1);
        Assert.assertEquals(roster1.getContacts().size(), 1);
        Contact contact1 = roster1.getContacts().get(0);
        Assert.assertEquals(contact1.getJid(), contact);
        Assert.assertEquals(contact1.getSubscription(), SubscriptionState.Subscription.REMOVE);
        Assert.assertEquals(contact1.getName(), "contact");
        Assert.assertEquals(contact1.getGroups(), Collections.singletonList("friends"));
        Assert.assertFalse(contact1.isPendingOut());

        Assert.assertTrue(rosterCaptor.getAllValues().get(1) instanceof IQ);
        IQ iq2 = (IQ) rosterCaptor.getAllValues().get(1);
        Assert.assertEquals(iq2.getTo(), resource2.getRemoteXmppAddress());
        Roster roster2 = iq2.getExtension(Roster.class);
        Assert.assertNotNull(roster2);
        Assert.assertEquals(roster2.getContacts().size(), 1);
        Contact contact2 = roster2.getContacts().get(0);
        Assert.assertEquals(contact2.getJid(), contact);
        Assert.assertEquals(contact2.getSubscription(), SubscriptionState.Subscription.REMOVE);
        Assert.assertEquals(contact2.getName(), "contact");
        Assert.assertEquals(contact2.getGroups(), Collections.singletonList("friends"));
        Assert.assertFalse(contact2.isPendingOut());
    }

    /**
     * Tests, that deleting a non-existing roster item does not interact with any session, but just returns null.
     */
    @Test
    public void testDeleteNonExistingRosterItem() {
        Jid contact = Jid.of("contactNonExisting@server");
        Mockito.when(rosterItemProvider.delete(Mockito.any(), Mockito.eq(contact))).thenReturn(null);
        Assert.assertNull(rosterManager.delete("a", contact));
        Mockito.verifyNoInteractions(resource1);
        Mockito.verifyNoInteractions(resource2);
    }
}
