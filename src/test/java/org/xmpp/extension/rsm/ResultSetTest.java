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

package org.xmpp.extension.rsm;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class ResultSetTest extends BaseTest {

    @Test
    public void marshalItemCount() throws IOException, JAXBException, XMLStreamException {
        String xml = marshall(ResultSet.forItemCount());
        Assert.assertEquals(xml, "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>0</max></set>");
    }

    @Test
    public void marshalLimit() throws IOException, JAXBException, XMLStreamException {
        String xml = marshall(ResultSet.forLimit(10));
        Assert.assertEquals(xml, "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>10</max></set>");
    }

    @Test
    public void marshalForFirstPage() throws IOException, JAXBException, XMLStreamException {
        String xml = marshall(ResultSet.forFirstPage(10));
        Assert.assertEquals(xml, "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>10</max></set>");
    }

    @Test
    public void marshalForNextPage() throws IOException, JAXBException, XMLStreamException {
        String xml = marshall(ResultSet.forNextPage(10, "next"));
        Assert.assertEquals(xml, "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>10</max><after>next</after></set>");
    }

    @Test
    public void marshalForLastPage() throws IOException, JAXBException, XMLStreamException {
        String xml = marshall(ResultSet.forLastPage(10));
        Assert.assertEquals(xml, "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>10</max><before></before></set>");
    }

    @Test
    public void marshalForPreviousPage() throws IOException, JAXBException, XMLStreamException {
        String xml = marshall(ResultSet.forPreviousPage(10, "previous"));
        Assert.assertEquals(xml, "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>10</max><before>previous</before></set>");
    }

    @Test
    public void marshalForIndex() throws IOException, JAXBException, XMLStreamException {
        String xml = marshall(ResultSet.forIndex(10, 371));
        Assert.assertEquals(xml, "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>10</max><index>371</index></set>");
    }

    @Test
    public void unmarshalResultSet() throws XMLStreamException, JAXBException {
        String xml = "<set xmlns='http://jabber.org/protocol/rsm'>\n" +
                "      <first index='0'>stpeter@jabber.org</first>\n" +
                "      <last>peterpan@neverland.lit</last>\n" +
                "      <count>800</count>\n" +
                "    </set>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        ResultSet set = (ResultSet) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(set);
        Assert.assertEquals(set.getFirstItem(), "stpeter@jabber.org");
        Assert.assertEquals(set.getFirstItemIndex(), Integer.valueOf(0));
        Assert.assertEquals(set.getLastItem(), "peterpan@neverland.lit");
        Assert.assertEquals(set.getItemCount(), Integer.valueOf(800));
    }
}
