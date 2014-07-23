# File Transfer
---

Implementing file transfer in XMPP is a relative complex task, because there are so many specifications involved, if you want to do it right.

Here\'s the \"short\" list of XEPs related to file transfer:

* [XEP-0066: Out of Band Data][Out of Band Data]
* [XEP-0047: In-Band Bytestreams][In-Band Bytestreams]
* [XEP-0065: SOCKS5 Bytestreams][SOCKS5 Bytestreams]
* [XEP-0095: Stream Initiation][Stream Initiation]
* [XEP-0096: SI File Transfer][SI File Transfer]
* [XEP-0166: Jingle][Jingle]
* [XEP-0234: Jingle File Transfer][Jingle File Transfer]
* [XEP-0260: Jingle SOCKS5 Bytestreams Transport Method][Jingle SOCKS5 Bytestreams Transport Method]
* [XEP-0261: Jingle In-Band Bytestreams Transport Method][Jingle In-Band Bytestreams Transport Method]

There are three approaches for transferring files. Let\'s have a closer look at them.

## The Old School Approach

First there is the old approach to just transfer files by communicating an URL to a contact, who then downloads the file via HTTP GET.
This approach is pretty simple, but inflexible and is described in [XEP-0066: Out of Band Data][Out of Band Data].

## The Classic Approach

Next we have the [XEP-0095: Stream Initiation][Stream Initiation] and [XEP-0096: SI File Transfer][SI File Transfer] approach, which has been around since a long time and until now is still the recommended way of doing file transfers.
Both XEPs are tightly coupled and can\'t really live without the other. Basically what they are doing is to communicate the raw file information (e.g. file name, description, size, ...) and a request to accept the file over a stream method.

If accepted [XEP-0065: SOCKS5 Bytestreams][SOCKS5 Bytestreams] takes effect: The sender offers a list of stream hosts and the receiver tries to connect to them using the SOCKS5 protocol. I everything works the file will be transferred over the negotiated stream host.

But what if the receiver is behind a firewall and can\'t connect to any offered stream host?

In this case the sender transfers the file directly over the XMPP protocol (which should naturally always work) as described in [XEP-0047: In-Band Bytestreams][In-Band Bytestreams].

This approach ensures, that the file can be transferred almost always.

## The Modern Jingle Approach

Nonetheless there\'s a more modern approach upcoming: [XEP-0234: Jingle File Transfer][Jingle File Transfer], which communicates the file information and negotiates the transport method over [XEP-0166: Jingle][Jingle].
The two transport methods are still the same, but are negotiated in the Jingle fashion via [XEP-0260: Jingle SOCKS5 Bytestreams Transport Method][Jingle SOCKS5 Bytestreams Transport Method] and [XEP-0261: Jingle In-Band Bytestreams Transport Method][Jingle In-Band Bytestreams Transport Method].

One of the big differences to the classic approach is that the transport method is negotiated bidirectionally. That means if the receiver can\'t connect to any of the offered stream hosts, it could suggest stream hosts by itself to the sender, which increases the chances of a successful transport negotiation.

Only if the SOCKS5 negotiation fails on both sides, the parties would fallback to In-Band Bytestreams.


## The File Transfer API


```java
...
```

[In-Band Bytestreams]: http://xmpp.org/extensions/xep-0047.html "XEP-0047: In-Band Bytestreams"
[SOCKS5 Bytestreams]: http://xmpp.org/extensions/xep-0065.html "XEP-0065: SOCKS5 Bytestreams"
[Out of Band Data]: http://xmpp.org/extensions/xep-0066.html "XEP-0066: Out of Band Data"
[Stream Initiation]: http://xmpp.org/extensions/xep-0095.html "XEP-0095: Stream Initiation"
[SI File Transfer]: http://xmpp.org/extensions/xep-0096.html "XEP-0096: SI File Transfer"
[Jingle]: http://xmpp.org/extensions/xep-0166.html "XEP-0166: Jingle"
[Jingle File Transfer]: http://xmpp.org/extensions/xep-0234.html "XEP-0234: Jingle File Transfer"
[Jingle SOCKS5 Bytestreams Transport Method]: http://xmpp.org/extensions/xep-0260.html "XEP-0260: Jingle SOCKS5 Bytestreams Transport Method"
[Jingle In-Band Bytestreams Transport Method]: http://xmpp.org/extensions/xep-0261.html "XEP-0261: Jingle In-Band Bytestreams Transport Method"