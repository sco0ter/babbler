/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.rsm;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.disco.model.items.DiscoverableItem;
import rocks.xmpp.extensions.disco.model.items.ItemElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinedResultSetProviderTest {

    private ResultSetProvider<DiscoverableItem> combined;

    @BeforeClass
    public void init() {
        List<DiscoverableItem> items1 = new ArrayList<>();
        items1.add(DiscoverableItem.from(new ItemElement(Jid.of("user1@server"), null, null), "user1"));
        items1.add(DiscoverableItem.from(new ItemElement(Jid.of("user2@server"), null, null), "user2"));

        List<DiscoverableItem> items2 = new ArrayList<>();
        items2.add(DiscoverableItem.from(new ItemElement(Jid.of("user3@server"), null, null), "user3"));
        items2.add(DiscoverableItem.from(new ItemElement(Jid.of("user4@server"), null, null), "user4"));
        items2.add(DiscoverableItem.from(new ItemElement(Jid.of("user5@server"), null, null), "user5"));

        List<DiscoverableItem> items3 = new ArrayList<>();
        items3.add(DiscoverableItem.from(new ItemElement(Jid.of("user6@server"), null, null), "user6"));
        items3.add(DiscoverableItem.from(new ItemElement(Jid.of("user7@server"), null, null), "user7"));
        items3.add(DiscoverableItem.from(new ItemElement(Jid.of("user8@server"), null, null), "user8"));

        ResultSetProvider<DiscoverableItem> resultSetProvider1 = ResultSetProvider.forItems(items1);
        ResultSetProvider<DiscoverableItem> resultSetProvider2 = ResultSetProvider.forItems(items2);
        ResultSetProvider<DiscoverableItem> resultSetProvider3 = ResultSetProvider.forItems(items3);
        combined = ResultSetProvider.combine(Arrays.asList(resultSetProvider1, resultSetProvider2, resultSetProvider3));
    }

    @Test
    public void testGetItemCount() {
        Assert.assertEquals(combined.getItemCount(), 8);
    }

    @Test
    public void testGetItems() {
        List<DiscoverableItem> allItems = combined.getItems();
        Assert.assertEquals(allItems.get(0).getJid(), Jid.of("user1@server"));
        Assert.assertEquals(allItems.get(2).getJid(), Jid.of("user3@server"));
        Assert.assertEquals(allItems.get(5).getJid(), Jid.of("user6@server"));
    }

    @Test
    public void testRangedGetItems() {

        List<DiscoverableItem> rangedItems0 = combined.getItems(1, 2);
        Assert.assertEquals(rangedItems0.get(0).getJid(), Jid.of("user2@server"));
        Assert.assertEquals(rangedItems0.get(1).getJid(), Jid.of("user3@server"));

        List<DiscoverableItem> rangedItems1 = combined.getItems(1, 3);
        Assert.assertEquals(rangedItems1.get(0).getJid(), Jid.of("user2@server"));
        Assert.assertEquals(rangedItems1.get(1).getJid(), Jid.of("user3@server"));
        Assert.assertEquals(rangedItems1.get(2).getJid(), Jid.of("user4@server"));

        List<DiscoverableItem> rangedItems2 = combined.getItems(2, 4);
        Assert.assertEquals(rangedItems2.get(0).getJid(), Jid.of("user3@server"));
        Assert.assertEquals(rangedItems2.get(1).getJid(), Jid.of("user4@server"));
        Assert.assertEquals(rangedItems2.get(2).getJid(), Jid.of("user5@server"));

        List<DiscoverableItem> rangedItems3 = combined.getItems(1, 6);
        Assert.assertEquals(rangedItems3.get(0).getJid(), Jid.of("user2@server"));
        Assert.assertEquals(rangedItems3.get(1).getJid(), Jid.of("user3@server"));
        Assert.assertEquals(rangedItems3.get(2).getJid(), Jid.of("user4@server"));
        Assert.assertEquals(rangedItems3.get(3).getJid(), Jid.of("user5@server"));
        Assert.assertEquals(rangedItems3.get(4).getJid(), Jid.of("user6@server"));
        Assert.assertEquals(rangedItems3.get(5).getJid(), Jid.of("user7@server"));

        List<DiscoverableItem> rangedItems4 = combined.getItems(3, 3);
        Assert.assertEquals(rangedItems4.get(0).getJid(), Jid.of("user4@server"));
        Assert.assertEquals(rangedItems4.get(1).getJid(), Jid.of("user5@server"));
        Assert.assertEquals(rangedItems4.get(2).getJid(), Jid.of("user6@server"));

        List<DiscoverableItem> rangedItems5 = combined.getItems(6, 2);
        Assert.assertEquals(rangedItems5.get(0).getJid(), Jid.of("user7@server"));
        Assert.assertEquals(rangedItems5.get(1).getJid(), Jid.of("user8@server"));

        List<DiscoverableItem> rangedItems6 = combined.getItems(5, 1);
        Assert.assertEquals(rangedItems6.get(0).getJid(), Jid.of("user6@server"));
    }

    @Test
    public void testIndexOf() {
        Assert.assertEquals(combined.indexOf("user1"), 0);
        Assert.assertEquals(combined.indexOf("user2"), 1);
        Assert.assertEquals(combined.indexOf("user3"), 2);
        Assert.assertEquals(combined.indexOf("user4"), 3);
        Assert.assertEquals(combined.indexOf("user5"), 4);
        Assert.assertEquals(combined.indexOf("user6"), 5);
        Assert.assertEquals(combined.indexOf("user7"), 6);
        Assert.assertEquals(combined.indexOf("user8"), 7);
    }

    @Test
    public void testGetItemsBefore() {
        List<DiscoverableItem> items1 = combined.getItemsBefore("user2", 2);
        Assert.assertEquals(items1.get(0).getJid(), Jid.of("user1@server"));

        List<DiscoverableItem> items2 = combined.getItemsBefore("user4", 2);
        Assert.assertEquals(items2.get(0).getJid(), Jid.of("user2@server"));
        Assert.assertEquals(items2.get(1).getJid(), Jid.of("user3@server"));
    }

    @Test
    public void testGetItemsAfter() {
        List<DiscoverableItem> items1 = combined.getItemsAfter("user2", 2);
        Assert.assertEquals(items1.get(0).getJid(), Jid.of("user3@server"));
        Assert.assertEquals(items1.get(1).getJid(), Jid.of("user4@server"));

        List<DiscoverableItem> items2 = combined.getItemsAfter("user1", 7);
        Assert.assertEquals(items2.get(0).getJid(), Jid.of("user2@server"));
        Assert.assertEquals(items2.get(1).getJid(), Jid.of("user3@server"));
    }
}
