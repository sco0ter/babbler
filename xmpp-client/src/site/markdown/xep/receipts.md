# XEP-0184: Message Delivery Receipts
---

[XEP-0184: Message Delivery Receipts][Receipts] are useful, if you want to know if a message has been delivered to the recipient.

When you send a message, you ask for a receipt and the recipient should send a receipt, if it supports the protocol.

If you enable the `MessageDeliverReceiptsManager`, your XMPP session automatically sends receipts, if they were requested in a message.


```java
MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = xmppSession.getExtensionManager(MessageDeliveryReceiptsManager.class);
messageDeliveryReceiptsManager.setEnabled(true);

messageDeliveryReceiptsManager.addMessageDeliveredListener(new MessageDeliveredListener() {
    @Override
    public void messageDelivered(MessageDeliveredEvent e) {
        // Message with ID 'e.getMessageId()' has been received!
    }
});
```

Currently, if the manager is enabled, message receipts are requested for every sent message. This will change in the future by adding message filters, so that you can request receipts for only chat messages (as example).

[Receipts]: http://xmpp.org/extensions/xep-0184.html "XEP-0184: Message Delivery Receipts"