/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Christian Schudt
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

package rocks.xmpp.extensions.chatstates;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.Chat;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.chatstates.model.ChatState;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Christian Schudt
 */
public class ChatStateManagerTest extends ExtensionTest {

    @Test
    public void testChatStateManager() {

        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        final Collection<ChatState> chatStatesReceived = new ArrayList<>();
        xmppSession2.addMessageListener(new MessageListener() {
            @Override
            public void handleMessage(MessageEvent e) {
                ChatState chatState = e.getMessage().getExtension(ChatState.class);
                if (e.isIncoming() && chatState != null) {
                    chatStatesReceived.add(chatState);
                }
            }
        });

        ChatStateManager chatStateManager = xmppSession1.getExtensionManager(ChatStateManager.class);
        chatStateManager.setEnabled(true);
        Chat chat = xmppSession1.getChatManager().createChatSession(JULIET);
        // At this point it is unknown if the chat partner supports chat states. Therefore send it.
        Assert.assertTrue(chatStateManager.setChatState(ChatState.COMPOSING, chat));
        Assert.assertTrue(chatStatesReceived.contains(ChatState.COMPOSING));
        // Repetition should be avoided, therefore don't send it.
        Assert.assertFalse(chatStateManager.setChatState(ChatState.COMPOSING, chat));
        // A new chat state should be sent again.
        Assert.assertTrue(chatStateManager.setChatState(ChatState.PAUSED, chat));
        Assert.assertFalse(chatStatesReceived.contains(ChatState.ACTIVE));
        chat.sendMessage("chat message");
        // A <active/> extension should have been added.
        Assert.assertTrue(chatStatesReceived.contains(ChatState.ACTIVE));
    }

    @Test
    public void testServiceDiscoveryEntry() {

        XmppSession xmppSession1 = new TestXmppSession();
        ChatStateManager chatStateManager = xmppSession1.getExtensionManager(ChatStateManager.class);
        // By default, Chat States are disabled.
        Assert.assertFalse(chatStateManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("http://jabber.org/protocol/chatstates");
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        chatStateManager.setEnabled(true);
        Assert.assertTrue(chatStateManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}