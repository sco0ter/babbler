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

package rocks.xmpp.extensions.vcard.v4;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.vcard.VCard;
import rocks.xmpp.extensions.vcard.v4.model.VCard4;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * @author Christian Schudt
 */
public class VCard4Test extends XmlTest {
    protected VCard4Test() throws JAXBException, XMLStreamException {
        super(IQ.class, VCard4.class);
    }

    @Test
    public void unmarshalVCardResponse() throws XMLStreamException, JAXBException, MalformedURLException {
        String xml = "<iq from='stpeter@jabber.org'\n" +
                "      id='bx81v356'\n" +
                "      to='samizzi@cisco.com/foo'\n" +
                "      type='result'>\n" +
                "    <vcard xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">\n" +
                "      <fn><text>Peter Saint-Andre</text></fn>\n" +
                "      <n><surname>Saint-Andre</surname><given>Peter</given><additional></additional></n>\n" +
                "      <nickname><text>stpeter</text></nickname>\n" +
                "      <nickname><text>psa</text></nickname>\n" +
                "      <photo><uri>https://stpeter.im/images/stpeter_oscon.jpg</uri></photo>\n" +
                "      <bday><date>1966-08-06</date></bday>\n" +
                "      <adr>\n" +
                "        <parameters>\n" +
                "          <type><text>work</text><text>voice</text></type>\n" +
                "          <pref><integer>1</integer></pref>\n" +
                "        </parameters>\n" +
                "        <ext>Suite 600</ext>\n" +
                "        <street>1899 Wynkoop Street</street>\n" +
                "        <locality>Denver</locality>\n" +
                "        <region>CO</region>\n" +
                "        <code>80202</code>\n" +
                "        <country>USA</country>\n" +
                "      </adr>\n" +
                "      <adr>\n" +
                "        <parameters><type><text>home</text></type></parameters>\n" +
                "        <ext></ext>\n" +
                "        <street></street>\n" +
                "        <locality>Parker</locality>\n" +
                "        <region>CO</region>\n" +
                "        <code>80138</code>\n" +
                "        <country>USA</country>\n" +
                "      </adr>\n" +
                "      <tel>\n" +
                "        <parameters>\n" +
                "          <type><text>work</text><text>voice</text></type>\n" +
                "          <pref><integer>1</integer></pref>\n" +
                "        </parameters>\n" +
                "        <uri>tel:+1-303-308-3282</uri>\n" +
                "      </tel>\n" +
                "      <tel>\n" +
                "        <parameters><type><text>work</text><text>fax</text></type></parameters>\n" +
                "        <uri>tel:+1-303-308-3219</uri>\n" +
                "      </tel>\n" +
                "      <tel>\n" +
                "        <parameters>\n" +
                "          <type><text>cell</text><text>voice</text><text>text</text></type>\n" +
                "        </parameters>\n" +
                "        <uri>tel:+1-720-256-6756</uri>\n" +
                "      </tel>\n" +
                "      <tel>\n" +
                "        <parameters><type><text>home</text><text>voice</text></type></parameters>\n" +
                "        <uri>tel:+1-303-555-1212</uri>\n" +
                "      </tel>\n" +
                "      <geo><uri>geo:39.59,-105.01</uri></geo>\n" +
                "      <title><text>Executive Director</text></title>\n" +
                "      <role><text>Patron Saint</text></role>\n" +
                "      <org>\n" +
                "        <parameters><type><text>work</text></type></parameters>\n" +
                "        <text>XMPP Standards Foundation</text>\n" +
                "      </org>\n" +
                "      <url><uri>https://stpeter.im/</uri></url>\n" +
                "      <note>\n" +
                "        <text>More information about me is located on my personal website: https://stpeter.im/</text>\n" +
                "      </note>\n" +
                "      <gender><sex><text>M</text></sex></gender>\n" +
                "      <lang>\n" +
                "        <parameters><pref>1</pref></parameters>\n" +
                "        <language-tag>en</language-tag>\n" +
                "      </lang>\n" +
                "      <email>\n" +
                "        <parameters><type><text>work</text><text>home</text></type></parameters>\n" +
                "        <text>psaintan@cisco.com</text>\n" +
                "      </email>\n" +
                "      <email>\n" +
                "        <parameters><type><text>home</text></type></parameters>\n" +
                "        <text>stpeter@jabber.org</text>\n" +
                "      </email>\n" +
                "      <impp>\n" +
                "        <parameters><type><text>work</text></type></parameters>\n" +
                "        <uri>xmpp:psaintan@cisco.com</uri>\n" +
                "      </impp>\n" +
                "      <impp>\n" +
                "        <parameters><type><text>home</text></type></parameters>\n" +
                "        <uri>xmpp:stpeter@jabber.org</uri>\n" +
                "      </impp>\n" +
                "      <key>\n" +
                "        <uri>https://stpeter.im/stpeter.asc</uri>\n" +
                "      </key>\n" +
                "    </vcard>\n" +
                "  </iq>\n";

        IQ iq = unmarshal(xml, IQ.class);
        VCard4 vCard4 = iq.getExtension(VCard4.class);

        // Test <fn>
        Assert.assertEquals(vCard4.getFormattedName(), "Peter Saint-Andre");

        // Test <n>
        Assert.assertNotNull(vCard4.getName());
        Assert.assertEquals(vCard4.getName().getFamilyName(), "Saint-Andre");
        Assert.assertEquals(vCard4.getName().getGivenName(), "Peter");
        Assert.assertEquals(vCard4.getName().getMiddleName(), "");
        Assert.assertEquals(vCard4.getName().getPrefix(), null);
        Assert.assertEquals(vCard4.getName().getSuffix(), null);

        // Test <nickname>
        //Assert.assertEquals(vCard4.getNickname(), "stpeter");
        Assert.assertEquals(vCard4.getNickname(), "psa");

        // Test <photo>
        Assert.assertEquals(vCard4.getPhotos().size(), 1);
        Assert.assertEquals(vCard4.getPhotos().get(0).getUri(), URI.create("https://stpeter.im/images/stpeter_oscon.jpg"));

        // Test <bday>
        Assert.assertEquals(vCard4.getBirthday(), LocalDate.of(1966, 8, 6));

        // Test <email>
        Assert.assertEquals(vCard4.getEmailAddresses().size(), 2);
        Assert.assertEquals(vCard4.getEmailAddresses().get(0).getParameters().size(), 1);
        Assert.assertTrue(vCard4.getEmailAddresses().get(0).getParameters().get(0) instanceof VCard4.Parameter.TypeParameter);
        VCard4.Parameter.TypeParameter typeParameter = ((VCard4.Parameter.TypeParameter) vCard4.getEmailAddresses().get(0).getParameters().get(0));
        Assert.assertEquals(typeParameter.getTypes().size(), 2);
        Assert.assertTrue(typeParameter.getTypes().containsAll(Arrays.asList(VCard4.Type.WORK, VCard4.Type.HOME)));
        Assert.assertEquals(vCard4.getEmailAddresses().get(0).getEmail(), "psaintan@cisco.com");
        Assert.assertEquals(vCard4.getEmailAddresses().get(1).getEmail(), "stpeter@jabber.org");

        // Test <note>
        Assert.assertEquals(vCard4.getNote(), "More information about me is located on my personal website: https://stpeter.im/");

        Assert.assertNotNull(vCard4);
    }

