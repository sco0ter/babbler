/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.bookmarks.pep.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.xmpp.extensions.bookmarks.model.AbstractBookmark;
import rocks.xmpp.extensions.bookmarks.model.ConferenceBookmark;

/**
 * A PEP native conference bookmark element.
 *
 * <p>PEP native conference bookmarks are stored in private, personal pubsub nodes using the Personal Eventing Protocol
 * (PEP).</p>
 *
 * @see <a href="https://xmpp.org/extensions/xep-0402.html">XEP-0402: PEP Native Bookmarks</a>
 */
@XmlRootElement(name = "conference")
public final class PepNativeBookmark extends AbstractBookmark implements ConferenceBookmark {

    /**
     * {@value}
     */
    public static final String NAMESPACE = "urn:xmpp:bookmarks:1";

    @XmlAttribute
    private final Boolean autojoin;

    @XmlElement
    private final String nick;

    @XmlElement
    private final String password;

    public PepNativeBookmark() {
        super(null);
        this.nick = null;
        this.password = null;
        this.autojoin = null;
    }

    public PepNativeBookmark(String name, String nick, String password, boolean autojoin) {
        super(name);
        this.nick = nick;
        this.password = password;
        this.autojoin = autojoin;
    }

    @Override
    public final boolean isAutojoin() {
        return autojoin != null && autojoin;
    }

    @Override
    public final String getNick() {
        return nick;
    }

    @Override
    public final String getPassword() {
        return password;
    }

    @Override
    public final String toString() {
        return getName() + ": [nick: " + nick + "; autojoin: " + isAutojoin() + ']';
    }
}
