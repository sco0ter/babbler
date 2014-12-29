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

package rocks.xmpp.extensions.bytestreams.ibb;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.bytestreams.ByteStreamSession;
import rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Christian Schudt
 */
final class IbbSession extends ByteStreamSession {

    private final IbbOutputStream outputStream;

    private final IbbInputStream inputStream;

    private final Jid jid;

    private final int blockSize;

    private final XmppSession xmppSession;

    private final InBandByteStreamManager inBandByteStreamManager;

    private int incomingSequence = 0;

    private int outgoingSequence = 0;

    private volatile boolean closed;

    IbbSession(String sessionId, XmppSession xmppSession, Jid jid, int blockSize, InBandByteStreamManager manager) {
        super(sessionId);
        this.outputStream = new IbbOutputStream(this, blockSize);
        this.inputStream = new IbbInputStream(this);
        this.jid = jid;
        this.xmppSession = xmppSession;
        this.blockSize = blockSize;
        this.inBandByteStreamManager = manager;
    }

    boolean dataReceived(InBandByteStream.Data data) {
        if (incomingSequence++ == data.getSequence()) {
            inputStream.queue.offer(data);
            return true;
        } else {
            return false;
        }
    }

    void open() throws XmppException {
        xmppSession.query(new IQ(jid, IQ.Type.SET, new InBandByteStream.Open(blockSize, getSessionId())));
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (closed) {
            throw new IOException("IBB session is closed.");
        }
        return outputStream;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (closed) {
            throw new IOException("IBB session is closed.");
        }
        return inputStream;
    }

    @Override
    public int getReadTimeout() {
        return inputStream.readTimeout;
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        inputStream.readTimeout = readTimeout;
    }

    synchronized void send(byte[] bytes) throws XmppException {
        xmppSession.query(new IQ(jid, IQ.Type.SET, new InBandByteStream.Data(bytes, getSessionId(), outgoingSequence)));
        // The 'seq' value starts at 0 (zero) for each sender and MUST be incremented for each packet sent by that entity. Thus, the second chunk sent has a 'seq' value of 1, the third chunk has a 'seq' value of 2, and so on. The counter loops at maximum, so that after value 65535 (215 - 1) the 'seq' MUST start again at 0.
        if (++outgoingSequence > 65535) {
            outgoingSequence = 0;
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            try {
                inputStream.close();
                outputStream.close();
                xmppSession.query(new IQ(jid, IQ.Type.SET, new InBandByteStream.Close(getSessionId())));
            } catch (XmppException e) {
                throw new IOException(e);
            } finally {
                // the party that sent the original <close/> element SHOULD wait to receive the IQ response from the receiving party before considering the bytestream to be closed.
                // Remove this session from the map.
                inBandByteStreamManager.ibbSessionMap.remove(getSessionId());
            }
        }
    }

    void closedByPeer() throws IOException {
        if (!closed) {
            closed = true;
            inputStream.close();
            outputStream.close();
        }
    }

    @Override
    public String toString() {
        return "In-Band Bytestream Session: " + getSessionId();
    }
}
