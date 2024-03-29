/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.blocking.model.Block;
import rocks.xmpp.extensions.blocking.model.BlockList;
import rocks.xmpp.extensions.blocking.model.Blockable;
import rocks.xmpp.extensions.blocking.model.Unblock;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * This manager allows to block communications with contacts.
 *
 * <h3>Usage</h3>
 *
 * <p>Listen for pushes from the server which informs you about changes in the block list:</p>
 *
 * <pre>{@code
 * blockingManager.addBlockingListener(e -> {
 *     // e.getBlockedContacts();
 *     // modify the block list accordingly.
 * });
 * }</pre>
 *
 * <p>After authenticating with a server and before any blocking operation you should retrieve the block list:</p>
 *
 * <pre>{@code
 * blockingManager.getBlockedContacts();
 * }</pre>
 *
 * <p>This will also inform the server that you are interested in updates to the list and will trigger pushes to the
 * client.</p>
 *
 * <p>This class is thread-safe.</p>
 */
public final class BlockingManager extends AbstractIQHandler implements ExtensionProtocol, DiscoverableInfo {

    private final Set<Jid> blockedContacts = new HashSet<>();

    private final Set<Consumer<BlockingEvent>> blockingListeners = new CopyOnWriteArraySet<>();

    private final XmppSession xmppSession;

    BlockingManager(final XmppSession xmppSession) {
        super(Blockable.class, IQ.Type.SET);
        this.xmppSession = xmppSession;
    }

    @Override
    protected IQ processRequest(IQ iq) {
        if (iq.getFrom() == null || iq.getFrom().equals(xmppSession.getConnectedResource().asBareJid())) {
            Blockable block = iq.getExtension(Blockable.class);
            if (block instanceof Block) {
                List<Jid> pushedContacts = new ArrayList<>();
                synchronized (blockedContacts) {
                    for (Jid item : block.getItems()) {
                        blockedContacts.add(item);
                        pushedContacts.add(item);
                    }
                }
                XmppUtils.notifyEventListeners(blockingListeners,
                        new BlockingEvent(BlockingManager.this, pushedContacts, Collections.emptyList()));
                return iq.createResult();
            } else if (block instanceof Unblock) {
                List<Jid> pushedContacts = new ArrayList<>();
                synchronized (blockedContacts) {
                    if (block.getItems().isEmpty()) {
                        // Empty means, the user has unblocked communications with all contacts.
                        pushedContacts.addAll(blockedContacts);
                        blockedContacts.clear();
                    } else {
                        for (Jid item : block.getItems()) {
                            blockedContacts.remove(item);
                            pushedContacts.add(item);
                        }
                    }
                }
                XmppUtils.notifyEventListeners(blockingListeners,
                        new BlockingEvent(BlockingManager.this, Collections.emptyList(), pushedContacts));
                return iq.createResult();
            }
        }
        return iq.createError(Condition.NOT_ACCEPTABLE);
    }

    /**
     * Adds a blocking listener, which allows to listen for block and unblock pushes.
     *
     * @param blockingListener The listener.
     * @see #removeBlockingListener(Consumer)
     */
    public final void addBlockingListener(Consumer<BlockingEvent> blockingListener) {
        blockingListeners.add(blockingListener);
    }

    /**
     * Removes a previously added blocking listener.
     *
     * @param blockingListener The listener.
     * @see #addBlockingListener(Consumer)
     */
    public final void removeBlockingListener(Consumer<BlockingEvent> blockingListener) {
        blockingListeners.remove(blockingListener);
    }

    /**
     * Retrieves the blocked contacts.
     *
     * @return The async result with the block list.
     * @see <a href="https://xmpp.org/extensions/xep-0191.html#blocklist">3.2 User Retrieves Block List</a>
     */
    public final AsyncResult<Set<Jid>> getBlockedContacts() {
        return xmppSession.query(IQ.get(new BlockList())).thenApply(result -> {
            BlockList blockList = result.getExtension(BlockList.class);
            if (blockList != null) {
                synchronized (blockedContacts) {
                    blockedContacts.addAll(blockList.getItems());
                }
            }
            return blockedContacts;
        });
    }

    /**
     * Blocks communications with contacts.
     *
     * @param jids The contacts.
     * @return The async result.
     * @see <a href="https://xmpp.org/extensions/xep-0191.html#block">3.3 User Blocks Contact</a>
     */
    public final AsyncResult<IQ> blockContact(Jid... jids) {
        return xmppSession.query(IQ.set(new Block(Arrays.asList(jids))));
    }

    /**
     * Unblocks communications with specific contacts or with all contacts. If you want to unblock all communications,
     * pass no arguments to this method.
     *
     * @param jids The contacts.
     * @return The async result.
     * @see <a href="https://xmpp.org/extensions/xep-0191.html#unblock">3.4 User Unblocks Contact</a>
     * @see <a href="https://xmpp.org/extensions/xep-0191.html#unblockall">3.5 User Unblocks All Contacts</a>
     */
    public final AsyncResult<IQ> unblockContact(Jid... jids) {
        return xmppSession.query(IQ.set(new Unblock(Arrays.asList(jids))));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@value BlockList#NAMESPACE}
     */
    @Override
    public final String getNamespace() {
        return BlockList.NAMESPACE;
    }

    @Override
    public final boolean isEnabled() {
        return !blockingListeners.isEmpty();
    }

    @Override
    public final Set<String> getFeatures() {
        return Collections.emptySet();
    }
}
