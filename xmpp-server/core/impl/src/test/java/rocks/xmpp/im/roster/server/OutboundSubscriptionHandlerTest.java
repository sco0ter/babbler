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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
import rocks.xmpp.session.server.OutboundSubscriptionHandler;
import rocks.xmpp.session.server.SessionManager;
import rocks.xmpp.session.server.StanzaRouter;

public class OutboundSubscriptionHandlerTest {

    @Mock
    private StanzaRouter stanzaRouter;

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

    @InjectMocks
    private OutboundSubscriptionHandler subscriptionHandler;

    @BeforeClass
    public void init() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(serverConfiguration.getDomain()).thenReturn(Jid.of("server"));
        Mockito.when(resource1.getRemoteXmppAddress()).thenReturn(Jid.of("user@server/resource1"));
        Mockito.when(resource2.getRemoteXmppAddress()).thenReturn(Jid.of("user@server/resource2"));
        Mockito.when(sessionManager.getUserSessions(Mockito.eq(Jid.of("user@server")))).thenReturn(Stream.of(resource1, resource2));
    }

    @BeforeMethod
    public void reset() {
        Mockito.clearInvocations(rosterManager, stanzaRouter);
    }

    /**
     * When a server processes or generates an outbound presence stanza of type "subscribe", "subscribed", "unsubscribe", or "unsubscribed",
     * the server MUST stamp the outgoing presence stanza with the bare JID {@code <localpart@domainpart>} of the sending entity, not the full JID {@code <localpart@domainpart/resourcepart>}
     * <p>
     * If the JID is of the form {@code <contact@domainpart/resourcepart>} instead of {@code <contact@domainpart>},
     * the user's server SHOULD treat it as if the request had been directed to the contact's bare JID and modify the 'to' address accordingly.
     */
    @Test
    public void shouldStampFromAndToAttributeWithBareJid() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBE);
        presence.setFrom(Jid.of("user@server/full"));
        presence.setTo(Jid.of("contact@server/full"));
        subscriptionHandler.process(presence);

        Assert.assertEquals(presence.getFrom(), Jid.of("user@server"));
        Assert.assertEquals(presence.getTo(), Jid.of("contact@server"));
    }

    @Test
    public void testProcessingOfOutboundSubscriptionRequestIfContactNotExists() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBE);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(null);

        subscriptionHandler.process(presence);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        Mockito.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getTo());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.NONE);
        Assert.assertTrue(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());

        Mockito.verify(stanzaRouter).route(presence);
    }

    @Test
    public void testProcessingOfOutboundSubscriptionRequestIfContactExists() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBE);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(new Contact(presence.getTo(), "contact", false, false, SubscriptionState.Subscription.BOTH, Collections.emptyList()));

        subscriptionHandler.process(presence);

        Mockito.verify(rosterManager, Mockito.times(0)).setRosterItem(Mockito.eq("user"), Mockito.any());
        Mockito.verify(stanzaRouter).route(presence);
    }

    @Test
    public void testProcessingOfOutboundSubscriptionApprovalIfUserHasSubscriptionToContact() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBED);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getTo();
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
                return true;
            }
        });

        subscriptionHandler.process(presence);

        InOrder inOrder = Mockito.inOrder(stanzaRouter, rosterManager);
        inOrder.verify(stanzaRouter).route(presence);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        inOrder.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());
        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getTo());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.BOTH);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());
    }

    @Test
    public void testProcessingOfOutboundSubscriptionApprovalIfUserHasMutualSubscriptionToContact() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBED);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getTo();
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
                return true;
            }
        });

        subscriptionHandler.process(presence);

        Mockito.verify(rosterManager, Mockito.times(0)).setRosterItem(Mockito.eq("user"), Mockito.any());
        Mockito.verify(stanzaRouter, Mockito.times(0)).route(presence);
    }

    @Test
    public void testProcessingOfOutboundSubscriptionApprovalIfUserHasNoSubscriptionToContact() {
        Presence presence = new Presence(Presence.Type.SUBSCRIBED);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getTo();
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
                return false;
            }

            @Override
            public boolean isPendingIn() {
                return true;
            }
        });

        subscriptionHandler.process(presence);

        InOrder inOrder = Mockito.inOrder(stanzaRouter, rosterManager);
        inOrder.verify(stanzaRouter).route(presence);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        inOrder.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getTo());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.FROM);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());
    }

    @Test
    public void testProcessingOfOutboundSubscriptionCancellationIfContactIsNotInUsersRoster() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBED);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(null);

        subscriptionHandler.process(presence);

        Mockito.verify(rosterManager, Mockito.times(0)).setRosterItem(Mockito.any(), Mockito.any());
        Mockito.verify(stanzaRouter, Mockito.times(0)).route(presence);
    }

    @Test
    public void testProcessingOfOutboundSubscriptionCancellationIfContactIsInUsersRosterWithWrongSubscriptionState() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBED);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getTo();
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
        Mockito.verify(stanzaRouter, Mockito.times(0)).route(presence);
    }

    @Test
    public void testProcessingOfOutboundSubscriptionCancellationIfMutualSubscription() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBED);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getTo();
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

        InOrder inOrder = Mockito.inOrder(stanzaRouter, rosterManager);

        ArgumentCaptor<Presence> presenceArgumentCaptor = ArgumentCaptor.forClass(Presence.class);
        inOrder.verify(stanzaRouter, Mockito.times(3)).route(presenceArgumentCaptor.capture());
        Assert.assertEquals(presenceArgumentCaptor.getAllValues().get(0).getType(), Presence.Type.UNAVAILABLE);
        Assert.assertEquals(presenceArgumentCaptor.getAllValues().get(1).getType(), Presence.Type.UNAVAILABLE);
        Assert.assertEquals(presenceArgumentCaptor.getAllValues().get(2).getType(), Presence.Type.UNSUBSCRIBED);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        inOrder.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getTo());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.TO);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());
    }

    @Test
    public void testProcessingOfOutboundUnsubscriptionRequestIfMutualSubscription() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBE);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getTo();
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

        InOrder inOrder = Mockito.inOrder(stanzaRouter, rosterManager);
        inOrder.verify(stanzaRouter).route(presence);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        inOrder.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getTo());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.FROM);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());
    }

    @Test
    public void testProcessingOfOutboundUnsubscriptionRequestIfUserIsSubscribedToContact() {
        Presence presence = new Presence(Presence.Type.UNSUBSCRIBE);
        presence.setFrom(Jid.of("user@server"));
        presence.setTo(Jid.of("contact@server"));
        Mockito.when(rosterManager.getRosterItem(Mockito.eq("user"), Mockito.eq(presence.getTo()))).thenReturn(new RosterItem() {
            @Override
            public Jid getJid() {
                return presence.getTo();
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

        InOrder inOrder = Mockito.inOrder(stanzaRouter, rosterManager);
        inOrder.verify(stanzaRouter).route(presence);

        ArgumentCaptor<RosterItem> rosterItemCaptor = ArgumentCaptor.forClass(RosterItem.class);
        inOrder.verify(rosterManager).setRosterItem(Mockito.eq("user"), rosterItemCaptor.capture());

        Assert.assertEquals(rosterItemCaptor.getValue().getJid(), presence.getTo());
        Assert.assertEquals(rosterItemCaptor.getValue().getSubscription(), SubscriptionState.Subscription.NONE);
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingOut());
        Assert.assertFalse(rosterItemCaptor.getValue().isPendingIn());
    }
}
