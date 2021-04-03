/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.extensions.dialback;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.dialback.model.Dialback;
import rocks.xmpp.extensions.dialback.model.feature.DialbackFeature;

/**
 * @author Christian Schudt
 */
public class DialbackTest extends XmlTest {

    protected DialbackTest() throws JAXBException {
        super("jabber:server");
    }

    @Test
    public void marshalResultDialbackKey() throws JAXBException, XMLStreamException {
        String xml = marshal(new Dialback.Result(Jid.ofDomain("from.domain"), Jid.ofDomain("to.domain"), "secret"));
        Assert.assertEquals(xml, "<db:result from=\"from.domain\" to=\"to.domain\">secret</db:result>");
    }

    @Test
    public void marshalResultValidKey() throws JAXBException, XMLStreamException {
        String xml = marshal(new Dialback.Result(Jid.ofDomain("from.domain"), Jid.ofDomain("to.domain"), true));
        Assert.assertEquals(xml, "<db:result from=\"from.domain\" to=\"to.domain\" type=\"valid\"></db:result>");
    }

    @Test
    public void marshalResultInvalidKey() throws JAXBException, XMLStreamException {
        String xml = marshal(new Dialback.Result(Jid.ofDomain("from.domain"), Jid.ofDomain("to.domain"), false));
        Assert.assertEquals(xml, "<db:result from=\"from.domain\" to=\"to.domain\" type=\"invalid\"></db:result>");
    }

    @Test
    public void marshalResultError() throws JAXBException, XMLStreamException {
        String xml = marshal(new Dialback.Result(Jid.ofDomain("from.domain"), Jid.ofDomain("to.domain"), new StanzaError(Condition.ITEM_NOT_FOUND)));
        Assert.assertEquals(xml, "<db:result from=\"from.domain\" to=\"to.domain\" type=\"error\"><error type=\"cancel\"><item-not-found xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"></item-not-found></error></db:result>");
    }

    @Test
    public void marshalVerifificationRequest() throws JAXBException, XMLStreamException {
        String xml = marshal(new Dialback.Verify(Jid.ofDomain("from.domain"), Jid.ofDomain("to.domain"), "123", "secret"));
        Assert.assertEquals(xml, "<db:verify from=\"from.domain\" to=\"to.domain\" id=\"123\">secret</db:verify>");
    }

    @Test
    public void marshalVerifyValidKey() throws JAXBException, XMLStreamException {
        String xml = marshal(new Dialback.Verify(Jid.ofDomain("from.domain"), Jid.ofDomain("to.domain"), "123", true));
        Assert.assertEquals(xml, "<db:verify from=\"from.domain\" to=\"to.domain\" type=\"valid\" id=\"123\"></db:verify>");
    }

    @Test
    public void marshalVerifyInvalidKey() throws JAXBException, XMLStreamException {
        String xml = marshal(new Dialback.Verify(Jid.ofDomain("from.domain"), Jid.ofDomain("to.domain"), "123", false));
        Assert.assertEquals(xml, "<db:verify from=\"from.domain\" to=\"to.domain\" type=\"invalid\" id=\"123\"></db:verify>");
    }

    @Test
    public void marshalVerifyError() throws JAXBException, XMLStreamException {
        String xml = marshal(new Dialback.Verify(Jid.ofDomain("from.domain"), Jid.ofDomain("to.domain"), "123", new StanzaError(Condition.ITEM_NOT_FOUND)));
        Assert.assertEquals(xml, "<db:verify from=\"from.domain\" to=\"to.domain\" type=\"error\" id=\"123\"><error type=\"cancel\"><item-not-found xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"></item-not-found></error></db:verify>");
    }

    @Test
    public void unmarshalVerify() throws JAXBException, XMLStreamException {
        String xml = "<db:verify\n" +
                "          from='montague.example'\n" +
                "          id='417GAF25'\n" +
                "          to='capulet.example'\n" +
                "          type='valid'/>";
        Dialback.Verify verify = unmarshal(xml, Dialback.Verify.class, new QName(Dialback.NAMESPACE, "", "db"));
        Assert.assertEquals(verify.getId(), "417GAF25");
        Assert.assertEquals(verify.getFrom(), Jid.ofDomain("montague.example"));
        Assert.assertEquals(verify.getTo(), Jid.ofDomain("capulet.example"));
        Assert.assertTrue(verify.isValid());
    }

    @Test
    public void unmarshalResultRequest() throws JAXBException, XMLStreamException {
        String xml = "<db:result\n" +
                "          from='capulet.example'\n" +
                "          to='montague.example'>" +
                "b4835385f37fe2895af6c196b59097b16862406db80559900d96bf6fa7d23df3" +
                "</db:result>";
        Dialback.Result result = unmarshal(xml, Dialback.Result.class, new QName(Dialback.NAMESPACE, "", "db"));
        Assert.assertEquals(result.getFrom(), Jid.ofDomain("capulet.example"));
        Assert.assertEquals(result.getTo(), Jid.ofDomain("montague.example"));
        Assert.assertEquals(result.getKey(), "b4835385f37fe2895af6c196b59097b16862406db80559900d96bf6fa7d23df3");
        Assert.assertNull(result.getError());
    }

    @Test
    public void unmarshalResultError() throws JAXBException, XMLStreamException {
        String xml = "<db:result\n" +
                "          from='montague.example'\n" +
                "          to='capulet.example'\n" +
                "          type='error'>" +
                "<error type='cancel'>\n" +
                "          <item-not-found xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "        </error>" +
                "</db:result>";
        Dialback.Result result = unmarshal(xml, Dialback.Result.class, new QName(Dialback.NAMESPACE, "", "db"));
        Assert.assertEquals(result.getFrom(), Jid.ofDomain("montague.example"));
        Assert.assertEquals(result.getTo(), Jid.ofDomain("capulet.example"));
        Assert.assertNull(result.getKey());
        Assert.assertNotNull(result.getError());
        Assert.assertEquals(result.getError().getCondition(), Condition.ITEM_NOT_FOUND);
    }

    @Test
    public void unmarshalResult() throws JAXBException, XMLStreamException {
        String xml = "<db:result\n" +
                "          from='montague.example'\n" +
                "          to='capulet.example'\n" +
                "          type='invalid'/>";
        Dialback.Result result = unmarshal(xml, Dialback.Result.class, new QName(Dialback.NAMESPACE, "", "db"));
        Assert.assertEquals(result.getFrom(), Jid.ofDomain("montague.example"));
        Assert.assertEquals(result.getTo(), Jid.ofDomain("capulet.example"));
        Assert.assertFalse(result.isValid());
    }

    @Test
    public void generateDialbackKey() {
        String key = Dialback.generateKey("s3cr3tf0rd14lb4ck", "xmpp.example.com", "example.org", "D60000229F");
        Assert.assertEquals(key, "37c69b1cf07a3f67c04a5ef5902fa5114f2c76fe4a2686482ba5b89323075643");
    }

    @Test
    public void testDialbackStreamFeature() throws JAXBException, XMLStreamException {
        String xml = marshal(DialbackFeature.INSTANCE);
        Assert.assertEquals(xml, "<dialback xmlns=\"urn:xmpp:features:dialback\"><errors></errors></dialback>");
    }
}
