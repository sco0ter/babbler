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

package rocks.xmpp.core.stanza;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.ServiceUnavailable;
import rocks.xmpp.core.stanza.model.errors.UndefinedCondition;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class IQTest extends XmlTest {

    protected IQTest() throws JAXBException, XMLStreamException {
        super(IQ.class);
    }

    @Test
    public void unmarshalIQ() throws JAXBException, XMLStreamException {
        String xml = "<iq from='juliet@example.com/balcony'\n" +
                "       id='b4vs9km4'\n" +
                "       to='romeo@example.net' type='error'/>";
        IQ iq = unmarshal(xml, IQ.class);
        Assert.assertEquals(iq.getType(), IQ.Type.ERROR);
        Assert.assertEquals(iq.getId(), "b4vs9km4");
        Assert.assertEquals(iq.getTo().toString(), "romeo@example.net");
        Assert.assertEquals(iq.getFrom().toString(), "juliet@example.com/balcony");
    }

    @Test
    public void unmarshalResultIQ() throws XMLStreamException, JAXBException {
        String xml = "<iq type=\"result\"/>";
        IQ iq = unmarshal(xml, IQ.class);
        Assert.assertEquals(iq.getType(), IQ.Type.RESULT);
    }

    @Test
    public void unmarshalGetIQ() throws XMLStreamException, JAXBException {
        String xml = "<iq type=\"get\"/>";
        IQ iq = unmarshal(xml, IQ.class);
        Assert.assertEquals(iq.getType(), IQ.Type.GET);
    }

    @Test
    public void unmarshalSetIQ() throws XMLStreamException, JAXBException {
        String xml = "<iq type=\"set\"/>";
        IQ iq = unmarshal(xml, IQ.class);
        Assert.assertEquals(iq.getType(), IQ.Type.SET);
    }

    @Test
    public void unmarshalErrorIQ() throws XMLStreamException, JAXBException {
        String xml = "<iq type=\"error\"/>";
        IQ iq = unmarshal(xml, IQ.class);
        Assert.assertEquals(iq.getType(), IQ.Type.ERROR);
    }

    @Test
    public void marshalIQ() throws JAXBException, XMLStreamException {
        IQ iq = new IQ(new Jid("to", "domain"), new Jid("from", "domain"), "id", IQ.Type.GET, null);
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"get\"></iq>");
    }

    @Test
    public void marshalIQWithError() throws JAXBException, XMLStreamException {
        IQ iq = new IQ(new Jid("to", "domain"), new Jid("from", "domain"), "id", IQ.Type.GET, null, new StanzaError(StanzaError.Type.MODIFY, new ServiceUnavailable()));
        String xml = marshal(iq);
        Assert.assertEquals(xml, "<iq from=\"from@domain\" id=\"id\" to=\"to@domain\" type=\"get\"><error type=\"modify\"><service-unavailable xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"></service-unavailable></error></iq>");
    }

    @Test
    public void testErrorIQ() throws JAXBException, XMLStreamException {
        IQ iq = new IQ(new Jid("to", "domain"), new Jid("from", "domain"), "id", IQ.Type.GET, null);
        IQ error = iq.createError(new StanzaError(new UndefinedCondition()));
        Assert.assertEquals(error.getType(), IQ.Type.ERROR);
        Assert.assertEquals(error.getId(), iq.getId());
        Assert.assertEquals(error.getTo(), iq.getFrom());
        Assert.assertEquals(error.getFrom(), iq.getTo());
    }

    @Test
    public void testResultIQ() throws JAXBException, XMLStreamException {
        IQ iq = new IQ(new Jid("to", "domain"), new Jid("from", "domain"), "id", IQ.Type.GET, null);
        IQ result = iq.createResult();
        Assert.assertEquals(result.getType(), IQ.Type.RESULT);
        Assert.assertEquals(result.getId(), iq.getId());
        Assert.assertEquals(result.getTo(), iq.getFrom());
        Assert.assertEquals(result.getFrom(), iq.getTo());
    }
}
