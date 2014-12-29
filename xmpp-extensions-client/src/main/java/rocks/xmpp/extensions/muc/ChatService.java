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
public final class ChatService {

    private final XmppSession xmppSession;

    private final Jid serviceAddress;

    private final String name;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    ChatService(Jid serviceAddress, String name, XmppSession xmppSession, ServiceDiscoveryManager serviceDiscoveryManager) {
        this.xmppSession = xmppSession;
        this.serviceAddress = serviceAddress;
        this.name = name;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
    }

    /**
     * Gets the list of public chat rooms hosted by this chat service.
     *
     * @return The public rooms.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     */
    public List<ChatRoom> getPublicRooms() throws XmppException {
        List<ChatRoom> chatRooms = new ArrayList<>();
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(serviceAddress);
        for (Item item : itemNode.getItems()) {
            chatRooms.add(new ChatRoom(item.getName(), item.getJid(), xmppSession));
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
        return new ChatRoom(null, new Jid(room, serviceAddress.getDomain()), xmppSession);
    }

    /**
     * Gets the service address.
     *
     * @return The service address.
     */
    public Jid getAddress() {
        return serviceAddress;
    }

    @Override
    public String toString() {
        return serviceAddress.toString();
    }

    /**
     * Gets the name of this service.
     *
     * @return The name or null, if the name is unknown.
     */
    public String getName() {
        return name;
    }
}
