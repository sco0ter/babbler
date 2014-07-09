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


import javax.xml.bind.annotation.XmlAttribute;
import java.net.URL;

/**
 * A web page bookmark.
 *
 * @author Christian Schudt
 */
public final class WebPageBookmark extends Bookmark {
    @XmlAttribute(name = "url")
    private URL url;

    private WebPageBookmark() {
        super(null);
    }

    /**
     * Creates a web page bookmark.
     *
     * @param name The bookmark name.
     * @param url  The URL of the web page.
     */
    public WebPageBookmark(String name, URL url) {
        super(name);
        this.url = url;
    }

    /**
     * Gets the URL of the web page.
     *
     * @return The URL.
     */
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WebPageBookmark)) {
            return false;
        }
        WebPageBookmark other = (WebPageBookmark) o;

        return (url == null ? other.url == null : url.equals(other.url));

    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }
}
