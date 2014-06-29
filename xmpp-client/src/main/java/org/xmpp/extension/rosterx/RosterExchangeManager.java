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

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.delay.DelayedDelivery;
import org.xmpp.im.Contact;
import org.xmpp.im.RosterManager;
import org.xmpp.stanza.*;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;
import org.xmpp.stanza.errors.NotAuthorized;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages roster item exchange.
 *
 * @author Christian Schudt
 */
public final class RosterExchangeManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(RosterExchangeManager.class.getName());

    private final Set<RosterExchangeListener> rosterExchangeListeners = new CopyOnWriteArraySet<>();

    private final Collection<Jid> trustedEntities = new CopyOnWriteArraySet<>();

    private RosterExchangeManager(final XmppSession xmppSession) {
        super(xmppSession, RosterExchange.NAMESPACE);

        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    Message message = e.getMessage();
                    RosterExchange rosterExchange = message.getExtension(RosterExchange.class);
                    if (rosterExchange != null) {
                        List<RosterExchange.Item> items = getItemsToProcess(rosterExchange.getItems());
                        if (!items.isEmpty()) {
                            Date date;
                            DelayedDelivery delayedDelivery = message.getExtension(DelayedDelivery.class);
                            if (delayedDelivery != null) {
                                date = delayedDelivery.getTimeStamp();
                            } else {
                                date = new Date();
                            }
                            processItems(items, message.getFrom(), message.getBody(), date);
                        }
                    }
                }
            }
        });

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    RosterExchange rosterExchange = iq.getExtension(RosterExchange.class);
                    if (rosterExchange != null) {
                        if (isEnabled()) {
                            if (iq.getType() == AbstractIQ.Type.SET) {
                                if (xmppSession.getRosterManager().getContact(iq.getFrom().asBareJid()) == null) {
                                    // If the receiving entity will not process the suggested action(s) because the sending entity is not in the receiving entity's roster, the receiving entity MUST return an error to the sending entity, which error SHOULD be <not-authorized/>.
                                    xmppSession.send(iq.createError(new StanzaError(new NotAuthorized())));
                                } else {
                                    List<RosterExchange.Item> items = getItemsToProcess(rosterExchange.getItems());
                                    if (!items.isEmpty()) {
                                        processItems(items, iq.getFrom(), null, new Date());
                                    }
                                    xmppSession.send(iq.createResult());
                                }
                            } else if (iq.getType() == AbstractIQ.Type.GET) {
                                sendServiceUnavailable(iq);
                            }
                        } else if (iq.getType() == AbstractIQ.Type.GET || iq.getType() == AbstractIQ.Type.SET) {
                            sendServiceUnavailable(iq);
                        }
                    }
                }
            }
        });
    }

    private void processItems(List<RosterExchange.Item> items, Jid sender, String message, Date date) {
        if (getTrustedEntities().contains(sender.asBareJid())) {
            for (RosterExchange.Item item : items) {
                try {
                    approve(item);
                } catch (XmppException e1) {
                    logger.log(Level.SEVERE, "Auto approving roster exchange item failed: " + e1.getMessage(), e1);
                }
            }
        } else {
            for (RosterExchangeListener rosterExchangeListener : rosterExchangeListeners) {
                try {
                    rosterExchangeListener.rosterItemExchangeSuggested(new RosterExchangeEvent(this, items, sender, message, date));
                } catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
    }

    List<RosterExchange.Item> getItemsToProcess(List<RosterExchange.Item> items) {
        List<RosterExchange.Item> newItems = new ArrayList<>();
        for (RosterExchange.Item item : items) {
            Contact contact = xmppSession.getRosterManager().getContact(item.getJid());
            // If "action" attribute is missing, it is implicitly "add" by default.
            if (item.getAction() == null || item.getAction() == RosterExchange.Item.Action.ADD) {
                if (contact != null) {
                    // 1. If the item already exists in the roster and the item is in the specified group (or no group is specified),
                    // the receiving application MUST NOT prompt a human user for approval regarding that item and MUST NOT add that item to the roster.

                    // 3. If the item already exists in the roster but not in the specified group, the receiving application MAY prompt the user
                    // for approval and SHOULD edit the existing item so that will also belong to the specified group (in addition to the existing group, if any).
                    List<String> specifiedGroups = new ArrayList<>(item.getGroups());
                    // Remove all existing groups.
                    specifiedGroups.removeAll(contact.getGroups());
                    // If there are still new groups.
                    if (!specifiedGroups.isEmpty()) {
                        // Only notify if the item will be added to new groups.
                        newItems.add(new RosterExchange.Item(item.getJid(), item.getName(), specifiedGroups, RosterExchange.Item.Action.ADD));
                    }
                } else {
                    // 2. If the item does not already exist in the roster, the receiving application SHOULD prompt a human user for approval
                    // regarding that item and, if approval is granted, MUST add that item to the roster.
                    newItems.add(item);
                }
            } else if (item.getAction() == RosterExchange.Item.Action.DELETE) {
                // 1. If the item does not exist in the roster, the receiving application MUST NOT prompt a human user for approval regarding that item and MUST NOT delete that item from the roster.
                if (contact != null) {
                    // 2. If the item exists in the roster but not in the specified group, the receiving application MUST NOT prompt the user for approval and MUST NOT delete the existing item.
                    // 3. If the item exists in the roster and is in both the specified group and another group, the receiving application MAY prompt the user for approval and SHOULD edit the existing item so that it no longer belongs to the specified group.
                    List<String> specifiedGroups = new ArrayList<>(item.getGroups());
                    // Retain only the groups, that exist in the roster.
                    specifiedGroups.retainAll(contact.getGroups());
                    if (!specifiedGroups.isEmpty() || contact.getGroups().isEmpty()) {
                        // Only notify if the item will be added to new groups.
                        newItems.add(new RosterExchange.Item(item.getJid(), item.getName(), specifiedGroups, RosterExchange.Item.Action.DELETE));
                    }
                }
            } else if (item.getAction() == RosterExchange.Item.Action.MODIFY) {
                // 1. If the item does not exist in the roster, the receiving application MUST NOT prompt a human user for approval regarding that item and MUST NOT add that item to the roster.
                if (contact != null) {
                    newItems.add(item);
                }
            }
        }

        return newItems;
    }

    /**
     * Gets a collection of trusted entities for which roster item exchange suggestions are approved automatically (no listeners will be called).
     * The JIDs contained in this collection must be bare JIDs.
     *
     * @return The trusted entities.
     * @see <a href="http://xmpp.org/extensions/xep-0144.html#security-trust">8.1 Trusted Entities</a>
     */
    public Collection<Jid> getTrustedEntities() {
        return trustedEntities;
    }

    /**
     * Suggests the addition of one or more contacts to another user.
     *
     * @param jid      The recipient.
     * @param contacts The contacts
     * @throws StanzaException              If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException If the entity did not respond.
     */
    public void suggestContactAddition(Jid jid, Contact... contacts) throws XmppException {

        // Only support adding contacts for now:
        // However, if the sender is a human user and/or the sending application has a primary Service Discovery category of "client" (e.g., a bot) [10], the sending application SHOULD NOT specify an 'action' attribute other than "add"

        if (contacts.length > 0) {
            RosterExchange rosterExchange = new RosterExchange();
            for (Contact contact : contacts) {
                if (contact.getJid() == null) {
                    // ... MUST possess a 'jid' attribute that specifies the JabberID of the item to be added
                    throw new IllegalArgumentException("Contact contains no JID.");
                }
                RosterExchange.Item rosterItem = new RosterExchange.Item(contact.getJid(), contact.getName(), contact.getGroups(), RosterExchange.Item.Action.ADD);
                rosterExchange.getItems().add(rosterItem);
            }
            // http://xmpp.org/extensions/xep-0144.html#stanza
            Presence presence = xmppSession.getPresenceManager().getPresence(jid);
            if (presence.isAvailable()) {
                xmppSession.query(new IQ(presence.getFrom(), IQ.Type.SET, rosterExchange));
            } else {
                // If the sending entity does not know that the receiving entity is online and available, it MUST send a <message/> stanza to the receiving entity's "bare JID" (user@host) rather than an <iq/> stanza to a particular resource.
                Message message = new Message(jid, Message.Type.NORMAL);
                message.getExtensions().add(rosterExchange);
                xmppSession.send(message);
            }
        }
    }

    /**
     * Approves a roster exchange item by modifying the roster accordingly.
     *
     * @param item The roster exchange item.
     * @return The action, which was actually performed. This may vary from the specified action, e.g. if you add a contact that already exists, only its groups are updated. If no action was performed, e.g. if you want to delete a contact, that does not exist, null is returned.
     * @throws StanzaException              If the entity returned a stanza error.
     * @throws org.xmpp.NoResponseException If the entity did not respond.
     */
    public RosterExchange.Item.Action approve(RosterExchange.Item item) throws XmppException {
        RosterManager rosterManager = xmppSession.getRosterManager();
        Contact contact = rosterManager.getContact(item.getJid());
        RosterExchange.Item.Action action = null;
        if (item.getAction() == null || item.getAction() == RosterExchange.Item.Action.ADD) {
            // If the contact does not exist yes, add it and request subscription.
            // (After completing the roster set, the receiving application SHOULD also send a <presence/> stanza of type "subscribe" to the JID of the new item.)
            if (contact == null) {
                rosterManager.addContact(new Contact(item.getJid(), item.getName(), item.getGroups()), true, null);
                action = RosterExchange.Item.Action.ADD;
            } else {
                List<String> newGroups = new ArrayList<>(contact.getGroups());
                List<String> additionalGroups = new ArrayList<>(item.getGroups());
                // Remove all existing groups from the list.
                additionalGroups.removeAll(newGroups);
                if (!additionalGroups.isEmpty()) {
                    // Then add all additional groups to the existing groups.
                    newGroups.addAll(additionalGroups);
                    // ... SHOULD edit the existing item so that will also belong to the specified group (in addition to the existing group, if any).
                    rosterManager.updateContact(new Contact(contact.getJid(), contact.getName(), newGroups));
                    action = RosterExchange.Item.Action.MODIFY;
                }
            }
        } else if (item.getAction() == RosterExchange.Item.Action.DELETE) {
            if (contact != null) {
                List<String> existingGroups = new ArrayList<>(contact.getGroups());
                List<String> specifiedGroups = new ArrayList<>(item.getGroups());
                // Remove all specified groups from the existing groups.
                existingGroups.removeAll(specifiedGroups);
                // If there are still some groups left, only update the contact, but do not delete it.
                if (!existingGroups.isEmpty()) {
                    rosterManager.updateContact(new Contact(contact.getJid(), contact.getName(), existingGroups));
                    action = RosterExchange.Item.Action.MODIFY;
                } else {
                    rosterManager.removeContact(item.getJid());
                    action = RosterExchange.Item.Action.DELETE;
                }
            }
        } else if (item.getAction() == RosterExchange.Item.Action.MODIFY) {
            if (contact != null) {
                rosterManager.updateContact(new Contact(item.getJid(), item.getName(), item.getGroups()));
                action = RosterExchange.Item.Action.MODIFY;
            }
        }
        return action;
    }

    /**
     * Adds a roster exchange listener.
     *
     * @param rosterExchangeListener The listener.
     * @see #removeRosterExchangeListener(RosterExchangeListener)
     */
    public void addRosterExchangeListener(RosterExchangeListener rosterExchangeListener) {
        rosterExchangeListeners.add(rosterExchangeListener);
    }

    /**
     * Removes a previously added roster exchange listener.
     *
     * @param rosterExchangeListener The listener.
     * @see #addRosterExchangeListener(RosterExchangeListener)
     */
    public void removeRosterExchangeListener(RosterExchangeListener rosterExchangeListener) {
        rosterExchangeListeners.remove(rosterExchangeListener);
    }
}
