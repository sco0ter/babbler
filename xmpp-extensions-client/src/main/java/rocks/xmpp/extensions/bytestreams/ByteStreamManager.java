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

package rocks.xmpp.extensions.bytestreams;

import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.IQExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.AbstractIQ;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract class to manage both <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a> or <a href="http://xmpp.org/extensions/xep-0065.html">XEP-0065: SOCKS5 Bytestreams</a>.
 *
 * @author Christian Schudt
 */
public abstract class ByteStreamManager extends IQExtensionManager implements SessionStatusListener {

    private static final Logger logger = Logger.getLogger(ByteStreamManager.class.getName());

    private final Set<ByteStreamListener> byteStreamListeners = new CopyOnWriteArraySet<>();

    protected ByteStreamManager(XmppSession xmppSession, String... features) {
        super(xmppSession, AbstractIQ.Type.SET, features);
        xmppSession.addSessionStatusListener(this);
    }

    /**
     * Adds a byte stream listener, which allows to listen for incoming byte stream requests.
     *
     * @param byteStreamListener The listener.
     * @see #removeByteStreamListener(ByteStreamListener)
     */
    public final void addByteStreamListener(ByteStreamListener byteStreamListener) {
        byteStreamListeners.add(byteStreamListener);
    }

    /**
     * Removes a previously added byte stream listener.
     *
     * @param byteStreamListener The listener.
     * @see #addByteStreamListener(ByteStreamListener)
     */
    public final void removeByteStreamListener(ByteStreamListener byteStreamListener) {
        byteStreamListeners.remove(byteStreamListener);
    }

    /**
     * Notifies the byte stream listener.
     *
     * @param byteStreamEvent The byte stream event.
     */
    protected final void notifyByteStreamEvent(ByteStreamEvent byteStreamEvent) {
        for (ByteStreamListener byteStreamListener : byteStreamListeners) {
            try {
                byteStreamListener.byteStreamRequested(byteStreamEvent);
            } catch (Exception exc) {
                logger.log(Level.WARNING, exc.getMessage(), exc);
            }
        }
    }

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            byteStreamListeners.clear();
        }
    }
}
