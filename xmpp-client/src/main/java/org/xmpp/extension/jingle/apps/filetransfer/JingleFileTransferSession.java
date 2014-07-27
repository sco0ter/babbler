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

import org.xmpp.XmppException;
import org.xmpp.extension.jingle.Jingle;
import org.xmpp.extension.jingle.JingleSession;

import java.io.File;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public final class JingleFileTransferSession {

    private final JingleSession jingleSession;

    public JingleFileTransferSession(JingleSession jingleSession) {
        this.jingleSession = jingleSession;
    }

    public void accept(Jingle.Content... contents) throws XmppException {
        jingleSession.accept(contents);
    }

    public void abort() throws XmppException {
        jingleSession.sendSessionInfo(new JingleFileTransfer.Abort(null));
    }

    public void communicateHash() throws XmppException {
        jingleSession.sendSessionInfo(new JingleFileTransfer.Checksum(null));
    }

    public void offer(File file, String description) throws XmppException {
    }
}
