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

package rocks.xmpp.extensions.blocking;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.IQExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.blocking.model.Block;
import rocks.xmpp.extensions.blocking.model.BlockList;
import rocks.xmpp.extensions.blocking.model.Unblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public final class BlockingManager extends IQExtensionManager implements SessionStatusListener {

    private static final Logger logger = Logger.getLogger(BlockingManager.class.getName());

    private final Set<Jid> blockedContacts = new HashSet<>();

    private final Set<BlockingListener> blockingListeners = new CopyOnWriteArraySet<>();

    private BlockingManager(final XmppSession xmppSession) {
        super(xmppSession, AbstractIQ.Type.SET);

        xmppSession.addSessionStatusListener(this);
        // Listen for "un/block pushes"
        xmppSession.addIQHandler(Block.class, this);
        xmppSession.addIQHandler(Unblock.class, this);
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#blocklist">3.2 User Retrieves Block List</a>
     */
    public Collection<Jid> getBlockedContacts() throws XmppException {
        synchronized (blockedContacts) {
            IQ result = xmppSession.query(new IQ(IQ.Type.GET, new BlockList()));
            BlockList blockList = result.getExtension(BlockList.class);
            if (blockList != null) {
                for (Jid item : blockList.getItems()) {
                    blockedContacts.add(item);
                }
            }
            return blockedContacts;
        }
    }

    /**
     * Blocks communications with contacts.
     *
     * @param jids The contacts.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#block">3.3 User Blocks Contact</a>
     */
    public void blockContact(Jid... jids) throws XmppException {
        List<Jid> items = new ArrayList<>();
        Collections.addAll(items, jids);
        xmppSession.query(new IQ(IQ.Type.SET, new Block(items)));
    }

    /**
     * Unblocks communications with specific contacts or with all contacts. If you want to unblock all communications, pass no arguments to this method.
     *
     * @param jids The contacts.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#unblock">3.4 User Unblocks Contact</a>
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#unblockall">3.5 User Unblocks All Contacts</a>
     */
    public void unblockContact(Jid... jids) throws XmppException {
        List<Jid> items = new ArrayList<>();
        Collections.addAll(items, jids);
        xmppSession.query(new IQ(IQ.Type.SET, new Unblock(items)));
    }

    @Override
    protected IQ processRequest(IQ iq) {
        if (iq.getFrom() == null || iq.getFrom().equals(xmppSession.getConnectedResource().asBareJid())) {
            Block block = iq.getExtension(Block.class);
            if (block != null) {
                List<Jid> pushedContacts = new ArrayList<>();
                synchronized (blockedContacts) {
                    for (Jid item : block.getItems()) {
                        blockedContacts.add(item);
                        pushedContacts.add(item);
                    }
                }
                notifyListeners(pushedContacts, Collections.<Jid>emptyList());
                return iq.createResult();
            } else {
                Unblock unblock = iq.getExtension(Unblock.class);
                if (unblock != null) {
                    List<Jid> pushedContacts = new ArrayList<>();
                    synchronized (blockedContacts) {
                        if (unblock.getItems().isEmpty()) {
                            // Empty means, the user has unblocked communications with all contacts.
                            pushedContacts.addAll(blockedContacts);
                            blockedContacts.clear();
                        } else {
                            for (Jid item : unblock.getItems()) {
                                blockedContacts.remove(item);
                                pushedContacts.add(item);
                            }
                        }
                    }
                    notifyListeners(Collections.<Jid>emptyList(), pushedContacts);
                    return iq.createResult();
                }
            }
        }
        return iq.createError(new StanzaError(Condition.NOT_ACCEPTABLE));
    }

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            blockingListeners.clear();
            blockedContacts.clear();
        }
    }
}
