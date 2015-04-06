# XEP-0202: Entity Time
---

[XEP-0202: Entity Time][Entity Time] allows you to request another entity\'s time and timezone.

Here\'s a simple example:

```java
EntityTimeManager entityTimeManager = xmppSession.getManager(EntityTimeManager.class);
EntityTime entityTime = entityTimeManager.getEntityTime(Jid.valueOf("juliet@example.net/balcony"));

OffsetDateTime date = entityTime.getDateTime();
```


[Entity Time]: http://xmpp.org/extensions/xep-0202.html "XEP-0202: Entity Time"
