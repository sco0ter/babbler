# XEP-0060: Publish-Subscribe
---

[XEP-0060: Publish-Subscribe][PubSub] allows XMPP entities to create nodes (topics) at a pubsub service and publish information at those nodes; an event notification (with or without payload) is then broadcasted to all entities that have subscribed to the node. Pubsub therefore adheres to the classic Observer design pattern and can serve as the foundation for a wide variety of applications, including news feeds, content syndication, rich presence, geolocation, workflow systems, network management systems, and any other application that requires event notifications.

For all PubSub related operations you need the `PubSubManager`.

```java
PubSubManager pubSubManager = xmppSession.getExtensionManager(PubSubManager.class);
```

## Discovering PubSub Services

If you don\'t know the address of your server\'s pubsub service, you can discover pubsub services like that:

```java
Collection<PubSubService> pubSubServices = pubSubManager.getPubSubServices();
```

The resulting list will contain the available PubSub services on your server. Most often it\'s probably only one: \"pubsub.yourxmppdomain\".

## Using a PubSub Service

If you know the address of a PubSub service, you can also create a pubsub service like this:

```java
PubSubService pubSubService = pubSubManager.createPubSubService(Jid.valueOf("pubsub.yourxmppdomain"));
```

The `PubSubService` instance allows you to perform all use cases described in [XEP-0060][PubSub]. Here are the most important of them:

### Discovering PubSub Features

Because [XEP-0060][PubSub] is quite complex and many features are optional, a PubSub service might not support all PubSub features. You can get the supported features of your service like that:

```java
Collection<PubSubFeature> pubSubFeatures = pubSubService.getFeatures();
```

Each feature is represented as an enum value of `PubSubFeature`, e.g. `PubSubFeature.AUTO_SUBSCRIBE`. If you want to know, if the service supports a feature, you can simply ask for:

```java
pubSubFeatures.contains(PubSubFeature.INSTANT_NODES);
```

## Working with PubSub Nodes

Whenever you want to work with a node (e.g. publish items, subscribe, unsubscribe, etc...), you first need to get an instance of `PubSubNode`.

```java
PubSubNode pubSubNode = pubSubService.getNode("princely_musings");
```

This will just create a `PubSubNode` instance locally which acts as an interface to the PubSub service and provides methods to work with the node.

### Creating the Node

This will create the node on the server:

```java
pubSubNode.create();
```

### Publishing Content to a Node

The following publishes a geo location to the node.

```java
pubSubNode.publish(new GeoLocation(45.44, 12.33));
```

*(Note, that this works, because `GeoLocation` is known to the JAXB Context. Unknown objects won\'t work)*

### Subscribing to a Node

If you are interested in receiving event notifications, whenever content of a node has been updated, you have to subscribe to the node.

```java
pubSubNode.subscribe();
```

### Unsubscribing from a Node

If you are no longer interested in receiving event notifications, you can unsubscribe from it again.

```java
pubSubNode.unsubscribe();
```

### Listening for PubSub Events

For now, you have to just deal directly with the messages. This may change in the future.

```java
xmppSession.addMessageListener(new MessageListener() {
    @Override
    public void handle(MessageEvent e) {
        if (e.isIncoming()) {
            Message message = e.getMessage();
            Event event = message.getExtension(Event.class);
            if (event != null) {
                for (Item item : event.getItems()) {
                    // ...
                }
            }
        }
    }
});
```

## Dealing with PubSub Errors

[PubSub][PubSub] defines multiple errors for various use cases. Those errors are an extension of stanza errors.

The following shows, how to deal with pubsub specific errors.

```java
try {
    PubSubService pubSubService = pubSubManager.createPubSubService(Jid.valueOf("pubsub.yourdomain"));
    PubSubNode pubSubNode = pubSubService.getNode("princely_musings");
    pubSubNode.subscribe();
} catch (XmppException e) {
    if (e instanceof StanzaException) {
        StanzaException stanzaException = (StanzaException) e;
        Object extension = stanzaException.getStanza().getError().getExtension();
        if (extension instanceof PresenceSubscriptionRequired) {
            // PubSub error <presence-subscription-required xmlns='http://jabber.org/protocol/pubsub#errors'/> occurred.
        }
    }
}
```

PubSub errors can be found in the ```org.xmpp.extension.pubsub.errors``` package.

[PubSub]: http://xmpp.org/extensions/xep-0060.html "XEP-0060: Publish-Subscribe"