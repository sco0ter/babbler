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

package org.xmpp.extension.vcard;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.time.EntityTime;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.util.Calendar;

/**
 * @author Christian Schudt
 */
public class VCardTest extends BaseTest {

    @Test
    public void unmarshalVCardResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq id='v1'\n" +
                "    to='stpeter@jabber.org/roundabout'\n" +
                "    type='result'>\n" +
                "  <vCard xmlns='vcard-temp'>\n" +
                "    <FN>Peter Saint-Andre</FN>\n" +
                "    <N>\n" +
                "      <FAMILY>Saint-Andre</FAMILY>\n" +
                "      <GIVEN>Peter</GIVEN>\n" +
                "      <MIDDLE/>\n" +
                "    </N>\n" +
                "    <NICKNAME>stpeter</NICKNAME>\n" +
                "    <URL>http://www.xmpp.org/xsf/people/stpeter.shtml</URL>\n" +
                "    <BDAY>1966-08-06</BDAY>\n" +
                "    <ORG>\n" +
                "      <ORGNAME>XMPP Standards Foundation</ORGNAME>\n" +
                "      <ORGUNIT/>\n" +
                "    </ORG>\n" +
                "    <TITLE>Executive Director</TITLE>\n" +
                "    <ROLE>Patron Saint</ROLE>\n" +
                "    <TEL><WORK/><VOICE/><NUMBER>303-308-3282</NUMBER></TEL>\n" +
                "    <TEL><WORK/><FAX/><NUMBER/></TEL>\n" +
                "    <TEL><WORK/><MSG/><NUMBER/></TEL>\n" +
                "    <ADR>\n" +
                "      <WORK/>\n" +
                "      <EXTADD>Suite 600</EXTADD>\n" +
                "      <STREET>1899 Wynkoop Street</STREET>\n" +
                "      <LOCALITY>Denver</LOCALITY>\n" +
                "      <REGION>CO</REGION>\n" +
                "      <PCODE>80202</PCODE>\n" +
                "      <CTRY>USA</CTRY>\n" +
                "    </ADR>\n" +
                "    <TEL><HOME/><VOICE/><NUMBER>303-555-1212</NUMBER></TEL>\n" +
                "    <TEL><HOME/><FAX/><NUMBER/></TEL>\n" +
                "    <TEL><HOME/><MSG/><NUMBER/></TEL>\n" +
                "    <ADR>\n" +
                "      <HOME/>\n" +
                "      <EXTADD/>\n" +
                "      <STREET/>\n" +
                "      <LOCALITY>Denver</LOCALITY>\n" +
                "      <REGION>CO</REGION>\n" +
                "      <PCODE>80209</PCODE>\n" +
                "      <CTRY>USA</CTRY>\n" +
                "    </ADR>\n" +
                "    <EMAIL><INTERNET/><PREF/><USERID>stpeter@jabber.org</USERID></EMAIL>\n" +
                "    <JABBERID>stpeter@jabber.org</JABBERID>\n" +
                "    <DESC>\n" +
                "      More information about me is located on my \n" +
                "      personal website: http://www.saint-andre.com/\n" +
                "    </DESC>\n" +
                "  </vCard>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        VCard vCard = iq.getExtension(VCard.class);
        Assert.assertNotNull(vCard);
    }
}
