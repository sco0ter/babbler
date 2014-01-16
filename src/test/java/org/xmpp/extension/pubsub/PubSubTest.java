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

package org.xmpp.extension.pubsub;

import junit.framework.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class PubSubTest extends BaseTest {

    @Test
    public void unmarshalPubSub() throws XMLStreamException, JAXBException {
        String xml = "<iq type='set'\n" +
                "    from='hamlet@denmark.lit/blogbot'\n" +
                "    to='pubsub.shakespeare.lit'\n" +
                "    id='pub1'>\n" +
                "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <publish node='princely_musings'>\n" +
                "      <item>\n" +
                "        <entry xmlns='http://www.w3.org/2005/Atom'>\n" +
                "          <title>Soliloquy</title>\n" +
                "          <summary>\n" +
                "To be, or not to be: that is the question:\n" +
                "Whether 'tis nobler in the mind to suffer\n" +
                "The slings and arrows of outrageous fortune,\n" +
                "Or to take arms against a sea of troubles,\n" +
                "And by opposing end them?\n" +
                "          </summary>\n" +
                "          <link rel='alternate' type='text/html'\n" +
                "                href='http://denmark.lit/2003/12/13/atom03'/>\n" +
                "          <id>tag:denmark.lit,2003:entry-32397</id>\n" +
                "          <published>2003-12-13T18:30:02Z</published>\n" +
                "          <updated>2003-12-13T18:30:02Z</updated>\n" +
                "        </entry>\n" +
                "      </item>\n" +
                "    </publish>\n" +
                "  </pubsub>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);

        PubSub pubSub = iq.getExtension(PubSub.class);
        Assert.assertNotNull(pubSub);
    }
}
