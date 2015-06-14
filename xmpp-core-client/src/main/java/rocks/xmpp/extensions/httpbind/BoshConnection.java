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

package rocks.xmpp.extensions.httpbind;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.Connection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.extensions.compress.CompressionMethod;
import rocks.xmpp.extensions.httpbind.model.Body;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0124.html">XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)</a> and <a href="http://xmpp.org/extensions/xep-0206.html">XEP-0206: XMPP Over BOSH</a>.
 *
 * @author Christian Schudt
 */
public final class BoshConnection extends Connection {

    /**
     * Use ConcurrentSkipListMap to maintain insertion order.
     */
    final Map<Long, Body> unacknowledgedRequests = new ConcurrentSkipListMap<>();

    /**
     * The request id. A large number which will get incremented with every request.
     */
    private final AtomicLong rid = new AtomicLong();

    private final XMLOutputFactory xmlOutputFactory;

    private final XMLInputFactory xmlInputFactory;

    private final BoshConnectionConfiguration boshConnectionConfiguration;

    private final XmppDebugger debugger;

    private final Deque<String> keySequence = new ArrayDeque<>();

    /**
     * The current request count, i.e. the current number of simultaneous requests.
     */
    private final AtomicInteger requestCount = new AtomicInteger();

    /**
     * Our supported compression methods.
     */
    private final Map<String, CompressionMethod> compressionMethods;

    /**
     * The encoding we can support. This is added as "Accept-Encoding" header in a request.
     */
    private final String clientAcceptEncoding;

    /**
     * The executor, which will execute HTTP requests.
     * Guarded by "this".
     */
    private ExecutorService httpBindExecutor;

    /**
     * The compression method which is used to compress requests.
     * Guarded by "this".
     */
    private CompressionMethod requestCompressionMethod;

    /**
     * The "Content-Encoding" header which is set during requests. It's only set, if the server included an "accept" attribute.
     * Guarded by "this".
     */
    private String requestContentEncoding;

    /**
     * Guarded by "this".
     */
    private long highestReceivedRid;

    /**
     * The SID MUST be unique within the context of the connection manager application.
     * Guarded by "this".
     */
    private String sessionId;

    /**
     * Guarded by "this".
     */
    private String authId;

    /**
     * True, if the connection manager sends acknowledgments.
     * Guarded by "this".
     */
    private boolean usingAcknowledgments;

    /**
     * Guarded by "this".
     */
    private URL url;

    private Consumer<String> onStreamOpened;

    BoshConnection(XmppSession xmppSession, BoshConnectionConfiguration configuration) {
        super(xmppSession, configuration);
        this.boshConnectionConfiguration = configuration;
        this.debugger = getXmppSession().getDebugger();

        xmlOutputFactory = XMLOutputFactory.newFactory();
        xmlInputFactory = XMLInputFactory.newFactory();
        compressionMethods = new LinkedHashMap<>();
        for (CompressionMethod compressionMethod : boshConnectionConfiguration.getCompressionMethods()) {
            compressionMethods.put(compressionMethod.getName(), compressionMethod);
        }
        if (!compressionMethods.isEmpty()) {
            clientAcceptEncoding = String.join(",", compressionMethods.keySet());
        } else {
            clientAcceptEncoding = null;
        }
    }

