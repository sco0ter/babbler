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

package org.xmpp.extension.httpbind;

import org.xmpp.Connection;
import org.xmpp.XmppUtils;
import org.xmpp.stream.ClientStreamElement;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0124.html">XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)</a> and <a href="http://xmpp.org/extensions/xep-0206.html">XEP-0206: XMPP Over BOSH</a>.
 *
 * @author Christian Schudt
 */
public final class BoshConnection extends Connection {
    /**
     *
     */
    private static final Logger logger = Logger.getLogger(BoshConnection.class.getName());

    /**
     * Use ConcurrentSkipListMap to maintain insertion order.
     */
    final Map<Long, Body> unacknowledgedRequests = new ConcurrentSkipListMap<>();

    /**
     * The request id. A large number which will get incremented with every request.
     */
    private final AtomicLong rid = new AtomicLong();

    /**
     * A queue of objects, which will wait for the next request available to be send to the server in one {@code <body/>} element.
     * Every time a new request is made, this queue is cleared.
     */
    private final Queue<Object> queue = new ConcurrentLinkedQueue<>();

    /**
     * The executor, which will execute HTTP requests.
     */
    private final ExecutorService requestExecutor;

    /**
     * The executor, which will handle responses from HTTP request.
     */
    private final ExecutorService responseExecutor;

    /**
     * The optional route.
     */
    private final String route;

    /**
     * The number of seconds to wait.
     */
    private final Short wait;

    private final XMLOutputFactory xmlOutputFactory;

    private final XMLInputFactory xmlInputFactory;

    /**
     *
     */
    private volatile long highestReceivedRid;

    /**
     * The URL for the HTTP requests.
     */
    private URL url;

    /**
     * The file which will be appended to the base URL. This is "/http-bind/" by default.
     */
    private String file;

    /**
     * The SID MUST be unique within the context of the connection manager application.
     */
    private volatile String sessionId;

    /**
     * The current request count, i.e. the current number of simultaneous requests.
     */
    private volatile byte requestCount;

    /**
     * True, if the connection manager sends acknowledgments.
     */
    private volatile boolean usingAcknowledgments;

    /**
     * Creates a BOSH connection on the basis of the XMPP service domain.
     * <p>
     * During connecting, the BOSH URL is looked up via a DNS-TXT lookup (<a href="http://xmpp.org/extensions/xep-0156.html">XEP-0156</a>).
     * </p>
     * If this fails, the fallback mechanism will be to assemble the BOSH URL in the following way:
     * http://[xmppServiceDomain]/http-bind/
     */
    public BoshConnection() {
        this(Proxy.NO_PROXY);
    }

    /**
     * Creates a BOSH connection on basis of the XMPP service domain through a proxy.
     *
     * @param proxy The proxy, which should be of type {@link java.net.Proxy.Type#HTTP}
     * @see #BoshConnection()
     */
    public BoshConnection(Proxy proxy) {
        this(null, 0, null, proxy, null, (short) 60);
    }

    /**
     * Creates a BOSH connection on basis of the XMPP service domain, host and port.
     * The BOSH URL used during connecting will be {@code http://<host>:<port>/http-bind/}
     *
     * @param hostname The hostname.
     * @param port     The port.
     */
    public BoshConnection(String hostname, int port) {
        this(hostname, port, null, Proxy.NO_PROXY, null, (short) 60);
    }

    /**
     * Creates a BOSH connection on basis of the XMPP service domain, host and port.
     * The BOSH URL used during connecting will be {@code http://<host>:<port>/http-bind/}
     *
     * @param hostname The hostname.
     * @param port     The port.
     * @param file     The file, which is needed to construct the URL, e.g. "/http-bind/".
     */
    public BoshConnection(String hostname, int port, String file) {
        this(hostname, port, file, Proxy.NO_PROXY, null, (short) 60);
    }

    /**
     * Creates a BOSH connection on basis of the XMPP service domain, host and port.
     * The BOSH URL used during connecting will be {@code http://<host>:<port>/http-bind/}
     *
     * @param hostname The hostname.
     * @param port     The port.
     * @param proxy    The proxy, which should be of type {@link java.net.Proxy.Type#HTTP}
     */
    public BoshConnection(String hostname, int port, Proxy proxy) {
        this(hostname, port, null, proxy, null, (short) 60);
    }

    /**
     * Creates a BOSH connection on basis of a host, port, file and proxy.
     * The BOSH URL used during connecting will be {@code http://<host>:<port>/<file>}
     *
     * @param hostname The hostname.
     * @param port     The port.
     * @param file     The file, which is needed to construct the URL, e.g. "/http-bind/".
     * @param proxy    The proxy, which should be of type {@link java.net.Proxy.Type#HTTP}
     */
    public BoshConnection(String hostname, int port, String file, Proxy proxy) {
        this(hostname, port, file, proxy, null, (short) 60);
    }

