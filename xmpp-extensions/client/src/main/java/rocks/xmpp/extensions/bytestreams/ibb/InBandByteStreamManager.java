/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.InboundMessageHandler;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.bytestreams.ByteStreamManager;
import rocks.xmpp.extensions.bytestreams.ByteStreamSession;
import rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * A manager for <a href="https://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a>. IBB streams use
 * the same transport as XMPP, i.e. the same TCP or BOSH connection.
 *
 * <p>To initiate an IBB session with another entity, use {@link #initiateSession(Jid, String, int)}.</p>
 *
 * <p>This class is thread-safe.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a>
 */
public final class InBandByteStreamManager extends ByteStreamManager implements IQHandler, InboundMessageHandler,
        ExtensionProtocol, DiscoverableInfo {

    private static final Set<String> FEATURES = Collections.singleton(InBandByteStream.NAMESPACE);

    private static final System.Logger logger = System.getLogger(InBandByteStreamManager.class.getName());

    final Map<String, IbbSession> ibbSessionMap = new ConcurrentHashMap<>();

    private final IQHandler iqHandler;

    // Guarded by "this"
    private InBandByteStream.Open.StanzaType stanzaType = InBandByteStream.Open.StanzaType.IQ;

    private InBandByteStreamManager(final XmppSession xmppSession) {
        super(xmppSession);
        iqHandler = new AbstractIQHandler(InBandByteStream.class, IQ.Type.SET) {
            @Override
            protected IQ processRequest(IQ iq) {
                InBandByteStream ibb = iq.getExtension(InBandByteStream.class);
                if (ibb instanceof InBandByteStream.Open) {
                    InBandByteStream.Open open = (InBandByteStream.Open) ibb;
                    if (open.getBlockSize() > 65535) {
                        return iq.createError(new StanzaError(StanzaError.Type.MODIFY,
                                rocks.xmpp.core.stanza.model.errors.Condition.RESOURCE_CONSTRAINT));
                    } else {
                        // Somebody wants to create a IBB session with me.
                        // Notify the listeners.
                        XmppUtils.notifyEventListeners(byteStreamListeners,
                                new IbbEvent(InBandByteStreamManager.this, open.getSessionId(), xmppSession, iq,
                                        open.getBlockSize(), open.getStanzaType()));
                        return null;
                    }
                } else if (ibb instanceof InBandByteStream.Data) {
                    InBandByteStream.Data data = (InBandByteStream.Data) ibb;
                    IbbSession ibbSession = ibbSessionMap.get(data.getSessionId());
                    // Data has been received for a session, so notify the IBB session about it.
                    if (ibbSession != null) {
                        if (ibbSession.dataReceived(data)) {
                            return iq.createResult();
                        } else {
                            // 2. Because the sequence number has already been used, the recipient returns an
                            // <unexpected-request/> error with a type of 'cancel'.
                            return iq.createError(new StanzaError(StanzaError.Type.CANCEL,
                                    rocks.xmpp.core.stanza.model.errors.Condition.UNEXPECTED_REQUEST));
                        }
                    } else {
                        return iq.createError(Condition.ITEM_NOT_FOUND);
                    }
                } else if (ibb instanceof InBandByteStream.Close) {
                    // Must be a close element.
                    InBandByteStream.Close close = (InBandByteStream.Close) ibb;
                    IbbSession ibbSession = ibbSessionMap.get(close.getSessionId());
                    if (ibbSession != null) {
                        try {
                            ibbSessionMap.remove(close.getSessionId());
                            ibbSession.closedByPeer();
                            return iq.createResult();
                        } catch (IOException e1) {
                            logger.log(System.Logger.Level.WARNING, e1.getMessage(), e1);
                            return iq.createResult();
                        }
                    } else {
                        return iq.createError(Condition.ITEM_NOT_FOUND);
                    }
                }
                return iq.createError(Condition.SERVICE_UNAVAILABLE);
            }
        };
    }

    /**
     * Creates an in-band byte stream session.
     *
     * @param receiver   The receiver.
     * @param sessionId  The session id.
     * @param blockSize  The block size.
     * @param stanzaType The stanza type.
     * @return The in-band byte stream session.
     */
    IbbSession createSession(Jid receiver, final String sessionId, int blockSize,
                             InBandByteStream.Open.StanzaType stanzaType) {
        IbbSession ibbSession = new IbbSession(sessionId, xmppSession, receiver, blockSize,
                xmppSession.getConfiguration().getDefaultResponseTimeout(), this, stanzaType);
        ibbSessionMap.put(ibbSession.getSessionId(), ibbSession);
        return ibbSession;
    }

    /**
     * Initiates an in-band byte stream session.
     *
     * @param receiver  The receiver.
     * @param sessionId The session id.
     * @param blockSize The block size.
     * @return The async result with the in-band byte stream session.
     */
    public final AsyncResult<ByteStreamSession> initiateSession(final Jid receiver, final String sessionId,
                                                                int blockSize) {
        if (blockSize > 65535) {
            throw new IllegalArgumentException("blockSize must not be greater than 65535.");
        }
        IbbSession ibbSession = createSession(receiver, sessionId, blockSize, getStanzaType());
        return ibbSession.open().thenApply(result -> ibbSession);
    }

    @Override
    public final AsyncResult<ByteStreamSession> initiateSession(final Jid receiver, final String sessionId) {
        return initiateSession(receiver, sessionId, 4096);
    }

    /**
     * Gets the stanza type.
     *
     * @return The stanza type.
     */
    public final synchronized InBandByteStream.Open.StanzaType getStanzaType() {
        return stanzaType;
    }

    /**
     * Sets the stanza type, which is used to send data chunks. It is recommended to leave this on the default ({@link
     * rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream.Open.StanzaType#IQ}).
     *
     * @param stanzaType The stanza type.
     */
    public final synchronized void setStanzaType(InBandByteStream.Open.StanzaType stanzaType) {
        this.stanzaType = stanzaType;
    }

    @Override
    protected void dispose() {
        super.dispose();
        ibbSessionMap.clear();
    }

    @Override
    public Class<?> getPayloadClass() {
        return iqHandler.getPayloadClass();
    }

    @Override
    public IQ handleRequest(IQ iq) {
        return iqHandler.handleRequest(iq);
    }

    @Override
    public void handleInboundMessage(MessageEvent e) {
        // 4. Use of Message Stanzas
        // an application MAY use message stanzas instead.
        InBandByteStream.Data data = e.getMessage().getExtension(InBandByteStream.Data.class);
        if (data != null) {
            IbbSession ibbSession = ibbSessionMap.get(data.getSessionId());
            if (ibbSession != null) {
                if (!ibbSession.dataReceived(data)) {
                    // 2. Because the sequence number has already been used, the recipient returns an
                    // <unexpected-request/> error with a type of 'cancel'.
                    xmppSession.send(e.getMessage()
                            .createError(new StanzaError(StanzaError.Type.CANCEL, Condition.UNEXPECTED_REQUEST)));
                }
            } else {
                xmppSession.send(e.getMessage().createError(Condition.ITEM_NOT_FOUND));
            }
        }
    }

    @Override
    public final String getNamespace() {
        return InBandByteStream.NAMESPACE;
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }
}
