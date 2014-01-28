package org.xmpp.extension.headers;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Christian Schudt
 */
public final class Header {

    @XmlAttribute(name = "name")
    private String name;

    public Header(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
