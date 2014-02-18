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

package org.xmpp.extension.search;

import org.xmpp.Jid;
import org.xmpp.extension.data.DataForm;
import org.xmpp.util.JidAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the 'jabber:iq:search' extension.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class Search {

    @XmlElement
    private String instructions;

    @XmlElement
    private String first;

    @XmlElement
    private String last;

    @XmlElement
    private String nick;

    @XmlElement
    private String email;

    @XmlElement(name = "item")
    private List<Item> items = new ArrayList<>();

    @XmlElementRef
    private DataForm form;

    /**
     * Creates an empty search request.
     */
    public Search() {
    }

    /**
     * Creates a search request, consisting of multiple search parameters.
     *
     * @param first The first name.
     * @param last  The last name.
     * @param nick  The nick name.
     * @param email The email.
     */
    public Search(String first, String last, String nick, String email) {
        this.first = first;
        this.last = last;
        this.nick = nick;
        this.email = email;
    }

    /**
     * Gets the search instructions.
     *
     * @return The search instructions.
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Gets the first name.
     *
     * @return The first name.
     * @see #setFirst(String)
     */
    public String getFirst() {
        return first;
    }

    /**
     * Sets the first name.
     *
     * @param first The first name.
     * @see #getFirst()
     */
    public void setFirst(String first) {
        this.first = first;
    }

    /**
     * Gets the last name.
     *
     * @return The last name.
     * @see #setLast(String)
     */
    public String getLast() {
        return last;
    }

    /**
     * Sets the last name.
     *
     * @param last The last name.
     * @see #getLast()
     */
    public void setLast(String last) {
        this.last = last;
    }

    /**
     * Gets the nick name.
     *
     * @return The nick name.
     * @see #setNick(String)
     */
    public String getNick() {
        return nick;
    }

    /**
     * Sets the nick name.
     *
     * @param nick The nick name.
     * @see #getNick()
     */
    public void setNick(String nick) {
        this.nick = nick;
    }

    /**
     * Gets the email address.
     *
     * @return The email address.
     * @see #setEmail(String)
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email The email address.
     * @see #getEmail()
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the items of a search result.
     *
     * @return The items.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Gets additional information for the search, e.g. for gender.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0055.html#extensibility">3. Extensibility</a></cite></p>
     * <p>The fields defined in the 'jabber:iq:search' namespace are strictly limited to those specified in the schema. If a host needs to gather additional information, Data Forms SHOULD be used; a host MUST NOT add new fields to the 'jabber:iq:search' namespace. Support for extensibility via Data Forms is RECOMMENDED, but is not required for compliance with this document.</p>
     * </blockquote>
     *
     * @return The data form, which contains additional information.
     */
    public DataForm getAdditionalInformation() {
        return form;
    }

    /**
     * The implementation of a search result item.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {

        @XmlJavaTypeAdapter(JidAdapter.class)
        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlElement(name = "first")
        private String first;

        @XmlElement(name = "last")
        private String last;

        @XmlElement(name = "nick")
        private String nick;

        @XmlElement(name = "email")
        private String email;

        /**
         * Gets the first name.
         *
         * @return The first name.
         */
        public String getFirst() {
            return first;
        }

        /**
         * Gets the last name.
         *
         * @return The last name.
         */
        public String getLast() {
            return last;
        }

        /**
         * Gets the nick name.
         *
         * @return The nick name.
         */
        public String getNick() {
            return nick;
        }

        /**
         * Gets the email address.
         *
         * @return The email address.
         */
        public String getEmail() {
            return email;
        }

        /**
         * Gets the JID.
         *
         * @return The JID.
         */
        public Jid getJid() {
            return jid;
        }
    }
}
