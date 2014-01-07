package org.xmpp.extension.time;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "time")
@XmlAccessorType(XmlAccessType.FIELD)
public final class EntityTime {

    @XmlElement(name = "utc")
    private Date date;

    @XmlJavaTypeAdapter(TimeZoneAdapter.class)
    @XmlElement(name = "tzo")
    private TimeZone timeZone;

    public Date getDate() {
        return date;
    }

    public TimeZone getTimezone() {
        return timeZone;
    }

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
