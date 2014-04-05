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

package org.xmpp.extension.sm;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.stanza.errors.UnexpectedRequest;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class StreamManagementTest extends BaseTest {

    @Test
    public void unmarshalFeature() throws XMLStreamException, JAXBException {
        String xml = "<sm xmlns='urn:xmpp:sm:3'/>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        StreamManagement feature = (StreamManagement) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(feature);
    }

    @Test
    public void marshalEnable() throws JAXBException, XMLStreamException, IOException {
        Enable enable = new Enable();
        String xml = marshall(enable);
        Assert.assertEquals("<enable xmlns=\"urn:xmpp:sm:3\"></enable>", xml);
    }

    @Test
    public void marshalEnableWithResume() throws JAXBException, XMLStreamException, IOException {
        Enable enable = new Enable(true, 23);
        String xml = marshall(enable);
        Assert.assertEquals("<enable xmlns=\"urn:xmpp:sm:3\" resume=\"true\" max=\"23\"></enable>", xml);
    }

    @Test
    public void unmarshalEnabled() throws XMLStreamException, JAXBException {
        String xml = "<enabled xmlns='urn:xmpp:sm:3' id='some-long-sm-id' resume='true' max='23' location='domain'/>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Enabled enabled = (Enabled) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(enabled);
        Assert.assertEquals(enabled.getId(), "some-long-sm-id");
        Assert.assertTrue(enabled.isResume());
        Assert.assertEquals(enabled.getLocation(), "domain");
        Assert.assertEquals(enabled.getMax(), (Integer) 23);
    }

    @Test
    public void unmarshalFailed() throws XMLStreamException, JAXBException {
        String xml = "<failed xmlns='urn:xmpp:sm:3'>\n" +
                "     <unexpected-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "   </failed>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Failed failed = (Failed) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(failed);
        Assert.assertTrue(failed.getError() instanceof UnexpectedRequest);
    }

    @Test
    public void marshalAnswer() throws JAXBException, XMLStreamException, IOException {
        Answer answer = new Answer(1);
        String xml = marshall(answer);
        Assert.assertEquals("<a xmlns=\"urn:xmpp:sm:3\" h=\"1\"></a>", xml);
    }

    @Test
    public void marshalRequest() throws JAXBException, XMLStreamException, IOException {
        Request request = new Request();
        String xml = marshall(request);
        Assert.assertEquals("<r xmlns=\"urn:xmpp:sm:3\"></r>", xml);
    }

    @Test
    public void unmarshalResume() throws XMLStreamException, JAXBException {
        String xml = "<resume xmlns='urn:xmpp:sm:3' \n" +
                "            h='2'\n" +
                "            previd='some-long-sm-id'/>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Resume resume = (Resume) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(resume);
        Assert.assertEquals(resume.getPreviousId(), "some-long-sm-id");
        Assert.assertEquals(resume.getLastHandledStanza(), 2);
    }

    @Test
    public void unmarshalResumed() throws XMLStreamException, JAXBException {
        String xml = "<resumed xmlns='urn:xmpp:sm:3' \n" +
                "            h='2'\n" +
                "            previd='some-long-sm-id'/>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Resumed resumed = (Resumed) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(resumed);
        Assert.assertEquals(resumed.getPreviousId(), "some-long-sm-id");
        Assert.assertEquals(resumed.getLastHandledStanza(), 2);
    }
}
