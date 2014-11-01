# About
---

**Babbler** is a young [XMPP][XMPP] client library for Java 7 SE built on top of [JAXB][JAXB], which allows you to write XMPP clients.

[JAXB][JAXB] as underlying technology makes it robust and allows you to write extensions in a simple and clean manner.

It aims to provide good (JavaDoc) documentation, clean code, an easy to use API and a high level of software quality (which is currently ensured by more than 700 unit tests).

**Note:** Since this project is quite young, it is still a work in progress and the API might change. Comments on the API are highly appreciated (write a message on my [BitBucket account](https://bitbucket.org/sco0ter) or comment on my [Blog](http://babbler-xmpp.blogspot.de/)).

On this site you learn about some general design considerations, a brief user guide and API documentation.

## Features

* XMPP core specifications ([RFC 6120][RFC 6120], [RFC 6121][RFC 6121], [RFC 6122][RFC 6122])
* Various authentication methods: SCRAM-SHA-1, DIGEST-MD5, CRAM-MD5, PLAIN, ANONYMOUS
* Security: TLS with Hostname Verification
* 700+ unit tests
* Support for **60+** [extensions](http://xmpp.org/xmpp-protocols/xmpp-extensions/), including important ones like:
    * Multi-User Chat
    * File Transfer
    * PubSub / PEP
    * Entity Capabilities
    * BOSH
* **Compact library**, no dependencies, one core package: **rocks.xmpp**
* [JAXB][JAXB] (XML binding) in the background, in order to make it **robust**, **easy to use** and easy to extend (e.g. writing custom protocol extensions)
* **Event-driven design** by using the [Java Event Model](http://en.wikibooks.org/wiki/Java_Programming/Event_Handling)
* Good documentation
* Very liberal **MIT license**


[JAXB]: http://en.wikipedia.org/wiki/Java_Architecture_for_XML_Binding "Java Architecture for XML Binding"
[XMPP]: http://xmpp.org "eXtensible Messaging and Presence Protocol"
[RFC 6120]: http://xmpp.org/rfcs/rfc6120.html "Extensible Messaging and Presence Protocol (XMPP): Core"
[RFC 6121]: http://xmpp.org/rfcs/rfc6121.html "Extensible Messaging and Presence Protocol (XMPP): Instant Messaging and Presence"
[RFC 6122]: http://xmpp.org/rfcs/rfc6122.html "Extensible Messaging and Presence Protocol (XMPP): Address Format"