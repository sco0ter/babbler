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

package org.xmpp.im;

import org.xmpp.XmppSession;
import org.xmpp.ConnectionEvent;
import org.xmpp.ConnectionListener;
import org.xmpp.Jid;
import org.xmpp.XmppSession;
import org.xmpp.stanza.*;
import org.xmpp.stanza.client.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages one-to-one chat sessions, which are described in <a href="http://xmpp.org/rfcs/rfc6121.html#message-chat">5.1.  One-to-One Chat Sessions</a> and <a href="http://xmpp.org/extensions/xep-0201.html">XEP-0201: Best Practices for Message Threads</a>.
 * <h3>Creating a new chat session</h3>
 * <pre>
 * <code>ChatSession chatSession = connection.getChatManager().createChatSession(chatPartner);</code>
 * </pre>
 * <h3>Listen for new chat sessions</h3>
 * <p>When a contact initiates a new chat session with you, you can listen for it with the {@link ChatSessionListener}.
 * The listener will be called either if you created the session programmatically as shown above, or if it is created by a contact, i.e. because he or she sent you a chat message.
 * </p>
 * <p>You should add a {@link MessageListener} to the chat session in order to listen for messages.</p>
 * <pre><code>
 * connection.getChatManager().addChatSessionListener(new ChatSessionListener() {
 *     {@literal @}Override
 *     public void chatSessionCreated(ChatSessionEvent chatSessionEvent) {
 *         ChatSession chatSession = chatSessionEvent.getChatSession();
 *         chatSession.addMessageListener(new MessageListener() {
 *             {@literal @}Override
 *             public void handle(MessageEvent e) {
 *                 Message message = e.getMessage();
 *             }
 *         });
 *     }
 * });
 * </code>
 * </pre>
 */
public final class ChatManager {

    private static final Logger logger = Logger.getLogger(ChatManager.class.getName());

    private final XmppSession xmppSession;

    /**
     * <blockquote>
     * <p>For {@code <message/>} stanzas of type "chat" exchanged between two entities, the value of the {@code <thread/>} element shall be considered equivalent to a unique identifier for the chat session or conversation thread.</p>
     * </blockquote>
     */
    private final Map<Jid, Map<String, ChatSession>> chatSessions = new ConcurrentHashMap<>();

    private final Set<ChatSessionListener> chatSessionListeners = new CopyOnWriteArraySet<>();

    /**
     * Creates the chat manager.
     *
     * @param xmppSession The connection.
     */
    public ChatManager(final XmppSession xmppSession) {
        this.xmppSession = xmppSession;

        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                Message message = e.getMessage();
                if (message.getType() == Message.Type.CHAT && message.getBody() != null && !message.getBody().isEmpty()) {
                    Jid chatPartner = e.isIncoming() ? message.getFrom() : message.getTo();
                    // If an entity receives such a message with a new or unknown ThreadID, it SHOULD treat the message as part of a new chat session.
                    // If an entity receives a message of type "chat" without a thread ID, then it SHOULD create a new session with a new thread ID (and include that thread ID in all the messages it sends within the new session).
                    String threadId = message.getThread() != null ? message.getThread() : UUID.randomUUID().toString();
                    if (chatPartner != null) {
                        Jid contact = chatPartner.asBareJid();
                        synchronized (chatSessions) {
                            // If there are no chat sessions with that contact yet, put the contact into the map.
                            if (!chatSessions.containsKey(contact)) {
                                chatSessions.put(contact, new HashMap<String, ChatSession>());
                            }
                            Map<String, ChatSession> chatSessionMap = chatSessions.get(contact);
                            if (!chatSessionMap.containsKey(threadId)) {
                                ChatSession chatSession = new ChatSession(chatPartner, threadId, xmppSession);
                                chatSessionMap.put(threadId, chatSession);
                                notifyChatSessionCreated(chatSession, e.isIncoming());
                            }
                            ChatSession chatSession = chatSessionMap.get(threadId);
                            if (e.isIncoming()) {
                                // Until and unless the user's client receives a reply from the contact, it SHOULD send any further messages to the contact's bare JID. The contact's client SHOULD address its replies to the user's full JID <user@domainpart/resourcepart> as provided in the 'from' address of the initial message.
                                chatSession.chatPartner = message.getFrom();
                            }
                            chatSession.notifyMessageListeners(message, e.isIncoming());
                        }
                    }
                }
            }
        });
        xmppSession.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (e.isIncoming()) {
                    // A client SHOULD "unlock" after having received a <message/> or <presence/> stanza from any other resource controlled by the peer (or a presence stanza from the locked resource); as a result, it SHOULD address its next message(s) in the chat session to the bare JID of the peer (thus "unlocking" the previous "lock") until it receives a message from one of the peer's full JIDs.
                    AbstractPresence presence = e.getPresence();
                    synchronized (chatSessions) {
                        Jid contact = presence.getFrom().asBareJid();
                        if (chatSessions.containsKey(contact)) {
                            for (ChatSession chatSession : chatSessions.get(contact).values()) {
                                chatSession.chatPartner = contact;
                            }
                        }
                    }
                }
            }
        });

        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    chatSessionListeners.clear();
                    chatSessions.clear();
                }
            }
        });
    }

    /**
     * Adds a chat session listener.
     *
     * @param chatSessionListener The listener.
     * @see #removeChatSessionListener(ChatSessionListener)
     */
    public void addChatSessionListener(ChatSessionListener chatSessionListener) {
        chatSessionListeners.add(chatSessionListener);
    }

    /**
     * Removes a previously added chat session listener.
     *
     * @param chatSessionListener The listener.
     * @see #addChatSessionListener(ChatSessionListener)
     */
    public void removeChatSessionListener(ChatSessionListener chatSessionListener) {
        chatSessionListeners.remove(chatSessionListener);
    }

    private void notifyChatSessionCreated(ChatSession chatSession, boolean createdByIncomingMessage) {
        for (ChatSessionListener chatSessionListener : chatSessionListeners) {
            try {
                chatSessionListener.chatSessionCreated(new ChatSessionEvent(this, chatSession, createdByIncomingMessage));
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a new chat session and notifies any {@linkplain ChatSessionListener chat session listeners} about it.
     *
     * @param chatPartner The chat partner.
     * @return The chat session.
     */
    public ChatSession createChatSession(Jid chatPartner) {
        if (chatPartner == null) {
            throw new IllegalArgumentException("chatPartner must not be null.");
        }
        ChatSession chatSession = new ChatSession(chatPartner, UUID.randomUUID().toString(), xmppSession);
        notifyChatSessionCreated(chatSession, false);
        return chatSession;
    }

    /**
     * Destroys the chat session.
     *
     * @param chatSession The chat session.
     */
    public void destroyChatSession(ChatSession chatSession) {
        if (chatSession == null) {
            throw new IllegalArgumentException("chatSession must not be null.");
        }
        Jid user = chatSession.getChatPartner().asBareJid();
        synchronized (chatSessions) {
            if (chatSessions.containsKey(user)) {
                Map<String, ChatSession> chatSessionMap = chatSessions.get(user);
                chatSessionMap.remove(chatSession.getThread());
                if (chatSessionMap.isEmpty()) {
                    chatSessions.remove(user);
                }
            }
        }
    }
}
