package org.xmpp.extension.messagedeliveryreceipts;

import java.util.Date;
import java.util.EventObject;

/**
 * This event notifies listeners, when a message has been successfully delivered.
 *
 * @author Christian Schudt
 * @see MessageDeliveredListener
 */
public final class MessageDeliveredEvent extends EventObject {
    private final String messageId;

    private final Date deliveryDate;

    /**
     * Constructs a message delivered event.
     *
     * @param source       The object on which the event initially occurred.
     * @param messageId    The message id of the delivered message.
     * @param deliveryDate The date of the delivery.
     * @throws IllegalArgumentException if source is null.
     */
    MessageDeliveredEvent(Object source, String messageId, Date deliveryDate) {
        super(source);
        this.messageId = messageId;
        this.deliveryDate = deliveryDate;
    }

    /**
     * Gets the message id of the successfully delivered message.
     *
     * @return The message id.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets the date, when the original message has been delivered, i.e. when the delivery receipt has been sent.
     *
     * @return The delivery date.
     */
    public Date getDeliveryDate() {
        return deliveryDate;
    }
}
