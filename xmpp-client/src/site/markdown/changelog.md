# Changelog
---

## Version 0.3.0-SNAPSHOT

### New Features

* Added support for [XEP-0071: XHTML-IM](http://xmpp.org/extensions/xep-0071.html)
* Added support for [XEP-0072: SOAP Over XMPP](http://xmpp.org/extensions/xep-0072.html)

### Improvements

* `BookmarkManager` added for more convenient management of bookmarks.
* `Comparable` interface implemented for bookmarks.



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