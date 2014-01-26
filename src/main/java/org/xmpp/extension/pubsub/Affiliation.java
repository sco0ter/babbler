package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Christian Schudt
 */
public final class Affiliation {

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
