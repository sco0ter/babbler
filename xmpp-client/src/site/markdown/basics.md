# Getting started
---

## Creating a XMPP session

The first thing you want to do in order to connect to a XMPP server is creating a ```XmppSession``` object:

```java
XmppSession xmppSession = new XmppSession("xmppDomain");
```

A session to a XMPP server can be established in two ways:

1. By a [normal TCP socket connection](http://xmpp.org/rfcs/rfc6120.html#tcp)
2. By a [BOSH connection (XEP-0124)](http://xmpp.org/extensions/xep-0124.html)

By default, the ```XmppSession``` instance will try to connect to the domain with a TCP connection first (port 5222) during the connection process.
If connection fails, it will try to connect with BOSH (port 5280).
The hostname and port is determined by doing a DNS lookup.

You can also configure the connections manually and specify concrete connection instances instead (e.g. if you want to use another port or want to use a proxy):

```java
Connection tcpConnection = new TcpConnection("hostname", 5222);
```

If you also want to use a BOSH connection with a HTTP proxy, use the following class:

```java
Connection boshConnection = new BoshConnection("hostname", 80, new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyServer", 3128)));
```

Then create the session with both connections:

```java
XmppSession xmppSession = new XmppSession("hostname", tcpConnection, boshConnection);
```

During connecting, the session will try both connections in order, until a connection is established.

The ```XmppSession``` instance is the central object. Every other action you will do revolves around this instance (e.g. sending and receiving messages).

Here\'s an overview over the relation between session and connections:

![Architecture](XmppSession.png)


## Preparing the session

Before connecting to a server, you should configure your XMPP session.

You might want to do one of the following:

* Adding event listeners in order to listen for incoming messages, roster and presence changes or to modify outgoing messages.
* Setting up a custom SSL
* Enable stream compression (XEP-0138)
* Configuring extensions, e.g.
    * Enable or disable certain extensions
    * Setting an identity for the connection (Service Discovery)
    * etc.


Here are some examples:

```java
// Setting a custom SSL context
xmppSession.getSecurityManager().setSSLContext(sslContext);
// Listen for presence changes
xmppSession.addPresenceListener(new PresenceListener() {
    @Override
    public void handle(PresenceEvent e) {
        if (e.isIncoming()) {
            // Handle incoming presence.
        }
    }
});
// Listen for messages
xmppSession.addMessageListener(new MessageListener() {
    @Override
    public void handle(MessageEvent e) {
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

## Connecting to a server

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


## Authenticating and binding a resource

After connecting, you have to authenticate and bind a resource, in order to become a "connected resource". Both steps are combined as a "login":

```java
try {
   xmppSession.login("username", "password", "resource");
} catch (FailedLoginException e) {
   // Login failed, due to wrong username/password
}
```

## Establishing a presence session

After you are connected, authenticated and have bound a resource, you should now establish a presence session, by sending [initial presence](http://xmpp.org/rfcs/rfc6121.html#presence-initial):

```java
xmppSession.send(new Presence());
```

## Sending a message

Sending a message works like this:

```java
xmppSession.send(new Message(Jid.valueOf("juliet@example.net"), Message.Type.CHAT));
```

## Closing the session

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
