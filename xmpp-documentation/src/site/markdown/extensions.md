# Using Extensions
---

## Getting Extensions from Stanzas

Extensions in stanzas can simply be retrieved in the following way:

```java
DelayedDelivery delayedDelivery = message.getExtension(DelayedDelivery.class); // XEP-203
```
```java
Attention attention = message.getExtension(Attention.class); // XEP-224
```
```java
EntityCapabilities entityCapabilities = presence.getExtension(EntityCapabilities.class); // XEP-115
```

## Managing Extensions

Most extensions need some kind of logic or have to implement business rules defined in the respective XEP.

Therefore extensions need to have a manager, which handles their business rules.

While reading through the specifications, you quickly notice that nearly all extensions have one thing in common: They can be either enabled or disabled, e.g. you can either enable or disable
support for [XEP-0115: Entity Capabilities](http://xmpp.org/extensions/xep-0115.html) or [XEP-0184: Message Delivery Receipts](http://xmpp.org/extensions/xep-0184.html).

By enabling an extension, support for it will be automatically advertised by [XEP-0030: Service Discovery](http://xmpp.org/extensions/xep-0030.html).

All so-called extension managers are therefore derived from `ExtensionManager` (which provides the enabling/disabling logic).

To get an extension manager for a specific extension, you use the `getExtensionManager` method of the `XmppSession`.

**Examples:**

```java
EntityCapabilitiesManager entityCapabilitiesManager = xmppSession.getExtensionManager(EntityCapabilitiesManager.class);
entityCapabilitiesManager.setEnabled(true);
```

This will enable support for [XEP-0115: Entity Capabilities](http://xmpp.org/extensions/xep-0115.html), which means Entity Capabilities are included in every presence being sent.
Furthermore, it will analyze incoming presence stanzas for a \"caps\" extension and manage a cache of capabilities.

---

```
EntityTimeManager entityTimeManager = xmppSession.getExtensionManager(EntityTimeManager.class);
entityTimeManager.setEnabled(false);
```

This manager manages [XEP-0202: Entity Time](http://xmpp.org/extensions/xep-0202.html) and is enabled by default.
Incoming \"time\" requests are automatically replied to with the current time, while enabled.

---

```
SoftwareVersionManager softwareVersionManager = xmppSession.getExtensionManager(SoftwareVersionManager.class);
softwareVersionManager.setSoftwareVersion(new SoftwareVersion("Babbler", "1.0"));
```

If an entity requests your software version, this manager automatically replies with the specified version.

---

Managers usually also allow to interact with other entities, e.g. retrieving the time, software version, or last activity of another entity:

```
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.valueOf("juliet@example.net"));
```
