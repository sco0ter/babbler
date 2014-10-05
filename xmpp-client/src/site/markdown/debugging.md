# Debugging XMPP Traffic
---

## Default Debugger

By default, XMPP traffic is printed to `System.out`, if you start your application in debug mode and do not configure anything else.

You can disable or enable it with the following code:

```java
XmppSessionConfiguration configuration = new XmppSessionConfiguration();
configuration.setDebugMode(false);
XmppSessionConfiguration.setDefault(configuration);
```
(Setting the configuration as default for all sessions)


```java
XmppSessionConfiguration configuration = new XmppSessionConfiguration();
configuration.setDebugMode(false);
XmppSession xmppSession = new XmppSession(domain, configuration);
```
(or passing it to each session)

## Custom Debugger

You can also configure the `XmppSession` to use a custom debugger by passing your own implementation of `XmppDebugger` to the configuration:

```java
configuration.setDebugger(new MyDebugger());
```

## Visual Debugger

There's already an advanced visual debugger implementation in the xmpp-debug project:

![Visual Debugger](VisualDebugger.png)

You can use it like that:

```java
configuration.setDebugger(new VisualDebugger());
```
