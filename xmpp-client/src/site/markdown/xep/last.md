# XEP-0012: Last Activity
---

[XEP-0012: Last Activity][Last Activity] allows you to determine the last activity of an XMPP entity.

This XEP defines three use cases:

1. [Finding out, how long ago a user logged out.](#lastlogout)
2. [Finding out the idle time of a connected user.](#idletime)
3. [Finding out the uptime of a server or component.](#serveruptime)


## <a name="lastlogout"></a>Getting the Last Logout Date

If you want to find out how long ago a user logged out, you query for the user\'s bare JID. The server should then answer on behalf of the user with the last logout date.

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.valueOf("juliet@im.example.com"));
// user logged out lastActivity.getSeconds() ago
```

## <a name="idletime"></a>Getting the Idle Time of a Connected Resource

In order to query a connected user for its idle time you must query for its full JID, so that the XMPP server routes the query directly to the resource.

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.valueOf("juliet@im.example.com/resource"));
```

If supported by the client, it will respond with its idle time.

You can also set the strategy for determining idle time of your session/client. E.g. you could track the last mouse movement or keyboard input. By default, the last sent message is used.

You can set a different strategy for determining idle time:

```java
lastActivityManager.setLastActivityStrategy(new LastActivityStrategy() {
    @Override
    public Date getLastActivity() {
        // Return whatever you think is your client's last activity.
        return new Date();
    }
});
```

If you don\'t want your XMPP session to respond to last activiy queries, you have to disable `LastActivityManager`, otherwise keep it enabled.

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
lastActivityManager.setEnabled(false);
```

## <a name="serveruptime"></a>Getting the Server Uptime

When querying a server or component the result is the uptime of it.

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
LastActivity lastActivity = lastActivityManager.getLastActivity(Jid.valueOf("im.example.com"));
```

or just pass `null`, if you want to query your connected server.

```java
LastActivity lastActivity = lastActivityManager.getLastActivity(null);
```

## Sending Last Activity in Presence

There\'s also a related specification: [XEP-0256: Last Activity in Presence][Last Activity in Presence], which basically says:
\"Let\'s just inform my contacts about my last activity automatically, so that they don\'t need to ask me\".

By default this information is automatically attached to your outbound presence stanzas of type "away" and "dnd".

If you don\'t want it, disable the manager:

```java
LastActivityManager lastActivityManager = xmppSession.getExtensionManager(LastActivityManager.class);
lastActivityManager.setEnabled(true);
```

[Last Activity]: http://xmpp.org/extensions/xep-0012.html "XEP-0012: Last Activity"
[Last Activity in Presence]: http://xmpp.org/extensions/xep-0256.html "XEP-0256: Last Activity in Presence"