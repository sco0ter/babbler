package org.xmpp.extension.muc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "x")
public final class Muc {

    public Muc() {
    }

    public Muc(String password) {
        this.password = password;
    }

    public Muc(History history) {
        this.history = history;
    }

    @XmlElement(name = "password")
    private String password;

    @XmlElement(name = "history")
    private History history;
}
