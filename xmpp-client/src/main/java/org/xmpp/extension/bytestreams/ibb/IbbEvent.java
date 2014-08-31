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

package org.xmpp.extension.bytestreams.ibb;

import org.xmpp.XmppSession;
import org.xmpp.extension.bytestreams.ByteStreamEvent;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.NotAcceptable;

/**
 * @author Christian Schudt
 */
final class IbbEvent extends ByteStreamEvent {

    private final XmppSession xmppSession;

    private final IQ iq;

    private final int blockSize;

    public IbbEvent(Object source, String sessionId, XmppSession xmppSession, IQ iq, int blockSize) {
        super(source, sessionId);
        this.xmppSession = xmppSession;
        this.iq = iq;
        this.blockSize = blockSize;
    }

    @Override
    public ByteStreamSession accept() {
        xmppSession.send(iq.createResult());
        return xmppSession.getExtensionManager(InBandByteStreamManager.class).createSession(iq.getFrom(), getSessionId(), blockSize);
    }

    @Override
    public void reject() {
        // If the responder supports IBB but does not wish to proceed with the session, it returns a <not-acceptable/> error.
        xmppSession.send(iq.createError(new StanzaError(StanzaError.Type.CANCEL, new NotAcceptable())));
    }
}
