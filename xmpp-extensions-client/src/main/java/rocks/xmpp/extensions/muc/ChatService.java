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
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A chat service hosts chat rooms. It allows you to discover public chat rooms or create new (instant) rooms, if allowed by the service.
 * <p>
 * You get an instance of this class by either using the {@link MultiUserChatManager#createChatService(rocks.xmpp.core.Jid)} method or by {@linkplain MultiUserChatManager#getChatServices() discovering} the chat services at your connected domain.
 * </p>
 *
 * @author Christian Schudt
 * @see MultiUserChatManager#createChatService(rocks.xmpp.core.Jid)
 */
public final class ChatService implements Comparable<ChatService> {

    private final XmppSession xmppSession;

    private final Jid serviceAddress;

    private final String name;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final MultiUserChatManager multiUserChatManager;

    ChatService(Jid serviceAddress, String name, XmppSession xmppSession, ServiceDiscoveryManager serviceDiscoveryManager, MultiUserChatManager multiUserChatManager) {
        this.xmppSession = xmppSession;
        this.serviceAddress = serviceAddress;
        this.name = name;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.multiUserChatManager = multiUserChatManager;
    }

    /**
     * Gets the list of public chat rooms hosted by this chat service.
     *
     * @return The public rooms.
     * @throws rocks.xmpp.core.stanza.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @deprecated Use {@link #discoverRooms()}
     */
    @Deprecated
    public List<ChatRoom> getPublicRooms() throws XmppException {
        return discoverRooms();
    }

    /**
     * Discovers the list of chat rooms hosted by this chat service.
     *
     * @return The public rooms.
     * @throws rocks.xmpp.core.stanza.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-rooms">6.3 Discovering Rooms</a>
     */
    public List<ChatRoom> discoverRooms() throws XmppException {
        List<ChatRoom> chatRooms = new ArrayList<>();
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(serviceAddress);
        for (Item item : itemNode.getItems()) {
            ChatRoom chatRoom = new ChatRoom(item.getJid(), item.getName(), xmppSession, serviceDiscoveryManager, multiUserChatManager);
            chatRoom.initialize();
            chatRooms.add(chatRoom);
        }
        return chatRooms;
    }

    /**
     * Creates a new chat room. Note that this room is only created locally.
     *
     * @param room The room. This is the local part of the room address, e.g. room@service.
     * @return The chat room.
     */
    public ChatRoom createRoom(String room) {
        ChatRoom chatRoom = new ChatRoom(new Jid(room, serviceAddress.getDomain()), null, xmppSession, serviceDiscoveryManager, multiUserChatManager);
        chatRoom.initialize();
        return chatRoom;
    }

    /**
     * Gets the service address.
     *
     * @return The service address.
     */
    public Jid getAddress() {
        return serviceAddress;
    }

    /**
     * Gets the name of this service.
     *
     * @return The name or null, if the name is unknown.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return serviceAddress != null ? serviceAddress.toString() : super.toString();
    }

    /**
     * Compares this chat service first by their name and then by their service address.
     *
     * @param o The other chat service.
     * @return The comparison result.
     */
    @Override
    public int compareTo(ChatService o) {
        if (this == o) {
            return 0;
        }
        if (o != null) {
            int result;
            // First compare name.
            if (name != null) {
                if (o.name != null) {
                    result = name.compareTo(o.name);
                } else {
                    result = -1;
                }
            } else {
                if (o.name != null) {
                    result = 1;
                } else {
                    result = 0;
                }
            }
            // If the names are equal, compare addresses.
            if (result == 0) {
                if (serviceAddress != null) {
                    if (o.serviceAddress != null) {
                        result = serviceAddress.compareTo(o.serviceAddress);
                    } else {
                        result = -1;
                    }
                } else {
                    if (o.serviceAddress != null) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
            }
            return result;
        } else {
            return -1;
        }
    }
}
