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

package org.xmpp.sasl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.XmlTest;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class SaslTest extends XmlTest {

    protected SaslTest() throws JAXBException, XMLStreamException {
        super(Auth.class, Challenge.class, Response.class, Success.class, Abort.class, Failure.class);
    }

    @Test
    public void unmarshalAuth() throws XMLStreamException, JAXBException {
        String xml = "<auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>";
        Auth auth = unmarshal(xml, Auth.class);
        Assert.assertNotNull(auth);
    }

    @Test
    public void unmarshalChallenge() throws XMLStreamException, JAXBException {
        String xml = "<challenge xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>";
        Challenge challenge = unmarshal(xml, Challenge.class);
        Assert.assertNotNull(challenge);
    }

    @Test
    public void unmarshalResponse() throws XMLStreamException, JAXBException {
        String xml = "<response xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>";
        Response response = unmarshal(xml, Response.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void unmarshalAbort() throws XMLStreamException, JAXBException {
        String xml = "<abort xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>";
        Abort abort = unmarshal(xml, Abort.class);
        Assert.assertNotNull(abort);
    }

    @Test
    public void unmarshalSuccess() throws XMLStreamException, JAXBException {
        String xml = "<success xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>base64</success>";
        Success success = unmarshal(xml, Success.class);
        Assert.assertNotNull(success);
        Assert.assertNotNull(success.getAdditionalData());
    }

    @Test
    public void unmarshalAccountDisabled() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <account-disabled/>\n" +
                "     <text xml:lang='en'>Call 212-555-1212 for assistance.</text>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.AccountDisabled);
        Assert.assertEquals(failure.getLanguage(), "en");
        Assert.assertEquals(failure.getText(), "Call 212-555-1212 for assistance.");
    }

    @Test
    public void unmarshalCredentialsExpired() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <credentials-expired/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.CredentialsExpired);
    }

    @Test
    public void unmarshalEncryptionRequired() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <encryption-required/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.EncryptionRequired);
    }

    @Test
    public void unmarshalIncorrectEncoding() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <incorrect-encoding/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.IncorrectEncoding);
    }

    @Test
    public void unmarshalInvalidAuthzid() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <invalid-authzid/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.InvalidAuthzid);
    }

    @Test
    public void unmarshalMalformedRequest() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <malformed-request/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.MalformedRequest);
    }

    @Test
    public void unmarshalMechanismTooWeak() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <mechanism-too-weak/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.MechanismTooWeak);
    }

    @Test
    public void unmarshalInvalidMechanism() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <invalid-mechanism/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.InvalidMechanism);
    }

    @Test
    public void unmarshalNotAuthorized() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <not-authorized/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.NotAuthorized);
    }

    @Test
    public void unmarshalTemporaryAuthFailure() throws XMLStreamException, JAXBException {
        String xml = "<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>\n" +
                "     <temporary-auth-failure/>\n" +
                "   </failure>";
        Failure failure = unmarshal(xml, Failure.class);
        Assert.assertNotNull(failure);
        Assert.assertTrue(failure.getCondition() instanceof Failure.TemporaryAuthFailure);
    }
}
