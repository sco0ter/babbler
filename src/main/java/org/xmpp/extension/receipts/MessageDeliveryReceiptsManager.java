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

package org.xmpp.extension.receipts;

import org.xmpp.Connection;
import org.xmpp.ConnectionEvent;
import org.xmpp.ConnectionListener;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.delay.DelayedDelivery;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0184.html">XEP-0184: Message Delivery Receipts</a>.
 * <p>
 * This manager automatically adds message delivery requests to outgoing messages, if enabled.
 * If a message has been received by the recipient, registered listeners will be notified about the receipt.
 * </p>
 * <p>
 * If an incoming message contains a delivery receipt request, a receipt is automatically sent back to the requesting entity.
 * </p>
 * <p>
 * Note that messages must contain an id, in order to track receipts. If a message does not contain an id, requests won't be added.
 * </p>
 * <h3>Code sample</h3>
 * <pre>
 * <code>
 * MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = connection.getExtensionManager(MessageDeliveryReceiptsManager.class);
 * messageDeliveryReceiptsManager.addMessageDeliveredListener(new MessageDeliveredListener() {
 *    {@literal @}Override
 *    public void messageDelivered(MessageDeliveredEvent e) {
 *       System.out.println("Message delivered: " + e.getMessageId());
 *    }
 * });
 * </code>
 * </pre>
 */
public final class MessageDeliveryReceiptsManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(MessageDeliveryReceiptsManager.class.getName());

    final Set<MessageDeliveredListener> messageDeliveredListeners = new CopyOnWriteArraySet<>();

    private final List<MessageFilter> messageFilters = new CopyOnWriteArrayList<>();

    /**
     * Creates the manager.
     *
     * @param connection The underlying connection.
     */
    private MessageDeliveryReceiptsManager(final Connection connection) {
        super(connection, Request.NAMESPACE);
        // Add a default filter
        // A sender could request receipts on any non-error content message (chat, groupchat, headline, or normal) no matter if the recipient's address is a bare JID <localpart@domain.tld> or a full JID <localpart@domain.tld/resource>.
        messageFilters.add(new MessageFilter() {
            @Override
            public boolean accept(Message message) {
                return message.getType() != Message.Type.ERROR;
            }
        });

        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == Connection.Status.CLOSED) {
                    messageDeliveredListeners.clear();
                }
            }
        });
        connection.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (isEnabled()) {
                    Message message = e.getMessage();

                    // If a message is received, check if it requests a receipt.
                    if (e.isIncoming()) {

                        // If a client requests a receipt, send an ack message.
                        if (message.getExtension(Request.class) != null && message.getId() != null) {
                            // Add an empty body. Otherwise some servers, won't store it in offline storage.
                            Message receiptMessage = new Message(message.getFrom(), Message.Type.NORMAL, " ");
                            receiptMessage.getExtensions().add(new Received(message.getId()));
                            connection.send(receiptMessage);
                        }
                        // If the message is a receipt.
                        Received received = message.getExtension(Received.class);
                        if (received != null) {
                            DelayedDelivery delayedDelivery = message.getExtension(DelayedDelivery.class);
                            Date deliveryDate;
                            if (delayedDelivery != null) {
                                deliveryDate = delayedDelivery.getTimeStamp();
                            } else {
                                deliveryDate = new Date();
                            }

                            // Notify the listeners about the reception.
                            for (MessageDeliveredListener messageDeliveredListener : messageDeliveredListeners) {
                                try {
                                    messageDeliveredListener.messageDelivered(new MessageDeliveredEvent(MessageDeliveryReceiptsManager.this, received.getId(), deliveryDate));
                                } catch (Exception ex) {
                                    logger.log(Level.WARNING, ex.getMessage(), ex);
                                }
                            }
                        }
                    } else {
                        // If we are sending a message, append a receipt request.
                        for (MessageFilter messageFilter : messageFilters) {
                            if (!messageFilter.accept(message)) {
                                return;
                            }
                        }
                        // To prevent looping, an entity MUST NOT include a receipt request (i.e., the <request/> element) in an ack message (i.e., a message stanza that includes the <received/> element).
                        // A sender MUST include an 'id' attribute on every content message that requests a receipt, so that the sender can properly track ack messages.
                        if (message.getExtension(Received.class) == null && message.getId() != null) {
                            // Add a delivery receipt request.
                            message.getExtensions().add(new Request());
                        }
                    }
                }
            }
        });
    }

    /**
     * Adds a message delivered listener, which allows to listen for delivered messages.
     *
     * @param messageDeliveredListener The listener.
     * @see #removeMessageDeliveredListener(MessageDeliveredListener)
     */
    public void addMessageDeliveredListener(MessageDeliveredListener messageDeliveredListener) {
        messageDeliveredListeners.add(messageDeliveredListener);
    }

    /**
     * Removes a previously added message delivered listener.
     *
     * @param messageDeliveredListener The listener.
     * @see #addMessageDeliveredListener(MessageDeliveredListener)
     */
    public void removeMessageDeliveredListener(MessageDeliveredListener messageDeliveredListener) {
        messageDeliveredListeners.remove(messageDeliveredListener);
    }

    private void addMessageFilter(MessageFilter messageFilter) {
        messageFilters.add(messageFilter);
    }

    private void removeMessageFilter(MessageFilter messageFilter) {
        messageFilters.remove(messageFilter);
    }
}
