# Getting started
---

## Creating a XMPP connection

The first thing you want to do in order to connect to a XMPP server is creating a connection object.

There are two kinds of connection:

1. A [normal TCP socket connection](http://xmpp.org/rfcs/rfc6120.html#tcp)
2. A [BOSH connection (XEP-0124)](http://xmpp.org/extensions/xep-0124.html)

A normal TCP connection is created with:

```java
Connection connection = new TcpConnection("hostname", 5222);
```

If you want to establish a BOSH connection, use the following class instead:

```java
Connection connection = new BoshConnection("hostname", 5280);
```

## Preparing the connection

Before connecting to a server, you should configure your connection.

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
connection.getSecurityManager().setSSLContext(sslContext);
// Listen for presence changes
connection.addPresenceListener(new PresenceListener() {
    @Override
    public void handle(PresenceEvent e) {
        if (e.isIncoming()) {
            // Handle incoming presence.
        }
    }
});
// Listen for messages
connection.addMessageListener(new MessageListener() {
    @Override
    public void handle(MessageEvent e) {
        // Handle outgoing or incoming message
    }
});
// Listen for roster pushes
connection.getRosterManager().addRosterListener(new RosterListener() {
    @Override
    public void rosterChanged(RosterEvent e) {
    }
});
```

## Connecting to a server

If you have prepared your connection, you are now ready to connect to the server:

```java
try {
   connection.connect();
} catch (IOException e) {
   // e.g. UnknownHostException
}
```

Connecting involves opening the initial XMPP stream header and negotiate any features offered by the server (most likely only TLS).

## Authenticating and binding a resource

After connecting, you have to authenticate and bind a resource, in order to become a "connected resource". Both steps are combined as a "login":

```java
try {
   connection.login("username", "password", "resource");
} catch (FailedLoginException e) {
   // Login failed, due to wrong username/password
}
```

## Establishing a presence session

After you are connected, authenticated and have bound a resource, you should now establish a presence session, by sending [initial presence](http://xmpp.org/rfcs/rfc6121.html#presence-initial):

```java
connection.send(new Presence());
```

## Sending a message

Sending a message works like this:

```java
connection.send(new Message(Jid.fromString("juliet@example.net"), Message.Type.CHAT));
```

## Closing the connection

Closing a connection is simply done with:

```java
connection.close();
```

Note, that ```org.xmpp.Connection``` implements ```java.io.Closeable```, which means you can also use the try-with-resources statement, which automatically close the connection:

```java
try (Connection connection = new TcpConnection("hostname", 5222)) {
    connection.connect();
} catch (Exception e) {
    // handle exception
}
```
