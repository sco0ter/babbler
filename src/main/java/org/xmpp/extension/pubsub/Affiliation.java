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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Christian Schudt
 */
public final class Affiliation {

    @XmlAttribute(name = "node")
    private String node;

    @XmlAttribute(name = "affiliation")
    private Type affiliation;

    @XmlAttribute(name = "jid")
    private Jid jid;

    public Jid getJid() {
        return jid;
    }

    public Type getType() {
        return affiliation;
    }

    public String getNode() {
        return node;
    }

    @XmlType(name = "affiliation-type")
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
