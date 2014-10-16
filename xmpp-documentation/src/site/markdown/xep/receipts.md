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

## Filtering Messages

In order to request receipts, you need to enable the manager. Simply enabling it will request receipts for every outbound non-error message.
If you want to request receipts only for a messages, you can add a filter. The following example would request receipts only for message of type `normal`.

```java
messageDeliveryReceiptsManager.addMessageFilter(new StanzaFilter<Message>() {
    @Override
    public boolean accept(Message message) {
        return message.getType() == Message.Type.NORMAL;
    }
});
```


[Receipts]: http://xmpp.org/extensions/xep-0184.html "XEP-0184: Message Delivery Receipts"