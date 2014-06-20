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

package org.xmpp;

import org.w3c.dom.Element;
import org.xmpp.bind.Bind;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.compress.CompressionManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.im.ChatManager;
import org.xmpp.im.PresenceManager;
import org.xmpp.im.RosterManager;
import org.xmpp.im.session.Session;
import org.xmpp.sasl.AuthenticationManager;
import org.xmpp.sasl.Mechanisms;
import org.xmpp.stanza.*;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;
import org.xmpp.stanza.errors.ServiceUnavailable;
import org.xmpp.stream.*;
import org.xmpp.tls.SecurityManager;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.sasl.SaslException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for establishing a XMPP session.
 *
 * @author Christian Schudt
 */
public class XmppSession implements Closeable {

    private static final long DEFAULT_REPLY_TIMEOUT = 20000;

    private static final Logger logger = Logger.getLogger(XmppSession.class.getName());

    /**
     * A lock object, used to create wait conditions.
     */
    private final Lock lock = new ReentrantLock();

    private final Condition streamNegotiatedUntilSasl;

    private final Condition streamNegotiatedUntilResourceBinding;

    /**
     * The unmarshaller, which is used to unmarshal XML during reading from the input stream.
     */
    private final Unmarshaller unmarshaller;

    /**
     * The marshaller, which is used to marshal XML during writing to the output stream.
     */
    private final Marshaller marshaller;

    private final SecurityManager securityManager;

    private final AuthenticationManager authenticationManager;

    private final RosterManager rosterManager;

    private final ReconnectionManager reconnectionManager;

    private final PresenceManager presenceManager;

    private final CompressionManager compressionManager;

    private final FeaturesManager featuresManager;

    private final ChatManager chatManager;

    private final Set<MessageListener> messageListeners = new CopyOnWriteArraySet<>();

    private final Set<PresenceListener> presenceListeners = new CopyOnWriteArraySet<>();

    private final Set<IQListener> iqListeners = new CopyOnWriteArraySet<>();

    private final Set<ConnectionListener> connectionListeners = new CopyOnWriteArraySet<>();

    private final Map<Class<? extends ExtensionManager>, ExtensionManager> instances = new ConcurrentHashMap<>();

    private final List<Connection> connections = new ArrayList<>();

    Executor stanzaListenerExecutor;

    /**
     * The user, which is assigned by the server after resource binding.
     */
    volatile Jid connectedResource;

    Connection activeConnection;

    /**
     * The XMPP domain which will be assigned by the server's response. This is read by different threads, so make it volatile to ensure visibility of the written value.
     */
    private volatile String xmppServiceDomain;

    /**
     * Holds the connection state.
     */
    private volatile Status status = Status.CLOSED;

    /**
     * The resource, which the user requested during resource binding. This value is stored, so that it can be reused during reconnection.
     */
    private volatile String resource;

    /**
     * Any exception that occurred during stream negotiation ({@link #connect()}) or ({@link #login(String, String)})
     */
    private volatile Exception exception;

    /**
     * The shutdown hook for JVM shutdown, which will disconnect each open connection before the JVM is halted.
     */
    private Thread shutdownHook;

    /**
     * Creates a connection with the specified XMPP domain through a proxy.
     *
     * @param xmppServiceDomain The XMPP service domain, which is used to lookup up the actual host via a DNS lookup.
     * @param connection        The connections. The session can have alternative connection methods, which are used during the connection process (e.g. a BOSH connection).
     */
    public XmppSession(String xmppServiceDomain, Connection... connection) {
        this(xmppServiceDomain, XmppContext.getDefault(), connection);
    }

