# XEP-0203: Delayed Delivery
---

[XEP-0203: Delayed Delivery][Delayed Delivery] is usually attached to messages, when they are sent from the server's offline storage.

Checking if a message has delayed delivery info attached is pretty simple:

```java
DelayedDelivery delayedDelivery = message.getExtension(DelayedDelivery.class);
if (delayedDelivery != null) {
    // This message was sent from offline storage
}
```

[Delayed Delivery]: http://xmpp.org/extensions/xep-0203.html "XEP-0203: Delayed Delivery"
