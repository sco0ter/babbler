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

package rocks.xmpp.extensions.shim;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.extensions.shim.model.Header;
import rocks.xmpp.extensions.shim.model.Headers;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @author Christian Schudt
 */
public class HeadersTest extends XmlTest {

    protected HeadersTest() throws JAXBException, XMLStreamException {
        super(ClientMessage.class, Headers.class);
    }

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

        String xmlStart = marshal(new Headers(Header.start(OffsetDateTime.of(2015, 3, 22, 1, 2, 3, 0, ZoneOffset.of("Z")))));
        Assert.assertEquals(xmlStart, ("<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Start\">2015-03-22T01:02:03Z</header></headers>"));

        String xmlStop = marshal(new Headers(Header.stop(OffsetDateTime.now())));
        Assert.assertTrue(xmlStop.startsWith("<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Stop\">"));

        String xmlPeriod = marshal(Headers.timePeriod(OffsetDateTime.now(), OffsetDateTime.now()));
        Assert.assertTrue(xmlPeriod.startsWith("<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Start\">"));
        Assert.assertTrue(xmlPeriod.contains("<header name=\"Stop\">"));
    }
}
