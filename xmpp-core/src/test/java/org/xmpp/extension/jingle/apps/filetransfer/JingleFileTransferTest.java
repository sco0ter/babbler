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

package org.xmpp.extension.jingle.apps.filetransfer;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.XmlTest;
import org.xmpp.extension.jingle.apps.rtp.Rtp;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class JingleFileTransferTest extends XmlTest {
    protected JingleFileTransferTest() throws JAXBException, XMLStreamException {
        super(JingleFileTransfer.class);
    }

    @Test
    public void unmarshalJingleFileTransfer() throws XMLStreamException, JAXBException {
        String xml = "<description xmlns='urn:xmpp:jingle:apps:file-transfer:3'>\n" +
                "        <offer>\n" +
                "          <file>\n" +
                "            <date>1969-07-21T02:56:15Z</date>\n" +
                "            <desc>This is a test. If this were a real file...</desc>\n" +
                "            <name>test.txt</name>\n" +
                "            <range/>\n" +
                "            <size>1022</size>\n" +
                "            <hash xmlns='urn:xmpp:hashes:1' algo='sha-1'>552da749930852c69ae5d2141d3766b1</hash>\n" +
                "          </file>\n" +
                "        </offer>\n" +
                "      </description>\n";

        JingleFileTransfer fileTransfer = unmarshal(xml, JingleFileTransfer.class);
        Assert.assertEquals(fileTransfer.getOffers().size(), 1);
        Assert.assertEquals(fileTransfer.getOffers().get(0).getDescription(), "This is a test. If this were a real file...");
        Assert.assertEquals(fileTransfer.getOffers().get(0).getName(), "test.txt");
        Assert.assertEquals(fileTransfer.getOffers().get(0).getSize(), 1022);
    }
}
