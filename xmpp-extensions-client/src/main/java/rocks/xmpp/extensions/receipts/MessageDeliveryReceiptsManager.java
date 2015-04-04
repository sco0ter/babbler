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

package rocks.xmpp.extensions.receipts;

import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.extensions.delay.model.DelayedDelivery;
import rocks.xmpp.extensions.receipts.model.MessageDeliveryReceipts;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0184.html">XEP-0184: Message Delivery Receipts</a>.
 * <p>
 * This manager automatically adds message delivery requests to outbound messages, if enabled.
 * If a message has been received by the recipient, registered listeners will be notified about the receipt.
 * </p>
 * <p>
 * If an inbound message contains a delivery receipt request, a receipt is automatically sent back to the requesting entity.
 * </p>
 * <p>
 * Note that messages must contain an id, in order to track receipts. If a message does not contain an id, requests won't be added.
 * </p>
 * <h3>Code sample</h3>
 * <pre>
 * <code>
 * MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = xmppSession.getManager(MessageDeliveryReceiptsManager.class);
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

    final Set<Consumer<MessageDeliveredEvent>> messageDeliveredListeners = new CopyOnWriteArraySet<>();

    private Predicate<Message> messageFilter;

    /**
     * Creates the manager.
     *
     * @param xmppSession The underlying connection.
     */
    private MessageDeliveryReceiptsManager(final XmppSession xmppSession) {
        super(xmppSession, MessageDeliveryReceipts.NAMESPACE);
    }

    @Override
    protected void initialize() {
        // Add a default filter
        // A sender could request receipts on any non-error content message (chat, groupchat, headline, or normal) no matter if the recipient's address is a bare JID <localpart@domain.tld> or a full JID <localpart@domain.tld/resource>.
        Predicate<Message> errorFilter = message -> message.getType() != Message.Type.ERROR;
        xmppSession.addSessionStatusListener(e -> {
            if (e.getStatus() == XmppSession.Status.CLOSED) {
                messageDeliveredListeners.clear();
            }
        });
        xmppSession.addInboundMessageListener(e -> {
            if (!isEnabled()) {
                return;
            }
            Message message = e.getMessage();
            // If a client requests a receipt, send an ack message.
            if (message.getExtension(MessageDeliveryReceipts.Request.class) != null && message.getId() != null) {
                // Add an empty body. Otherwise some servers, won't store it in offline storage.
                Message receiptMessage = new Message(message.getFrom(), Message.Type.NORMAL, " ");
                receiptMessage.getExtensions().add(new MessageDeliveryReceipts.Received(message.getId()));
                xmppSession.send(receiptMessage);
            }
            // If the message is a receipt.
            MessageDeliveryReceipts.Received received = message.getExtension(MessageDeliveryReceipts.Received.class);
            if (received != null) {
                // Notify the listeners about the reception.
                XmppUtils.notifyEventListeners(messageDeliveredListeners, new MessageDeliveredEvent(MessageDeliveryReceiptsManager.this, received.getId(), DelayedDelivery.deliveryDateOrNow(message)));
            }
        });
        xmppSession.addOutboundMessageListener(e -> {
            if (!isEnabled()) {
                return;
            }
            Message message = e.getMessage();
            // If we are sending a message, append a receipt request, if it passes all filters.
            Predicate<Message> predicate;
            synchronized (this) {
                if (messageFilter != null) {
                    predicate = errorFilter.and(messageFilter);
                } else {
                    predicate = errorFilter;
                }
            }
            if (!predicate.test(message)) {
                return;
            }
            // To prevent looping, an entity MUST NOT include a receipt request (i.e., the <request/> element) in an ack message (i.e., a message stanza that includes the <received/> element).
            // A sender MUST include an 'id' attribute on every content message that requests a receipt, so that the sender can properly track ack messages.
            if (message.getExtension(MessageDeliveryReceipts.Received.class) == null && message.getId() != null) {
                // Add a delivery receipt request.
                message.getExtensions().add(MessageDeliveryReceipts.REQUEST);
            }
        });
    }

    /**
     * Adds a message delivered listener, which allows to listen for delivered messages.
     *
     * @param messageDeliveredListener The listener.
     * @see #removeMessageDeliveredListener(Consumer)
     */
    public void addMessageDeliveredListener(Consumer<MessageDeliveredEvent> messageDeliveredListener) {
        messageDeliveredListeners.add(messageDeliveredListener);
    }

    /**
     * Removes a previously added message delivered listener.
     *
     * @param messageDeliveredListener The listener.
     * @see #addMessageDeliveredListener(Consumer)
     */
    public void removeMessageDeliveredListener(Consumer<MessageDeliveredEvent> messageDeliveredListener) {
        messageDeliveredListeners.remove(messageDeliveredListener);
    }

    /**
     * Outbound messages, which pass the filter automatically request a receipt, i.e. a {@code <request/>} extension.
     *
     * @param messageFilter The message filter.
     */
    public synchronized void setMessageFilter(Predicate<Message> messageFilter) {
        this.messageFilter = messageFilter;
    }
}
