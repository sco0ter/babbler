/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.bookmarks;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.bookmarks.model.Bookmark;
import rocks.xmpp.extensions.bookmarks.model.BookmarkStorage;
import rocks.xmpp.extensions.bookmarks.model.ChatRoomBookmark;
import rocks.xmpp.extensions.bookmarks.model.WebPageBookmark;
import rocks.xmpp.extensions.privatedata.PrivateDataManager;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This manager facilitates the access to the private storage by providing convenient method for adding, retrieving or removing bookmarks.
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
public final class BookmarkManager extends Manager {

    private final PrivateDataManager privateDataManager;

    private BookmarkManager(XmppSession xmppSession) {
        super(xmppSession);
        privateDataManager = xmppSession.getManager(PrivateDataManager.class);
    }

    /**
     * Gets a sorted collection of chat room bookmarks.
     *
     * @return The chat room bookmarks.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final Collection<ChatRoomBookmark> getChatRoomBookmarks() throws XmppException {
        return getBookmarks(ChatRoomBookmark.class);
    }

    /**
     * Gets a sorted collection of web page bookmarks.
     *
     * @return The web page bookmarks.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final Collection<WebPageBookmark> getWebPageBookmarks() throws XmppException {
        return getBookmarks(WebPageBookmark.class);
    }

    /**
     * Adds a bookmark.
     *
     * @param bookmark The bookmark.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final void addBookmark(Bookmark bookmark) throws XmppException {
        BookmarkStorage bookmarkStorage = privateDataManager.getData(BookmarkStorage.class);
        Collection<Bookmark> bookmarks = new ArrayDeque<>(bookmarkStorage.getBookmarks());
        bookmarks.remove(bookmark);
        bookmarks.add(bookmark);
        privateDataManager.storeData(new BookmarkStorage(bookmarks));
    }

    /**
     * Removes a chat room bookmark.
     *
     * @param chatRoom The chat room.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final void removeChatRoomBookmark(Jid chatRoom) throws XmppException {
        BookmarkStorage bookmarkStorage = privateDataManager.getData(BookmarkStorage.class);
        Collection<Bookmark> bookmarks = new ArrayDeque<>(bookmarkStorage.getBookmarks());
        bookmarks.remove(new ChatRoomBookmark("", chatRoom));
        privateDataManager.storeData(new BookmarkStorage(bookmarks));
    }

    /**
     * Removes a web page bookmark.
     *
     * @param webPage The web page.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public final void removeWebPageBookmark(URL webPage) throws XmppException {
        BookmarkStorage bookmarkStorage = privateDataManager.getData(BookmarkStorage.class);
        Collection<Bookmark> bookmarks = new ArrayDeque<>(bookmarkStorage.getBookmarks());
        bookmarks.remove(new WebPageBookmark("", webPage));
        privateDataManager.storeData(new BookmarkStorage(bookmarks));
    }

    @SuppressWarnings("unchecked")
    private <T extends Bookmark> Collection<T> getBookmarks(Class<T> clazz) throws XmppException {
        List<T> bookmarks = new ArrayList<>();
        BookmarkStorage bookmarkStorage = privateDataManager.getData(BookmarkStorage.class);

        bookmarks.addAll(bookmarkStorage.getBookmarks().stream().filter(bookmark -> bookmark.getClass() == clazz).map(bookmark -> (T) bookmark).collect(Collectors.toList()));
        bookmarks.sort(null);
        return bookmarks;
    }
}
