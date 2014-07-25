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

package org.xmpp.extension.shim;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.XmlTest;
import org.xmpp.stanza.client.Message;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public class HeadersTest extends XmlTest {

    protected HeadersTest() throws JAXBException, XMLStreamException {
        super(Message.class, Headers.class);
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

        String xmlStart = marshal(new Headers(Header.start(new Date())));
        Assert.assertTrue(xmlStart.startsWith("<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Start\">"));

        String xmlStop = marshal(new Headers(Header.stop(new Date())));
        Assert.assertTrue(xmlStop.startsWith("<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Stop\">"));

        String xmlPeriod = marshal(Headers.timePeriod(new Date(), new Date()));
        Assert.assertTrue(xmlPeriod.startsWith("<headers xmlns=\"http://jabber.org/protocol/shim\"><header name=\"Start\">"));
        Assert.assertTrue(xmlPeriod.contains("<header name=\"Stop\">"));
    }
}
