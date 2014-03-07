package org.xmpp.extension.muc.admin;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class MucAdmin {

    @XmlElement(name = "item")
    private List<Item> item;

    public List<Item> getItems() {
        return item;
    }
}
