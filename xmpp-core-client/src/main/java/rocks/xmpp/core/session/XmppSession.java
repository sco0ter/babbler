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

package rocks.xmpp.core.session;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.roster.RosterManager;
import rocks.xmpp.core.sasl.model.AuthenticationException;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.session.model.Session;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.IQListener;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.PresenceListener;
import rocks.xmpp.core.stanza.StanzaFilter;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stanza.model.StanzaException;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.stream.StreamFeatureEvent;
import rocks.xmpp.core.stream.StreamFeatureListener;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.ClientStreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamException;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.subscription.PresenceManager;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The base class for establishing an XMPP session with a server.
 *
 * @author Christian Schudt
 */
public class XmppSession implements Closeable {

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

    private final AuthenticationManager authenticationManager;

    private final RosterManager rosterManager;

    private final ReconnectionManager reconnectionManager;

    private final PresenceManager presenceManager;

    private final StreamFeaturesManager streamFeaturesManager;

    private final ChatManager chatManager;

    private final Set<MessageListener> messageListeners = new CopyOnWriteArraySet<>();

    private final Set<PresenceListener> presenceListeners = new CopyOnWriteArraySet<>();

    private final Set<IQListener> iqListeners = new CopyOnWriteArraySet<>();

    private final Map<Class<?>, IQHandler> iqHandlerMap = new ConcurrentHashMap<>();

    private final Set<SessionStatusListener> sessionStatusListeners = new CopyOnWriteArraySet<>();

    private final Map<Class<? extends ExtensionManager>, ExtensionManager> instances = new ConcurrentHashMap<>();

    private final List<Connection> connections = new ArrayList<>();

    private final XmppSessionConfiguration configuration;

    ExecutorService iqHandlerExecutor;

    ExecutorService stanzaListenerExecutor;

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
    private volatile Status status = Status.INITIAL;

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

    private boolean wasLoggedIn;

    private XmppDebugger debugger;

    /**
     * Creates a session with the specified service domain, by using the default configuration.
     *
     * @param xmppServiceDomain        The service domain.
     * @param connectionConfigurations The connection configurations.
     */
    public XmppSession(String xmppServiceDomain, ConnectionConfiguration... connectionConfigurations) {
        this(xmppServiceDomain, XmppSessionConfiguration.getDefault(), connectionConfigurations);
    }

