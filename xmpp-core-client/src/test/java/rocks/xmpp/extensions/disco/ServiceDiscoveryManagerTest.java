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

package rocks.xmpp.extensions.disco;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class ServiceDiscoveryManagerTest extends BaseTest {

    @Test
    public void testFeatureEquals() {
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addFeature(new Feature("http://jabber.org/protocol/muc"));
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(new Feature("http://jabber.org/protocol/muc")));
    }

    @Test
    public void testItemsEquals() {
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addIdentity(new Identity("conference", "text", "name1", "en"));
        Assert.assertTrue(serviceDiscoveryManager.getIdentities().contains(new Identity("conference", "text", "name2", "en")));
    }

    @Test
    public void testInfoDiscovery() throws XmppException {
        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        InfoNode result = serviceDiscoveryManager.discoverInformation(JULIET);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getFeatures().size() > 1);
        //  Every entity MUST have at least one identity
        Assert.assertTrue(result.getIdentities().size() > 0);
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestXmppSession connection1 = new TestXmppSession();
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(serviceDiscoveryManager.isEnabled());
        Feature featureInfo = new Feature("http://jabber.org/protocol/disco#info");
        Feature featureItems = new Feature("http://jabber.org/protocol/disco#items");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(featureInfo));
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(featureItems));
        serviceDiscoveryManager.setEnabled(false);
        Assert.assertFalse(serviceDiscoveryManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(featureInfo));
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(featureItems));

        // Enable it by adding the features.
        serviceDiscoveryManager.addFeature(featureInfo);
        serviceDiscoveryManager.addFeature(featureItems);
        Assert.assertTrue(serviceDiscoveryManager.isEnabled());
    }

    @Test
    public void testItemDiscovery() throws XmppException {

        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.setItemProvider(new DefaultItemProvider(Arrays.asList(new Item(Jid.valueOf("test"), "root", "name"))));
        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager2 = connection2.getManager(ServiceDiscoveryManager.class);
        ItemNode result = serviceDiscoveryManager2.discoverItems(ROMEO);
        Assert.assertEquals(result.getItems().size(), 1);
        Assert.assertEquals(result.getItems().get(0).getNode(), "root");
        Assert.assertEquals(result.getItems().get(0).getName(), "name");
        Assert.assertEquals(result.getItems().get(0).getJid(), Jid.valueOf("test"));
    }

    @Test
    public void testItemDiscoveryWithNode() throws XmppException {

        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);

        DefaultItemProvider defaultItemProvider = new DefaultItemProvider(Arrays.asList(new Item(Jid.valueOf("test"), "node1")));
        serviceDiscoveryManager.setItemProvider("node1", defaultItemProvider);
        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager2 = connection2.getManager(ServiceDiscoveryManager.class);
        ItemNode result = serviceDiscoveryManager2.discoverItems(ROMEO, "node1");
        Assert.assertEquals(result.getItems().size(), 1);
        Assert.assertEquals(result.getItems().get(0).getNode(), "node1");
    }

    @Test
    public void testItemDiscoveryWithRsm() throws XmppException {

        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);

        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new Item(Jid.valueOf("test"), "item" + i));
        }
        serviceDiscoveryManager.setItemProvider(new DefaultItemProvider(items));

        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager2 = connection2.getManager(ServiceDiscoveryManager.class);
        ItemNode resultItemCount = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forItemCount());
        Assert.assertTrue(resultItemCount.getItems().isEmpty());
        Assert.assertEquals(resultItemCount.getResultSetManagement().getItemCount(), Integer.valueOf(100));

        ItemNode result = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forLimit(10));
        Assert.assertEquals(result.getItems().size(), 10);
        Assert.assertEquals(result.getItems().get(0).getNode(), "item0");
        Assert.assertEquals(result.getResultSetManagement().getItemCount(), Integer.valueOf(100));
        Assert.assertEquals(result.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(0));

        ItemNode result2 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forLimit(10, 20));
        Assert.assertEquals(result2.getItems().size(), 10);
        Assert.assertEquals(result2.getItems().get(0).getNode(), "item20");
        Assert.assertEquals(result2.getResultSetManagement().getItemCount(), Integer.valueOf(100));
        Assert.assertEquals(result2.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(20));

        ItemNode page1 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forNextPage(10, result2.getItems().get(result2.getItems().size() - 1).getId()));
        Assert.assertEquals(page1.getItems().size(), 10);
        Assert.assertEquals(page1.getItems().get(0).getNode(), "item30");
        Assert.assertEquals(page1.getResultSetManagement().getItemCount(), Integer.valueOf(100));
        Assert.assertEquals(page1.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(30));

        ItemNode page2 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forNextPage(10, page1.getItems().get(page1.getItems().size() - 1).getId()));
        Assert.assertEquals(page2.getItems().size(), 10);
        Assert.assertEquals(page2.getItems().get(0).getNode(), "item40");
        Assert.assertEquals(page2.getResultSetManagement().getItemCount(), Integer.valueOf(100));
        Assert.assertEquals(page2.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(40));

    }

    @Test
    public void testItemDiscoveryWithPaging() throws XmppException {

        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            items.add(new Item(Jid.valueOf("test"), "item" + i));
        }
        serviceDiscoveryManager.setItemProvider(new DefaultItemProvider(items));
        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager2 = connection2.getManager(ServiceDiscoveryManager.class);
        ItemNode resultItemCount = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forItemCount());
        Assert.assertTrue(resultItemCount.getItems().isEmpty());
        Assert.assertEquals(resultItemCount.getResultSetManagement().getItemCount(), Integer.valueOf(30));

        ItemNode page1 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forLimit(10));
        Assert.assertEquals(page1.getItems().size(), 10);
        Assert.assertEquals(page1.getItems().get(0).getNode(), "item0");
        Assert.assertEquals(page1.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page1.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(0));

        ItemNode page2 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forNextPage(10, page1.getItems().get(page1.getItems().size() - 1).getId()));
        Assert.assertEquals(page2.getItems().size(), 10);
        Assert.assertEquals(page2.getItems().get(0).getNode(), "item10");
        Assert.assertEquals(page2.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page2.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(10));

        ItemNode page3 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forNextPage(10, page2.getItems().get(page2.getItems().size() - 1).getId()));
        Assert.assertEquals(page3.getItems().size(), 10);
        Assert.assertEquals(page3.getItems().get(0).getNode(), "item20");
        Assert.assertEquals(page3.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page3.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(20));

        // Empty page.
        ItemNode page4 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forNextPage(10, page3.getItems().get(page3.getItems().size() - 1).getId()));
        Assert.assertEquals(page4.getItems().size(), 0);

        // Now page backwards
        ItemNode page5 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forPreviousPage(10, page3.getItems().get(0).getId()));
        Assert.assertEquals(page5.getItems().size(), 10);
        Assert.assertEquals(page5.getItems().get(0).getNode(), "item10");
        Assert.assertEquals(page5.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page5.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(10));

        ItemNode page6 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forPreviousPage(5, page5.getItems().get(0).getId()));
        Assert.assertEquals(page6.getItems().size(), 5);
        Assert.assertEquals(page6.getItems().get(0).getNode(), "item5");
        Assert.assertEquals(page6.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page6.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(5));
    }

    @Test
    public void testPropertyChangeHandler() {
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        final int[] listenerCalled = {0};
        serviceDiscoveryManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                listenerCalled[0]++;
            }
        });
        serviceDiscoveryManager.addFeature(new Feature("dummy"));
        serviceDiscoveryManager.removeFeature(new Feature("dummy"));
        serviceDiscoveryManager.addIdentity(new Identity("cat", "type"));
        serviceDiscoveryManager.removeIdentity(new Identity("cat", "type"));
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        serviceDiscoveryManager.addExtension(dataForm);
        serviceDiscoveryManager.removeExtension(dataForm);
        if (listenerCalled[0] != 6) {
            Assert.fail();
        }
    }

}
