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
import org.xmpp.BaseTest;
import org.xmpp.MockServer;
import org.xmpp.TestConnection;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class EntityTimeTest extends BaseTest {

    @Test
    public void marshalEntityTimeRequest() throws XMLStreamException, JAXBException, IOException {
        IQ iq = new IQ("time_1", IQ.Type.GET, new EntityTime());
        String xml = marshall(iq);
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
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
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

        String xml = marshall(new EntityTime(timeZone, calendar.getTime()));
        Assert.assertEquals(xml, "<time xmlns=\"urn:xmpp:time\"><tzo>-02:00</tzo><utc>2014-01-07T03:34:03.001Z</utc></time>");
    }

    @Test
    public void testTimezoneAdapter() throws Exception {
        EntityTime.TimeZoneAdapter adapter = new EntityTime.TimeZoneAdapter();
        TimeZone timeZone = TimeZone.getTimeZone("GMT-08:00");
        String str = adapter.marshal(timeZone);
        Assert.assertEquals(str, "-08:00");
    }

    @Test
    public void testEntityTimeManager() throws IOException, TimeoutException, StanzaException {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        new TestConnection(JULIET, mockServer);
        EntityTimeManager entityTimeManager = connection1.getExtensionManager(EntityTimeManager.class);
        EntityTime entityTime = entityTimeManager.getEntityTime(JULIET);
        Assert.assertNotNull(entityTime);
        Assert.assertNotNull(entityTime.getDate());
        Assert.assertNotNull(entityTime.getTimezone());
    }

    @Test
    public void testEntityTimeIfDisabled() throws TimeoutException {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        TestConnection connection2 = new TestConnection(JULIET, mockServer);
        connection2.getExtensionManager(EntityTimeManager.class).setEnabled(false);
        EntityTimeManager entityTimeManager = connection1.getExtensionManager(EntityTimeManager.class);
        try {
            entityTimeManager.getEntityTime(JULIET);
        } catch (StanzaException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestConnection connection1 = new TestConnection();
        EntityTimeManager entityTimeManager = connection1.getExtensionManager(EntityTimeManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(entityTimeManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:time");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
        entityTimeManager.setEnabled(false);
        Assert.assertFalse(entityTimeManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
