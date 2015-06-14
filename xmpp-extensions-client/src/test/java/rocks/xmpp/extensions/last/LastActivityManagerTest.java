/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.last;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppXmpp;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.avatar.AvatarManager;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.last.model.LastActivity;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * @author Christian Schudt
 */
public class LastActivityManagerTest extends ExtensionTest {

    @Test
    public void testGetLastActivity() throws XmppException {
        MockServer mockServer = new MockServer();
        TestXmppXmpp xmppSession1 = new TestXmppXmpp(ROMEO, mockServer);
        new TestXmppXmpp(JULIET, mockServer);
        LastActivityManager lastActivityManager = xmppSession1.getManager(LastActivityManager.class);
        LastActivity lastActivity = lastActivityManager.getLastActivity(JULIET);
        Assert.assertNotNull(lastActivity);
    }

    @Test
    public void testGetLastActivityIfDisabled() throws XmppException {
        MockServer mockServer = new MockServer();
        TestXmppXmpp xmppSession1 = new TestXmppXmpp(ROMEO, mockServer);
        TestXmppXmpp xmppSession2 = new TestXmppXmpp(JULIET, mockServer);
        xmppSession2.getManager(LastActivityManager.class).setEnabled(false);
        LastActivityManager lastActivityManager = xmppSession1.getManager(LastActivityManager.class);
        try {
            lastActivityManager.getLastActivity(JULIET);
        } catch (StanzaException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testLastActivityInAwayPresence() {
        final TestXmppXmpp xmppSession1 = new TestXmppXmpp(ROMEO, new MockServer());
        xmppSession1.getManager(AvatarManager.class).setEnabled(false);
        xmppSession1.addOutboundPresenceListener(new Consumer<PresenceEvent>() {
            @Override
            public void accept(PresenceEvent e) {
                xmppSession1.removeInboundPresenceListener(this);
                Assert.assertTrue(e.getPresence().getExtension(LastActivity.class) != null);
            }
        });
        xmppSession1.send(new Message(JULIET));
        xmppSession1.send(new Presence(Presence.Show.AWAY));
    }

    @Test
    public void testLastActivityInXAPresence() {
        TestXmppXmpp xmppSession1 = new TestXmppXmpp(ROMEO, new MockServer());
        xmppSession1.getManager(AvatarManager.class).setEnabled(false);
        xmppSession1.addOutboundPresenceListener(e -> Assert.assertTrue(e.getPresence().getExtension(LastActivity.class) != null));
        xmppSession1.send(new Message(JULIET));
        xmppSession1.send(new Presence(Presence.Show.AWAY));
    }

    @Test
    public void testLastActivityInChatPresence() {
        TestXmppXmpp xmppSession1 = new TestXmppXmpp(ROMEO, new MockServer());
        xmppSession1.getManager(AvatarManager.class).setEnabled(false);
        xmppSession1.addOutboundPresenceListener(e -> Assert.assertFalse(e.getPresence().getExtension(LastActivity.class) != null));
        xmppSession1.send(new Presence(Presence.Show.CHAT));
    }

    @Test
    public void testLastActivityInDndPresence() {
        TestXmppXmpp xmppSession1 = new TestXmppXmpp(ROMEO, new MockServer());
        xmppSession1.getManager(AvatarManager.class).setEnabled(false);
        xmppSession1.addOutboundPresenceListener(e -> Assert.assertFalse(e.getPresence().getExtension(LastActivity.class) != null));
        xmppSession1.send(new Presence(Presence.Show.DND));
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestXmppXmpp xmppSession1 = new TestXmppXmpp();
        LastActivityManager lastActivityManager = xmppSession1.getManager(LastActivityManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(lastActivityManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession1.getManager(ServiceDiscoveryManager.class);
        String feature = "jabber:iq:last";
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
        lastActivityManager.setEnabled(false);
        Assert.assertFalse(lastActivityManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
    }

    @Test
    public void testSeconds() {
        Assert.assertEquals(LastActivityManager.getSecondsSince(Instant.now().minusSeconds(30)), 30);
    }
}
