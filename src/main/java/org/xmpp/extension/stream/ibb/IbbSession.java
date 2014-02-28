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

package org.xmpp.extension.stream.ibb;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author Christian Schudt
 */
public final class IbbSession {

    private final IbbOutputStream outputStream;

    private final IbbInputStream inputStream;

    private final Jid jid;

    private final String sessionId;

    private final int blockSize;

    private final Connection connection;

    private int incomingSequence = 0;

    private int outgoingSequence = 0;

    private volatile boolean closed;

    IbbSession(Connection connection, final Jid jid, int blockSize) {
        this(connection, jid, blockSize, UUID.randomUUID().toString());
    }

    IbbSession(Connection connection, final Jid jid, int blockSize, final String sessionId) {
        this.outputStream = new IbbOutputStream(this, blockSize);
        this.inputStream = new IbbInputStream();
        this.jid = jid;
        this.sessionId = sessionId;
        this.connection = connection;
        this.blockSize = blockSize;
    }

    boolean handleData(Data data) {
        if (incomingSequence++ == data.getSequence()) {
            System.out.println("Offering data");
            inputStream.queue.offer(data);
            return true;
        } else {
            return false;
        }
    }

    public void open() throws XmppException {
        IQ iq = new IQ(IQ.Type.SET, new Open(blockSize, sessionId));
        iq.setTo(jid);
        connection.query(iq);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    synchronized void send(byte[] bytes) throws XmppException {
        IQ response = connection.query(new IQ(jid, IQ.Type.SET, new Data(bytes, sessionId, outgoingSequence)));
        if (response != null && response.getType() == IQ.Type.ERROR) {
            throw new StanzaException(response.getError());
        }
        // The 'seq' value starts at 0 (zero) for each sender and MUST be incremented for each packet sent by that entity. Thus, the second chunk sent has a 'seq' value of 1, the third chunk has a 'seq' value of 2, and so on. The counter loops at maximum, so that after value 65535 (215 - 1) the 'seq' MUST start again at 0.
        if (++outgoingSequence > 65535) {
            outgoingSequence = 0;
        }
    }

    public void close() throws IOException {
        if (!closed) {
            closed = true;
            inputStream.close();
            outputStream.close();
            connection.send(new IQ(jid, IQ.Type.SET, new Close(sessionId)));
        }
    }

    void closedByPeer() throws IOException {
        if (!closed) {
            closed = true;
            inputStream.close();
            outputStream.close();
        }
    }

    public String getSessionId() {
        return sessionId;
    }
}