    /**
     * Creates a session with the specified service domain by using a configuration.
     *
     * @param xmppServiceDomain        The service domain.
     * @param configuration            The configuration.
     * @param connectionConfigurations The connection configurations.
     */
    public XmppSession(String xmppServiceDomain, XmppSessionConfiguration configuration, ConnectionConfiguration... connectionConfigurations) {
        this.xmppServiceDomain = xmppServiceDomain;
        this.configuration = configuration;
        this.stanzaListenerExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        this.iqHandlerExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        try {
            // Create the marshaller and unmarshaller, which will be used for this connection.
            unmarshaller = configuration.getJAXBContext().createUnmarshaller();
            marshaller = configuration.getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }

        for (Class<? extends ExtensionManager> cls : configuration.getInitialExtensionManagers()) {
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

        reconnectionManager = new ReconnectionManager(this);
        streamFeaturesManager = new StreamFeaturesManager(this);
        chatManager = new ChatManager(this);
        authenticationManager = new AuthenticationManager(this, lock, configuration.getAuthenticationMechanisms());
        authenticationManager.addFeatureListener(new StreamFeatureListener() {
            @Override
            public void negotiationStatusChanged(StreamFeatureEvent streamFeatureEvent) {
                if (streamFeatureEvent.getStatus() == StreamFeatureNegotiator.Status.INCOMPLETE && streamFeatureEvent.getElement() instanceof Mechanisms) {
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

        streamFeaturesManager.addFeatureNegotiator(authenticationManager);
        streamFeaturesManager.addFeatureNegotiator(new StreamFeatureNegotiator(Bind.class) {
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

        // Every connection supports XEP-106 JID Escaping.
        getExtensionManager(ServiceDiscoveryManager.class).addFeature(new Feature("jid\\20escaping"));

        if (configuration.getDebugger() != null) {
            try {
                this.debugger = configuration.getDebugger().newInstance();
                this.debugger.initialize(this);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        if (connectionConfigurations.length == 0) {
            // Add two fallback connections. Host and port will be determined by the XMPP domain via SRV lookup.
            connections.add(TcpConnectionConfiguration.getDefault().createConnection(this));
            connections.add(BoshConnectionConfiguration.getDefault().createConnection(this));
        } else {
            for (ConnectionConfiguration connectionConfiguration : connectionConfigurations) {
                connections.add(connectionConfiguration.createConnection(this));
            }
        }
    }

    private static void throwExceptionIfNotNull(Exception e) throws AuthenticationException {
        if (e != null) {
            AuthenticationException authenticationException = new AuthenticationException("Authentication failed");
            authenticationException.initCause(e);
            throw authenticationException;
        }
    }

    /**
     * Gets the actively used connection.
     *
     * @return The actively used connection.
     */
    public Connection getActiveConnection() {
        return activeConnection;
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
     * Adds an IQ handler for a given payload type.
     *
     * @param type      The payload type.
     * @param iqHandler The IQ handler.
     * @see #removeIQHandler(Class)
     */
    public final void addIQHandler(Class<?> type, IQHandler iqHandler) {
        iqHandlerMap.put(type, iqHandler);
    }

    /**
     * Removes an IQ handler.
     *
     * @param type The payload type.
     * @see #addIQHandler(Class, rocks.xmpp.core.stanza.IQHandler)
     */
    public final void removeIQHandler(Class<?> type) {
        iqHandlerMap.remove(type);
    }

    /**
     * Adds a session listener, which listens for session status changes.
     * Each time the {@linkplain Status session status} changes, all listeners will be notified.
     *
     * @param sessionStatusListener The session listener.
     * @see #removeSessionStatusListener(SessionStatusListener)
     */
    public final void addSessionStatusListener(SessionStatusListener sessionStatusListener) {
        sessionStatusListeners.add(sessionStatusListener);
    }

    /**
     * Removes a previously added connection listener.
     *
     * @param sessionStatusListener The session listener.
     * @see #addSessionStatusListener(SessionStatusListener)
     */
    public final void removeSessionStatusListener(SessionStatusListener sessionStatusListener) {
        sessionStatusListeners.remove(sessionStatusListener);
    }

    private void notifyStanzaListeners(Stanza element, final boolean incoming) {

        if (element instanceof Message) {
            MessageEvent messageEvent = new MessageEvent(this, (Message) element, incoming);
            for (MessageListener messageListener : messageListeners) {
                try {
                    messageListener.handleMessage(messageEvent);
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else if (element instanceof Presence) {
            PresenceEvent presenceEvent = new PresenceEvent(this, (Presence) element, incoming);
            for (PresenceListener presenceListener : presenceListeners) {
                try {
                    presenceListener.handlePresence(presenceEvent);
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else if (element instanceof IQ) {
            final IQ iq = (IQ) element;

            if (incoming) {
                if (iq.getType() == null) {
                    // return <bad-request/> if the <iq/> has no type.
                    send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.BAD_REQUEST));
                } else if (iq.isRequest()) {
                    Object payload = iq.getExtension(Object.class);
                    if (payload == null) {
                        // return <bad-request/> if the <iq/> has no payload.
                        send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.BAD_REQUEST));
                    } else {
                        final IQHandler iqHandler = iqHandlerMap.get(payload.getClass());
                        if (iqHandler != null) {
                            iqHandlerExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        IQ response = iqHandler.handleRequest(iq);
                                        if (response != null) {
                                            send(response);
                                        }
                                    } catch (Exception e) {
                                        // If any exception occurs during processing the IQ, return <service-unavailable/>.
                                        send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.SERVICE_UNAVAILABLE));
                                    }
                                }
                            });
                        } else {
                            // return <service-unavailable/> if the <iq/> is not understood.
                            send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.SERVICE_UNAVAILABLE));
                        }
                    }
                }
            }


            IQEvent iqEvent = new IQEvent(this, iq, incoming);
            for (IQListener iqListener : iqListeners) {
                try {
                    iqListener.handleIQ(iqEvent);
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    private void notifyConnectionListeners(Status status, Status oldStatus, Exception exception) {
        for (SessionStatusListener connectionListener : sessionStatusListeners) {
            try {
                connectionListener.sessionStatusChanged(new SessionStatusEvent(this, status, oldStatus, exception));
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Sends an {@code <iq/>} stanza and waits for the response.
     * <p>
     * This method blocks until a result was received or a timeout occurred.
     * </p>
     *
     * @param iq The {@code <iq/>} stanza, which must be of type {@linkplain rocks.xmpp.core.stanza.model.AbstractIQ.Type#GET get} or {@linkplain rocks.xmpp.core.stanza.model.AbstractIQ.Type#SET set}.
     * @return The result {@code <iq/>} stanza.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws NoResponseException                          If the entity did not respond.
     */
    public IQ query(IQ iq) throws XmppException {
        return query(iq, configuration.getDefaultResponseTimeout());
    }

    /**
     * Sends an {@code <iq/>} stanza and waits for the response.
     * <p>
     * This method blocks until a result was received or a timeout occurred.
     * </p>
     *
     * @param iq      The {@code <iq/>} stanza, which must be of type {@linkplain rocks.xmpp.core.stanza.model.AbstractIQ.Type#GET get} or {@linkplain rocks.xmpp.core.stanza.model.AbstractIQ.Type#SET set}.
     * @param timeout The timeout.
     * @return The result {@code <iq/>} stanza.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws NoResponseException                          If the entity did not respond.
     */
    public IQ query(final IQ iq, long timeout) throws XmppException {
        if (!iq.isRequest()) {
            throw new IllegalArgumentException("IQ must be of type 'get' or 'set'");
        }

        final IQ[] result = new IQ[1];
        final Lock queryLock = new ReentrantLock();
        final Condition resultReceived = queryLock.newCondition();

        final IQListener listener = new IQListener() {
            @Override
            public void handleIQ(IQEvent e) {
                IQ responseIQ = e.getIQ();
                if (e.isIncoming() && responseIQ.isResponse() && responseIQ.getId() != null && responseIQ.getId().equals(iq.getId())) {
                    queryLock.lock();
                    try {
                        result[0] = responseIQ;
                    } finally {
                        resultReceived.signal();
                        queryLock.unlock();
                    }
                }
            }
        };

        queryLock.lock();
        try {
            addIQListener(listener);
            send(iq);
            // Wait for the stanza to arrive.
            if (!resultReceived.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on a response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            queryLock.unlock();
            removeIQListener(listener);
        }
        IQ response = result[0];
        if (response.getType() == IQ.Type.ERROR) {
            throw new StanzaException(response);
        }
        return result[0];
    }

    /**
     * Sends a stanza and then waits for a presence stanza to arrive. The filter determines the characteristics of the presence stanza.
     *
     * @param stanza The stanza, which is sent.
     * @param filter The presence filter.
     * @return The presence stanza.
     * @throws NoResponseException If no presence stanza has arrived in time.
     * @throws StanzaException     If the returned presence contains a stanza error.
     */
    public Presence sendAndAwaitPresence(ClientStreamElement stanza, final StanzaFilter<Presence> filter) throws NoResponseException, StanzaException {
        final Presence[] result = new Presence[1];
        final Lock presenceLock = new ReentrantLock();
        final Condition resultReceived = presenceLock.newCondition();

        final PresenceListener listener = new PresenceListener() {
            @Override
            public void handlePresence(PresenceEvent e) {
                Presence presence = e.getPresence();
                if (e.isIncoming() && filter.accept(presence)) {
                    presenceLock.lock();
                    try {
                        result[0] = presence;
                    } finally {
                        resultReceived.signal();
                        presenceLock.unlock();
                    }
                }
            }
        };

        presenceLock.lock();
        try {
            addPresenceListener(listener);
            send(stanza);
            // Wait for the stanza to arrive.
            if (!resultReceived.await(configuration.getDefaultResponseTimeout(), TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on a response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            presenceLock.unlock();
            removePresenceListener(listener);
        }
        Presence response = result[0];
        if (response.getType() == Presence.Type.ERROR) {
            throw new StanzaException(response);
        }
        return result[0];
    }

    /**
     * Sends a stanza and then waits for a message stanza to arrive. The filter determines the characteristics of the message stanza.
     *
     * @param stanza The stanza, which is sent.
     * @param filter The message filter.
     * @return The message stanza.
     * @throws NoResponseException If no message stanza has arrived in time.
     * @throws StanzaException     If the returned message contains a stanza error.
     */
    public Message sendAndAwaitMessage(ClientStreamElement stanza, final StanzaFilter<Message> filter) throws NoResponseException, StanzaException {

        final Message[] result = new Message[1];
        final Lock messageLock = new ReentrantLock();
        final Condition resultReceived = messageLock.newCondition();

        final MessageListener listener = new MessageListener() {
            @Override
            public void handleMessage(MessageEvent e) {
                Message message = e.getMessage();
                if (e.isIncoming() && filter.accept(message)) {
                    messageLock.lock();
                    try {
                        result[0] = message;
                    } finally {
                        resultReceived.signal();
                        messageLock.unlock();
                    }
                }
            }
        };

        messageLock.lock();
        try {
            addMessageListener(listener);
            send(stanza);
            // Wait for the stanza to arrive.
            if (!resultReceived.await(configuration.getDefaultResponseTimeout(), TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on a response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            messageLock.unlock();
            removeMessageListener(listener);
        }
        Message response = result[0];
        if (response.getType() == Message.Type.ERROR) {
            throw new StanzaException(response);
        }
        return response;
    }

    /**
     * Reconnects to the XMPP server and automatically logs in by using the last known information (e.g. user name, password and bound resource).
     *
     * @throws IOException   If an exception occurred while connecting.
     * @throws XmppException If an exception occurred during login.
     */
    public final synchronized void reconnect() throws IOException, XmppException {
        if (status == Status.DISCONNECTED) {
            connect();
            if (wasLoggedIn) {
                try {
                    updateStatus(Status.AUTHENTICATING);
                    authenticationManager.reAuthenticate();
                    bindResource(resource);
                } catch (AuthenticationException e) {
                    updateStatus(Status.DISCONNECTED);
                    throw e;
                }
                updateStatus(Status.AUTHENTICATED);
            }
        }
    }

    /**
     * Connects to the XMPP server.
     *
     * @throws IOException If anything went wrong, e.g. the host was not found.
     */
    public synchronized void connect() throws IOException {
        connect(null);
    }

    /**
     * Connects to the XMPP server.
     *
     * @param from The 'from' attribute.
     * @throws IOException If anything went wrong, e.g. the host was not found.
     */
    public synchronized void connect(Jid from) throws IOException {
        if (status == Status.CLOSED) {
            throw new IllegalStateException("Session is already closed. Create a new one.");
        }
        if (status != Status.INITIAL && status != Status.DISCONNECTED) {
            throw new IllegalStateException("Already connected.");
        }

        Status oldStatus = status;

        // Should either be INITIAL or DISCONNECTED
        Status previousStatus = status;
        updateStatus(Status.CONNECTING);
        // Reset
        exception = null;

        Iterator<Connection> connectionIterator = connections.iterator();
        while (connectionIterator.hasNext()) {
            Connection connection = connectionIterator.next();
            try {
                connection.connect(from);
                activeConnection = connection;
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
        if (exception != null) {
            updateStatus(oldStatus);
            throw new IOException(exception);
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

        stanzaListenerExecutor.shutdown();
        iqHandlerExecutor.shutdown();

        updateStatus(Status.CLOSED);
    }

    /**
     * Sends an XML element to the server, usually a stanza, i.e. a message, presence or IQ.
     *
     * @param element The XML element.
     */
    public void send(ClientStreamElement element) {

        if (!isConnected() && status != Status.CONNECTING) {
            throw new IllegalStateException(String.format("Session is not connected to server"));
        }
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
     * @throws AuthenticationException If the login failed, due to a SASL error reported by the server.
     * @throws XmppException           If the login failed, due to a XMPP-level error.
     */
    public final void login(String user, String password) throws XmppException {
        login(user, password, null);
    }

    /**
     * Authenticates against the server with username/password credential and binds a resource.
     *
     * @param user     The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password The password. Must not be null.
     * @param resource The resource. If null or empty, the resource is randomly assigned by the server.
     * @throws AuthenticationException If the login failed, due to a SASL error reported by the server.
     */
    public final void login(String user, String password, String resource) throws XmppException {
        login(null, user, password, resource);
    }

    /**
     * Authenticates against the server with an authorization id and username/password credential and binds a resource.
     *
     * @param authorizationId The authorization id.
     * @param user            The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password        The password. Must not be null.
     * @param resource        The resource. If null or empty, the resource is randomly assigned by the server.
     * @throws AuthenticationException If the login failed, due to a SASL error reported by the server.
     */
    public final void login(String authorizationId, final String user, final String password, String resource) throws XmppException {
        Objects.requireNonNull(user, "user must not be null.");
        Objects.requireNonNull(password, "password must not be null.");

        // A default callback handler for username/password retrieval:
        login(authorizationId, new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(user);
                    }
                    if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(password.toCharArray());
                    }
                    if (callback instanceof RealmCallback) {
                        ((RealmCallback) callback).setText(((RealmCallback) callback).getDefaultText());
                    }
                }
            }
        }, resource);
    }

    /**
     * Authenticates against the server with a custom callback handler and binds a resource.
     *
     * @param authorizationId The authorization id.
     * @param callbackHandler The callback handler.
     * @param resource        The resource. If null or empty, the resource is randomly assigned by the server.
     * @throws AuthenticationException If the login failed, due to a SASL error reported by the server.             If the login failed, due to a SASL error reported by the server.
     */
    public final void login(String authorizationId, CallbackHandler callbackHandler, String resource) throws XmppException {
        loginInternal(null, authorizationId, callbackHandler, resource);
    }

    /**
     * Logs in anonymously and binds a resource.
     *
     * @throws AuthenticationException If the login failed, due to a SASL error reported by the server. If the anonymous login failed.
     */
    public final void loginAnonymously() throws XmppException {
        loginInternal(new String[]{"ANONYMOUS"}, null, null, null);
    }

    private synchronized void loginInternal(String[] mechanisms, String authorizationId, CallbackHandler callbackHandler, String resource) throws XmppException {
        if (getStatus() == Status.AUTHENTICATED) {
            throw new IllegalStateException("You are already logged in.");
        }
        if (getStatus() != Status.CONNECTED) {
            throw new IllegalStateException("You must be connected to the server before trying to login.");
        }
        if (getDomain() == null) {
            throw new IllegalStateException("The XMPP domain must not be null.");
        }
        exception = null;
        try {
            updateStatus(Status.AUTHENTICATING);
            if (callbackHandler == null) {
                authenticationManager.authenticate(mechanisms, null, null);
            } else {
                authenticationManager.authenticate(mechanisms, authorizationId, callbackHandler);
            }
            bindResource(resource);

            if (callbackHandler != null && getRosterManager().isRetrieveRosterOnLogin()) {
                getRosterManager().requestRoster();
            }
        } catch (AuthenticationException e) {
            // Revert status
            updateStatus(Status.CONNECTED);
            throw e;
        } catch (Exception e) {
            // Revert status
            updateStatus(Status.CONNECTED);
            if (exception != null) {
                Throwable ex = e;
                while (ex.getCause() != null) {
                    ex = e.getCause();
                }
                ex.initCause(exception);
            }
            throwExceptionIfNotNull(e);
        }
        updateStatus(Status.AUTHENTICATED);
    }

    /**
     * Binds a resource to the connection.
     *
     * @param resource The resource to bind. If the resource is null and random resource is bound by the server.
     */
    private void bindResource(String resource) throws XmppException {
        this.resource = resource;

        // Then wait until the bind feature is received, if it hasn't yet.
        if (!streamFeaturesManager.getFeatures().containsKey(Bind.class)) {
            lock.lock();
            try {
                // Double check Bind feature. Theoretically it could be put in the features list after checking for the first time, which would lead to a dead lock here.
                if (!streamFeaturesManager.getFeatures().containsKey(Bind.class) && !streamNegotiatedUntilResourceBinding.await(5, TimeUnit.SECONDS)) {
                    throw new NoResponseException("Timeout reached during resource binding.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }

        // Check if stream feature negotiation failed with an exception.
        throwExceptionIfNotNull(exception);

        // Bind the resource
        IQ iq = new IQ(IQ.Type.SET, new Bind(this.resource));
        IQ result = query(iq);

        Bind bindResult = result.getExtension(Bind.class);
        this.connectedResource = bindResult.getJid();

        // Deprecated method of session binding, according to the <a href="http://xmpp.org/rfcs/rfc3921.html#session">old specification</a>
        // This is no longer used, according to the <a href="http://xmpp.org/rfcs/rfc6120.html">updated specification</a>.
        // But some old server implementation still require it.
        Session session = (Session) streamFeaturesManager.getFeatures().get(Session.class);
        if (session != null && session.isMandatory()) {
            query(new IQ(IQ.Type.SET, new Session()));
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
     * @throws rocks.xmpp.core.stream.model.StreamException If the element is a stream error.
     * @throws Exception                                    If any exception occurred during feature negotiation.
     */
    public final boolean handleElement(final Object element) throws Exception {

        if (element instanceof Stanza) {
            stanzaListenerExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    notifyStanzaListeners((Stanza) element, true);
                }
            });
        } else if (element instanceof StreamFeatures) {
            streamFeaturesManager.processFeatures((StreamFeatures) element);
        } else if (element instanceof StreamError) {
            throw new StreamException((StreamError) element);
        } else {
            // Let's see, if the element is known to any feature negotiator.
            return streamFeaturesManager.processElement(element);
        }
        return false;
    }

    /**
     * Called if any unhandled exception is thrown during reading or writing.
     * <p>
     * This method will close the stream.
     * </p>
     *
     * @param e The exception. If an unrecoverable XMPP stream error occurred, the exception is a {@link rocks.xmpp.core.stream.model.StreamError}.
     */
    public final void notifyException(Exception e) {
        // If the exception occurred during stream negotiation, i.e. before the connect() method has finished, the exception will be thrown.
        exception = e;
        // Release a potential waiting thread.
        lock.lock();
        try {
            streamNegotiatedUntilSasl.signalAll();
            streamNegotiatedUntilResourceBinding.signalAll();
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
                        instances.put(clazz, instance);
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return instance;
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
    public final StreamFeaturesManager getStreamFeaturesManager() {
        return streamFeaturesManager;
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

    void updateStatus(Status status) {
        updateStatus(status, null);
    }

    /**
     * Updates the status and notifies the connection listeners.
     *
     * @param status The new status.
     * @param e      The exception.
     */
    final void updateStatus(Status status, Exception e) {
        if (this.status != status) {
            Status oldStatus = this.status;
            this.status = status;
            notifyConnectionListeners(status, oldStatus, e);
        }
        if (status == Status.CLOSED) {
            sessionStatusListeners.clear();
        }
        if (status == Status.AUTHENTICATED) {
            wasLoggedIn = true;
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
    private void waitUntilSaslNegotiationStarted() throws NoResponseException, IOException {
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
     * Gets the default timeout for synchronous operations.
     *
     * @return The default timeout.
     * @deprecated Use {@link XmppSessionConfiguration#getDefaultResponseTimeout()}.
     */
    @Deprecated
    public final int getDefaultTimeout() {
        return configuration.getDefaultResponseTimeout();
    }

    /**
     * Indicates, whether the session is connected.
     *
     * @return True, if the status is {@link Status#CONNECTED}, {@link Status#AUTHENTICATED} or {@link Status#AUTHENTICATING}.
     * @see #getStatus()
     */
    public final boolean isConnected() {
        return status == Status.CONNECTED || status == Status.AUTHENTICATED || status == Status.AUTHENTICATING;
    }

    /**
     * Gets the configuration for this session.
     *
     * @return The configuration.
     */
    public final XmppSessionConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Gets the debugger.
     *
     * @return The debugger.
     */
    public final XmppDebugger getDebugger() {
        return debugger;
    }

    /**
     * Represents the connection status.
     */
    public enum Status {
        /**
         * The session is initial state.
         */
        INITIAL,
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
         * The session has been temporarily disconnected by an exception.
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
