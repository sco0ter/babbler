/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.extensions.component.accept;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.Trackable;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.extensions.component.accept.model.ComponentIQ;
import rocks.xmpp.extensions.component.accept.model.ComponentMessage;
import rocks.xmpp.extensions.component.accept.model.ComponentPresence;
import rocks.xmpp.extensions.component.accept.model.Handshake;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * An external component session which connects to an XMPP server using the "Jabber Component Protocol".
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0114.html">XEP-0114: Jabber Component Protocol</a>
 */
public final class ExternalComponent extends XmppSession {

    private static final Logger logger = Logger.getLogger(ExternalComponent.class.getName());

    private final Lock lock = new ReentrantLock();

    private final Condition streamOpened = lock.newCondition();

    private final Condition handshakeReceived = lock.newCondition();

    private final String sharedSecret;

    private volatile Jid connectedResource;

    private volatile boolean streamHeaderReceived;

    public ExternalComponent(String componentName, String sharedSecret, String hostname, int port) {
        this(componentName, sharedSecret, XmppSessionConfiguration.getDefault(), hostname, port);
    }

    public ExternalComponent(String componentName, String sharedSecret, XmppSessionConfiguration configuration, String hostname, int port) {
        super(componentName, configuration, TcpConnectionConfiguration.builder().hostname(hostname).port(port).build());
        this.sharedSecret = sharedSecret;
    }

    @Override
    public final void connect(Jid from) throws XmppException {
        Status previousStatus = preConnect();

        try {
            if (!checkConnected()) {
                // Don't call listeners from within synchronized blocks to avoid possible deadlocks.

                updateStatus(Status.CONNECTING);
                synchronized (this) {

                    // Double-checked locking: Recheck connected status. In a multi-threaded environment multiple threads could have passed the first check.
                    if (!checkConnected()) {
                        // Reset
                        exception = null;
                        streamHeaderReceived = false;

                        tryConnect(from, "jabber:component:accept", this::onStreamOpened);
                        logger.fine("Negotiating stream, waiting until handshake is ready to be negotiated.");

                        lock.lock();
                        try {
                            if (!streamHeaderReceived) {
                                streamOpened.await(configuration.getDefaultResponseTimeout(), TimeUnit.MILLISECONDS);
                            }
                        } finally {
                            lock.unlock();
                        }

                        // Wait shortly to see if the server will respond with a <conflict/>, <host-unknown/> or other stream error.
                        Thread.sleep(20);

                        // Check if the server returned a stream error, e.g. conflict.
                        throwAsXmppExceptionIfNotNull(exception);

                        connectedResource = getDomain();
                    }
                }
            }

            // Don't call listeners from within synchronized blocks to avoid possible deadlocks.
            updateStatus(Status.CONNECTING, Status.CONNECTED);
            login(sharedSecret);
        } catch (Throwable e) {
            onConnectionFailed(previousStatus, e);
        }
    }

    /**
     * Authenticates with the server using a shared secret.
     *
     * @param sharedSecret The shared secret.
     * @throws XmppException If authentication failed.
     */
    private void login(String sharedSecret) throws XmppException {
        Status previousStatus = preLogin();

        try {
            if (checkAuthenticated()) {
                // Silently return, when we are already authenticated.
                return;
            }
            updateStatus(Status.AUTHENTICATING);
            synchronized (this) {
                if (checkAuthenticated()) {
                    // Silently return, when we are already authenticated.
                    return;
                }
                // Send the <handshake/> element.
                send(Handshake.create(activeConnection.getStreamId(), sharedSecret));
                lock.lock();
                try {
                    // Wait for the <handshake/> element to be received from the server.
                    handshakeReceived.await(configuration.getDefaultResponseTimeout(), TimeUnit.MILLISECONDS);
                } finally {
                    lock.unlock();
                }
            }
            // Authentication succeeded, update the status.
            updateStatus(Status.AUTHENTICATED);
            // Check if the server returned a stream error, e.g. not-authorized and throw it.
            throwAsXmppExceptionIfNotNull(exception);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Revert status
            updateStatus(previousStatus, e);
            throwAsXmppExceptionIfNotNull(e);
        } catch (Throwable e) {
            // Revert status
            updateStatus(previousStatus, e);
            throwAsXmppExceptionIfNotNull(e);
        }

    }

    @Override
    public final boolean handleElement(Object element) throws XmppException {
        if (element instanceof Handshake) {
            releaseLock();
        } else {
            super.handleElement(element);
        }
        return false;
    }

    @Override
    public final void notifyException(Throwable e) {
        super.notifyException(e);
        releaseLock();
    }

    private void releaseLock() {
        lock.lock();
        try {
            handshakeReceived.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void onStreamOpened(Jid domain) {
        setXmppServiceDomain(domain);
        streamHeaderReceived = true;
        lock.lock();
        try {
            streamOpened.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final Jid getConnectedResource() {
        return connectedResource;
    }

    @Override
    public final Future<?> send(StreamElement element) {

        if (element instanceof Stanza && ((Stanza) element).getFrom() == null) {
            ((Stanza) element).setFrom(connectedResource);
        }
        if (element instanceof Message) {
            element = ComponentMessage.from((Message) element);
        } else if (element instanceof Presence) {
            element = ComponentPresence.from((Presence) element);
        } else if (element instanceof IQ) {
            element = ComponentIQ.from((IQ) element);
        }

        return super.send(element);
    }

    @Override
    public final Trackable<IQ> sendIQ(final IQ iq) {
        return trackAndSend(ComponentIQ.from(iq));
    }

    @Override
    public final Trackable<Message> sendMessage(final Message message) {
        return trackAndSend(ComponentMessage.from(message));
    }

    @Override
    public final Trackable<Presence> sendPresence(final Presence presence) {
        return trackAndSend(ComponentPresence.from(presence));
    }
}
