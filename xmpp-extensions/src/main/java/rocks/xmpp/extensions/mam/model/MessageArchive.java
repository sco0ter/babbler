/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.extensions.mam.model;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.forward.model.Forwarded;
import rocks.xmpp.extensions.rsm.model.ResultSetItem;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Christian Schudt
 */
@XmlSeeAlso({MessageArchive.Query.class, MessageArchive.Result.class, MessageArchive.Fin.class, MessageArchive.Preferences.class})
public abstract class MessageArchive {

    /**
     * urn:xmpp:mam:1
     */
    public static final String NAMESPACE = "urn:xmpp:mam:2";

    @XmlRootElement
    public static final class Query extends MessageArchive {

        @XmlAttribute
        private final String queryid;

        @XmlAttribute
        private final String node;

        @XmlElementRef
        private final DataForm dataForm;

        @XmlElementRef
        private final ResultSetManagement rsm;

        private Query() {
            this.queryid = null;
            this.node = null;
            this.dataForm = null;
            this.rsm = null;
        }

        /**
         * @param queryid The client can optionally include a 'queryid' attribute in their query, which allows the client to match results to their initiating query.
         */
        public Query(String queryid) {
            this(queryid, null);
        }

        /**
         * @param queryid The client can optionally include a 'queryid' attribute in their query, which allows the client to match results to their initiating query.
         * @param node    The pubsub node, when querying a pubsub node's archive.
         */
        public Query(String queryid, String node) {
            this.queryid = queryid;
            this.node = node;
            this.dataForm = null;
            this.rsm = null;
        }

        public final String getQueryId() {
            return queryid;
        }

        public final DataForm getDataForm() {
            return dataForm;
        }

        public final String getNode() {
            return node;
        }

        public final ResultSetManagement getResultSetManagement() {
            return rsm;
        }
    }

    /**
     * The {@code <result/>} element.
     */
    @XmlRootElement
    public static final class Result extends MessageArchive implements ResultSetItem {

        @XmlAttribute
        private final String queryid;

        @XmlAttribute
        private final String id;

        @XmlElementRef
        private final Forwarded forwarded;

        private Result() {
            this.id = null;
            this.forwarded = null;
            this.queryid = null;
        }

        public Result(String id, Forwarded forwarded, String queryId) {
            this.id = Objects.requireNonNull(id);
            this.forwarded = Objects.requireNonNull(forwarded);
            this.queryid = queryId;
        }

        public final Forwarded getForwarded() {
            return forwarded;
        }

        public final String getQueryId() {
            return queryid;
        }

        @Override
        public final String getId() {
            return id;
        }
    }

    /**
     * The {@code <fin/>} element.
     */
    @XmlRootElement
    public static final class Fin extends MessageArchive {

        @XmlAttribute
        private final Boolean complete;

        @XmlElementRef
        private final ResultSetManagement rsm;

        private Fin() {
            this.rsm = null;
            this.complete = null;
        }

        public Fin(ResultSetManagement rsm, Boolean complete) {
            this.rsm = Objects.requireNonNull(rsm);
            this.complete = complete;
        }

        public final ResultSetManagement getResultSet() {
            return rsm;
        }

        public final boolean isComplete() {
            return complete != null && complete;
        }
    }

    /**
     * The {@code <prefs/>} element, i.e. the preferences for the message archive.
     */
    @XmlRootElement(name = "prefs")
    public static final class Preferences extends MessageArchive {

        @XmlAttribute(name = "default")
        private final Default _default;

        @XmlElementWrapper(name = "always")
        @XmlElement(name = "jid")
        private final List<Jid> always;

        @XmlElementWrapper(name = "never")
        @XmlElement(name = "jid")
        private final List<Jid> never;

        public Preferences() {
            this._default = null;
            this.always = null;
            this.never = null;
        }

        public Preferences(Default theDefault, Collection<Jid> always, Collection<Jid> never) {
            this._default = theDefault;
            this.always = new ArrayList<>(always);
            this.never = new ArrayList<>(never);
        }

        /**
         * Gets the default store behavior, which applies for JIDs which are neither in the 'always' nor in the 'never' list.
         *
         * @return The default store behavior.
         * @see #getAlways()
         * @see #getNever()
         */
        public final Default getDefault() {
            return _default;
        }

        /**
         * Gets the JIDs which should always be archived by the server.
         *
         * @return The list of JIDs.
         */
        public final List<Jid> getAlways() {
            return always != null ? Collections.unmodifiableList(always) : Collections.emptyList();
        }

        /**
         * Gets the JIDs which should never be archived by the server.
         *
         * @return The list of JIDs.
         */
        public final List<Jid> getNever() {
            return never != null ? Collections.unmodifiableList(never) : Collections.emptyList();
        }

        /**
         * The default behavior to store messages.
         */
        public enum Default {
            /**
             * All messages are archived by default.
             */
            @XmlEnumValue("always")
            ALWAYS,
            /**
             * Messages are never archived by default.
             */
            @XmlEnumValue("never")
            NEVER,
            /**
             * Messages are archived only if the contact's bare JID is in the user's roster.
             */
            @XmlEnumValue("roster")
            ROSTER
        }
    }
}