    @Test
    public void unmarshalVCardSample() throws JAXBException, XMLStreamException, MalformedURLException {

        String xml = "<vcard xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">\n" +
                "       <fn><text>Simon Perreault</text></fn>\n" +
                "       <n>\n" +
                "         <surname>Perreault</surname>\n" +
                "         <given>Simon</given>\n" +
                "         <additional/>\n" +
                "         <prefix/>\n" +
                "         <suffix>ing. jr</suffix>\n" +
                "         <suffix>M.Sc.</suffix>\n" +
                "       </n>\n" +
                "       <bday><date>--0203</date></bday>\n" +
                "       <anniversary>\n" +
                "         <date-time>20090808T1430-0500</date-time>\n" +
                "       </anniversary>\n" +
                "       <gender><sex>M</sex></gender>\n" +
                "       <lang>\n" +
                "         <parameters><pref><integer>1</integer></pref></parameters>\n" +
                "         <language-tag>fr</language-tag>\n" +
                "       </lang>\n" +
                "       <lang>\n" +
                "         <parameters><pref><integer>2</integer></pref></parameters>\n" +
                "         <language-tag>en</language-tag>\n" +
                "       </lang>\n" +
                "       <org>\n" +
                "         <parameters><type><text>work</text></type></parameters>\n" +
                "         <text>Viagenie</text>\n" +
                "       </org>\n" +
                "       <adr>\n" +
                "         <parameters>\n" +
                "           <type><text>work</text></type>\n" +
                "           <label><text>Simon Perreault\n" +
                "   2875 boul. Laurier, suite D2-630\n" +
                "   Quebec, QC, Canada\n" +
                "   G1V 2M2</text></label>\n" +
                "         </parameters>\n" +
                "         <pobox/>\n" +
                "         <ext/>\n" +
                "         <street>2875 boul. Laurier, suite D2-630</street>\n" +
                "         <locality>Quebec</locality>\n" +
                "         <region>QC</region>\n" +
                "         <code>G1V 2M2</code>\n" +
                "         <country>Canada</country>\n" +
                "       </adr>\n" +
                "       <tel>\n" +
                "         <parameters>\n" +
                "           <type>\n" +
                "             <text>work</text>\n" +
                "             <text>voice</text>\n" +
                "           </type>\n" +
                "         </parameters>\n" +
                "         <uri>tel:+1-418-656-9254;ext=102</uri>\n" +
                "       </tel>\n" +
                "       <tel>\n" +
                "         <parameters>\n" +
                "           <type>\n" +
                "             <text>work</text>\n" +
                "             <text>text</text>\n" +
                "             <text>voice</text>\n" +
                "             <text>cell</text>\n" +
                "             <text>video</text>\n" +
                "           </type>\n" +
                "         </parameters>\n" +
                "         <uri>tel:+1-418-262-6501</uri>\n" +
                "       </tel>\n" +
                "       <email>\n" +
                "         <parameters><type><text>work</text></type></parameters>\n" +
                "         <text>simon.perreault@viagenie.ca</text>\n" +
                "       </email>\n" +
                "       <geo>\n" +
                "         <parameters><type><text>work</text></type></parameters>\n" +
                "         <uri>geo:46.766336,-71.28955</uri>\n" +
                "       </geo>\n" +
                "       <key>\n" +
                "         <parameters><type><text>work</text></type></parameters>\n" +
                "         <uri>http://www.viagenie.ca/simon.perreault/simon.asc</uri>\n" +
                "       </key>\n" +
                "       <tz><text>America/Montreal</text></tz>\n" +
                "       <url>\n" +
                "         <parameters><type><text>home</text></type></parameters>\n" +
                "         <uri>http://nomis80.org</uri>\n" +
                "       </url>\n" +
                "     </vcard>\n";

        VCard4 vCard = unmarshal(xml, VCard4.class);

        Assert.assertNotNull(vCard);

        // Names
        Assert.assertEquals(vCard.getFormattedName(), "Simon Perreault");
        Assert.assertEquals(vCard.getName().getFamilyName(), "Perreault");
        Assert.assertEquals(vCard.getName().getGivenName(), "Simon");
        Assert.assertEquals(vCard.getName().getSuffix(), "M.Sc.");

        // Birthday
        // TODO

        // Anniversary
        // TODO

        // Gender
        Assert.assertEquals(vCard.getGender().getSex(), VCard4.Sex.MALE);

        // Languages
        Assert.assertEquals(vCard.getLanguages().size(), 2);
        Assert.assertEquals(vCard.getLanguages().get(0).getLanguage(), "fr");
        Assert.assertEquals(vCard.getLanguages().get(1).getLanguage(), "en");

        // Addresses
        Assert.assertEquals(vCard.getAddresses().size(), 1);
        VCard.Address address = vCard.getAddresses().get(0);
        Assert.assertEquals(address.getCity(), "Quebec");
        Assert.assertEquals(address.getRegion(), "QC");
        Assert.assertEquals(address.getPostalCode(), "G1V 2M2");
        Assert.assertEquals(address.getCountry(), "Canada");
        Assert.assertEquals(address.getStreet(), "2875 boul. Laurier, suite D2-630");

        // Telephone
        Assert.assertEquals(vCard.getTelephoneNumbers().size(), 2);
        Assert.assertEquals(vCard.getTelephoneNumbers().get(0).getNumber(), "tel:+1-418-656-9254;ext=102");
        Assert.assertEquals(vCard.getTelephoneNumbers().get(1).getNumber(), "tel:+1-418-262-6501");


        Assert.assertEquals(vCard.getEmailAddresses().get(0).getEmail(), "simon.perreault@viagenie.ca");
        Assert.assertEquals(vCard.getUrl(), new URL("http://nomis80.org"));

        Assert.assertEquals(vCard.getTimeZone(), ZoneId.of("America/Montreal"));

    }
}
