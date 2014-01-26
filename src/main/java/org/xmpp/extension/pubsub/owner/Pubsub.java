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

package org.xmpp.extension.pubsub.owner;

import org.xmpp.Jid;
import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class PubSub {
    @XmlElement(name = "affiliations")
    private Affiliations affiliations;

    @XmlElement(name = "configure")
    private Configure configure;

    @XmlElement(name = "default")
    private Default aDefault;

    @XmlElement(name = "delete")
    private Delete delete;

    @XmlElement(name = "purge")
    private Purge purge;

    @XmlElement(name = "subscriptions")
    private Subscriptions subscriptions;

    public static final class Affiliations {
        @XmlElementRef(name = "affiliations")
        private List<Affiliation> affiliations;

        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Affiliation {
        @XmlAttribute(name = "affiliation")
        private Type type;

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlEnum
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

    public static final class Configure {
        @XmlElementRef
        private DataForm dataForm;

        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Default {
        @XmlElementRef
        private DataForm dataForm;
    }

    public static final class Delete {
        @XmlAttribute(name = "node")
        private String node;

        @XmlElementRef(name = "redirect")
        private Redirect redirect;

    }

    public static final class Purge {
        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Redirect {
        @XmlAttribute(name = "uri")
        private URI uri;
    }

    public static final class Subscriptions {

        @XmlElement(name = "subscription")
        private List<Subscription> subscriptions;

        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Subscription {

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "subscription")
        private Type type;

        @XmlEnum
        public enum Type {
            @XmlEnumValue("none")
            NONE,
            @XmlEnumValue("pending")
            PENDING,
            @XmlEnumValue("subscribed")
            SUBSCRIBED,
            @XmlEnumValue("unconfigured")
            UNCONFIGURED
        }
    }
}
