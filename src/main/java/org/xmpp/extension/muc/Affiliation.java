package org.xmpp.extension.muc;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Christian Schudt
 */
public enum Affiliation {
    @XmlEnumValue("admin")
    ADMIN,
    @XmlEnumValue("member")
    MEMBER,
    @XmlEnumValue("none")
    NONE,
    @XmlEnumValue("outcast")
    OUTCAST,
    @XmlEnumValue("owner")
    OWNER
}
