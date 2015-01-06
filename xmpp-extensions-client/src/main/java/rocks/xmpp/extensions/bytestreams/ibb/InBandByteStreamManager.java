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
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQListener;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.bytestreams.ByteStreamManager;
import rocks.xmpp.extensions.bytestreams.ByteStreamSession;
import rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A manager for <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a>. IBB streams use the same transport as XMPP, i.e. the same TCP or BOSH connection.
 * <p>
 * To initiate an IBB session with another entity, use {@link #initiateSession(rocks.xmpp.core.Jid, String, int)}.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a>
 */
public final class InBandByteStreamManager extends ByteStreamManager implements IQListener, MessageListener {

    private static final Logger logger = Logger.getLogger(InBandByteStreamManager.class.getName());

    final Map<String, IbbSession> ibbSessionMap = new ConcurrentHashMap<>();

    private InBandByteStreamManager(final XmppSession xmppSession) {
        super(xmppSession, InBandByteStream.NAMESPACE);

        xmppSession.addIQListener(this);

        // 4. Use of Message Stanzas
        // an application MAY use message stanzas instead.
        xmppSession.addMessageListener(this);
        setEnabled(true);
    }

    private IbbSession getIbbSession(IQ iq, String sessionId) {
        // Check, if the session id is known.
        IbbSession ibbSession = ibbSessionMap.get(sessionId);
        if (ibbSession == null) {
            // 1. Because the session ID is unknown, the recipient returns an <item-not-found/> error with a type of 'cancel'.
            xmppSession.send(iq.createError(new StanzaError(Condition.ITEM_NOT_FOUND)));
        }
        return ibbSession;
    }

    /**
     * Creates an in-band byte stream session.
     *
     * @param receiver  The receiver.
     * @param sessionId The session id.
     * @param blockSize The block size.
     * @return The in-band byte stream session.
     */
    IbbSession createSession(Jid receiver, final String sessionId, int blockSize) {
        IbbSession ibbSession = new IbbSession(sessionId, xmppSession, receiver, blockSize, this);
        ibbSessionMap.put(ibbSession.getSessionId(), ibbSession);
        return ibbSession;
    }

    /**
     * Initiates an in-band byte stream session.
     *
     * @param receiver  The receiver.
     * @param sessionId The session id.
     * @param blockSize The block size.
     * @return The in-band byte stream session.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public ByteStreamSession initiateSession(Jid receiver, final String sessionId, int blockSize) throws XmppException {
        if (blockSize > 65535) {
            throw new IllegalArgumentException("blockSize must not be greater than 65535.");
        }
        IbbSession ibbSession = createSession(receiver, sessionId, blockSize);
        ibbSession.open();
        return ibbSession;
    }

    @Override
    public void handleIQ(IQEvent e) {
        IQ iq = e.getIQ();
        if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {
            // Check, if the IQ carries some IBB related payload.
            InBandByteStream.Data data = iq.getExtension(InBandByteStream.Data.class);
            if (data != null) {
                IbbSession ibbSession = getIbbSession(iq, data.getSessionId());
                // Data has been received for a session, so notify the IBB session about it.
                if (ibbSession != null) {
                    if (ibbSession.dataReceived(data)) {
                        xmppSession.send(iq.createResult());
                    } else {
                        // 2. Because the sequence number has already been used, the recipient returns an <unexpected-request/> error with a type of 'cancel'.
                        xmppSession.send(iq.createError(new StanzaError(StanzaError.Type.CANCEL, Condition.UNEXPECTED_REQUEST)));
                    }
                }
                e.consume();
            } else {
                InBandByteStream.Open open = iq.getExtension(InBandByteStream.Open.class);
                if (open != null) {
                    if (open.getBlockSize() > 65535) {
                        xmppSession.send(iq.createError(new StanzaError(StanzaError.Type.MODIFY, Condition.RESOURCE_CONSTRAINT)));
                    } else {
                        // Somebody wants to create a IBB session with me.
                        // Notify the listeners.
                        notifyByteStreamEvent(new IbbEvent(InBandByteStreamManager.this, open.getSessionId(), xmppSession, iq, open.getBlockSize()));
                    }
                    e.consume();
                } else {
                    InBandByteStream.Close close = iq.getExtension(InBandByteStream.Close.class);
                    // The session got closed.
                    if (close != null) {
                        IbbSession ibbSession = getIbbSession(iq, close.getSessionId());
                        if (ibbSession != null) {
                            try {
                                ibbSessionMap.remove(close.getSessionId());
                                ibbSession.closedByPeer();
                            } catch (IOException e1) {
                                logger.log(Level.WARNING, e1.getMessage(), e1);
                            } finally {
                                xmppSession.send(iq.createResult());
                            }
                        }
                        e.consume();
                    }
                }
            }
        }
    }

    @Override
    public void handleMessage(MessageEvent e) {
        if (e.isIncoming() && isEnabled()) {
            InBandByteStream.Data data = e.getMessage().getExtension(InBandByteStream.Data.class);
            if (data != null) {
                IbbSession ibbSession = ibbSessionMap.get(data.getSessionId());
                if (ibbSession != null) {
                    if (!ibbSession.dataReceived(data)) {
                        // 2. Because the sequence number has already been used, the recipient returns an <unexpected-request/> error with a type of 'cancel'.
                        xmppSession.send(e.getMessage().createError(new StanzaError(StanzaError.Type.CANCEL, Condition.UNEXPECTED_REQUEST)));
                    }
                } else {
                    xmppSession.send(e.getMessage().createError(new StanzaError(Condition.ITEM_NOT_FOUND)));
                }
            }
        }
    }

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        super.sessionStatusChanged(e);
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            ibbSessionMap.clear();
        }
    }
}
