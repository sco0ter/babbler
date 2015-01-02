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

package rocks.xmpp.core.stream;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.errors.BadFormat;
import rocks.xmpp.core.stream.model.errors.BadNamespacePrefix;
import rocks.xmpp.core.stream.model.errors.Conflict;
import rocks.xmpp.core.stream.model.errors.ConnectionTimeout;
import rocks.xmpp.core.stream.model.errors.HostGone;
import rocks.xmpp.core.stream.model.errors.HostUnknown;
import rocks.xmpp.core.stream.model.errors.ImproperAddressing;
import rocks.xmpp.core.stream.model.errors.InternalServerError;
import rocks.xmpp.core.stream.model.errors.InvalidFrom;
import rocks.xmpp.core.stream.model.errors.InvalidNamespace;
import rocks.xmpp.core.stream.model.errors.InvalidXml;
import rocks.xmpp.core.stream.model.errors.NotAuthorized;
import rocks.xmpp.core.stream.model.errors.NotWellFormed;
import rocks.xmpp.core.stream.model.errors.PolicyViolation;
import rocks.xmpp.core.stream.model.errors.RemoteConnectionFailed;
import rocks.xmpp.core.stream.model.errors.Reset;
import rocks.xmpp.core.stream.model.errors.ResourceConstraint;
import rocks.xmpp.core.stream.model.errors.RestrictedXml;
import rocks.xmpp.core.stream.model.errors.SeeOtherHost;
import rocks.xmpp.core.stream.model.errors.SystemShutdown;
import rocks.xmpp.core.stream.model.errors.UndefinedCondition;
import rocks.xmpp.core.stream.model.errors.UnsupportedEncoding;
import rocks.xmpp.core.stream.model.errors.UnsupportedFeature;
import rocks.xmpp.core.stream.model.errors.UnsupportedStanzaType;
import rocks.xmpp.core.stream.model.errors.UnsupportedVersion;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class StreamErrorTest extends XmlTest {

    protected StreamErrorTest() throws JAXBException, XMLStreamException {
        super(StreamError.class);
    }

    @Test
    public void unmarshalBadFormat() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <bad-format\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof BadFormat);
        Assert.assertTrue(streamError.getCondition() == BadFormat.INSTANCE);
    }

    @Test
    public void unmarshalBadNamespacePrefix() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <bad-namespace-prefix\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof BadNamespacePrefix);
        Assert.assertTrue(streamError.getCondition() == BadNamespacePrefix.INSTANCE);
    }

    @Test
    public void unmarshalConflict() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <conflict\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof Conflict);
        Assert.assertTrue(streamError.getCondition() == Conflict.INSTANCE);
    }

    @Test
    public void unmarshalConnectionTimeout() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <connection-timeout\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof ConnectionTimeout);
        Assert.assertTrue(streamError.getCondition() == ConnectionTimeout.INSTANCE);
    }

    @Test
    public void unmarshalHostGone() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <host-gone\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof HostGone);
        Assert.assertTrue(streamError.getCondition() == HostGone.INSTANCE);
    }

    @Test
    public void unmarshalHostUnknown() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <host-unknown\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof HostUnknown);
        Assert.assertTrue(streamError.getCondition() == HostUnknown.INSTANCE);
    }

    @Test
    public void unmarshalImproperAddressing() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <improper-addressing\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof ImproperAddressing);
        Assert.assertTrue(streamError.getCondition() == ImproperAddressing.INSTANCE);
    }

    @Test
    public void unmarshalInternalServerError() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <internal-server-error\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof InternalServerError);
        Assert.assertTrue(streamError.getCondition() == InternalServerError.INSTANCE);
    }

    @Test
    public void unmarshalInvalidFrom() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <invalid-from\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof InvalidFrom);
        Assert.assertTrue(streamError.getCondition() == InvalidFrom.INSTANCE);
    }

    @Test
    public void unmarshalInvalidNamespace() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <invalid-namespace\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof InvalidNamespace);
        Assert.assertTrue(streamError.getCondition() == InvalidNamespace.INSTANCE);
    }

    @Test
    public void unmarshalInvalidXml() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <invalid-xml\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof InvalidXml);
        Assert.assertTrue(streamError.getCondition() == InvalidXml.INSTANCE);
    }

    @Test
    public void unmarshalNotAuthorized() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <not-authorized\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof NotAuthorized);
        Assert.assertTrue(streamError.getCondition() == NotAuthorized.INSTANCE);
    }

    @Test
    public void unmarshalNotWellFormed() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <not-well-formed\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof NotWellFormed);
        Assert.assertTrue(streamError.getCondition() == NotWellFormed.INSTANCE);
    }

    @Test
    public void unmarshalPolicyViolation() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <policy-violation\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof PolicyViolation);
        Assert.assertTrue(streamError.getCondition() == PolicyViolation.INSTANCE);
    }

    @Test
    public void unmarshalRemoteConnectionFailed() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <remote-connection-failed\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof RemoteConnectionFailed);
        Assert.assertTrue(streamError.getCondition() == RemoteConnectionFailed.INSTANCE);
    }

    @Test
    public void unmarshalReset() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <reset\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof Reset);
        Assert.assertTrue(streamError.getCondition() == Reset.INSTANCE);
    }

    @Test
    public void unmarshalResourceConstraint() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <resource-constraint\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof ResourceConstraint);
        Assert.assertTrue(streamError.getCondition() == ResourceConstraint.INSTANCE);
    }

    @Test
    public void unmarshalRestrictedXml() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <restricted-xml\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof RestrictedXml);
        Assert.assertTrue(streamError.getCondition() == RestrictedXml.INSTANCE);
    }

    @Test
    public void unmarshalSeeOtherHost() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <see-other-host\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'>[2001:41D0:1:A49b::1]:9222</see-other-host>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof SeeOtherHost);
        Assert.assertEquals(((SeeOtherHost) streamError.getCondition()).getOtherHost(), "[2001:41D0:1:A49b::1]:9222");
    }

    @Test
    public void unmarshalSystemShutdown() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <system-shutdown\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof SystemShutdown);
        Assert.assertTrue(streamError.getCondition() == SystemShutdown.INSTANCE);
    }

    @Test
    public void unmarshalUndefinedCondition() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <undefined-condition\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof UndefinedCondition);
        Assert.assertTrue(streamError.getCondition() == UndefinedCondition.INSTANCE);
    }

    @Test
    public void unmarshalUnsupportedEncoding() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <unsupported-encoding\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof UnsupportedEncoding);
        Assert.assertTrue(streamError.getCondition() == UnsupportedEncoding.INSTANCE);
    }

    @Test
    public void unmarshalUnsupportedFeature() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <unsupported-feature\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof UnsupportedFeature);
        Assert.assertTrue(streamError.getCondition() == UnsupportedFeature.INSTANCE);
    }

    @Test
    public void unmarshalUnsupportedStanzaType() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <unsupported-stanza-type\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof UnsupportedStanzaType);
        Assert.assertTrue(streamError.getCondition() == UnsupportedStanzaType.INSTANCE);
    }

    @Test
    public void shouldUnmarshalUnsupportedVersion() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <unsupported-version\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertTrue(streamError.getCondition() instanceof UnsupportedVersion);
        Assert.assertTrue(streamError.getCondition() == UnsupportedVersion.INSTANCE);
    }

    @Test
    public void shouldUnmarshalErrorWithText() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <host-unknown\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "     <text xmlns='urn:ietf:params:xml:ns:xmpp-streams'\n" +
                "           xml:lang='en'>OPTIONAL descriptive text</text>" +
                "   </stream:error>";
        StreamError streamError = unmarshal(xml, StreamError.class);
        Assert.assertEquals(streamError.getText(), "OPTIONAL descriptive text");
        Assert.assertEquals(streamError.getLanguage(), "en");
        Assert.assertTrue(streamError.getCondition() instanceof HostUnknown);
        Assert.assertTrue(streamError.getCondition() == HostUnknown.INSTANCE);
    }
}
