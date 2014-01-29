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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Christian Schudt
 */
public class BookmarkStorageTest extends BaseTest {

    @Test
    public void unmarshalBookmarks() throws XMLStreamException, JAXBException, MalformedURLException {
        String xml = "<storage xmlns='storage:bookmarks'>\n" +
                "      <conference name='Council of Oberon' \n" +
                "                  autojoin='true'\n" +
                "                  jid='council@conference.underhill.org'>\n" +
                "        <nick>Puck</nick>\n" +
                "      </conference>\n" +
                "      <url name='Complete Works of Shakespeare'\n" +
                "           url='http://the-tech.mit.edu/Shakespeare/'/>\n" +
                "    </storage>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        BookmarkStorage bookmarkStorage = (BookmarkStorage) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(bookmarkStorage);
        Assert.assertEquals(bookmarkStorage.getBookmarks().size(), 2);
        Conference bookmark = (Conference) bookmarkStorage.getBookmarks().get(0);
        Assert.assertEquals(bookmark.getName(), "Council of Oberon");
        Assert.assertTrue(bookmark.isAutojoin());
        Assert.assertEquals(bookmark.getRoom(), Jid.fromString("council@conference.underhill.org"));
        Assert.assertEquals(bookmark.getNick(), "Puck");

        WebPage bookmark2 = (WebPage) bookmarkStorage.getBookmarks().get(1);
        Assert.assertEquals(bookmark2.getName(), "Complete Works of Shakespeare");
        Assert.assertEquals(bookmark2.getUrl(), new URL("http://the-tech.mit.edu/Shakespeare/"));
    }
}