    /**
     * Creates a BOSH connection, using the specified host, port and file to create the URL in the form of {@code http://<host>:<port>/<file>}.
     * The initial session creation request will contain the specified route and content.
     *
     * @param hostname The host.
     * @param port     The port.
     * @param file     The file, which is needed to construct the URL, e.g. "/http-bind/".
     * @param proxy    The proxy, which should be of type {@link java.net.Proxy.Type#HTTP}
     * @param route    The route, formatted as "proto:host:port" (e.g., "xmpp:example.com:9999").
     * @param wait     The maximal number of seconds to wait between two requests. If a request exceeds this time an exception is thrown, which will terminate the connection.
     */
    public BoshConnection(String hostname, int port, String file, Proxy proxy, String route, short wait) {
        super(hostname, port, proxy);
        this.route = route;
        this.file = file;
        this.wait = wait;

        // Threads created by this thread pool, will be used to do simultaneous requests.
        // Even in the unusual case, where the connection manager allows for more requests, two are enough.
        requestExecutor = Executors.newFixedThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "XMPP BOSH request thread");
                thread.setDaemon(true);
                return thread;
            }
        });

        // The responses should be handled by a single thread.
        responseExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "XMPP BOSH response thread");
                thread.setDaemon(true);
                return thread;
            }
        });
        xmlOutputFactory = XMLOutputFactory.newFactory();
        xmlInputFactory = XMLInputFactory.newFactory();
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
                    throw new BoshException(Body.Condition.BAD_REQEST);
                case HttpURLConnection.HTTP_FORBIDDEN:
                    // Superseded by policy-violation
                    throw new BoshException(Body.Condition.POLICY_VIOLATION);
                case HttpURLConnection.HTTP_NOT_FOUND:
                    // Superseded by item-not-found
                    throw new BoshException(Body.Condition.ITEM_NOT_FOUND);
                default:
                    throw new BoshException(Body.Condition.UNDEFINED_CONDITION, httpCode);
            }
        }
    }

    /**
     * Tries to find the BOSH URL by a DNS TXT lookup as described in <a href="http://xmpp.org/extensions/xep-0156.html">XEP-0156</a>.
     *
     * @param xmppServiceDomain The fully qualified domain name.
     * @return The BOSH URL, if it could be found or null.
     * @throws javax.naming.NamingException If the context threw an exception.
     */
    private static String findBoshUrl(String xmppServiceDomain) throws NamingException {

        String query = "_xmppconnect." + xmppServiceDomain;

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        DirContext ctx = new InitialDirContext(env);

        Attributes attributes = ctx.getAttributes(query, new String[]{"TXT"});
        Attribute srvAttribute = attributes.get("TXT");

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
        return null;
    }

    @Override
    public synchronized void connect() throws IOException {
        // If a URL has not been set, try to find the URL by the domain via a DNS-TXT lookup as described in XEP-0156.
        if (url == null) {
            if (file == null) {
                file = "/http-bind/";
            }
            if (getHostname() != null) {
                url = new URL("http", getHostname(), getPort(), file);
            } else if (getXmppSession().getXmppServiceDomain() != null) {
                try {
                    url = new URL(findBoshUrl(getXmppSession().getXmppServiceDomain()));
                } catch (NamingException e) {
                    // Fallback mechanism:
                    // If the URL could not be resolved, use the domain name and port 80 as default.
                    url = new URL("http", getXmppSession().getXmppServiceDomain(), 80, file);
                }
            } else {
                throw new IllegalStateException("Neither an URL nor a domain given for a BOSH connection.");
            }
        }

        sessionId = null;

        // Set the initial request id with a large random number.
        // The largest possible number for a RID is (2^53)-1
        // So initialize it with a random number with max value of 2^52.
        // This will still allow for at least 4503599627370495 requests (2^53-1-2^52), which should be sufficient.
        rid.set(new BigInteger(52, new Random()).longValue());

        // Create initial request.
        Body body = new Body();
        body.setTo(getXmppSession().getXmppServiceDomain());
        body.setLanguage(Locale.getDefault().getLanguage());
        body.setVersion("1.10");
        body.setWait(wait);
        body.setHold((byte) 1);
        body.setRoute(route);
        body.setAck(1L);
        body.setXmppVersion("1.0");

        // Send the initial request.
        sendNewRequest(body, false);
    }

    /**
     * Gets the body element from the response and unpacks its content.
     * <p>
     * If it is the session creation response it contains additional attributes like the session id. These attributes are set to this connection.
     * </p>
     * The contents are delegated to the {@link org.xmpp.XmppSession#handleElement(Object)} method, where they are treated as normal XMPP elements, i.e. the same way as in a normal TCP connection.
     *
     * @param body The body.
     * @param rid  The request id, which was used for the request. If the body contains no 'ack' attribute, it means it's the response to the request with that 'rid'.
     * @throws Exception If any exception occurred during handling the inner XMPP elements.
     */
    private void unpackBody(Body body, long rid) throws Exception {
        // It's the session creation response.
        if (body.getSid() != null) {
            sessionId = body.getSid();

            if (body.getAck() != null) {
                usingAcknowledgments = true;
            }

            if (body.getFrom() != null) {
                getXmppSession().setXmppServiceDomain(body.getFrom().getDomain());
            }
        }

        highestReceivedRid = body.getRid() != null ? body.getRid() : rid;
        unacknowledgedRequests.remove(highestReceivedRid);

        // If the body contains an error condition, which is not a stream error, terminate the connection by throwing an exception.
        if (body.getType() == Body.Type.TERMINATE && body.getCondition() != null && body.getCondition() != Body.Condition.REMOTE_STREAM_ERROR) {
            throw new BoshException(body.getCondition(), body.getUri());
        } else if (body.getType() == Body.Type.ERROR) {
            // In any response it sends to the client, the connection manager MAY return a recoverable error by setting a 'type' attribute of the <body/> element to "error". These errors do not imply that the HTTP session is terminated.
            // If it decides to recover from the error, then the client MUST repeat the HTTP request that resulted in the error, as well as all the preceding HTTP requests that have not received responses. The content of these requests MUST be identical to the <body/> elements of the original requests. This enables the connection manager to recover a session after the previous request was lost due to a communication failure.
            for (Body unacknowledgedRequest : unacknowledgedRequests.values()) {
                sendNewRequest(unacknowledgedRequest, false);
            }
        }

        if (body.getWrappedObjects() != null) {
            for (Object wrappedObject : body.getWrappedObjects()) {
                getXmppSession().handleElement(wrappedObject);
            }
        }
    }

    /**
     * Nothing to do here.
     * <blockquote>
     * <p>The client SHOULD ignore any Transport Layer Security (TLS) feature since BOSH channel encryption SHOULD be negotiated at the HTTP layer.</p>
     * </blockquote>
     */
    @Override
    protected void secureConnection() {
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
    protected void restartStream() {
        Body body = new Body();
        body.setRestart(true);
        body.setTo(getXmppSession().getXmppServiceDomain());
        body.setLanguage(Locale.getDefault().getLanguage());
        body.setSid(getSessionId());
        sendNewRequest(body, false);
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
     * @throws IOException If the underlying HTTP connection threw an exception.
     */
    @Override
    public synchronized void close() throws IOException {
        if (!requestExecutor.isShutdown()) {
            // Terminate the BOSH session.
            Body body = new Body();
            body.setType(Body.Type.TERMINATE);
            sendNewRequest(body, true);

            // and then shut it down.
            requestExecutor.shutdown();
            try {
                // Wait shortly, until the "terminate" body has been sent.
                requestExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        // Then shutdown the response executor.
        if (!responseExecutor.isShutdown()) {
            responseExecutor.shutdown();
            try {
                // Wait shortly, if the connection manager responds with a <body type='terminate' xmlns='http://jabber.org/protocol/httpbind'/>.
                responseExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void send(ClientStreamElement element) {
        queue.add(element);
        sendNewRequest(new Body(), true);
    }

    /**
     * Gets the session id of this BOSH connection.
     *
     * @return The session id.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sends all elements waiting in the queue to the server.
     * <p>
     * If there are currently more requests than allowed by the server, the waiting elements will be send as soon one of the requests return.
     * </p>
     *
     * @param body        The wrapper body element.
     * @param addElements True, if waiting elements should be added to the body; false if an empty body shall be sent.
     */
    private void sendNewRequest(final Body body, final boolean addElements) {

        // Make sure, no two threads access this block, in order to ensure that requestCount and requestExecutor.isShutdown() don't return inconsistent values.
        synchronized (requestExecutor) {
            if (!requestExecutor.isShutdown()) {
                requestExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int httpResponseCode = 0;
                        // Open a HTTP connection.
                        HttpURLConnection httpConnection = null;
                        try {
                            synchronized (requestExecutor) {
                                requestCount++;

                                // Only put content in the body element, if it is allowed (e.g. it does not contain restart='true' and isn't an unacknowledged body isn't resent).
                                if (addElements) {
                                    for (Object object : queue) {
                                        body.getWrappedObjects().add(object);
                                    }
                                    queue.clear();
                                }

                                // Increment the request id.
                                body.setRid(rid.getAndIncrement());
                                body.setSid(getSessionId());

                                // The only exception is that, after its session creation request, the client SHOULD NOT include an 'ack' attribute in any request if it has received responses to all its previous requests.
                                if (!unacknowledgedRequests.isEmpty()) {
                                    body.setAck(highestReceivedRid);
                                }
                                if (usingAcknowledgments) {
                                    unacknowledgedRequests.put(body.getRid(), body);
                                }

                                httpConnection = (HttpURLConnection) url.openConnection(getProxy());
                                //httpConnection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                                httpConnection.setDoOutput(true);
                                httpConnection.setRequestMethod("POST");
                                // If the connection manager does not respond in time, throw a SocketTimeoutException, which terminates the connection.
                                httpConnection.setReadTimeout((wait + 5) * 1000);

                                // This is for logging only.
                                ByteArrayOutputStream byteArrayOutputStreamRequest = new ByteArrayOutputStream();

                                XMLStreamWriter xmlStreamWriter = null;
                                // Branch the stream, so that its output can also be logged.
                                try (OutputStream branchedOutputStream = XmppUtils.createBranchedOutputStream(httpConnection.getOutputStream(), byteArrayOutputStreamRequest)) {
                                    // Create the writer for this connection.
                                    xmlStreamWriter = XmppUtils.createXmppStreamWriter(xmlOutputFactory.createXMLStreamWriter(branchedOutputStream), true);
                                    // Then write the XML to the output stream by marshalling the object to the writer.
                                    getXmppSession().getMarshaller().marshal(body, xmlStreamWriter);

                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("--> " + new String(byteArrayOutputStreamRequest.toByteArray()));
                                    }
                                } finally {
                                    if (xmlStreamWriter != null) {
                                        xmlStreamWriter.close();
                                    }
                                }
                            }

                            // Wait for the response
                            if ((httpResponseCode = httpConnection.getResponseCode()) == HttpURLConnection.HTTP_OK) {
                                // This is for logging only.
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                XMLEventReader xmlEventReader = null;
                                // Branch the stream so that its input can be logged.
                                try (InputStream inputStream = XmppUtils.createBranchedInputStream(httpConnection.getInputStream(), byteArrayOutputStream)) {
                                    // Read the response.
                                    xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream);
                                    while (xmlEventReader.hasNext()) {
                                        XMLEvent xmlEvent = xmlEventReader.peek();

                                        // Parse the <body/> element.
                                        synchronized (responseExecutor) {
                                            if (xmlEvent.isStartElement()) {
                                                final JAXBElement<Body> element = getXmppSession().getUnmarshaller().unmarshal(xmlEventReader, Body.class);
                                                if (logger.isLoggable(Level.FINE)) {
                                                    logger.fine("<-- " + new String(byteArrayOutputStream.toByteArray()));
                                                }
                                                byteArrayOutputStream.reset();
                                                if (!responseExecutor.isShutdown()) {
                                                    responseExecutor.execute(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                unpackBody(element.getValue(), body.getRid());
                                                            } catch (Exception e) {
                                                                getXmppSession().notifyException(e);
                                                            }
                                                        }
                                                    });
                                                }
                                            } else {
                                                xmlEventReader.next();
                                            }
                                        }
                                    }
                                } finally {
                                    if (xmlEventReader != null) {
                                        xmlEventReader.close();
                                    }
                                }
                            } else {
                                handleCode(httpConnection.getResponseCode());
                            }
                        } catch (Exception e) {
                            getXmppSession().notifyException(e);
                        } finally {
                            if (httpConnection != null) {
                                httpConnection.disconnect();
                            }
                            synchronized (requestExecutor) {
                                // As soon as the client receives a response from the connection manager it sends another request, thereby ensuring that the connection manager is (almost) always holding a request that it can use to "push" data to the client.
                                if (--requestCount == 0 && httpResponseCode == HttpURLConnection.HTTP_OK) {
                                    sendNewRequest(new Body(), true);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * <blockquote>
     * <p>A connection manager MAY be configured to enable sessions with more than one server in different domains. When requesting a session with such a "proxy" connection manager, a client SHOULD include a 'route' attribute that specifies the protocol, hostname, and port of the server with which it wants to communicate, formatted as "proto:host:port" (e.g., "xmpp:example.com:9999").</p>
     * </blockquote>
     *
     * @return The route.
     */
    public String getRoute() {
        return route;
    }
}
