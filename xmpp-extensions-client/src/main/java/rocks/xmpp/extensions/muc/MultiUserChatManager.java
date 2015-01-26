/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.muc;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.extensions.disco.DefaultItemProvider;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.muc.conference.model.DirectInvitation;
import rocks.xmpp.extensions.muc.model.Muc;
import rocks.xmpp.extensions.muc.model.user.Invite;
import rocks.xmpp.extensions.muc.model.user.MucUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages Multi-User Chat.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html">XEP-0045: Multi-User Chat</a>
 */
public final class MultiUserChatManager extends ExtensionManager implements SessionStatusListener, MessageListener {
    private static final Logger logger = Logger.getLogger(MultiUserChatManager.class.getName());

    private static final String ROOMS_NODE = "http://jabber.org/protocol/muc#rooms";

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Set<InvitationListener> invitationListeners = new CopyOnWriteArraySet<>();

    private final Map<Jid, Item> enteredRoomsMap = new ConcurrentHashMap<>();

    private MultiUserChatManager(final XmppSession xmppSession) {
        super(xmppSession, Muc.NAMESPACE);
        this.serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
    }

    @Override
    protected void initialize() {
        xmppSession.addSessionStatusListener(this);

        // Listen for incoming invitations.
        xmppSession.addMessageListener(this);
        serviceDiscoveryManager.setItemProvider(ROOMS_NODE, new DefaultItemProvider(enteredRoomsMap.values()));
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-service">6.1 Discovering a MUC Service</a>
     * @deprecated Use {@link #discoverChatServices()}
     */
    @Deprecated
    public Collection<ChatService> getChatServices() throws XmppException {
        return discoverChatServices();
    }

    /**
     * Discovers the multi-user chat services hosted at the connected domain.
     *
     * @return The list of chat services.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-service">6.1 Discovering a MUC Service</a>
     */
    public Collection<ChatService> discoverChatServices() throws XmppException {
        Collection<Item> services = serviceDiscoveryManager.discoverServices(Muc.NAMESPACE);
        Collection<ChatService> chatServices = new ArrayList<>();
        for (Item service : services) {
            chatServices.add(new ChatService(service.getJid(), service.getName(), xmppSession, serviceDiscoveryManager, this));
        }
        return chatServices;
    }

    /**
     * Discovers the rooms, where a contact is in.
     *
     * @param contact The contact, which must be a full JID.
     * @return The items, {@link rocks.xmpp.extensions.disco.model.items.Item#getJid()} has the room address, and {@link rocks.xmpp.extensions.disco.model.items.Item#getName()}} has the nickname.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-client">6.7 Discovering Client Support for MUC</a>
     */
    public Collection<Item> discoverEnteredRooms(Jid contact) throws XmppException {
        return serviceDiscoveryManager.discoverItems(contact, ROOMS_NODE).getItems();
    }

    /**
     * Creates a chat service for the specified service address.
     *
     * @param chatService The chat service address. Usually this is hosted at the subdomain "conference".
     * @return The chat service.
     */
    public ChatService createChatService(Jid chatService) {
        return new ChatService(chatService, null, xmppSession, serviceDiscoveryManager, this);
    }

    @Override
    public void handleMessage(MessageEvent e) {
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

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            invitationListeners.clear();
        }
    }

    void roomEntered(ChatRoom chatRoom, String nick) {
        enteredRoomsMap.put(chatRoom.getAddress(), new Item(chatRoom.getAddress(), null, nick));
    }

    void roomExited(ChatRoom chatRoom) {
        enteredRoomsMap.remove(chatRoom.getAddress());
    }
}
