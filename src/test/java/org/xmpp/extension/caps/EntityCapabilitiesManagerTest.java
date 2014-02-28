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

package org.xmpp.extension.caps;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoDiscovery;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.shim.HeaderManager;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class EntityCapabilitiesManagerTest extends BaseTest {

    @Test
    public void testSortIdentities() throws XMLStreamException, JAXBException {

        Identity identity1 = new Identity("AAA", "aaa", "name1", "en");
        Identity identity2 = new Identity("AAA", "aaa", "name2", "de");
        Identity identity3 = new Identity("AAA", "bbb", "name2", "de");
        Identity identity4 = new Identity("BBB", "bbb", "name2", "de");
        Identity identity5 = new Identity("BBB", "aaa", "name1", "en");
        Identity identity6 = new Identity("CCC", "aaa", "name2", "de");

        List<Identity> identities = new ArrayList<>();
        identities.add(identity1);
        identities.add(identity2);
        identities.add(identity3);
        identities.add(identity4);
        identities.add(identity5);
        identities.add(identity6);

        Collections.shuffle(identities);
        Collections.sort(identities);

        Assert.assertEquals(identities.get(0), identity2);
        Assert.assertEquals(identities.get(1), identity1);
        Assert.assertEquals(identities.get(2), identity3);
        Assert.assertEquals(identities.get(3), identity5);
        Assert.assertEquals(identities.get(4), identity4);
        Assert.assertEquals(identities.get(5), identity6);
    }

    @Test
    public void testSortDataForms() throws XMLStreamException, JAXBException {

        DataForm dataForm1 = new DataForm(DataForm.Type.FORM);
        dataForm1.getFields().add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, "ccc"));
        dataForm1.getFields().add(new DataForm.Field(DataForm.Field.Type.HIDDEN, "FORM_TYPE", "aaa"));

        DataForm dataForm2 = new DataForm(DataForm.Type.FORM);
        dataForm2.getFields().add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, "bbb"));

        DataForm dataForm3 = new DataForm(DataForm.Type.FORM);
        dataForm3.getFields().add(new DataForm.Field(DataForm.Field.Type.HIDDEN, "FORM_TYPE", "bbb"));
        dataForm3.getFields().add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, "aaa"));

        List<DataForm> dataForms = new ArrayList<>();
        dataForms.add(dataForm1);
        dataForms.add(dataForm2);
        dataForms.add(dataForm3);

        Collections.shuffle(dataForms);
        Collections.sort(dataForms);

        Assert.assertEquals(dataForms.get(0), dataForm1);
        Assert.assertEquals(dataForms.get(1), dataForm3);
        Assert.assertEquals(dataForms.get(2), dataForm2);
    }

    @Test
    public void testSortDataFormFields() throws XMLStreamException, JAXBException {

        List<DataForm.Field> dataFields = new ArrayList<>();
        DataForm.Field field1 = new DataForm.Field(DataForm.Field.Type.BOOLEAN, "ccc");
        DataForm.Field field2 = new DataForm.Field(DataForm.Field.Type.HIDDEN, "FORM_TYPE", "aaa");
        DataForm.Field field3 = new DataForm.Field(DataForm.Field.Type.BOOLEAN, "aaa");
        DataForm.Field field4 = new DataForm.Field(DataForm.Field.Type.BOOLEAN, "ggg");
        DataForm.Field field5 = new DataForm.Field(DataForm.Field.Type.BOOLEAN, "eee");

        dataFields.add(field1);
        dataFields.add(field2);
        dataFields.add(field3);
        dataFields.add(field4);
        dataFields.add(field5);

        Collections.shuffle(dataFields);
        Collections.sort(dataFields);

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
        identities.add(new Identity("client", "pc", "Exodus 0.9.1"));

        List<Feature> features = new ArrayList<>();
        features.add(new Feature("http://jabber.org/protocol/disco#info"));
        features.add(new Feature("http://jabber.org/protocol/disco#items"));
        features.add(new Feature("http://jabber.org/protocol/muc"));
        features.add(new Feature("http://jabber.org/protocol/caps"));

        InfoNode infoNode = new InfoDiscovery();
        infoNode.getFeatures().addAll(features);
        infoNode.getIdentities().addAll(identities);
        String verificationString = EntityCapabilitiesManager.getVerificationString(infoNode, MessageDigest.getInstance("sha-1"));
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
        identities.add(new Identity("client", "pc", "Psi 0.11", "en"));
        identities.add(new Identity("client", "pc", "P 0.11", "el"));

        List<Feature> features = new ArrayList<>();
        features.add(new Feature("http://jabber.org/protocol/caps"));
        features.add(new Feature("http://jabber.org/protocol/disco#info"));
        features.add(new Feature("http://jabber.org/protocol/disco#items"));
        features.add(new Feature("http://jabber.org/protocol/muc"));

        DataForm dataForm = new DataForm(DataForm.Type.RESULT);
        dataForm.getFields().add(new DataForm.Field(DataForm.Field.Type.HIDDEN, "FORM_TYPE", "urn:xmpp:dataforms:softwareinfo"));
        dataForm.getFields().add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, "ip_version", "ipv4", "ipv6"));
        dataForm.getFields().add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, "os", "Mac"));
        dataForm.getFields().add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, "os_version", "10.5.1"));
        dataForm.getFields().add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, "software", "Psi"));
        dataForm.getFields().add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, "software_version", "0.11"));

        InfoDiscovery infoDiscovery = new InfoDiscovery();
        infoDiscovery.getFeatures().addAll(features);
        infoDiscovery.getIdentities().addAll(identities);
        infoDiscovery.getExtensions().add(dataForm);
        String verificationString = EntityCapabilitiesManager.getVerificationString(infoDiscovery, MessageDigest.getInstance("sha-1"));
        Assert.assertEquals(verificationString, "dsMdhhH+tbCICmoptvSp3x+DafI=");
    }

    @Test
    public void testVerificationStringWithExtendedForm() throws NoSuchAlgorithmException {
        List<Identity> identities = new ArrayList<>();
        identities.add(new Identity("client", "pc", "Exodus 0.9.1"));

        List<Feature> features = new ArrayList<>();
        features.add(new Feature("http://jabber.org/protocol/disco#info"));
        features.add(new Feature("http://jabber.org/protocol/disco#items"));
        features.add(new Feature("http://jabber.org/protocol/muc"));
        features.add(new Feature("http://jabber.org/protocol/caps"));

        DataForm dataForm1 = new DataForm(DataForm.Type.FORM);
        dataForm1.getFields().add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, "ccc"));
        dataForm1.getFields().add(new DataForm.Field(DataForm.Field.Type.HIDDEN, "FORM_TYPE"));

        DataForm dataForm2 = new DataForm(DataForm.Type.FORM);
        dataForm2.getFields().add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, "bbb"));

        DataForm dataForm3 = new DataForm(DataForm.Type.FORM);
        dataForm3.getFields().add(new DataForm.Field(DataForm.Field.Type.HIDDEN, "FORM_TYPE"));
        dataForm3.getFields().add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, "aaa"));

        InfoDiscovery infoDiscovery = new InfoDiscovery();
        infoDiscovery.getFeatures().addAll(features);
        infoDiscovery.getIdentities().addAll(identities);
        infoDiscovery.getExtensions().add(dataForm1);
        infoDiscovery.getExtensions().add(dataForm2);
        infoDiscovery.getExtensions().add(dataForm3);

        String verificationString = EntityCapabilitiesManager.getVerificationString(infoDiscovery, MessageDigest.getInstance("sha-1"));
        Assert.assertEquals(verificationString, "EwaG/3/PLTavYdlrevpQmoqM3nw=");
    }

    @Test
    public void testCapabilitiesUpdate() {
        HeaderManager headerManager = connection.getExtensionManager(HeaderManager.class);
        headerManager.setEnabled(true);

    }

    @Test
    public void testVerificationString3() throws NoSuchAlgorithmException {
        List<Identity> identities = new ArrayList<>();
        identities.add(new Identity("client", "pc"));

        List<Feature> features = new ArrayList<>();
        features.add(new Feature("http://jabber.org/protocol/disco#info"));
        features.add(new Feature("http://jabber.org/protocol/disco#items"));
        features.add(new Feature("urn:xmpp:ping"));
        features.add(new Feature("jabber:iq:last"));
        features.add(new Feature("jabber:iq:version"));
        features.add(new Feature("http://jabber.org/protocol/ibb"));
        features.add(new Feature("vcard-temp"));
        features.add(new Feature("urn:xmpp:time"));
        features.add(new Feature("http://jabber.org/protocol/shim"));
        features.add(new Feature("http://jabber.org/protocol/caps"));

        InfoNode infoNode = new InfoDiscovery();
        infoNode.getFeatures().addAll(features);
        infoNode.getIdentities().addAll(identities);
        String verificationString = EntityCapabilitiesManager.getVerificationString(infoNode, MessageDigest.getInstance("sha-1"));
        Assert.assertEquals(verificationString, "40K55pBx86cs2cR44flP35MpLCk=");
    }
}
