/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.extensions.oob.model.iq;

import java.net.URI;
import java.util.Objects;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <query/>} element in the {@code jabber:iq:oob} namespace.
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0066.html">XEP-0066: Out of Band Data</a>
 */
@XmlRootElement(name = "query")
public final class OobIQ {

    /**
     * jabber:iq:oob
     */
    public static final String NAMESPACE = "jabber:iq:oob";

    private final URI url;

    private final String desc;

    @XmlAttribute
    private final String sid;

    private OobIQ() {
        this.url = null;
        this.desc = null;
        this.sid = null;
    }

    public OobIQ(URI uri) {
        this(uri, null);
    }

    public OobIQ(URI uri, String description) {
        this(uri, description, null);
    }

    public OobIQ(URI uri, String description, String sessionId) {
        this.url = Objects.requireNonNull(uri);
        this.desc = description;
        this.sid = sessionId;
    }

    /**
     * Gets the URI.
     *
     * @return The URI.
     */
    public final URI getUri() {
        return url;
    }

    /**
     * Gets the description.
     *
     * @return The description.
     */
    public final String getDescription() {
        return desc;
    }

    /**
     * Gets the session id. This is used only in conjunction with stream initiation.
     *
     * @return The session id.
     */
    public final String getSessionId() {
        return sid;
    }

    @Override
    public final String toString() {
        return String.valueOf(url);
    }
}