    /**
     * Creates a connection to the specified host and port.
     *
     * @param xmppServiceDomain The XMPP service domain, which is used as value in the 'to' attribute of the opening stream.
     * @param xmppContext       The XMPP context.
     * @param connection        The connections. The session can have alternative connection methods, which are used during the connection process (e.g. a BOSH connection).
     */
    public XmppSession(String xmppServiceDomain, XmppContext xmppContext, Connection... connection) {
        this.xmppServiceDomain = xmppServiceDomain;
        this.stanzaListenerExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        try {
            // Create the JAXB context, the marshaller and unmarshaller, which will be used for this connection.
            Class[] newContext = new Class[xmppContext.getExtensions().size()];
            xmppContext.getExtensions().toArray(newContext);

            JAXBContext jaxbContext = JAXBContext.newInstance(newContext);
            unmarshaller = jaxbContext.createUnmarshaller();
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }

        for (Class<? extends ExtensionManager> cls : xmppContext.getExtensionManagers()) {
            // Initialize the managers.
            getExtensionManager(cls);
        }

        // Add a shutdown hook, which will gracefully close the connection, when the JVM is halted.
        shutdownHook = new Thread() {
            @Override
            public void run() {
                shutdownHook = null;
                try {
                    if (status == Status.CONNECTED) {
                        close();
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        securityManager = new SecurityManager(this, new FeatureListener() {
            @Override
            public void negotiationStatusChanged(FeatureEvent featureEvent) throws Exception {
                if (featureEvent.getStatus() == FeatureNegotiator.Status.SUCCESS) {
                    activeConnection.secureConnection();
                }
            }
        });

        reconnectionManager = new ReconnectionManager(this);
        featuresManager = new FeaturesManager(this);
        chatManager = new ChatManager(this);
        authenticationManager = new AuthenticationManager(this, lock);
        authenticationManager.addFeatureListener(new FeatureListener() {
            @Override
            public void negotiationStatusChanged(FeatureEvent featureEvent) {
                if (featureEvent.getStatus() == FeatureNegotiator.Status.INCOMPLETE && featureEvent.getElement() instanceof Mechanisms) {
                    // Release the waiting thread.
                    lock.lock();
                    try {
                        streamNegotiatedUntilSasl.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        });
        rosterManager = new RosterManager(this);
        presenceManager = new PresenceManager(this);

        streamNegotiatedUntilSasl = lock.newCondition();
        streamNegotiatedUntilResourceBinding = lock.newCondition();

        featuresManager.addFeatureNegotiator(securityManager);
        featuresManager.addFeatureNegotiator(authenticationManager);
        featuresManager.addFeatureNegotiator(new FeatureNegotiator(Bind.class) {
            @Override
            public Status processNegotiation(Object element) throws Exception {
                lock.lock();
                try {
                    streamNegotiatedUntilResourceBinding.signalAll();
                } finally {
                    lock.unlock();
                }
                // Resource binding will be negotiated manually
                return Status.INCOMPLETE;
            }

            @Override
            public boolean canProcess(Object element) {
                return false;
            }
        });

        compressionManager = new CompressionManager(this, new FeatureListener() {
            @Override
            public void negotiationStatusChanged(FeatureEvent featureEvent) {
                if (featureEvent.getStatus() == FeatureNegotiator.Status.SUCCESS) {
                    activeConnection.compressStream();
                }
            }
        });
        featuresManager.addFeatureNegotiator(compressionManager);

        // Every connection supports XEP-106 JID Escaping.
        getExtensionManager(ServiceDiscoveryManager.class).addFeature(new Feature("jid\\20escaping"));

        for (Connection con : connection) {
            con.setXmppSession(this);
            connections.add(con);
        }
    }

    /**
     * Gets the XMPP service domain.
     *
     * @return The XMPP service domain.
     */
    public String getXmppServiceDomain() {
        return xmppServiceDomain;
    }

    /**
     * Sets the XMPP service domain. This should only be set by a connection implementation.
     *
     * @param xmppServiceDomain The XMPP service domain.
     */
    public void setXmppServiceDomain(String xmppServiceDomain) {
        this.xmppServiceDomain = xmppServiceDomain;
    }

    /**
     * Adds a message listener to the connection, which will get notified, whenever a message is received.
     *
     * @param messageListener The message listener.
     * @see #removeMessageListener(MessageListener)
     */
    public final void addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    /**
     * Removes a previously added message listener from the connection.
     *
     * @param messageListener The message listener.
     * @see #addMessageListener(MessageListener)
     */
    public final void removeMessageListener(MessageListener messageListener) {
        messageListeners.remove(messageListener);
    }

    /**
     * Adds a presence listener to the connection, which will get notified, whenever a presence is received.
     *
     * @param presenceListener The presence listener.
     * @see #removePresenceListener(PresenceListener)
     */
    public final void addPresenceListener(PresenceListener presenceListener) {
        presenceListeners.add(presenceListener);
    }

    /**
     * Removes a previously added presence listener from the connection.
     *
     * @param presenceListener The presence listener.
     * @see #addPresenceListener(PresenceListener)
     */
    public final void removePresenceListener(PresenceListener presenceListener) {
        presenceListeners.remove(presenceListener);
    }

    /**
     * Adds an IQ listener to the connection, which will get notified, whenever an IQ stanza is received.
     *
     * @param iqListener The IQ listener.
     * @see #removeIQListener(IQListener)
     */
    public final void addIQListener(IQListener iqListener) {
        iqListeners.add(iqListener);
    }

    /**
     * Removes a previously added IQ listener from the connection.
     *
     * @param iqListener The IQ listener.
     * @see #addIQListener(IQListener)
     */
    public final void removeIQListener(IQListener iqListener) {
        iqListeners.remove(iqListener);
    }

    /**
     * Adds a connection listener, which listens for connection status changes.
     * Each time the {@linkplain Status connection status} changes, all listeners will be notified.
     *
     * @param connectionListener The connection listener.
     * @see #removeConnectionListener(ConnectionListener)
     */
    public final void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    /**
     * Removes a previously added connection listener.
     *
     * @param connectionListener The connection listener.
     * @see #addConnectionListener(ConnectionListener)
     */
    public final void removeConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.remove(connectionListener);
    }

    private void notifyStanzaListeners(Stanza element, boolean incoming) {
        if (element instanceof Message) {
            for (MessageListener messageListener : messageListeners) {
                try {
                    messageListener.handle(new MessageEvent(this, (Message) element, incoming));
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else if (element instanceof Presence) {
            for (PresenceListener presenceListener : presenceListeners) {
                try {
                    presenceListener.handle(new PresenceEvent(this, (Presence) element, incoming));
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else if (element instanceof IQ) {
            for (IQListener iqListener : iqListeners) {
                try {
                    iqListener.handle(new IQEvent(this, (IQ) element, incoming));
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    private void notifyConnectionListeners(Status status, Status oldStatus, Exception exception) {
        for (ConnectionListener connectionListener : connectionListeners) {
            try {
                connectionListener.statusChanged(new ConnectionEvent(this, status, oldStatus, exception));
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Awaits for a presence stanza to arrive. The filter determined the characteristics of the presence stanza.
     *
     * @param presence The presence, which is sent.
     * @param filter   The presence filter.
     * @return The presence stanza.
     * @throws NoResponseException If no presence stanza has arrived in time.
     * @throws StanzaException     If the returned presence contains a stanza error.
     */
    public Presence sendAndAwait(Presence presence, final StanzaFilter<Presence> filter) throws NoResponseException, StanzaException {

        final Presence[] result = new Presence[1];

        final Condition resultReceived = lock.newCondition();

        final PresenceListener listener = new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                Presence presence = e.getPresence();
                if (e.isIncoming() && filter.accept(presence)) {
                    lock.lock();
                    try {
                        result[0] = presence;
                    } finally {
                        resultReceived.signal();
                        lock.unlock();
                    }
                }
            }
        };

        lock.lock();
        try {
            addPresenceListener(listener);
            send(presence);
            // Wait for the stanza to arrive.
            if (!resultReceived.await(DEFAULT_REPLY_TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on a response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
            removePresenceListener(listener);
        }
        Presence response = result[0];
        if (response.getType() == Presence.Type.ERROR) {
            throw new StanzaException(response);
        }
        return result[0];
    }

    /**
     * Awaits for a presence stanza to arrive. The filter determined the characteristics of the presence stanza.
     *
     * @param message The message, which is sent.
     * @param filter  The message filter.
     * @return The message stanza.
     * @throws NoResponseException If no message stanza has arrived in time.
     * @throws StanzaException     If the returned message contains a stanza error.
     */
    public Message sendAndAwait(Message message, final StanzaFilter<Message> filter) throws NoResponseException, StanzaException {

        final Message[] result = new Message[1];

        final Condition resultReceived = lock.newCondition();

        final MessageListener listener = new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                Message message = e.getMessage();
                if (e.isIncoming() && filter.accept(message)) {
                    lock.lock();
                    try {
                        result[0] = message;
                    } finally {
                        resultReceived.signal();
                        lock.unlock();
                    }
                }
            }
        };

        lock.lock();
        try {
            addMessageListener(listener);
            send(message);
            // Wait for the stanza to arrive.
            if (!resultReceived.await(DEFAULT_REPLY_TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on a response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
            removeMessageListener(listener);
        }
        Message response = result[0];
        if (response.getType() == Message.Type.ERROR) {
            throw new StanzaException(response);
        }
        return response;
    }

    /**
     * Sends an {@code <iq/>} stanza and waits for the response.
     * <p>
     * This method blocks until a result was received or a timeout occurred.
     * </p>
     *
     * @param iq The {@code <iq/>} stanza, which must be of type {@linkplain org.xmpp.stanza.AbstractIQ.Type#GET get} or {@linkplain org.xmpp.stanza.AbstractIQ.Type#SET set}.
     * @return The result {@code <iq/>} stanza.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public IQ query(IQ iq) throws XmppException {
        return query(iq, DEFAULT_REPLY_TIMEOUT);
    }

    /**
     * Sends an {@code <iq/>} stanza and waits for the response.
     * <p>
     * This method blocks until a result was received or a timeout occurred.
     * </p>
     *
     * @param iq      The {@code <iq/>} stanza, which must be of type {@linkplain org.xmpp.stanza.AbstractIQ.Type#GET get} or {@linkplain org.xmpp.stanza.AbstractIQ.Type#SET set}.
     * @param timeout The timeout.
     * @return The result {@code <iq/>} stanza.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public IQ query(final IQ iq, long timeout) throws XmppException {
        if (!(iq.getType() == IQ.Type.GET || iq.getType() == IQ.Type.SET)) {
            throw new IllegalArgumentException("IQ must be of type 'get' or 'set'");
        }

        final IQ[] result = new IQ[1];

        final Condition resultReceived = lock.newCondition();

        final IQListener iqListener = new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming() && e.getIQ().getId() != null && e.getIQ().getId().equals(iq.getId())) {
                    lock.lock();
                    try {
                        result[0] = e.getIQ();
                    } finally {
                        resultReceived.signal();
                        lock.unlock();
                    }
                }
            }
        };

        lock.lock();
        try {
            addIQListener(iqListener);
            send(iq);
            if (!resultReceived.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on an IQ response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
            removeIQListener(iqListener);
        }

        IQ response = result[0];
        if (response.getType() == IQ.Type.ERROR) {
            throw new StanzaException(response);
        }
        return response;
    }

    /**
     * Reconnects to the XMPP server and automatically logs in by using the last known information (e.g. user name, password and bound resource).
     *
     * @throws IOException    If an exception occurred while connecting.
     * @throws LoginException If an exception occurred while logging in.
     */
    public final synchronized void reconnect() throws IOException, LoginException {
        connect();
        try {
            updateStatus(Status.AUTHENTICATING);
            getAuthenticationManager().reAuthenticate();
            bindResource(resource);
        } catch (Exception e) {
            updateStatus(Status.DISCONNECTED);
            throw e;
        }
        updateStatus(Status.AUTHENTICATED);
    }

    /**
     * Connects to the XMPP server.
     *
     * @throws IOException If anything went wrong, e.g. the host was not found.
     */
    public synchronized void connect() throws IOException {
        if (status != Status.CLOSED && status != Status.DISCONNECTED) {
            throw new IllegalStateException("Already connected.");
        }
        // Should either be CLOSED or DISCONNECTED
        Status previousStatus = status;
        updateStatus(Status.CONNECTING);
        // Reset
        exception = null;

        Iterator<Connection> connectionIterator = connections.iterator();
        while (connectionIterator.hasNext()) {
            Connection connection = connectionIterator.next();
            try {
                activeConnection = connection;
                connection.connect();
                break;
            } catch (IOException e) {
                if (connectionIterator.hasNext()) {
                    if (xmppServiceDomain != null) {
                        logger.log(Level.WARNING, String.format("Connection to domain %s failed. Trying alternative connection.", xmppServiceDomain), e);
                    } else {
                        logger.log(Level.WARNING, String.format("Connection to host %s:%s failed. Trying alternative connection.", connection.getHostname(), connection.getPort()), e);
                    }
                } else {
                    updateStatus(previousStatus, e);
                    throw e;
                }
            }
        }

        // Wait until the reader thread signals, that we are connected. That is after TLS negotiation and before SASL negotiation.
        try {
            waitUntilSaslNegotiationStarted();
        } catch (NoResponseException e) {
            throw new IOException(e);
        }

        updateStatus(Status.CONNECTED);
    }

    /**
     * Explicitly closes the connection and performs a clean up of all listeners.
     *
     * @throws IOException If an exception occurs while closing the connection, e.g. the underlying socket connection.
     */
    @Override
    public synchronized void close() throws IOException {
        updateStatus(Status.CLOSING);
        // Clear everything.
        messageListeners.clear();
        presenceListeners.clear();
        iqListeners.clear();
        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        if (activeConnection != null) {
            activeConnection.close();
            activeConnection = null;
        }
        updateStatus(Status.CLOSED);
    }

    /**
     * Sends an XML element to the server, usually a stanza, i.e. a message, presence or IQ.
     *
     * @param element The XML element.
     */
    public void send(ClientStreamElement element) {
        if (element instanceof Stanza) {
            notifyStanzaListeners((Stanza) element, false);
        }
        if (activeConnection != null) {
            activeConnection.send(element);
        } else {
            throw new IllegalStateException("No connection established.");
        }
    }

    /**
     * Authenticates against the server and binds a random resource (assigned by the server).
     *
     * @param user     The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password The password. Must not be null.
     * @throws LoginException             If the login failed, due to a SASL error reported by the server.
     * @throws FailedLoginException       If the login failed, due to a wrong username or password. It is thrown if the server reports a {@code <not-authorized/>} SASL error.
     * @throws AccountLockedException     If the login failed, because the account has been disabled.  It is thrown if the server reports a {@code <account-disabled/>} SASL error.
     * @throws CredentialExpiredException If the login failed, because the credentials have expired. It is thrown if the server reports a {@code <credentials-expired/>} SASL error.
     */
    public synchronized final void login(String user, String password) throws LoginException {
        login(user, password, null);
    }

    /**
     * Authenticates against the server and binds a resource.
     *
     * @param user     The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password The password. Must not be null.
     * @param resource The resource. If null or empty, the resource is randomly assigned by the server.
     * @throws LoginException             If the login failed, due to a SASL error reported by the server.
     * @throws FailedLoginException       If the login failed, due to a wrong username or password. It is thrown if the server reports a {@code <not-authorized/>} SASL error.
     * @throws AccountLockedException     If the login failed, because the account has been disabled.  It is thrown if the server reports a {@code <account-disabled/>} SASL error.
     * @throws CredentialExpiredException If the login failed, because the credentials have expired. It is thrown if the server reports a {@code <credentials-expired/>} SASL error.
     */
    public synchronized final void login(String user, String password, String resource) throws LoginException {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null.");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null.");
        }
        if (getDomain() == null) {
            throw new IllegalStateException("The XMPP domain must not be null.");
        }
        if (getStatus() == Status.AUTHENTICATED) {
            throw new IllegalStateException("You are already logged in.");
        }
        if (getStatus() != Status.CONNECTED) {
            throw new IllegalStateException("You must be connected to the server before trying to login.");
        }
        try {
            try {
                updateStatus(Status.AUTHENTICATING);
                authenticationManager.authenticate(new Jid(user, getDomain()).toString(), user, password, null);
            } catch (SaslException e) {
                throw new LoginException(e.getMessage());
            }

            bindResource(resource);

            if (getRosterManager().isRetrieveRosterOnLogin()) {
                getRosterManager().requestRoster();
            }
        } catch (Exception e) {
            // Revert status
            updateStatus(Status.CONNECTED);
            throw e;
        }
        updateStatus(Status.AUTHENTICATED);
    }

    /**
     * Logs in anonymously and binds a resource.
     *
     * @throws LoginException If the anonymous login failed.
     * @see org.xmpp.sasl.AuthenticationManager#authenticateAnonymously()
     */
    public synchronized final void loginAnonymously() throws LoginException {
        try {
            updateStatus(Status.AUTHENTICATING);
            authenticationManager.authenticateAnonymously();
            bindResource(null);
        } catch (Exception e) {
            // Revert status
            updateStatus(Status.CONNECTED);
            throw e;
        }
        updateStatus(Status.AUTHENTICATED);
    }

    /**
     * Binds a resource to the connection.
     *
     * @param resource The resource to bind. If the resource is null and random resource is bound by the server.
     * @throws LoginException If the resource binding failed as described in <a href="http://xmpp.org/rfcs/rfc6120.html#bind-servergen-error">7.6.2.  Error Cases</a>
     */
    private void bindResource(String resource) throws LoginException {
        this.resource = resource;

        // Then wait until the bind feature is received, if it hasn't yet.
        if (!featuresManager.getFeatures().containsKey(Bind.class)) {
            lock.lock();
            try {
                if (!streamNegotiatedUntilResourceBinding.await(5, TimeUnit.SECONDS)) {
                    throw new LoginException("Timeout reached during resource binding.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
        // Bind the resource
        IQ iq = new IQ(IQ.Type.SET, new Bind(this.resource));
        IQ result;
        try {
            result = query(iq);
        } catch (StanzaException e) {
            LoginException loginException = new LoginException("Error during resource binding: " + e.getStanza().toString());
            loginException.initCause(e);
            throw loginException;
        } catch (XmppException e) {
            LoginException loginException = new LoginException(e.getMessage());
            loginException.initCause(e);
            throw loginException;
        }

        Bind bindResult = result.getExtension(Bind.class);
        this.connectedResource = bindResult.getJid();

        // Deprecated method of session binding, according to the <a href="http://xmpp.org/rfcs/rfc3921.html#session">old specification</a>
        // This is no longer used, according to the <a href="http://xmpp.org/rfcs/rfc6120.html">updated specification</a>.
        // But some old server implementation still require it.
        if (featuresManager.getFeatures().containsKey(Session.class)) {
            try {
                query(new IQ(IQ.Type.SET, new Session()));
            } catch (StanzaException e) {
                LoginException loginException = new LoginException("Error during session establishment: " + e.getStanza().toString());
                loginException.initCause(e);
                throw loginException;
            } catch (XmppException e) {
                LoginException loginException = new LoginException(e.getMessage());
                loginException.initCause(e);
                throw loginException;
            }
        }
    }

    /**
     * Handles a XMPP element.
     * <p>
     * This method should be called on the reader thread.
     * </p>
     *
     * @param element The XMPP element.
     * @return True, if the stream needs to be restarted; otherwise false.
     * @throws StreamException If the element is a stream error.
     * @throws Exception       If any exception occurred during feature negotiation.
     */
    public final boolean handleElement(final Object element) throws Exception {

        if (element instanceof Stanza) {
            stanzaListenerExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (element instanceof IQ) {
                        IQ iq = (IQ) element;
                        if (iq.getType() == IQ.Type.GET || iq.getType() == IQ.Type.SET) {
                            // By default JAXB creates an Element if it can't unmarshall some XML.
                            // If it can't unmarshall it, it means that no listener will handle it and we should return an error.
                            if (iq.getExtension(Element.class) != null) {
                                // return <service-unavailble/> if the <iq/> is not understood.
                                IQ error = iq.createError(new StanzaError(new ServiceUnavailable()));
                                send(error);
                            }
                        }
                    }
                    notifyStanzaListeners((Stanza) element, true);
                }
            });
        } else if (element instanceof Features) {
            featuresManager.processFeatures((Features) element);
        } else if (element instanceof StreamException) {
            throw (StreamException) element;
        } else {
            // Let's see, if the element is known to any feature negotiator.
            return featuresManager.processElement(element);
        }
        return false;
    }

    /**
     * Called if any unhandled exception is thrown during reading or writing.
     * <p>
     * This method will close the stream.
     * </p>
     *
     * @param e The exception. If an unrecoverable XMPP stream error occurred, the exception is a {@link org.xmpp.stream.StreamException}.
     */
    public final void notifyException(Exception e) {
        // If the exception occurred during stream negotiation, i.e. before the connect() method has finished, the exception will be thrown.
        exception = e;
        if (status != Status.CLOSING && status != Status.CLOSED) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        // Release a potential waiting thread.
        lock.lock();
        try {
            streamNegotiatedUntilSasl.signalAll();
        } finally {
            lock.unlock();
        }
        synchronized (this) {
            if (status == Status.AUTHENTICATED || status == Status.AUTHENTICATING || status == Status.CONNECTED || status == Status.CONNECTING) {
                updateStatus(Status.DISCONNECTED, e);
            }
        }
    }

    /**
     * @param clazz The class of the extension manager.
     * @param <T>   The type.
     * @return An instance of the specified extension manager.
     */
    @SuppressWarnings("unchecked")
    public final <T extends ExtensionManager> T getExtensionManager(Class<T> clazz) {
        // http://j2eeblogger.blogspot.de/2007/10/singleton-vs-multiton-synchronization.html
        T instance;
        if ((instance = (T) instances.get(clazz)) == null) {
            synchronized (instances) {
                if ((instance = (T) instances.get(clazz)) == null) {
                    try {
                        Constructor<T> constructor = clazz.getDeclaredConstructor(XmppSession.class);
                        constructor.setAccessible(true);
                        instance = constructor.newInstance(this);
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        throw new IllegalArgumentException(e);
                    }
                    instances.put(clazz, instance);
                }
            }
        }
        return instance;
    }

    /**
     * Gets the security manager, which is responsible for TLS.
     *
     * @return The security manager.
     */
    public final SecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Gets the authentication manager, which is responsible for SASL negotiation.
     *
     * @return The authentication manager.
     */
    public final AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * Gets the roster manager, which is responsible for retrieving, updating and deleting contacts from the roster.
     *
     * @return The roster manager.
     */
    public final RosterManager getRosterManager() {
        return rosterManager;
    }

    /**
     * Gets the presence manager, which is responsible for presence subscriptions.
     *
     * @return The presence manager.
     */
    public final PresenceManager getPresenceManager() {
        return presenceManager;
    }

    /**
     * Gets the reconnection manager, which is responsible for automatic reconnection.
     *
     * @return The reconnection manager.
     */
    public final ReconnectionManager getReconnectionManager() {
        return reconnectionManager;
    }

    /**
     * Gets the features manager, which is responsible for negotiating features.
     *
     * @return The features manager.
     */
    public final FeaturesManager getFeaturesManager() {
        return featuresManager;
    }

    /**
     * Gets the chat manager, which is responsible for one-to-one chat sessions.
     *
     * @return The chat manager.
     */
    public final ChatManager getChatManager() {
        return chatManager;
    }

    /**
     * Gets the compression manager, which is responsible for stream compression.
     *
     * @return The compression manager.
     */
    public final CompressionManager getCompressionManager() {
        return compressionManager;
    }

    /**
     * Gets the connected resource, which is assigned by the server after resource binding.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#bind-fundamentals">7.1.  Fundamentals</a></cite></p>
     * <p>After a client has bound a resource to the stream, it is referred to as a "connected resource".</p>
     * </blockquote>
     *
     * @return The connected resource.
     */
    public final Jid getConnectedResource() {
        return connectedResource;
    }

    /**
     * Gets the XMPP domain of the connected server. This variable is set after server has responded with a stream header. The domain is the stream header's 'from' attribute.
     *
     * @return The XMPP domain.
     */
    public final String getDomain() {
        return xmppServiceDomain;
    }

    /**
     * Gets the status of the connection.
     *
     * @return The status.
     */
    public final Status getStatus() {
        return status;
    }

    private void updateStatus(Status status) {
        updateStatus(status, null);
    }

    /**
     * Updates the status and notifies the connection listeners.
     *
     * @param status The new status.
     */
    final void updateStatus(Status status, Exception e) {
        if (this.status != status) {
            Status oldStatus = this.status;
            this.status = status;
            notifyConnectionListeners(status, oldStatus, e);
        }
        if (status == Status.CLOSED) {
            connectionListeners.clear();
        }
    }

    /**
     * Gets an unmodifiable list of connections.
     *
     * @return The connections.
     */
    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     * Gets the unmarshaller, which is used to unmarshal XML during reading from the input stream.
     *
     * @return The unmarshaller.
     */
    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Gets the marshaller, which is used to marshal XML during writing to the output stream.
     *
     * @return The marshaller.
     */
    public Marshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Waits until SASL negotiation has started and then releases the lock. This method must be invoked at the end of the {@link #connect()} method.
     *
     * @throws NoResponseException If no response was received from the server.
     * @throws IOException         If any exception occurred during stream negotiation.
     */
    protected final void waitUntilSaslNegotiationStarted() throws NoResponseException, IOException {
        // Wait for the response and wait until all features have been negotiated.
        lock.lock();
        try {
            if (!streamNegotiatedUntilSasl.await(10000, TimeUnit.SECONDS)) {
                // Check if an exception has occurred during stream negotiation and throw it.
                if (exception != null) {
                    try {
                        throw new IOException(exception);
                    } finally {
                        exception = null;
                    }
                } else {
                    throw new NoResponseException("Timeout reached while connecting.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Represents the connection status.
     */
    public enum Status {
        /**
         * The session is currently negotiating features.
         */
        CONNECTING,
        /**
         * The session is established with the server, features are negotiated, but we are not yet authenticated.
         */
        CONNECTED,
        /**
         * The session is currently authenticating with the server.
         */
        AUTHENTICATING,
        /**
         * The session has authenticated.
         */
        AUTHENTICATED,
        /**
         * The session has been temporarily disconnect by an exception.
         */
        DISCONNECTED,
        /**
         * The session is closing.
         */
        CLOSING,
        /**
         * The session is closed.
         */
        CLOSED
    }
}
