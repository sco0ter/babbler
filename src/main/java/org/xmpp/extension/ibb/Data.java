package org.xmpp.extension.ibb;

import javax.xml.bind.annotation.*;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Data {
    @XmlAttribute(name = "sid")
    private String sid;

    @XmlAttribute(name = "seq")
    private Short seq;

    @XmlValue
    private String value;

    public Data() {

    }

}
