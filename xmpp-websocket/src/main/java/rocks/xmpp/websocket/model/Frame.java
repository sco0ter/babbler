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

package rocks.xmpp.websocket.model;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.util.adapters.LocaleAdapter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Locale;

/**
 * @author Christian Schudt
 */
@XmlTransient
abstract class Frame implements StreamElement {

    @XmlAttribute
    private final String version;

    @XmlAttribute
    private final Jid to;

    @XmlAttribute
    private final Jid from;

    @XmlAttribute
    private final String id;

    @XmlAttribute(namespace = XMLConstants.XML_NS_URI)
    @XmlJavaTypeAdapter(LocaleAdapter.class)
    private final Locale lang;

    Frame(Jid to, Jid from, String id, Locale language, String version) {
        this.to = to;
        this.from = from;
        this.id = id;
        this.lang = language;
        this.version = version;
    }

    public final Jid getTo() {
        return to;
    }

    public final Jid getFrom() {
        return from;
    }

    public final String getVersion() {
        return version;
    }

    public final String getId() {
        return id;
    }

    public final Locale getLanguage() {
        return lang;
    }
}
