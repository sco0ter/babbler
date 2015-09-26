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

package rocks.xmpp.extensions.mam.model;

import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.forward.model.Forwarded;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Christian Schudt
 */
@XmlSeeAlso({MessageArchive.Query.class, MessageArchive.Result.class})
public abstract class MessageArchive {
    /**
     * urn:xmpp:mam:1
     */
    public static final String NAMESPACE = "urn:xmpp:mam:1";

    @XmlRootElement()
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

        public Query(String queryid) {
            this(queryid, null);
        }

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
    }

    @XmlRootElement
    public static final class Result extends MessageArchive {

        @XmlElementRef
        private final Forwarded forwarded;

        private Result() {
            this.forwarded = null;
        }

        public Forwarded getForwarded() {
            return forwarded;
        }
    }
}
