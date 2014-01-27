package org.xmpp.extension.avatar.data;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "data")
public final class AvatarData {

    @XmlValue
    private byte[] data;

}
