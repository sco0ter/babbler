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

package rocks.xmpp.core.session;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.sasl.AuthenticationException;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.IQ.Type;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamErrorException;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;
import rocks.xmpp.extensions.sm.StreamManager;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;
import rocks.xmpp.util.concurrent.CompletionStages;

import javax.xml.bind.DataBindingException;
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
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for different kinds of XMPP sessions.
 * <p>
 * To date there are three kinds of sessions:
 * </p>
 * <ul>
 * <li>A normal client-to-server session. This is the default and most used XMPP session. It's concrete implementation is the {@link XmppClient}.</li>
 * <li>An external component session (<a href="http://xmpp.org/extensions/xep-0114.html">XEP-0114: Jabber Component Protocol</a>).</li>
 * <li>A client-to-client session (<a href="http://xmpp.org/extensions/xep-0174.html">XEP-0174: Serverless Messaging</a>) (no implementation yet).</li>
 * </ul>
 * <p>
 * This class provides the common functionality and abstract methods for connection establishment, sending and receiving XML stanzas, closing the session, etc.
 * </p>
 * Concrete implementations may have different concepts for authentication, e.g. normal C2S sessions use SASL, while the Jabber Component Protocol uses a different kind of handshake for authenticating.
 *
 * @author Christian Schudt
 * @see XmppClient
 * @see <a href="http://xmpp.org/extensions/xep-0114.html">XEP-0114: Jabber Component Protocol</a>
 * @see <a href="http://xmpp.org/extensions/xep-0174.html">XEP-0174: Serverless Messaging</a>
 */
public abstract class XmppSession implements AutoCloseable {

    protected static final Collection<Consumer<XmppSession>> creationListeners = new CopyOnWriteArraySet<>();

    private static final Logger logger = Logger.getLogger(XmppSession.class.getName());

    private static final EnumSet<Status> IS_CONNECTED = EnumSet.of(Status.CONNECTED, Status.AUTHENTICATED, Status.AUTHENTICATING);

    protected final List<Connection> connections = new ArrayList<>();

    protected final XmppSessionConfiguration configuration;

    protected final ServiceDiscoveryManager serviceDiscoveryManager;

    protected final StreamFeaturesManager streamFeaturesManager;

    final Set<Consumer<ConnectionEvent>> connectionListeners = new CopyOnWriteArraySet<>();

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

    private final Set<Consumer<MessageEvent>> messageAcknowledgedListeners = new CopyOnWriteArraySet<>();

    /**
     * The unacknowledged stanzas.
     */
    private final Queue<Stanza> unacknowledgedStanzas = new ConcurrentLinkedDeque<>();

    private final Map<Stanza, SendTask<? extends Stanza>> stanzaTrackingMap = new ConcurrentHashMap<>();

    /**
     * Holds the connection state.
     */
    private final AtomicReference<Status> status = new AtomicReference<>(Status.INITIAL);

    /**
     * guarded by "connections"
     */
    protected Connection activeConnection;

    /**
     * The XMPP domain which will be assigned by the server's response. This is read by different threads, so make it volatile to ensure visibility of the written value.
     */
    protected volatile Jid xmppServiceDomain;

    /**
     * Any exception that occurred during stream negotiation ({@link #connect()}))
     */
    protected volatile Throwable exception;

    protected volatile boolean wasLoggedIn;

    ExecutorService iqHandlerExecutor;

    ExecutorService stanzaListenerExecutor;

    /**
     * The shutdown hook for JVM shutdown, which will disconnect each open connection before the JVM is halted.
     */
    private volatile Thread shutdownHook;

    private volatile XmppDebugger debugger;

