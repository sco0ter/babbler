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

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * This manager allows to block communications with contacts.
 * <p>
 * Enabling or disabling this manager has no effect, because blocking is done on server side.
 * </p>
 *
 * @author Christian Schudt
 */
public final class BlockingManager extends ExtensionManager {

    private BlockingManager(final Connection connection) {
        super(connection);
        // Listen for "un/block pushes"
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    if (iq.getType() == IQ.Type.SET) {
                        Block block = iq.getExtension(Block.class);
                        if (block != null) {
                            connection.send(iq.createResult());
                        }

                        Unblock unblock = iq.getExtension(Unblock.class);
                        if (unblock != null) {
                            connection.send(iq.createResult());
                        }
                    }
                }
            }
        });
    }

    /**
     * Retrieves the block list.
     *
     * @return The block list.
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#blocklist">3.2 User Retrieves Block List</a>
     */
    public BlockList getBlockList() throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new BlockList()));
        return result.getExtension(BlockList.class);
    }

    /**
     * Blocks communications with contacts.
     *
     * @param jids The contacts.
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#block">3.3 User Blocks Contact</a>
     */
    public void blockContact(Jid... jids) throws TimeoutException, StanzaException {
        if (jids.length == 0) {
            throw new IllegalArgumentException("At least one JID must be set.");
        }
        List<Item> items = new ArrayList<>();
        for (Jid jid : jids) {
            items.add(new Item(jid));
        }
        connection.query(new IQ(IQ.Type.SET, new Block(items)));
    }

    /**
     * Unblocks communications with contacts.
     *
     * @param jids The contacts.
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0191.html#unblock">3.4 User Unblocks Contact</a>
     */
    public void unblock(Jid... jids) throws TimeoutException, StanzaException {
        List<Item> items = new ArrayList<>();
        for (Jid jid : jids) {
            items.add(new Item(jid));
        }
        connection.query(new IQ(IQ.Type.SET, new Unblock(items)));
    }
}
