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

package rocks.xmpp.extensions.privatedata;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.privatedata.model.PrivateData;
import rocks.xmpp.extensions.privatedata.rosternotes.model.Annotation;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.Collections;

/**
 * @author Christian Schudt
 */
public class PrivateDataTest extends XmlTest {
    protected PrivateDataTest() throws JAXBException, XMLStreamException {
        super(PrivateData.class, Annotation.class);
    }

    @Test
    public void marshalPrivateData() throws JAXBException, XMLStreamException {
        PrivateData privateData = new PrivateData(new Annotation(Collections.<Annotation.Note>emptyList()));
        String xml = marshal(privateData);
        Assert.assertEquals("<query xmlns=\"jabber:iq:private\"><storage xmlns=\"storage:rosternotes\"></storage></query>", xml);
    }
}