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

package rocks.xmpp.extensions.shim.model;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * A header element which hold stanza header information or internet metadata.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0131.html">XEP-0131: Stanza Headers and Internet Metadata</a>
 * @see <a href="https://xmpp.org/extensions/xep-0131.html#schema">XML Schema</a>
 * @see Headers
 */
public final class Header {

    @XmlAttribute
    private final String name;

    @XmlValue
    private final String value;

    private Header() {
        this(null, null);
    }

    private Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Creates a header.
     *
     * @param name  The name of the header. See <a href="https://xmpp.org/extensions/xep-0131.html#registrar-shim">9.3 SHIM Headers Registry</a> for registered headers.
     * @param value The header value.
     * @return The header.
     * @see <a href="https://xmpp.org/extensions/xep-0131.html#registrar-shim">9.3 SHIM Headers Registry</a>
     */
    public static Header of(String name, String value) {
        return new Header(Objects.requireNonNull(name), value);
    }

    /**
     * Creates a 'Classification' header.
     * <p>
     * The Classification header enables a sender or other entity to classify a stanza according to some classification scheme.
     *
     * @param value The value.
     * @return The 'Classification' header.
     * @see <a href="https://xmpp.org/extensions/xep-0131.html#headers-classification">5.1 Classification</a>
     */
    public static Header ofClassification(String value) {
        return of("Classification", value);
    }

    /**
     * Creates a header which specifies the date and time when a stanza was created by the originating entity.
     *
     * @param dateTime The date time.
     * @return The 'Created' header.
     * @see <a href="https://xmpp.org/extensions/xep-0131.html#headers-created">5.2 Created</a>
     */
    public static Header ofCreated(OffsetDateTime dateTime) {
        return of("Created", dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    /**
     * The Distribute header enables a sender to specify whether the stanza may be further distributed by the recipient to other entities on the network. The allowable values for this header are "true" and "false". If the sender specifies a value of "false", the recipient MUST NOT further distribute the stanza or any information contained therein; if the sender specifies a value of "true", the recipient MAY further distribute the stanza or any information contained therein; if the value is anything other than "true" or "false" and the recipient does not understand the value, the recipient MUST assume the default value of "false".
     *
     * @param distribute If the stanza may be further distributed by the recipient.
     * @return The header.
     * @see <a href="https://xmpp.org/extensions/xep-0131.html#headers-distribute">5.3 Distribute</a>
     */
    public static Header ofDistribute(boolean distribute) {
        return of("Distribute", Boolean.toString(distribute));
    }

    /**
     * The Store header enables a sender to specify whether the stanza may be stored or archived by the recipient.
     *
     * @param store If the stanza may be stored by the recipient.
     * @return The header.
     * @see <a href="https://xmpp.org/extensions/xep-0131.html#headers-store">5.4 Store</a>
     */
    public static Header ofStore(boolean store) {
        return of("Store", Boolean.toString(store));
    }

    /**
     * Specifies that the information contained in a stanza is valid only for a limited period of time.
     *
     * @param timeToLive The time to live.
     * @return The header.
     * @see <a href="https://xmpp.org/extensions/xep-0131.html#headers-ttl">5.5 TTL</a>
     */
    public static Header ofTimeToLive(Duration timeToLive) {
        return of("TTL", String.valueOf(timeToLive.getSeconds()));
    }

    /**
     * Specifies that the information contained in a stanza is more or less time-sensitive.
     *
     * @param urgency The urgency, must be "high", "medium" or "low".
     * @return The header.
     * @see <a href="https://xmpp.org/extensions/xep-0131.html#headers-urgency">5.6 Urgency</a>
     */
    public static Header ofUrgency(String urgency) {
        if (!urgency.equals("high") && !urgency.equals("medium") && !urgency.equals("low")) {
            throw new IllegalArgumentException("urgency must be 'high', 'medium' or 'value', but is '" + urgency + "'");
        }
        return of("Urgency", urgency);
    }

    /**
     * Creates a header with a start date.
     *
     * @param dateTime The start date.
     * @return The header.
     * @see <a href="https://xmpp.org/extensions/xep-0149.html">XEP-0149: Time Periods</a>
     */
    public static Header ofStartDate(OffsetDateTime dateTime) {
        return of("Start", dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    /**
     * Creates a header with a stop date.
     *
     * @param dateTime The stop date.
     * @return The header.
     * @see <a href="https://xmpp.org/extensions/xep-0149.html">XEP-0149: Time Periods</a>
     */
    public static Header ofStopDate(OffsetDateTime dateTime) {
        return of("Stop", dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    /**
     * Gets the name of the header.
     *
     * @return The header.
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the value of the header.
     *
     * @return The header.
     */
    public final String getValue() {
        return value;
    }

    @Override
    public final String toString() {
        return name + ": " + value;
    }
}
