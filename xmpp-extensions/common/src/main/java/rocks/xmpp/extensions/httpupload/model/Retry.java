/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.extensions.httpupload.model;

import rocks.xmpp.util.adapters.InstantAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.Objects;

/**
 * Indicates a temporary error such as exceeding a personal quota and advises the requesting entity to retry the request later.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0363.html#errors">5. Error Conditions</a>
 */
@XmlRootElement(name = "retry")
public final class Retry {

    @XmlJavaTypeAdapter(InstantAdapter.class)
    @XmlAttribute
    private final Instant stamp;

    private Retry() {
        this.stamp = null;
    }

    public Retry(final Instant stamp) {
        this.stamp = Objects.requireNonNull(stamp, "stamp is required");
    }

    /**
     * Gets the time at which the requesting entity may try again.
     *
     * @return The time.
     */
    public final Instant getStamp() {
        return stamp;
    }

    @Override
    public final String toString() {
        return "Retry at: " + stamp.toString();
    }
}
