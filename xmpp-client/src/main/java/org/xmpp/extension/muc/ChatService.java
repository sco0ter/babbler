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

import org.xmpp.XmppSession;
import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class ChatService {

    private final XmppSession xmppSession;

    private final Jid serviceAddress;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    public ChatService(Jid serviceAddress, XmppSession xmppSession, ServiceDiscoveryManager serviceDiscoveryManager) {
        this.xmppSession = xmppSession;
        this.serviceAddress = serviceAddress;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
    }

    public List<ChatRoom> getPublicRooms() throws XmppException {
        List<ChatRoom> chatRooms = new ArrayList<>();
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(serviceAddress);
        for (Item item : itemNode.getItems()) {
            chatRooms.add(new ChatRoom(item.getName(), item.getJid(), xmppSession));
        }
        return chatRooms;
    }

    public ChatRoom createRoom(String room) {
        return new ChatRoom(null, new Jid(room, serviceAddress.getDomain()), xmppSession);
    }
}
