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

package org.xmpp.extension.receipts;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.XmlTest;
import org.xmpp.stanza.client.Message;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class MessageDeliveryReceiptsTest extends XmlTest {
    protected MessageDeliveryReceiptsTest() throws JAXBException, XMLStreamException {
        super(Message.class, Request.class, Received.class);
    }

    @Test
    public void unmarshalMessageDeliveryRequest() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='northumberland@shakespeare.lit/westminster'\n" +
                "    id='richard2-4.1.247'\n" +
                "    to='kingrichard@royalty.england.lit/throne'>\n" +
                "  <body>My lord, dispatch; read o'er these articles.</body>\n" +
                "  <request xmlns='urn:xmpp:receipts'/>\n" +
                "</message>\n";

        Message message = unmarshal(xml, Message.class);
        Request request = message.getExtension(Request.class);
        Assert.assertNotNull(request);
    }

    @Test
    public void unmarshalMessageDeliveryReceived() throws XMLStreamException, JAXBException {
        String xml = "<message\n" +
                "    from='kingrichard@royalty.england.lit/throne'\n" +
                "    id='bi29sg183b4v'\n" +
                "    to='northumberland@shakespeare.lit/westminster'>\n" +
                "  <received xmlns='urn:xmpp:receipts' id='richard2-4.1.247'/>\n" +
                "</message>\n";

        Message message = unmarshal(xml, Message.class);
        Received received = message.getExtension(Received.class);
        Assert.assertNotNull(received);
        Assert.assertEquals(received.getId(), "richard2-4.1.247");
    }
}
