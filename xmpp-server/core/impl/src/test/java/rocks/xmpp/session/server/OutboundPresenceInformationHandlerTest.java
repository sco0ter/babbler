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

package rocks.xmpp.session.server;

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
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.im.roster.model.SubscriptionState;
import rocks.xmpp.im.roster.server.ServerRosterManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class OutboundPresenceInformationHandlerTest {

    @Mock
    private ServerRosterManager rosterManager;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private ServerConfiguration serverConfiguration;

    @Mock
    private Session resource1;

    @Mock
    private Session resource2;

    @Mock
    private OutboundStanzaProcessor outboundStanzaProcessor;

    @InjectMocks
    private OutboundPresenceInformationHandler presenceInformationHandler;

    @BeforeClass
    public void init() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(serverConfiguration.getDomain()).thenReturn(Jid.of("server"));
        Mockito.when(resource1.getRemoteXmppAddress()).thenReturn(Jid.of("user@server/resource1"));
        Mockito.when(resource2.getRemoteXmppAddress()).thenReturn(Jid.of("user@server/resource2"));
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(Jid.of("contact1@server"), "contact1", false, false, SubscriptionState.Subscription.FROM, Collections.emptyList()));
        contacts.add(new Contact(Jid.of("contact2@server"), "contact2", false, false, SubscriptionState.Subscription.TO, Collections.emptyList()));
        contacts.add(new Contact(Jid.of("contact3@server"), "contact3", false, false, SubscriptionState.Subscription.BOTH, Collections.emptyList()));
        contacts.add(new Contact(Jid.of("contact4@server"), "contact4", false, false, SubscriptionState.Subscription.NONE, Collections.emptyList()));
        Mockito.doReturn(contacts).when(rosterManager).getRosterItems("user");
        Mockito.when(rosterManager.getRosterItem("user", Jid.of("contact1@server"))).thenReturn(contacts.get(0));
        Mockito.when(rosterManager.getRosterItem("user", Jid.of("contact2@server"))).thenReturn(contacts.get(1));
        Mockito.when(rosterManager.getRosterItem("user", Jid.of("contact3@server"))).thenReturn(contacts.get(2));
        Mockito.when(rosterManager.getRosterItem("user", Jid.of("contact4@server"))).thenReturn(contacts.get(3));
    }

    @BeforeMethod
    public void reset() {
        Mockito.clearInvocations(rosterManager, outboundStanzaProcessor);
        Mockito.when(sessionManager.getUserSessions(Mockito.eq(Jid.of("user@server")))).thenReturn(Stream.of(resource1, resource2));
    }

    @Test
    public void testInitialPresence() {
        Presence presence = new Presence();
        presence.setFrom(Jid.of("user@server/resource1"));
        presenceInformationHandler.process(presence);

        Mockito.verify(rosterManager).getRosterItems(Mockito.eq("user"));

        ArgumentCaptor<Presence> presenceArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        Mockito.verify(outboundStanzaProcessor, Mockito.times(3)).process(presenceArgumentCaptor.capture());

        Assert.assertEquals(presenceArgumentCaptor.getAllValues().get(0).getTo(), Jid.of("contact1@server"));
        Assert.assertEquals(presenceArgumentCaptor.getAllValues().get(1).getTo(), Jid.of("contact3@server"));
        Assert.assertEquals(presenceArgumentCaptor.getAllValues().get(2).getTo(), Jid.of("user@server"));
        Assert.assertNull(presenceArgumentCaptor.getValue().getType());
    }

    @Test
    public void testDirectPresence() {
        Presence presence1 = new Presence();
        presence1.setFrom(Jid.of("user@server/resource1"));
        presence1.setTo(Jid.of("contact1@server"));

        presenceInformationHandler.process(presence1);

        Set<Jid> directPresenceRecipients = presenceInformationHandler.directPresences.get(presence1.getFrom());
        // contact1 is subscribed to user, no need to track the direct presence.
        Assert.assertNull(directPresenceRecipients);

        Presence presence2 = new Presence();
        presence2.setFrom(Jid.of("user@server/resource1"));
        presence2.setTo(Jid.of("contact2@server"));

        presenceInformationHandler.process(presence2);

        directPresenceRecipients = presenceInformationHandler.directPresences.get(presence2.getFrom());
        Assert.assertEquals(directPresenceRecipients.size(), 1);
        Assert.assertTrue(directPresenceRecipients.contains(presence2.getTo()));

        Presence presence3 = new Presence();
        presence3.setFrom(Jid.of("user@server/resource1"));
        presence3.setTo(Jid.of("contact3@server"));

        presenceInformationHandler.process(presence3);

        // contact1 is subscribed to user, no need to track the direct presence.
        directPresenceRecipients = presenceInformationHandler.directPresences.get(presence2.getFrom());
        Assert.assertEquals(directPresenceRecipients.size(), 1);
        Assert.assertTrue(directPresenceRecipients.contains(presence2.getTo()));

        Presence presence4 = new Presence();
        presence4.setFrom(Jid.of("user@server/resource1"));
        presence4.setTo(Jid.of("contact4@server"));

        presenceInformationHandler.process(presence4);
        directPresenceRecipients = presenceInformationHandler.directPresences.get(presence2.getFrom());
        Assert.assertEquals(directPresenceRecipients.size(), 2);
        Assert.assertTrue(directPresenceRecipients.contains(presence4.getTo()));

        // Direct unavailable presence
        Presence unavailableDirectPresence = new Presence(Presence.Type.UNAVAILABLE);
        unavailableDirectPresence.setFrom(Jid.of("user@server/resource1"));
        unavailableDirectPresence.setTo(Jid.of("contact2@server"));

        presenceInformationHandler.process(unavailableDirectPresence);

        directPresenceRecipients = presenceInformationHandler.directPresences.get(unavailableDirectPresence.getFrom());
        Assert.assertNotNull(directPresenceRecipients);
        Assert.assertEquals(directPresenceRecipients.size(), 1);
        Assert.assertTrue(directPresenceRecipients.contains(presence4.getTo()));

        Presence unavailablePresence = new Presence(Presence.Type.UNAVAILABLE);
        unavailablePresence.setFrom(Jid.of("user@server/resource1"));
        presenceInformationHandler.process(unavailablePresence);

        directPresenceRecipients = presenceInformationHandler.directPresences.get(unavailablePresence.getFrom());
        Assert.assertNull(directPresenceRecipients);
    }
}
