/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.extensions.spoiler.model;

import rocks.xmpp.util.adapters.LocaleAdapter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Locale;

/**
 * The implementation of the {@code <spoiler/>} element in the {@code urn:xmpp:spoiler:0} namespace.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0382.html">XEP-0382: Spoiler Messages</a>
 * @see <a href="https://xmpp.org/extensions/xep-0382.html#schema">XML Schema</a>
 */
@XmlRootElement
public final class Spoiler {

    /**
     * urn:xmpp:spoiler:0
     */
    public static final String NAMESPACE = "urn:xmpp:spoiler:0";

    @XmlJavaTypeAdapter(LocaleAdapter.class)
    @XmlAttribute(namespace = XMLConstants.XML_NS_URI)
    private final Locale lang;

    @XmlValue
    private final String text;

    private Spoiler() {
        this.lang = null;
        this.text = null;
    }

    public Spoiler(String text) {
        this(text, null);
    }

    public Spoiler(String text, Locale lang) {
        this.text = text;
        this.lang = lang;
    }

    /**
     * Gets the language.
     *
     * @return The language.
     */
    public final Locale getLanguage() {
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
