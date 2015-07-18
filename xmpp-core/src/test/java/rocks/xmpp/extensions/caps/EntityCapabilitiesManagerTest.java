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

package rocks.xmpp.extensions.caps;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.caps.model.EntityCapabilities;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoNode;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class EntityCapabilitiesManagerTest extends XmlTest {
    protected EntityCapabilitiesManagerTest() throws JAXBException, XMLStreamException {
        super(EntityCapabilities.class);
    }

    @Test
    public void unmarshalCaps() throws JAXBException, XMLStreamException {
        String xml = "<c xmlns='http://jabber.org/protocol/caps' \n" +
                "     hash='sha-1'\n" +
                "     node='http://code.google.com/p/exodus'\n" +
                "     ver='QgayPKawpkPSDYmwT/WM94uAlu0='/>\n";
        EntityCapabilities entityCapabilities = unmarshal(xml, EntityCapabilities.class);
        Assert.assertNotNull(entityCapabilities);
        Assert.assertEquals(entityCapabilities.getHashingAlgorithm(), "sha-1");
        Assert.assertEquals(entityCapabilities.getNode(), "http://code.google.com/p/exodus");
        Assert.assertEquals(entityCapabilities.getVerificationString(), "QgayPKawpkPSDYmwT/WM94uAlu0=");
    }

    @Test
    public void testSortIdentities() throws XMLStreamException, JAXBException {

        Identity identity1 = Identity.ofCategoryAndType("AAA", "aaa").withName("name1", "en");
        Identity identity2 = Identity.ofCategoryAndType("AAA", "aaa").withName("name2", "de");
        Identity identity3 = Identity.ofCategoryAndType("AAA", "bbb").withName("name2", "de");
        Identity identity4 = Identity.ofCategoryAndType("BBB", "bbb").withName("name2", "de");
        Identity identity5 = Identity.ofCategoryAndType("BBB", "aaa").withName("name1", "en");
        Identity identity6 = Identity.ofCategoryAndType("CCC", "aaa").withName("name2", "de");

        List<Identity> identities = new ArrayList<>();
        identities.add(identity1);
        identities.add(identity2);
        identities.add(identity3);
        identities.add(identity4);
        identities.add(identity5);
        identities.add(identity6);

        Collections.shuffle(identities);
        identities.sort(null);

        Assert.assertEquals(identities.get(0), identity2);
        Assert.assertEquals(identities.get(1), identity1);
        Assert.assertEquals(identities.get(2), identity3);
        Assert.assertEquals(identities.get(3), identity5);
        Assert.assertEquals(identities.get(4), identity4);
        Assert.assertEquals(identities.get(5), identity6);
    }

    @Test
    public void testSortDataForms() throws XMLStreamException, JAXBException {

        DataForm dataForm1 = new DataForm(DataForm.Type.FORM, Arrays.asList(DataForm.Field.builder().var("ccc").type(DataForm.Field.Type.BOOLEAN).build(),
                DataForm.Field.builder().var("FORM_TYPE").value("aaa").type(DataForm.Field.Type.HIDDEN).build()));

        DataForm dataForm2 = new DataForm(DataForm.Type.FORM, Collections.singleton(DataForm.Field.builder().var("bbb").type(DataForm.Field.Type.BOOLEAN).build()));

        DataForm dataForm3 = new DataForm(DataForm.Type.FORM, Arrays.asList(DataForm.Field.builder().var("FORM_TYPE").value("bbb").type(DataForm.Field.Type.HIDDEN).build(),
                DataForm.Field.builder().var("aaa").type(DataForm.Field.Type.BOOLEAN).build()));

        List<DataForm> dataForms = new ArrayList<>();
        dataForms.add(dataForm1);
        dataForms.add(dataForm2);
        dataForms.add(dataForm3);

        Collections.shuffle(dataForms);
        dataForms.sort(null);

        Assert.assertEquals(dataForms.get(0), dataForm1);
        Assert.assertEquals(dataForms.get(1), dataForm3);
        Assert.assertEquals(dataForms.get(2), dataForm2);
    }

    @Test
    public void testSortDataFormFields() throws XMLStreamException, JAXBException {

        List<DataForm.Field> dataFields = new ArrayList<>();
        DataForm.Field field1 = DataForm.Field.builder().var("ccc").type(DataForm.Field.Type.BOOLEAN).value("ccc").build();
        DataForm.Field field2 = DataForm.Field.builder().var("FORM_TYPE").value("aaa").type(DataForm.Field.Type.HIDDEN).build();
        DataForm.Field field3 = DataForm.Field.builder().var("aaa").type(DataForm.Field.Type.BOOLEAN).build();
        DataForm.Field field4 = DataForm.Field.builder().var("ggg").type(DataForm.Field.Type.BOOLEAN).build();
        DataForm.Field field5 = DataForm.Field.builder().var("eee").type(DataForm.Field.Type.BOOLEAN).build();

        dataFields.add(field1);
        dataFields.add(field2);
        dataFields.add(field3);
        dataFields.add(field4);
        dataFields.add(field5);

        Collections.shuffle(dataFields);
        dataFields.sort(null);

        Assert.assertEquals(dataFields.get(0), field2);
        Assert.assertEquals(dataFields.get(1), field3);
        Assert.assertEquals(dataFields.get(2), field1);
        Assert.assertEquals(dataFields.get(3), field5);
        Assert.assertEquals(dataFields.get(4), field4);
    }

    /**
     * Generation example from <a href="http://xmpp.org/extensions/xep-0115.html#ver-gen-simple">5.2 Simple Generation Example</a>
     *
     * @throws NoSuchAlgorithmException If SHA-1 algorithm could not be found.
     */
    @Test
    public void testVerificationString() throws NoSuchAlgorithmException {
        List<Identity> identities = new ArrayList<>();
        identities.add(Identity.clientPc().withName("Exodus 0.9.1"));

        List<String> features = new ArrayList<>();
        features.add("http://jabber.org/protocol/disco#info");
        features.add("http://jabber.org/protocol/disco#items");
        features.add("http://jabber.org/protocol/muc");
        features.add("http://jabber.org/protocol/caps");

        InfoNode infoNode = new InfoDiscovery(identities, features);
        String verificationString = EntityCapabilities.getVerificationString(infoNode, MessageDigest.getInstance("sha-1"));
        Assert.assertEquals(verificationString, "QgayPKawpkPSDYmwT/WM94uAlu0=");
    }

    /**
     * Generation example from <a href="http://xmpp.org/extensions/xep-0115.html#ver-gen-complex">5.3 Complex Generation Example</a>
     *
     * @throws NoSuchAlgorithmException If SHA-1 algorithm could not be found.
     */
    @Test
    public void testVerificationStringComplex() throws NoSuchAlgorithmException {
        List<Identity> identities = new ArrayList<>();
        identities.add(Identity.clientPc().withName("Psi 0.11", "en"));
        identities.add(Identity.clientPc().withName("P 0.11", "el"));

        List<String> features = new ArrayList<>();
        features.add("http://jabber.org/protocol/caps");
        features.add("http://jabber.org/protocol/disco#info");
        features.add("http://jabber.org/protocol/disco#items");
        features.add("http://jabber.org/protocol/muc");

        DataForm dataForm = new DataForm(DataForm.Type.RESULT, Arrays.asList(
                DataForm.Field.builder().var("FORM_TYPE").value("urn:xmpp:dataforms:softwareinfo").type(DataForm.Field.Type.HIDDEN).build(),
                DataForm.Field.builder().var("ip_version").values(Arrays.asList("ipv4", "ipv6")).type(DataForm.Field.Type.TEXT_SINGLE).build(),
                DataForm.Field.builder().var("os").value("Mac").type(DataForm.Field.Type.TEXT_SINGLE).build(),
                DataForm.Field.builder().var("os_version").value("10.5.1").type(DataForm.Field.Type.TEXT_SINGLE).build(),
                DataForm.Field.builder().var("software").value("Psi").type(DataForm.Field.Type.TEXT_SINGLE).build(),
                DataForm.Field.builder().var("software_version").value("0.11").type(DataForm.Field.Type.TEXT_SINGLE).build()

        ));
        InfoDiscovery infoDiscovery = new InfoDiscovery(identities, features, Collections.singleton(dataForm));
        String verificationString = EntityCapabilities.getVerificationString(infoDiscovery, MessageDigest.getInstance("sha-1"));
        Assert.assertEquals(verificationString, "dsMdhhH+tbCICmoptvSp3x+DafI=");
    }

    @Test
    public void testVerificationStringWithExtendedForm() throws NoSuchAlgorithmException {
        List<Identity> identities = new ArrayList<>();
        identities.add(Identity.clientPc().withName("Exodus 0.9.1"));

        List<String> features = new ArrayList<>();
        features.add("http://jabber.org/protocol/disco#info");
        features.add("http://jabber.org/protocol/disco#items");
        features.add("http://jabber.org/protocol/muc");
        features.add("http://jabber.org/protocol/caps");

        DataForm dataForm1 = new DataForm(DataForm.Type.FORM, Arrays.asList(DataForm.Field.builder().var("ccc").build(),
                DataForm.Field.builder().var("FORM_TYPE").type(DataForm.Field.Type.HIDDEN).build()));

        DataForm dataForm2 = new DataForm(DataForm.Type.FORM, Collections.singleton(
                DataForm.Field.builder().var("bbb").type(DataForm.Field.Type.BOOLEAN).build()
        ));

        DataForm dataForm3 = new DataForm(DataForm.Type.FORM, Arrays.asList(
                DataForm.Field.builder().var("FORM_TYPE").type(DataForm.Field.Type.HIDDEN).build(),
                DataForm.Field.builder().var("aaa").type(DataForm.Field.Type.BOOLEAN).build()
        ));

        InfoDiscovery infoDiscovery = new InfoDiscovery(identities, features, Arrays.asList(dataForm1, dataForm2, dataForm3));
        String verificationString = EntityCapabilities.getVerificationString(infoDiscovery, MessageDigest.getInstance("sha-1"));
        Assert.assertEquals(verificationString, "EwaG/3/PLTavYdlrevpQmoqM3nw=");
    }

    @Test
    public void testVerificationString3() throws NoSuchAlgorithmException {
        List<Identity> identities = new ArrayList<>();
        identities.add(Identity.clientPc());

        List<String> features = new ArrayList<>();
        features.add("http://jabber.org/protocol/disco#info");
        features.add("http://jabber.org/protocol/disco#items");
        features.add("urn:xmpp:ping");
        features.add("jabber:iq:last");
        features.add("jabber:iq:version");
        features.add("http://jabber.org/protocol/ibb");
        features.add("vcard-temp");
        features.add("urn:xmpp:time");
        features.add("http://jabber.org/protocol/shim");
        features.add("http://jabber.org/protocol/caps");

        InfoNode infoNode = new InfoDiscovery(identities, features);
        String verificationString = EntityCapabilities.getVerificationString(infoNode, MessageDigest.getInstance("sha-1"));
        Assert.assertEquals(verificationString, "40K55pBx86cs2cR44flP35MpLCk=");
    }
}
