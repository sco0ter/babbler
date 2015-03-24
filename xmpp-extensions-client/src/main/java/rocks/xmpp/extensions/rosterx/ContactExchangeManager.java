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

package rocks.xmpp.extensions.rosterx;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.roster.RosterManager;
import rocks.xmpp.core.roster.model.Contact;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.subscription.PresenceManager;
import rocks.xmpp.extensions.delay.model.DelayedDelivery;
import rocks.xmpp.extensions.rosterx.model.ContactExchange;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages contact exchange between entities.
 *
 * @author Christian Schudt
 */
public final class ContactExchangeManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(ContactExchangeManager.class.getName());

    private final Set<ContactExchangeListener> contactExchangeListeners = new CopyOnWriteArraySet<>();

    private final Collection<Jid> trustedEntities = new CopyOnWriteArraySet<>();

    private ContactExchangeManager(final XmppSession xmppSession) {
        super(xmppSession, ContactExchange.NAMESPACE);
    }

    @Override
    protected void initialize() {
        xmppSession.addSessionStatusListener(e -> {
            if (e.getStatus() == XmppSession.Status.CLOSED) {
                contactExchangeListeners.clear();
                trustedEntities.clear();
            }
        });
        xmppSession.addInboundMessageListener(e -> {
            if (isEnabled()) {
                Message message = e.getMessage();
                ContactExchange contactExchange = message.getExtension(ContactExchange.class);
                if (contactExchange != null) {
                    List<ContactExchange.Item> items = getItemsToProcess(contactExchange.getItems());
                    if (!items.isEmpty()) {
                        processItems(items, message.getFrom(), message.getBody(), DelayedDelivery.deliveryDateOrNow(message));
                    }
                }
            }
        });
        xmppSession.addIQHandler(ContactExchange.class, new AbstractIQHandler(this, AbstractIQ.Type.SET) {
            @Override
            protected IQ processRequest(IQ iq) {
                ContactExchange contactExchange = iq.getExtension(ContactExchange.class);
                if (xmppSession.getManager(RosterManager.class).getContact(iq.getFrom().asBareJid()) == null) {
                    // If the receiving entity will not process the suggested action(s) because the sending entity is not in the receiving entity's roster, the receiving entity MUST return an error to the sending entity, which error SHOULD be <not-authorized/>.
                    return iq.createError(Condition.NOT_AUTHORIZED);
                } else {
                    List<ContactExchange.Item> items = getItemsToProcess(contactExchange.getItems());
                    if (!items.isEmpty()) {
                        processItems(items, iq.getFrom(), null, Instant.now());
                    }
                    return iq.createResult();
                }
            }
        });
    }

    private void processItems(List<ContactExchange.Item> items, Jid sender, String message, Instant date) {
        if (getTrustedEntities().contains(sender.asBareJid())) {
            for (ContactExchange.Item item : items) {
                try {
                    approve(item);
                } catch (XmppException e1) {
                    logger.log(Level.SEVERE, e1, () -> "Auto approving roster exchange item failed: " + e1.getMessage());
                }
            }
        } else {
            for (ContactExchangeListener contactExchangeListener : contactExchangeListeners) {
                try {
                    contactExchangeListener.contactExchangeSuggested(new ContactExchangeEvent(this, items, sender, message, date));
                } catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
    }

    List<ContactExchange.Item> getItemsToProcess(List<ContactExchange.Item> items) {
        List<ContactExchange.Item> newItems = new ArrayList<>();
        for (ContactExchange.Item item : items) {
            Contact contact = xmppSession.getManager(RosterManager.class).getContact(item.getJid());
            // If "action" attribute is missing, it is implicitly "add" by default.
            if (item.getAction() == null || item.getAction() == ContactExchange.Item.Action.ADD) {
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
                        newItems.add(new ContactExchange.Item(item.getJid(), item.getName(), specifiedGroups, ContactExchange.Item.Action.ADD));
                    }
                } else {
                    // 2. If the item does not already exist in the roster, the receiving application SHOULD prompt a human user for approval
                    // regarding that item and, if approval is granted, MUST add that item to the roster.
                    newItems.add(item);
                }
            } else if (item.getAction() == ContactExchange.Item.Action.DELETE) {
                // 1. If the item does not exist in the roster, the receiving application MUST NOT prompt a human user for approval regarding that item and MUST NOT delete that item from the roster.
                if (contact != null) {
                    // 2. If the item exists in the roster but not in the specified group, the receiving application MUST NOT prompt the user for approval and MUST NOT delete the existing item.
                    // 3. If the item exists in the roster and is in both the specified group and another group, the receiving application MAY prompt the user for approval and SHOULD edit the existing item so that it no longer belongs to the specified group.
                    List<String> specifiedGroups = new ArrayList<>(item.getGroups());
                    // Retain only the groups, that exist in the roster.
                    specifiedGroups.retainAll(contact.getGroups());
                    if (!specifiedGroups.isEmpty() || contact.getGroups().isEmpty()) {
                        // Only notify if the item will be added to new groups.
                        newItems.add(new ContactExchange.Item(item.getJid(), item.getName(), specifiedGroups, ContactExchange.Item.Action.DELETE));
                    }
                }
            } else if (item.getAction() == ContactExchange.Item.Action.MODIFY) {
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
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public void suggestContactAddition(Jid jid, Contact... contacts) throws XmppException {

        // Only support adding contacts for now:
        // However, if the sender is a human user and/or the sending application has a primary Service Discovery category of "client" (e.g., a bot) [10], the sending application SHOULD NOT specify an 'action' attribute other than "add"

        if (contacts.length > 0) {
            ContactExchange contactExchange = new ContactExchange();
            for (Contact contact : contacts) {
                if (contact.getJid() == null) {
                    // ... MUST possess a 'jid' attribute that specifies the JabberID of the item to be added
                    throw new IllegalArgumentException("Contact contains no JID.");
                }
                ContactExchange.Item rosterItem = new ContactExchange.Item(contact.getJid(), contact.getName(), contact.getGroups(), ContactExchange.Item.Action.ADD);
                contactExchange.getItems().add(rosterItem);
            }
            // http://xmpp.org/extensions/xep-0144.html#stanza
            Presence presence = xmppSession.getManager(PresenceManager.class).getPresence(jid);
            if (presence.isAvailable()) {
                xmppSession.query(new IQ(presence.getFrom(), IQ.Type.SET, contactExchange));
            } else {
                // If the sending entity does not know that the receiving entity is online and available, it MUST send a <message/> stanza to the receiving entity's "bare JID" (user@host) rather than an <iq/> stanza to a particular resource.
                Message message = new Message(jid, Message.Type.NORMAL);
                message.getExtensions().add(contactExchange);
                xmppSession.send(message);
            }
        }
    }

    /**
     * Approves a roster exchange item by modifying the roster accordingly.
     * <p>
     * If the item is to be added and does not yet exist, it will be added to your roster and you are subscribed to its presence.<br>
     * If it already exists in your roster but in a different group than suggested, it will additionally be added to the suggested group.
     * </p>
     * <p>
     * If the item is to be deleted, it will be deleted from your roster, if the suggested group(s) matches the same group(s) in your roster.<br>
     * Otherwise it will be edited, so that it no longer belongs to the suggested groups.
     * </p>
     * <p>
     * If the item is to be modified, it will be modified accordingly, if it exists.
     * </p>
     *
     * @param item The roster exchange item.
     * @return The action, which was actually performed. This may vary from the specified action, e.g. if you add a contact that already exists, only its groups are updated. If no action was performed, e.g. if you want to delete a contact, that does not exist, null is returned.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public ContactExchange.Item.Action approve(ContactExchange.Item item) throws XmppException {
        RosterManager rosterManager = xmppSession.getManager(RosterManager.class);
        Contact contact = rosterManager.getContact(item.getJid());
        ContactExchange.Item.Action action = null;
        if (item.getAction() == null || item.getAction() == ContactExchange.Item.Action.ADD) {
            // If the contact does not exist yes, add it and request subscription.
            // (After completing the roster set, the receiving application SHOULD also send a <presence/> stanza of type "subscribe" to the JID of the new item.)
            if (contact == null) {
                rosterManager.addContact(new Contact(item.getJid(), item.getName(), item.getGroups()), true, null);
                action = ContactExchange.Item.Action.ADD;
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
                    action = ContactExchange.Item.Action.MODIFY;
                }
            }
        } else if (item.getAction() == ContactExchange.Item.Action.DELETE) {
            if (contact != null) {
                List<String> existingGroups = new ArrayList<>(contact.getGroups());
                List<String> specifiedGroups = new ArrayList<>(item.getGroups());
                // Remove all specified groups from the existing groups.
                existingGroups.removeAll(specifiedGroups);
                // If there are still some groups left, only update the contact, but do not delete it.
                if (!existingGroups.isEmpty()) {
                    rosterManager.updateContact(new Contact(contact.getJid(), contact.getName(), existingGroups));
                    action = ContactExchange.Item.Action.MODIFY;
                } else {
                    rosterManager.removeContact(item.getJid());
                    action = ContactExchange.Item.Action.DELETE;
                }
            }
        } else if (item.getAction() == ContactExchange.Item.Action.MODIFY) {
            if (contact != null) {
                rosterManager.updateContact(new Contact(item.getJid(), item.getName(), item.getGroups()));
                action = ContactExchange.Item.Action.MODIFY;
            }
        }
        return action;
    }

    /**
     * Adds a contact exchange listener.
     *
     * @param contactExchangeListener The listener.
     * @see #removeContactExchangeListener(ContactExchangeListener)
     */
    public void addContactExchangeListener(ContactExchangeListener contactExchangeListener) {
        contactExchangeListeners.add(contactExchangeListener);
    }

    /**
     * Removes a previously added contact exchange listener.
     *
     * @param contactExchangeListener The listener.
     * @see #addContactExchangeListener(ContactExchangeListener)
     */
    public void removeContactExchangeListener(ContactExchangeListener contactExchangeListener) {
        contactExchangeListeners.remove(contactExchangeListener);
    }
}
