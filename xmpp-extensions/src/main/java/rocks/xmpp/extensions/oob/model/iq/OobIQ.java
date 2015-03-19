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

package rocks.xmpp.extensions.oob.model.iq;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;

/**
 * The implementation of the {@code <query/>} element in the {@code jabber:iq:oob} namespace.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0066.html">XEP-0066: Out of Band Data</a>
 */
@XmlRootElement(name = "query")
public final class OobIQ {

    /**
     * jabber:iq:oob
     */
    public static final String NAMESPACE = "jabber:iq:oob";

    @XmlElement(name = "url")
    private final URL url;

    @XmlElement(name = "desc")
    private final String description;

    @XmlAttribute(name = "sid")
    private final String sessionId;

    private OobIQ() {
        this(null, null, null);
    }

    public OobIQ(URL url) {
        this(url, null);
    }

    public OobIQ(URL url, String description) {
        this(url, description, null);
    }

    public OobIQ(URL url, String description, String sessionId) {
        this.url = url;
        this.description = description;
        this.sessionId = sessionId;
    }

    /**
     * Gets the URL.
     *
     * @return The URL.
     */
    public final URL getUrl() {
        return url;
    }

    /**
     * Gets the description.
     *
     * @return The description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Gets the session id. This is used only in conjunction with stream initiation.
     *
     * @return The session id.
     */
    public final String getSessionId() {
        return sessionId;
    }

    @Override
    public final String toString() {
        return url != null ? url.toString() : null;
    }
}
