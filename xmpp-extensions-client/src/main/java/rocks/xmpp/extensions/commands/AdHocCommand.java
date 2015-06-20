/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package rocks.xmpp.extensions.commands;


import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.commands.model.Command;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.InfoNode;

/**
 * @author Christian Schudt
 */
public final class AdHocCommand {

    private ServiceDiscoveryManager serviceDiscoveryManager;

    private XmppSession xmppSession;

    private String name;

    private String node;

    private Jid responder;

    public AdHocCommand(ServiceDiscoveryManager serviceDiscoveryManager, XmppSession xmppSession, Jid responder, String name, String node) {
        this.name = name;
        this.node = node;
        this.responder = responder;
        this.xmppSession = xmppSession;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
    }

    public InfoNode getInfo() throws XmppException, XmppException {
        return serviceDiscoveryManager.discoverInformation(null, node);
    }

    /**
     * Gets the name of the command.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the node of the command.
     *
     * @return The node.
     */
    public String getNode() {
        return node;
    }

    public Jid getResponder() {
        return responder;
    }

    public CommandSession execute() throws XmppException {
        IQ result = xmppSession.query(new IQ(responder, IQ.Type.SET, new Command(node, Command.Action.EXECUTE)));
        return new CommandSession(responder, xmppSession, result.getExtension(Command.class));
    }
}
