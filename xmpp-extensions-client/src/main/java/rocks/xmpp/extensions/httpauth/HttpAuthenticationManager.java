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

package rocks.xmpp.extensions.httpauth;

import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQListener;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.extensions.httpauth.model.ConfirmationRequest;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This manager allows to listen for incoming requests (by an XMPP server) to confirm that the current XMPP user made an HTTP request, i.e. to verify that the HTTP request was in fact made by the XMPP user.
 * <p>
 * If you want to confirm or deny HTTP requests, {@linkplain #addHttpAuthenticationListener(HttpAuthenticationListener) add a listener} and call {@link HttpAuthenticationEvent#confirm()} or {@link HttpAuthenticationEvent#deny()} on the event object.
 * </p>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0070.html">XEP-0070: Verifying HTTP Requests via XMPP</a>
 */
public final class HttpAuthenticationManager extends ExtensionManager implements SessionStatusListener, IQListener, MessageListener {

    private static final Logger logger = Logger.getLogger(HttpAuthenticationManager.class.getName());

    private final Set<HttpAuthenticationListener> httpAuthenticationListeners = new CopyOnWriteArraySet<>();

    private HttpAuthenticationManager(XmppSession xmppSession) {
        // TODO: Include namespace here for Service Discovery? (no mentioning in XEP-0070)
        super(xmppSession);

        xmppSession.addSessionStatusListener(this);

        xmppSession.addIQListener(this);

        xmppSession.addMessageListener(this);
    }

    private boolean notifyHttpAuthListeners(Stanza stanza, ConfirmationRequest confirmationRequest) {
        boolean handled = false;
        for (HttpAuthenticationListener httpAuthenticationListener : httpAuthenticationListeners) {
            try {
                httpAuthenticationListener.confirmationRequested(new HttpAuthenticationEvent(HttpAuthenticationManager.this, xmppSession, stanza, confirmationRequest));
                handled = true;
            } catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return handled;
    }

    /**
     * Adds a HTTP authentication listener, which allows to listen for HTTP authentication confirmation requests.
     *
     * @param httpAuthenticationListener The listener.
     * @see #removeHttpAuthenticationListener(HttpAuthenticationListener)
     */
    public void addHttpAuthenticationListener(HttpAuthenticationListener httpAuthenticationListener) {
        httpAuthenticationListeners.add(httpAuthenticationListener);
    }

    /**
     * Removes a previously added HTTP authentication listener.
     *
     * @param httpAuthenticationListener The listener.
     * @see #addHttpAuthenticationListener(HttpAuthenticationListener)
     */
    public void removeHttpAuthenticationListener(HttpAuthenticationListener httpAuthenticationListener) {
        httpAuthenticationListeners.remove(httpAuthenticationListener);
    }

    @Override
    public void handleIQ(IQEvent e) {
        IQ iq = e.getIQ();
        if (e.isIncoming() && !e.isConsumed() && iq.getType() == IQ.Type.GET) {
            ConfirmationRequest confirmationRequest = iq.getExtension(ConfirmationRequest.class);
            if (confirmationRequest != null) {
                if (notifyHttpAuthListeners(iq, confirmationRequest)) {
                    e.consume();
                }
            }
        }
    }

    @Override
    public void handleMessage(MessageEvent e) {
        if (e.isIncoming()) {
            Message message = e.getMessage();
            if (message.getType() == null || message.getType() == Message.Type.NORMAL) {
                ConfirmationRequest confirmationRequest = message.getExtension(ConfirmationRequest.class);
                if (confirmationRequest != null) {
                    notifyHttpAuthListeners(message, confirmationRequest);
                }
            }
        }
    }

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            httpAuthenticationListeners.clear();
        }
    }
}
