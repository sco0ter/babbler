package org.xmpp.extension.muc;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Christian Schudt
 */
public enum Role {

    @XmlEnumValue("moderator")
    MODERATOR,
    @XmlEnumValue("none")
    NONE,
    @XmlEnumValue("participant")
    PARTICIPANT,
    @XmlEnumValue("visitor")
    VISITOR
}
