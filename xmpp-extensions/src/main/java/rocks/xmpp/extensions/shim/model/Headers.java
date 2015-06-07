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

package rocks.xmpp.extensions.shim.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <headers/>} element in the {@code http://jabber.org/protocol/shim} namespace.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0131.html">XEP-0131: Stanza Headers and Internet Metadata</a>
 * @see <a href="http://xmpp.org/extensions/xep-0131.html#schema">XML Schema</a>
 * @see Header
 */
@XmlRootElement
public final class Headers {

    /**
     * http://jabber.org/protocol/shim
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/shim";

    private final List<Header> header = new ArrayList<>();

    private Headers() {
    }

    public Headers(Header... headers) {
        this.header.addAll(Arrays.asList(headers));
    }

    /**
     * Creates a headers element with a time period.
     *
     * @param start The start date.
     * @param stop  The stop date.
     * @return The header.
     * @see <a href="http://xmpp.org/extensions/xep-0149.html">XEP-0149: Time Periods</a>
     */
    public static Headers timePeriod(OffsetDateTime start, OffsetDateTime stop) {
        // If both a start time and a stop time are specified, the stop time MUST be later than the start time.
        if (start.isAfter(stop)) {
            throw new IllegalArgumentException("start date must not be after the start date.");
        }
        return new Headers(Header.start(start), Header.stop(stop));
    }

    /**
     * Gets the headers.
     *
     * @return The headers.
     */
    public final List<Header> getHeaders() {
        return Collections.unmodifiableList(header);
    }

    @Override
    public final String toString() {
        return header.toString();
    }
}
