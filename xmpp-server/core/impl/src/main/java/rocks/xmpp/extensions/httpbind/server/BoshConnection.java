/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.extensions.httpbind.server;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.SecurityContext;

import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.net.AbstractConnection;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stream.StreamHandler;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.session.server.DefaultServerConfiguration;
import rocks.xmpp.util.XmppUtils;

/**
 * @author Christian Schudt
 */
public final class BoshConnection extends AbstractConnection {

    /**
     * This is the longest time (in seconds) that the connection manager will wait before responding to any request
     * during the session.
     */
    private static final Duration DEFAULT_WAIT = Duration.ofMinutes(1);

    private static final Duration DEFAULT_INACTIVITY = Duration.ofSeconds(10);

    private static final short MAX_HOLD = 1;

    private static final Duration MAX_PAUSE = Duration.ofMinutes(2);

    private final Queue<BodyRequest> requests = new ConcurrentLinkedQueue<>();

    private final Queue<BodyRequest> inboundQueue = new PriorityQueue<>();

    private final Collection<Object> deliverables = Collections.synchronizedList(new ArrayList<>());

    private final AtomicBoolean terminated = new AtomicBoolean();

    private final BoshConnectionManager connectionManager;

    private final CompletableFuture<Void> closeFuture = new CompletableFuture<>();

    private final StreamHandler streamHandler;

    /**
     * The initial "hold" value should be 1 so that at least the session creation request is hold until data is there.
     */
    private short hold = 1;

    private Duration wait = DEFAULT_WAIT;

    private Duration maxPause;

    private short simultaneousRequests = 1;

    private final Map<Long, Body> responseBuffer = Collections.synchronizedMap(new LinkedHashMap<Long, Body>(2, 0.75F) {
        private static final long serialVersionUID = 8694099654479207279L;

        @Override
        protected final boolean removeEldestEntry(final Map.Entry<Long, Body> entry) {
            // The number of responses to non-pause requests kept in the buffer SHOULD be either
            // the same as the maximum number of simultaneous requests allowed by the connection manager
            // or, if Acknowledgements are being used, the number of responses that have not yet been acknowledged.
            return size() > simultaneousRequests;
        }
    });

    /**
     * The scheduled future, which will timeout after the inactivity period, which starts with the last response and if
     * there are no other requests. It is cancelled on each new request.
     *
     * <p>Guarded by "inboundQueue".</p>
     */
    private Future<?> inactivityFuture;

    /**
     * The last received request, whose RID is +1 from the previous RID.
     *
     * <p>Guarded by "inboundQueue".</p>
     */
    private Body lastRequest;

    /**
     * The date of the last sent response.
     *
     * <p>Guarded by "inboundQueue".</p>
     */
    private Instant lastResponseDate;

    /**
     * The RID of the last responded request.
     *
     * <p>Guarded by "inboundQueue".</p>
     */
    private long lastResponseRid;

    private boolean secure;

    BoshConnection(final StreamHandler streamHandler, final Body sessionRequest, final AsyncResponse asyncResponse,
                   final SecurityContext securityContext, final BoshConnectionManager connectionManager) {
        super(null, streamHandler, null);
        this.connectionManager = connectionManager;
        this.streamHandler = streamHandler;
        requestReceived(sessionRequest, asyncResponse, securityContext);
    }

    /**
     * Verifies a request by comparing the hash of its key with the previous request's key.
     *
     * @param body            The request.
     * @param previousRequest The previous request.
     * @return true, if verified.
     * @see <a href="https://xmpp.org/extensions/xep-0124.html#keys">15. Protecting Insecure Sessions</a>
     */
    private static boolean verifyKey(final Body body, final Body previousRequest) {
        if (previousRequest != null && (previousRequest.getNewKey() != null || previousRequest.getKey() != null)) {
            if (body.getKey() == null) {
                // If it receives a request without a 'key' attribute and the 'newkey' or 'key' attribute of the
                // previous request was set then the connection manager MUST NOT process the element
                return false;
            } else {
                final String hashedKey = XmppUtils.hash(body.getKey().getBytes(StandardCharsets.UTF_8));
                final String previousKey =
                        previousRequest.getNewKey() != null ? previousRequest.getNewKey() : previousRequest.getKey();
                return hashedKey.equalsIgnoreCase(previousKey);
            }
        }
        // No previous request or previous request is not using the key sequencing mechanism.
        return true;
    }

