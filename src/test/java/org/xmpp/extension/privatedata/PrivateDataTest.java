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

package org.xmpp.extension.privatedata;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.*;
import org.xmpp.extension.privatedata.annotations.Annotation;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class PrivateDataTest extends BaseTest {

    @Test
    public void marshalPrivateData() throws JAXBException, XMLStreamException, IOException {
        PrivateData privateData = new PrivateData(new Annotation(null));
        String xml = marshall(privateData);
        Assert.assertEquals("<query xmlns=\"jabber:iq:private\"><storage xmlns=\"storage:rosternotes\"></storage></query>", xml);
    }

    @Test
    public void testManager() throws XmppException, IOException, TimeoutException {
        MockServer mockServer = new MockServer();

        Connection connection = new TestConnection(null, mockServer);
        connection.connect();

        PrivateDataManager privateDataManager = connection.getExtensionManager(PrivateDataManager.class);
        privateDataManager.storeData(new Annotation(null));

        List<Annotation> annotationList = privateDataManager.getData(Annotation.class);

    }
}
