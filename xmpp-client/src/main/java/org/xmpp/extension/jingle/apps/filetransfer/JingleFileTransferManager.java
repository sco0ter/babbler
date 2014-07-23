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

package org.xmpp.extension.jingle.apps.filetransfer;

import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.filetransfer.FileTransferRejectedException;
import org.xmpp.extension.jingle.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Christian Schudt
 */
public final class JingleFileTransferManager extends ExtensionManager {

    private final JingleManager jingleManager;

    private JingleFileTransferManager(XmppSession xmppSession) {
        super(xmppSession, "urn:xmpp:jingle:apps:file-transfer:3");
        jingleManager = xmppSession.getExtensionManager(JingleManager.class);
    }

    public JingleFileTransferSession initFileTransferSession(final Jid responder, long timeout) throws XmppException {
        JingleFileTransfer jingleFileTransfer = new JingleFileTransfer();

        Jingle.Content content = new Jingle.Content("a-file-offer", Jingle.Content.Creator.INITIATOR, jingleFileTransfer, null);
        final JingleSession jingleSession = jingleManager.createSession(responder, content);

        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final Jingle[] response = new Jingle[1];

        jingleSession.addJingleListener(new JingleListener() {
            @Override
            public void jingleReceived(JingleEvent e) {
                if (e.getJingle().getAction() == Jingle.Action.SESSION_ACCEPT || e.getJingle().getAction() == Jingle.Action.SESSION_TERMINATE) {
                    lock.lock();
                    try {
                        response[0] = e.getJingle();
                        condition.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        });
        jingleSession.initiate();

        // Wait until the session is either accepted or declined (terminated).
        lock.lock();
        try {
            if (!condition.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("The receiver did not respond in time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }

        if (response[0].getAction() == Jingle.Action.SESSION_TERMINATE) {
            throw new FileTransferRejectedException();
        }
        if (response[0].getAction() == Jingle.Action.SESSION_ACCEPT) {
            // TODO negotiate transport
        }
        return new JingleFileTransferSession(jingleSession);
    }
}
