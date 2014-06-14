# XEP-0030: Service Discovery
---

[XEP-0030: Service Discovery][Service Discovery] allows you to discover features and items of another XMPP entity.

By enabling an extension (XEP), you automatically support it and it will be listed in the feature list of your service discovery response.

E.g., if you have enabled support for [XEP-0092: Software Version][Software Version] your client automatically includes this XEP in its feature list, when it responds to a query.

Generally speaking all enabled extension manager will add their feature to the feature list of service discovery. If you disable a certain extension, it will be removed.

## Discover Information

You discover information about another XMPP entity by sending a request to it:

```java
ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
InfoNode infoNode = serviceDiscoveryManager.discoverInformation(Jid.valueOf("example.net"));
```

`infoNode` will contain information about the entity, i.e. its identity and features it supports.

## Discover Items

You can also discover items associated with an XMPP entity.

```java
ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
ItemNode itemNode = serviceDiscoveryManager.discoverItems(Jid.valueOf("example.net"));
List<Item> items = itemNode.getItems();
```

This will discover items at the \"root\" node, but you can also discover items at another node:

```java
ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
ItemNode itemNode = serviceDiscoveryManager.discoverItems(Jid.valueOf("example.net"), "music");
List<Item> items = itemNode.getItems();
```

[Service Discovery]: http://xmpp.org/extensions/xep-0030.html "XEP-0030: Service Discovery"
[Software Version]: http://xmpp.org/extensions/xep-0092.html "XEP-0092: Software Version"