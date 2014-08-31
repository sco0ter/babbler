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

import org.xmpp.im.ChatSession;

import java.util.EventObject;

/**
 * @author Christian Schudt
 */
final class ChatStateEvent extends EventObject {

    private final ChatSession chatSession;

    private final ChatState chatState;

    private final boolean local;

    /**
     * Constructs a prototypical Event.
     *
     * @param source      The object on which the Event initially occurred.
     * @param chatSession The chat session.
     * @param chatState   The chat state.
     * @param local       True, if the chat state has been changed locally, i.e. by me. False if the chat state was updated by the chat partner.
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
