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

package org.xmpp.extension.chatstates;

import org.xmpp.Jid;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.im.ChatSession;
import org.xmpp.im.ChatSessionEvent;
import org.xmpp.im.ChatSessionListener;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;
import org.xmpp.stanza.client.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This manager merely allows you to enable or disable chat states for service discovery purposes.
 * <h2>Sending chat states</h2>
 * <pre><code>
 * Message message = new Message(Jid.valueOf("juliet@example.net"), Message.Type.CHAT);
 * message.getExtensions().add(new Composing());
 * xmppSession.send(message);
 * </code></pre>
 * <h2>Reacting to chat states</h2>
 * <pre><code>
 * if (message.getExtension(Paused.class) != null) {
 *     // "paused" chat state has been sent.
 * }
 * </code></pre>
 *
 * @author Christian Schudt
 */
public final class ChatStateManager extends ExtensionManager {

    private final Set<ChatStateListener> chatStateListeners = new CopyOnWriteArraySet<>();

    private final Map<ChatSession, ChatState> chatSessionMap = new ConcurrentHashMap<>();

    private final Map<Jid, Boolean> contactSupportsChatStateNotifications = new HashMap<>();

    private ChatStateManager(final XmppSession xmppSession) {
        super(xmppSession, "http://jabber.org/protocol/chatstates");
        xmppSession.getChatManager().addChatSessionListener(new ChatSessionListener() {
            @Override
            public void chatSessionCreated(ChatSessionEvent chatSessionEvent) {
                final ChatSession chatSession = chatSessionEvent.getChatSession();
                chatSession.addMessageListener(new MessageListener() {
                    @Override
                    public void handle(MessageEvent e) {
                        Message message = e.getMessage();
                        if (!e.isIncoming()) {
                            // In the absence of explicit discovery or negotiation, the User MAY implicitly request and discover the use of chat state notifications in a one-to-one chat session by adhering to the following business rules:
                            // 1. If the User desires chat state notifications, the message(s) that it sends to the Contact before receiving a reply MUST contain a chat state notification extension, which SHOULD be <active/>.
                            if (!contactSupportsChatStateNotifications.containsKey(message.getTo()) && contactSupportsChatStateNotifications.get(message.getTo())
                                    && message.getExtension(Active.class) == null
                                    && message.getExtension(Composing.class) == null
                                    && message.getExtension(Gone.class) == null
                                    && message.getExtension(Inactive.class) == null
                                    && message.getExtension(Paused.class) == null) {

                                message.getExtensions().add(new Active());
                                notifyChatStateListeners(chatSession, ChatState.ACTIVE, !e.isIncoming());
                            }
                        }

                        if (message.getExtension(Active.class) != null) {
                            notifyChatStateListeners(chatSession, ChatState.ACTIVE, !e.isIncoming());
                        } else if (message.getExtension(Composing.class) != null) {
                            notifyChatStateListeners(chatSession, ChatState.COMPOSING, !e.isIncoming());
                        } else if (message.getExtension(Gone.class) != null) {
                            notifyChatStateListeners(chatSession, ChatState.GONE, !e.isIncoming());
                            //connection.getChatManager().destroyChatSession(chatSession);
                        } else if (message.getExtension(Inactive.class) != null) {
                            notifyChatStateListeners(chatSession, ChatState.INACTIVE, !e.isIncoming());
                        } else if (message.getExtension(Paused.class) != null) {
                            notifyChatStateListeners(chatSession, ChatState.PAUSED, !e.isIncoming());
                        } else if (!e.isIncoming()) {
                            // For outgoing messages, automatically add an <active/> state, if there's no other state.
                            message.getExtensions().add(new Active());
                            notifyChatStateListeners(chatSession, ChatState.ACTIVE, !e.isIncoming());
                        }

                    }
                });
            }
        });
    }

    private void notifyChatStateListeners(ChatSession chatSession, ChatState chatState, boolean local) {
        for (ChatStateListener chatStateListener : chatStateListeners) {
            chatStateListener.chatStateUpdated(new ChatStateEvent(this, chatSession, chatState, local));
        }
    }

    //    public void addChatStateListener(ChatStateListener chatStateListener) {
    //        chatStateListeners.add(chatStateListener);
    //    }
    //
    //    public void removeChatStateListener(ChatStateListener chatStateListener) {
    //        chatStateListeners.remove(chatStateListener);
    //    }
    //
    //    public void sendState(ChatSession chatSession, ChatState chatState) {
    //        if (chatSession == null) {
    //            throw new IllegalArgumentException("chatSession must not be null");
    //        }
    //        if (chatState == null) {
    //            throw new IllegalArgumentException("chatState must not be null.");
    //        }
    //        ChatState oldState = chatSessionMap.get(chatSession);
    //        if (oldState != chatState) {
    //            Object state;
    //            switch (chatState) {
    //                case COMPOSING:
    //                    state = new Composing();
    //                    break;
    //                case GONE:
    //                    state = new Gone();
    //                    break;
    //                case INACTIVE:
    //                    state = new Inactive();
    //                    break;
    //                case PAUSED:
    //                    state = new Paused();
    //                    break;
    //                default:
    //                    state = new Active();
    //                    break;
    //            }
    //            Message message = new Message(chatSession.getChatPartner());
    //            message.getExtensions().add(state);
    //            chatSession.send(message);
    //        }
    //        chatSessionMap.put(chatSession, chatState);
    //    }
}
