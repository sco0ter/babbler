# Getting Started
---

## Establishing an XMPP Session

The first thing you want to do in order to connect to an XMPP server is creating a `XmppSession` object:

```java
XmppSession xmppSession = new XmppSession("domain");
```

The `XmppSession` instance is the central object. Every other action you will do revolves around this instance (e.g. sending and receiving messages).

A session to an XMPP server can be established in at least two ways:

1. By a [normal TCP socket connection](http://xmpp.org/rfcs/rfc6120.html#tcp)
2. By a [BOSH connection (XEP-0124)](http://xmpp.org/extensions/xep-0124.html)

By default, the `XmppSession` will try to establish a connection via TCP first during the connection process.
If the connection fails, it will try to discover alternative connection methods and try to connect with one of them (usually BOSH).
The hostname and port is determined by doing a DNS lookup.

### Configuring the Connections

You can also configure different connection methods manually (e.g. if you want to use another port or want to use a proxy).

In order to create immutable and reusable configuration objects (which could be reused by multiple sessions) and to avoid huge constructors, the Builder Pattern is used to create custom configurations:

```java
TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
    .hostname("localhost")
    .port(5222)
    .proxy(Proxy.NO_PROXY)        // Proxy for the TCP connection
    .keepAliveInterval(20)        // Whitespace keep-alive interval
    .socketFactory(socketFactory) // Custom socket factory
    .build();
```
Here's another example how to configure a BOSH connection (which would connect to the URL `http://domain:5280/http-bind/` over a HTTP proxy server):

```java
BoshConnectionConfiguration boshConfiguration = BoshConnectionConfiguration.builder()
    .hostname("domain")
    .port(5280)
    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("hostname", 3128)))
    .file("/http-bind/")
    .wait(60)  // BOSH connection manager should wait maximal 60 seconds before responding to a request.
    .build();
```

Now let's pass them to the session to tell it that it should use them:

```java
XmppSession xmppSession = new XmppSession("domain", tcpConfiguration, boshConfiguration);
```

During connecting, the session will try all configured connections in order, until a connection is established.

Here\'s an overview over the relation between the session and connections:

![Architecture](XmppSession.png)


#### Securing the Connection

You can set a custom `SSLContext` or disable the use of SSL altogether by configuring it like this:

```java
TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
    .secure(true)          // Default value is true
    .sslContext(sslContext)
    .hostnameVerifier(hostnameVerifier)
    .build();
```

Note that the use of a custom `HostnameVerifier` is possible but not recommended in most cases, since the built-in logic to verify the host name does a good job.

## Preparing the Session

Before connecting to a server, you should configure your XMPP session.

You might want to do one of the following:

* Adding event listeners in order to listen for incoming messages, roster and presence changes or to modify outgoing messages.
* Setting up a custom SSL context
* Configuring extensions, e.g.
    * Enable or disable certain extensions
    * Setting an identity for the connection (Service Discovery)
    * etc.


Here are some examples:

```java
// Listen for presence changes
xmppSession.addPresenceListener(new PresenceListener() {
    @Override
    public void handlePresence(PresenceEvent e) {
        if (e.isIncoming()) {
            // Handle incoming presence.
        }
    }
});
// Listen for messages
xmppSession.addMessageListener(new MessageListener() {
    @Override
    public void handleMessage(MessageEvent e) {
        // Handle outgoing or incoming message
    }
});
// Listen for roster pushes
xmppSession.getRosterManager().addRosterListener(new RosterListener() {
    @Override
    public void rosterChanged(RosterEvent e) {

    }
});
```

## Connecting to a Server

If you have prepared your session, you are now ready to connect to the server:

```java
try {
   xmppSession.connect();
} catch (IOException e) {
   // e.g. UnknownHostException
}
```

The session will try to connect to the XMPP server by using the configured connections in order.

Connecting involves opening the initial XMPP stream header and negotiate any features offered by the server (most likely only TLS).


## Authenticating and Binding a Resource

After connecting, you have to authenticate and bind a resource, in order to become a \"connected resource\". Both steps are understood as \"login\":

```java
try {
   xmppSession.login("username", "password", "resource");
} catch (FailedLoginException e) {
   // Login failed, due to wrong username/password
}
```

## Establishing a Presence Session

After you are connected, authenticated and have bound a resource, you should now establish a [presence session](http://xmpp.org/rfcs/rfc6121.html#presence-fundamentals), by sending [initial presence](http://xmpp.org/rfcs/rfc6121.html#presence-initial):

```java
xmppSession.send(new Presence());
```

You are now an \"available resource\" (you will appear online to your contacts) and can now start sending messages.

## Sending a Message

Sending a simple chat message works like this:

```java
xmppSession.send(new Message(Jid.valueOf("juliet@example.net"), Message.Type.CHAT));
```

## Changing Availability

If you want to change your presence availability, just send a new presence with a \"show\" value.

```java
xmppSession.send(new Presence(Presence.Show.AWAY));
```

## Closing the Session

Closing a session is simply done with:

```java
xmppSession.close();
```

Note, that ```org.xmpp.XmppSession``` implements ```java.io.Closeable```, which means you can also use the try-with-resources statement, which automatically closes the session:

```java
try (XmppSession xmppSession = new XmppSession("domain")) {
    xmppSession.connect();
} catch (Exception e) {
    // handle exception
}
```
