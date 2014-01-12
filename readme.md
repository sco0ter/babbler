# What is *Babbler*?

*Babbler* is a yet experimental [XMPP](http://xmpp.org) library for Java SE based on JAXB as XML processing technology in a early stage.

You can find some more information [here](http://babbler-xmpp.blogspot.de/).

# Supported extensions
* [XEP-0004: Data Forms](http://xmpp.org/extensions/xep-0004.html)
* [XEP-0012: Last Activity](http://xmpp.org/extensions/xep-0012.html)
* [XEP-0030: Service Discovery](http://xmpp.org/extensions/xep-0030.html)
* [XEP-0055: Jabber Search](http://xmpp.org/extensions/xep-0055.html)
* [XEP-0092: Software Version](http://xmpp.org/extensions/xep-0092.html)
* [XEP-0106: JID Escaping](http://xmpp.org/extensions/xep-0106.html)
* [XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)](http://xmpp.org/extensions/xep-0124.html)
* [XEP-0156: Discovering Alternative XMPP Connection Methods](http://xmpp.org/extensions/xep-0156.html)
* [XEP-0184: Message Delivery Receipts](http://xmpp.org/extensions/xep-0184.html)
* [XEP-0199: XMPP Ping](http://xmpp.org/extensions/xep-0199.html)
* [XEP-0201: Best Practices for Message Threads](http://xmpp.org/extensions/xep-0201.html)
* [XEP-0202: Entity Time](http://xmpp.org/extensions/xep-0202.html)
* [XEP-0203: Delayed Delivery](http://xmpp.org/extensions/xep-0203.html)
* [XEP-0206: XMPP Over BOSH](http://xmpp.org/extensions/xep-0206.html)
* [XEP-0224: Attention](http://xmpp.org/extensions/xep-0224.html)
* [XEP-0256: Last Activity in Presence](http://xmpp.org/extensions/xep-0256.html)
* [XEP-0297: Stanza Forwarding](http://xmpp.org/extensions/xep-0297.html)

In development (or just in experimental state):

* [XEP-0016: Privacy Lists](http://xmpp.org/extensions/xep-0016.html)
* [XEP-0047: In-Band Bytestreams](http://xmpp.org/extensions/xep-0047.html)
* [XEP-0049: Private XML Storage](http://xmpp.org/extensions/xep-0049.html)
* [XEP-0085: Chat State Notifications](http://xmpp.org/extensions/xep-0085.html)
* [XEP-0115: Entity Capabilities](http://xmpp.org/extensions/xep-0115.html)
* [XEP-0144: Roster Item Exchange](http://xmpp.org/extensions/xep-0144.html)

# License

This project is licensed under [MIT License](http://opensource.org/licenses/MIT).

# Getting started

## Creating a XMPP connection

There are two kinds of connection:

1. A [standard TCP connection](http://xmpp.org/rfcs/rfc6120.html#tcp)
2. A [BOSH connection](http://xmpp.org/extensions/xep-0124.html)

The following will create a normal connection to the given host:

```
Connection connection = new TcpConnection("hostname", 5222);
```

If you want to establish a BOSH connection, use the following class instead:
```
Connection connection = new BoshConnection("hostname", 5222);
```

In either case you get an abstract connection object, you can now work with.

## Preparing your connection

Before actually connecting to the server, you should setup your environment:

* Setting up event listeners in order to listen for incoming messages, roster and presence changes or to modify outgoing messages.
* Configure how features are negotiated, e.g. by setting up a `SSLContext`.
* Configure extensions, e.g.
 * Enable or disable certain extensions
 * Set an identity for the connection (Service Discovery)
 * ...


```
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

## Connecting

If you want to connect to the server, you can do it like that:

```
try {
   connection.connect();
} catch (IOException e) {
   // e.g. UnknownHostException
}
```

This will

* open the initial XMPP stream to the server.
* negotiate any features offered by the server, especially TLS.

## Authenticating and binding a resource

After connecting, you have to authenticate and bind a resource, in order to become a "connected resource". After that step you will be able to send message, presence and iq stanzas.

```
try {
   connection.login("username", "password", "resource");
} catch (FailedLoginException e) {
   // Login failed, due to wrong username/password
}
```

## Establishing a presence session
After you are connected, authenticated and have bound a resource you should now establish a presence session, by sending [initial presence](http://xmpp.org/rfcs/rfc6121.html#presence-initial):
```
connection.send(new Presence());
```

# Managing extensions
## Getting extensions from stanzas
```
DelayedDelivery delayedDelivery = message.getExtension(DelayedDelivery.class);
```
```
Attention attention = message.getExtension(Attention.class);
```
## Enabling and using extensions
```
MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = connection.getExtensionManager(MessageDeliveryReceiptsManager.class);
messageDeliveryReceiptsManager.setEnabled(true);
messageDeliveryReceiptsManager.addMessageDeliveredListener(new MessageDeliveredListener() {
    @Override
    public void messageDelivered(MessageDeliveredEvent e) {
        System.out.println("Message delivered: " + e.getMessageId());
    }
});
```
```
LastActivityManager lastActivityManager = connection.getExtensionManager(LastActivityManager.class);
lastActivityManager.getLastActivity(Jid.fromString("juliet@example.net"));
```
```
SoftwareVersionManager softwareVersionManager = connection.getExtensionManager(SoftwareVersionManager.class);
SoftwareVersion softwareVersion = softwareVersionManager.getSoftwareVersion(Jid.fromString("romeo@example.net"));
```
