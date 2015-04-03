# XEP-0115: Entity Capabilities
---

[XEP-0115: Entity Capabilities][Entity Capabilities] is an XMPP protocol extension for broadcasting and dynamically discovering client, device, or generic entity capabilities.
It caches the capabilities of entities thus obviating the need for extensive service discovery requests.

Managing this extension is actually pretty easy, since there\'s nothing much you need to do here. It all happens automatically under the hood.

Entity Capabilities are enabled by default since they are generally beneficial.

## Discovering Support for a Feature

Discovering if an entity supports a particular feature is probably the most useful thing you can do.
The following example checks, whether Juliet supports Chat State Notifications.

```java
EntityCapabilitiesManager entityCapabilitiesManager = xmppSession.getManager(EntityCapabilitiesManager.class);
boolean isSupported = entityCapabilitiesManager.isSupported("http://jabber.org/protocol/chatstates", Jid.valueOf("juliet@example.net/balcony"));
```

Note that the passed JID should be a full JID in most cases (if you want to check client capabilities).

This method will first check the cache for entity capabilities. Only if no cache entry is found it will ask the entity via Service Discovery.

For now, entity capabilities are only cached in memory for the duration of a presence session.

[Entity Capabilities]: http://xmpp.org/extensions/xep-0115.html "XEP-0115: Entity Capabilities"
