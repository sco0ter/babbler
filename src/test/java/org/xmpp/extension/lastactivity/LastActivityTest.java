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

package org.xmpp.extension.lastactivity;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class LastActivityTest extends BaseTest {

    @Test
    public void unmarshalLastActivityResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq from='juliet@capulet.com' \n" +
                "    id='last1'\n" +
                "    to='romeo@montague.net/orchard'\n" +
                "    type='result'>\n" +
                "  <query xmlns='jabber:iq:last' seconds='903'/>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        LastActivity lastActivity = iq.getExtension(LastActivity.class);
        Assert.assertNotNull(lastActivity);
        Assert.assertEquals(lastActivity.getSeconds(), 903);
    }

    @Test
    public void testLastActivityManager() {
        LastActivityManager lastActivityManager = connection.getExtensionManager(LastActivityManager.class);
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);

        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(LastActivityManager.feature));

        lastActivityManager.setEnabled(false);
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(LastActivityManager.feature));

    }
}
