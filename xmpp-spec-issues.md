This is a list of various issues which have occurred while reading through and implementing XEPs. Some of them were also mentioned in the XMPP standards mailing list.

## XEP-0045: Multi-User Chat

1. 7.2.18 Error Conditions does not list `<jid-malformed/>` (when no nickname provided)
2. Example 90 lacks status code 110 (self-presence)
3. Full JID in item element (e.g. Example 144, 147)
4. `muc#roomconfig_allowpm` is `boolean`, but should be `list-single`
5. `muc#roomconfig_allowinvites` is `list-single`, but should be `boolean`
6. Typo: Publish-Subcribe
7. `muc#roomconfig_whois`: Affiliations or Roles?
8. Room Subject can/should have delayed delivery?
9. Actor in #admin not needed?
10. 15.6.2 Initial Submission: Status code 174 is missing.
11. `muc#role` should be `list-single`
12. `muc#register_faqentry`: mixed occurrences of `text-multi` and `text-single`

## XEP-0048: Bookmarks

1. *A bookmark set may contain any number of conference rooms.* contradicts with XML schema (?)

## XEP-0050: Ad-Hoc Commands

1. *The "xml:lang" attribute specifies the language/locale this <command/> is intended for. This element MAY be...*
 => should be attribute
2. Is the `type` attribute of the `note` element required? [See mailing list](http://mail.jabber.org/pipermail/standards/2014-October/029266.html)

## XEP-0060: Publish-Subscribe

1. `retrieve-default-sub` feature missing in XML Schema.
2. `max-nodes-exceeded` not described anywhere.
3. 8.2 Configure Node: `node` missing in XML Schema
4. 6.5 Retrieve Items from a Node vs 5.5 Discover Items for a Node (confusing)
5. `<subscription-required xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>` vs.
   `<presence-subscription-required xmlns='http://jabber.org/protocol/pubsub#errors'/>` ?

## XEP-0065: SOCKS5 Bytestreams

1. `<activate/>` element has wrong description ( *is always empty*, while it isn't) [See mailing list](http://mail.jabber.org/pipermail/standards/2015-March/029663.html)

## XEP-0084: User Avatar

1. Should probably make use of persistent storage ([XEP-0222](http://www.xmpp.org/extensions/xep-0222.html))?
2. Example 4 is confusing because it uses only image/gif, although image/png is mandatory (?).

## XEP-0184: Message Delivery Receipts

1. Should clarify message type of receipts.

## XEP-0198: Stream Management

1. *When receiving an <r/> or <a/> element with an 'h' attribute* => The <r/> element has no 'h' attribute.

## XEP-0163: Personal Eventing Protocol

1. One node per namespace dead link (#approach-onenode) in 1.2
2. Typo: "there there"