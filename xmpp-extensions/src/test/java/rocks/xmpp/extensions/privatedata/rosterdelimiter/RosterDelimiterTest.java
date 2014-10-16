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

package rocks.xmpp.extensions.privatedata.rosterdelimiter;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.privatedata.model.PrivateData;
import rocks.xmpp.extensions.privatedata.rosterdelimiter.model.RosterDelimiter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class RosterDelimiterTest extends XmlTest {

    protected RosterDelimiterTest() throws JAXBException, XMLStreamException {
        super(IQ.class, PrivateData.class, RosterDelimiter.class);
    }

    @Test
    public void unmarshalAnnotations() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    id='1'\n" +
                "    from='bill@shakespeare.lit/Globe'\n" +
                "    to='bill@shakespeare.lit/Globe'>\n" +
                "  <query xmlns='jabber:iq:private'>\n" +
                "    <roster xmlns='roster:delimiter'>::</roster>\n" +
                "  </query>\n" +
                "</iq>\n";

        IQ iq = unmarshal(xml, IQ.class);
        PrivateData privateData = iq.getExtension(PrivateData.class);
        Assert.assertNotNull(privateData);
        Assert.assertTrue(privateData.getData() instanceof RosterDelimiter);
        RosterDelimiter rosterDelimiter = (RosterDelimiter) privateData.getData();
        Assert.assertEquals(rosterDelimiter.getRosterDelimiter(), "::");
    }

    @Test
    public void marshalRosterDelimiterQuery() throws JAXBException, XMLStreamException {
        IQ iq = new IQ("1", IQ.Type.GET, new PrivateData(new RosterDelimiter()));
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq id=\"1\" type=\"get\"><query xmlns=\"jabber:iq:private\"><roster xmlns=\"roster:delimiter\"></roster></query></iq>");
    }
}
