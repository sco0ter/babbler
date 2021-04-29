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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.client.ClientServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.items.DiscoverableItem;
import rocks.xmpp.extensions.disco.model.items.ItemElement;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.rsm.ResultSetProvider;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;

/**
 * @author Christian Schudt
 */
public class ServiceDiscoveryManagerTest extends BaseTest {

    @Test
    public void testThreadSafety() {
        Map<Integer, DiscoverableItem> collection = new ConcurrentSkipListMap<>();

        ResultSetProvider<DiscoverableItem> resultSetProvider = ResultSetProvider.forItems(collection.values());
        for (int i = 0; i < 1000; i++) {
            collection.put(i,
                    DiscoverableItem.from(new ItemElement(Jid.of("test"), "node", "name"), Integer.toString(i)));
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 1000; i > 0; i--) {
            final int finalI = i;
            executor.execute(() -> collection.remove(finalI));
            executor.execute(() -> resultSetProvider.indexOf(Integer.toString(finalI)));
            executor.execute(() -> resultSetProvider.getItems(0, collection.size()));
        }
    }

    @Test
    public void testThreadSafety2() {
        Collection<DiscoverableItem> collection = Collections.synchronizedCollection(new ArrayDeque<>());

        ResultSetProvider<DiscoverableItem> resultSetProvider = ResultSetProvider.forItems(collection);
        for (int i = 0; i < 1000; i++) {
            collection.add(DiscoverableItem.from(new ItemElement(Jid.of("test"), "node", "name"), Integer.toString(i)));
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 1000; i > 0; i--) {
            final int finalI = i;
            executor.execute(() -> collection.add(DiscoverableItem
                    .from(new ItemElement(Jid.of("test"), "node", "name"), Integer.toString(finalI))));
            executor.execute(() -> resultSetProvider.indexOf(Integer.toString(finalI)));
            executor.execute(() -> resultSetProvider.getItems(0, collection.size()));
        }
    }

    @Test
    public void testFeatureEquals() {
        ClientServiceDiscoveryManager serviceDiscoveryManager =
                xmppSession.getManager(ClientServiceDiscoveryManager.class);
        serviceDiscoveryManager.addFeature("http://jabber.org/protocol/muc");
        Assert.assertTrue(
                serviceDiscoveryManager.getDefaultInfo().getFeatures().contains("http://jabber.org/protocol/muc"));
    }

    @Test
    public void testItemsEquals() {
        // Tests if two Identities are equal although their name is different. That is because there must not be multiple identities with the same category+type+xml:lang but different names.
        // From XEP-0030: the <query/> element MAY include multiple <identity/> elements with the same category+type but with different 'xml:lang' values, however the <query/> element MUST NOT include multiple <identity/> elements with the same category+type+xml:lang but with different 'name' values
        ClientServiceDiscoveryManager serviceDiscoveryManager =
                xmppSession.getManager(ClientServiceDiscoveryManager.class);
        serviceDiscoveryManager
                .addIdentity(Identity.ofCategoryAndType("conference", "text").withName("name1", Locale.ENGLISH));
        Assert.assertTrue(serviceDiscoveryManager.getIdentities()
                .contains(Identity.ofCategoryAndType("conference", "text").withName("name2", Locale.ENGLISH)));
    }

