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

package rocks.xmpp.session.server;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.addr.MalformedJid;
import rocks.xmpp.core.Addressable;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.bind.server.ResourceBindingNegotiator;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.sasl.server.SaslNegotiator;
import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.core.stanza.model.client.ClientPresence;
import rocks.xmpp.core.stream.StreamHandler;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.StreamNegotiationResult;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.core.stream.server.StreamFeatureProvider;
import rocks.xmpp.core.stream.server.ServerStreamFeaturesManager;
import rocks.xmpp.extensions.sm.server.ServerStreamManager;
import rocks.xmpp.util.concurrent.AsyncResult;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.security.Principal;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Christian Schudt
 */
@Dependent
public class InboundClientSession implements Addressable, Session, StreamHandler, AutoCloseable {

    private static final Duration QUERY_TIMEOUT = Duration.ofSeconds(5);

    @Inject
    private StanzaProcessor stanzaProcessor;

    @Inject
    private ServerConfiguration serverConfiguration;

    @Inject
    private Instance<StreamFeatureProvider<?>> streamFeatureNegotiators;

    @Inject
    private IQRouter iqRouter;

    private final String id = UUID.randomUUID().toString();

    private Connection connection;

    private final AtomicBoolean open = new AtomicBoolean();

    private final ServerStreamFeaturesManager streamFeaturesManager = new ServerStreamFeaturesManager();

    private Principal principal;

    private Jid address;

    public InboundClientSession() {
        this.streamFeaturesManager.registerStreamFeatureProvider(new SaslNegotiator(this));
        this.streamFeaturesManager.registerStreamFeatureProvider(new ResourceBindingNegotiator(this));
        this.streamFeaturesManager.registerStreamFeatureProvider(new ServerStreamManager(this));
    }

    @PostConstruct
    public void init() {
        streamFeatureNegotiators.stream().forEach(this.streamFeaturesManager::registerStreamFeatureProvider);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Jid getLocalXmppAddress() {
        return null;
    }

    @Override
    public Jid getRemoteXmppAddress() {
        return address;
    }

    @Override
    public CompletionStage<Void> send(StreamElement streamElement) {
        if (streamElement instanceof Message) {
            streamElement = ClientMessage.from((Message) streamElement);
        } else if (streamElement instanceof Presence) {
            streamElement = ClientPresence.from((Presence) streamElement);
        } else if (streamElement instanceof IQ) {
            streamElement = ClientIQ.from((IQ) streamElement);
        }
        CompletionStage<Void> stage = connection.write(streamElement);
        connection.flush();
        return stage;
    }

    public final ServerStreamFeaturesManager getStreamFeatureManager() {
        return streamFeaturesManager;
    }

    @Override
    public final void close() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public final CompletionStage<Void> closeAsync() {
        if (open.compareAndSet(false, true)) {
            StreamHeader streamHeader = StreamHeader.responseClientToServer(CDI.current().select(DefaultServerConfiguration.class).get().getDomain(), null, getId(), getLanguage());
            connection.write(streamHeader);
        }
        return connection.closeAsync();
    }

    @Override
    public final CompletionStage<Void> closeAsync(StreamError streamError) {
        if (open.compareAndSet(false, true)) {
            StreamHeader streamHeader = StreamHeader.responseClientToServer(CDI.current().select(DefaultServerConfiguration.class).get().getDomain(), null, getId(), getLanguage());
            connection.write(streamHeader);
        }
        return connection.closeAsync(streamError);
    }

    public AsyncResult<IQ> query(IQ iq) {
        CompletableFuture<IQ> resultFuture = iqRouter.waitForResult(iq, QUERY_TIMEOUT);
        send(iq);
        return new AsyncResult<>(resultFuture);
    }

    @Override
    public final boolean handleElement(Object element) throws StreamNegotiationException {
        if (element instanceof SessionOpen) {
            SessionOpen initialStreamHeader = (SessionOpen) element;
            SessionOpen responseStreamHeader = StreamHeader.create(Jid.ofDomain("domain"), initialStreamHeader.getTo(), getId(), "1.0", Locale.ENGLISH, "jabber:client");
            connection.open(responseStreamHeader);
            connection.send(new StreamFeatures(streamFeaturesManager.getStreamFeatures()));
            open.set(true);
        } else {
            StreamNegotiationResult result = streamFeaturesManager.handleElement((StreamElement) element);
            switch (result) {
                case RESTART:
                    return true;
                case INCOMPLETE:
                case SUCCESS:
                    return false;
                default:
                    break;
            }
            if (element instanceof Stanza) {
                Stanza stanza = (Stanza) element;
                Optional<Jid> address = getAddress();
                if (address.isPresent()) {
                    // RFC 6120 § 8.1.2.1.  Client-to-Server Streams
                    // When a server receives an XML stanza from a connected client, the server MUST add a 'from' attribute to the stanza
                    // or override the 'from' attribute specified by the client, where the value of the 'from' attribute MUST be the full JID (<localpart@domainpart/resource>) determined by the server for the connected resource that generated the stanza (see Section 4.3.6),
                    // or the bare JID (<localpart@domainpart>) in the case of subscription-related presence stanzas
                    if (stanza instanceof Presence && ((Presence) stanza).isSubscription()) {
                        stanza.setFrom(address.get().asBareJid());
                    } else {
                        stanza.setFrom(address.get());
                    }
                } else {
                    // Resource binding not completed.
                    closeAsync(new StreamError(Condition.NOT_AUTHORIZED));
                    return false;
                }
                if (stanza.getTo() instanceof MalformedJid) {
                    Stanza errorStanza = stanza.createError(new StanzaError(StanzaError.Type.MODIFY, rocks.xmpp.core.stanza.model.errors.Condition.JID_MALFORMED, ((MalformedJid) stanza.getTo()).getCause().getMessage(), Locale.ENGLISH, null, serverConfiguration.getDomain()));
                    send(errorStanza);
                    return false;
                }

                stanzaProcessor.process(stanza);
            }
        }
        return false;
    }

    public final String getId() {
        return id;
    }

    public Optional<Principal> getPrincipal() {
        return Optional.ofNullable(principal);
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public final Optional<Jid> getAddress() {
        return Optional.ofNullable(address);
    }

    public void setAddress(Jid address) {
        this.address = address;
    }

    public Connection getConnection() {
        return connection;
    }

    public final Locale getLanguage() {
        return Locale.ENGLISH;
    }

    @Override
    public Jid getJid() {
        return getRemoteXmppAddress();
    }
}
