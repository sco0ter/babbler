package org.xmpp.extension.commands;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.ItemNotFound;

import java.util.*;

/**
 * @author Christian Schudt
 */
public class AdHocCommandsManager extends ExtensionManager {

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final ItemNode itemNode;

    private final InfoNode infoNode;

    private final Map<String, AdHocCommand> commandMap;

    private final Map<String, CommandSession> commandSessionMap;

    private AdHocCommandsManager(final XmppSession xmppSession) {
        super(xmppSession, Command.NAMESPACE);
        serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
        commandMap = new HashMap<>();
        commandSessionMap = new HashMap<>();

        itemNode = new ItemNode() {
            @Override
            public String getNode() {
                return Command.NAMESPACE;
            }

            @Override
            public List<Item> getItems() {
                return null;
            }
        };

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

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {
                    Command command = iq.getExtension(Command.class);
                    if (command != null) {
                        String sessionId = command.getSessionId();
                        String node = command.getNode();

                        if (sessionId == null) {
                            AdHocCommand adHocCommand = commandMap.get(node);
                            if (adHocCommand == null) {
                                xmppSession.send(iq.createError(new StanzaError(new ItemNotFound())));
                                e.consume();
                            }
                        } else {
                            CommandSession commandSession = commandSessionMap.get(sessionId);
                            if (commandSession == null) {
                                StanzaError stanzaError = new StanzaError(new ItemNotFound());
                                // stanzaError.setExtension();
                                xmppSession.send(iq.createError(stanzaError));
                                e.consume();
                            }
                        }
                    }
                }
            }
        });

        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            serviceDiscoveryManager.addInfoNode(infoNode);
            serviceDiscoveryManager.addItemNode(itemNode);
        } else {
            serviceDiscoveryManager.removeInfoNode(Command.NAMESPACE);
            serviceDiscoveryManager.removeItemNode(Command.NAMESPACE);
        }
    }

    public List<AdHocCommand> getCommands() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(null, Command.NAMESPACE);
        List<AdHocCommand> commands = new ArrayList<>();
        for (Item item : itemNode.getItems()) {
            commands.add(new AdHocCommand(serviceDiscoveryManager, xmppSession, item.getName(), item.getNode()));
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
