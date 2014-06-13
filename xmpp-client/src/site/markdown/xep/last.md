# XEP-0012: Last Activity
---

[XEP-0012: Last Activity][Last Activity] allows you to determine the last activity of an XMPP entity.

XMPP distinguishes three use cases here:

1. The server uptime
2. The last activity on the server, i.e. the date, when the user logged in the last time.
3. The last activity of a XMPP client, i.e. a contact.


## Getting the server uptime

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.valueOf("im.example.com"));
```

or just pass `null`, if you want to ask your connected server.

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
LastActivity lastActivity = lastActivityManager.getLastActivity(null);
```

## Getting the last login date

If you want to get the last login date for user, you query for a bare JID. The server should then answer on behalf of the user with the last login date.

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.valueOf("juliet@im.example.com"));
```

## Getting the last activity of a client

In order to query a client for its last activity it\'s important, that you query for a full JID, so that the XMPP server routes the query directly to the client.

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.valueOf("juliet@im.example.com/resource"));
```

If supported by the client, it will respond with its last activity.

Determining last activity of a client is a matter of local policy. E.g. you could track the last mouse movement or keyboard input. By default, the last sent message is taken as last activity.

You can set a different strategy for determining last activity:

```java
lastActivityManager.setLastActivityStrategy(new LastActivityStrategy() {
    @Override
    public Date getLastActivity() {
        // Return whatever you think is your client's last activity.
        return new Date();
    }
});
```

## Sending last activity in presence

There's also a related specification: [XEP-0256: Last Activity in Presence][Last Activity in Presence], which basically says:
\"Let's just inform my contacts about my last activity automatically without asking me\".

This is simply done by enabling `LastActivityManager`. Last activity information is then automatically attached to your outbound presence stanzas of type "away" and "dnd".


[Last Activity]: http://xmpp.org/extensions/xep-0012.html "XEP-0012: Last Activity"
[Last Activity in Presence]: http://xmpp.org/extensions/xep-0256.html "XEP-0256: Last Activity in Presence"