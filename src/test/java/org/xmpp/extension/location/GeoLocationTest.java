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

package org.xmpp.extension.location;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.nickname.Nickname;
import org.xmpp.stanza.Presence;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class GeoLocationTest extends BaseTest {

    @Test
    public void unmarshalGeoLocation() throws XMLStreamException, JAXBException {
        String xml = "<geoloc xmlns='http://jabber.org/protocol/geoloc' xml:lang='en'>\n" +
                "          <accuracy>20</accuracy>\n" +
                "          <country>Italy</country>\n" +
                "          <lat>45.44</lat>\n" +
                "          <locality>Venice</locality>\n" +
                "          <lon>12.33</lon>\n" +
                "        </geoloc>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        GeoLocation geoLocation = (GeoLocation) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(geoLocation);
        Assert.assertEquals(geoLocation.getLanguage(), "en");
        Assert.assertEquals(geoLocation.getAccuracy(), 20.0);
        Assert.assertEquals(geoLocation.getCountry(), "Italy");
        Assert.assertEquals(geoLocation.getLatitude(), 45.44);
        Assert.assertEquals(geoLocation.getLocality(), "Venice");
        Assert.assertEquals(geoLocation.getLongitude(), 12.33);

    }
}
