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

package rocks.xmpp.extensions.oob.model.iq;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class OobIQ {

    @XmlElement(name = "url")
    private URL url;

    @XmlElement(name = "desc")
    private String description;

    @XmlAttribute(name = "sid")
    private String sessionId;

    private OobIQ() {
    }

    public OobIQ(URL url) {
        this(url, null);
    }

    public OobIQ(URL url, String description) {
        this.url = url;
        this.description = description;
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
    public URL getUrl() {
        return url;
    }

    /**
     * Gets the description.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the session id. This is used only in conjunction with stream initiation.
     *
     * @return The session id.
     */
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return url != null ? url.toString() : null;
    }
}
