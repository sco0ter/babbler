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

package org.xmpp.extension.rosterx;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.im.Contact;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.Presence;

import java.util.List;

/**
 * @author Christian Schudt
 */
public class RosterExchangeManager extends ExtensionManager {
    private RosterExchangeManager(Connection connection) {
        super(connection);
    }

    public void suggestContactAddition(Jid jid, List<Contact> contacts) throws XmppException {
        RosterExchange rosterExchange = new RosterExchange();
        for (Contact contact : contacts) {
            RosterExchange.Item rosterItem = new RosterExchange.Item(contact.getJid(), contact.getName(), contact.getGroups(), RosterExchange.Item.Action.ADD);
            rosterExchange.getItems().add(rosterItem);
        }
        // http://xmpp.org/extensions/xep-0144.html#stanza
        Presence presence = connection.getPresenceManager().getPresence(jid);
        if (presence.isAvailable()) {
            connection.query(new IQ(presence.getFrom(), IQ.Type.SET, rosterExchange));
        } else {
            connection.send(new Message(jid, Message.Type.NORMAL));
        }
    }
}