    protected XmppSession(String xmppServiceDomain, XmppSessionConfiguration configuration, ConnectionConfiguration... connectionConfigurations) {
        this.xmppServiceDomain = xmppServiceDomain != null && !xmppServiceDomain.isEmpty() ? Jid.of(xmppServiceDomain) : null;
        this.configuration = configuration;
        this.stanzaListenerExecutor = Executors.newSingleThreadExecutor(XmppUtils.createNamedThreadFactory("Stanza Listener Thread"));
        this.iqHandlerExecutor = Executors.newCachedThreadPool(XmppUtils.createNamedThreadFactory("IQ Handler Thread"));
        this.serviceDiscoveryManager = getManager(ServiceDiscoveryManager.class);
        this.streamFeaturesManager = getManager(StreamFeaturesManager.class);

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

    static boolean isSentToUserOrServer(Stanza stanza, Jid domain, Jid connectedResource) {
        if (stanza instanceof Presence) {
            return false;
        }
        if (stanza.getTo() == null) {
            return true;
        }
        Jid toBare = stanza.getTo().asBareJid();
        return connectedResource != null && toBare.equals(connectedResource.asBareJid())
                || domain != null && (toBare.equals(domain) || toBare.toString().endsWith("." + domain.toEscapedString()));
    }

    protected static void throwAsXmppExceptionIfNotNull(Throwable e) throws XmppException {
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
     * Adds a listener, which is triggered, whenever a new session is created.
     *
     * @param listener The listener.
     * @see #removeCreationListener(Consumer)
     */
    public static void addCreationListener(Consumer<XmppSession> listener) {
        creationListeners.add(listener);
    }

    /**
     * Removes a previously added creation listener.
     *
     * @param listener The listener.
     * @see #addCreationListener(Consumer)
     */
    public static void removeCreationListener(Consumer<XmppSession> listener) {
        creationListeners.remove(listener);
    }

    protected static void notifyCreationListeners(XmppSession xmppSession) {
        creationListeners.forEach(xmppSessionConsumer -> {
            try {
                xmppSessionConsumer.accept(xmppSession);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        });
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

    public abstract void connect(Jid from) throws XmppException;

    protected final void tryConnect(Jid from, String namespace, Consumer<Jid> onStreamOpened) throws XmppException {

        // Close any previous connection, which might still be open.
        try {
            closeAndNullifyConnection();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failure during closing previous connection.", e);
        }

        synchronized (connections) {
            Iterator<Connection> connectionIterator = getConnections().iterator();
            while (connectionIterator.hasNext()) {
                Connection connection = connectionIterator.next();
                try {
                    connection.connect(from, namespace, onStreamOpened);
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
        }
    }

    /**
     * Checks, if the session is already connected and if so, logs it.
     *
     * @return True, if the session is already connected.
     */
    protected boolean checkConnected() {
        if (isConnected()) {
            // Silently return, when we are already connected.
            logger.fine("Already connected. Return silently.");
            return true;
        }
        return false;
    }

    /**
     * Checks, if the session is closed and throws an {@link IllegalStateException} if so.
     *
     * @return The current status of session (which will never be {@link Status#CLOSED}.
     */
    protected Status preConnect() {
        Status previousStatus = getStatus();
        if (previousStatus == Status.CLOSED) {
            throw new IllegalStateException("Session is already closed. Create a new one.");
        }
        return previousStatus;
    }

    /**
     * Called, when the connection has failed. It closes the connection and reverts the session status to the previous status.
     *
     * @param previousStatus The previous status.
     * @param e              The exception. Any exception during closing the connection will be added as suppressed exception to this one.
     * @throws XmppException The exception, which will be rethrown.
     */
    protected final void onConnectionFailed(Status previousStatus, Throwable e) throws XmppException {
        try {
            closeAndNullifyConnection();
        } catch (Exception e1) {
            e.addSuppressed(e1);
        }
        updateStatus(previousStatus, e);
        throwAsXmppExceptionIfNotNull(e);
    }

    /**
     * Checks several preconditions before login.
     *
     * @return The previous status.
     */
    protected final Status preLogin() {
        Status previousStatus = getStatus();
        if (!IS_CONNECTED.contains(previousStatus)) {
            throw new IllegalStateException("You must be connected to the server before trying to login. Status is " + previousStatus);
        }
        if (getDomain() == null) {
            throw new IllegalStateException("The XMPP domain must not be null.");
        }
        exception = null;
        return previousStatus;
    }

    /**
     * Checks, if the session is already authenticated and if so, logs it.
     *
     * @return True, if the session is already authenticated.
     */
    protected boolean checkAuthenticated() {
        if (getStatus() == Status.AUTHENTICATED) {
            // Silently return, when we are already logged in.
            logger.fine("Already logged in. Return silently.");
            return true;
        }
        return false;
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
     * @param messageListener The message listener.
     */
    public final void addMessageAcknowledgedListener(Consumer<MessageEvent> messageListener) {
        messageAcknowledgedListeners.add(messageListener);
    }

    /**
     * Adds an IQ handler for a given payload type. The handler will be processed asynchronously, which means it won't block the inbound stanza processing queue.
     *
     * @param type      The payload type.
     * @param iqHandler The IQ handler.
     * @see #removeIQHandler(Class)
     * @see #addIQHandler(Class, IQHandler, boolean)
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
     * @see #addIQHandler(Class, IQHandler)
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
     * Adds a connection listener, which is triggered, when the connection used by this session is disconnected or reconnected.
     *
     * @param connectionListener The connection listener.
     * @see #removeConnectionListener(Consumer)
     */
    public final void addConnectionListener(Consumer<ConnectionEvent> connectionListener) {
        connectionListeners.add(connectionListener);
    }

    /**
     * Removes a previously added connection listener.
     *
     * @param connectionListener The connection listener.
     * @see #addConnectionListener(Consumer)
     */
    public final void removeConnectionListener(Consumer<ConnectionEvent> connectionListener) {
        connectionListeners.remove(connectionListener);
    }

    /**
     * Sends an {@code <iq/>} stanza and returns an async result, which can be used to wait for the response. The result is completed if the response IQ has arrived or the default timeout has exceeded, in which case the result completes with a {@link NoResponseException}.
     * <p>
     *
     * @param iq The {@code <iq/>} stanza, which must be of type {@linkplain Type#GET get} or {@linkplain Type#SET set}.
     * @return The async IQ result.
     */
    public final AsyncResult<IQ> query(IQ iq) {
        return query(iq, configuration.getDefaultResponseTimeout());
    }

    /**
     * Sends an {@code <iq/>} stanza and returns an async result, which can be used to wait for the response. The result is completed if the response IQ has arrived or the timeout has exceeded, in which case the result completes with a {@link NoResponseException}.
     *
     * @param iq      The {@code <iq/>} stanza, which must be of type {@linkplain Type#GET get} or {@linkplain Type#SET set}.
     * @param timeout The timeout.
     * @return The async IQ result.
     */
    public final AsyncResult<IQ> query(IQ iq, long timeout) {

        if (!iq.isRequest()) {
            throw new IllegalArgumentException("IQ must be of type 'get' or 'set'");
        }
        return sendAndAwait(iq,
                IQEvent::getIQ,
                responseIQ -> responseIQ.isResponse() && responseIQ.getId() != null && responseIQ.getId().equals(iq.getId()),
                this::sendIQ,
                this::addInboundIQListener,
                this::removeInboundIQListener,
                timeout);
    }

    /**
     * Sends an {@code <iq/>} stanza and returns an async result, which can be used to wait for the response. The result is completed if the response IQ has arrived or the default timeout has exceeded, in which case the result completes with a {@link NoResponseException}.
     * The payload of the response IQ is returned in the async result class.
     *
     * @param <T>   The type.
     * @param iq    The {@code <iq/>} stanza, which must be of type {@linkplain rocks.xmpp.core.stanza.model.IQ.Type#GET get} or {@linkplain rocks.xmpp.core.stanza.model.IQ.Type#SET set}.
     * @param clazz The class which is IQ response's payload.
     * @return The async result with the IQ response's payload.
     */
    public final <T> AsyncResult<T> query(IQ iq, Class<T> clazz) {
        return query(iq).thenApply(
                result -> result.getExtension(clazz));
    }

    /**
     * Sends a stanza and returns an async result which can wait for the presence stanza, which matches the predicate, to arrive.
     *
     * @param stanza The stanza, which is sent.
     * @param filter The presence filter.
     * @return The async presence result.
     */
    public final AsyncResult<Presence> sendAndAwaitPresence(Presence stanza, final Predicate<Presence> filter) {
        return sendAndAwait(stanza,
                PresenceEvent::getPresence,
                filter,
                this::sendPresence,
                this::addInboundPresenceListener,
                this::removeInboundPresenceListener,
                configuration.getDefaultResponseTimeout()
        );
    }

    /**
     * Sends a stanza and returns an async result which can wait for the message stanza, which matches the predicate, to arrive.
     *
     * @param stanza The stanza, which is sent.
     * @param filter The message filter.
     * @return The async message result.
     */
    public final AsyncResult<Message> sendAndAwaitMessage(Message stanza, final Predicate<Message> filter) {
        return sendAndAwait(stanza,
                MessageEvent::getMessage,
                filter,
                this::sendMessage,
                this::addInboundMessageListener,
                this::removeInboundMessageListener,
                configuration.getDefaultResponseTimeout()
        );
    }

    private <S extends Stanza, E extends EventObject> AsyncResult<S> sendAndAwait(S stanza, Function<E, S> stanzaMapper, final Predicate<S> filter, Function<S, SendTask<S>> sendFunction, Consumer<Consumer<E>> addListener, Consumer<Consumer<E>> removeListener, long timeout) {
        CompletableFuture<S> completableFuture = new CompletableFuture<>();

        final Consumer<E> listener = e -> {
            S st = stanzaMapper.apply(e);
            if (filter.test(st)) {
                if (st.getError() != null) {
                    completableFuture.completeExceptionally(new StanzaException(st));
                }
                completableFuture.complete(st);
            }
        };

        addListener.accept(listener);

        sendFunction.apply(stanza);

        return new AsyncResult<>(completableFuture.applyToEither(CompletionStages.timeoutAfter(timeout, TimeUnit.MILLISECONDS, () -> new NoResponseException("Timeout reached, while waiting on a response.")), Function.identity())).whenComplete((result, e) ->
                removeListener.accept(listener));
    }

    /**
     * Gets the actively used connection.
     *
     * @return The actively used connection.
     */
    public final Connection getActiveConnection() {
        synchronized (connections) {
            return activeConnection;
        }
    }

    /**
     * Sets the XMPP service domain. This should only be set by a connection implementation.
     *
     * @param xmppServiceDomain The XMPP service domain.
     */
    protected final void setXmppServiceDomain(Jid xmppServiceDomain) {
        this.xmppServiceDomain = xmppServiceDomain;
    }

    /**
     * Sends an XML element to the server, usually a stanza, i.e. a message, presence or IQ.
     *
     * @param element The XML element.
     * @return The sent stream element, which is usually the same as the parameter, but may differ in case a stanza is sent, e.g. a {@link Message} is translated to a {@link rocks.xmpp.core.stanza.model.client.ClientMessage}.
     */
    public Future<?> send(StreamElement element) {
        return sendInternal(element, connection -> {
        });
    }

    private Future<?> sendInternal(StreamElement element, Consumer<Connection> beforeSend) {

        Status status = getStatus();
        if (element instanceof Stanza) {
            Stanza stanza = (Stanza) element;
            // If resource binding has not completed and it's tried to send a stanza which doesn't serve the purpose
            // of resource binding, throw an exception, because otherwise the server will terminate the connection with a stream error.
            // TODO: Consider queuing such stanzas and send them as soon as logged in instead of throwing exception.
            if (!EnumSet.of(Status.AUTHENTICATED, Status.CLOSING).contains(getStatus())
                    && !isSentToUserOrServer(stanza, getDomain(), getConnectedResource())) {
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
        synchronized (connections) {
            if (activeConnection == null) {
                IllegalStateException ise = new IllegalStateException("Session is not connected to server (status: " + status + ')');
                Throwable cause = exception;
                if (cause != null) {
                    ise.initCause(cause);
                }
                throw ise;
            } else {
                beforeSend.accept(activeConnection);
                return activeConnection.send(element);
            }
        }
    }

    /**
     * Sends an IQ.
     *
     * @param iq The IQ.
     * @return The send task, which allows to track the stanza.
     */
    public abstract SendTask<IQ> sendIQ(final IQ iq);

    /**
     * Sends a message.
     *
     * @param message The message.
     * @return The send task, which allows to track the stanza.
     */
    public abstract SendTask<Message> sendMessage(final Message message);

    /**
     * Sends a presence.
     *
     * @param presence The presence.
     * @return The send task, which allows to track the stanza.
     */
    public abstract SendTask<Presence> sendPresence(final Presence presence);

    protected final <S extends Stanza> SendTask<S> trackAndSend(S stanza) {
        SendTask<S> sendTask = new SendTask<>(stanza);
        sendInternal(stanza, connection -> {
            // Only track stanzas, if the connection allows it.
            if (connection.isUsingAcknowledgements()) {
                stanzaTrackingMap.putIfAbsent(stanza, sendTask);
            }
        });
        return sendTask;
    }

    /**
     * Gets the status of the session.
     *
     * @return The status.
     */
    public final Status getStatus() {
        return status.get();
    }

    /**
     * Updates the status and notifies the session listeners.
     *
     * @param status The new status.
     * @return True, if the status has changed; otherwise false.
     */
    protected final boolean updateStatus(Status status) {
        return updateStatus(status, (Throwable) null);
    }

    /**
     * Updates the status and notifies the session listeners.
     *
     * @param status The new status.
     * @param e      The exception.
     * @return True, if the status has changed; otherwise false.
     */
    protected final boolean updateStatus(Status status, Throwable e) {
        Status oldStatus = this.status.getAndSet(status);
        if (status != oldStatus) {
            // Make sure to not call listeners from within synchronized region.
            XmppUtils.notifyEventListeners(sessionStatusListeners, new SessionStatusEvent(this, status, oldStatus, e));
        }
        return status != oldStatus;
    }

    /**
     * Updates the status and notifies the session listeners.
     *
     * @param expected The expected status.
     * @param status   The new status.
     * @return True, if the status has changed; otherwise false.
     */
    protected final boolean updateStatus(Status expected, Status status) {
        boolean hasChanged = this.status.compareAndSet(expected, status);
        if (hasChanged) {
            // Make sure to not call listeners from within synchronized region.
            XmppUtils.notifyEventListeners(sessionStatusListeners, new SessionStatusEvent(this, status, expected, null));
        }
        return hasChanged;
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
     * </p>
     *
     * @return The unmarshaller.
     * @see #createMarshaller()
     */
    public final Unmarshaller createUnmarshaller() {
        try {
            return configuration.getJAXBContext().createUnmarshaller();
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Creates a marshaller, which can be used to create XML from objects.
     * <p>
     * The returned marshaller is configured with {@code Marshaller.JAXB_FRAGMENT = true}, so that no XML header is written
     * (which is usually what we want in XMPP when writing stanzas).
     * </p>
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
            throw new DataBindingException(e);
        }
    }

    /**
     * Indicates, whether the session is connected.
     *
     * @return True, if the status is {@link Status#CONNECTED}, {@link Status#AUTHENTICATED} or {@link Status#AUTHENTICATING}.
     * @see #getStatus()
     */
    public final boolean isConnected() {
        return IS_CONNECTED.contains(getStatus());
    }

    /**
     * Returns true, if the session is authenticated. For a normal client-to-server session this means, when the user has logged in (successfully completed SASL negotiation and resource binding).
     *
     * @return True, if authenticated.
     * @since 0.7.0
     */
    public final boolean isAuthenticated() {
        return getStatus() == Status.AUTHENTICATED;
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
    public boolean handleElement(final Object element) throws XmppException {

        StreamManager streamManager = getManager(StreamManager.class);

        if (element instanceof IQ) {
            final IQ iq = (IQ) element;

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
                                IQ response = iqHandler.handleRequest(iq);
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
            iqHandlerExecutor.execute(() -> {
                XmppUtils.notifyEventListeners(inboundIQListeners, new IQEvent(this, iq, true));
                streamManager.incrementInboundStanzaCount();
            });
        } else if (element instanceof Message) {
            stanzaListenerExecutor.execute(() -> {
                XmppUtils.notifyEventListeners(inboundMessageListeners, new MessageEvent(this, (Message) element, true));
                streamManager.incrementInboundStanzaCount();
            });
        } else if (element instanceof Presence) {
            stanzaListenerExecutor.execute(() -> {
                XmppUtils.notifyEventListeners(inboundPresenceListeners, new PresenceEvent(this, (Presence) element, true));
                streamManager.incrementInboundStanzaCount();
            });
        } else if (element instanceof StreamFeatures) {
            streamFeaturesManager.processFeatures((StreamFeatures) element);
        } else if (element instanceof StreamError) {
            throw new StreamErrorException((StreamError) element);
        } else {
            // Let's see, if the element is known to any feature negotiator.
            return streamFeaturesManager.processElement(element);
        }
        return false;
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
        T instance;
        if ((instance = (T) instances.get(clazz)) == null) {
            synchronized (instances) {
                if ((instance = (T) instances.get(clazz)) == null) {
                    try {
                        Constructor<T> constructor = clazz.getDeclaredConstructor(XmppSession.class);
                        constructor.setAccessible(true);
                        instance = constructor.newInstance(this);
                        instance.initialize();
                        instances.put(clazz, instance);
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        throw new IllegalArgumentException("Can't instantiate the provided class:" + clazz, e);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Explicitly closes the session and performs a clean up of all listeners. Calling this method, if the session is already closing or closed has no effect.
     *
     * @throws XmppException If an exception occurs while closing the connection, e.g. the underlying socket connection.
     */
    @Override
    public final void close() throws XmppException {
        if (getStatus() == Status.CLOSED || !updateStatus(Status.CLOSING)) {
            return;
        }
        // The following code should only be called once, no matter how many threads concurrently call this method.
        try {
            closeAndNullifyConnection();
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

    private void closeAndNullifyConnection() throws Exception {
        synchronized (connections) {
            if (activeConnection != null) {
                try {
                    activeConnection.close();
                } finally {
                    activeConnection = null;
                }
            }
        }
    }

    /**
     * Called if any unhandled exception is thrown during reading or writing.
     * <p>
     * This method will close the stream.
     * </p>
     *
     * @param e The exception. If an unrecoverable XMPP stream error occurred, the exception is a {@link rocks.xmpp.core.stream.model.StreamError}.
     */
    public void notifyException(Throwable e) {
        // If the exception occurred during stream negotiation, i.e. before the connect() method has finished, the exception will be thrown.
        exception = Objects.requireNonNull(e, "exception must not be null");
        // Release a potential waiting thread.
        streamFeaturesManager.cancelNegotiation();
        if (EnumSet.of(Status.AUTHENTICATED, Status.AUTHENTICATING, Status.CONNECTED, Status.CONNECTING).contains(getStatus()) && !(e instanceof AuthenticationException)) {
            try {
                closeAndNullifyConnection();
            } catch (Exception e1) {
                e.addSuppressed(e1);
            }
            updateStatus(Status.DISCONNECTED, e);
        }
    }

    /**
     * Gets the XMPP domain of the connected server. This variable is set after server has responded with a stream header. The domain is the stream header's 'from' attribute.
     *
     * @return The XMPP domain.
     */
    public Jid getDomain() {
        return xmppServiceDomain;
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
     * The connected resource, which is assigned by the server.
     *
     * @return The connected resource.
     */
    public abstract Jid getConnectedResource();

    public final Queue<Stanza> getUnacknowledgedStanzas() {
        return unacknowledgedStanzas;
    }

    public final void markAcknowledged(Stanza acknowledgedStanza) {
        if (acknowledgedStanza != null) {
            if (acknowledgedStanza instanceof Message) {
                XmppUtils.notifyEventListeners(messageAcknowledgedListeners, new MessageEvent(this, (Message) acknowledgedStanza, false));
            }
            SendTask sendTask = stanzaTrackingMap.remove(acknowledgedStanza);
            if (sendTask != null) {
                sendTask.receivedByServer();
            }
        }
    }

    /**
     * Represents the session status.
     * <p>
     * The following chart illustrates the valid status transitions:
     * </p>
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
