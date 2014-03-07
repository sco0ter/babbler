package org.xmpp.extension.muc.user;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "x")
public class MucUser {

    @XmlElement(name = "item")
    private Item item;

    @XmlElement(name = "status")
    private List<Status> statusCodes;

    @XmlElement(name = "invite")
    private List<Invitation> invitations;

    @XmlElement(name = "decline")
    private Decline decline;

    public List<Status> getStatusCodes() {
        return statusCodes;
    }

    public Item getItem() {
        return item;
    }

    public List<Invitation> getInvitations() {
        return invitations;
    }

    public Decline getDecline() {
        return decline;
    }
}