    /**
     * <blockquote>
     * <p>All HTTP codes except 200 have been superseded by Terminal Binding Conditions to allow clients to determine whether the source of errors is the connection manager application or an HTTP intermediary.</p>
     * <p>A legacy client (or connection manager) is a client (or connection manager) that did not include a 'ver' attribute in its session creation request (or response). A legacy client (or connection manager) will interpret (or respond with) HTTP error codes according to the table below.</p>
     * </blockquote>
     *
     * @param httpCode The HTTP response code.
     * @throws BoshException If the HTTP code was not 200.
     */
    private static void handleCode(int httpCode) throws BoshException {
        if (httpCode != HttpURLConnection.HTTP_OK) {
            switch (httpCode) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    // Superseded by bad-request
                    throw new BoshException(Body.Condition.BAD_REQUEST, httpCode);
                case HttpURLConnection.HTTP_FORBIDDEN:
                    // Superseded by policy-violation
                    throw new BoshException(Body.Condition.POLICY_VIOLATION, httpCode);
                case HttpURLConnection.HTTP_NOT_FOUND:
                    // Superseded by item-not-found
                    throw new BoshException(Body.Condition.ITEM_NOT_FOUND, httpCode);
                default:
                    throw new BoshException(Body.Condition.UNDEFINED_CONDITION, httpCode);
            }
        }
    }

    /**
     * Tries to find the BOSH URL by a DNS TXT lookup as described in <a href="http://xmpp.org/extensions/xep-0156.html">XEP-0156</a>.
     *
     * @param xmppServiceDomain The fully qualified domain name.
     * @param timeout           The lookup timeout.
     * @return The BOSH URL, if it could be found or null.
     */
    private static String findBoshUrl(String xmppServiceDomain, long timeout) {

        try {
            String query = "_xmppconnect." + xmppServiceDomain;

            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            // 0 seems to mean "infinite", which is a bad idea.
            if (timeout > 0) {
                // http://docs.oracle.com/javase/7/docs/technotes/guides/jndi/jndi-dns.html
                // If this property has not been set, the default initial timeout is 1000 milliseconds.
                env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(timeout));
            }

            DirContext ctx = new InitialDirContext(env);

            Attributes attributes = ctx.getAttributes(query, new String[]{"TXT"});
            Attribute srvAttribute = attributes.get("TXT");

            if (srvAttribute != null) {
                NamingEnumeration<?> enumeration = srvAttribute.getAll();
                while (enumeration.hasMore()) {
                    String txtRecord = (String) enumeration.next();
                    String[] txtRecordParts = txtRecord.split("=");
                    String key = txtRecordParts[0];
                    String value = txtRecordParts[1];
                    if ("_xmpp-client-xbosh".equals(key)) {
                        return value;
                    }
                }
            }
        } catch (NamingException e) {
            return null;
        }
        return null;
    }

    /**
     * Generates a key sequence.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0124.html#keys-generate">15.3 Generating the Key Sequence</a>
     */
    private void generateKeySequence() {
        keySequence.clear();
        try {
            // K(1) = hex(SHA-1(seed))
            // K(2) = hex(SHA-1(K(1)))
            // ...
            // K(n) = hex(SHA-1(K(n-1)))

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            Random random = new SecureRandom();
            // Generate a high random value "n"
            int n = 256 + random.nextInt(32768 - 256);
            // Generate a random seed value.
            String kn = UUID.randomUUID().toString();
            for (int i = 0; i < n; i++) {
                kn = String.format("%040x", new BigInteger(1, digest.digest(kn.getBytes(StandardCharsets.UTF_8))));
                keySequence.add(kn);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Connects to the BOSH server.
     *
     * @param from The optional 'from' attribute in the initial BOSH session creation request.
     * @throws IOException If a connection could not be established.
     */
    @Override
    public final synchronized void connect(Jid from, String namespace, Consumer<String> onStreamOpened) throws IOException {

        if (sessionId != null) {
            // Already connected.
            return;
        }

        if (getXmppSession() == null) {
            throw new IllegalStateException("Can't connect without XmppSession. Use XmppSession to connect.");
        }

        if (url == null) {
            String protocol = boshConnectionConfiguration.isSecure() ? "https" : "http";
            // If no port has been configured, use the default ports.
            int targetPort = getPort() > 0 ? getPort() : (boshConnectionConfiguration.isSecure() ? 5281 : 5280);
            // If a hostname has been configured, use it to connect.
            if (getHostname() != null) {
                url = new URL(protocol, getHostname(), targetPort, boshConnectionConfiguration.getFile());
            } else if (getXmppSession().getDomain() != null) {
                // If a URL has not been set, try to find the URL by the domain via a DNS-TXT lookup as described in XEP-0156.
                String resolvedUrl = findBoshUrl(getXmppSession().getDomain(), boshConnectionConfiguration.getConnectTimeout());
                if (resolvedUrl != null) {
                    url = new URL(resolvedUrl);
                } else {
                    // Fallback mechanism:
                    // If the URL could not be resolved, use the domain name and port 5280 as default.
                    url = new URL(protocol, getXmppSession().getDomain(), targetPort, boshConnectionConfiguration.getFile());
                }
                this.port = url.getPort() > 0 ? url.getPort() : url.getDefaultPort();
                this.hostname = url.getHost();
            } else {
                throw new IllegalStateException("Neither an URL nor a domain given for a BOSH connection.");
            }
        }

        this.from = from;
        this.sessionId = null;
        this.authId = null;
        this.requestCount.set(0);
        this.onStreamOpened = onStreamOpened;

        // Set the initial request id with a large random number.
        // The largest possible number for a RID is (2^53)-1
        // So initialize it with a random number with max value of 2^52.
        // This will still allow for at least 4503599627370495 requests (2^53-1-2^52), which should be sufficient.
        rid.set(new BigInteger(52, new Random()).longValue());

        // Create initial request.
        Body.Builder body = Body.builder()
                .requestId(rid.getAndIncrement())
                .language(Locale.getDefault().getLanguage())
                .version("1.11")
                .wait(boshConnectionConfiguration.getWait())
                .hold((byte) 1)
                .route(boshConnectionConfiguration.getRoute())
                .ack(1L)
                .from(from)
                .xmppVersion("1.0");

        if (boshConnectionConfiguration.isUseKeySequence()) {
            synchronized (keySequence) {
                generateKeySequence();
                body.newKey(keySequence.removeLast());
            }
        }

        if (getXmppSession().getDomain() != null && !getXmppSession().getDomain().isEmpty()) {
            body.to(getXmppSession().getDomain());
        }

        // Try if we can connect in order to fail fast if we can't.
        HttpURLConnection connection = null;
        try {
            connection = getConnection();
            connection.setConnectTimeout(boshConnectionConfiguration.getConnectTimeout());
            connection.setReadTimeout(boshConnectionConfiguration.getConnectTimeout());
            connection.connect();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        // Threads created by this thread pool, will be used to do simultaneous requests.
        // Even in the unusual case, where the connection manager allows for more requests, two are enough.
        httpBindExecutor = Executors.newFixedThreadPool(2, XmppUtils.createNamedThreadFactory("XMPP BOSH Request Thread"));

        // Send the initial request.
        sendNewRequest(body.build());
    }

    @Override
    public final boolean isSecure() {
        return boshConnectionConfiguration.isSecure();
    }

    /**
     * Gets the body element from the response and unpacks its content.
     * <p>
     * If it is the session creation response it contains additional attributes like the session id. These attributes are set to this connection.
     * </p>
     * The contents are delegated to the {@link rocks.xmpp.core.session.XmppSession#handleElement(Object)} method, where they are treated as normal XMPP elements, i.e. the same way as in a normal TCP connection.
     *
     * @param responseBody The body.
     * @throws Exception If any exception occurred during handling the inner XMPP elements.
     */
    private void unpackBody(Body responseBody) throws Exception {
        // It's the session creation response.
        if (responseBody.getSid() != null) {
            synchronized (this) {
                sessionId = responseBody.getSid();
                authId = responseBody.getAuthId();
                if (responseBody.getAck() != null) {
                    usingAcknowledgments = true;
                }
                // The connection manager MAY include an 'accept' attribute in the session creation response element, to specify a comma-separated list of the content encodings it can decompress.
                if (responseBody.getAccept() != null) {
                    // After receiving a session creation response with an 'accept' attribute,
                    // clients MAY include an HTTP Content-Encoding header in subsequent requests (indicating one of the encodings specified in the 'accept' attribute) and compress the bodies of the requests accordingly.
                    String[] serverAcceptedEncodings = responseBody.getAccept().split(",");
                    // Let's see if we can compress the contents for the server by choosing a known compression method.
                    for (String serverAcceptedEncoding : serverAcceptedEncodings) {
                        requestCompressionMethod = compressionMethods.get(serverAcceptedEncoding);
                        requestContentEncoding = serverAcceptedEncoding;
                        if (requestCompressionMethod != null) {
                            break;
                        }
                    }
                }
                if (responseBody.getFrom() != null) {
                    onStreamOpened.accept(responseBody.getFrom().getDomain());
                }
            }
        }

        if (responseBody.getAck() != null) {
            // The response has acknowledged another request.
            ackReceived(responseBody.getAck());
        }

        // If the body contains an error condition, which is not a stream error, terminate the connection by throwing an exception.
        if (responseBody.getType() == Body.Type.TERMINATE && responseBody.getCondition() != null && responseBody.getCondition() != Body.Condition.REMOTE_STREAM_ERROR) {
            throw new BoshException(responseBody.getCondition(), responseBody.getUri());
        } else if (responseBody.getType() == Body.Type.ERROR) {
            // In any response it sends to the client, the connection manager MAY return a recoverable error by setting a 'type' attribute of the <body/> element to "error". These errors do not imply that the HTTP session is terminated.
            // If it decides to recover from the error, then the client MUST repeat the HTTP request that resulted in the error, as well as all the preceding HTTP requests that have not received responses. The content of these requests MUST be identical to the <body/> elements of the original requests. This enables the connection manager to recover a session after the previous request was lost due to a communication failure.
            unacknowledgedRequests.values().forEach(this::sendNewRequest);
        }

        for (Object wrappedObject : responseBody.getWrappedObjects()) {
            if (getXmppSession().handleElement(wrappedObject)) {
                restartStream();
            }
        }
    }

    /**
     * Restarts the stream.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0206.html#preconditions-sasl">Authentication and Resource Binding</a></cite></p>
     * <p>Upon receiving the {@code <success/>} element, the client MUST then ask the connection manager to restart the stream by sending a "restart request" that is structured as follows</p>
     * <ul>
     * <li>The BOSH {@code <body/>} element MUST include a boolean 'restart' attribute (qualified by the 'urn:xmpp:xbosh' namespace) whose value is set to "true".</li>
     * <li>The BOSH {@code <body/>} element SHOULD include the 'to' attribute.</li>
     * <li>The BOSH {@code <body/>} element SHOULD include the 'xml:lang' attribute.</li>
     * <li>The BOSH {@code <body/>} element SHOULD be empty (i.e., not contain an XML stanza). However, if the client includes an XML stanza in the body, the connection manager SHOULD ignore it.</li>
     * </ul>
     * </blockquote>
     */
    @Override
    protected final void restartStream() {
        Body.Builder bodyBuilder;
        synchronized (this) {
            bodyBuilder = Body.builder()
                    .restart(true)
                    .to(getXmppSession().getDomain())
                    .language(Locale.getDefault().getLanguage())
                    .sessionId(getSessionId())
                    .from(from)
                    .requestId(rid.getAndIncrement());

            appendKey(bodyBuilder);

            // Acknowledge the highest received rid.
            // The only exception is that, after its session creation request, the client SHOULD NOT include an 'ack' attribute in any request if it has received responses to all its previous requests.
            if (!unacknowledgedRequests.isEmpty()) {
                bodyBuilder.ack(highestReceivedRid);
            }
        }
        sendNewRequest(bodyBuilder.build());
    }

    /**
     * Closes the session as described in <a href="http://xmpp.org/extensions/xep-0124.html#terminate">Terminating the HTTP Session</a>.
     * <p>
     * This method can be called from different threads:
     * </p>
     * <ol>
     * <li>The application thread.</li>
     * <li>The reader thread, which closes the BOSH session after seeing an error from the connection manager.</li>
     * <li>The writer thread.</li>
     * </ol>
     *
     * @throws java.lang.InterruptedException If the current thread is interrupted.
     */
    @Override
    public final void close() throws Exception {
        if (getSessionId() != null) {
            synchronized (this) {
                sessionId = null;
                authId = null;
                requestContentEncoding = null;
                keySequence.clear();
                requestContentEncoding = null;

                if (httpBindExecutor != null && !httpBindExecutor.isShutdown()) {
                    // Terminate the BOSH session.
                    Body.Builder bodyBuilder = Body.builder()
                            .requestId(rid.getAndIncrement())
                            .sessionId(getSessionId())
                            .type(Body.Type.TERMINATE);

                    appendKey(bodyBuilder);

                    sendNewRequest(bodyBuilder.build());

                    // and then shut it down.
                    httpBindExecutor.shutdown();
                    // Wait shortly, until the "terminate" body has been sent.
                    httpBindExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
                    httpBindExecutor = null;
                }
            }
        }
    }

    /**
     * Detaches this BOSH session without closing (aka terminating) it. This way the BOSH session is still alive on the server and can be ported over to a web page, but new BOSH requests are no longer sent by this connection.
     *
     * @return The current request ID (RID) which was used for the last BOSH request.
     * @see <a href="https://conversejs.org/docs/html/#prebinding-and-single-session-support">https://conversejs.org/docs/html/#prebinding-and-single-session-support</a>
     */
    public final long detach() {
        synchronized (this) {
            if (httpBindExecutor != null && !httpBindExecutor.isShutdown()) {
                httpBindExecutor.shutdown();
                httpBindExecutor = null;
            }
        }
        // Return the latest and greatest rid.
        return rid.get();
    }

    @Override
    public final void send(StreamElement element) {
        // Only put content in the body element, if it is allowed (e.g. it does not contain restart='true' and an unacknowledged body isn't resent).
        Body.Builder bodyBuilder = Body.builder()
                .wrappedObjects(Collections.singletonList(element))
                .requestId(rid.getAndIncrement())
                .sessionId(getSessionId());

        appendKey(bodyBuilder);

        // Acknowledge the highest received rid.
        // The only exception is that, after its session creation request, the client SHOULD NOT include an 'ack' attribute in any request if it has received responses to all its previous requests.
        if (!unacknowledgedRequests.isEmpty()) {
            synchronized (this) {
                bodyBuilder.ack(highestReceivedRid);
            }
        }
        sendNewRequest(bodyBuilder.build());
    }

    /**
     * Appends a key attribute to the body and generates a new key sequence if the old one is empty.
     *
     * @param bodyBuilder The builder.
     * @see <a href="http://xmpp.org/extensions/xep-0124.html#keys-use">15.4 Use of Keys</a>
     * @see <a href="http://xmpp.org/extensions/xep-0124.html#keys-switch">15.5 Switching to Another Key Sequence</a>
     */
    private void appendKey(Body.Builder bodyBuilder) {
        if (boshConnectionConfiguration.isUseKeySequence()) {
            synchronized (keySequence) {
                if (!keySequence.isEmpty()) {
                    bodyBuilder.key(keySequence.removeLast());
                    if (keySequence.isEmpty()) {
                        generateKeySequence();
                        bodyBuilder.newKey(keySequence.removeLast());
                    }
                }
            }
        }
    }

    /**
     * Gets the session id of this BOSH connection.
     *
     * @return The session id.
     */
    public final synchronized String getSessionId() {
        return sessionId;
    }

    @Override
    public final synchronized String getStreamId() {
        // The same procedure applies to the obsolete XMPP-specific 'authid' attribute of the BOSH <body/> element, which contains the value of the XMPP stream ID generated by the XMPP server.
        return authId;
    }

    /**
     * Sends all elements waiting in the queue to the server.
     * <p>
     * If there are currently more requests than allowed by the server, the waiting elements will be send as soon one of the requests return.
     * </p>
     *
     * @param body The wrapper body element.
     */
    private void sendNewRequest(final Body body) {

        // Make sure, no two threads access this block, in order to ensure that requestCount and httpBindExecutor.isShutdown() don't return inconsistent values.
        synchronized (this) {
            if (httpBindExecutor != null && !httpBindExecutor.isShutdown()) {
                httpBindExecutor.execute(() -> {

                    // Open a HTTP connection.
                    HttpURLConnection httpConnection = null;
                    try {
                        // Synchronize the requests, so that nearly parallel requests are still sent in the same order (to prevent <item-not-found/> errors).
                        synchronized (BoshConnection.this) {
                            requestCount.getAndIncrement();

                            if (usingAcknowledgments) {
                                unacknowledgedRequests.put(body.getRid(), body);
                            }

                            httpConnection = getConnection();
                            httpConnection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

                            // We can decompress server responses, so tell the server about it.
                            if (clientAcceptEncoding != null) {
                                httpConnection.setRequestProperty("Accept-Encoding", clientAcceptEncoding);
                            }

                            // If we can compress, tell the server about it.
                            if (requestCompressionMethod != null && requestContentEncoding != null) {
                                httpConnection.setRequestProperty("Content-Encoding", requestContentEncoding);
                            }

                            httpConnection.setDoOutput(true);
                            httpConnection.setRequestMethod("POST");
                            // If the connection manager does not respond in time, throw a SocketTimeoutException, which terminates the connection.
                            httpConnection.setReadTimeout((boshConnectionConfiguration.getWait() + 5) * 1000);

                            try (OutputStream requestStream = requestCompressionMethod != null ? requestCompressionMethod.compress(httpConnection.getOutputStream()) : httpConnection.getOutputStream()) {
                                // This is for logging only.
                                ByteArrayOutputStream byteArrayOutputStreamRequest = new ByteArrayOutputStream();

                                // Branch the stream, so that its output can also be logged.
                                try (OutputStream branchedOutputStream = XmppUtils.createBranchedOutputStream(requestStream, byteArrayOutputStreamRequest)) {

                                    XMLStreamWriter xmlStreamWriter = null;
                                    try (OutputStream xmppOutputStream = debugger != null ? debugger.createOutputStream(branchedOutputStream) : branchedOutputStream) {
                                        // Create the writer for this connection.
                                        xmlStreamWriter = XmppUtils.createXmppStreamWriter(xmlOutputFactory.createXMLStreamWriter(xmppOutputStream, "UTF-8"));

                                        // Then write the XML to the output stream by marshalling the object to the writer.
                                        // Marshaller needs to be recreated here, because it's not thread-safe.
                                        getXmppSession().createMarshaller().marshal(body, xmlStreamWriter);
                                        xmlStreamWriter.flush();

                                        if (debugger != null) {
                                            debugger.writeStanza(byteArrayOutputStreamRequest.toString(), body);
                                        }
                                    } finally {
                                        if (xmlStreamWriter != null) {
                                            xmlStreamWriter.close();
                                        }
                                    }
                                }
                            }
                        }

                        // Wait for the response
                        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                            // We received a response for the request. Store the RID, so that we can inform the connection manager with our next request, that we received a response.
                            synchronized (BoshConnection.this) {
                                highestReceivedRid = body.getRid();
                            }

                            String contentEncoding = httpConnection.getHeaderField("Content-Encoding");
                            try (InputStream responseStream = contentEncoding != null ? compressionMethods.get(contentEncoding).decompress(httpConnection.getInputStream()) : httpConnection.getInputStream()) {
                                // This is for logging only.
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                // Branch the stream so that its input can be logged.
                                try (InputStream inputStream = XmppUtils.createBranchedInputStream(responseStream, byteArrayOutputStream)) {
                                    try (InputStream xmppInputStream = debugger != null ? debugger.createInputStream(inputStream) : inputStream) {
                                        XMLEventReader xmlEventReader = null;
                                        try {
                                            // Read the response.
                                            xmlEventReader = xmlInputFactory.createXMLEventReader(xmppInputStream, "UTF-8");
                                            while (xmlEventReader.hasNext()) {
                                                XMLEvent xmlEvent = xmlEventReader.peek();

                                                // Parse the <body/> element.
                                                if (xmlEvent.isStartElement()) {
                                                    JAXBElement<Body> element = getXmppSession().createUnmarshaller().unmarshal(xmlEventReader, Body.class);

                                                    if (debugger != null) {
                                                        debugger.readStanza(byteArrayOutputStream.toString(), element.getValue());
                                                    }
                                                    unpackBody(element.getValue());
                                                } else {
                                                    xmlEventReader.next();
                                                }
                                            }
                                        } finally {
                                            // The response itself acknowledges the request, so we can remove the request.
                                            ackReceived(body.getRid());
                                            if (xmlEventReader != null) {
                                                xmlEventReader.close();
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            handleCode(httpConnection.getResponseCode());
                            try (InputStream errorStream = httpConnection.getErrorStream()) {
                                while (errorStream.read() > -1) {
                                    // Just read the error stream, so that the connection can be reused.
                                    // http://docs.oracle.com/javase/8/docs/technotes/guides/net/http-keepalive.html
                                }
                            }
                        }

                        // Wait shortly before sending the long polling request.
                        // This allows the send method to chime in and send a <body/> with actual payload instead of an empty body just to "hold the line".
                        Thread.sleep(100);

                        // As soon as the client receives a response from the connection manager it sends another request, thereby ensuring that the connection manager is (almost) always holding a request that it can use to "push" data to the client.
                        if (requestCount.decrementAndGet() == 0) {
                            Body.Builder bodyBuilder = Body.builder()
                                    .requestId(rid.getAndIncrement())
                                    .sessionId(getSessionId());

                            appendKey(bodyBuilder);

                            // Acknowledge the highest received rid.
                            // The only exception is that, after its session creation request, the client SHOULD NOT include an 'ack' attribute in any request if it has received responses to all its previous requests.
                            if (!unacknowledgedRequests.isEmpty()) {
                                synchronized (BoshConnection.this) {
                                    bodyBuilder.ack(highestReceivedRid);
                                }
                            }
                            sendNewRequest(bodyBuilder.build());
                        }
                    } catch (Exception e) {
                        synchronized (this) {
                            if (httpBindExecutor != null && !httpBindExecutor.isShutdown()) {
                                httpBindExecutor.shutdown();
                            }
                        }
                        getXmppSession().notifyException(e);
                    } finally {
                        if (httpConnection != null) {
                            httpConnection.disconnect();
                        }
                    }
                });
            }
        }
    }

    private void ackReceived(Long rid) {
        Body body = unacknowledgedRequests.remove(rid);
        if (body != null) {
            // TODO trigger some listener
            body.getWrappedObjects().stream().filter(object -> object instanceof Stanza).forEach(object -> {
                // TODO trigger some listener
            });
        }
    }

    private HttpURLConnection getConnection() throws IOException {
        Proxy proxy = getProxy();
        HttpURLConnection httpURLConnection;
        if (proxy != null) {
            httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        }
        if (httpURLConnection instanceof HttpsURLConnection) {
            if (boshConnectionConfiguration.getSSLContext() != null) {
                ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(boshConnectionConfiguration.getSSLContext().getSocketFactory());
            }
            if (boshConnectionConfiguration.getHostnameVerifier() != null) {
                ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(boshConnectionConfiguration.getHostnameVerifier());
            }
        }
        return httpURLConnection;
    }

    /**
     * Gets the route.
     * <blockquote>
     * <p>A connection manager MAY be configured to enable sessions with more than one server in different domains. When requesting a session with such a "proxy" connection manager, a client SHOULD include a 'route' attribute that specifies the protocol, hostname, and port of the server with which it wants to communicate, formatted as "proto:host:port" (e.g., "xmpp:example.com:9999").</p>
     * </blockquote>
     *
     * @return The route.
     */
    public final String getRoute() {
        return boshConnectionConfiguration.getRoute();
    }

    @Override
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder("BOSH connection");
        if (hostname != null) {
            sb.append(String.format(" to %s", url));
        }
        if (sessionId != null) {
            sb.append(" (").append(sessionId).append(")");
        }
        if (from != null) {
            sb.append(", from: ").append(from);
        }
        return sb.toString();
    }
}
