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
import org.mockito.InOrder;
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
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.model.SubscriptionState;
import rocks.xmpp.session.server.InboundStanzaProcessor;
import rocks.xmpp.session.server.InboundSubscriptionHandler;
import rocks.xmpp.session.server.SessionManager;
import rocks.xmpp.session.server.OutboundStanzaProcessor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class InboundSubscriptionHandlerTest {

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
    private Session contactResource1;

    @Mock
    private Session contactResource2;

    @Mock
    private InboundStanzaProcessor inboundStanzaProcessor;

    @Mock
    private OutboundStanzaProcessor outboundStanzaProcessor;

    @InjectMocks
    private InboundSubscriptionHandler subscriptionHandler;

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(serverConfiguration.getDomain()).thenReturn(Jid.of("server"));
        Mockito.when(resource1.getRemoteXmppAddress()).thenReturn(Jid.of("user@server/resource1"));
        Mockito.when(resource2.getRemoteXmppAddress()).thenReturn(Jid.of("user@server/resource2"));
        Mockito.when(resource1.getRemoteXmppAddress()).thenReturn(Jid.of("contact@server/resource1"));
        Mockito.when(resource2.getRemoteXmppAddress()).thenReturn(Jid.of("contact@server/resource2"));
    }

    @BeforeMethod
    public void reset() {
        Mockito.clearInvocations(rosterManager, inboundStanzaProcessor, outboundStanzaProcessor, resource1, resource2, contactResource1, contactResource2);
        Mockito.when(sessionManager.getUserSessions(Mockito.eq(Jid.of("user@server")))).thenReturn(Stream.of(resource1, resource2));
        Mockito.when(sessionManager.getUserSessions(Mockito.eq(Jid.of("contact@server")))).thenReturn(Stream.of(contactResource1, contactResource2));
    }

    /**
     * If the JID is of the form {@code <contact@domainpart/resourcepart>} instead of {@code <contact@domainpart>},
     * the user's server SHOULD treat it as if the request had been directed to the contact's bare JID and modify the 'to' address accordingly.
     */
    @Test
    public void shouldStampToAttributeWithBareJid() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBE);
        presence.setTo(Jid.of("user@server/full"));
        subscriptionHandler.process(presence);

        Assert.assertEquals(presence.getTo(), Jid.of("user@server"));
    }

    @Test
    public void testProcessingOfInboundSubscriptionRequestIfUserExistsAndContactHasSubscriptionToUser() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBE);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(new Contact(presence.getTo(), "contact", false, false, SubscriptionState.Subscription.BOTH, Collections.emptyList()));

        subscriptionHandler.process(presence);

        ArgumentCaptor<Presence> presenceArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        Mockito.verify(outboundStanzaProcessor).process(presenceArgumentCaptor.capture());
        Assert.assertEquals(presenceArgumentCaptor.getValue().getType(), Presence.Type.SUBSCRIBED);
        Assert.assertEquals(presenceArgumentCaptor.getValue().getTo(), presence.getFrom());

        Mockito.verify(rosterManager, Mockito.times(0)).setRosterItem(Mockito.eq("user"), Mockito.any());
    }

    @Test
    public void testProcessingOfInboundSubscriptionApprovalIfWrongSubscriptionState() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBED);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getFrom();
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
                return Collections.emptyList();
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.TO;
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

        subscriptionHandler.process(presence);

        Mockito.verify(rosterManager, Mockito.times(0)).setRosterItem(Mockito.eq("user"), Mockito.any());
    }

    @Test
    public void testProcessingOfInboundSubscriptionApprovalIfPendingOutNoneSubscriptionState() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBED);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getFrom();
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
                return Collections.emptyList();
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.NONE;
            }

            @Override
            public boolean isPendingOut() {
                return true;
            }

            @Override
            public boolean isPendingIn() {
                return false;
            }
        });

        subscriptionHandler.process(presence);

        InOrder inOrder = Mockito.inOrder(resource1, resource2, rosterManager, inboundStanzaProcessor);

        ArgumentCaptor<Presence> subscribedArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(resource1, Mockito.times(1)).send(subscribedArgumentCaptor.capture());
        Assert.assertEquals(subscribedArgumentCaptor.getValue().getType(), Presence.Type.SUBSCRIBED);
        Assert.assertEquals(subscribedArgumentCaptor.getValue().getTo(), presence.getTo());

        ArgumentCaptor<Presence> subscribedArgumentCaptor2 = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(resource2, Mockito.times(1)).send(subscribedArgumentCaptor2.capture());
        Assert.assertEquals(subscribedArgumentCaptor2.getValue().getType(), Presence.Type.SUBSCRIBED);
        Assert.assertEquals(subscribedArgumentCaptor2.getValue().getTo(), presence.getTo());

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        inOrder.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getFrom());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.TO);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());

        ArgumentCaptor<Presence> presenceArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(inboundStanzaProcessor, Mockito.times(2)).process(presenceArgumentCaptor.capture());
        Assert.assertNull(presenceArgumentCaptor.getValue().getType());
        Assert.assertEquals(presenceArgumentCaptor.getValue().getTo(), presence.getTo());
    }

    @Test
    public void testProcessingOfInboundSubscriptionApprovalIfPendingOutFromSubscriptionState() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBED);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getFrom();
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
                return Collections.emptyList();
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.FROM;
            }

            @Override
            public boolean isPendingOut() {
                return true;
            }

            @Override
            public boolean isPendingIn() {
                return false;
            }
        });

        subscriptionHandler.process(presence);

        InOrder inOrder = Mockito.inOrder(resource1, resource2, rosterManager, inboundStanzaProcessor);

        ArgumentCaptor<Presence> subscribedArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(resource1, Mockito.times(1)).send(subscribedArgumentCaptor.capture());
        Assert.assertEquals(subscribedArgumentCaptor.getValue().getType(), Presence.Type.SUBSCRIBED);
        Assert.assertEquals(subscribedArgumentCaptor.getValue().getTo(), presence.getTo());

        ArgumentCaptor<Presence> subscribedArgumentCaptor2 = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(resource2, Mockito.times(1)).send(subscribedArgumentCaptor2.capture());
        Assert.assertEquals(subscribedArgumentCaptor2.getValue().getType(), Presence.Type.SUBSCRIBED);
        Assert.assertEquals(subscribedArgumentCaptor2.getValue().getTo(), presence.getTo());

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        inOrder.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getFrom());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.BOTH);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());

        ArgumentCaptor<Presence> presenceArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(inboundStanzaProcessor, Mockito.times(2)).process(presenceArgumentCaptor.capture());
        Assert.assertNull(presenceArgumentCaptor.getValue().getType());
        Assert.assertEquals(presenceArgumentCaptor.getValue().getTo(), presence.getTo());
    }

    @Test
    public void testProcessingOfInboundSubscriptionCancellationIfContactIsNotInUsersRoster() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBED);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(null);

        subscriptionHandler.process(presence);

        Mockito.verify(rosterManager, Mockito.times(0)).setRosterItem(Mockito.any(), Mockito.any());
    }

    @Test
    public void testProcessingOfInboundSubscriptionCancellationIfContactIsNotSubscribedToUser() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBED);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getFrom();
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
                return Collections.emptyList();
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.NONE;
            }

            @Override
            public boolean isPendingOut() {
                return true;
            }

            @Override
            public boolean isPendingIn() {
                return false;
            }
        });

        subscriptionHandler.process(presence);

        Mockito.verify(rosterManager, Mockito.times(0)).setRosterItem(Mockito.any(), Mockito.any());
    }

    @Test
    public void testProcessingOfInboundSubscriptionCancellationIfMutualSubscription() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBED);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getFrom();
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
                return Collections.emptyList();
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.BOTH;
            }

            @Override
            public boolean isPendingOut() {
                return true;
            }

            @Override
            public boolean isPendingIn() {
                return false;
            }
        });

        subscriptionHandler.process(presence);

        InOrder inOrder = Mockito.inOrder(resource1, resource2, rosterManager, inboundStanzaProcessor);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);

        ArgumentCaptor<Presence> subscribedArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(resource1, Mockito.times(1)).send(subscribedArgumentCaptor.capture());
        Assert.assertEquals(subscribedArgumentCaptor.getValue().getType(), Presence.Type.UNSUBSCRIBED);
        Assert.assertEquals(subscribedArgumentCaptor.getValue().getTo(), presence.getTo());

        ArgumentCaptor<Presence> subscribedArgumentCaptor2 = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(resource2, Mockito.times(1)).send(subscribedArgumentCaptor2.capture());
        Assert.assertEquals(subscribedArgumentCaptor2.getValue().getType(), Presence.Type.UNSUBSCRIBED);
        Assert.assertEquals(subscribedArgumentCaptor2.getValue().getTo(), presence.getTo());

        inOrder.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getFrom());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.FROM);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());

        ArgumentCaptor<Presence> presenceArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(inboundStanzaProcessor, Mockito.times(2)).process(presenceArgumentCaptor.capture());
        Assert.assertEquals(presenceArgumentCaptor.getValue().getType(), Presence.Type.UNAVAILABLE);
        Assert.assertEquals(presenceArgumentCaptor.getValue().getTo(), presence.getTo());
    }

    @Test
    public void testProcessingOfInboundUnsubscriptionRequestIfMutualSubscription() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBE);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getFrom();
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
                return Collections.emptyList();
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.BOTH;
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

        subscriptionHandler.process(presence);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        Mockito.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getFrom());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.TO);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());
    }

    @Test
    public void testProcessingOfOutboundUnsubscriptionRequestIfContactIsSubscribedToUser() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBE);
        presence.setFrom(Jid.of("contact@server"));
        presence.setTo(Jid.of("user@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getFrom()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getFrom();
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
                return Collections.emptyList();
            }

            @Override
            public Subscription getSubscription() {
                return Subscription.FROM;
            }

            @Override
            public boolean isPendingOut() {
                return true;
            }

            @Override
            public boolean isPendingIn() {
                return false;
            }
        });

        subscriptionHandler.process(presence);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        Mockito.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getFrom());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.NONE);
        Assert.assertTrue(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());
    }
}