    final void requestReceived(final Body body, final AsyncResponse asyncResponse,
                               final SecurityContext securityContext) {

        final BodyRequest request = new BodyRequest(body, asyncResponse);

        // 14.3 Broken Connections
        // Whenever the connection manager receives a request with a 'rid' that it has already received,
        // it SHOULD return an HTTP 200 (OK) response that includes the buffered copy of the original XML response to
        // the client.
        final Body bufferedBody = responseBuffer.get(body.getRid());
        if (bufferedBody != null) {
            request.asyncResponse.resume(bufferedBody);
            return;
        }

        // Remove and resume the request after the timeout ('wait' attribute).
        asyncResponse.setTimeoutHandler(response -> {
            requests.remove(request);
            request.resume(Body.builder(), true);
        });
        asyncResponse.setTimeout(wait.getSeconds(), TimeUnit.SECONDS);

        final List<BodyRequest> orderedRequests = new ArrayList<>();
        Body.Condition condition = null;
        synchronized (inboundQueue) {
            this.secure = securityContext.isSecure();

            // 14.3 Broken Connections
            // If the connection manager receives a request for a 'rid' which has already been received
            // but to which it has not yet responded then it SHOULD respond immediately to the existing request
            // with a recoverable binding condition (see Recoverable Binding Conditions)
            // and send any future response to the latest request.
            final Optional<BodyRequest> pendingRequest = Stream.concat(requests.stream(), inboundQueue.stream())
                    .filter(r -> r.body.getRid().equals(body.getRid())).findAny();
            pendingRequest.ifPresent(bodyRequest -> bodyRequest.resume(Body.builder().type(Body.Type.ERROR), true));

            // This queue will sort the requests by their RID.
            inboundQueue.offer(request);
            if (requests.size() + inboundQueue.size() > simultaneousRequests) {
                // Too many simultaneous requests
                condition = Body.Condition.POLICY_VIOLATION;
            } else {
                BodyRequest queuedRequest;
                // Check the RID of the next request.
                while ((queuedRequest = inboundQueue.peek()) != null) {
                    // Loop while the next element's RID is ok.
                    if (lastRequest == null || queuedRequest.body.getRid() == lastRequest.getRid() + 1) {
                        final BodyRequest nextRequest = inboundQueue.poll();
                        // Verify the keys, if the requests are using the key sequencing mechanism.
                        if (!verifyKey(nextRequest.body, lastRequest)) {
                            condition = Body.Condition.ITEM_NOT_FOUND;
                            break;
                        } else {
                            orderedRequests.add(nextRequest);
                        }
                        lastRequest = body;
                    } else {
                        // We received a RID which is out of order.
                        // Do nothing with the request and wait if the next request contains the missing RID,
                        // unless the RID is larger than the upper limit of the expected window
                        if (queuedRequest.body.getRid() < lastRequest.getRid()
                                || queuedRequest.body.getRid() - lastRequest.getRid() > simultaneousRequests) {
                            condition = Body.Condition.ITEM_NOT_FOUND;
                            break;
                        }
                        break;
                    }
                }
            }
        }
        if (condition != null) {
            asyncResponse.resume(Body.builder().type(Body.Type.TERMINATE).condition(condition).build());
            closeAsync();
            return;
        }
        orderedRequests.forEach(this::processMessageInOrder);
    }

