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

import org.xmpp.XmppSession;
import org.xmpp.XmppSession;
import org.xmpp.stanza.client.IQ;

import java.util.EventObject;

/**
 * @author Christian Schudt
 */
public final class IbbEvent extends EventObject {

    private final XmppSession xmppSession;

    private final IQ iq;

    private final Open open;

    public IbbEvent(Object source, XmppSession xmppSession, IQ iq, Open open) {
        super(source);
        this.xmppSession = xmppSession;
        this.iq = iq;
        this.open = open;
    }

    public IbbSession accept() {
        xmppSession.send(iq.createResult());
        return xmppSession.getExtensionManager(InBandBytestreamManager.class).createInBandByteStream(iq.getFrom(), open.getBlockSize(), open.getSessionId());
    }

    public void reject() {

    }
}
