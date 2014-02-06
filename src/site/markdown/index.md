# About
---

**Babbler** is a young [XMPP][XMPP] client library for Java 7 SE built on top of [JAXB][JAXB], which allows you to write XMPP clients.

[JAXB][JAXB] as underlying technology makes it robust and allows you to write extensions in a simple and clean manner.

It aims to provide good (JavaDoc) documentation, clean code, an easy to use API and a high level of software quality (which is currently ensured by ca. 500 tests).

It supports most of the core specifications ([RFC 6120](http://xmpp.org/rfcs/rfc6120.html), [RFC 6121](http://xmpp.org/rfcs/rfc6121.html), [RFC 6122](http://xmpp.org/rfcs/rfc6122.html)), short of a few minor things like SCRAM-SHA-1 authentication.

**Note:** Since this project is quite young, it is still in beta status and in development. The API might change. Comments on the API are highly appreciated (write a message on my [BitBucket account](https://bitbucket.org/sco0ter) or comment on my [Blog](http://babbler-xmpp.blogspot.de/)).

On this site you learn about some general design considerations, a brief user guide and API documentation.


## Design Considerations

While designing the library I considered the following guidelines:

* Use [JAXB][JAXB], in order to make it robust, easy to use and easy to extend (i.e. writing custom protocol extensions).
* Use an adequate core package: **org.xmpp** and use a separate sub-package for each XMPP namespace (e.g. bind, sasl, stanza, tls) and each extension.
* Use [java.util.logging](http://docs.oracle.com/javase/7/docs/api/java/util/logging/package-summary.html) to properly log exceptions and XMPP input/output (not the best framework, but I didn't want to include a 3rd party logger).
* Use an event-driven design by using the [Java Event Model](http://en.wikibooks.org/wiki/Java_Programming/Event_Handling).
* Keep it lean and simple, e.g. by not relying on 3rd party dependencies (if possible)
* Respect Java Coding Guidelines and best practices, e.g. use upper-case enum values, use appropriate modifiers for visibility, make classes final and immutable etc...
* Be as accurate and strict as possible with the XMPP specifications.
* Use a class, which represents a JID.


[JAXB]: http://en.wikipedia.org/wiki/Java_Architecture_for_XML_Binding "Java Architecture for XML Binding"
[XMPP]: http://xmpp.org "eXtensible Messaging and Presence Protocol"