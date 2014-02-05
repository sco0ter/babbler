# Basics

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
Connection connection = new BoshConnection("hostname", 5222);
```
