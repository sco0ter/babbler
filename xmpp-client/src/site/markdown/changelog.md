# Changelog
---

## Version 0.4.0

## New Features

* Added support for [XEP-0070: Verifying HTTP Requests via XMPP](http://xmpp.org/extensions/xep-0070.html]
* Added support for [XEP-0084: User Avatar](http://xmpp.org/extensions/xep-0084.html]
* Added visual debugger.

## Improvements

* More options to configure a connection (e.g. to set a `SocketFactory`, a keep-alive ping interval, etc...)
* Reconnection logic improved.
* Allow to use a secure BOSH connection over HTTPS.
* Periodically ping the server (XEP-0199) to allow for a more stable connection.
* Added `isRequest()` and `isResponse()` method to IQ
* Bug fix in authentication logic, which prevented successful authentication on some servers.
* Various minor bug fixes (e.g. stuff like `NullPointerException`s).
* Added more documentation.


## Version 0.3.0 (2014-08-02)

### New Features

* Added support for [XEP-0047: In-Band Bytestreams](http://xmpp.org/extensions/xep-0047.html)
* Added support for [XEP-0065: SOCKS5 Bytestreams](http://xmpp.org/extensions/xep-0065.html)
* Added support for [XEP-0066: Out of Band Data](http://xmpp.org/extensions/xep-0066.html)
* Added support for [XEP-0071: XHTML-IM](http://xmpp.org/extensions/xep-0071.html)
* Added support for [XEP-0072: SOAP Over XMPP](http://xmpp.org/extensions/xep-0072.html)
* Added support for [XEP-0095: Stream Initiation](http://xmpp.org/extensions/xep-0095.html)
* Added support for [XEP-0096: SI File Transfer](http://xmpp.org/extensions/xep-0096.html)
* Added support for [XEP-0186: Invisible Command](http://xmpp.org/extensions/xep-0186.html)
* Added support for [XEP-0300: Use of Cryptographic Hash Functions in XMPP](http://xmpp.org/extensions/xep-0300.html)

### Improvements

* Fixed Man-in-the-Middle vulnerability / Added Hostname Verification. [Read more about it](http://tersesystems.com/2014/03/23/fixing-hostname-verification/).
* `BookmarkManager` added for more convenient management of bookmarks.
* `Comparable` interface implemented for bookmarks.
* Improved BOSH logic with regard to overactivity. Also updated BOSH version to 1.11.
* Stream errors now have their own `StreamError` class and no longer derive from `Exception` due to some JAXB problems.
* Enhanced `RosterManager` API to easily rename or remove (nested) roster groups.
* Added helper methods to facilitate use of [XEP-0149: Time Periods](http://xmpp.org/extensions/xep-0149.html)
* Set encoding for the XMPP stream to UTF-8.
* Made connection establishment a little bit more robust.
* Increased performance by using buffered streams.
* Improved logic for vCard based avatars.
* toString() method implemented on many objects.
* More documentation added.

## Version 0.2.0 (2014-07-06)

### New Features

* Added support for [XEP-0045: Multi-User Chat](http://xmpp.org/extensions/xep-0045.html)
* Added support for [XEP-0079: Advanced Message Processing](http://xmpp.org/extensions/xep-0079.html)
* Added support for [XEP-0122: Data Forms Validation](http://xmpp.org/extensions/xep-0122.html)
* Added support for [XEP-0141: Data Forms Layout](http://xmpp.org/extensions/xep-0141.html)
* Added support for [XEP-0144: Roster Item Exchange](http://xmpp.org/extensions/xep-0144.html)
* Added support for [XEP-0280: Message Carbons](http://xmpp.org/extensions/xep-0280.html)
* Added support for [XEP-0335: JSON Containers](http://xmpp.org/extensions/xep-0335.html)
* RosterManager now supports (nested) contact groups

### Improvements

* `Jid` class improved (nodeprep, resourceprep, better escaping logic, caching for better performance, `Comparable interface implemented)
* Added JID Escaping feature (\"jid\\escaping\") to feature list for Service Discovery.
* Restructured the project: It\'s now separated into modules:
    * xmpp-core, which contains XML schema implementations and core classes. It could theoretically be useful for a server implementation, too.
    * xmpp-client, which contains business logic, used by XMPP clients (e.g. connection logic, roster management, ...)
* The base `Connection` class is now called `XmppSession`, which can have multiple connection methods. Each connection method is tried while connecting. That way a XMPP session can have a normal TcpConnection and an alternative BoshConnection, which is tried as fallback.
* Message, Presence and IQ classes have been moved from `org.xmpp.stanza` to `org.xmpp.stanza.client package (API change).
* The \'from\' attribute of roster/privacy lists/blocking command pushes are now checked to prevent IQ spoofing.
* Updated [XEP-0080: User Location](http://xmpp.org/extensions/xep-0080.html) implementation from version 1.7 to 1.8.
* Provided convenience methods for creating [XEP-0126: Invisibility](http://xmpp.org/extensions/xep-0126.html) privacy lists.
* `Comparable` interface implemented for `PrivacyList` and `PrivacyRule`.
* Various minor refactoring and improvements.

## Version 0.1.0 (2014-03-22)

* Initial version