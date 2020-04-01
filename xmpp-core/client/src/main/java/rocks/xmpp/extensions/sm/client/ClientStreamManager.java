/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.extensions.sm.client;

import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.extensions.sm.AbstractStreamManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.util.concurrent.CompletableFuture;

/**
 * Manages the stream as described in <a href="https://xmpp.org/extensions/xep-0198.html">XEP-0198: Stream Management</a>.
 * <p>
 * This class enables stream management during stream negotiation, if the stream management feature has been enabled before login:
 * ```java
 * xmppSession.enableFeature(StreamManagement.NAMESPACE);
 * ```
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0198.html">XEP-0198: Stream Management</a>
 */
public final class ClientStreamManager extends AbstractStreamManager {


    private final XmppSession xmppSession;

    /**
     * Guarded by "this".
     */
    private StreamManagement.Enabled enabled;

    /**
     * Guarded by "this".
     */
    private CompletableFuture<Boolean> resumeFuture;

    private ClientStreamManager(XmppSession xmppSession) {
        super(xmppSession);
        this.xmppSession = xmppSession;
        xmppSession.addSessionStatusListener(sessionStatusEvent -> {
                    if (sessionStatusEvent.getStatus() == XmppSession.Status.CLOSING) {
                        // When the client closes the session, acknowledge the receipt stanza count to the server,
                        // so that the server won't resend them (store them offline).
                        if (isActive()) {
                            StreamManagement.Answer answer;
                            synchronized (this) {
                                answer = new StreamManagement.Answer(inboundCount);
                            }
                            xmppSession.send(answer);
                        }
                    }
                }
        );
    }

    @Override
    public final StreamNegotiationResult processNegotiation(Object element) throws StreamNegotiationException {
        if (!isEnabled()) {
            return StreamNegotiationResult.IGNORE;
        }
        StreamNegotiationResult result = super.processNegotiation(element);
        if (result != StreamNegotiationResult.IGNORE) {
            return result;
        }
        try {
            if (element instanceof StreamManagement) {
                // Client sets outbound count to zero.
                synchronized (this) {
                    acknowledgedStanzaCount = 0;
                    enabledByClient.set(true);
                }
                unacknowledgedStanzas.clear();
                xmppSession.send(new StreamManagement.Enable(true));
                return StreamNegotiationResult.INCOMPLETE;
            } else if (element instanceof StreamManagement.Enabled) {
                // In addition, client sets inbound count to zero.
                synchronized (this) {
                    inboundCount = 0;
                    enabled = (StreamManagement.Enabled) element;
                }
                return StreamNegotiationResult.SUCCESS;
            } else if (element instanceof StreamManagement.Failed) {
                StreamManagement.Failed failed = (StreamManagement.Failed) element;
                if (failed.getLastHandledStanza() != null) {
                    markAcknowledged(failed.getLastHandledStanza());
                }
                resumed(false);
                // Stream management errors SHOULD be considered recoverable.
                // Therefore don't throw an exception.
                // Most likely Stream Resumption failed, because the server session timed out.
                // Return Status.INCOMPLETE here, so that SM negotiation can be renegotiated normally.
                return StreamNegotiationResult.INCOMPLETE;
            } else if (element instanceof StreamManagement.Resumed) {
                StreamManagement.Resumed resumed = (StreamManagement.Resumed) element;
                markAcknowledged(resumed.getLastHandledStanza());
                resumed(true);
                return StreamNegotiationResult.SUCCESS;
            }
        } catch (StreamErrorException e) {
            xmppSession.send(e.getError());
            try {
                xmppSession.close();
            } catch (XmppException e1) {
                xmppSession.notifyException(e1);
            }
            return StreamNegotiationResult.IGNORE;
        }
        return StreamNegotiationResult.IGNORE;
    }

    private void resumed(boolean resumed) {
        CompletableFuture<Boolean> completableFuture;
        synchronized (this) {
            completableFuture = resumeFuture;
        }
        if (completableFuture != null) {
            completableFuture.complete(resumed);
        }
    }

    @Override
    protected final void onAcknowledged(Stanza stanza) {
        xmppSession.markAcknowledged(stanza);
    }


    /**
     * Returns true, as soon as the server has enabled stream management, i.e. if both client and server are using stream management.
     *
     * @return True, as soon as the server has enabled stream management.
     */
    public final synchronized boolean isActive() {
        return enabled != null;
    }

    /**
     * If the server will allow the stream to be resumed.
     *
     * @return If the server will allow the stream to be resumed.
     */
    public final synchronized boolean isResumable() {
        // If the server will allow the stream to be resumed,
        // it MUST include a 'resume' attribute set to "true" or "1" on the <enabled/> element
        return enabled != null && enabled.isResume();
    }

    /**
     * Gets the "SM-ID".
     * <blockquote>
     * <p>The 'id' attribute defines a unique identifier for purposes of stream management (an "SM-ID").
     * The SM-ID MUST be generated by the server. The client MUST consider the SM-ID to be opaque and therefore MUST NOT assign any semantic meaning to the SM-ID.</p>
     * </blockquote>
     *
     * @return The "SM-ID".
     */
    public final synchronized String getStreamManagementId() {
        return enabled != null ? enabled.getId() : null;
    }

    /**
     * Resets any client enabled state.
     */
    public synchronized void reset() {
        enabled = null;
        enabledByClient.set(true);
    }

    /**
     * Resumes the stream.
     *
     * @return The async result, which is done, if either the stream is not resumable, or the server resumed the stream.
     * @see <a href="https://xmpp.org/extensions/xep-0198.html#resumption">5. Resumption</a>
     */
    public AsyncResult<Boolean> resume() {
        if (!isResumable()) {
            return new AsyncResult<>(CompletableFuture.completedFuture(false));
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        StreamManagement.Resume resume;
        synchronized (this) {
            resumeFuture = future;
            // The <resume/> element MUST include a 'previd' attribute whose value is the SM-ID of the former stream and
            // MUST include an 'h' attribute that identifies the sequence number of the last handled stanza
            // sent over the former stream from the server to the client
            resume = new StreamManagement.Resume(inboundCount, getStreamManagementId());
        }
        xmppSession.send(resume);
        return new AsyncResult<>(future);
    }
}
