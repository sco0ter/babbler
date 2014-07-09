/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp.extension.privatedata.bookmarks;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.privatedata.PrivateDataManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This manager facilitates the access to the private storage by providing convenient method for adding, retrieving or removing bookmarks.
 *
 * @author Christian Schudt
 */
public final class BookmarkManager extends ExtensionManager {

    private final PrivateDataManager privateDataManager;

    private BookmarkManager(XmppSession xmppSession) {
        super(xmppSession);
        privateDataManager = xmppSession.getExtensionManager(PrivateDataManager.class);
    }

    /**
     * Gets a sorted collection of chat room bookmarks.
     *
     * @return The chat room bookmarks.
     * @throws XmppException
     */
    public Collection<ChatRoomBookmark> getChatRoomBookmarks() throws XmppException {
        return getBookmarks(ChatRoomBookmark.class);
    }

    /**
     * Gets a sorted collection of web page bookmarks.
     *
     * @return The web page bookmarks.
     * @throws XmppException
     */
    public Collection<WebPageBookmark> getWebPageBookmarks() throws XmppException {
        return getBookmarks(WebPageBookmark.class);
    }

    /**
     * Adds a bookmark.
     *
     * @param bookmark The bookmark.
     * @throws XmppException
     */
    public void addBookmark(Bookmark bookmark) throws XmppException {
        BookmarkStorage bookmarkStorage = privateDataManager.getData(BookmarkStorage.class);
        bookmarkStorage.getBookmarks().remove(bookmark);
        bookmarkStorage.getBookmarks().add(bookmark);
        privateDataManager.storeData(bookmarkStorage);
    }

    /**
     * Removes a chat room bookmark.
     *
     * @param chatRoom The chat room.
     * @throws XmppException
     */
    public void removeChatRoomBookmark(Jid chatRoom) throws XmppException {
        BookmarkStorage bookmarkStorage = privateDataManager.getData(BookmarkStorage.class);
        bookmarkStorage.getBookmarks().remove(new ChatRoomBookmark("", chatRoom));
        privateDataManager.storeData(bookmarkStorage);
    }

    /**
     * Removes a web page bookmark.
     *
     * @param webPage The web page.
     * @throws XmppException
     */
    public void removeWebPageBookmark(URL webPage) throws XmppException {
        BookmarkStorage bookmarkStorage = privateDataManager.getData(BookmarkStorage.class);
        bookmarkStorage.getBookmarks().remove(new WebPageBookmark("", webPage));
        privateDataManager.storeData(bookmarkStorage);
    }

    @SuppressWarnings("unchecked")
    private <T extends Bookmark> Collection<T> getBookmarks(Class<T> clazz) throws XmppException {
        List<T> bookmarks = new ArrayList<>();
        BookmarkStorage bookmarkStorage = privateDataManager.getData(BookmarkStorage.class);

        for (Bookmark bookmark : bookmarkStorage.getBookmarks()) {
            if (bookmark.getClass() == clazz) {
                bookmarks.add((T) bookmark);
            }
        }
        Collections.sort(bookmarks);
        return bookmarks;
    }
}
