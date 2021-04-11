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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.addr.server.persistence.JidConverter;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.model.SubscriptionState;

/**
 * @author Christian Schudt
 */
@Entity(name = "RosterItem")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "jid"})})
@NamedQueries({
        @NamedQuery(name = RosterItemEntity.NamedQueries.GET_BY_USERNAME_AND_JID,
                query = "select ri from RosterItem ri where ri.username = :username and jid = :jid"),
        @NamedQuery(name = RosterItemEntity.NamedQueries.GET_ROSTER_ITEMS,
                query = "select ri from RosterItem ri where ri.username = :username and ri.pendingIn = false"),
        @NamedQuery(name = RosterItemEntity.NamedQueries.GET_ITEM_COUNT,
                query = "select count(ri) from RosterItem ri where ri.username = :username and ri.pendingIn = false")
})
public class RosterItemEntity implements RosterItem {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Convert(converter = JidConverter.class)
    @Column(name = "jid", nullable = false, length = Jid.MAX_BARE_JID_LENGTH)
    private Jid jid;

    @Column(name = "name")
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "subscription")
    private SubscriptionState.Subscription subscription;

    @Column(name = "pendingOut")
    private boolean pendingOut;

    @Column(name = "pendingIn")
    private boolean pendingIn;

    @Column(name = "approved")
    private boolean approved;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "groups")
    private List<String> groups = new ArrayList<>();

    @Column(name = "presenceStanza")
    private String presenceStanza;

    protected RosterItemEntity() {
    }

    public RosterItemEntity(String username, Jid jid, String name, SubscriptionState.Subscription subscription) {
        this(username, jid, false, false, false, name, subscription, null, null);
    }

    public RosterItemEntity(String username, Jid jid, boolean approved, boolean pendingOut, boolean pendingIn,
                            String name, SubscriptionState.Subscription subscription, Collection<String> groups,
                            String presenceStanza) {
        this.username = Objects.requireNonNull(username);
        this.jid = jid;
        this.approved = approved;
        this.pendingOut = pendingOut;
        this.pendingIn = pendingIn;
        this.name = name;
        this.subscription = subscription;
        if (groups != null) {
            this.groups.addAll(groups);
        }
        this.presenceStanza = presenceStanza;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public Jid getJid() {
        return jid;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

    @Override
    public SubscriptionState.Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionState.Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public boolean isPendingOut() {
        return pendingOut;
    }

    public void setPendingOut(boolean pendingOut) {
        this.pendingOut = pendingOut;
    }

    @Override
    public boolean isPendingIn() {
        return pendingIn;
    }

    public void setPendingIn(boolean pendingIn) {
        this.pendingIn = pendingIn;
    }

    public String getPresenceStanza() {
        return presenceStanza;
    }

    static final class NamedQueries {

        static final String GET_BY_USERNAME_AND_JID = "RosterItem.getByUsernameAndJid";

        static final String GET_ROSTER_ITEMS = "RosterItem.getRosterItems";

        static final String GET_ITEM_COUNT = "RosterItem.getItemCount";

        private NamedQueries() {
        }
    }
}
                    