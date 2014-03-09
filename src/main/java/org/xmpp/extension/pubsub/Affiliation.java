package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Christian Schudt
 */
public enum Affiliation {
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