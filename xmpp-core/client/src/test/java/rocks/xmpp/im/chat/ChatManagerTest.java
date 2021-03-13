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

package rocks.xmpp.im.chat;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.SameThreadExecutorService;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.net.client.ClientConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;

import java.util.function.Consumer;

public class ChatManagerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateChatSession() throws XmppException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class, Mockito.withSettings().useConstructor("domain", XmppSessionConfiguration.builder().build(), new ClientConnectionConfiguration[0]).defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.doReturn(new SameThreadExecutorService()).when(xmppSession).getStanzaListenerExecutor();
        ChatManager chatManager = new ChatManager(xmppSession);
        chatManager.initialize();

        Consumer<ChatSessionEvent> chatSessionEventlistener = Mockito.mock(Consumer.class);
        chatManager.addChatSessionListener(chatSessionEventlistener);

        Jid to = Jid.of("to/resource");
        chatManager.createChatSession(to, "123");
        ArgumentCaptor<ChatSessionEvent> chatSessionEventArgumentCaptor = ArgumentCaptor.forClass(ChatSessionEvent.class);
        Mockito.verify(chatSessionEventlistener).accept(chatSessionEventArgumentCaptor.capture());
        ChatSessionEvent chatSessionEvent = chatSessionEventArgumentCaptor.getValue();
        Assert.assertFalse(chatSessionEvent.isInbound());
        ChatSession chatSession = chatSessionEvent.getChatSession();
        Assert.assertNotNull(chatSession);
        // Should be bare JID because we did initiate the chat session and haven't received a response yet.
        Assert.assertEquals(chatSession.getChatPartner(), to.asBareJid());
        Assert.assertEquals(chatSession.getThread(), "123");

        Consumer<MessageEvent> messageListener = Mockito.mock(Consumer.class);
        chatSession.addInboundMessageListener(messageListener);
        Consumer<ChatSession.ChatPartnerEvent> chatPartnerListener = Mockito.mock(Consumer.class);
        chatSession.addChatPartnerListener(chatPartnerListener);

        // Now we receive a response.
        Message messageResponse = new Message(null, Message.Type.CHAT);
        messageResponse.setFrom(to);
        messageResponse.setThread("123");
        xmppSession.handleElement(messageResponse);

        // It should trigger the message listener on the chat session.
        ArgumentCaptor<MessageEvent> messageArgumentCaptor = ArgumentCaptor.forClass(MessageEvent.class);
        Mockito.verify(messageListener).accept(messageArgumentCaptor.capture());
        Message message = messageArgumentCaptor.getValue().getMessage();
        Assert.assertSame(message, messageResponse);

        ArgumentCaptor<ChatSession.ChatPartnerEvent> chatPartnerCaptor = ArgumentCaptor.forClass(ChatSession.ChatPartnerEvent.class);
        Mockito.verify(chatPartnerListener).accept(chatPartnerCaptor.capture());
        // The message should also lock the chat session to the full JID.
        Assert.assertEquals(chatPartnerCaptor.getValue().getNewChatPartner(), to);
        Assert.assertEquals(chatPartnerCaptor.getValue().getOldChatPartner(), to.asBareJid());
        Mockito.clearInvocations(chatPartnerListener);

        // Now we receive a message from the same contact, but different resource.
        Message newMessage = new Message(null, Message.Type.CHAT);
        newMessage.setFrom(to.withResource("another"));
        newMessage.setThread("456");
        xmppSession.handleElement(newMessage);
        Mockito.verify(chatSessionEventlistener, Mockito.times(2)).accept(Mockito.any());
        // First session should be unaffected.
        Mockito.verifyNoInteractions(chatPartnerListener);

        // Test removal of listener
        Mockito.clearInvocations(chatSessionEventlistener);
        chatManager.removeChatSessionListener(chatSessionEventlistener);
        Message message4 = new Message(null, Message.Type.CHAT);
        message4.setFrom(to.withResource("another"));
        message4.setThread("456");
        xmppSession.handleElement(message4);
        Mockito.verifyNoInteractions(chatSessionEventlistener);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInboundChatMessages() throws XmppException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class, Mockito.withSettings().useConstructor("domain", XmppSessionConfiguration.builder().build(), new ClientConnectionConfiguration[0]).defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.doReturn(new SameThreadExecutorService()).when(xmppSession).getStanzaListenerExecutor();
        ChatManager chatManager = new ChatManager(xmppSession);
        chatManager.initialize();

        Consumer<ChatSessionEvent> listener = Mockito.mock(Consumer.class);
        chatManager.addChatSessionListener(listener);
        Jid from = Jid.of("from/resource");
        Message message1 = new Message(null, Message.Type.CHAT);
        message1.setFrom(from);
        message1.setThread("123");
        xmppSession.handleElement(message1);

        ArgumentCaptor<ChatSessionEvent> chatSessionEventArgumentCaptor = ArgumentCaptor.forClass(ChatSessionEvent.class);
        Mockito.verify(listener).accept(chatSessionEventArgumentCaptor.capture());
        ChatSessionEvent chatSessionEvent = chatSessionEventArgumentCaptor.getValue();
        Assert.assertTrue(chatSessionEvent.isInbound());
        ChatSession chatSession = chatSessionEvent.getChatSession();
        Assert.assertNotNull(chatSession);
        // Should be full JID because we did not initiate the chat session
        Assert.assertEquals(chatSession.getChatPartner(), from);
        Assert.assertEquals(chatSession.getThread(), "123");

        ArgumentCaptor<MessageEvent> messageArgumentCaptor = ArgumentCaptor.forClass(MessageEvent.class);

        Consumer<MessageEvent> messageListener = Mockito.mock(Consumer.class);
        // Then add a listener to the session
        chatSession.addInboundMessageListener(messageListener);

        Message message2 = new Message(null, Message.Type.CHAT);
        message2.setFrom(from);
        message2.setThread("123");
        xmppSession.handleElement(message2);

        Mockito.verify(messageListener).accept(messageArgumentCaptor.capture());
        Message message = messageArgumentCaptor.getValue().getMessage();
        Assert.assertSame(message, message2);

        Consumer<ChatSession.ChatPartnerEvent> chatPartnerListener = Mockito.mock(Consumer.class);
        chatSession.addChatPartnerListener(chatPartnerListener);

        // Test removal of listener
        chatSession.removeInboundMessageListener(messageListener);
        Mockito.clearInvocations(messageListener);

        Message message3 = new Message(null, Message.Type.CHAT);
        message3.setFrom(from.withResource("another"));
        message3.setThread("123");
        xmppSession.handleElement(message3);

        Mockito.verifyNoInteractions(messageListener);

        ArgumentCaptor<ChatSession.ChatPartnerEvent> chatPartnerCaptor = ArgumentCaptor.forClass(ChatSession.ChatPartnerEvent.class);

        Mockito.verify(chatPartnerListener).accept(chatPartnerCaptor.capture());
        Jid newChatPartner = chatPartnerCaptor.getValue().getNewChatPartner();
        Assert.assertEquals(newChatPartner, from.withResource("another"));

        // Test removal of listener
        Mockito.clearInvocations(chatPartnerListener);
        chatSession.removeChatPartnerListener(chatPartnerListener);
        Message message4 = new Message(null, Message.Type.CHAT);
        message4.setFrom(from.withResource("anotherOne"));
        message4.setThread("123");
        xmppSession.handleElement(message4);
        Mockito.verifyNoInteractions(chatPartnerListener);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInboundPresence() throws XmppException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class, Mockito.withSettings().useConstructor("domain", XmppSessionConfiguration.builder().build(), new ClientConnectionConfiguration[0]).defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.doReturn(new SameThreadExecutorService()).when(xmppSession).getStanzaListenerExecutor();
        ChatManager chatManager = new ChatManager(xmppSession);
        chatManager.initialize();

        Consumer<ChatSessionEvent> listener = Mockito.mock(Consumer.class);
        chatManager.addChatSessionListener(listener);
        Jid from = Jid.of("from/resource");
        Message message1 = new Message(null, Message.Type.CHAT);
        message1.setFrom(from);
        message1.setThread("123");
        xmppSession.handleElement(message1);

        ArgumentCaptor<ChatSessionEvent> chatSessionEventArgumentCaptor = ArgumentCaptor.forClass(ChatSessionEvent.class);
        Mockito.verify(listener).accept(chatSessionEventArgumentCaptor.capture());
        ChatSessionEvent chatSessionEvent = chatSessionEventArgumentCaptor.getValue();
        Assert.assertTrue(chatSessionEvent.isInbound());
        ChatSession chatSession = chatSessionEvent.getChatSession();
        Assert.assertNotNull(chatSession);

        Consumer<ChatSession.ChatPartnerEvent> chatPartnerListener = Mockito.mock(Consumer.class);
        chatSession.addChatPartnerListener(chatPartnerListener);

        Presence presence = new Presence();
        presence.setFrom(from.withResource("another"));
        xmppSession.handleElement(presence);

        ArgumentCaptor<ChatSession.ChatPartnerEvent> chatPartnerCaptor = ArgumentCaptor.forClass(ChatSession.ChatPartnerEvent.class);

        Mockito.verify(chatPartnerListener).accept(chatPartnerCaptor.capture());
        Jid newChatPartner = chatPartnerCaptor.getValue().getNewChatPartner();
        Assert.assertEquals(newChatPartner, from.withResource("another"));

    }
}
