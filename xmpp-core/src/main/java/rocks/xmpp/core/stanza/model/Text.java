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

package rocks.xmpp.core.stanza.model;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * A common "text" element which is used for a message's body or subject, a presence's status or a stanza error's text element.
 *
 * @author Christian Schudt
 * @see AbstractMessage#getBodies()
 * @see AbstractMessage#getSubjects()
 * @see AbstractPresence#getStatuses()
 */
public final class Text {
    @XmlAttribute(namespace = XMLConstants.XML_NS_URI)
    private final String lang;

    @XmlValue
    private final String text;

    /**
     * Private default constructor, needed for unmarshalling.
     */
    @SuppressWarnings("unused")
    Text() {
        this.text = null;
        this.lang = null;
    }

    /**
     * Constructs a default status.
     *
     * @param text The text.
     */
    public Text(String text) {
        this(text, null);
    }

    /**
     * Constructs a status with a language attribute.
     *
     * @param text     The text
     * @param language The language.
     */
    public Text(String text, String language) {
        this.text = text;
        this.lang = language;
    }

    /**
     * Gets the language.
     *
     * @return The language.
     */
    public final String getLanguage() {
        return lang;
    }

    /**
     * Gets the text.
     *
     * @return The text.
     */
    public final String getText() {
        return text;
    }

    @Override
    public final String toString() {
        return text;
    }
}