/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Christian Schudt
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

import org.testng.annotations.BeforeClass;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class BaseTest {

    protected Marshaller marshaller;

    protected Unmarshaller unmarshaller;

    protected TestConnection connection;

    public BaseTest() {
        connection = new TestConnection();
    }

    @BeforeClass
    public void setupMarshaller() throws JAXBException, XMLStreamException {
        marshaller = connection.getMarhaller();
        unmarshaller = connection.getUnmarshaller();
    }

    protected String marshall(Object object) throws XMLStreamException, JAXBException, IOException {
        TestConnection connection = new TestConnection();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = connection.createXMLStreamWriter(outputStream);
        marshaller.marshal(object, xmlStreamWriter);
        xmlStreamWriter.close();
        outputStream.close();
        return outputStream.toString();
    }
}
