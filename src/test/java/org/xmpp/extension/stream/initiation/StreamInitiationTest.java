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

package org.xmpp.extension.stream.initiation;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.Stanza;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class StreamInitiationTest extends BaseTest {

    @Test
    public void unmarshalStreamInitiation() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result' to='sender@jabber.org/resource' id='offer1'>\n" +
                "  <si xmlns='http://jabber.org/protocol/si'>\n" +
                "    <feature xmlns='http://jabber.org/protocol/feature-neg'>\n" +
                "      <x xmlns='jabber:x:data' type='submit'>\n" +
                "        <field var='stream-method'>\n" +
                "          <value>http://jabber.org/protocol/bytestreams</value>\n" +
                "        </field>\n" +
                "      </x>\n" +
                "    </feature>\n" +
                "  </si>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        StreamInitiation streamInitiation = iq.getExtension(StreamInitiation.class);
        Assert.assertNotNull(streamInitiation);
        Assert.assertNotNull(streamInitiation.getFeatureNegotiation());
    }

    @Test
    public void marshalBadProfile() throws JAXBException, XMLStreamException, IOException {
        IQ result = new IQ("1", IQ.Type.ERROR);
        result.setError(new Stanza.Error(new Stanza.Error.BadRequest()));
        result.getError().setExtension(new BadProfile());
        String xml = marshall(result);
        Assert.assertEquals(xml, "<iq id=\"1\" type=\"error\"><error type=\"modify\"><bad-request xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"></bad-request><bad-profile xmlns=\"http://jabber.org/protocol/si\"></bad-profile></error></iq>");

    }

    @Test
    public void marshalNoValidStreams() throws JAXBException, XMLStreamException, IOException {
        IQ result = new IQ("1", IQ.Type.ERROR);
        result.setError(new Stanza.Error(new Stanza.Error.BadRequest()));
        result.getError().setExtension(new NoValidStreams());
        String xml = marshall(result);
        Assert.assertEquals(xml, "<iq id=\"1\" type=\"error\"><error type=\"modify\"><bad-request xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"></bad-request><no-valid-streams xmlns=\"http://jabber.org/protocol/si\"></no-valid-streams></error></iq>");

    }
}
