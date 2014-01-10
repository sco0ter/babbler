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

package org.xmpp.extension.entitycapabilities;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.extension.servicediscovery.info.Identity;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class EntityCapabilitiesManagerTest extends BaseTest {

    @Test
    public void testSortIdentities() throws XMLStreamException, JAXBException {
        EntityCapabilitiesManager entityCapabilitiesManager = new EntityCapabilitiesManager();

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
        entityCapabilitiesManager.sortIdentities(identities);

        Assert.assertEquals(identities.get(0), identity2);
        Assert.assertEquals(identities.get(1), identity1);
        Assert.assertEquals(identities.get(2), identity3);
        Assert.assertEquals(identities.get(3), identity5);
        Assert.assertEquals(identities.get(4), identity4);
        Assert.assertEquals(identities.get(5), identity6);
    }

    /**
     * Generation example from <a href="http://xmpp.org/extensions/xep-0115.html#ver-gen-simple">5.2 Simple Generation Example</a>
     */
    @Test
    public void testVerificationString() {
        List<Identity> identities = new ArrayList<>();
        identities.add(new Identity("client", "pc", "Exodus 0.9.1"));

        List<Feature> features = new ArrayList<>();
        features.add(new Feature("http://jabber.org/protocol/disco#info"));
        features.add(new Feature("http://jabber.org/protocol/disco#items"));
        features.add(new Feature("http://jabber.org/protocol/muc"));
        features.add(new Feature("http://jabber.org/protocol/caps"));

        EntityCapabilitiesManager entityCapabilitiesManager = new EntityCapabilitiesManager();
        String verificationString = entityCapabilitiesManager.getVerificationString(identities, features);
        Assert.assertEquals(verificationString, "QgayPKawpkPSDYmwT/WM94uAlu0=");
    }
}
