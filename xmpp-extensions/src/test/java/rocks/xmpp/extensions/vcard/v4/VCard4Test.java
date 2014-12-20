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

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URI;
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
        Assert.assertEquals(vCard4.getFormattedNames().get(0), "Peter Saint-Andre");

        // Test <n>
        Assert.assertNotNull(vCard4.getName());
        Assert.assertEquals(vCard4.getName().getSurname(), "Saint-Andre");
        Assert.assertEquals(vCard4.getName().getGivenName(), "Peter");
        Assert.assertEquals(vCard4.getName().getAdditionalName(), "");
        Assert.assertEquals(vCard4.getName().getPrefix(), null);
        Assert.assertEquals(vCard4.getName().getSuffix(), null);

        // Test <nickname>
        Assert.assertEquals(vCard4.getNicknames().get(0), "stpeter");
        Assert.assertEquals(vCard4.getNicknames().get(1), "psa");

        // Test <photo>
        Assert.assertEquals(vCard4.getPhotos().size(), 1);
        Assert.assertEquals(vCard4.getPhotos().get(0).getUri(), URI.create("https://stpeter.im/images/stpeter_oscon.jpg"));

        // Test <bday>
        Assert.assertNotNull(vCard4.getBirthday());

        // Test <email>
        Assert.assertEquals(vCard4.getEmailAddresses().size(), 2);
        Assert.assertEquals(vCard4.getEmailAddresses().get(0).getParameters().size(), 1);
        Assert.assertTrue(vCard4.getEmailAddresses().get(0).getParameters().get(0) instanceof VCard4.Parameter.TypeParameter);
        VCard4.Parameter.TypeParameter typeParameter = ((VCard4.Parameter.TypeParameter) vCard4.getEmailAddresses().get(0).getParameters().get(0));
        Assert.assertEquals(typeParameter.getTypes().size(), 2);
        Assert.assertTrue(typeParameter.getTypes().containsAll(Arrays.asList(VCard4.Type.WORK, VCard4.Type.HOME)));
        Assert.assertEquals(vCard4.getEmailAddresses().get(0).getEmailAddress(), "psaintan@cisco.com");
        Assert.assertEquals(vCard4.getEmailAddresses().get(1).getEmailAddress(), "stpeter@jabber.org");

        // Test <note>
        Assert.assertEquals(vCard4.getNotes().size(), 1);
        Assert.assertEquals(vCard4.getNotes().get(0), "More information about me is located on my personal website: https://stpeter.im/");

        Assert.assertNotNull(vCard4);
    }
}
