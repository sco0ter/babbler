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

package org.xmpp.tls;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class TlsTest extends BaseTest {

    @Test
    public void unmarshalStartTls() throws XMLStreamException, JAXBException {
        String xml = "<starttls xmlns='urn:ietf:params:xml:ns:xmpp-tls'/>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        StartTls startTls = (StartTls) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(startTls);
    }

    @Test
    public void unmarshalProceed() throws XMLStreamException, JAXBException {
        String xml = "<proceed xmlns='urn:ietf:params:xml:ns:xmpp-tls'/>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Proceed proceed = (Proceed) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(proceed);
    }

    @Test
    public void unmarshalFailure() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-tls'/>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Failure failure = (Failure) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(failure);
    }

    @Test
    public void marshalStartTls() throws JAXBException, XMLStreamException, IOException {
        String xml = marshall(new StartTls());
        Assert.assertEquals(xml, "<starttls xmlns=\"urn:ietf:params:xml:ns:xmpp-tls\"></starttls>");
    }
}
