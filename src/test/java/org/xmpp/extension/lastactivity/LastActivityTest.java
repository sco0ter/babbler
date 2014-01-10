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

package org.xmpp.extension.lastactivity;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.MockServer;
import org.xmpp.TestConnection;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.stanza.*;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public class LastActivityTest extends BaseTest {

    @Test
    public void unmarshalLastActivityResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq from='juliet@capulet.com' \n" +
                "    id='last1'\n" +
                "    to='romeo@montague.net/orchard'\n" +
                "    type='result'>\n" +
                "  <query xmlns='jabber:iq:last' seconds='903'/>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        LastActivity lastActivity = iq.getExtension(LastActivity.class);
        Assert.assertNotNull(lastActivity);
        Assert.assertEquals(lastActivity.getSeconds(), 903);
    }

    @Test
    public void testLastActivityManagerIsCleared() throws IOException {
        TestConnection connection1 = new TestConnection();
        LastActivityManager lastActivityManager = connection1.getExtensionManager(LastActivityManager.class);
        lastActivityManager.setLastActivityStrategy(new LastActivityStrategy() {
            @Override
            public Date getLastActivity() {
                return new Date();
            }
        });
        connection1.close();
        Assert.assertNull(lastActivityManager.getLastActivityStrategy());
    }

    @Test
    public void testGetLastActivity() {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        new TestConnection(JULIET, mockServer);
        LastActivityManager lastActivityManager = connection1.getExtensionManager(LastActivityManager.class);
        LastActivity lastActivity = lastActivityManager.getLastActivity(JULIET);
        Assert.assertNotNull(lastActivity);
    }

    @Test
    public void testGetLastActivityIfDisabled() {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        TestConnection connection2 = new TestConnection(JULIET, mockServer);
        connection2.getExtensionManager(LastActivityManager.class).setEnabled(false);
        LastActivityManager lastActivityManager = connection1.getExtensionManager(LastActivityManager.class);
        LastActivity lastActivity = lastActivityManager.getLastActivity(JULIET);
        Assert.assertNull(lastActivity);
    }

    @Test
    public void testLastActivityInAwayPresence() {
        TestConnection connection1 = new TestConnection(ROMEO, new MockServer());
        connection1.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                Assert.assertTrue(e.getPresence().getExtension(LastActivity.class) != null);
            }
        });
        connection1.send(new Message(JULIET));
        connection1.send(new Presence(Presence.Show.AWAY));
    }

    @Test
    public void testLastActivityInXAPresence() {
        TestConnection connection1 = new TestConnection(ROMEO, new MockServer());
        connection1.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                Assert.assertTrue(e.getPresence().getExtension(LastActivity.class) != null);
            }
        });
        connection1.send(new Message(JULIET));
        connection1.send(new Presence(Presence.Show.AWAY));
    }

    @Test
    public void testLastActivityInChatPresence() {
        TestConnection connection1 = new TestConnection(ROMEO, new MockServer());
        connection1.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                Assert.assertFalse(e.getPresence().getExtension(LastActivity.class) != null);
            }
        });
        connection1.send(new Message(JULIET));
        connection1.send(new Presence(Presence.Show.CHAT));
    }

    @Test
    public void testLastActivityInDndPresence() {
        TestConnection connection1 = new TestConnection(ROMEO, new MockServer());
        connection1.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                Assert.assertFalse(e.getPresence().getExtension(LastActivity.class) != null);
            }
        });
        connection1.send(new Message(JULIET));
        connection1.send(new Presence(Presence.Show.DND));
    }

    @Test
    public void testLastActivityInInitialPresence() {
        TestConnection connection1 = new TestConnection(ROMEO, new MockServer());
        connection1.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                Assert.assertFalse(e.getPresence().getExtension(LastActivity.class) != null);
            }
        });
        connection1.send(new Presence(Presence.Show.AWAY));
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestConnection connection1 = new TestConnection();
        LastActivityManager lastActivityManager = connection1.getExtensionManager(LastActivityManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(lastActivityManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("jabber:iq:last");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
        lastActivityManager.setEnabled(false);
        Assert.assertFalse(lastActivityManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