    /**
     * Process a request. The contents of the request are forwarded to the session in order.
     *
     * @param request The request.
     * @see <a href="https://xmpp.org/extensions/xep-0124.html#rids-order">14.2 In-Order Message Forwarding</a>
     */
    private void processMessageInOrder(final BodyRequest request) {

        final Body body = request.body;

        // Upon reception of a session pause request,
        // if the requested period is not greater than the maximum permitted time,
        // then the connection manager SHOULD respond immediately to all pending requests
        // (including the pause request) and temporarily increase the maximum inactivity period to the requested time.
        short hold = this.hold;
        final boolean isPause = body.getPause() != null && body.getPause().getSeconds() > 0;
        if (isPause) {
            maxPause = Duration.ofSeconds(Math.min(body.getPause().getSeconds(), MAX_PAUSE.getSeconds()));
            // Note: The response to the pause request MUST NOT contain any payloads.
            request.resume(Body.builder(), false);
            // Respond to all requests.
            hold = 0;
        } else {
            // The connection manager SHOULD set the maximum inactivity period back to normal upon reception of the next
            // request from the client
            maxPause = DEFAULT_INACTIVITY;
            requests.add(request);
        }

        synchronized (inboundQueue) {
            // Cancel the inactivity future, the inactivity timeout will start again on the next response.
            if (inactivityFuture != null) {
                inactivityFuture.cancel(false);
            }
            // After receiving a request with an 'ack' value less than the 'rid'
            // of the last request that it has already responded to,
            // the connection manager MAY inform the client of the situation by sending its next
            // response immediately instead of waiting until it has payloads to send to the client.
            if (body.getAck() != null && body.getAck() < lastResponseRid) {
                final BodyRequest nextResponse = requests.poll();
                if (nextResponse != null) {
                    nextResponse.resume(Body.builder()
                            // In this case it SHOULD include a 'report' attribute set to one greater
                            // than the 'ack' attribute it received from the client,
                            .report(body.getAck() + 1)
                            // and a 'time' attribute set to the number of milliseconds
                            // since it sent the response associated with the 'report' attribute.
                            .time(Duration.between(lastResponseDate, Instant.now())), true);
                }
            }
        }

        for (Object object : body.getWrappedObjects()) {
            if (object instanceof StreamElement) {
                try {
                    streamHandler.handleElement(object);
                } catch (StreamErrorException e) {
                    closeAsync(e.getError());
                } catch (XmppException e) {
                    closeAsync(new StreamError(Condition.UNDEFINED_CONDITION));
                }
            }
        }

        // Respond any pending request, which exceeds the 'hold' value.
        while (requests.size() > hold) {
            BodyRequest bodyRequest = requests.poll();
            if (bodyRequest != null) {
                bodyRequest.resume(Body.builder(), true);
            }
        }

        if (body.isRestart()) {
            try {
                streamHandler.handleElement(body);
            } catch (StreamErrorException e) {
                closeAsync(e.getError());
            } catch (XmppException e) {
                closeAsync(new StreamError(Condition.UNDEFINED_CONDITION));
            }
        } else {
            flush();
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public final CompletableFuture<Void> open(final SessionOpen sessionOpen) {
        final BodyRequest bodyRequest = requests.peek();
        final Body sessionRequest = bodyRequest.body;
        this.hold =
                (short) (sessionRequest.getHold() != null ? Math.min(sessionRequest.getHold(), MAX_HOLD) : MAX_HOLD);
        this.wait = sessionRequest.getWait() != null ? Duration
                .ofSeconds(Math.min(sessionRequest.getWait().getSeconds(), DEFAULT_WAIT.getSeconds())) : DEFAULT_WAIT;
        this.simultaneousRequests =
                (short) ((sessionRequest.getHold() != null ? sessionRequest.getHold() : MAX_HOLD) + 1);
        // Send the Session Creation Response
        bodyRequest.response = Body.builder()
                .accept("gzip,deflate")
                .sessionId(sessionOpen.getId())
                .authId(sessionOpen.getId())
                .wait(wait)
                .ack(sessionRequest.getRid())
                .requests(simultaneousRequests)
                .version("1.11.1")
                .hold(hold)
                .inactivity(DEFAULT_INACTIVITY)
                .maxPause(MAX_PAUSE)
                .to(sessionRequest.getTo())
                .from(CDI.current().select(DefaultServerConfiguration.class).get().getDomain())
                .language(sessionOpen.getLanguage())
                .restartLogic(true)
                .xmppVersion("1.0");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> send(StreamElement streamElement) {
        CompletableFuture<Void> future = write(streamElement);
        flush();
        return future;
    }

    @Override
    public final CompletableFuture<Void> write(final StreamElement streamElement) {
        deliverables.add(streamElement);
        // TODO: return completed if flushed.
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public final void flush() {
        Body.Builder body = null;
        BodyRequest bodyRequest = null;
        synchronized (deliverables) {
            if (!deliverables.isEmpty()) {
                do {
                    bodyRequest = requests.poll();
                    if (bodyRequest != null) {
                        Body.Builder bodyBuilder = bodyRequest.response != null ? bodyRequest.response : Body.builder();
                        if (deliverables.stream().anyMatch(deliverable -> deliverable instanceof StreamError)) {
                            bodyBuilder.condition(Body.Condition.REMOTE_STREAM_ERROR);
                        }
                        if (isClosed()) {
                            bodyBuilder.type(Body.Type.TERMINATE);
                            terminated.set(true);
                        }
                        body = bodyBuilder.wrappedObjects(deliverables);
                        deliverables.clear();
                    }
                } while (bodyRequest != null && bodyRequest.asyncResponse.isDone());
            }
        }
        if (bodyRequest != null) {
            bodyRequest.resume(body, false);
        }
    }

    @Override
    public final boolean isSecure() {
        synchronized (inboundQueue) {
            return this.secure;
        }
    }

    @Override
    public final CompletionStage<Void> closeFuture() {
        return closeFuture;
    }

    @Override
    protected void restartStream() {
    }

    @Override
    protected CompletionStage<Void> closeStream() {
        flush();
        BodyRequest bodyRequest;
        // The connection manager SHOULD acknowledge the session termination on the oldest connection
        // with a HTTP 200 OK containing a <body/> element of the type 'terminate'.
        // On all other open connections, the connection manager SHOULD respond with an HTTP 200 OK containing an empty
        // <body/> element.
        while ((bodyRequest = requests.poll()) != null) {
            Body.Builder bodyBuilder = Body.builder();
            // Only send a type="terminate" once.
            if (terminated.compareAndSet(false, true)) {
                bodyBuilder.type(Body.Type.TERMINATE);
            }
            bodyRequest.resume(bodyBuilder, true);
        }
        closeFuture.complete(null);
        return closeFuture;
    }

    @Override
    protected CompletionStage<Void> closeConnection() {
        return null;
    }

    private final class BodyRequest implements Comparable<BodyRequest> {

        private final Body body;

        private final AsyncResponse asyncResponse;

        private Body.Builder response;

        private BodyRequest(Body body, AsyncResponse asyncResponse) {
            this.body = body;
            this.asyncResponse = asyncResponse;
        }

        private boolean resume(Body.Builder response, boolean mayAddPayload) {
            final Body bodyToSend;

            synchronized (inboundQueue) {
                if (!lastRequest.getRid().equals(body.getRid())) {
                    response.ack(lastRequest.getRid());
                }
            }
            if (mayAddPayload) {
                synchronized (deliverables) {
                    response.wrappedObjects(deliverables);
                    deliverables.clear();
                }
            }

            bodyToSend = response.build();

            // The connection manager SHOULD remember the 'rid' and the associated HTTP response body
            // of the client's most recent requests which were not session pause requests (see Inactivity)
            // and which did not result in an HTTP or binding error.
            if (body.getPause() == null && bodyToSend.getType() != Body.Type.TERMINATE) {
                BoshConnection.this.responseBuffer.put(this.body.getRid(), bodyToSend);
            }

            // 10. Inactivity
            // If the connection manager has responded to all the requests it has received within a session
            // and the time since its last response is longer than the maximum inactivity period, then it SHOULD assume
            // the client has been disconnected and terminate the session without informing the client.
            if (requests.isEmpty()) {
                synchronized (inboundQueue) {
                    BoshConnection.this.inactivityFuture =
                            BoshConnection.this.connectionManager.scheduledExecutorService.schedule(() -> {
                                System.out.println("Closing inactive BOSH session.");
                                closeAsync();
                            }, maxPause.getSeconds(), TimeUnit.SECONDS);
                }
            }

            if (asyncResponse.resume(bodyToSend)) {
                synchronized (inboundQueue) {
                    lastResponseDate = Instant.now();
                    lastResponseRid = this.body.getRid();
                }
                return true;
            }
            return false;
        }

        @Override
        public int compareTo(BodyRequest o) {
            return body.compareTo(o.body);
        }
    }
}
