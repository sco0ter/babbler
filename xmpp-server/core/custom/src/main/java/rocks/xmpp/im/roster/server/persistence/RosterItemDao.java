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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.server.spi.RosterItemProvider;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Christian Schudt
 */
@Transactional
@Alternative
@Dependent
@Priority(Interceptor.Priority.APPLICATION)
public class RosterItemDao implements RosterItemProvider {

    @Inject
    private EntityManager entityManager;

    @Override
    public void create(String username, RosterItem rosterItem) {
        RosterItemEntity rosterItemEntity = new RosterItemEntity(username, rosterItem.getJid(), rosterItem.getName(), rosterItem.getSubscription());
        entityManager.persist(rosterItemEntity);
    }

    @Override
    public void update(String username, RosterItem rosterItem) {
        entityManager.merge(rosterItem);
    }

    @Override
    public RosterItemEntity delete(String username, Jid jid) {
        RosterItemEntity item = get(username, jid);
        if (item == null) {
            return null;
        }
        entityManager.remove(item);
        return item;
    }

    @Override
    public RosterItemEntity get(long id) {
        return entityManager.find(RosterItemEntity.class, id);
    }

    @Override
    public RosterItemEntity get(String username, Jid jid) {
        TypedQuery<RosterItemEntity> query = entityManager.createNamedQuery(RosterItemEntity.NamedQueries.GET_BY_USERNAME_AND_JID, RosterItemEntity.class);
        query.setParameter("username", username);
        query.setParameter("jid", jid);
        List<RosterItemEntity> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public List<RosterItemEntity> getRosterItems(String username) {
        TypedQuery<RosterItemEntity> query = entityManager.createNamedQuery(RosterItemEntity.NamedQueries.GET_ROSTER_ITEMS, RosterItemEntity.class);
        query.setParameter("username", username);
        return query.getResultList();
    }

    @Override
    public long getItemCount(String username) {
        TypedQuery<Number> query = entityManager.createNamedQuery(RosterItemEntity.NamedQueries.GET_ITEM_COUNT, Number.class);
        query.setParameter("username", username);
        return query.getSingleResult().longValue();
    }
}
