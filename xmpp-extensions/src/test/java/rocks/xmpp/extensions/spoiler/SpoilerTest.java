/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.extensions.spoiler;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.extensions.spoiler.model.Spoiler;
import rocks.xmpp.extensions.time.model.EntityTime;
import rocks.xmpp.util.adapters.ZoneOffsetAdapter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

/**
 * @author Christian Schudt
 */
public class SpoilerTest extends XmlTest {

    protected SpoilerTest() throws JAXBException, XMLStreamException {
        super(ClientMessage.class, Spoiler.class);
    }

    @Test
    public void unmarshalSpoiler() throws XMLStreamException, JAXBException {
        String xml = "<message to='romeo@montague.net/orchard' from='juliet@capulet.net/balcony' id='spoiler3'>\n" +
                "  <body>holidays.png</body>\n" +
                "  <spoiler xml:lang='en' xmlns='urn:xmpp:spoiler:0'>Trip to the beach</spoiler>\n" +
                "  <spoiler xml:lang='ca' xmlns='urn:xmpp:spoiler:0'>Viatge a la platja</spoiler>\n" +
                "</message>";

        Message message = unmarshal(xml, Message.class);
        List<Spoiler> spoiler = message.getExtensions(Spoiler.class);
        Assert.assertNotNull(spoiler);
        Assert.assertEquals(spoiler.size(), 2);
        Assert.assertEquals(spoiler.get(0).getText(), "Trip to the beach");
        Assert.assertEquals(spoiler.get(0).getLanguage(), Locale.ENGLISH);
        Assert.assertEquals(spoiler.get(1).getText(), "Viatge a la platja");
        Assert.assertEquals(spoiler.get(1).getLanguage(), Locale.forLanguageTag("ca"));
    }
}
