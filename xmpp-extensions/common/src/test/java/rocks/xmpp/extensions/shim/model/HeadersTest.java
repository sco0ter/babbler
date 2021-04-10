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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.Message;

/**
 * @author Christian Schudt
 */
public class HeadersTest extends XmlTest {

    @Test
    public void unmarshalHeaders() throws XMLStreamException, JAXBException {
        String xml = "<message from='romeo@montague.net/orchard'\n" +
                "         to='juliet@capulet.com/balcony'>\n" +
                "  <body>Neither, fair saint, if either thee dislike.</body>\n" +
                "  <headers xmlns='http://jabber.org/protocol/shim'>\n" +
                "    <header name='In-Reply-To'>123456789@capulet.com</header>\n" +
                "    <header name='Keywords'>shakespeare,&lt;xmpp/&gt;</header>\n" +
                "  </headers>\n" +
                "</message>";

        Message message = unmarshal(xml, Message.class);
        Headers headers = message.getExtension(Headers.class);
        Assert.assertNotNull(headers);
        Assert.assertEquals(headers.getHeaders().size(), 2);
        Assert.assertEquals(headers.getHeaders().get(0).getName(), "In-Reply-To");
        Assert.assertEquals(headers.getHeaders().get(0).getValue(), "123456789@capulet.com");
        Assert.assertEquals(headers.getHeaders().get(1).getName(), "Keywords");
        Assert.assertEquals(headers.getHeaders().get(1).getValue(), "shakespeare,<xmpp/>");
    }

    @Test
    public void marshalDateTimePeriods() throws XMLStreamException, JAXBException {

        String xmlStart =
                marshal(Headers.of(Header.ofStartDate(OffsetDateTime.of(2015, 3, 22, 1, 2, 3, 0, ZoneOffset.UTC))));
        Assert.assertEquals(xmlStart,
                "<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Start\">2015-03-22T01:02:03Z</header></headers>");

        String xmlStop = marshal(Headers.of(Header.ofStopDate(OffsetDateTime.now(ZoneId.systemDefault()))));
        Assert.assertTrue(
                xmlStop.startsWith("<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Stop\">"));

        String xmlPeriod = marshal(Headers
                .ofTimePeriod(OffsetDateTime.now(ZoneId.systemDefault()), OffsetDateTime.now(ZoneId.systemDefault())));
        Assert.assertTrue(
                xmlPeriod.startsWith("<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Start\">"));
        Assert.assertTrue(xmlPeriod.contains("<header name=\"Stop\">"));
    }

    @Test
    public void marshalMap() throws XMLStreamException, JAXBException {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Header1", "Value1");
        map.put("Header2", "Value2");
        map.put("Header3", "Value3");
        map.put("Header4", "Value4");
        Headers headers = Headers.of(map);
        Assert.assertEquals(marshal(headers),
                "<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Header1\">Value1</header><header name=\"Header2\">Value2</header><header name=\"Header3\">Value3</header><header name=\"Header4\">Value4</header></headers>");
        Assert.assertEquals(marshal(headers.withHeader("Header2", "newValue")),
                "<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Header1\">Value1</header><header name=\"Header3\">Value3</header><header name=\"Header4\">Value4</header><header name=\"Header2\">newValue</header></headers>");
        Assert.assertEquals(marshal(headers.withHeader("Header5", "Value5")),
                "<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Header1\">Value1</header><header name=\"Header2\">Value2</header><header name=\"Header3\">Value3</header><header name=\"Header4\">Value4</header><header name=\"Header5\">Value5</header></headers>");
        Assert.assertEquals(marshal(headers.withoutHeader("Header3")),
                "<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Header1\">Value1</header><header name=\"Header2\">Value2</header><header name=\"Header4\">Value4</header></headers>");
    }

    @Test
    public void testHeaders() {
        Header header = Header.ofClassification("class");
        Assert.assertEquals(header.getName(), "Classification");
        Assert.assertEquals(header.getValue(), "class");

        header = Header.ofCreated(OffsetDateTime.of(2020, 2, 2, 0, 0, 0, 0, ZoneOffset.UTC));
        Assert.assertEquals(header.getName(), "Created");
        Assert.assertEquals(header.getValue(), "2020-02-02T00:00:00Z");

        header = Header.ofStartDate(OffsetDateTime.of(2020, 2, 2, 0, 0, 0, 0, ZoneOffset.UTC));
        Assert.assertEquals(header.getName(), "Start");
        Assert.assertEquals(header.getValue(), "2020-02-02T00:00:00Z");

        header = Header.ofStopDate(OffsetDateTime.of(2020, 2, 2, 0, 0, 0, 0, ZoneOffset.ofHours(2)));
        Assert.assertEquals(header.getName(), "Stop");
        Assert.assertEquals(header.getValue(), "2020-02-02T00:00:00+02:00");

        header = Header.ofDistribute(true);
        Assert.assertEquals(header.getName(), "Distribute");
        Assert.assertEquals(header.getValue(), "true");

        header = Header.ofStore(true);
        Assert.assertEquals(header.getName(), "Store");
        Assert.assertEquals(header.getValue(), "true");

        header = Header.ofTimeToLive(Duration.ofMinutes(1));
        Assert.assertEquals(header.getName(), "TTL");
        Assert.assertEquals(header.getValue(), "60");

        header = Header.ofUrgency("high");
        Assert.assertEquals(header.getName(), "Urgency");
        Assert.assertEquals(header.getValue(), "high");
        Assert.assertEquals(Header.ofUrgency("medium").getValue(), "medium");
        Assert.assertEquals(Header.ofUrgency("low").getValue(), "low");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidUrgency() {
        Assert.assertEquals(Header.ofUrgency("test").getValue(), "high");
    }
}
