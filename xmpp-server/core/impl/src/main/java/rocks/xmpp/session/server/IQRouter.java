/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.InboundIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.util.concurrent.CompletionStages;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class IQRouter implements InboundIQHandler {

    @Inject
    private UserManager userManager;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private Instance<IQHandler> iqHandlers;

    private final Map<String, CompletableFuture<IQ>> pendingResults = new ConcurrentHashMap<>();

    public boolean process(IQ iq) {
        Session sessionSender = sessionManager.getSession(iq.getFrom());

        if (iq.getType() == null) {
            // return <bad-request/> if the <iq/> has unknown type.
            sessionSender.send(iq.createError(Condition.BAD_REQUEST));
            return true;
        }

        if (iq.isRequest()) {
            Object payload = iq.getExtension(Object.class);

            if (payload == null) {
                // return <bad-request/> if the <iq/> has unknown type.
                sessionSender.send(iq.createError(Condition.BAD_REQUEST));
                return true;
            }

            try {
                if (iq.getTo() == null) {
                    // 10.3.  No 'to' Address
                    // the server MUST handle it directly on behalf of the entity that sent it
                    iq.setTo(iq.getFrom().asBareJid());
                }

                // 10.5.3.  localpart@domainpart
                // 10.5.3.1.  No Such User
                if (iq.getTo().getLocal() != null && !userManager.userExists(iq.getTo().getLocal())) {
                    // For an IQ stanza, the server MUST return a <service-unavailable/> stanza error (Section 8.3.3.19) to the sender.
                    sessionSender.send(iq.createError(Condition.SERVICE_UNAVAILABLE));
                    return true;
                }

                // 10.5.4.  localpart@domainpart/resourcepart
                if (iq.getTo().isFullJid() && iq.getTo().getLocal() != null) {
                    Session sessionRecipient = sessionManager.getSession(iq.getTo());
                    if (sessionRecipient == null) {
                        // If there is no connected resource that exactly matches the full JID, the stanza SHOULD be processed as if the JID were of the form <localpart@domainpart>
                        iq.setTo(iq.getTo().asBareJid());
                    } else {
                        // If the JID contained in the 'to' attribute is of the form <localpart@domainpart/resourcepart>, the user exists, and there is a connected resource that exactly matches the full JID, the server MUST deliver the stanza to that connected resource.
                        sessionRecipient.send(iq);
                        return true;
                    }
                }

                final Optional<IQHandler> iqHandler = iqHandlers.stream()
                        .filter(handler -> handler.getPayloadClass() != null && handler.getPayloadClass().isAssignableFrom(payload.getClass()))
                        .findFirst();

                if (iqHandler.isPresent()) {

                    IQ result = iqHandler.get().handleRequest(iq);
                    if (result != null) {
                        sessionSender.send(result);
                        return true;
                    }

                } else {
                    sessionSender.send(iq.createError(Condition.SERVICE_UNAVAILABLE));
                    return true;
                }
            } catch (Exception e) {
                sessionSender.send(iq.createError(Condition.INTERNAL_SERVER_ERROR));
                return true;
            }
        } else {
            CompletableFuture<IQ> resultFuture = pendingResults.remove(iq.getId());
            if (resultFuture != null) {
                resultFuture.complete(iq);
            }
        }
        return false;
    }

    CompletableFuture<IQ> waitForResult(IQ iq, Duration duration) {
        CompletableFuture<IQ> resultFuture = new CompletableFuture<>();
        pendingResults.put(iq.getId(), resultFuture);
        return resultFuture.applyToEither(CompletionStages.timeoutAfter(duration.toMillis(), TimeUnit.MILLISECONDS), Function.identity());
    }

    @Override
    public void handleInboundIQ(IQEvent e) {
        process(e.getIQ());
    }
}
