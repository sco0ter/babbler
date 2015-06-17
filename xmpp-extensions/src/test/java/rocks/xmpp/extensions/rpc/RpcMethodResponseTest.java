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

package rocks.xmpp.extensions.rpc;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.extensions.rpc.model.Rpc;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class RpcMethodResponseTest extends XmlTest {

    protected RpcMethodResponseTest() throws JAXBException, XMLStreamException {
        super(ClientIQ.class, Rpc.class);
    }

    @Test
    public void unmarshalRpc() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result' \n" +
                "    from='responder@company-a.com/jrpc-server' \n" +
                "    to='requester@company-b.com/jrpc-client' \n" +
                "    id='rpc1'>\n" +
                "  <query xmlns='jabber:iq:rpc'>\n" +
                "    <methodResponse>\n" +
                "      <params>\n" +
                "        <param>\n" +
                "          <value><string>Colorado</string></value>\n" +
                "        </param>\n" +
                "      </params>\n" +
                "    </methodResponse>\n" +
                "  </query>\n" +
                "</iq>";

        IQ iq = unmarshal(xml, IQ.class);
        Rpc rpc = iq.getExtension(Rpc.class);
        Assert.assertNotNull(rpc);
        Assert.assertEquals(rpc.getMethodResponse().getResponse().getAsString(), "Colorado");
    }

    @Test
    public void unmarshalFault() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result' \n" +
                "    from='responder@company-a.com/jrpc-server' \n" +
                "    to='requester@company-b.com/jrpc-client' \n" +
                "    id='rpc1'>\n" +
                "  <query xmlns='jabber:iq:rpc'>\n" +
                "   <methodResponse>\n" +
                "  <fault>\n" +
                "    <value>\n" +
                "      <struct>\n" +
                "        <member>\n" +
                "          <name>faultCode</name>\n" +
                "          <value><int>4</int></value>\n" +
                "        </member>\n" +
                "        <member>\n" +
                "          <name>faultString</name>\n" +
                "          <value><string>Too many parameters.</string></value>\n" +
                "        </member>\n" +
                "      </struct>\n" +
                "    </value>\n" +
                "  </fault>\n" +
                "</methodResponse>" +
                "  </query>\n" +
                "</iq>";

        IQ iq = unmarshal(xml, IQ.class);
        Rpc rpc = iq.getExtension(Rpc.class);
        Assert.assertNotNull(rpc);
        Assert.assertNotNull(rpc.getMethodResponse().getFault());
        Assert.assertEquals(rpc.getMethodResponse().getFault().getFaultCode(), 4);
        Assert.assertEquals(rpc.getMethodResponse().getFault().getFaultString(), "Too many parameters.");
    }
}
