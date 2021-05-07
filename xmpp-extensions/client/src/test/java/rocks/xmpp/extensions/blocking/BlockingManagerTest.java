/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.blocking.model.Block;
import rocks.xmpp.extensions.blocking.model.BlockList;
import rocks.xmpp.extensions.blocking.model.Unblock;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * Tests for {@link BlockingManager}.
 */
public class BlockingManagerTest {

    @Test
    public void getBlockedContacts() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        Mockito.doReturn(new AsyncResult<>(CompletableFuture.completedFuture(
                new IQ(IQ.Type.RESULT, new BlockList(List.of(Jid.of("romeo@montague.net"),
                        Jid.of("iago@shakespeare.lit")))))))
                .when(xmppSession).query(Mockito.any(IQ.class));

        BlockingManager blockingManager = Mockito.spy(new BlockingManager(xmppSession));
        Set<Jid> blockedItems = blockingManager.getBlockedContacts().get();

        Assert.assertEquals(blockedItems.size(), 2);
    }

    @Test
    public void blockContact() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        Mockito.doReturn(new AsyncResult<>(CompletableFuture.completedFuture(new IQ(IQ.Type.RESULT, null))))
                .when(xmppSession).query(Mockito.any(IQ.class));
        BlockingManager blockingManager = Mockito.spy(new BlockingManager(xmppSession));
        blockingManager.blockContact(Jid.of("romeo@montague.net")).get();

        ArgumentCaptor<IQ> argumentCaptor = ArgumentCaptor.forClass(IQ.class);
        Mockito.verify(xmppSession).query(argumentCaptor.capture());

        IQ iq = argumentCaptor.getValue();
        Assert.assertEquals(iq.getType(), IQ.Type.SET);
        Block block = iq.getExtension(Block.class);
        Assert.assertNotNull(block);
        Assert.assertEquals(block.getItems().get(0), Jid.of("romeo@montague.net"));
    }

    @Test
    public void unblockContact() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        Mockito.doReturn(new AsyncResult<>(CompletableFuture.completedFuture(new IQ(IQ.Type.RESULT, null))))
                .when(xmppSession).query(Mockito.any(IQ.class));
        BlockingManager blockingManager = Mockito.spy(new BlockingManager(xmppSession));
        blockingManager.unblockContact(Jid.of("romeo@montague.net")).get();

        ArgumentCaptor<IQ> argumentCaptor = ArgumentCaptor.forClass(IQ.class);
        Mockito.verify(xmppSession).query(argumentCaptor.capture());

        IQ iq = argumentCaptor.getValue();
        Assert.assertEquals(iq.getType(), IQ.Type.SET);
        Unblock unblock = iq.getExtension(Unblock.class);
        Assert.assertNotNull(unblock);
        Assert.assertEquals(unblock.getItems().get(0), Jid.of("romeo@montague.net"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processRequestBlock() {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        Mockito.doReturn(new AsyncResult<>(CompletableFuture.completedFuture(new IQ(IQ.Type.RESULT, null))))
                .when(xmppSession).query(Mockito.any(IQ.class));
        BlockingManager blockingManager = Mockito.spy(new BlockingManager(xmppSession));

        Consumer<BlockingEvent> blockingListener = Mockito.mock(Consumer.class);
        blockingManager.addBlockingListener(blockingListener);
        blockingManager.processRequest(IQ.set(new Block(Collections.singleton(Jid.of("romeo@montague.net")))));

        ArgumentCaptor<BlockingEvent> blockingEventArgumentCaptor = ArgumentCaptor.forClass(BlockingEvent.class);
        Mockito.verify(blockingListener).accept(blockingEventArgumentCaptor.capture());

        BlockingEvent blockingEvent = blockingEventArgumentCaptor.getValue();
        Assert.assertEquals(blockingEvent.getBlockedContacts(), Collections.singleton(Jid.of("romeo@montague.net")));
        Assert.assertTrue(blockingEvent.getUnblockedContacts().isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processRequestUnblock() {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        Mockito.doReturn(new AsyncResult<>(CompletableFuture.completedFuture(new IQ(IQ.Type.RESULT, null))))
                .when(xmppSession).query(Mockito.any(IQ.class));
        BlockingManager blockingManager = Mockito.spy(new BlockingManager(xmppSession));
        blockingManager.processRequest(IQ.set(new Block(List.of(Jid.of("romeo@montague.net"), Jid.of("iago@shakespeare.lit")))));

        Consumer<BlockingEvent> blockingListener = Mockito.mock(Consumer.class);
        blockingManager.addBlockingListener(blockingListener);
        blockingManager.processRequest(IQ.set(new Unblock(Collections.singleton(Jid.of("romeo@montague.net")))));

        ArgumentCaptor<BlockingEvent> blockingEventArgumentCaptor = ArgumentCaptor.forClass(BlockingEvent.class);
        Mockito.verify(blockingListener).accept(blockingEventArgumentCaptor.capture());

        BlockingEvent blockingEvent = blockingEventArgumentCaptor.getValue();
        Assert.assertEquals(blockingEvent.getUnblockedContacts(), Collections.singleton(Jid.of("romeo@montague.net")));
        Assert.assertTrue(blockingEvent.getBlockedContacts().isEmpty());

        // Unblock all
        Mockito.clearInvocations(blockingListener);
        blockingManager.processRequest(IQ.set(new Unblock()));
        ArgumentCaptor<BlockingEvent> blockingEventArgumentCaptor2 = ArgumentCaptor.forClass(BlockingEvent.class);
        Mockito.verify(blockingListener).accept(blockingEventArgumentCaptor2.capture());

        blockingEvent = blockingEventArgumentCaptor2.getValue();
        Assert.assertEquals(blockingEvent.getUnblockedContacts(), Collections.singleton(Jid.of("iago@shakespeare.lit")));
        Assert.assertTrue(blockingEvent.getBlockedContacts().isEmpty());

        // Test listener removal
        Mockito.clearInvocations(blockingListener);
        blockingManager.removeBlockingListener(blockingListener);
        blockingManager.processRequest(IQ.set(new Unblock()));
        Mockito.verifyNoInteractions(blockingListener);
    }
}
