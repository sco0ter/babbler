/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.bookmarks.pep.model;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;

public class PepNativeBookmarkTest extends XmlTest {

    @Test
    public void unmarshal() throws JAXBException, XMLStreamException {
        String xml = "<conference xmlns='urn:xmpp:bookmarks:1'\n" +
                "            name='Council of Oberon'\n" +
                "            autojoin='true'>\n" +
                "  <nick>Puck</nick>\n" +
                "</conference>";

        PepNativeBookmark pepNativeBookmark = unmarshal(xml, PepNativeBookmark.class);

        Assert.assertEquals(pepNativeBookmark.getName(), "Council of Oberon");
        Assert.assertTrue(pepNativeBookmark.isAutojoin());
        Assert.assertEquals(pepNativeBookmark.getNick(), "Puck");
    }

    @Test
    public void marshal() throws JAXBException, XMLStreamException {

        PepNativeBookmark pepNativeBookmark = new PepNativeBookmark("Council of Oberon", "Puck", "pwd", true);
        String xml = marshal(pepNativeBookmark);

        Assert.assertEquals(xml, "<conference xmlns=\"urn:xmpp:bookmarks:1\" name=\"Council of Oberon\" autojoin=\"true\"><nick>Puck</nick><password>pwd</password></conference>");
    }
}
