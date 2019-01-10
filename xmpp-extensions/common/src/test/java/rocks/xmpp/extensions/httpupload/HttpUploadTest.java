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

package rocks.xmpp.extensions.httpupload;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.extensions.httpupload.model.FileTooLarge;
import rocks.xmpp.extensions.httpupload.model.Request;
import rocks.xmpp.extensions.httpupload.model.Retry;
import rocks.xmpp.extensions.httpupload.model.Slot;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;

/**
 * @author Christian Schudt
 */
public class HttpUploadTest extends XmlTest {

    protected HttpUploadTest() throws JAXBException {
        super(ClientIQ.class, Request.class, Slot.class, Retry.class, FileTooLarge.class);
    }

    @Test
    public void unmarshalRequest() throws JAXBException, XMLStreamException {
        String xml = "<iq from='romeo@montague.tld/garden'\n" +
                "    id='step_03'\n" +
                "    to='upload.montague.tld'\n" +
                "    type='get'>\n" +
                "  <request xmlns='urn:xmpp:http:upload:0'\n" +
                "    filename='très cool.jpg'\n" +
                "    size='23456'\n" +
                "    content-type='image/jpeg' />\n" +
                "</iq>";
        IQ iq = unmarshal(xml, IQ.class);
        Request request = iq.getExtension(Request.class);
        Assert.assertNotNull(request);
        Assert.assertEquals(request.getFilename(), "très cool.jpg");
        Assert.assertEquals(request.getContentType(), "image/jpeg");
        Assert.assertEquals(request.getSize(), 23456);
    }

    @Test
    public void unmarshalSlot() throws JAXBException, XMLStreamException {
        String xml = "<iq from='upload.montague.tld'\n" +
                "    id='step_03'\n" +
                "    to='romeo@montague.tld/garden'\n" +
                "    type='result'>\n" +
                "  <slot xmlns='urn:xmpp:http:upload:0'>\n" +
                "    <put url='https://upload.montague.tld/4a771ac1-f0b2-4a4a-9700-f2a26fa2bb67/tr%C3%A8s%20cool.jpg'>\n" +
                "      <header name='Authorization'>Basic Base64String==</header>\n" +
                "      <header name='Cookie'>foo=bar; user=romeo</header>\n" +
                "    </put>\n" +
                "    <get url='https://download.montague.tld/4a771ac1-f0b2-4a4a-9700-f2a26fa2bb67/tr%C3%A8s%20cool.jpg' />\n" +
                "  </slot>\n" +
                "</iq>";

        IQ iq = unmarshal(xml, IQ.class);
        Slot slot = iq.getExtension(Slot.class);
        Assert.assertNotNull(slot);
        Assert.assertEquals(slot.getDownloadUrl().toString(), "https://download.montague.tld/4a771ac1-f0b2-4a4a-9700-f2a26fa2bb67/tr%C3%A8s%20cool.jpg");
        Assert.assertEquals(slot.getUploadUrl().toString(), "https://upload.montague.tld/4a771ac1-f0b2-4a4a-9700-f2a26fa2bb67/tr%C3%A8s%20cool.jpg");
        Assert.assertEquals(slot.getUploadHeaders().size(), 2);
        Assert.assertEquals(slot.getUploadHeaders().get("Authorization"), "Basic Base64String==");
        Assert.assertEquals(slot.getUploadHeaders().get("Cookie"), "foo=bar; user=romeo");
    }

    @Test
    public void marshalSlot() throws JAXBException, XMLStreamException, MalformedURLException {
        Slot slot = new Slot(new URL("https://upload"), Collections.singletonMap("Cookie", "test"), new URL("https://download"));
        Assert.assertEquals(slot.getDownloadUrl().toString(), "https://download");
        Assert.assertEquals(slot.getUploadUrl().toString(), "https://upload");
        Assert.assertEquals(slot.getUploadHeaders().size(), 1);
        Assert.assertEquals(slot.getUploadHeaders().get("Cookie"), "test");
        Assert.assertEquals(marshal(slot), "<slot xmlns=\"urn:xmpp:http:upload:0\"><put url=\"https://upload\"><header name=\"Cookie\">test</header></put><get url=\"https://download\"></get></slot>");
    }

    @Test
    public void unmarshalFileTooLarge() throws JAXBException, XMLStreamException {
        String xml = "<iq from='upload.montague.tld'\n" +
                "    id='step_03'\n" +
                "    to='romeo@montague.tld/garden'\n" +
                "    type='error'>\n" +
                "  <request xmlns='urn:xmpp:http:upload:0'\n" +
                "    filename='très cool.jpg'\n" +
                "    size='23456'\n" +
                "    content-type='image/jpeg' />\n" +
                "  <error type='modify'>\n" +
                "    <not-acceptable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas' />\n" +
                "    <text xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'>File too large. The maximum file size is 20000 bytes</text>\n" +
                "    <file-too-large xmlns='urn:xmpp:http:upload:0'>\n" +
                "      <max-file-size>20000</max-file-size>\n" +
                "    </file-too-large>\n" +
                "  </error>\n" +
                "</iq>";

        IQ iq = unmarshal(xml, IQ.class);
        StanzaError error = iq.getError();
        Assert.assertTrue(error.getExtension() instanceof FileTooLarge);
        Assert.assertEquals(((FileTooLarge) error.getExtension()).getMaxFileSize(), Long.valueOf(20000));
    }

    @Test
    public void unmarshalRetry() throws JAXBException, XMLStreamException {
        String xml = "<iq from='upload.montague.tld'\n" +
                "    id='step_03'\n" +
                "    to='romeo@montague.tld/garden'\n" +
                "    type='error'>\n" +
                "  <request xmlns='urn:xmpp:http:upload:0'\n" +
                "    filename='très cool.jpg'\n" +
                "    size='23456'\n" +
                "    content-type='image/jpeg' />\n" +
                "  <error type='wait'>\n" +
                "    <resource-constraint xmlns='urn:ietf:params:xml:ns:xmpp-stanzas' />\n" +
                "    <text xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'>Quota reached. You can only upload 5 files in 5 minutes</text>\n" +
                "    <retry xmlns='urn:xmpp:http:upload:0'\n" +
                "      stamp='2017-12-03T23:42:05Z' />\n" +
                "  </error>\n" +
                "</iq>";

        IQ iq = unmarshal(xml, IQ.class);
        StanzaError error = iq.getError();
        Assert.assertTrue(error.getExtension() instanceof Retry);
        Assert.assertEquals(((Retry) error.getExtension()).getStamp(), Instant.parse("2017-12-03T23:42:05Z"));
    }
}
