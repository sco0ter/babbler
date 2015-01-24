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

package rocks.xmpp.extensions.search.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <query/>} element in the {@code jabber:iq:search} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0055.html">XEP-0055: Jabber Search</a>
 * @see <a href="http://xmpp.org/extensions/xep-0055.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "query")
public final class Search {

    /**
     * jabber:iq:search
     */
    public static final String NAMESPACE = "jabber:iq:search";

    @XmlElement(name = "item")
    private final List<Item> items = new ArrayList<>();

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

    @XmlElementRef
    private DataForm form;

    @XmlElementRef
    private ResultSetManagement resultSet;

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
     * Creates a search request, consisting of multiple search parameters.
     *
     * @param first     The first name.
     * @param last      The last name.
     * @param nick      The nick name.
     * @param email     The email.
     * @param resultSet The result set information.
     */
    public Search(String first, String last, String nick, String email, ResultSetManagement resultSet) {
        this.first = first;
        this.last = last;
        this.nick = nick;
        this.email = email;
        this.resultSet = resultSet;
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
     * Gets the items of a search result.
     *
     * @return The items.
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
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
     * Gets the result set information.
     *
     * @return The result set.
     * @see <a href="http://xmpp.org/extensions/xep-0059.html">XEP-0059: Result Set Management</a>
     */
    public ResultSetManagement getResultSet() {
        return resultSet;
    }

    /**
     * The implementation of a search result item.
     */
    public static final class Item {

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
