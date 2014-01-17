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

package org.xmpp.extension.version;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.MockServer;
import org.xmpp.TestConnection;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class SoftwareVersionTest extends BaseTest {

    @Test
    public void unmarshalSoftwareVersionRequest() throws XMLStreamException, JAXBException {
        String xml = "<iq\n" +
                "    type='get'\n" +
                "    from='romeo@montague.net/orchard'\n" +
                "    to='juliet@capulet.com/balcony'\n" +
                "    id='version_1'>\n" +
                "  <query xmlns='jabber:iq:version'/>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        SoftwareVersion softwareVersion = iq.getExtension(SoftwareVersion.class);
        Assert.assertNotNull(softwareVersion);
    }

    @Test
    public void unmarshalSearchResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq\n" +
                "    type='result'\n" +
                "    to='romeo@montague.net/orchard'\n" +
                "    from='juliet@capulet.com/balcony'\n" +
                "    id='version_1'>\n" +
                "  <query xmlns='jabber:iq:version'>\n" +
                "    <name>Exodus</name>\n" +
                "    <version>0.7.0.4</version>\n" +
                "    <os>Windows-XP 5.01.2600</os>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        SoftwareVersion softwareVersion = iq.getExtension(SoftwareVersion.class);
        Assert.assertNotNull(softwareVersion);
        Assert.assertEquals(softwareVersion.getName(), "Exodus");
        Assert.assertEquals(softwareVersion.getVersion(), "0.7.0.4");
        Assert.assertEquals(softwareVersion.getOs(), "Windows-XP 5.01.2600");
    }

    @Test
    public void marshalSoftwareVersion() throws JAXBException, XMLStreamException, IOException {
        SoftwareVersion softwareVersion = new SoftwareVersion("Babbler", "1.0");
        String xml = marshall(softwareVersion);
        Assert.assertEquals("<query xmlns=\"jabber:iq:version\"><name>Babbler</name><version>1.0</version><os>" + System.getProperty("os.name") + "</os></query>", xml);
    }

    @Test
    public void testSoftwareVersionManager() throws IOException, TimeoutException {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        new TestConnection(JULIET, mockServer);
        TestConnection connection2 = new TestConnection(JULIET, mockServer);
        connection2.getExtensionManager(SoftwareVersionManager.class).setSoftwareVersion(new SoftwareVersion("Name", "Version"));
        SoftwareVersionManager softwareVersionManager = connection1.getExtensionManager(SoftwareVersionManager.class);
        SoftwareVersion softwareVersion = softwareVersionManager.getSoftwareVersion(JULIET);
        Assert.assertNotNull(softwareVersion);
        Assert.assertEquals(softwareVersion.getName(), "Name");
        Assert.assertEquals(softwareVersion.getVersion(), "Version");
    }

    @Test
    public void testSoftwareVersionManagerIfDisabled() throws IOException, TimeoutException {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        TestConnection connection2 = new TestConnection(JULIET, mockServer);
        connection2.getExtensionManager(SoftwareVersionManager.class).setEnabled(false);
        SoftwareVersionManager softwareVersionManager = connection1.getExtensionManager(SoftwareVersionManager.class);
        SoftwareVersion softwareVersion = softwareVersionManager.getSoftwareVersion(JULIET);
        Assert.assertNull(softwareVersion);
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestConnection connection1 = new TestConnection();
        SoftwareVersionManager softwareVersionManager = connection1.getExtensionManager(SoftwareVersionManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(softwareVersionManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("jabber:iq:version");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
        softwareVersionManager.setEnabled(false);
        Assert.assertFalse(softwareVersionManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
