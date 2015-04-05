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

package rocks.xmpp.extensions.time.model;

import rocks.xmpp.core.util.adapters.InstantAdapter;
import rocks.xmpp.core.util.adapters.ZoneOffsetAdapter;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * The implementation of the {@code <time/>} element in the {@code urn:xmpp:time} namespace.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0202.html">XEP-0202: Entity Time</a>
 * @see <a href="http://xmpp.org/extensions/xep-0202.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "time")
public final class EntityTime {

    /**
     * urn:xmpp:time
     */
    public static final String NAMESPACE = "urn:xmpp:time";

    @XmlJavaTypeAdapter(ZoneOffsetAdapter.class)
    private final ZoneOffset tzo;

    @XmlJavaTypeAdapter(InstantAdapter.class)
    private final Instant utc;

    /**
     * Creates a empty entity time element for requesting entity time.
     */
    public EntityTime() {
        this.tzo = null;
        this.utc = null;
    }

    @Deprecated
    public EntityTime(TimeZone timeZone, Date date) {
        int seconds = Math.abs(timeZone.getRawOffset()) / 1000;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        this.tzo = ZoneOffset.of((timeZone.getRawOffset() < 0 ? "-" : "+") + String.format("%02d:%02d", hours, minutes));
        this.utc = Objects.requireNonNull(date.toInstant());
    }

    public EntityTime(OffsetDateTime dateTime) {
        this.tzo = Objects.requireNonNull(dateTime).getOffset();
        this.utc = dateTime.toInstant();
    }

    /**
     * Converts a string representation of a date to {@link java.util.Date}.
     *
     * @param v The string value of the date.
     * @return The date in UTC.
     * @throws java.lang.IllegalArgumentException If the string value does not conform to XEP-0082.
     * @see <a href="http://xmpp.org/extensions/xep-0082.html">XEP-0082: XMPP Date and Time Profiles</a>
     * @deprecated Use {@link java.time.Instant#parse(CharSequence)}}
     */
    @Deprecated
    public static Date toUtcDate(String v) {
        Calendar calendar = DatatypeConverter.parseDateTime(v);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        return calendar.getTime();
    }

    /**
     * Converts a date to its UTC string representation.
     *
     * @param date The date.
     * @return The date in UTC as string.
     * @see <a href="http://xmpp.org/extensions/xep-0082.html">XEP-0082: XMPP Date and Time Profiles</a>
     * @deprecated Use {@link java.time.Instant#toString()}
     */
    @Deprecated
    public static String toUtcString(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        return DatatypeConverter.printDateTime(calendar);
    }

    /**
     * Gets the entity's time zone.
     *
     * @return The time zone.
     * @deprecated Use {@link #getDateTime()}
     */
    @Deprecated
    public final TimeZone getTimezone() {
        return TimeZone.getTimeZone(tzo);
    }

    /**
     * Gets the entity's date.
     *
     * @return The date.
     */
    @Deprecated
    public final Date getDate() {
        return utc != null ? Date.from(utc) : null;
    }

    /**
     * Gets the entity's date.
     *
     * @return The date.
     */
    public final OffsetDateTime getDateTime() {
        return utc != null ? OffsetDateTime.ofInstant(utc, tzo != null ? tzo : ZoneId.of("Z")) : null;
    }

    @Override
    public final String toString() {
        OffsetDateTime dateTime = getDateTime();
        if (dateTime != null) {
            return dateTime.toString();
        }
        return super.toString();
    }
}
