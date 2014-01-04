package org.xmpp.extension.chatstate;

import org.xmpp.im.ChatSession;

import java.util.EventObject;

/**
 * @author Christian Schudt
 */
public final class ChatStateEvent extends EventObject {

    private final ChatSession chatSession;

    private final ChatState chatState;

    private final boolean local;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ChatStateEvent(Object source, ChatSession chatSession, ChatState chatState, boolean local) {
        super(source);
        this.chatSession = chatSession;
        this.chatState = chatState;
        this.local = local;
    }

    public ChatState getChatState() {
        return chatState;
    }

    public ChatSession getChatSession() {
        return chatSession;
    }

    public boolean isLocal() {
        return local;
    }
}
