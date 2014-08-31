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

package org.xmpp.extension.muc;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.extension.muc.conference.DirectInvitation;
import org.xmpp.extension.muc.user.Invite;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages Multi-User Chat.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html">XEP-0045: Multi-User Chat</a>
 */
public final class MultiUserChatManager extends ExtensionManager {
    private static final Logger logger = Logger.getLogger(MultiUserChatManager.class.getName());

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Set<InvitationListener> invitationListeners = new CopyOnWriteArraySet<>();

    private MultiUserChatManager(final XmppSession xmppSession) {
        super(xmppSession, Muc.NAMESPACE);

        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    invitationListeners.clear();
                }
            }
        });

        // Listen for incoming invitations.
        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    Message message = e.getMessage();
                    // Check, if the message contains a mediated invitation.
                    MucUser mucUser = message.getExtension(MucUser.class);
                    if (mucUser != null) {
                        for (Invite invite : mucUser.getInvites()) {
                            notifyListeners(new InvitationEvent(MultiUserChatManager.this, xmppSession, invite.getFrom(), message.getFrom(), invite.getReason(), mucUser.getPassword(), invite.isContinue(), invite.getThread(), true));
                        }
                    } else {
                        // Check, if the message contains a direct invitation.
                        DirectInvitation directInvitation = message.getExtension(DirectInvitation.class);
                        if (directInvitation != null) {
                            notifyListeners(new InvitationEvent(MultiUserChatManager.this, xmppSession, message.getFrom(), directInvitation.getRoomAddress(), directInvitation.getReason(), directInvitation.getPassword(), directInvitation.isContinue(), directInvitation.getThread(), false));
                        }
                    }
                }
            }
        });
        this.serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
    }

    private void notifyListeners(InvitationEvent invitationEvent) {
        for (InvitationListener invitationListener : invitationListeners) {
            try {
                invitationListener.invitationReceived(invitationEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Adds an invitation listener, which allows to listen for incoming multi-user chat invitations.
     *
     * @param invitationListener The listener.
     * @see #removeInvitationListener(InvitationListener)
     */
    public void addInvitationListener(InvitationListener invitationListener) {
        invitationListeners.add(invitationListener);
    }

    /**
     * Removes a previously added invitation listener.
     *
     * @param invitationListener The listener.
     * @see #addInvitationListener(InvitationListener)
     */
    public void removeInvitationListener(InvitationListener invitationListener) {
        invitationListeners.remove(invitationListener);
    }

    /**
     * Discovers the multi-user chat services hosted at the connected domain.
     *
     * @return The list of chat services.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-service">6.1 Discovering a MUC Service</a>
     */
    public Collection<ChatService> getChatServices() throws XmppException {
        ItemNode itemDiscovery = serviceDiscoveryManager.discoverItems(null);
        Collection<ChatService> chatServices = new ArrayList<>();

        for (Item item : itemDiscovery.getItems()) {
            InfoNode infoDiscovery = serviceDiscoveryManager.discoverInformation(item.getJid());
            if (infoDiscovery.getFeatures().contains(new Feature(Muc.NAMESPACE))) {
                chatServices.add(new ChatService(item.getJid(), xmppSession, serviceDiscoveryManager));
            }
        }
        return chatServices;
    }

    /**
     * Creates a chat service for the specified service address.
     *
     * @param chatService The chat service address. Usually this is hosted at the subdomain "conference".
     * @return The chat service.
     */
    public ChatService createChatService(Jid chatService) {
        return new ChatService(chatService, xmppSession, serviceDiscoveryManager);
    }
}
