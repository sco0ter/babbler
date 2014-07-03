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

package org.xmpp.extension.blocking;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.IQ;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This manager allows to block communications with contacts.
 * <p>
 * Enabling or disabling this manager has no effect, because blocking is done on server side.
 * </p>
 *
 * @author Christian Schudt
 */
public final class BlockingManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(BlockingManager.class.getName());

    private final Set<Jid> blockedContacts = new HashSet<>();

    private final Set<BlockingListener> blockingListeners = new CopyOnWriteArraySet<>();

    private BlockingManager(final XmppSession xmppSession) {
        super(xmppSession);

        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    blockingListeners.clear();
                    blockedContacts.clear();
                }
            }
        });

        // Listen for "un/block pushes"
        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    if (iq.getType() == IQ.Type.SET) {
                        Block block = iq.getExtension(Block.class);
                        synchronized (blockedContacts) {
                            if (block != null) {
                                List<Jid> pushedContacts = new ArrayList<>();
                                for (Item item : block.getItems()) {
                                    blockedContacts.add(item.getJid());
                                    pushedContacts.add(item.getJid());
                                }
                                xmppSession.send(iq.createResult());
                                notifyListeners(pushedContacts, Collections.<Jid>emptyList());
                            } else {
                                Unblock unblock = iq.getExtension(Unblock.class);
                                if (unblock != null) {
                                    List<Jid> pushedContacts = new ArrayList<>();
                                    if (unblock.getItems().isEmpty()) {
                                        // Empty means, the user has unblocked communications with all contacts.
                                        pushedContacts.addAll(blockedContacts);
                                        blockedContacts.clear();
                                    } else {
                                        for (Item item : unblock.getItems()) {
                                            blockedContacts.remove(item.getJid());
                                            pushedContacts.add(item.getJid());
                                        }
                                    }
                                    xmppSession.send(iq.createResult());
                                    notifyListeners(Collections.<Jid>emptyList(), pushedContacts);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void notifyListeners(List<Jid> blockedContacts, List<Jid> unblockedContacts) {
        for (BlockingListener blockingListener : blockingListeners) {
            try {
                blockingListener.blockListChanged(new BlockingEvent(BlockingManager.this, blockedContacts, unblockedContacts));
            } catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Adds a blocking listener, which allows to listen for block and unblock pushes.
     *
     * @param blockingListener The listener.
     * @see #removeBlockingListener(BlockingListener)
     */
    public void addBlockingListener(BlockingListener blockingListener) {
        blockingListeners.add(blockingListener);
    }

    /**
     * Removes a previously added blocking listener.
     *
     * @param blockingListener The listener.
     * @see #addBlockingListener(BlockingListener)
     */
    public void removeBlockingListener(BlockingListener blockingListener) {
        blockingListeners.remove(blockingListener);
    }

    /**
     * Retrieves the blocked contacts.
     *
     * @return The block list.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#blocklist">3.2 User Retrieves Block List</a>
     */
    public Collection<Jid> getBlockedContacts() throws XmppException {
        synchronized (blockedContacts) {
            IQ result = xmppSession.query(new IQ(IQ.Type.GET, new BlockList()));
            BlockList blockList = result.getExtension(BlockList.class);
            if (blockList != null) {
                for (Item item : blockList.getItems()) {
                    blockedContacts.add(item.getJid());
                }
            }
            return blockedContacts;
        }
    }

    /**
     * Blocks communications with contacts.
     *
     * @param jids The contacts.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#block">3.3 User Blocks Contact</a>
     */
    public void blockContact(Jid... jids) throws XmppException {
        List<Item> items = new ArrayList<>();
        for (Jid jid : jids) {
            items.add(new Item(jid));
        }
        xmppSession.query(new IQ(IQ.Type.SET, new Block(items)));
    }

    /**
     * Unblocks communications with specific contacts or with all contacts. If you want to unblock all communications, pass no arguments to this method.
     *
     * @param jids The contacts.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#unblock">3.4 User Unblocks Contact</a>
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#unblockall">3.5 User Unblocks All Contacts</a>
     */
    public void unblockContact(Jid... jids) throws XmppException {
        List<Item> items = new ArrayList<>();
        for (Jid jid : jids) {
            items.add(new Item(jid));
        }
        xmppSession.query(new IQ(IQ.Type.SET, new Unblock(items)));
    }
}
