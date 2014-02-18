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

package org.xmpp.extension.compress;

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
public class CompressionTest extends BaseTest {

    @Test
    public void unmarshalCompression() throws XMLStreamException, JAXBException {
        String xml = "<compression xmlns='http://jabber.org/features/compress'>\n" +
                "    <method>zlib</method>\n" +
                "    <method>lzw</method>\n" +
                "  </compression>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Compression compression = (Compression) unmarshaller.unmarshal(xmlEventReader);

        Assert.assertNotNull(compression);
        Assert.assertEquals(compression.getMethods().size(), 2);
        Assert.assertEquals(compression.getMethods().get(0), Method.ZLIB);
        //Assert.assertEquals(compression.getMethods().get(1).getName(), "lzw");
    }

    @Test
    public void marshalCompress() throws JAXBException, XMLStreamException, IOException {
        Compress compress = new Compress(Method.ZLIB);
        String xml = marshall(compress);
        Assert.assertEquals(xml, "<compress xmlns=\"http://jabber.org/protocol/compress\"><method>zlib</method></compress>");
    }

    @Test
    public void unmarshalFailureUnsupportedMethod() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='http://jabber.org/protocol/compress'>\n" +
                "  <unsupported-method/>\n" +
                "</failure>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Failure failure = (Failure) unmarshaller.unmarshal(xmlEventReader);

        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.UnsupportedMethod);
    }

    @Test
    public void unmarshalFailureSetupFailed() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='http://jabber.org/protocol/compress'>\n" +
                "  <setup-failed/>\n" +
                "</failure>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Failure failure = (Failure) unmarshaller.unmarshal(xmlEventReader);

        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.SetupFailed);
    }

    @Test
    public void unmarshalFailureProcessingFailed() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='http://jabber.org/protocol/compress'>\n" +
                "  <processing-failed/>\n" +
                "</failure>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Failure failure = (Failure) unmarshaller.unmarshal(xmlEventReader);

        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.ProcessingFailed);
    }
}
