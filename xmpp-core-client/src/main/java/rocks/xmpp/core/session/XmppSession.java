/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.roster.RosterManager;
import rocks.xmpp.core.sasl.AuthenticationException;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.session.model.Session;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.stream.StreamErrorException;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.subscription.PresenceManager;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.sasl.RealmCallback;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for establishing an XMPP session with a server.
 * <h3>Establishing an XMPP Session</h3>
 * The following example shows the most simple way to establish a session:
 * <pre>
 * {@code
 * XmppSession xmppSession = new XmppSession("domain");
 * xmppSession.connect();
 * xmppSession.login("username", "password");
 * }
 * </pre>
 * By default, the session will try to establish a TCP connection over port 5222 and will try BOSH as fallback.
 * You can configure a session and its connection methods by passing appropriate configurations in its constructor.
 * <h3>Sending Messages</h3>
 * Once connected, you can send messages:
 * <pre>
 * {@code
 * xmppSession.send(new Message(Jid.valueOf("juliet@example.net"), Message.Type.CHAT));
 * }
 * </pre>
 * <h3>Closing the Session</h3>
 * <pre>
 * {@code
 * xmppSession.close();
 * }
 * </pre>
 * <h3>Listening for Messages and Presence</h3>
 * <b>Note:</b> Adding the following listeners should be added before logging in, otherwise they might not trigger.
 * <pre>
 * {@code
 * // Listen for messages
 * xmppSession.addInboundMessageListener(e ->
 *     // Handle inbound message.
 * );
 *
 * // Listen for presence changes
 * xmppSession.addInboundPresenceListener(e ->
 *     // Handle inbound presence.
 * );
 * }
 * </pre>
 * This class is thread-safe, which means you can safely add listeners or call <code>send()</code>, <code>close()</code> (and other methods) from different threads.
 *
 * @author Christian Schudt
 * @see rocks.xmpp.core.session.XmppSessionConfiguration
 * @see rocks.xmpp.core.session.TcpConnectionConfiguration
 * @see rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration
 */
