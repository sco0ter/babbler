/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.im.roster.server.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.im.roster.model.RosterItem;

/**
 * @author Christian Schudt
 */
public class RosterItemTest {

    private SeContainer seContainer;

    private RosterItemDao rosterItemDao;

    @BeforeClass
    public void setup() {
        seContainer = SeContainerInitializer.newInstance().initialize();
        rosterItemDao = seContainer.select(RosterItemDao.class).get();
    }

    @AfterClass
    public void shutdown() {
        seContainer.close();
    }

    @Test
    public void shouldCreateRosterItems() throws SQLException {
        EntityTransaction transaction = seContainer.select(EntityManager.class).get().getTransaction();
        transaction.begin();

        rosterItemDao.create("test",
                new RosterItemEntity("test", Jid.of("111"), false, false, false, "name", Contact.Subscription.BOTH,
                        Collections.singleton("aaa"), null));
        rosterItemDao.create("test",
                new RosterItemEntity("test", Jid.of("222"), false, true, false, "name", Contact.Subscription.NONE,
                        Collections.singleton("bbb"), null));
        rosterItemDao.create("test1",
                new RosterItemEntity("test1", Jid.of("333"), false, false, true, "name", Contact.Subscription.NONE,
                        Collections.emptyList(), null));

        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        DBTablePrinter.printTable(conn, "RosterItem_groups");

        transaction.commit();
    }

    @Test(dependsOnMethods = "shouldCreateRosterItems")
    public void testGet() throws SQLException {
        RosterItem rosterItem = rosterItemDao.get("test", Jid.of("222"));
        Assert.assertNotNull(rosterItem);
    }

    @Test(dependsOnMethods = "shouldCreateRosterItems")
    public void testGetCompleteRoster() throws SQLException {
        List<RosterItemEntity> rosterItem = rosterItemDao.getRosterItems("test");
        Assert.assertEquals(rosterItem.size(), 2);
    }

    @Test(dependsOnMethods = "shouldCreateRosterItems")
    public void testItemCount() throws SQLException {
        long count = rosterItemDao.getItemCount("test");
        Assert.assertEquals(count, 2);
    }
}
