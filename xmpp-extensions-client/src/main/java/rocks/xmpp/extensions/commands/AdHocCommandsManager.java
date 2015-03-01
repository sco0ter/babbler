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


import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.commands.model.Command;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.rsm.ResultSetProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Schudt
 */
public class AdHocCommandsManager extends ExtensionManager {

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final InfoNode infoNode;

    private final Map<String, AdHocCommand> commandMap;

    private final Map<String, CommandSession> commandSessionMap;

    private AdHocCommandsManager(final XmppSession xmppSession) {
        super(xmppSession, Command.NAMESPACE);
        serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        commandMap = new HashMap<>();
        commandSessionMap = new HashMap<>();

        infoNode = new InfoNode() {
            @Override
            public String getNode() {
                return Command.NAMESPACE;
            }

            @Override
            public Set<Identity> getIdentities() {
                Set<Identity> identities = new HashSet<>();
                // the service discovery identity category is "automation" and type is "command-list"
                identities.add(new Identity("automation", "command-list"));
                return identities;
            }

            @Override
            public Set<Feature> getFeatures() {
                return null;
            }

            @Override
            public List<DataForm> getExtensions() {
                return null;
            }
        };

        xmppSession.addIQHandler(Command.class, new AbstractIQHandler(this, AbstractIQ.Type.SET) {
            @Override
            public IQ processRequest(IQ iq) {

                Command command = iq.getExtension(Command.class);

                String sessionId = command.getSessionId();
                String node = command.getNode();

                if (sessionId == null) {
                    AdHocCommand adHocCommand = commandMap.get(node);
                    if (adHocCommand == null) {
                        return iq.createError(Condition.ITEM_NOT_FOUND);
                    }
                } else {
                    CommandSession commandSession = commandSessionMap.get(sessionId);
                    if (commandSession == null) {
                        // We can't find the requested command node.
                        return iq.createError(Condition.ITEM_NOT_FOUND);
                    }
                }
                return iq.createError(Condition.ITEM_NOT_FOUND);
            }
        });

        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
//        if (enabled) {
//            serviceDiscoveryManager.addInfoNode(infoNode);
//            serviceDiscoveryManager.setItemProvider(Command.NAMESPACE, new ResultSetProvider<Item>() {
//                @Override
//                public List<Item> getItems() {
//                    return null;
//                }
//
//                @Override
//                public int getItemCount() {
//                    return 0;
//                }
//
//                @Override
//                public List<Item> getItems(int index, int maxSize) {
//                    return null;
//                }
//
//                @Override
//                public List<Item> getItemsAfter(String itemId, int maxSize) {
//                    return null;
//                }
//
//                @Override
//                public List<Item> getItemsBefore(String itemId, int maxSize) {
//                    return null;
//                }
//
//                @Override
//                public int indexOf(String itemId) {
//                    return 0;
//                }
//            });
//        } else {
//            serviceDiscoveryManager.removeInfoNode(Command.NAMESPACE);
//            serviceDiscoveryManager.setItemProvider(Command.NAMESPACE, null);
//        }
    }

    /**
     * Retrieves the command list of another entity.
     *
     * @param entity The responder.
     * @return The command list.
     * @throws XmppException
     * @see <a href="http://xmpp.org/extensions/xep-0050.html#retrieve">2.2 Retrieving the Command List</a>
     */
    public List<AdHocCommand> discoverCommands(Jid entity) throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(entity, Command.NAMESPACE);
        List<AdHocCommand> commands = new ArrayList<>();
        for (Item item : itemNode.getItems()) {
            commands.add(new AdHocCommand(serviceDiscoveryManager, xmppSession, item.getJid(), item.getName(), item.getNode()));
        }
        return commands;
    }

    /**
     * @param to
     * @param node
     * @see <a href="http://xmpp.org/extensions/xep-0050.html#execute">2.4 Executing Commands</a>
     */
    public CommandSession execute(Jid to, String node) throws XmppException {
        IQ result = xmppSession.query(new IQ(to, IQ.Type.SET, new Command(node, Command.Action.EXECUTE)));
        return new CommandSession(to, xmppSession, result.getExtension(Command.class));
    }

    public void addCommand(final String node, final String name, AdHocCommand command) {
        serviceDiscoveryManager.addInfoNode(new InfoNode() {
            private final Set<Identity> identities = new HashSet<>();

            private final Set<Feature> features = new HashSet<>();

            {
                identities.add(new Identity("automation", "command-node", name));
                features.add(new Feature(Command.NAMESPACE));
                features.add(new Feature("jabber:x:data"));
            }

            @Override
            public String getNode() {
                return node;
            }

            @Override
            public Set<Identity> getIdentities() {
                return identities;
            }

            @Override
            public Set<Feature> getFeatures() {
                return features;
            }

            @Override
            public List<DataForm> getExtensions() {
                return null;
            }
        });
    }
}
