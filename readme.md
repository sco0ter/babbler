**Babbler** is a young [XMPP](http://xmpp.org) client library for Java SE 7 based on JAXB as XML processing technology.

It aims to provide good JavaDoc documentation, clean code, an easy to use API and a high level of software quality (which is currently ensured by 700+ unit tests).

You can find some more information [here](http://babbler-xmpp.blogspot.de/).

[HERE'S THE PROJECT SITE WITH DOCUMENTATION.](http://sco0ter.bitbucket.org/babbler/)

It supports the core specifications ([RFC 6120](http://xmpp.org/rfcs/rfc6120.html), [RFC 6121](http://xmpp.org/rfcs/rfc6121.html), [RFC 6122](http://xmpp.org/rfcs/rfc6122.html)), short of optional features like Roster Versioning.

Since this project is quite young, the API might change. Comments on the API are appreciated.

# Supported Extensions
*(Only listing historical and standard tracks extensions with status 'Draft' or 'Final' that are applicable to XMPP clients)*

* ![supported][supported]           [XEP-0004: Data Forms](http://xmpp.org/extensions/xep-0004.html)
* ![supported][supported]           [XEP-0009: Jabber-RPC](http://xmpp.org/extensions/xep-0009.html)
* ![supported][supported]           [XEP-0012: Last Activity](http://xmpp.org/extensions/xep-0012.html)
* ![supported][supported]           [XEP-0013: Flexible Offline Message Retrieval](http://xmpp.org/extensions/xep-0013.html)
* ![supported][supported]           [XEP-0016: Privacy Lists](http://xmpp.org/extensions/xep-0016.html)
* ![supported][supported]           [XEP-0020: Feature Negotiation](http://xmpp.org/extensions/xep-0020.html)
* ![supported][supported]           [XEP-0030: Service Discovery](http://xmpp.org/extensions/xep-0030.html)
* ![supported][supported]           [XEP-0033: Extended Stanza Addressing](http://xmpp.org/extensions/xep-0033.html)
* ![supported][supported]           [XEP-0045: Multi-User Chat](http://xmpp.org/extensions/xep-0045.html)
* ![in development][in development] [XEP-0047: In-Band Bytestreams](http://xmpp.org/extensions/xep-0047.html)
* ![supported][supported]           [XEP-0048: Bookmarks](http://xmpp.org/extensions/xep-0048.html)
* ![supported][supported]           [XEP-0049: Private XML Storage](http://xmpp.org/extensions/xep-0049.html)
* ![not supported][not supported]   [XEP-0050: Ad-Hoc Commands](http://xmpp.org/extensions/xep-0050.html)
* ![supported][supported]           [XEP-0054: vcard-temp](http://xmpp.org/extensions/xep-0054.html)
* ![supported][supported]           [XEP-0055: Jabber Search](http://xmpp.org/extensions/xep-0055.html)
* ![in development][in development] [XEP-0059: Result Set Management](http://xmpp.org/extensions/xep-0059.html)
* ![supported][supported]           [XEP-0060: Publish-Subscribe](http://xmpp.org/extensions/xep-0060.html)
* ![not supported][not supported]   [XEP-0065: SOCKS5 Bytestreams](http://xmpp.org/extensions/xep-0065.html)
* ![not supported][not supported]   [XEP-0066: Out of Band Data](http://xmpp.org/extensions/xep-0066.html)
* ![not supported][not supported]   [XEP-0070: Verifying HTTP Requests via XMPP](http://xmpp.org/extensions/xep-0070.html)
* ![supported][supported]           [XEP-0071: XHTML-IM](http://xmpp.org/extensions/xep-0071.html)
* ![supported][supported]           [XEP-0072: SOAP Over XMPP](http://xmpp.org/extensions/xep-0072.html)
* ![supported][supported]           [XEP-0077: In-Band Registration](http://xmpp.org/extensions/xep-0077.html)
* ![supported][supported]           [XEP-0079: Advanced Message Processing](http://xmpp.org/extensions/xep-0079.html)
* ![supported][supported]           [XEP-0080: User Location](http://xmpp.org/extensions/xep-0080.html)
* ![not supported][not supported]   [XEP-0084: User Avatar](http://xmpp.org/extensions/xep-0084.html)
* ![supported][supported]           [XEP-0085: Chat State Notifications](http://xmpp.org/extensions/xep-0085.html)
* ![supported][supported]           [XEP-0092: Software Version](http://xmpp.org/extensions/xep-0092.html)
* ![not supported][not supported]   [XEP-0095: Stream Initiation](http://xmpp.org/extensions/xep-0095.html)
* ![not supported][not supported]   [XEP-0096: SI File Transfer](http://xmpp.org/extensions/xep-0096.html)
* ![supported][supported]           [XEP-0106: JID Escaping](http://xmpp.org/extensions/xep-0106.html)
* ![supported][supported]           [XEP-0107: User Mood](http://xmpp.org/extensions/xep-0107.html)
* ![supported][supported]           [XEP-0108: User Activity](http://xmpp.org/extensions/xep-0108.html)
* ![supported][supported]           [XEP-0115: Entity Capabilities](http://xmpp.org/extensions/xep-0115.html)
* ![supported][supported]           [XEP-0118: User Tune](http://xmpp.org/extensions/xep-0118.html)
* ![supported][supported]           [XEP-0122: Data Forms Validation](http://xmpp.org/extensions/xep-0122.html)
* ![supported][supported]           [XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)](http://xmpp.org/extensions/xep-0124.html)
* ![supported][supported]           [XEP-0131: Stanza Headers and Internet Metadata](http://xmpp.org/extensions/xep-0131.html)
* ![not supported][not supported]   [XEP-0136: Message Archiving](http://xmpp.org/extensions/xep-0136.html)
* ![not supported][not supported]   [XEP-0137: Publishing Stream Initiation Requests](http://xmpp.org/extensions/xep-0137.html)
* ![supported][supported]           [XEP-0138: Stream Compression](http://xmpp.org/extensions/xep-0138.html)
* ![supported][supported]           [XEP-0141: Data Forms Layout](http://xmpp.org/extensions/xep-0141.html)
* ![supported][supported]           [XEP-0144: Roster Item Exchange](http://xmpp.org/extensions/xep-0144.html)
* ![supported][supported]           [XEP-0145: Annotations](http://xmpp.org/extensions/xep-0145.html)
* ![supported][supported]           [XEP-0152: Reachability Addresses](http://xmpp.org/extensions/xep-0152.html)
* ![supported][supported]           [XEP-0153: vCard-Based Avatars](http://xmpp.org/extensions/xep-0153.html)
* ![not supported][not supported]   [XEP-0155: Stanza Session Negotiation](http://xmpp.org/extensions/xep-0155.html)
* ![supported][supported]           [XEP-0156: Discovering Alternative XMPP Connection Methods](http://xmpp.org/extensions/xep-0156.html)
* ![not supported][not supported]   [XEP-0158: CAPTCHA Forms](http://xmpp.org/extensions/xep-0158.html)
* ![supported][supported]           [XEP-0163: Personal Eventing Protocol](http://xmpp.org/extensions/xep-0163.html)
* ![not supported][not supported]   [XEP-0166: Jingle](http://xmpp.org/extensions/xep-0166.html)
* ![not supported][not supported]   [XEP-0167: Jingle RTP Sessions](http://xmpp.org/extensions/xep-0167.html)
* ![not supported][not supported]   [XEP-0171: Language Translation](http://xmpp.org/extensions/xep-0171.html)
* ![supported][supported]           [XEP-0172: User Nickname](http://xmpp.org/extensions/xep-0172.html)
* ![not supported][not supported]   [XEP-0174: Serverless Messaging](http://xmpp.org/extensions/xep-0174.html)
* ![not supported][not supported]   [XEP-0176: Jingle ICE-UDP Transport Method](http://xmpp.org/extensions/xep-0176.html)
* ![not supported][not supported]   [XEP-0177: Jingle Raw UDP Transport Method](http://xmpp.org/extensions/xep-0177.html)
* ![supported][supported]           [XEP-0184: Message Delivery Receipts](http://xmpp.org/extensions/xep-0184.html)
* ![supported][supported]           [XEP-0191: Blocking Command](http://xmpp.org/extensions/xep-0191.html)
* ![not supported][not supported]   [XEP-0198: Stream Management](http://xmpp.org/extensions/xep-0198.html)
* ![supported][supported]           [XEP-0199: XMPP Ping](http://xmpp.org/extensions/xep-0199.html)
* ![supported][supported]           [XEP-0202: Entity Time](http://xmpp.org/extensions/xep-0202.html)
* ![supported][supported]           [XEP-0203: Delayed Delivery](http://xmpp.org/extensions/xep-0203.html)
* ![supported][supported]           [XEP-0206: XMPP Over BOSH](http://xmpp.org/extensions/xep-0206.html)
* ![supported][supported]           [XEP-0221: Data Forms Media Element](http://xmpp.org/extensions/xep-0221.html)
* ![supported][supported]           [XEP-0224: Attention](http://xmpp.org/extensions/xep-0224.html)
* ![not supported][not supported]   [XEP-0229: Stream Compression with LZW](http://xmpp.org/extensions/xep-0229.html)
* ![not supported][not supported]   [XEP-0231: Bits of Binary](http://xmpp.org/extensions/xep-0231.html)
* ![supported][supported]           [XEP-0249: Direct MUC Invitations](http://xmpp.org/extensions/xep-0249.html)
* ![supported][supported]           [XEP-0256: Last Activity in Presence](http://xmpp.org/extensions/xep-0256.html)
* ![not supported][not supported]   [XEP-0258: Security Labels in XMPP](http://xmpp.org/extensions/xep-0258.html)
* ![not supported][not supported]   [XEP-0260: Jingle SOCKS5 Bytestreams Transport Method](http://xmpp.org/extensions/xep-0260.html)
* ![not supported][not supported]   [XEP-0261: Jingle In-Band Bytestreams Transport Method](http://xmpp.org/extensions/xep-0261.html)
* ![not supported][not supported]   [XEP-0262: Use of ZRTP in Jingle RTP Sessions](http://xmpp.org/extensions/xep-0262.html)
* ![not supported][not supported]   [XEP-0266: Codecs for Jingle Audio](http://xmpp.org/extensions/xep-0266.html)
* ![supported][supported]           [XEP-0297: Stanza Forwarding](http://xmpp.org/extensions/xep-0297.html)
* ![not supported][not supported]   [XEP-0301: In-Band Real Time Text](http://xmpp.org/extensions/xep-0301.html)
* ![supported][supported]           [XEP-0308: Last Message Correction](http://xmpp.org/extensions/xep-0308.html)

Supported experimental XEPs:

* ![supported][supported]           [XEP-0280: Message Carbons](http://xmpp.org/extensions/xep-0280.html)
* ![supported][supported]           [XEP-0335: JSON Containers](http://xmpp.org/extensions/xep-0335.html)


Additionally following informational XEP documents are respected:

* ![supported][supported]           [XEP-0082: XMPP Date and Time Profiles](http://xmpp.org/extensions/xep-0082.html)
* ![supported][supported]           [XEP-0083: Nested Roster Groups](http://xmpp.org/extensions/xep-0083.html)
* ![supported][supported]           [XEP-0128: Service Discovery Extensions](http://xmpp.org/extensions/xep-0128.html)
* ![supported][supported]           [XEP-0126: Invisibility](http://xmpp.org/extensions/xep-0126.html)
* ![supported][supported]           [XEP-0170: Recommended Order of Stream Feature Negotiation](http://xmpp.org/extensions/xep-0170.html)
* ![supported][supported]           [XEP-0201: Best Practices for Message Threads](http://xmpp.org/extensions/xep-0201.html)



# License

This project is licensed under [MIT License](http://opensource.org/licenses/MIT).

# Getting Started
---

## Creating an XMPP Session

The first thing you want to do in order to connect to a XMPP server is creating a ```XmppSession``` object:

```java
XmppSession xmppSession = new XmppSession("xmppDomain");
```

A session to a XMPP server can be established in two ways:

1. By a [normal TCP socket connection](http://xmpp.org/rfcs/rfc6120.html#tcp)
2. By a [BOSH connection (XEP-0124)](http://xmpp.org/extensions/xep-0124.html)

By default, the ```XmppSession``` instance will try to connect to the domain with a TCP connection first (port 5222) during the connection process.
If the connection fails, it will try to discover alternative connection methods and try to connect with one of them (usually BOSH).
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

## Preparing the Session

Before connecting to a server, you should configure your XMPP session.

You might want to do one of the following:

* Adding event listeners in order to listen for incoming messages, roster and presence changes or to modify outgoing messages.
* Setting up a custom SSL
* Enable stream compression ([XEP-0138](http://xmpp.org/extensions/xep-0138.html))
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

After connecting, you have to authenticate and bind a resource, in order to become a "connected resource". Both steps are understood as "login":

```java
try {
   xmppSession.login("username", "password", "resource");
} catch (FailedLoginException e) {
   // Login failed, due to wrong username/password
}
```

## Establishing a Presence Session

After you are connected, authenticated and have bound a resource, you should now establish a presence session, by sending [initial presence](http://xmpp.org/rfcs/rfc6121.html#presence-initial):

```java
xmppSession.send(new Presence());
```

You are now an "available resource" (you will appear online to your contacts) and can now start sending messages.

## Sending a Message

Sending a simple chat message works like this:

```java
xmppSession.send(new Message(Jid.valueOf("juliet@example.net"), Message.Type.CHAT));
```

## Changing Availability

If you want to change your presence availability, just send a new presence with a "show" value.

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

[supported]: /sco0ter/babbler/raw/tip/supported.png "Is supported"
[not supported]: /sco0ter/babbler/raw/tip/notsupported.png "Is not supported"
[in development]: /sco0ter/babbler/raw/tip/development.png "Is in development or planned"