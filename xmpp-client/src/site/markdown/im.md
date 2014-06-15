# Contacts, Presence and Messaging
---

For XMPP core aspects like roster and presence management, there\'s a corresponding manager class, directly on the `XmppSession` instance.

## Adding Contacts to your Roster

```java
xmppSession.getRosterManager().addContact(new Contact(Jid.valueOf("juliet@example.net"), "Juliet"), true, "Hi Juliet, please add me.");
```

This will create a contact on your roster and subsequently sends a presence subscription request to the user.

The roster manager also provides other methods, e.g. for deleting and updating a contact.

## Roster Pushes

You can listen for roster pushes like this:

```java
xmppSession.getRosterManager().addRosterListener(new RosterListener() {
    @Override
    public void rosterChanged(RosterEvent e) {
        // The roster event contains information about added, updated or deleted contacts.
        // TODO: Update your roster!
        Collection<Contact> contacts = xmppSession.getRosterManager().getContacts();
        for (Contact contact : contacts) {
            System.out.println(contact.getName());
        }
    }
});
```

## Dealing with Presence Updates and Subscription Requests

Whenever one of your contacts updates his presence (e.g. comes offline, goes away, goes offline, ...), you can react to it with:

```java
xmppSession.addPresenceListener(new PresenceListener() {
    @Override
    public void handle(PresenceEvent e) {
        if (e.isIncoming()) {
            Presence presence = e.getPresence();
            Contact contact = xmppSession.getRosterManager().getContact(presence.getFrom());
            if (contact != null) {
                // ... contact's presence has updated.
            }
        }
    }
});
```

Presence is also used for requesting subscription to your presence status.

```java
xmppSession.addPresenceListener(new PresenceListener() {
    @Override
    public void handle(PresenceEvent e) {
        if (e.isIncoming()) {
            Presence presence = e.getPresence();
            if (presence.getType() == Presence.Type.SUBSCRIBE) {
                // presence.getFrom() wants to subscribe to your presence.
            }
        }
    }
});
```

You can then either approve or deny the subscription request:

```java
xmppSession.getPresenceManager().approveSubscription(presence.getFrom());
```

```java
xmppSession.getPresenceManager().denySubscription(presence.getFrom());
```

## Listening for Incoming Messages

Listening for messages is done by adding a message listener to the session.

```java
xmppSession.addMessageListener(new MessageListener() {
    @Override
    public void handle(MessageEvent e) {
        if (e.isIncoming()) {
            Message message = e.getMessage();
            // Handle message.
        }
    }
});
```

## Intercepting Outgoing Messages (or Stanzas in general)

The same approach as for incoming messages is also used for outgoing messages. The only difference is the \'incoming\’ property of the event.

You can use this, if you want to add extensions to a stanza or otherwise modify the stanza.

```java
xmppSession.addMessageListener(new MessageListener() {
    @Override
    public void handle(MessageEvent e) {
        if (!e.isIncoming()) {
            Message message = e.getMessage();
            // you could add an extension to the message here.
        }
    }
});
```
