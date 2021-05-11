/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.extensions.reach;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.pubsub.model.event.Event;
import rocks.xmpp.extensions.reach.model.Address;
import rocks.xmpp.extensions.reach.model.Reachability;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * Tests for {@link ReachabilityManager}.
 */
public class ReachabilityManagerTest extends BaseTest {

    /**
     * @see <a href="https://xmpp.org/extensions/xep-0152.html#transport-presence">4.1 Presence Transport</a>
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testPresenceTransport() {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        ReachabilityManager reachabilityManager = Mockito.spy(new ReachabilityManager(xmppSession));

        //  User's Client Includes Reachability Addresses in Presence
        Presence presence = new Presence();
        presence.setFrom(Jid.of("reachability@test/office"));
        Reachability reachability =
                new Reachability(Collections.singleton(new Address(URI.create("tel:+1-303-555-1212"))));
        presence.addExtension(reachability);
        PresenceEvent e = new PresenceEvent(xmppSession, presence, true);
        Consumer<ReachabilityEvent> reachabilityEventConsumer = Mockito.mock(Consumer.class);
        reachabilityManager.addReachabilityListener(reachabilityEventConsumer);

        reachabilityManager.handleInboundPresence(e);

        ArgumentCaptor<ReachabilityEvent> reachabilityEventCaptor = ArgumentCaptor.forClass(ReachabilityEvent.class);

        Mockito.verify(reachabilityEventConsumer).accept(reachabilityEventCaptor.capture());
        Assert.assertEquals(reachabilityEventCaptor.getValue().getContact(),
                Jid.of("reachability@test/office").asBareJid());
        Assert.assertEquals(reachabilityEventCaptor.getValue().getReachabilityAddresses(), reachability.getAddresses());

        // User's Client Sends Updated Presence Without Reachability Addresses
        Mockito.clearInvocations(reachabilityEventConsumer);
        Presence presenceWithoutAddresses = new Presence();
        presenceWithoutAddresses.setFrom(Jid.of("reachability@test/office"));
        PresenceEvent e2 = new PresenceEvent(xmppSession, presenceWithoutAddresses, true);
        reachabilityManager.handleInboundPresence(e2);

        reachabilityEventCaptor = ArgumentCaptor.forClass(ReachabilityEvent.class);
        Mockito.verify(reachabilityEventConsumer).accept(reachabilityEventCaptor.capture());

        Assert.assertEquals(reachabilityEventCaptor.getValue().getContact(),
                Jid.of("reachability@test/office").asBareJid());
        Assert.assertTrue(reachabilityEventCaptor.getValue().getReachabilityAddresses().isEmpty());

        // Subsequent presences without addresses should not trigger
        Mockito.clearInvocations(reachabilityEventConsumer);
        reachabilityManager.handleInboundPresence(e2);
        Mockito.verify(reachabilityEventConsumer, Mockito.times(0)).accept(Mockito.any());
    }

    /**
     * @see <a href="https://xmpp.org/extensions/xep-0152.html#transport-pep">4.2 Personal Eventing Protocol</a>
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testPepTransport() {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        ReachabilityManager reachabilityManager = Mockito.spy(new ReachabilityManager(xmppSession));

        Message message = new Message();
        message.setFrom(Jid.of("romeo@example.com"));
        Reachability reachability =
                new Reachability(Collections.singleton(new Address(URI.create("tel:+1-303-555-1212"))));
        message.addExtension(Event.withItem(Reachability.NAMESPACE, reachability, "a1s2d3f4g5h6bjeh936", null));
        MessageEvent e = new MessageEvent(xmppSession, message, true);
        Consumer<ReachabilityEvent> reachabilityEventConsumer = Mockito.mock(Consumer.class);
        reachabilityManager.addReachabilityListener(reachabilityEventConsumer);

        reachabilityManager.handleInboundMessage(e);

        ArgumentCaptor<ReachabilityEvent> reachabilityEventCaptor = ArgumentCaptor.forClass(ReachabilityEvent.class);

        Mockito.verify(reachabilityEventConsumer).accept(reachabilityEventCaptor.capture());
        Assert.assertEquals(reachabilityEventCaptor.getValue().getContact(),
                Jid.of("romeo@example.com").asBareJid());
        Assert.assertEquals(reachabilityEventCaptor.getValue().getReachabilityAddresses(), reachability.getAddresses());

        // Test listener removal
        Mockito.clearInvocations(reachabilityEventConsumer);
        reachabilityManager.removeReachabilityListener(reachabilityEventConsumer);
        reachabilityManager.handleInboundMessage(e);
        Mockito.verify(reachabilityEventConsumer, Mockito.times(0)).accept(Mockito.any());
    }

    @Test
    public void testIQTransport() {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        ReachabilityManager reachabilityManager = Mockito.spy(new ReachabilityManager(xmppSession));

        reachabilityManager.addReachabilityAddress(new Address(URI.create("tel:+1-303-555-1212")));

        IQ iq = new IQ(IQ.Type.GET, new Reachability());

        IQ result = reachabilityManager.handleRequest(iq);
        Reachability reachability = result.getExtension(Reachability.class);
        Assert.assertNotNull(reachability);
        Assert.assertEquals(reachability.getAddresses().size(), 1);
        Assert.assertEquals(reachability.getAddresses().get(0), new Address(URI.create("tel:+1-303-555-1212")));
    }

    @Test
    public void testRequestReachabilityAddresses() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        ReachabilityManager reachabilityManager = Mockito.spy(new ReachabilityManager(xmppSession));
        Mockito.doReturn(new AsyncResult<>(CompletableFuture.completedFuture(new IQ(IQ.Type.RESULT,
                new Reachability(Collections.singleton(new Address(URI.create("tel:+1-303-555-1212"))))))))
                .when(xmppSession).query(Mockito.any(IQ.class));
        List<Address> addresses = reachabilityManager.requestReachabilityAddresses(JULIET).get();
        ArgumentCaptor<IQ> argumentCaptor = ArgumentCaptor.forClass(IQ.class);
        Mockito.verify(xmppSession).query(argumentCaptor.capture());

        IQ request = argumentCaptor.getValue();
        Assert.assertEquals(request.getType(), IQ.Type.GET);
        Assert.assertTrue(request.hasExtension(Reachability.class));
        Assert.assertEquals(addresses.size(), 1);
        Assert.assertEquals(addresses.get(0), new Address(URI.create("tel:+1-303-555-1212")));
    }

    @Test
    public void testServiceDiscoveryEntry() throws ExecutionException, InterruptedException {

        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());
        ReachabilityManager reachabilityManager = xmppSession.getManager(ReachabilityManager.class);
        Assert.assertFalse(reachabilityManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        Assert.assertFalse(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures().contains(
                Reachability.NAMESPACE));
        reachabilityManager.addReachabilityAddress(new Address(URI.create("")));
        Assert.assertEquals(reachabilityManager.getReachabilityAddresses().size(), 1);
        Assert.assertTrue(reachabilityManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures()
                .contains(Reachability.NAMESPACE));
        Assert.assertTrue(reachabilityManager.removeReachabilityAddress(new Address(URI.create(""))));
        Assert.assertFalse(serviceDiscoveryManager.discoverInformation(JULIET).get().getFeatures()
                .contains(Reachability.NAMESPACE));

    }
}
