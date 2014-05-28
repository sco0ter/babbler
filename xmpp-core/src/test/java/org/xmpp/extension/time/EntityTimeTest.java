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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.XmlTest;
import org.xmpp.stanza.client.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Christian Schudt
 */
public class EntityTimeTest extends XmlTest {

    protected EntityTimeTest() throws JAXBException, XMLStreamException {
        super(IQ.class, EntityTime.class);
    }


    @Test
    public void marshalEntityTimeRequest() throws XMLStreamException, JAXBException {
        IQ iq = new IQ("time_1", IQ.Type.GET, new EntityTime());
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq id=\"time_1\" type=\"get\"><time xmlns=\"urn:xmpp:time\"></time></iq>");
    }

    @Test
    public void unmarshalEntityTimeResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='juliet@capulet.com/balcony'\n" +
                "    to='romeo@montague.net/orchard'\n" +
                "    id='time_1'>\n" +
                "  <time xmlns='urn:xmpp:time'>\n" +
                "    <tzo>-06:00</tzo>\n" +
                "    <utc>2006-12-19T17:58:35Z</utc>\n" +
                "  </time>\n" +
                "</iq>";

        IQ iq = unmarshal(xml, IQ.class);
        EntityTime entityTime = iq.getExtension(EntityTime.class);
        Assert.assertNotNull(entityTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(entityTime.getTimezone());
        calendar.setTime(entityTime.getDate());
        Assert.assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 11);
    }

    @Test
    public void marshalEntityTimeResponse() throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-2:00");
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.YEAR, 2014);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 34);
        calendar.set(Calendar.SECOND, 3);
        calendar.set(Calendar.MILLISECOND, 1);

        String xml = marshal(new EntityTime(timeZone, calendar.getTime()));
        Assert.assertEquals(xml, "<time xmlns=\"urn:xmpp:time\"><tzo>-02:00</tzo><utc>2014-01-07T03:34:03.001Z</utc></time>");
    }

    @Test
    public void testTimezoneAdapter() throws Exception {
        TimeZoneAdapter adapter = new TimeZoneAdapter();
        TimeZone timeZone = TimeZone.getTimeZone("GMT-08:00");
        String str = adapter.marshal(timeZone);
        Assert.assertEquals(str, "-08:00");

        TimeZone timeZoneUtc = adapter.unmarshal("Z");
        Assert.assertEquals(timeZoneUtc, TimeZone.getTimeZone("GMT"));
    }
}
