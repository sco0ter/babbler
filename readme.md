**Babbler** is a young [XMPP](http://xmpp.org) client library for Java SE 7 based on JAXB as XML processing technology.

It aims to provide good JavaDoc documentation, clean code, an easy to use API and a high level of software quality (which is currently ensured by 400+ unit tests).

You can find some more information [here](http://babbler-xmpp.blogspot.de/).

It supports most of the core specifications ([RFC 6120](http://xmpp.org/rfcs/rfc6120.html), [RFC 6121](http://xmpp.org/rfcs/rfc6121.html), [RFC 6122](http://xmpp.org/rfcs/rfc6122.html)), short of a few minor things like SCRAM-SHA-1 authentication.

Since this project is quite young, the API might change. Comments on the API are appreciated.

# Supported extensions
*(Only listing historical and standard tracks extensions with status 'Draft' or 'Final')*

* ![alt supported][supported]           [XEP-0004: Data Forms](http://xmpp.org/extensions/xep-0004.html)
* ![alt not supported][not supported]   [XEP-0009: Jabber-RPC](http://xmpp.org/extensions/xep-0009.html)
* ![alt supported][supported]           [XEP-0012: Last Activity](http://xmpp.org/extensions/xep-0012.html)
* ![alt not supported][not supported]   [XEP-0013: Flexible Offline Message Retrieval](http://xmpp.org/extensions/xep-0013.html)
* ![alt in development][in development] [XEP-0016: Privacy Lists](http://xmpp.org/extensions/xep-0016.html)
* ![alt supported][supported]           [XEP-0020: Feature Negotiation](http://xmpp.org/extensions/xep-0020.html)
* ![alt not supported][not supported]   [XEP-0027: Current Jabber OpenPGP Usage](http://xmpp.org/extensions/xep-0027.html)
* ![alt supported][supported]           [XEP-0030: Service Discovery](http://xmpp.org/extensions/xep-0030.html)
* ![alt not supported][not supported]   [XEP-0033: Extended Stanza Addressing](http://xmpp.org/extensions/xep-0033.html)
* ![alt not supported][not supported]   [XEP-0045: Multi-User Chat](http://xmpp.org/extensions/xep-0045.html)
* ![alt in development][in development] [XEP-0047: In-Band Bytestreams](http://xmpp.org/extensions/xep-0047.html)
* ![alt not supported][not supported]   [XEP-0048: Bookmarks](http://xmpp.org/extensions/xep-0048.html)
* ![alt supported][supported]           [XEP-0049: Private XML Storage](http://xmpp.org/extensions/xep-0049.html)
* ![alt not supported][not supported]   [XEP-0050: Ad-Hoc Commands](http://xmpp.org/extensions/xep-0050.html)
* ![alt supported][supported]           [XEP-0054: vcard-temp](http://xmpp.org/extensions/xep-0054.html)
* ![alt supported][supported]           [XEP-0055: Jabber Search](http://xmpp.org/extensions/xep-0055.html)
* ![alt not supported][not supported]   [XEP-0059: Result Set Management](http://xmpp.org/extensions/xep-0059.html)
* ![alt in development][in development] [XEP-0060: Publish-Subscribe](http://xmpp.org/extensions/xep-0060.html)
* ![alt not supported][not supported]   [XEP-0065: SOCKS5 Bytestreams](http://xmpp.org/extensions/xep-0065.html)
* ![alt not supported][not supported]   [XEP-0066: Out of Band Data](http://xmpp.org/extensions/xep-0066.html)
* ![alt not supported][not supported]   [XEP-0070: Verifying HTTP Requests via XMPP](http://xmpp.org/extensions/xep-0070.html)
* ![alt not supported][not supported]   [XEP-0071: XHTML-IM](http://xmpp.org/extensions/xep-0071.html)
* ![alt not supported][not supported]   [XEP-0072: SOAP Over XMPP](http://xmpp.org/extensions/xep-0072.html)
* ![alt in development][in development] [XEP-0077: In-Band Registration](http://xmpp.org/extensions/xep-0077.html)
* ![alt not supported][not supported]   [XEP-0079: Advanced Message Processing](http://xmpp.org/extensions/xep-0079.html)
* ![alt in development][in development] [XEP-0080: User Location](http://xmpp.org/extensions/xep-0080.html)
* ![alt not supported][not supported]   [XEP-0084: User Avatar](http://xmpp.org/extensions/xep-0084.html)
* ![alt in development][in development] [XEP-0085: Chat State Notifications](http://xmpp.org/extensions/xep-0085.html)
* ![alt supported][supported]           [XEP-0092: Software Version](http://xmpp.org/extensions/xep-0092.html)
* ![alt not supported][not supported]   [XEP-0095: Stream Initiation](http://xmpp.org/extensions/xep-0095.html)
* ![alt not supported][not supported]   [XEP-0096: SI File Transfer](http://xmpp.org/extensions/xep-0096.html)
* ![alt supported][supported]           [XEP-0106: JID Escaping](http://xmpp.org/extensions/xep-0106.html)
* ![alt supported][supported]           [XEP-0107: User Mood](http://xmpp.org/extensions/xep-0107.html)
* ![alt not supported][not supported]   [XEP-0108: User Activity](http://xmpp.org/extensions/xep-0108.html)
* ![alt not supported][not supported]   [XEP-0114: Jabber Component Protocol](http://xmpp.org/extensions/xep-0114.html)
* ![alt in development][in development] [XEP-0115: Entity Capabilities](http://xmpp.org/extensions/xep-0115.html)
* ![alt in development][in development] [XEP-0118: User Tune](http://xmpp.org/extensions/xep-0118.html)
* ![alt not supported][not supported]   [XEP-0122: Data Forms Validation](http://xmpp.org/extensions/xep-0122.html)
* ![alt supported][supported]           [XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)](http://xmpp.org/extensions/xep-0124.html)
* ![alt not supported][not supported]   [XEP-0131: Stanza Headers and Internet Metadata](http://xmpp.org/extensions/xep-0131.html)
* ![alt not supported][not supported]   [XEP-0136: Message Archiving](http://xmpp.org/extensions/xep-0136.html)
* ![alt not supported][not supported]   [XEP-0137: Publishing Stream Initiation Requests](http://xmpp.org/extensions/xep-0137.html)
* ![alt supported][supported]           [XEP-0138: Stream Compression](http://xmpp.org/extensions/xep-0138.html)
* ![alt not supported][not supported]   [XEP-0141: Data Forms Layout](http://xmpp.org/extensions/xep-0141.html)
* ![alt in development][in development] [XEP-0144: Roster Item Exchange](http://xmpp.org/extensions/xep-0144.html)
* ![alt supported][supported]           [XEP-0145: Annotations](http://xmpp.org/extensions/xep-0145.html)
* ![alt supported][supported]           [XEP-0153: vCard-Based Avatars](http://xmpp.org/extensions/xep-0153.html)
* ![alt not supported][not supported]   [XEP-0155: Stanza Session Negotiation](http://xmpp.org/extensions/xep-0155.html)
* ![alt supported][supported]           [XEP-0156: Discovering Alternative XMPP Connection Methods](http://xmpp.org/extensions/xep-0156.html)
* ![alt not supported][not supported]   [XEP-0158: CAPTCHA Forms](http://xmpp.org/extensions/xep-0158.html)
* ![alt not supported][not supported]   [XEP-0163: Personal Eventing Protocol](http://xmpp.org/extensions/xep-0163.html)
* ![alt not supported][not supported]   [XEP-0166: Jingle](http://xmpp.org/extensions/xep-0166.html)
* ![alt not supported][not supported]   [XEP-0167: Jingle RTP Sessions](http://xmpp.org/extensions/xep-0167.html)
* ![alt not supported][not supported]   [XEP-0171: Language Translation](http://xmpp.org/extensions/xep-0171.html)
* ![alt supported][supported]           [XEP-0172: User Nickname](http://xmpp.org/extensions/xep-0172.html)
* ![alt not supported][not supported]   [XEP-0174: Serverless Messaging](http://xmpp.org/extensions/xep-0174.html)
* ![alt not supported][not supported]   [XEP-0176: Jingle ICE-UDP Transport Method](http://xmpp.org/extensions/xep-0176.html)
* ![alt not supported][not supported]   [XEP-0177: Jingle Raw UDP Transport Method](http://xmpp.org/extensions/xep-0177.html)
* ![alt supported][supported]           [XEP-0184: Message Delivery Receipts](http://xmpp.org/extensions/xep-0184.html)
* ![alt not supported][not supported]   [XEP-0191: Blocking Command](http://xmpp.org/extensions/xep-0191.html)
* ![alt not supported][not supported]   [XEP-0198: Stream Management](http://xmpp.org/extensions/xep-0198.html)
* ![alt supported][supported]           [XEP-0199: XMPP Ping](http://xmpp.org/extensions/xep-0199.html)
* ![alt supported][supported]           [XEP-0202: Entity Time](http://xmpp.org/extensions/xep-0202.html)
* ![alt supported][supported]           [XEP-0203: Delayed Delivery](http://xmpp.org/extensions/xep-0203.html)
* ![alt supported][supported]           [XEP-0206: XMPP Over BOSH](http://xmpp.org/extensions/xep-0206.html)
* ![alt not supported][not supported]   [XEP-0220: Server Dialback](http://xmpp.org/extensions/xep-0220.html)
* ![alt supported][supported]           [XEP-0221: Data Forms Media Element](http://xmpp.org/extensions/xep-0221.html)
* ![alt supported][supported]           [XEP-0224: Attention](http://xmpp.org/extensions/xep-0224.html)
* ![alt not supported][not supported]   [XEP-0227: Portable Import/Export Format for XMPP-IM Servers](http://xmpp.org/extensions/xep-0227.html)
* ![alt not supported][not supported]   [XEP-0229: Stream Compression with LZW](http://xmpp.org/extensions/xep-0229.html)
* ![alt not supported][not supported]   [XEP-0231: Bits of Binary](http://xmpp.org/extensions/xep-0231.html)
* ![alt not supported][not supported]   [XEP-0249: Direct MUC Invitations](http://xmpp.org/extensions/xep-0249.html)
* ![alt supported][supported]           [XEP-0256: Last Activity in Presence](http://xmpp.org/extensions/xep-0256.html)
* ![alt not supported][not supported]   [XEP-0258: Security Labels in XMPP](http://xmpp.org/extensions/xep-0258.html)
* ![alt not supported][not supported]   [XEP-0260: Jingle SOCKS5 Bytestreams Transport Method](http://xmpp.org/extensions/xep-0260.html)
* ![alt not supported][not supported]   [XEP-0261: Jingle In-Band Bytestreams Transport Method](http://xmpp.org/extensions/xep-0261.html)
* ![alt not supported][not supported]   [XEP-0262: Use of ZRTP in Jingle RTP Sessions](http://xmpp.org/extensions/xep-0262.html)
* ![alt not supported][not supported]   [XEP-0266: Codecs for Jingle Audio](http://xmpp.org/extensions/xep-0266.html)
* ![alt not supported][not supported]   [XEP-0270: XMPP Compliance Suites 2010](http://xmpp.org/extensions/xep-0270.html)
* ![alt not supported][not supported]   [XEP-0288: Bidirectional Server-to-Server Connections](http://xmpp.org/extensions/xep-0288.html)
* ![alt supported][supported]           [XEP-0297: Stanza Forwarding](http://xmpp.org/extensions/xep-0297.html)
* ![alt not supported][not supported]   [XEP-0301: In-Band Real Time Text](http://xmpp.org/extensions/xep-0301.html)
* ![alt supported][supported]           [XEP-0308: Last Message Correction](http://xmpp.org/extensions/xep-0308.html)

Additionally following informational XEP documents are respected:

* ![alt supported][supported]           [XEP-0082: XMPP Date and Time Profiles](http://xmpp.org/extensions/xep-0082.html)
* ![alt supported][supported]           [XEP-0083: Nested Roster Groups](http://xmpp.org/extensions/xep-0083.html)
* ![alt supported][supported]           [XEP-0128: Service Discovery Extensions](http://xmpp.org/extensions/xep-0128.html)
* ![alt supported][supported]           [XEP-0170: Recommended Order of Stream Feature Negotiation](http://xmpp.org/extensions/xep-0170.html)
* ![alt supported][supported]           [XEP-0201: Best Practices for Message Threads](http://xmpp.org/extensions/xep-0201.html)

# License

This project is licensed under [MIT License](http://opensource.org/licenses/MIT).

# Getting started

## Creating a XMPP connection

There are two kinds of connection:

1. A [standard TCP connection](http://xmpp.org/rfcs/rfc6120.html#tcp)
2. A [BOSH connection](http://xmpp.org/extensions/xep-0124.html)

The following will create a normal connection to the given host:

```java
Connection connection = new TcpConnection("hostname", 5222);
```

If you want to establish a BOSH connection, use the following class instead:
```java
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

## Connecting

If you want to connect to the server, you can do it like that:


```java
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


```java
try {
   connection.login("username", "password", "resource");
} catch (FailedLoginException e) {
   // Login failed, due to wrong username/password
}
```

## Establishing a presence session
After you are connected, authenticated and have bound a resource you should now establish a presence session, by sending [initial presence](http://xmpp.org/rfcs/rfc6121.html#presence-initial):


```java
connection.send(new Presence());
```

# Managing extensions
## Getting extensions from stanzas

```java
DelayedDelivery delayedDelivery = message.getExtension(DelayedDelivery.class);
```

```java
Attention attention = message.getExtension(Attention.class);
```
## Enabling and using extensions

```java
MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = connection.getExtensionManager(MessageDeliveryReceiptsManager.class);
messageDeliveryReceiptsManager.setEnabled(true);
messageDeliveryReceiptsManager.addMessageDeliveredListener(new MessageDeliveredListener() {
    @Override
    public void messageDelivered(MessageDeliveredEvent e) {
        System.out.println("Message delivered: " + e.getMessageId());
    }
});
```

```java
LastActivityManager lastActivityManager = connection.getExtensionManager(LastActivityManager.class);
lastActivityManager.getLastActivity(Jid.fromString("juliet@example.net"));
```


```java
SoftwareVersionManager softwareVersionManager = connection.getExtensionManager(SoftwareVersionManager.class);
SoftwareVersion softwareVersion = softwareVersionManager.getSoftwareVersion(Jid.fromString("romeo@example.net"));
```

[supported]: /sco0ter/babbler/raw/tip/supported.png "Is supported"
[not supported]: /sco0ter/babbler/raw/tip/notsupported.png "Is not supported"
[in development]: /sco0ter/babbler/raw/tip/development.png "Is in development or planned"