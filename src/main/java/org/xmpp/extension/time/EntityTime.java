package org.xmpp.extension.time;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
@XmlAccessorType(XmlAccessType.FIELD)
public final class EntityTime {

    /**
     * Creates a empty entity time element for requesting entity time.
     */
    public EntityTime() {
    }

    EntityTime(TimeZone timeZone, Date date) {
        this.timeZone = timeZone;
        this.date = date;
    }

    @XmlJavaTypeAdapter(TimeZoneAdapter.class)
    @XmlElement(name = "tzo")
    private TimeZone timeZone;

    @XmlJavaTypeAdapter(UTCDateAdapter.class)
    @XmlElement(name = "utc")
    private Date date;

    /**
     * Gets the entity's time zone.
     *
     * @return The time zone.
     */
    public TimeZone getTimezone() {
        return timeZone;
    }

    /**
     * Gets the entity's date.
     *
     * @return The date.
     */
    public Date getDate() {
        return date;
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
