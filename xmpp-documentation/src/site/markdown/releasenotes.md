# Release Notes

## 0.8.0

### `Jid` is now an interface

Motivation:

1. The old `Jid` class always constructed a new instance with every call of `asBareJid()`.
The interface now allows to reference the enclosing class' fields and returning only a "bare" view of the enclosing `Jid` instead of creating new instances.
This is a performance improvement to avoid creating too many objects and decreases GC pressure.

2. Jid being an interface allows for more flexibility. E.g. there's now a package-private `MalformedJid` implementation,
which is internally used during parsing and allows to send a `malformed-jid` stanza error.

### Update PRECIS to RFC 8264

The XMPP Address Format uses PRECIS for internationalizing the local part.
The previous `Jid` implementation used [RFC 7564](https://tools.ietf.org/html/rfc7564), which case folded the Jid's local part.
The updated PRECIS specification [RFC 8264](https://tools.ietf.org/html/rfc8264) only lower cases it.
This has the following impact:

**Old** `Jid` behavior:

```
Jid jid = Jid.of('fußball@domain');
// jid.toString() == "fussball@domain"
```

**New** `Jid` behavior:

```
Jid jid = Jid.of('fußball@domain');
// jid.toString() == "fußball@domain"
```

### XEP-0392: Consistent Color Generation

Sample usage:

```
ConsistentColor color = ConsistentColor.generate(input);
float red = color.getRed();
float green = color.getGreen();
float blue = color.getBlue();
```

(`float` is used for easier integration with `java.awt.Color`.)

### Custom ThreadFactory

For all threads being started you can now specify a custom thread factory. This might be useful when using this library in Java EE with a `ManagedThreadFactory`:

```
@Resource
private ManagedThreadFactory mtf;
```
```
XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
    .threadFactory(mtf)
    .build();
```

### Major API changes

