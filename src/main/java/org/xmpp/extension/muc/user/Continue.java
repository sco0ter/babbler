package org.xmpp.extension.muc.user;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Christian Schudt
 */
public final class Continue {

    @XmlAttribute(name = "thread")
    private String thread;

    public String getThread() {
        return thread;
    }
}
