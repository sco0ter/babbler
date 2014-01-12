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

package org.xmpp.extension.time;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * The implementation of the {@code <time/>} element for requesting and retrieving entity time.
 *
 * @author Christian Schudt
 * @see EntityTimeManager
 */
@XmlRootElement(name = "time")
public final class EntityTime {

    /**
     * Creates a empty entity time element for requesting entity time.
     */
    public EntityTime() {
    }

    EntityTime(TimeZone timeZone, Date date) {
        this.tzo = timeZone;
        this.utc = date;
    }

    @XmlJavaTypeAdapter(TimeZoneAdapter.class)
    @XmlElement(name = "tzo")
    private TimeZone tzo;

    @XmlJavaTypeAdapter(UTCDateAdapter.class)
    @XmlElement(name = "utc")
    private Date utc;

    /**
     * Gets the entity's time zone.
     *
     * @return The time zone.
     */
    public TimeZone getTimezone() {
        return tzo;
    }

    /**
     * Gets the entity's date.
     *
     * @return The date.
     */
    public Date getDate() {
        return utc;
    }

    /**
     * Converts a date to its UTC representation.
     */
    private static class UTCDateAdapter extends XmlAdapter<String, Date> {

        @Override
        public Date unmarshal(String v) throws Exception {
            Calendar calendar = DatatypeConverter.parseDateTime(v);
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            return calendar.getTime();
        }

        @Override
        public String marshal(Date v) throws Exception {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(v);
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            return DatatypeConverter.printDateTime(calendar);
        }
    }

    /**
     * Converts a time zone to a string representation according to <a href="http://xmpp.org/extensions/xep-0082.html">XEP-0082: XMPP Date and Time Profiles</a>
     */
    static class TimeZoneAdapter extends XmlAdapter<String, TimeZone> {

        @Override
        public TimeZone unmarshal(String v) throws Exception {
            return TimeZone.getTimeZone("GMT" + v);
        }

        @Override
        public String marshal(TimeZone v) throws Exception {
            int seconds = Math.abs(v.getRawOffset()) / 1000;
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return (v.getRawOffset() < 0 ? "-" : "+") + String.format("%02d:%02d", hours, minutes);
        }
    }
}