    @Test
    public void testInfoDiscovery() throws ExecutionException, InterruptedException {
        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        DiscoverableInfo result = serviceDiscoveryManager.discoverInformation(JULIET).get();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getFeatures().size() > 1);
        //  Every entity MUST have at least one identity
        Assert.assertTrue(result.getIdentities().size() > 0);
    }

    @Test
    public void testServiceDiscoveryEntry() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(serviceDiscoveryManager.isEnabled());
        String featureInfo = "http://jabber.org/protocol/disco#info";
        String featureItems = "http://jabber.org/protocol/disco#items";
        DiscoverableInfo discoverableInfo = serviceDiscoveryManager.discoverInformation(JULIET).get();
        Assert.assertTrue(discoverableInfo.getFeatures().contains(featureInfo));
        Assert.assertTrue(discoverableInfo.getFeatures().contains(featureItems));
        Assert.assertTrue(serviceDiscoveryManager.isEnabled());
    }

    @Test
    public void testItemDiscovery() throws ExecutionException, InterruptedException {

        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addItemProvider((to, from, node, locale) -> ResultSetProvider.forItems(Collections
                .singletonList(DiscoverableItem.from(new ItemElement(Jid.of("test"), "root", "name"), "name"))));
        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager2 = connection2.getManager(ServiceDiscoveryManager.class);
        ItemNode result = serviceDiscoveryManager2.discoverItems(ROMEO).get();
        Assert.assertEquals(result.getItems().size(), 1);
        Assert.assertEquals(result.getItems().get(0).getNode(), "root");
        Assert.assertEquals(result.getItems().get(0).getName(), "name");
        Assert.assertEquals(result.getItems().get(0).getJid(), Jid.of("test"));
    }

    @Test
    public void testItemDiscoveryWithNode() throws ExecutionException, InterruptedException {

        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);

        ResultSetProvider<DiscoverableItem> defaultItemProvider = ResultSetProvider.forItems(Collections
                .singletonList(DiscoverableItem.from(new ItemElement(Jid.of("test"), "node1", "name1"), "node1")));
        serviceDiscoveryManager.addItemProvider((to, from, node, locale) -> defaultItemProvider);
        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager2 = connection2.getManager(ServiceDiscoveryManager.class);
        ItemNode result = serviceDiscoveryManager2.discoverItems(ROMEO, "node1").get();
        Assert.assertEquals(result.getItems().size(), 1);
        Assert.assertEquals(result.getItems().get(0).getNode(), "node1");
    }

    @Test
    public void testItemDiscoveryWithRsm() throws ExecutionException, InterruptedException {

        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);

        List<DiscoverableItem> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(DiscoverableItem.from(new ItemElement(Jid.of("test"), "item" + i, "item" + i), "item" + i));
        }
        serviceDiscoveryManager.addItemProvider((to, from, node, locale) -> ResultSetProvider.forItems(items));

        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager2 = connection2.getManager(ServiceDiscoveryManager.class);
        ItemNode resultItemCount =
                serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forItemCount()).get();
        Assert.assertTrue(resultItemCount.getItems().isEmpty());
        Assert.assertEquals(resultItemCount.getResultSetManagement().getItemCount(), Integer.valueOf(100));

        ItemNode result = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forFirstPage(10)).get();
        Assert.assertEquals(result.getItems().size(), 10);
        Assert.assertEquals(result.getItems().get(0).getNode(), "item0");
        Assert.assertEquals(result.getResultSetManagement().getItemCount(), Integer.valueOf(100));
        Assert.assertEquals(result.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(0));

        ItemNode result2 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forLimit(10, 20)).get();
        Assert.assertEquals(result2.getItems().size(), 10);
        Assert.assertEquals(result2.getItems().get(0).getNode(), "item20");
        Assert.assertEquals(result2.getResultSetManagement().getItemCount(), Integer.valueOf(100));
        Assert.assertEquals(result2.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(20));

        ItemNode page1 = serviceDiscoveryManager2.discoverItems(ROMEO,
                ResultSetManagement.forNextPage(10, result2.getItems().get((result2.getItems().size() - 1)).getName()))
                .get();
        Assert.assertEquals(page1.getItems().size(), 10);
        Assert.assertEquals(page1.getItems().get(0).getNode(), "item30");
        Assert.assertEquals(page1.getResultSetManagement().getItemCount(), Integer.valueOf(100));
        Assert.assertEquals(page1.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(30));

        ItemNode page2 = serviceDiscoveryManager2.discoverItems(ROMEO,
                ResultSetManagement.forNextPage(10, page1.getItems().get(page1.getItems().size() - 1).getName())).get();
        Assert.assertEquals(page2.getItems().size(), 10);
        Assert.assertEquals(page2.getItems().get(0).getNode(), "item40");
        Assert.assertEquals(page2.getResultSetManagement().getItemCount(), Integer.valueOf(100));
        Assert.assertEquals(page2.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(40));
    }

    @Test
    public void testItemDiscoveryWithPaging() throws ExecutionException, InterruptedException {

        MockServer mockServer = new MockServer();
        TestXmppSession connection1 = new TestXmppSession(ROMEO, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getManager(ServiceDiscoveryManager.class);
        List<DiscoverableItem> items = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            items.add(DiscoverableItem.from(new ItemElement(Jid.of("test"), "item" + i, "item" + i), "item" + i));
        }
        serviceDiscoveryManager.addItemProvider((to, from, node, locale) -> ResultSetProvider.forItems(items));
        TestXmppSession connection2 = new TestXmppSession(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager2 = connection2.getManager(ServiceDiscoveryManager.class);
        ItemNode resultItemCount =
                serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forItemCount()).get();
        Assert.assertTrue(resultItemCount.getItems().isEmpty());
        Assert.assertEquals(resultItemCount.getResultSetManagement().getItemCount(), Integer.valueOf(30));

        ItemNode page1 = serviceDiscoveryManager2.discoverItems(ROMEO, ResultSetManagement.forFirstPage(10)).get();
        Assert.assertEquals(page1.getItems().size(), 10);
        Assert.assertEquals(page1.getItems().get(0).getNode(), "item0");
        Assert.assertEquals(page1.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page1.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(0));

        ItemNode page2 = serviceDiscoveryManager2.discoverItems(ROMEO,
                ResultSetManagement.forNextPage(10, page1.getItems().get(page1.getItems().size() - 1).getName())).get();
        Assert.assertEquals(page2.getItems().size(), 10);
        Assert.assertEquals(page2.getItems().get(0).getNode(), "item10");
        Assert.assertEquals(page2.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page2.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(10));

        ItemNode page3 = serviceDiscoveryManager2.discoverItems(ROMEO,
                ResultSetManagement.forNextPage(10, page2.getItems().get(page2.getItems().size() - 1).getName())).get();
        Assert.assertEquals(page3.getItems().size(), 10);
        Assert.assertEquals(page3.getItems().get(0).getNode(), "item20");
        Assert.assertEquals(page3.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page3.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(20));

        // Empty page.
        ItemNode page4 = serviceDiscoveryManager2.discoverItems(ROMEO,
                ResultSetManagement.forNextPage(10, page3.getItems().get(page3.getItems().size() - 1).getName())).get();
        Assert.assertEquals(page4.getItems().size(), 0);

        // Now page backwards
        ItemNode page5 = serviceDiscoveryManager2
                .discoverItems(ROMEO, ResultSetManagement.forPreviousPage(10, page3.getItems().get(0).getName())).get();
        Assert.assertEquals(page5.getItems().size(), 10);
        Assert.assertEquals(page5.getItems().get(0).getNode(), "item10");
        Assert.assertEquals(page5.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page5.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(10));

        ItemNode page6 = serviceDiscoveryManager2
                .discoverItems(ROMEO, ResultSetManagement.forPreviousPage(5, page5.getItems().get(0).getName())).get();
        Assert.assertEquals(page6.getItems().size(), 5);
        Assert.assertEquals(page6.getItems().get(0).getNode(), "item5");
        Assert.assertEquals(page6.getResultSetManagement().getItemCount(), Integer.valueOf(30));
        Assert.assertEquals(page6.getResultSetManagement().getFirstItemIndex(), Integer.valueOf(5));
    }

    @Test
    public void testPropertyChangeHandler() {
        ClientServiceDiscoveryManager serviceDiscoveryManager =
                xmppSession.getManager(ClientServiceDiscoveryManager.class);
        final int[] listenerCalled = {0};
        serviceDiscoveryManager.addCapabilitiesChangeListener(evt -> listenerCalled[0]++);
        serviceDiscoveryManager.addFeature("dummy");
        serviceDiscoveryManager.removeFeature("dummy");
        serviceDiscoveryManager.addIdentity(Identity.clientBot());
        serviceDiscoveryManager.removeIdentity(Identity.clientBot());
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        serviceDiscoveryManager.addExtension(dataForm);
        serviceDiscoveryManager.removeExtension(dataForm);
        if (listenerCalled[0] != 6) {
            Assert.fail();
        }
    }

