# XEP-0048: Bookmarks
---

[XEP-0048: Bookmarks][Bookmarks] allows you to store bookmarks for multi-user chat rooms and web pages by using the server-side XML storage.

The `BookmarkManager` facilitates the access to the private storage by providing convenient method for adding, retrieving or removing bookmarks.

## Adding a Bookmark

```java
BookmarkManager bookmarkManager = xmppClient.getManager(BookmarkManager.class);
bookmarkManager.addBookmark(new ChatRoomBookmark("chatroom", Jid.valueOf("chat@conference.domain")));
```

```java
bookmarkManager.addBookmark(new WebPageBookmark("chatroom", new URL("http://www.xmpp.org")));
```

## Retrieving Bookmarks

```java
Collection<ChatRoomBookmark> chatRoomBookmarks = bookmarkManager.getChatRoomBookmarks();
```

```java
Collection<WebPageBookmark> webPageBookmarks = bookmarkManager.getWebPageBookmarks();
```

## Removing a Bookmark

```java
bookmarkManager.removeChatRoomBookmark(Jid.valueOf("chat@conference.domain"));
```

```java
bookmarkManager.removeWebPageBookmark(url);
```


[Bookmarks]: http://xmpp.org/extensions/xep-0048.html "XEP-0048: Bookmarks"
