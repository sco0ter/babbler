# Basic Concepts

## Connection Architecture

There are three possible ways to bind XMPP to a transport:

1. [TCP binding](https://xmpp.org/rfcs/rfc6120.html#tcp)
2. [HTTP binding (BOSH: XEP-0124)](https://xmpp.org/extensions/xep-0124.html)
3. [WebSocket binding (RFC 7395)](https://tools.ietf.org/html/rfc7395)

Each transport binding

1. has its own configuration, e.g. BOSH connections could use a key sequence, while TCP connections
could use a white space interval.
2. can use different transport implementations, e.g. a BOSH connection can be established with
different HTTP implementations like `java.net.HttpURLConnection` or `java.net.http.HttpClient`.

A transport connector establishes a connection using one of the three transport bindings with a specific implementation,
i.e. it is a factory for a connection.

A `Connection` is used by a `Session` to send and receive XMPP elements.

The following diagram shows the involved classes:

![Connection Architecture](ConnectionArchitecture.png)