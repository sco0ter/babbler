# Changelog
---

## Version 0.2.0-SNAPSHOT

* RosterManager now supports (nested) contact groups
* The 'from' attribute of roster pushes are now checked
* Roster.Contact class refactored to class Contact (API change)	
* Added support for [XEP-0141: Data Forms Layout](http://xmpp.org/extensions/xep-0141.html) 
* Added support for [XEP-0122: Data Forms Validation](http://xmpp.org/extensions/xep-0122.html)
* Added support for [XEP-0335: JSON Containers](http://xmpp.org/extensions/xep-0335.html)
* Jid class improved (nodeprep, resourceprep, better escaping logic, caching for better performance, Comparable interface implemented)
* Added JID Escaping feature ("jid\\escaping") to feature list for Service Discovery.
* Restructured the project: It's now separated into modules:
** xmpp-core, which contains XML schema implementations and core classes. It could theoretically be useful for a server implementation, too.
** xmpp-client, which contains business logic, used by XMPP clients (e.g. connection logic, roster management, ...)
* Message, Presence and IQ classes have been moved from org.xmpp.stanza to org.xmpp.stanza.client package (API change).
* The base Connection class is now called XmppSession, which can have multiple connection methods. Each connection method is tried while connecting. That way a XMPP session can have a normal TcpConnection and an alternative BoshConnection, which is tried as fallback.
* Updated [XEP-0080: User Location](http://xmpp.org/extensions/xep-0080.html) from version 1.7 to 1.8.


## Version 0.1.0 (2014-03-22)

* Initial version