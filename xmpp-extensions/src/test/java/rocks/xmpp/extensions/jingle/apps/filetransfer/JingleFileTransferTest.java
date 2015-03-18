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

package rocks.xmpp.extensions.jingle.apps.filetransfer;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.extensions.jingle.apps.filetransfer.model.JingleFileTransfer;
import rocks.xmpp.extensions.jingle.model.Jingle;
import rocks.xmpp.extensions.jingle.transports.s5b.model.S5bTransportMethod;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class JingleFileTransferTest extends XmlTest {
    protected JingleFileTransferTest() throws JAXBException, XMLStreamException {
        super(Hash.class, Jingle.class, JingleFileTransfer.class, S5bTransportMethod.class);
    }

    @Test
    public void unmarshalJingleFileTransfer() throws XMLStreamException, JAXBException {
        String xml = "<description xmlns='urn:xmpp:jingle:apps:file-transfer:4'>\n" +
                "        <file>\n" +
                "          <date>1969-07-21T02:56:15Z</date>\n" +
                "          <desc>This is a test. If this were a real file...</desc>\n" +
                "          <media-type>text/plain</media-type>\n" +
                "          <name>test.txt</name>\n" +
                "          <range/>\n" +
                "          <size>1022</size>\n" +
                "          <hash xmlns='urn:xmpp:hashes:1' algo='sha-1'>552da749930852c69ae5d2141d3766b1</hash>\n" +
                "        </file>\n" +
                "      </description>\n";

        JingleFileTransfer fileTransfer = unmarshal(xml, JingleFileTransfer.class);
        Assert.assertNotNull(fileTransfer.getFile());
        Assert.assertEquals(fileTransfer.getFile().getDescription(), "This is a test. If this were a real file...");
        Assert.assertEquals(fileTransfer.getFile().getMediaType(), "text/plain");
        Assert.assertEquals(fileTransfer.getFile().getName(), "test.txt");
        Assert.assertEquals(fileTransfer.getFile().getSize(), 1022);
    }

    @Test
    public void unmarshalJingleFileTransferChecksum() throws XMLStreamException, JAXBException {
        String xml = "<jingle xmlns='urn:xmpp:jingle:1'\n" +
                "          action='session-info'\n" +
                "          initiator='romeo@montague.lit/orchard'\n" +
                "          sid='a73sjjvkla37jfea'>\n" +
                "    <checksum xmlns='urn:xmpp:jingle:apps:file-transfer:4'>\n" +
                "      <file>\n" +
                "        <hash xmlns='urn:xmpp:hashes:1' algo='sha-1'>552da749930852c69ae5d2141d3766b1</hash>\n" +
                "      </file>\n" +
                "    </checksum>\n" +
                "  </jingle>\n";

        Jingle jingle = unmarshal(xml, Jingle.class);
        Assert.assertTrue(jingle.getPayload() instanceof JingleFileTransfer.Checksum);
        Assert.assertEquals(((JingleFileTransfer.Checksum) jingle.getPayload()).getFile().getHashes().size(), 1);
    }
}