public class XmppSession implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(XmppSession.class.getName());

    private final AuthenticationManager authenticationManager;

    private final Set<Consumer<MessageEvent>> inboundMessageListeners = new CopyOnWriteArraySet<>();

    private final Set<Consumer<MessageEvent>> outboundMessageListeners = new CopyOnWriteArraySet<>();

    private final Set<Consumer<PresenceEvent>> inboundPresenceListeners = new CopyOnWriteArraySet<>();

    private final Set<Consumer<PresenceEvent>> outboundPresenceListeners = new CopyOnWriteArraySet<>();

    private final Set<Consumer<IQEvent>> inboundIQListeners = new CopyOnWriteArraySet<>();

    private final Set<Consumer<IQEvent>> outboundIQListeners = new CopyOnWriteArraySet<>();

    private final Map<Class<?>, IQHandler> iqHandlerMap = new HashMap<>();

    private final Map<Class<?>, Boolean> iqHandlerInvocationModes = new HashMap<>();

    private final Set<Consumer<SessionStatusEvent>> sessionStatusListeners = new CopyOnWriteArraySet<>();

    private final Map<Class<? extends Manager>, Manager> instances = new ConcurrentHashMap<>();

    private final List<Connection> connections = new ArrayList<>();

    private final XmppSessionConfiguration configuration;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    ExecutorService iqHandlerExecutor;

    ExecutorService stanzaListenerExecutor;

    /**
     * The user, which is assigned by the server after resource binding.
     */
    volatile Jid connectedResource;

    volatile Connection activeConnection;

    /**
     * The XMPP domain which will be assigned by the server's response. This is read by different threads, so make it volatile to ensure visibility of the written value.
     */
    private volatile String xmppServiceDomain;

    /**
     * Holds the connection state.
     * Guarded by "this".
     */
    private Status status = Status.INITIAL;

    /**
     * The resource, which the user requested during resource binding. This value is stored, so that it can be reused during reconnection.
     */
    private volatile String resource;

    /**
     * Any exception that occurred during stream negotiation ({@link #connect()}) or ({@link #login(String, String)})
     */
    private volatile Throwable exception;

    /**
     * The shutdown hook for JVM shutdown, which will disconnect each open connection before the JVM is halted.
     */
    private volatile Thread shutdownHook;

    private volatile XmppDebugger debugger;

    private volatile boolean wasLoggedIn;

    private volatile String lastAuthorizationId;

    private volatile Collection<String> lastMechanisms;

    private volatile CallbackHandler lastCallbackHandler;

    private volatile boolean anonymous;

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
        this.stanzaListenerExecutor = Executors.newSingleThreadExecutor(XmppUtils.createNamedThreadFactory("Stanza Listener Thread"));
        this.iqHandlerExecutor = Executors.newCachedThreadPool(XmppUtils.createNamedThreadFactory("IQ Handler Thread"));
        this.serviceDiscoveryManager = getManager(ServiceDiscoveryManager.class);

        // Add a shutdown hook, which will gracefully close the connection, when the JVM is halted.
        shutdownHook = new Thread() {
            @Override
            public void run() {
                shutdownHook = null;
                try {
                    close();
                } catch (XmppException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        StreamFeaturesManager streamFeaturesManager = getManager(StreamFeaturesManager.class);

        authenticationManager = new AuthenticationManager(this);

        streamFeaturesManager.addFeatureNegotiator(authenticationManager);
        streamFeaturesManager.addFeatureNegotiator(new StreamFeatureNegotiator(this, Bind.class) {
            @Override
            public Status processNegotiation(Object element) throws StreamNegotiationException {
                // Resource binding will be negotiated manually
                return Status.INCOMPLETE;
            }

            @Override
            public boolean canProcess(Object element) {
                return false;
            }
        });


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
            Arrays.stream(connectionConfigurations).map(connectionConfiguration -> connectionConfiguration.createConnection(this)).forEach(connections::add);
        }

        configuration.getExtensions().forEach(serviceDiscoveryManager::registerFeature);
    }

    private static void throwAsXmppExceptionIfNotNull(Throwable e) throws XmppException {
        if (e != null) {
            if (e instanceof XmppException) {
                throw (XmppException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            } else {
                throw new XmppException(e);
            }
        }
    }

    /**
     * Gets the actively used connection.
     *
     * @return The actively used connection.
     */
    public final Connection getActiveConnection() {
        return activeConnection;
    }

    /**
     * Sets the XMPP service domain. This should only be set by a connection implementation.
     *
     * @param xmppServiceDomain The XMPP service domain.
     */
    public final void setXmppServiceDomain(String xmppServiceDomain) {
        this.xmppServiceDomain = xmppServiceDomain;
    }

    /**
     * Adds an inbound message listener to the session, which will get notified, whenever a message is received.
     *
     * @param messageListener The message listener.
     * @see #removeInboundMessageListener(Consumer)
     */
    public final void addInboundMessageListener(Consumer<MessageEvent> messageListener) {
        inboundMessageListeners.add(messageListener);
    }

    /**
     * Removes a previously added inbound message listener from the session.
     *
     * @param messageListener The message listener.
     * @see #addInboundMessageListener(Consumer)
     */
    public final void removeInboundMessageListener(Consumer<MessageEvent> messageListener) {
        inboundMessageListeners.remove(messageListener);
    }

    /**
     * Adds an outbound message listener to the session, which will get notified, whenever a message is sent.
     *
     * @param messageListener The message listener.
     * @see #removeOutboundMessageListener(Consumer)
     */
    public final void addOutboundMessageListener(Consumer<MessageEvent> messageListener) {
        outboundMessageListeners.add(messageListener);
    }

    /**
     * Removes a previously added outbound message listener from the session.
     *
     * @param messageListener The message listener.
     * @see #addOutboundMessageListener(Consumer)
     */
    public final void removeOutboundMessageListener(Consumer<MessageEvent> messageListener) {
        outboundMessageListeners.remove(messageListener);
    }

    /**
     * Adds an inbound presence listener to the session, which will get notified, whenever a presence is received.
     *
     * @param presenceListener The presence listener.
     * @see #removeInboundPresenceListener(Consumer)
     */
    public final void addInboundPresenceListener(Consumer<PresenceEvent> presenceListener) {
        inboundPresenceListeners.add(presenceListener);
    }

    /**
     * Removes a previously added inbound presence listener from the session.
     *
     * @param presenceListener The presence listener.
     * @see #addInboundPresenceListener(Consumer)
     */
    public final void removeInboundPresenceListener(Consumer<PresenceEvent> presenceListener) {
        inboundPresenceListeners.remove(presenceListener);
    }

    /**
     * Adds an outbound presence listener to the session, which will get notified, whenever a presence is sent.
     *
     * @param presenceListener The presence listener.
     * @see #removeOutboundPresenceListener(Consumer)
     */
    public final void addOutboundPresenceListener(Consumer<PresenceEvent> presenceListener) {
        outboundPresenceListeners.add(presenceListener);
    }

    /**
     * Removes a previously added outbound presence listener from the session.
     *
     * @param presenceListener The presence listener.
     * @see #addOutboundPresenceListener(Consumer)
     */
    public final void removeOutboundPresenceListener(Consumer<PresenceEvent> presenceListener) {
        outboundPresenceListeners.remove(presenceListener);
    }

    /**
     * Adds an inbound IQ listener to the session, which will get notified, whenever an IQ stanza is received.
     *
     * @param iqListener The IQ listener.
     * @see #removeInboundIQListener(Consumer)
     */
    public final void addInboundIQListener(Consumer<IQEvent> iqListener) {
        inboundIQListeners.add(iqListener);
    }

    /**
     * Removes a previously added inbound IQ listener from the session.
     *
     * @param iqListener The IQ listener.
     * @see #addInboundIQListener(Consumer)
     */
    public final void removeInboundIQListener(Consumer<IQEvent> iqListener) {
        inboundIQListeners.remove(iqListener);
    }

    /**
     * Adds an outbound IQ listener to the session, which will get notified, whenever an IQ stanza is sent.
     *
     * @param iqListener The IQ listener.
     * @see #removeOutboundIQListener(Consumer)
     */
    public final void addOutboundIQListener(Consumer<IQEvent> iqListener) {
        outboundIQListeners.add(iqListener);
    }

    /**
     * Removes a previously added outbound IQ listener from the session.
     *
     * @param iqListener The IQ listener.
     * @see #addOutboundIQListener(Consumer)
     */
    public final void removeOutboundIQListener(Consumer<IQEvent> iqListener) {
        outboundIQListeners.remove(iqListener);
    }

    /**
     * Adds an IQ handler for a given payload type. The handler will be processed asynchronously, which means it won't block the inbound stanza processing queue.
     *
     * @param type      The payload type.
     * @param iqHandler The IQ handler.
     * @see #removeIQHandler(Class)
     * @see #addIQHandler(Class, rocks.xmpp.core.stanza.IQHandler, boolean)
     */
    public final void addIQHandler(Class<?> type, IQHandler iqHandler) {
        addIQHandler(type, iqHandler, true);
    }

    /**
     * Adds an IQ handler for a given payload type. The handler can either be processed asynchronously (which means it won't block the inbound stanza processing queue),
     * or synchronously, which means IQ requests are processed on the same thread as other stanzas.
     * In other words synchronous processing means, the IQ requests are processed in the same order as they arrive and no other stanzas can be
     * processed until the handler has returned.
     *
     * @param type        The payload type.
     * @param iqHandler   The IQ handler.
     * @param invokeAsync True, if the handler should be processed asynchronously; false, if the handler should be processed asynchronously.
     * @see #removeIQHandler(Class)
     */
    public final void addIQHandler(Class<?> type, IQHandler iqHandler, boolean invokeAsync) {
        synchronized (iqHandlerMap) {
            iqHandlerMap.put(type, iqHandler);
            iqHandlerInvocationModes.put(type, invokeAsync);
        }
    }

    /**
     * Removes an IQ handler.
     *
     * @param type The payload type.
     * @see #addIQHandler(Class, rocks.xmpp.core.stanza.IQHandler)
     */
    public final void removeIQHandler(Class<?> type) {
        synchronized (iqHandlerMap) {
            iqHandlerMap.remove(type);
            iqHandlerInvocationModes.remove(type);
        }
    }

    /**
     * Adds a session listener, which listens for session status changes.
     * Each time the {@linkplain Status session status} changes, all listeners will be notified.
     *
     * @param sessionStatusListener The session listener.
     * @see #removeSessionStatusListener(Consumer)
     */
    public final void addSessionStatusListener(Consumer<SessionStatusEvent> sessionStatusListener) {
        sessionStatusListeners.add(sessionStatusListener);
    }

    /**
     * Removes a previously added session listener.
     *
     * @param sessionStatusListener The session listener.
     * @see #addSessionStatusListener(Consumer)
     */
    public final void removeSessionStatusListener(Consumer<SessionStatusEvent> sessionStatusListener) {
        sessionStatusListeners.remove(sessionStatusListener);
    }

    /**
     * Sends an {@code <iq/>} stanza and waits for the response.
     * <p>
     * This method blocks until a result was received or a timeout occurred.
     * </p>
     *
     * @param iq The {@code <iq/>} stanza, which must be of type {@linkplain rocks.xmpp.core.stanza.model.AbstractIQ.Type#GET get} or {@linkplain rocks.xmpp.core.stanza.model.AbstractIQ.Type#SET set}.
     * @return The result {@code <iq/>} stanza.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public AbstractIQ query(AbstractIQ iq) throws XmppException {
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
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public AbstractIQ query(final AbstractIQ iq, long timeout) throws XmppException {
        if (!iq.isRequest()) {
            throw new IllegalArgumentException("IQ must be of type 'get' or 'set'");
        }

        final AbstractIQ[] result = new AbstractIQ[1];
        final Lock queryLock = new ReentrantLock();
        final Condition resultReceived = queryLock.newCondition();

        final Consumer<IQEvent> listener = e -> {
            AbstractIQ responseIQ = e.getIQ();
            if (responseIQ.isResponse() && responseIQ.getId() != null && responseIQ.getId().equals(iq.getId())) {
                queryLock.lock();
                try {
                    result[0] = responseIQ;
                } finally {
                    resultReceived.signal();
                    queryLock.unlock();
                }
            }
        };

        queryLock.lock();
        try {
            addInboundIQListener(listener);
            send(iq);
            // Wait for the stanza to arrive.
            if (!resultReceived.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on a response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new XmppException("Thread is interrupted.", e);
        } finally {
            queryLock.unlock();
            removeInboundIQListener(listener);
        }
        AbstractIQ response = result[0];
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
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public final Presence sendAndAwaitPresence(StreamElement stanza, final Predicate<Presence> filter) throws XmppException {
        final Presence[] result = new Presence[1];
        final Lock presenceLock = new ReentrantLock();
        final Condition resultReceived = presenceLock.newCondition();

        final Consumer<PresenceEvent> listener = e -> {
            Presence presence = e.getPresence();
            if (filter.test(presence)) {
                presenceLock.lock();
                try {
                    result[0] = presence;
                } finally {
                    resultReceived.signal();
                    presenceLock.unlock();
                }
            }
        };

        presenceLock.lock();
        try {
            addInboundPresenceListener(listener);
            send(stanza);
            // Wait for the stanza to arrive.
            if (!resultReceived.await(configuration.getDefaultResponseTimeout(), TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on a response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new XmppException("Thread is interrupted.", e);
        } finally {
            presenceLock.unlock();
            removeInboundPresenceListener(listener);
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
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public final Message sendAndAwaitMessage(StreamElement stanza, final Predicate<Message> filter) throws XmppException {

        final Message[] result = new Message[1];
        final Lock messageLock = new ReentrantLock();
        final Condition resultReceived = messageLock.newCondition();

        final Consumer<MessageEvent> listener = e -> {
            Message message = e.getMessage();
            if (filter.test(message)) {
                messageLock.lock();
                try {
                    result[0] = message;
                } finally {
                    resultReceived.signal();
                    messageLock.unlock();
                }
            }
        };

        messageLock.lock();
        try {
            addInboundMessageListener(listener);
            send(stanza);
            // Wait for the stanza to arrive.
            if (!resultReceived.await(configuration.getDefaultResponseTimeout(), TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("Timeout reached, while waiting on a response.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new XmppException("Thread is interrupted.", e);
        } finally {
            messageLock.unlock();
            removeInboundMessageListener(listener);
        }
        Message response = result[0];
        if (response.getType() == Message.Type.ERROR) {
            throw new StanzaException(response);
        }
        return response;
    }

    /**
     * Connects to the XMPP server.
     *
     * @throws ConnectionException        If a connection error occurred on the transport layer, e.g. the socket could not connect.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws XmppException              If any other XMPP exception occurs.
     * @throws IllegalStateException      If the session is in a wrong state, e.g. closed or already connected.
     */
    public final void connect() throws XmppException {
        connect(null);
    }

    /**
     * Connects to the XMPP server.
     *
     * @param from The 'from' attribute.
     * @throws ConnectionException        If a connection error occurred on the transport layer, e.g. the socket could not connect.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws XmppException              If any other XMPP exception occurs.
     * @throws IllegalStateException      If the session is in a wrong state, e.g. closed or already connected.
     */
    public final void connect(Jid from) throws XmppException {

        Status previousStatus = getStatus();

        if (previousStatus == Status.CLOSED) {
            throw new IllegalStateException("Session is already closed. Create a new one.");
        }

        if (isConnected() || !updateStatus(Status.CONNECTING)) {
            // Silently return, when we are already connected or connecting.
            logger.fine("Already connected. Return silently.");
            return;
        }
        // Reset
        exception = null;

        try {
            Iterator<Connection> connectionIterator = connections.iterator();
            while (connectionIterator.hasNext()) {
                Connection connection = connectionIterator.next();
                try {
                    connection.connect(from);
                    activeConnection = connection;
                    break;
                } catch (IOException e) {
                    if (connectionIterator.hasNext()) {
                        logger.log(Level.WARNING, "{0} failed to connect. Trying alternative connection.", connection);
                        logger.log(Level.FINE, e.getMessage(), e);
                    } else {
                        throw new ConnectionException(e);
                    }
                }
            }

            logger.log(Level.FINE, "Connected via {0}", activeConnection);
            logger.fine("Negotiating stream, waiting until SASL is ready to be negotiated.");

            // Wait until the reader thread signals, that we are connected. That is after TLS negotiation and before SASL negotiation.
            try {
                getManager(StreamFeaturesManager.class).awaitNegotiation(Mechanisms.class, 10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }

            // Check if stream negotiation threw any exception.
            throwAsXmppExceptionIfNotNull(exception);

            logger.fine("Stream negotiated until SASL, now ready to login.");
            updateStatus(Status.CONNECTED);

            // This is for reconnection.
            if (wasLoggedIn) {
                logger.fine("Was already logged in. Re-login automatically with known credentials.");
                login(lastMechanisms, lastAuthorizationId, lastCallbackHandler, resource);
            }
        } catch (Throwable e) {
            try {
                if (activeConnection != null) {
                    activeConnection.close();
                    activeConnection = null;
                }
            } catch (Exception e1) {
                e.addSuppressed(e1);
            }
            updateStatus(previousStatus, e);
            throwAsXmppExceptionIfNotNull(e);
        }
    }

    /**
     * Explicitly closes the session and performs a clean up of all listeners. Calling this method, if the session is already closing or closed has no effect.
     *
     * @throws XmppException If an exception occurs while closing the connection, e.g. the underlying socket connection.
     */
    @Override
    public void close() throws XmppException {
        if (getStatus() == Status.CLOSED || !updateStatus(Status.CLOSING)) {
            return;
        }
        // The following code should only be called once, no matter how many threads concurrently call this method.
        try {
            if (activeConnection != null) {
                activeConnection.close();
                activeConnection = null;
            }
        } catch (Exception e) {
            throwAsXmppExceptionIfNotNull(e);
        } finally {
            // Clear everything.
            inboundMessageListeners.clear();
            outboundMessageListeners.clear();
            inboundPresenceListeners.clear();
            outboundPresenceListeners.clear();
            inboundIQListeners.clear();
            outboundIQListeners.clear();
            stanzaListenerExecutor.shutdown();
            iqHandlerExecutor.shutdown();
            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            updateStatus(Status.CLOSED);
            sessionStatusListeners.clear();
        }
    }

    /**
     * Sends an XML element to the server, usually a stanza, i.e. a message, presence or IQ.
     *
     * @param element The XML element.
     */
    public void send(StreamElement element) {

        if (!isConnected() && getStatus() != Status.CONNECTING) {
            throw new IllegalStateException("Session is not connected to server");
        }
        if (element instanceof Stanza) {
            Stanza stanza = (Stanza) element;
            // If resource binding has not completed and it's tried to send a stanza which doesn't serve the purpose
            // of resource binding, throw an exception, because otherwise the server will terminate the connection with a stream error.
            // TODO: Consider queuing such stanzas and send them as soon as logged in instead of throwing exception.
            if (getStatus() != Status.AUTHENTICATED && stanza.getExtension(Bind.class) == null && !(stanza instanceof IQ && ((IQ) stanza).isResponse())) {
                throw new IllegalStateException("Cannot send stanzas before resource binding has completed.");
            }
            if (stanza instanceof Message) {
                XmppUtils.notifyEventListeners(outboundMessageListeners, new MessageEvent(this, (Message) stanza, false));
            } else if (stanza instanceof Presence) {
                XmppUtils.notifyEventListeners(outboundPresenceListeners, new PresenceEvent(this, (Presence) stanza, false));
            } else if (stanza instanceof IQ) {
                XmppUtils.notifyEventListeners(outboundIQListeners, new IQEvent(this, (IQ) stanza, false));
            }
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
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding or roster retrieval.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] login(String user, String password) throws XmppException {
        return login(user, password, null);
    }

    /**
     * Authenticates against the server with username/password credential and binds a resource.
     *
     * @param user     The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password The password. Must not be null.
     * @param resource The resource. If null or empty, the resource is randomly assigned by the server.
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding or roster retrieval.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] login(String user, String password, String resource) throws XmppException {
        return login(null, user, password, resource);
    }

    /**
     * Authenticates against the server with an authorization id and username/password credential and binds a resource.
     *
     * @param authorizationId The authorization id.
     * @param user            The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password        The password. Must not be null.
     * @param resource        The resource. If null or empty, the resource is randomly assigned by the server.
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding or roster retrieval.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] login(String authorizationId, final String user, final String password, String resource) throws XmppException {
        Objects.requireNonNull(user, "user must not be null.");
        Objects.requireNonNull(password, "password must not be null.");

        // A default callback handler for username/password retrieval:
        return login(authorizationId, callbacks -> Arrays.stream(callbacks).forEach(callback -> {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(user);
            }
            if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(password.toCharArray());
            }
            if (callback instanceof RealmCallback) {
                ((RealmCallback) callback).setText(((RealmCallback) callback).getDefaultText());
            }
        }), resource);
    }

    /**
     * Authenticates against the server with a custom callback handler and binds a resource.
     *
     * @param authorizationId The authorization id.
     * @param callbackHandler The callback handler.
     * @param resource        The resource. If null or empty, the resource is randomly assigned by the server.
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding or roster retrieval.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] login(String authorizationId, CallbackHandler callbackHandler, String resource) throws XmppException {
        return login(configuration.getAuthenticationMechanisms(), authorizationId, callbackHandler, resource);
    }

    /**
     * Logs in anonymously and binds a resource.
     *
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] loginAnonymously() throws XmppException {
        byte[] successData = login(Collections.singleton("ANONYMOUS"), null, null, null);
        anonymous = true;
        return successData;
    }

    private byte[] login(Collection<String> mechanisms, String authorizationId, CallbackHandler callbackHandler, String resource) throws XmppException {

        Status previousStatus = getStatus();

        if (previousStatus == Status.AUTHENTICATED || !updateStatus(Status.AUTHENTICATING)) {
            throw new IllegalStateException("You are already logged in.");
        }
        if (previousStatus != Status.CONNECTED) {
            throw new IllegalStateException("You must be connected to the server before trying to login.");
        }
        if (getDomain() == null) {
            throw new IllegalStateException("The XMPP domain must not be null.");
        }

        exception = null;

        lastMechanisms = mechanisms;
        lastAuthorizationId = authorizationId;
        lastCallbackHandler = callbackHandler;
        try {
            logger.fine("Starting SASL negotiation (authentication).");
            if (callbackHandler == null) {
                authenticationManager.startAuthentication(mechanisms, null, null);
            } else {
                authenticationManager.startAuthentication(mechanisms, authorizationId, callbackHandler);
            }
            StreamFeaturesManager streamFeaturesManager = getManager(StreamFeaturesManager.class);
            // Negotiate all pending features until <bind/> would be negotiated.
            streamFeaturesManager.awaitNegotiation(Bind.class, configuration.getDefaultResponseTimeout());

            // Check if stream feature negotiation failed with an exception.
            throwAsXmppExceptionIfNotNull(exception);

            // Then negotiate resource binding manually.
            bindResource(resource);

            // Proceed with any outstanding stream features which are negotiated after resource binding, e.g. XEP-0198
            // and wait until all features have been negotiated.
            streamFeaturesManager.completeNegotiation(configuration.getDefaultResponseTimeout());

            // Check again, if stream feature negotiation failed with an exception.
            throwAsXmppExceptionIfNotNull(exception);

            logger.fine("Stream negotiation completed successfully.");

            // Retrieve roster.
            RosterManager rosterManager = getManager(RosterManager.class);
            if (callbackHandler != null && rosterManager.isRetrieveRosterOnLogin()) {
                logger.fine("Retrieving roster on login (as per configuration).");
                rosterManager.requestRoster();
            }
            PresenceManager presenceManager = getManager(PresenceManager.class);
            if (presenceManager.getLastSentPresence() != null) {
                // After retrieving the roster, resend the last presence, if any (in reconnection case).
                presenceManager.getLastSentPresences().forEach(presence -> {
                    presence.getExtensions().clear();
                    send(presence);
                });
            } else if (configuration.getInitialPresence() != null) {
                // Or send initial presence
                Presence initialPresence = configuration.getInitialPresence().get();
                if (initialPresence != null) {
                    send(initialPresence);
                }
            }
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
        logger.fine("Login successful.");
        return authenticationManager.getSuccessData();
    }

    /**
     * Binds a resource to the session.
     *
     * @param resource The resource to bind. If the resource is null and random resource is bound by the server.
     */
    private void bindResource(String resource) throws XmppException {
        this.resource = resource;

        logger.log(Level.FINE, "Negotiating resource binding, resource: {0}.", resource);

        // Bind the resource
        IQ iq = new IQ(IQ.Type.SET, new Bind(this.resource));
        AbstractIQ result = query(iq);

        Bind bindResult = result.getExtension(Bind.class);
        this.connectedResource = bindResult.getJid();

        logger.log(Level.FINE, "Resource binding completed, connected resource: {0}.", connectedResource);

        // At this point the entity is free to send stanzas:
        // "If, before completing the resource binding step, the client attempts to send an XML stanza to an entity other
        // than the server itself or the client's account, the server MUST NOT process the stanza
        // and MUST close the stream with a <not-authorized/> stream error."
        updateStatus(Status.AUTHENTICATED);
        wasLoggedIn = true;

        // Deprecated method of session binding, according to the <a href="http://xmpp.org/rfcs/rfc3921.html#session">old specification</a>
        // This is no longer used, according to the <a href="http://xmpp.org/rfcs/rfc6120.html">updated specification</a>.
        // But some old server implementation still require it.
        Session session = (Session) getManager(StreamFeaturesManager.class).getFeatures().get(Session.class);
        if (session != null && session.isMandatory()) {
            logger.fine("Establishing session.");
            query(new IQ(IQ.Type.SET, new Session()));
        }
    }

    /**
     * Handles an XMPP element.
     * <p>
     * This method should be called on the reader thread.
     * </p>
     *
     * @param element The XMPP element.
     * @return True, if the stream needs to be restarted; otherwise false.
     * @throws StreamErrorException       If the element is a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws XmppException              If any other XMPP exception occurs.
     */
    public final boolean handleElement(final Object element) throws XmppException {

        if (element instanceof AbstractIQ) {
            final AbstractIQ iq = (AbstractIQ) element;

            if (iq.getType() == null) {
                // return <bad-request/> if the <iq/> has no type.
                send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.BAD_REQUEST));
            } else if (iq.isRequest()) {
                Object payload = iq.getExtension(Object.class);
                if (payload == null) {
                    // return <bad-request/> if the <iq/> has no payload.
                    send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.BAD_REQUEST));
                } else {
                    Executor executor;
                    final IQHandler iqHandler;
                    synchronized (iqHandlerMap) {
                        iqHandler = iqHandlerMap.get(payload.getClass());
                        // If the handler is to be invoked asynchronously, get the iqHandlerExecutor, otherwise use stanzaListenerExecutor (which is a single thread executor).
                        executor = iqHandler != null ? (iqHandlerInvocationModes.get(payload.getClass()) ? iqHandlerExecutor : stanzaListenerExecutor) : null;
                    }

                    if (iqHandler != null) {
                        executor.execute(() -> {
                            try {
                                AbstractIQ response = iqHandler.handleRequest(iq);
                                if (response != null) {
                                    send(response);
                                }
                            } catch (Exception e) {
                                logger.log(Level.WARNING, e, () -> "Failed to handle IQ request: " + e.getMessage());
                                // If any exception occurs during processing the IQ, return <service-unavailable/>.
                                send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.SERVICE_UNAVAILABLE));
                            }
                        });
                    } else {
                        // return <service-unavailable/> if the <iq/> is not understood.
                        send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.SERVICE_UNAVAILABLE));
                    }
                }
            }
            stanzaListenerExecutor.execute(() -> XmppUtils.notifyEventListeners(inboundIQListeners, new IQEvent(this, iq, true)));
        } else if (element instanceof Message) {
            stanzaListenerExecutor.execute(() -> XmppUtils.notifyEventListeners(inboundMessageListeners, new MessageEvent(this, (Message) element, true)));
        } else if (element instanceof Presence) {
            stanzaListenerExecutor.execute(() -> XmppUtils.notifyEventListeners(inboundPresenceListeners, new PresenceEvent(this, (Presence) element, true)));
        } else if (element instanceof StreamFeatures) {
            getManager(StreamFeaturesManager.class).processFeatures((StreamFeatures) element);
        } else if (element instanceof StreamError) {
            throw new StreamErrorException((StreamError) element);
        } else {
            // Let's see, if the element is known to any feature negotiator.
            return getManager(StreamFeaturesManager.class).processElement(element);
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
        exception = Objects.requireNonNull(e, "exception must not be null");
        // Release a potential waiting thread.
        getManager(StreamFeaturesManager.class).cancelNegotiation();

        if (EnumSet.of(Status.AUTHENTICATED, Status.AUTHENTICATING, Status.CONNECTED, Status.CONNECTING).contains(getStatus())) {

            try {
                activeConnection.close();
            } catch (Exception e1) {
                e.addSuppressed(e1);
            }
            if (updateStatus(Status.DISCONNECTED, e)) {
                logger.log(Level.FINE, "Session disconnected due to exception: ", e);
            }
        }
    }

    /**
     * Gets an instance of the specified manager class. The class MUST have a constructor which takes a single parameter, whose type is {@link rocks.xmpp.core.session.XmppSession}.
     *
     * @param clazz The class of the manager.
     * @param <T>   The type.
     * @return An instance of the specified manager.
     */
    @SuppressWarnings("unchecked")
    public final <T extends Manager> T getManager(Class<T> clazz) {
        return (T) instances.computeIfAbsent(clazz, key -> {
            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor(XmppSession.class);
                constructor.setAccessible(true);
                T instance = constructor.newInstance(this);
                instance.initialize();
                return instance;
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalArgumentException("Can't instantiate the provided class:" + clazz, e);
            }
        });
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
     * Gets the status of the session.
     *
     * @return The status.
     */
    public final synchronized Status getStatus() {
        return status;
    }

    /**
     * Updates the status and notifies the session listeners.
     *
     * @param status The new status.
     * @return True, if the status has changed; otherwise false.
     */
    final boolean updateStatus(Status status) {
        return updateStatus(status, null);
    }

    /**
     * Updates the status and notifies the session listeners.
     *
     * @param status The new status.
     * @param e      The exception.
     * @return True, if the status has changed; otherwise false.
     */
    final boolean updateStatus(Status status, Throwable e) {
        Status oldStatus;
        synchronized (this) {
            oldStatus = this.status;
            this.status = status;
        }
        if (status != oldStatus) {
            // Make sure to not call listeners from within synchronized region.
            XmppUtils.notifyEventListeners(sessionStatusListeners, new SessionStatusEvent(this, status, oldStatus, e));
        }
        return status != oldStatus;
    }

    /**
     * Gets an unmodifiable list of connections, which this session will try during connecting.
     *
     * @return The connections.
     */
    public final List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     * Creates a new unmarshaller, which can be used to unmarshal XML to objects.
     * <p>
     * Note that the returned unmarshaller is not thread-safe.
     *
     * @return The unmarshaller.
     * @see #createMarshaller()
     */
    public final Unmarshaller createUnmarshaller() {
        try {
            return configuration.getJAXBContext().createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a marshaller, which can be used to create XML from objects.
     * <p>
     * The returned marshaller is configured with {@code Marshaller.JAXB_FRAGMENT = true}, so that no XML header is written
     * (which is usually what we want in XMPP when writing stanzas).
     * <p>
     * Note that the returned unmarshaller is not thread-safe.
     *
     * @return The marshaller.
     * @see #createUnmarshaller()
     */
    public final Marshaller createMarshaller() {
        try {
            Marshaller marshaller = configuration.getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            return marshaller;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Indicates, whether the session is connected.
     *
     * @return True, if the status is {@link Status#CONNECTED}, {@link Status#AUTHENTICATED} or {@link Status#AUTHENTICATING}.
     * @see #getStatus()
     */
    public final boolean isConnected() {
        return EnumSet.of(Status.CONNECTED, Status.AUTHENTICATED, Status.AUTHENTICATING).contains(getStatus());
    }

    /**
     * Indicates whether the session has been logged in anonymously. If never logged in at all, returns false.
     *
     * @return True, if the session is anonymous.
     */
    public final boolean isAnonymous() {
        return anonymous;
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
     * Gets the debugger or null if no debugger class was specified in the configuration.
     *
     * @return The debugger.
     * @see XmppSessionConfiguration#getDebugger()
     */
    public final XmppDebugger getDebugger() {
        return debugger;
    }

    /**
     * Enables a feature by its name, usually a protocol namespace.
     *
     * @param name The associated manager class.
     */
    public final void enableFeature(String name) {
        serviceDiscoveryManager.addFeature(name);
    }

    /**
     * Disables a feature by its name, usually a protocol namespace.
     *
     * @param name The associated manager class.
     */
    public final void disableFeature(String name) {
        serviceDiscoveryManager.removeFeature(name);
    }

    /**
     * Enables a feature by its manager class.
     *
     * @param managerClass The associated manager class.
     */
    public final void enableFeature(Class<? extends Manager> managerClass) {
        serviceDiscoveryManager.addFeature(managerClass);
    }

    /**
     * Disables a feature by its manager class.
     *
     * @param managerClass The associated manager class.
     */
    public final void disableFeature(Class<? extends Manager> managerClass) {
        serviceDiscoveryManager.removeFeature(managerClass);
    }

    /**
     * Gets the enabled features.
     *
     * @return The enabled features.
     */
    public final Set<String> getEnabledFeatures() {
        return serviceDiscoveryManager.getFeatures();
    }

    /**
     * Determines support of another XMPP entity for a given feature.
     * <p>
     * Note that if you want to determine support of another client, you have to provide that client's full JID (user@domain/resource).
     * If you want to determine the server's capabilities provide only the domain JID of the server.
     * <p>
     * This method uses cached information and the presence based entity capabilities (XEP-0115) to determine support. Only if no information is available an explicit service discovery request is made.
     *
     * @param feature The feature, usually defined by an XMPP Extension Protocol, e.g. "urn:xmpp:ping".
     * @param jid     The XMPP entity.
     * @return True, if the XMPP entity supports the given feature; otherwise false.
     */
    public final boolean isSupported(String feature, Jid jid) throws XmppException {
        return getManager(EntityCapabilitiesManager.class).isSupported(feature, jid);
    }

    /**
     * Represents the session status.
     * <p>
     * The following chart illustrates the valid status transitions:
     * <pre>
     * &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500; INITIAL
     * &#x2502;          &#x2502;
     * &#x2502;          &#x25BC;
     * &#x251C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500; CONNECTING &#x25C4;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510;
     * &#x2502;          &#x2502;               &#x2502;
     * &#x2502;          &#x25BC;               &#x2502;
     * &#x251C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500; CONNECTED &#x2500;&#x2500;&#x2500;&#x25BA; DISCONNECTED
     * &#x2502;          &#x2502;               &#9650;
     * &#x2502;          &#x25BC;               &#x2502;
     * &#x251C;&#x2500;&#x2500;&#x2500; AUTHENTICATING        &#x2502;
     * &#x2502;          &#x2502;               &#x2502;
     * &#x2502;          &#x25BC;               &#x2502;
     * &#x251C;&#x2500;&#x2500;&#x2500; AUTHENTICATED &#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518;
     * &#x2502;          &#x2502;
     * &#x2502;          &#x25BC;
     * &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x25BA; CLOSING
     * &nbsp;          &#x2502;
     * &nbsp;          &#x25BC;
     * &nbsp;       CLOSED
     * </pre>
     */
    public enum Status {
        /**
         * The session is in initial state.
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
