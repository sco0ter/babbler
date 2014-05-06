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

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.stanza.*;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Christian Schudt
 */
public class ChatService {

    private final Connection connection;

    private final Jid serviceAddress;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Lock lock = new ReentrantLock();

    public ChatService(Jid serviceAddress, Connection connection, ServiceDiscoveryManager serviceDiscoveryManager) {
        this.connection = connection;
        this.serviceAddress = serviceAddress;
        this.serviceDiscoveryManager = serviceDiscoveryManager;

        connection.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    Message message = e.getMessage();
                    if (message.getBody() == null && message.getSubject() != null) {
                        // Subject changed.
                    }
                }
            }
        });
    }

    public List<ChatRoom> getPublicRooms() throws XmppException {
        List<ChatRoom> chatRooms = new ArrayList<>();
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(serviceAddress);
        for (Item item : itemNode.getItems()) {
            chatRooms.add(new ChatRoom(item.getName(), item.getJid()));
        }
        return chatRooms;
    }

    public void joinRoom(String room, String nick) throws XmppException {
        joinRoom(room, nick, null, null);
    }

    public void joinRoom(String room, String nick, String password) throws XmppException {
        joinRoom(room, nick, password, null);
    }

    public void joinRoom(String room, String nick, History history) throws XmppException {
        joinRoom(room, nick, null, history);
    }

    public void joinRoom(String room, String nick, String password, History history) throws XmppException {
        Presence presence = new Presence();
        presence.setTo(new Jid(room, serviceAddress.getDomain(), nick));
        presence.getExtensions().add(new Muc(password, history));
        waitForPresence(presence);
    }

    public void waitForPresence(final Presence presence) throws XmppException {
        final Presence[] result = new Presence[1];

        final Condition resultReceived = lock.newCondition();

        final PresenceListener iqListener = new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (e.isIncoming() && e.getPresence().getFrom() != null && e.getPresence().getFrom().equals(presence.getTo())) {
                    lock.lock();
                    try {
                        result[0] = e.getPresence();
                    } finally {
                        resultReceived.signal();
                        lock.unlock();
                    }
                }
            }
        };

        lock.lock();
        try {
            connection.addPresenceListener(iqListener);
            connection.send(presence);
            if (!resultReceived.await(5000, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on an IQ response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
            connection.removePresenceListener(iqListener);
        }

        Presence response = result[0];
        if (response.getType() == Presence.Type.ERROR) {
            throw new StanzaException(response);
        }
    }
}
