# Exceptions and XMPP Errors
---

There is one abstract ```org.xmpp.XmppException``` for most kind of exceptions, which are related to XMPP.

For now, there are three subclasses of it:

* org.xmpp.stream.StreamException for [Stream Errors][Stream Errors].
* org.xmpp.stanza.StanzaException for [Stanza Errors][Stanza Errors].
* org.xmpp.NoResponseException for IQ queries, which didn't receive a response.


## Dealing with Stream Errors

XMPP Stream Errors are unrecoverable, which means the session will get disconnected after they have occurred.

Here's an example how to react with stream errors.

```java
xmppSession.addConnectionListener(new ConnectionListener() {
    @Override
    public void statusChanged(ConnectionEvent e) {
        if (e.getException() instanceof StreamException) {
            StreamException streamException = (StreamException) e.getException();
            if (streamException.getCondition() instanceof SystemShutdown) {
                // Server was shut down.
            }
        }
    }
});
```

## Dealing with Stanza Errors and Lack of Responses

Most stanza errors are returned in response to an IQ-get or IQ-set query.

For querying another XMPP entity, there\'s a `query` method on the `org.xmpp.XmppSession` class, which queries another entity for information and which is used by most methods.

Now two things can happen:
* The entity does not respond at all.
* The entity returned a stanza error.

In the first case a `NoResponseException` is thrown, in the latter case a `StanzaException` is thrown.

If you want to determine, which kind of exception it is, simply ask for their type.

Here's an example:

```java
try {
    EntityTime entityTime = entityTimeManager.getEntityTime(Jid.valueOf("juliet@example.net/balcony"));
} catch (XmppException e) {
    if (e instanceof NoResponseException) {
        // The entity did not respond
    } else if (e instanceof StanzaException) {
        StanzaError stanzaError = ((StanzaException) e).getStanza().getError();
        if (stanzaError.getCondition() instanceof ServiceUnavailable) {
            // The entity returned a <service-unavailable/> stanza error.
        }
    }
}
```

As you can see asking for the type and then for the specific condition gets you where you want.

Btw: If you wonder, why the error conditions aren't represented as enum, it is because some error conditions like <gone/> has additional data.


[Stream Errors]: http://xmpp.org/rfcs/rfc6120.html#streams-error "Stream Errors"
[Stanza Errors]: http://xmpp.org/rfcs/rfc6120.html#stanzas-error "Stanza Errors"
