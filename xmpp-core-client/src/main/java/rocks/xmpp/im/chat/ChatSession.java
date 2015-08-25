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

package rocks.xmpp.im.chat;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.Message;

import java.util.EventObject;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Implements a one-to-one chat session. They are described in <a href="http://xmpp.org/rfcs/rfc6121.html#message-chat">5.1.  One-to-One Chat Sessions</a> and <a href="http://xmpp.org/extensions/xep-0201.html">XEP-0201: Best Practices for Message Threads</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-chat">5.1.  One-to-One Chat Sessions</a></cite></p>
 * <p>In practice, instant messaging activity between human users tends to occur in the form of a conversational burst that we call a "chat session": the exchange of multiple messages between two parties in relatively rapid succession within a relatively brief period of time.</p>
 * </blockquote>
 * <p>
 * In order to create a new chat session, use the {@linkplain ChatManager#createChatSession(Jid) chat manager}.
 * </p>
 */
public final class ChatSession extends Chat implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ChatSession.class.getName());

    private final Set<Consumer<ChatPartnerEvent>> chatPartnerListeners = new CopyOnWriteArraySet<>();

    private final String thread;

    private final XmppSession xmppSession;

    private volatile Jid chatPartner;

    private final ChatManager chatManager;

    ChatSession(Jid chatPartner, String thread, XmppSession xmppSession, ChatManager chatManager) {
        // The user's client SHOULD address the initial message in a chat session to the bare JID <contact@domainpart> of the contact (rather than attempting to guess an appropriate full JID <contact@domainpart/resourcepart> based on the <show/>, <status/>, or <priority/> value of any presence notifications it might have received from the contact).
        this.chatPartner = Objects.requireNonNull(chatPartner, "chatPartner must not be null.").asBareJid();
        this.thread = thread;
        this.xmppSession = xmppSession;
        this.chatManager = chatManager;
    }

    /**
     * Adds a chat partner listener.
     *
     * @param chatPartnerListener The listener to add. Must not be {@code null}.
     * @see #removeChatPartnerListener(Consumer)
     * @see ChatPartnerEvent
     * @since 0.5.0
     */
    public final void addChatPartnerListener(final Consumer<ChatPartnerEvent> chatPartnerListener) {
        chatPartnerListeners.add(requireNonNull(chatPartnerListener, "chatPartnerListener must not be null"));
    }

    /**
     * Removes a chat partner listener.
     *
     * @param chatPartnerListener The listener to remove. Must not be {@code null}.
     * @see #addChatPartnerListener(Consumer)
     * @see ChatPartnerEvent
     * @since 0.5.0
     */
    public final void removeChatPartnerListener(final Consumer<ChatPartnerEvent> chatPartnerListener) {
        chatPartnerListeners.remove(requireNonNull(chatPartnerListener, "chatPartnerListener must not be nulll"));
    }

    private final void notifyChatPartnerListeners(final ChatPartnerEvent chatPartnerEvent) {
        requireNonNull(chatPartnerEvent, "chatPartnerEvent must not be null");
        for (final Consumer<ChatPartnerEvent> chatPartnerListener : chatPartnerListeners) {
            try {
                chatPartnerListener.accept(chatPartnerEvent);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private final void notifyChatPartnerListeners(final Jid oldChatPartner, final Jid newChatPartner) {
        notifyChatPartnerListeners(new ChatPartnerEvent(this, requireNonNull(oldChatPartner, "oldChatPartner must not be null"), requireNonNull(newChatPartner, "newChatPartner must not be null")));
    }

    /**
     * Sends a chat message to the chat partner.
     *
     * @param message The message.
     */
    @Override
    public Message sendMessage(String message) {
        return sendMessage(new Message(chatPartner, Message.Type.CHAT, message));
    }

    /**
     * Sends a chat message to the chat partner.
     *
     * @param message The message.
     */
    @Override
    public Message sendMessage(Message message) {
        // the message type generated by the user's client SHOULD be "chat" and the contact's client SHOULD preserve that message type in subsequent replies.
        // The user's client also SHOULD include a <thread/> element with its initial message, which the contact's client SHOULD also preserve during the life of the chat session (see Section 5.2.5).
        Message m = new Message(chatPartner, Message.Type.CHAT, message.getBodies(), message.getSubjects(), thread, message.getParentThread(), message.getId(), message.getFrom(), message.getLanguage(), message.getExtensions(), message.getError());
        xmppSession.send(m);
        return m;
    }

    /**
     * Gets the chat partner of this chat session.
     *
     * @return The chat partner.
     */
    public Jid getChatPartner() {
        return chatPartner;
    }

    final void setChatPartner(final Jid chatPartner) {
        final Jid oldChatPartner = this.chatPartner;
        this.chatPartner = chatPartner;
        if (!Objects.equals(oldChatPartner, chatPartner))
            notifyChatPartnerListeners(oldChatPartner, chatPartner);
    }

    /**
     * Gets the thread id which is used for this chat session.
     *
     * @return The thread id.
     */
    public String getThread() {
        return thread;
    }

    @Override
    public void close() {
        chatManager.destroyChatSession(this);
    }

    /**
     * A {@code ChatPartnerEvent} is fired, whenever a {@link ChatSession}'s partner was
     * replaced.
     *
     * @author Markus KARG (markus@headcrashing.eu)
     * @see ChatSession#addChatPartnerListener(Consumer)
     * @see ChatSession#removeChatPartnerListener(Consumer)
     * @since 0.5.0
     */
    @SuppressWarnings("serial")
    public static final class ChatPartnerEvent extends EventObject {

        private final Jid oldChatPartner;

        private final Jid newChatPartner;

        /**
         * Constructs a {@link ChatPartnerEvent}.
         *
         * @param source         The {@link ChatSession} on which the event initially occurred.
         * @param oldChatPartner The {@link Jid} of the old chat partner. Must not be {@code null}.
         * @param newChatPartner The {@link Jid} of the new chat partner. Must not be {@code null}.
         * @see #getOldChatPartner()
         * @see #getNewChatPartner()
         */
        private ChatPartnerEvent(final ChatSession source, final Jid oldChatPartner, final Jid newChatPartner) {
            super(requireNonNull(source, "source must not be null"));
            this.oldChatPartner = requireNonNull(oldChatPartner, "oldChatPartner must not be null");
            this.newChatPartner = requireNonNull(newChatPartner, "newChatPartner must not be null");
        }

        /**
         * Gets the JID of the new chat partner. Will never be {@code null}.
         *
         * @return The JID of the new chat partner.
         * @see #getOldChatPartner()
         */
        public final Jid getNewChatPartner() {
            return newChatPartner;
        }

        /**
         * Gets the JID of the old chat partner. Will never be {@code null}.
         *
         * @return The JID of the old chat partner.
         * @see #getNewChatPartner()
         */
        public final Jid getOldChatPartner() {
            return oldChatPartner;
        }
    }
}