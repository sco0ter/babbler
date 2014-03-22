# Debugging XMPP traffic
---

XMPP traffic is logged with java.util.logging (JUL) on log level "FINE".

It can be a little bit tricky to make it work, therefore here's a brief advice.

Most likely you have to "get" the logger for "org.xmpp" before initializing it. This is a little bit strange, but appearently JUL works like this.

Then you have to set the log level of the "org.xmpp" logger to Level.FINE.

Resetting the log manager first is probably also a good idea. This will remove any registered default handler.

Further JUL configuration depends on what you want. If you want to just log to the console, you have to add a ConsoleHandler with log level FINE because the default ConsoleHandler only logs to WARN.
(That's why we removed it with the reset method, because otherwise we would have two console handlers).

Here's an example, which you can use, e.g. in your main() method.

```java

private static final Logger XMPP_LOGGER = Logger.getLogger("org.xmpp");

private void initializeLogging() {
    LogManager.getLogManager().reset();

    final Logger logger = Logger.getLogger("org.xmpp");
    logger.setLevel(Level.FINE);

    Logger globalLogger = Logger.getLogger("");
    Handler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.FINE);
    consoleHandler.setFormatter(new LogFormatter());
    globalLogger.addHandler(consoleHandler);
}
```

Here's an optional formatter class, which logs XMPP traffic in only one row. The default formatter uses two rows per log, which makes it less readable.

```java
public class LogFormatter extends SimpleFormatter {
    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        Date resultDate = new Date(record.getMillis());
        sb.append(dateFormat.format(resultDate));
        sb.append(" ");
        sb.append(record.getLevel());
        sb.append("  ");
        sb.append(formatMessage(record));
        if (record.getThrown() != null) {
            record.getThrown().printStackTrace();
        }
        sb.append("\n");
        return sb.toString();
    }
}
```

XMPP traffic is then logged in the console as follows (outgoing and incoming):

```
> Mar 21, 2014 9:08:20 PM FINE  -->  <?xml version="1.0" encoding="UTF-8"?><stream:stream xml:lang="en" version="1.0" xmlns="jabber:client" xmlns:stream="http://etherx.jabber.org/streams">

> Mar 21, 2014 9:08:21 PM FINE  <--  <?xml version='1.0' ?><stream:stream from='chat.facebook.com' id='1' version='1.0' xmlns:stream='http://etherx.jabber.org/streams' xmlns='jabber:client' xml:lang='en'><stream:features><starttls xmlns='urn:ietf:params:xml:ns:xmpp-tls'/><mechanisms xmlns='urn:ietf:params:xml:ns:xmpp-sasl'><mechanism>X-FACEBOOK-PLATFORM</mechanism><mechanism>PLAIN</mechanism></mechanisms></stream:features>

> Mar 21, 2014 9:08:21 PM FINE  -->  <starttls xmlns="urn:ietf:params:xml:ns:xmpp-tls"></starttls>

> Mar 21, 2014 9:08:21 PM FINE  <--  <proceed xmlns='urn:ietf:params:xml:ns:xmpp-tls'/>
```