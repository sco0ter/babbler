# Contacts, Presence and Messaging
---

For XMPP core aspects like roster and presence management, there\'s a corresponding manager class, directly on the connection.

## Adding contacts to your roster

```java
connection.getRosterManager().addContact(new Roster.Contact(Jid.valueOf("juliet@example.net"), "Juliet"), true, "Hi Juliet, please add me.");
```

This will create a contact on your roster and subsequently sends a presence subscription request to the user.

The roster manager also provides other methods, e.g. for deleting and updating a contact.

## Roster pushes

You can listen for roster pushes with:

```java
connection.getRosterManager().addRosterListener(new RosterListener() {
    @Override
    public void rosterChanged(RosterEvent e) {
        // The roster event contains information about added, updated or deleted contacts.
    }
});
```

## Dealing with presence updates


```java
connection.addPresenceListener(new PresenceListener() {
    @Override
    public void handle(PresenceEvent e) {
        if (e.isIncoming()) {
            Presence presence = e.getPresence();
            Roster.Contact contact = connection.getRosterManager().getContact(presence.getFrom());
            if (contact != null) {
                // ... contact's presence has updated.
            }
        }
    }
});
```

## Listen for incoming messages

Listening for messages is done by adding a message listener to the connection.

```java
connection.addMessageListener(new MessageListener() {
    @Override
    public void handle(MessageEvent e) {
        if (e.isIncoming()) {
            Message message = e.getMessage();
            // Handle message.
        }
    }
});
```

## Intercept outgoing messages (or stanzas in general)

The same approach as for incoming messages is also used for outgoing messages. The only difference is the \'incoming\’ property of the event.

You can use this, if you want to add extensions to a stanza or otherwise modify the stanza.

```java
connection.addMessageListener(new MessageListener() {
    @Override
    public void handle(MessageEvent e) {
        if (!e.isIncoming()) {
            Message message = e.getMessage();
            // add an extension to a message.
        }
    }
});
```
