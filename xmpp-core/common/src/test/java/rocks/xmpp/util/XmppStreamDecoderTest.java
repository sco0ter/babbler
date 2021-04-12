package rocks.xmpp.util;

import java.io.Reader;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.StreamHeader;

/**
 * Tests for {@link XmppStreamDecoder}.
 *
 * @author Christian Schudt
 */
public class XmppStreamDecoderTest extends XmlTest {

    @Test
    public void testAdditionalNamespaces() throws StreamErrorException {
        Reader reader = new StringReader("<stream:stream\n"
                + "    from='example.com'\n"
                + "    xmlns='jabber:client'\n"
                + "    xmlns:stream='http://etherx.jabber.org/streams'\n"
                + "    xmlns:add='namespace'\n"
                + "    xmlns:add2='namespace2'\n"
                + "    version='1.0'></stream:stream>");

        XmppStreamDecoder decoder =
                new XmppStreamDecoder(XMLInputFactory.newFactory(), () -> unmarshaller, "jabber:client");
        decoder.decode(reader, streamElement -> {
            if (streamElement instanceof StreamHeader) {
                StreamHeader streamHeader = (StreamHeader) streamElement;
                Assert.assertEquals(streamHeader.getAdditionalNamespaces().size(), 2);
            }
        });
    }
}
