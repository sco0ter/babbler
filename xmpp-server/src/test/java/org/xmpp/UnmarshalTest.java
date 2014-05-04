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

package org.xmpp;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.*;
import java.io.*;

/**
 * @author Christian Schudt
 */
public abstract class UnmarshalTest {

    private static final String START_STREAM = "<?xml version='1.0' encoding='UTF-8'?><stream:stream xmlns:stream=\"http://etherx.jabber.org/streams\" xmlns=\"jabber:server\" from=\"localhost\" id=\"55aa4529\" xml:lang=\"en\" version=\"1.0\">";

    private static final String END_STREAM = "</stream:stream>";

    private Unmarshaller unmarshaller;

    private Marshaller marshaller;

    @Deprecated
    protected UnmarshalTest() {

    }

    protected UnmarshalTest(Class<?>... context) throws JAXBException, XMLStreamException {
        JAXBContext jaxbContext = JAXBContext.newInstance(context);
        unmarshaller = jaxbContext.createUnmarshaller();

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
    }

    protected <T> T unmarshal(String xml, Class<T> type) throws XMLStreamException, JAXBException {
        XMLEventReader xmlEventReader = getStream(xml);
        return unmarshaller.unmarshal(xmlEventReader, type).getValue();
    }

    protected String marshal(Object object) throws XMLStreamException, JAXBException {
        Writer writer = new StringWriter();

        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);

        PrefixFreeCanonicalizationWriter prefixFreeWriter = new PrefixFreeCanonicalizationWriter(xmlStreamWriter);
        marshaller.marshal(object, prefixFreeWriter);
        return writer.toString();
    }

    private static XMLEventReader getStream(String stanza) throws XMLStreamException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        printStream.print(START_STREAM + stanza + END_STREAM);

        InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        XMLEventReader xmlEventReader = XMLInputFactory.newFactory().createXMLEventReader(inputStream);
        xmlEventReader.nextEvent();
        xmlEventReader.nextEvent();
        return xmlEventReader;
    }
}
