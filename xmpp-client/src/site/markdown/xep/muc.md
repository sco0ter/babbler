# Multi-User Chat
---

For all MUC-related operations, you will need the ```MultiUserChatManager```, which you can get like this:

```java
MultiUserChatManager multiUserChatManager = xmppSession.getExtensionManager(MultiUserChatManager.class);
```

## Listening to MUC invitations

The ```MultiUserChatManager``` listens for both kinds of invitations: [mediated][Mediated] and [direct][Direct].
You can listen for them in the following way:

```java
multiUserChatManager.addInvitationListener(new InvitationListener() {
    @Override
    public void invitationReceived(InvitationEvent e) {
        // e.getInviter() has invited you to the MUC room e.getRoomAddress()
    }
});
```

### Accepting an invitation

You get the MUC with ```e.getRoomAddress()``` of the event object. A room is in the form ```room@chatservice```, so we have to create the service and then the room on that service:

```java
ChatService chatService = multiUserChatManager.createChatService(new Jid(e.getRoomAddress().getDomain()));
ChatRoom chatRoom = chatService.createRoom(e.getRoomAddress().getLocal());
```

When you've created the room, you should add listeners to it and then enter it.

### Declining an invitation

```java
e.decline("I don\'t have time right now...");
```

## Entering a room

Before entering a room, you need to (locally) create the room, i.e. get a \"room\" instance, you can work with. This is done via the ```ChatService``` instance:

```java
ChatService chatService = multiUserChatManager.createChatService(Jid.valueOf("conference.domain"));
ChatRoom chatRoom = chatService.createRoom("myroom");
```

Once you have the room instance, you should add listeners to it, if you want to listen for occupants \"joins\" and \"leaves\", subject changes or messages being sent by the room:

```java
chatRoom.addMessageListener(new MessageListener() {
    @Override
    public void handle(MessageEvent e) {

    }
});
chatRoom.addSubjectChangeListener(new SubjectChangeListener() {
    @Override
    public void subjectChanged(SubjectChangeEvent e) {

    }
});
```

Finally enter the room with a nickname:

```java
try {
    chatRoom.enter("nickname");
} catch (XmppException e) {
    // E.g. if the nick is reserved, or you are not allowed to enter the room.
}
```

## Sending messages

```java
chatRoom.sendMessage("Hi all!");
```

## Exiting a room


```java
chatRoom.exit();
```

or with an optional presence status exit message:

```java
chatRoom.exit("Bye!");
```

## Banning an user

```java
try {
    chatRoom.banUser(Jid.valueOf("juliet@example.net"), "Because I can!");
} catch (XmppException e) {
    // e.g. if you are not allowed to ban a user.
}
```

## Kicking an occupant

```java
try {
    chatRoom.kickOccupant("Nickname", "Because I can!");
} catch (XmppException e) {
    // e.g. if you are not allowed to kick an occupant.
}
```

[MUC]: http://xmpp.org/extensions/xep-0045.html "XEP-0045: Multi-User Chat"
[Mediated]: http://xmpp.org/extensions/xep-0045.html#invite-mediated
[Direct]: http://xmpp.org/extensions/xep-0249.html
