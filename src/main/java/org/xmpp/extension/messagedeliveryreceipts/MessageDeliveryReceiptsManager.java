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

package org.xmpp.extension.messagedeliveryreceipts;

import org.xmpp.Connection;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Christian Schudt
 */
public final class MessageDeliveryReceiptsManager extends ExtensionManager {
    private final List<MessageDeliveryListener> messageDeliveryListeners = new CopyOnWriteArrayList<>();

    private final List<MessageFilter> messageFilters = new CopyOnWriteArrayList<>();

    private Connection connection;

    public MessageDeliveryReceiptsManager(Connection connection) {
        super(connection);
        connection.getExtensionManager(ServiceDiscoveryManager.class).addFeature(new Feature("urn:xmpp:receipts"));
        // Add a default filter
        // A sender could request receipts on any non-error content message (chat, groupchat, headline, or normal) no matter if the recipient's address is a bare JID <localpart@domain.tld> or a full JID <localpart@domain.tld/resource>.
        messageFilters.add(new MessageFilter() {
            @Override
            public boolean accept(Message message) {
                return message.getType() != Message.Type.ERROR;
            }
        });

        connection.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                Message message = e.getMessage();
                if (e.isIncoming()) {

                    // If a client requests a receipt, send an ack message.
                    if (message.getExtension(Request.class) != null) {
                        Message receiptMessage = new Message();
                        receiptMessage.getExtensions().add(new Received(message.getId()));
                    }
                    Received received = message.getExtension(Received.class);
                    // If a message has been received.
                    if (received != null) {
                        // Notify the listeners about the reception.
                        for (MessageDeliveryListener messageDeliveryListener : messageDeliveryListeners) {
                            messageDeliveryListener.messageDelivered(received.getId());
                        }
                    }
                } else {
                    for (MessageFilter messageFilter : messageFilters) {
                        if (!messageFilter.accept(message)) {
                            return;
                        }
                    }
                    // To prevent looping, an entity MUST NOT include a receipt request (i.e., the <request/> element) in an ack message (i.e., a message stanza that includes the <received/> element).
                    if (message.getExtension(Received.class) != null) {
                        // Add a delivery receipt request.
                        message.getExtensions().add(new Request());
                        //
                        if (message.getId() == null) {
                            // A sender MUST include an 'id' attribute on every content message that requests a receipt, so that the sender can properly track ack messages.
                            message.setId(UUID.randomUUID().toString());
                        }
                    }
                }
            }
        });
    }

    private void addMessageFilter(MessageFilter messageFilter) {
        messageFilters.add(messageFilter);
    }

    private void removeMessageFilter(MessageFilter messageFilter) {
        messageFilters.remove(messageFilter);
    }
}
