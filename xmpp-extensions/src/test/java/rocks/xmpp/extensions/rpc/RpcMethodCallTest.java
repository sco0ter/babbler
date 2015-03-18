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
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.rpc.model.Rpc;
import rocks.xmpp.extensions.rpc.model.Value;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Schudt
 */
public class RpcMethodCallTest extends XmlTest {

    protected RpcMethodCallTest() throws JAXBException, XMLStreamException {
        super(IQ.class, Rpc.class);
    }

    @Test
    public void unmarshalRpc() throws JAXBException, XMLStreamException {
        String xml = "<iq type='set' \n" +
                "    from='requester@company-b.com/jrpc-client' \n" +
                "    to='responder@company-a.com/jrpc-server' \n" +
                "    id='rpc1'>\n" +
                "  <query xmlns='jabber:iq:rpc'>\n" +
                "    <methodCall>\n" +
                "      <methodName>examples.getStateName</methodName>\n" +
                "      <params>\n" +
                "        <param>\n" +
                "          <value><i4>6</i4></value>\n" +
                "        </param>\n" +
                "        <param>\n" +
                "          <value><boolean>0</boolean></value>\n" +
                "        </param>\n" +
                "        <param>\n" +
                "          <value><array><data><value><boolean>1</boolean></value></data></array></value>\n" +
                "        </param>" +
                "        <param>\n" +
                "          <value><struct><member><name>key</name><value><boolean>1</boolean></value></member></struct></value>\n" +
                "        </param>" +
                "      </params>\n" +
                "    </methodCall>\n" +
                "  </query>\n" +
                "</iq>\n";
        IQ iq = unmarshal(xml, IQ.class);
        Rpc rpc = iq.getExtension(Rpc.class);
        Assert.assertNotNull(rpc);
        Assert.assertEquals(rpc.getMethodCall().getMethodName(), "examples.getStateName");
        Assert.assertEquals(rpc.getMethodCall().getParameters().size(), 4);
        Assert.assertEquals(rpc.getMethodCall().getParameters().get(0).getAsInteger(), (Integer) 6);
        Assert.assertEquals(rpc.getMethodCall().getParameters().get(1).getAsBoolean(), Boolean.FALSE);
        Assert.assertEquals(rpc.getMethodCall().getParameters().get(2).getAsArray().get(0).getAsBoolean(), Boolean.TRUE);
        Assert.assertEquals(rpc.getMethodCall().getParameters().get(3).getAsMap().get("key").getAsBoolean(), Boolean.TRUE);
    }

    @Test
    public void marshalRpcMethodCallWithInteger() throws JAXBException, XMLStreamException {
        Rpc rpc = new Rpc("testMethod", new Value(1));
        String xml = marshal(rpc);
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:rpc\"><methodCall><methodName>testMethod</methodName><params><param><value><int>1</int></value></param></params></methodCall></query>");
    }

    @Test
    public void marshalRpcMethodCallWithString() throws JAXBException, XMLStreamException {
        Rpc rpc = new Rpc("testMethod", new Value("test"));
        String xml = marshal(rpc);
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:rpc\"><methodCall><methodName>testMethod</methodName><params><param><value><string>test</string></value></param></params></methodCall></query>");
    }

    @Test
    public void marshalRpcMethodCallWithDouble() throws JAXBException, XMLStreamException {
        Rpc rpc = new Rpc("testMethod", new Value(10.4));
        String xml = marshal(rpc);
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:rpc\"><methodCall><methodName>testMethod</methodName><params><param><value><double>10.4</double></value></param></params></methodCall></query>");
    }

    @Test
    public void marshalRpcMethodCallWithBase64() throws JAXBException, XMLStreamException {
        byte[] bytes = new byte[1];
        bytes[0] = 65;
        Rpc rpc = new Rpc("testMethod", new Value(bytes));
        String xml = marshal(rpc);
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:rpc\"><methodCall><methodName>testMethod</methodName><params><param><value><base64>QQ==</base64></value></param></params></methodCall></query>");
    }

    @Test
    public void marshalRpcMethodCallWithDate() throws JAXBException, XMLStreamException {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2014);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 23);
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 37);
        calendar.set(Calendar.SECOND, 34);
        calendar.set(Calendar.MILLISECOND, 0);
        Rpc rpc = new Rpc("testMethod", new Value(calendar.getTime()));
        String xml = marshal(rpc);
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:rpc\"><methodCall><methodName>testMethod</methodName><params><param><value><dateTime.iso8601>" + DatatypeConverter.printDateTime(calendar) + "</dateTime.iso8601></value></param></params></methodCall></query>");
    }

    @Test
    public void marshalRpcMethodCallWithArray() throws JAXBException, XMLStreamException {
        List<Value> values = new ArrayList<>();
        values.add(new Value(1));
        values.add(new Value(2));
        Rpc rpc = new Rpc("testMethod", new Value(values));
        String xml = marshal(rpc);
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:rpc\"><methodCall><methodName>testMethod</methodName><params><param><value><array><data><value><int>1</int></value><value><int>2</int></value></data></array></value></param></params></methodCall></query>");
    }

    @Test
    public void marshalRpcMethodCallWithMap() throws JAXBException, XMLStreamException {
        Map<String, Value> map = new LinkedHashMap<>();
        map.put("key1", new Value(1));
        map.put("key2", new Value(true));
        Rpc rpc = new Rpc("testMethod", new Value(map));
        String xml = marshal(rpc);
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:rpc\"><methodCall><methodName>testMethod</methodName><params><param><value><struct><member><name>key1</name><value><int>1</int></value></member><member><name>key2</name><value><boolean>1</boolean></value></member></struct></value></param></params></methodCall></query>");
    }
}
