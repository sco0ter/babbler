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

package org.xmpp.im;

import org.xmpp.Jid;
import org.xmpp.util.JidAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The implementation of the roster.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#roster-syntax">2.1.  Syntax and Semantics</a></cite></p>
 * <p>Rosters are managed using {@code <iq/>} stanzas (see Section 8.2.3 of [XMPP-CORE]), specifically by means of a {@code <query/>} child element qualified by the 'jabber:iq:roster' namespace. The detailed syntax and semantics are defined in the following sections.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Roster {

    @XmlAttribute(name = "ver")
    @SuppressWarnings("unused") // Only set by server.
    private String version;

    @XmlElement(name = "item")
    private List<Contact> contacts = new ArrayList<>();

    /**
     * Gets the roster version.
     *
     * @return The roster version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the contacts.
     *
     * @return The contacts.
     */
    public List<Contact> getContacts() {
        return contacts;
    }

    /**
     * A contact in the user's roster.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#roster-syntax-items">2.1.2.  Roster Items</a></cite></p>
     * <p>The {@code <query/>} element inside a roster set contains one {@code <item/>} child, and a roster result typically contains multiple {@code <item/>} children. Each {@code <item/>} element describes a unique "roster item" (sometimes also called a "contact").</p>
     * </blockquote>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Contact implements Comparable<Contact> {

        @XmlAttribute(name = "approved")
        Boolean approved;

        @XmlJavaTypeAdapter(PendingAdapter.class)
        @XmlAttribute(name = "ask")
        Boolean pending;

        @XmlJavaTypeAdapter(JidAdapter.class)
        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "name")
        private String name;

        @XmlAttribute(name = "subscription")
        private Subscription subscription;

        @XmlElement(name = "group")
        private List<String> groups = new ArrayList<>();

        /**
         * Private default constructor for unmarshalling.
         */
        @SuppressWarnings("unused")
        private Contact() {
        }

        /**
         * Creates a new contact associated with the given JID.
         *
         * @param jid The JID.
         */
        public Contact(Jid jid) {
            this.jid = jid;
        }

        /**
         * Creates a new contact associated with the given JID and name.
         *
         * @param jid  The JID.
         * @param name The name.
         */
        public Contact(Jid jid, String name) {
            this.jid = jid;
            this.name = name;
        }

        /**
         * Creates a new contact associated with the given JID, name and groups.
         *
         * @param jid    The JID.
         * @param name   The name.
         * @param groups The groups for this contact.
         */
        public Contact(Jid jid, String name, String... groups) {
            this.jid = jid;
            this.name = name;
            this.groups.addAll(Arrays.asList(groups));
        }

        /**
         * Gets the JID of the contact.
         *
         * @return The JID.
         */
        public Jid getJid() {
            return jid;
        }

        /**
         * Gets the name of the contact.
         *
         * @return The name.
         * @see #setName(String)
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the contact.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#roster-syntax-items-name">2.1.2.4.  Name Attribute</a></cite></p>
         * <p>The 'name' attribute of the {@code <item/>} element specifies the "handle" to be associated with the JID, as determined by the user (not the contact). Although the value of the 'name' attribute MAY have meaning to a human user, it is opaque to the server. However, the 'name' attribute MAY be used by the server for matching purposes within the context of various XMPP extensions (one possible comparison method is that described for XMPP resourceparts in [XMPP-ADDR]).</p>
         * <p>It is OPTIONAL for a client to include the 'name' attribute when adding or updating a roster item.</p>
         * </blockquote>
         *
         * @param name The name.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the subscription attribute.
         *
         * @return The subscription attribute.
         */
        public Subscription getSubscription() {
            return subscription;
        }

        /**
         * Sets the subscription state of the contact. A client should only set {@link Subscription#REMOVE} as other states are managed via presence stanzas.
         *
         * @param subscription The subscription.
         */
        public void setSubscription(Subscription subscription) {
            this.subscription = subscription;
        }

        /**
         * Gets the groups of the contact.
         *
         * @return The groups.
         */
        public List<String> getGroups() {
            return groups;
        }

        /**
         * Gets the pending state of the contact.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#roster-syntax-items-ask">2.1.2.2.  Ask Attribute</a></cite></p>
         * <p>The 'ask' attribute of the {@code <item/>} element with a value of "subscribe" is used to signal various subscription sub-states that include a "Pending Out" aspect as described under Section 3.1.2.</p>
         * <p>A server SHOULD include the 'ask' attribute to inform the client of "Pending Out" sub-states. A client MUST NOT include the 'ask' attribute in the roster sets it sends to the server, but instead MUST use presence stanzas of type "subscribe" and "unsubscribe" to manage such sub-states as described under Section 3.1.2. </p>
         * </blockquote>
         *
         * @return True, if a subscription request for the contact is pending, i.e. the contact has not yet approved or denied a subscription request.
         */
        public boolean isPending() {
            return pending != null && pending;
        }

        /**
         * Gets the subscription pre-approval status.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#roster-syntax-items-approved">2.1.2.1.  Approved Attribute</a></cite></p>
         * <p>The boolean 'approved' attribute with a value of "true" is used to signal subscription pre-approval as described under Section 3.4</p>
         * </blockquote>
         *
         * @return True, if the contact is pre approved.
         */
        public boolean isApproved() {
            return approved != null && approved;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Contact)) {
                return false;
            }
            Contact other = (Contact) o;

            return (jid == null ? other.jid == null : jid.equals(other.jid))
                    && (name == null ? other.name == null : name.equals(other.name))
                    && (subscription == null ? other.subscription == null : subscription.equals(other.subscription))
                    && (approved == null ? other.approved == null : approved.equals(other.approved))
                    && (pending == null ? other.pending == null : pending.equals(other.pending))
                    && (groups == null ? other.groups == null : (groups.containsAll(other.groups) && other.groups.containsAll(groups)));

        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + ((jid == null) ? 0 : jid.hashCode());
            result = 31 * result + ((name == null) ? 0 : name.hashCode());
            result = 31 * result + ((subscription == null) ? 0 : subscription.hashCode());
            result = 31 * result + ((approved == null) ? 0 : approved.hashCode());
            result = 31 * result + ((pending == null) ? 0 : pending.hashCode());
            result = 31 * result + ((groups == null) ? 0 : groups.hashCode());
            return result;
        }

        /**
         * Compares two contacts with each other by first comparing their names, then their subscription states and eventually their pending states.
         *
         * @param o The other contact.
         * @return The result of the comparison.
         */
        @Override
        public int compareTo(Contact o) {
            if (this == o) {
                return 0;
            }

            if (o != null) {
                int result;
                if (name != null) {

                    if (o.name != null) {
                        result = name.compareTo(o.name);
                    } else {
                        result = -1;
                    }
                } else {
                    if (o.name != null) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
                if (result == 0) {
                    if (subscription != null) {
                        if (o.subscription != null) {
                            result = o.subscription.compareTo(o.subscription);
                        } else {
                            result = -1;
                        }
                    } else {
                        if (o.subscription != null) {
                            result = 1;
                        } else {
                            result = 0;
                        }
                    }
                }
                if (result == 0) {
                    result = Boolean.compare(isPending(), o.isPending());
                }
                return result;
            } else {
                return 1;
            }
        }

        /**
         * The implementation of the 'subscription' attribute.
         */
        @XmlEnum
        public enum Subscription {
            /**
             * The user and the contact have subscriptions to each other's presence (also called a "mutual subscription").
             */
            @XmlEnumValue("both")
            BOTH,
            /**
             * The contact has a subscription to the user's presence, but the user does not have a subscription to the contact's presence.
             */
            @XmlEnumValue("from")
            FROM,
            /**
             * The user has a subscription to the contact's presence, but the contact does not have a subscription to the user's presence.
             */
            @XmlEnumValue("to")
            TO,
            /**
             * The user does not have a subscription to the contact's presence, and the contact does not have a subscription to the user's presence; this is the default value, so if the subscription attribute is not included then the state is to be understood as "none".
             */
            @XmlEnumValue("none")
            NONE,
            /**
             * At any time, a client can delete an item from his or her roster by sending a roster set and specifying a value of "remove" for the 'subscription' attribute.
             */
            @XmlEnumValue("remove")
            REMOVE
        }

        @XmlEnum
        private enum Ask {
            @XmlEnumValue("subscribe")
            SUBSCRIBE
        }

        /**
         * Converts the "ask" attribute, which can only have a single value ("subscribe") to a boolean for convenience.
         */
        private static final class PendingAdapter extends XmlAdapter<Ask, Boolean> {

            @Override
            public Boolean unmarshal(Ask v) throws Exception {
                return v != null && v == Ask.SUBSCRIBE;
            }

            @Override
            public Ask marshal(Boolean v) throws Exception {
                return v != null ? Ask.SUBSCRIBE : null;
            }
        }
    }
}