//    @Test
//    public void testExtensionReplace() {
//
//        XmppSessionConfiguration configuration1 = XmppSessionConfiguration.builder()
//                .build();
//
//        XmppClient xmppClient1 = XmppClient.create("domain", configuration1);
//        // By default XEP-0115 Entity Capabilities should be enabled.
//        Assert.assertTrue(xmppClient1.getManager(EntityCapabilitiesManager.class).isEnabled());
//        Assert.assertTrue(xmppClient1.getEnabledFeatures().contains(EntityCapabilities1.NAMESPACE));
//        // By default also reconnection should be enabled.
//        Assert.assertTrue(xmppClient1.getManager(RosterManager.class).isEnabled());
//
//        XmppSessionConfiguration configuration2 = XmppSessionConfiguration.builder()
//                // We override the default context with a disabled EntityCapabilitiesManager
//                .extensions(
//                        Extension.of(EntityCapabilities1.NAMESPACE, EntityCapabilitiesManager.class, false, EntityCapabilities1.class),
//                        Extension.of(EntityCapabilities2.NAMESPACE, EntityCapabilitiesManager.class, false, EntityCapabilities2.class),
//                        Extension.of(Roster.NAMESPACE, RosterManager.class, false))
//                .build();
//
//        XmppClient xmppClient2 = XmppClient.create("domain", configuration2);
//        Assert.assertFalse(xmppClient2.getManager(EntityCapabilitiesManager.class).isEnabled());
//        Assert.assertFalse(xmppClient2.getEnabledFeatures().contains(EntityCapabilities1.NAMESPACE));
//
//        // Reconnection should now be disabled.
//        Assert.assertFalse(xmppClient2.getManager(RosterManager.class).isEnabled());
//    }
}
