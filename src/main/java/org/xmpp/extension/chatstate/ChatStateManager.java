package org.xmpp.extension.chatstate;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.im.ChatSession;
import org.xmpp.im.ChatSessionEvent;
import org.xmpp.im.ChatSessionListener;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Christian Schudt
 */
public final class ChatStateManager extends ExtensionManager {

    private Set<ChatStateListener> chatStateListeners = new CopyOnWriteArraySet<>();

    private Map<ChatSession, ChatState> chatSessionMap = new ConcurrentHashMap<>();

    private Map<Jid, Boolean> contactSupportsChatStateNotifications = new HashMap<>();

    public ChatStateManager(final Connection connection) {
        super(connection);
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addFeature(new Feature("http://jabber.org/protocol/chatstates"));
        connection.getChatManager().addChatSessionListener(new ChatSessionListener() {
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

    public void addChatStateListener(ChatStateListener chatStateListener) {
        chatStateListeners.add(chatStateListener);
    }

    public void removeChatStateListener(ChatStateListener chatStateListener) {
        chatStateListeners.remove(chatStateListener);
    }

    public void sendState(ChatSession chatSession, ChatState chatState) {
        if (chatSession == null) {
            throw new IllegalArgumentException("chatSession must not be null");
        }
        if (chatState == null) {
            throw new IllegalArgumentException("chatState must not be null.");
        }
        ChatState oldState = chatSessionMap.get(chatSession);
        if (oldState != chatState) {
            Object state;
            switch (chatState) {
                case COMPOSING:
                    state = new Composing();
                    break;
                case GONE:
                    state = new Gone();
                    break;
                case INACTIVE:
                    state = new Inactive();
                    break;
                case PAUSED:
                    state = new Paused();
                    break;
                default:
                    state = new Active();
                    break;
            }
            Message message = new Message();
            message.getExtensions().add(state);
            chatSession.send(message);
        }
        chatSessionMap.put(chatSession, chatState);
    }
}
