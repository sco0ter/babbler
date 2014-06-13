# Multi-User Chat
---

For all MUC-related operations, you will need the ```MultiUserChatManager```, which you can get like this:

```java
MultiUserChatManager multiUserChatManager = xmppSession.getExtensionManager(MultiUserChatManager.class);
```

## Chat Services

If you don\'t have an idea, which chat services your XMPP domain hosts, you can easily discover them:

```java
Collection<ChatService> chatServices = multiUserChatManager.getChatServices();
```

If you already know the address of your chat service, you can also create an instance of the `ChatService` directly:

```java
ChatService chatService = multiUserChatManager.createChatService(Jid.valueOf("conference.yourxmppdomain.com"));
```

A chat service allows you to do two things:

First, discovering public chat rooms hosted by this service:

```java
List<ChatRoom> publicRooms = chatService.getPublicRooms();
```

or second, creating a new chat room directly:

```java
ChatRoom chatRoom = chatService.createRoom("myroom");
```

whose room address would basically be `myroom@conference.yourxmppdomain.com`.

This can also be used, if you want to create instant chat rooms (e.g. with an UUID).

Note, that no room is created on the server! All you have now is a local `ChatRoom` instance, you can work with.


## Chat Rooms

Once you have obtained a `ChatRoom` instance as described above, you can now do multiple things with it:

* Discover occupants in the room and room information
* Entering (aka joining) and exiting (aka leaving) the room.
* Configuring a room.
* Sending messages.
* Listening to messages, subject changes, occupant events (\"leaves\" and \"joins\"), ...

### Discovering occupants and room info

```java
List<String> occupants = chatRoom.getOccupants();
```

gets the occupants, which are currently in the room (nicknames only).

```java
RoomInfo roomInfo = chatRoom.getRoomInfo();
```

gets the room info, e.g. the current subject, the max history messages, the description and room features.

### Entering a room

Before entering a room, you should add listeners to it, if you want to listen for occupants \"joins\" and \"leaves\", subject changes or messages being sent by the room, then enter the room with your desired nickname:

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

chatRoom.enter("nickname");
```



### Exiting a room

Simply call

```java
chatRoom.exit();
```

or with an optional presence status exit message:

```java
chatRoom.exit("Bye!");
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

You get the MUC with ```e.getRoomAddress()``` of the event object. A room is in the form ```room@chatservice```, so we have to create the service and then the room at that service:

```java
ChatService chatService = multiUserChatManager.createChatService(new Jid(e.getRoomAddress().getDomain()));
ChatRoom chatRoom = chatService.createRoom(e.getRoomAddress().getLocal());
```

When you've created the room, you should add listeners to it and then enter it.

### Declining an invitation

```java
e.decline("I don\'t have time right now...");
```


## Sending messages

```java
chatRoom.sendMessage("Hi all!");
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
