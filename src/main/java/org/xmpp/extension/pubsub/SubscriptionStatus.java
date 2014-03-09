package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Christian Schudt
 */
public enum SubscriptionStatus {
    @XmlEnumValue("none")
    NONE,
    @XmlEnumValue("pending")
    PENDING,
    @XmlEnumValue("subscribed")
    SUBSCRIBED,
    @XmlEnumValue("unconfigured")
    UNCONFIGURED,
}