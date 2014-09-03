package org.xmpp.extension.commands;

import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.InfoNode;

/**
 * @author Christian Schudt
 */
public final class AdHocCommand {


    public AdHocCommand(ServiceDiscoveryManager serviceDiscoveryManager, XmppSession xmppSession, String name, String node) {
        this.name = name;
        this.node = node;
        this.xmppSession = xmppSession;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
    }

    public InfoNode getInfo() throws XmppException {
        return serviceDiscoveryManager.discoverInformation(null, node);
    }

    private ServiceDiscoveryManager serviceDiscoveryManager;

    private XmppSession xmppSession;

    private String name;

    private String node;
}
