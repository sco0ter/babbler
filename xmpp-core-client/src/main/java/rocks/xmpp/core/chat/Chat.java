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

package rocks.xmpp.core.chat;

import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.model.client.Message;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract chat class, which represents either a one-to-one chat session or a group chat session.
 *
 * @author Christian Schudt
 * @see ChatSession
 */
public abstract class Chat {

    private static final Logger logger = Logger.getLogger(Chat.class.getName());

    protected final Set<MessageListener> inboundMessageListeners = new CopyOnWriteArraySet<>();

    /**
     * Sends a message to the chat.
     *
     * @param message The message.
     * @return The message, which has been sent.
     */
    public abstract Message sendMessage(String message);

    /**
     * Sends a message to the chat.
     *
     * @param message The message.
     * @return The message, which has been sent.
     */
    public abstract Message sendMessage(Message message);

    /**
     * Adds a message listener, which allows to listen for inbound messages.
     *
     * @param messageListener The listener.
     * @see #removeInboundMessageListener(rocks.xmpp.core.stanza.MessageListener)
     */
    public final void addInboundMessageListener(MessageListener messageListener) {
        inboundMessageListeners.add(messageListener);
    }

    /**
     * Removes a previously added message listener.
     *
     * @param messageListener The listener.
     * @see #addInboundMessageListener(rocks.xmpp.core.stanza.MessageListener)
     */
    public final void removeInboundMessageListener(MessageListener messageListener) {
        inboundMessageListeners.remove(messageListener);
    }

    protected void notifyInboundMessageListeners(MessageEvent messageEvent) {
        for (MessageListener messageListener : inboundMessageListeners) {
            try {
                messageListener.handleMessage(messageEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }
}
