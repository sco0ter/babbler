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

package rocks.xmpp.extensions.json.model;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;

/**
 * @author Christian Schudt
 */
public class JsonTest extends XmlTest {

    @Test
    public void unmarshalJson() throws XMLStreamException, JAXBException {
        String xml =
                "<json xmlns=\"urn:xmpp:json:0\">{ \"name\": \"romeo\", \"age\": \"421\", \"status\": \"single\" }</json>";
        Json json = unmarshal(xml, Json.class);
        Assert.assertNotNull(json);
        Assert.assertEquals(json.getValue(), "{ \"name\": \"romeo\", \"age\": \"421\", \"status\": \"single\" }");
    }
}
