# Message Delivery Receipts
---

Message Delivery Receipts are useful, if you want to know if a message has been delivered to the recipient.

When you send a message, you ask for a receipt and the recipient should send a receipt, if it supports the protocol.

If you enable the `MessageDeliverReceiptsManager`, your XMPP session automatically sends receipts, if they were requested in a message.


```java
MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = xmppSession.getExtensionManager(MessageDeliveryReceiptsManager.class);
messageDeliveryReceiptsManager.setEnabled(true);

messageDeliveryReceiptsManager.addMessageDeliveredListener(new MessageDeliveredListener() {
    @Override
    public void messageDelivered(MessageDeliveredEvent e) {

    }
});
```

[MUC]: http://xmpp.org/extensions/xep-0184.html "XEP-0045: Multi-User Chat"
[Mediated]: http://xmpp.org/extensions/xep-0045.html#invite-mediated
[Direct]: http://xmpp.org/extensions/xep-0249.html
