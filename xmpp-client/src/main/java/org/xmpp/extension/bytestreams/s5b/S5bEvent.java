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

package org.xmpp.extension.bytestreams.s5b;

import org.xmpp.XmppSession;
import org.xmpp.extension.bytestreams.ByteStreamEvent;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.stanza.client.IQ;

import java.util.List;

/**
 * @author Christian Schudt
 */
final class S5bEvent extends ByteStreamEvent {

    private final XmppSession xmppSession;

    private final IQ iq;

    private final List<StreamHost> streamHosts;

    public S5bEvent(Object source, String sessionId, XmppSession xmppSession, IQ iq, List<StreamHost> streamHosts) {
        super(source, sessionId);
        this.xmppSession = xmppSession;
        this.iq = iq;
        this.streamHosts = streamHosts;
    }

    @Override
    public ByteStreamSession accept() {
        S5bSession s5bSession = xmppSession.getExtensionManager(Socks5ByteStreamManager.class).createS5bSession(iq.getFrom(), iq.getTo(), getSessionId(), streamHosts);
        // 6.3.3 Target Acknowledges Bytestream
        IQ result = iq.createResult();
        result.setExtension(new Socks5ByteStream(s5bSession.getStreamHost()));
        xmppSession.send(iq);
        return s5bSession;
    }

    @Override
    public void reject() {

    }
}
