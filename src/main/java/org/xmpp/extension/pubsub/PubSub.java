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

package org.xmpp.extension.pubsub;

import org.xmpp.Jid;
import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "pubsub")
public final class PubSub {

    @XmlElement
    private Publish publish;

    public static final class Create {
        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Configure {

        @XmlElementRef
        private DataForm dataForm;
    }

    public static final class Subscribe {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "jid")
        private Jid jid;


        private Subscribe() {

        }

        public Subscribe(String node, Jid jid) {
            this.node = node;
            this.jid = jid;
        }
    }

    public static final class Unsubscribe {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "jid")
        private Jid jid;


        private Unsubscribe() {

        }

        public Unsubscribe(String node, Jid jid) {
            this.node = node;
            this.jid = jid;
        }
    }

    public static final class Options {

    }

    public static final class Affiliations {
        @XmlAttribute(name = "node")
        private String node;

        @XmlElement(name = "affiliation")
        private List<Affiliation> affiliations;

        public Affiliations() {

        }

        public Affiliations(String node) {
            this.node = node;
        }

        public static final class Affiliation {

            @XmlAttribute(name = "node")
            private String node;

            @XmlAttribute(name = "affiliation")
            private Type affiliation;

            public enum Type {
                @XmlEnumValue("member")
                MEMBER,
                @XmlEnumValue("none")
                NONE,
                @XmlEnumValue("outcast")
                OUTCAST,
                @XmlEnumValue("owner")
                OWNER,
                @XmlEnumValue("publisher")
                PUBLISHER,
                @XmlEnumValue("publish-only")
                PUBLISH_ONLY
            }
        }
    }

    public static final class Default {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "type")
        private Type type;

        public enum Type {
            @XmlEnumValue("collection")
            COLLECTION,
            @XmlEnumValue("leaf")
            LEAF
        }
    }

    public static final class Publish {
        @XmlAttribute(name = "node")
        private String node;

        @XmlElementRef
        private Item item;

        private Publish() {

        }

        public Publish(String node, Item item) {
            this.node = node;
            this.item = item;
        }
    }

    @XmlRootElement(name = "item")
    public static final class Item {
        @XmlAnyElement
        private Object object;

        @XmlAttribute(name = "id")
        private String id;
    }

    public static final class Subscriptions {

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "subscription")
        private List<Subscription> subscriptions;

        public Subscriptions() {
        }

        public Subscriptions(String node) {

        }

    }

}
