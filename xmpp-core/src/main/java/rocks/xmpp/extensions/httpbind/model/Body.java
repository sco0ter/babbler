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

package rocks.xmpp.extensions.httpbind.model;

import rocks.xmpp.core.Jid;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@code <body/>} element in the {@code http://jabber.org/protocol/httpbind} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0124.html">XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)</a>
 * @see <a href="http://xmpp.org/extensions/xep-0206.html">XEP-0206: XMPP Over BOSH</a>
 * @see <a href="http://xmpp.org/extensions/xep-0124.html#schema">XML Schema</a>
 */
@XmlRootElement
public final class Body {

    @XmlElement
    private URI uri;

    @XmlAnyElement(lax = true)
    private List<Object> wrappedObjects = new ArrayList<>();

    /**
     * The connection manager MAY include an 'accept' attribute in the session creation response element, to specify a space-separated list of the content encodings it can decompress. After receiving a session creation response with an 'accept' attribute, clients MAY include an HTTP Content-Encoding header in subsequent requests (indicating one of the encodings specified in the 'accept' attribute) and compress the bodies of the requests accordingly.
     */
    @XmlAttribute
    private String accept;

    /**
     * A connection manager MAY include an 'ack' attribute (set to the value of the 'rid' attribute of the session creation request) to indicate that it will be using acknowledgements throughout the session and that the absence of an 'ack' attribute in any response is meaningful (see Acknowledgements).
     */
    @XmlAttribute
    private Long ack;

    @XmlAttribute
    private String authid;

    @XmlAttribute
    private String charsets;

    @XmlAttribute
    private Condition condition;

    @XmlAttribute
    private String content;

    @XmlAttribute
    private Jid from;

    /**
     * This attribute informs the client about the maximum number of requests the connection manager will keep waiting at any one time during the session. This value MUST NOT be greater than the value specified by the client in the session request.
     */
    @XmlAttribute
    private Byte hold;

    /**
     * This attribute specifies the longest allowable inactivity period (in seconds). This enables the client to ensure that the periods with no requests pending are never too long (see Polling Sessions and Inactivity).
     */
    @XmlAttribute
    private Short inactivity;

    @XmlAttribute
    private String key;

    /**
     * If the connection manager supports session pausing (see Inactivity) then it SHOULD advertise that to the client by including a 'maxpause' attribute in the session creation response element. The value of the attribute indicates the maximum length of a temporary session pause (in seconds) that a client can request.
     */
    @XmlAttribute
    private Short maxpause;

    @XmlAttribute
    private String newkey;

    @XmlAttribute
    private Short pause;

    /**
     * This attribute specifies the shortest allowable polling interval (in seconds). This enables the client to not send empty request elements more often than desired (see Polling Sessions and Overactivity).
     */
    @XmlAttribute
    private Short polling;

    @XmlAttribute
    private Integer report;

    /**
     * This attribute enables the connection manager to limit the number of simultaneous requests the client makes (see Overactivity and Polling Sessions). The RECOMMENDED values are either "2" or one more than the value of the 'hold' attribute specified in the session request.
     */
    @XmlAttribute
    private Byte requests;

    @XmlAttribute
    private Long rid;

    @XmlAttribute
    private String route;

    @XmlAttribute
    private Boolean secure;

    @XmlAttribute
    private String sid;

    @XmlAttribute
    private String stream;

    @XmlAttribute
    private Short time;

    /**
     * This attribute communicates the identity of the backend server to which the client is attempting to connect.
     */
    @XmlAttribute
    private String to;

    @XmlAttribute
    private Type type;

    /**
     * This attribute specifies the highest version of the BOSH protocol that the connection manager supports, or the version specified by the client in its request, whichever is lower.
     */
    @XmlAttribute
    private String ver;

    @XmlAttribute
    private Integer wait;

    @XmlAttribute(namespace = XMLConstants.XML_NS_URI)
    private String lang;

    @XmlAttribute(namespace = "urn:xmpp:xbosh")
    private String version;

    @XmlAttribute(namespace = "urn:xmpp:xbosh")
    private Boolean restartLogic;

    @XmlAttribute(namespace = "urn:xmpp:xbosh")
    private Boolean restart;

    public Body(Object wrappedObject) {
        this.wrappedObjects = new ArrayList<>();
        this.wrappedObjects.add(wrappedObject);
    }

    public Body() {
    }

    public List<Object> getWrappedObjects() {
        return wrappedObjects;
    }

    /**
     * If the connection manager reports a {@link Condition#SEE_OTHER_URI} error condition, this method returns the URI.
     *
     * @return The URI.
     */
    public URI getUri() {
        return uri;
    }

    /**
     * <blockquote>
     * <p>The connection manager MAY include an 'accept' attribute in the session creation response element, to specify a comma-separated list of the content encodings it can decompress. After receiving a session creation response with an 'accept' attribute, clients MAY include an HTTP Content-Encoding header in subsequent requests (indicating one of the encodings specified in the 'accept' attribute) and compress the bodies of the requests accordingly.</p>
     * </blockquote>
     *
     * @return The comma-separated list of the content encodings.
     */
    public String getAccept() {
        return accept;
    }

    /**
     * <blockquote>
     * <p>A client MAY include an 'ack' attribute (set to "1") to indicate that it will be using acknowledgements throughout the session and that the absence of an 'ack' attribute in any request is meaningful.</p>
     * <p>When responding to a request that it has been holding, if the connection manager finds it has already received another request with a higher 'rid' attribute (typically while it was holding the first request), then it MAY acknowledge the reception to the client. The connection manager MAY set the 'ack' attribute of any response to the value of the highest 'rid' attribute it has received in the case where it has also received all requests with lower 'rid' values.</p>
     * </blockquote>
     *
     * @return The acknowledged request.
     */
    public Long getAck() {
        return ack;
    }

    /**
     * Sets the 'ack' value. The client only sets it to value 1.
     *
     * @param ack The ack value.
     */
    public void setAck(Long ack) {
        this.ack = ack;
    }

    /**
     * <blockquote>
     * <p>The connection manager MAY inform the client which encodings it can convert by setting the optional 'charsets' attribute in the session creation response element to a space-separated list of encodings.</p>
     * </blockquote>
     *
     * @return The available charsets.
     */
    public String getCharsets() {
        return charsets;
    }

    /**
     * <blockquote>
     * <p>The {@code <body/>} element of the first request MAY also possess a 'from' attribute, which specifies the originator of the first stream and which enables the connection manager to forward the originating entity's identity to the application server.</p>
     * </blockquote>
     *
     * @return The 'from' attribute.
     */
    public Jid getFrom() {
        return from;
    }

    /**
     * @param from The 'from' attribute.
     * @see #getFrom()
     */
    public void setFrom(Jid from) {
        this.from = from;
    }

    /**
     * <blockquote>
     * <p>This attribute informs the client about the maximum number of requests the connection manager will keep waiting at any one time during the session. This value MUST NOT be greater than the value specified by the client in the session request.</p>
     * </blockquote>
     *
     * @return The 'hold' attribute.
     */
    public Byte getHold() {
        return hold;
    }

    /**
     * <blockquote>
     * <p>The client SHOULD set the 'hold' attribute to a value of "1".</p>
     * </blockquote>
     *
     * @param hold The 'hold' attribute.
     */
    public void setHold(Byte hold) {
        this.hold = hold;
    }

    public Short getInactivity() {
        return inactivity;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * <blockquote>
     * <p>If the connection manager supports session pausing (see Inactivity) then it SHOULD advertise that to the client by including a 'maxpause' attribute in the session creation response element. The value of the attribute indicates the maximum length of a temporary session pause (in seconds) that a client can request.</p>
     * </blockquote>
     *
     * @return The maximal pause in seconds.
     */
    public Short getMaxPause() {
        return maxpause;
    }

    public String getNewKey() {
        return newkey;
    }

    public void setNewKey(String newKey) {
        this.newkey = newKey;
    }

    /**
     * <blockquote>
     * <p>If a client encounters an exceptional temporary situation during which it will be unable to send requests to the connection manager for a period of time greater than the maximum inactivity period (e.g., while a runtime environment changes from one web page to another), and if the connection manager included a 'maxpause' attribute in its Session Creation Response, then the client MAY request a temporary increase to the maximum inactivity period by including a 'pause' attribute in a request.</p>
     * </blockquote>
     *
     * @return The 'pause' attribute value.
     */
    public Short getPause() {
        return pause;
    }

    /**
     * @param pause The 'pause' attribute value.
     * @see #getPause()
     */
    public void setPause(Short pause) {
        this.pause = pause;
    }

    /**
     * <blockquote>
     * <p>This attribute specifies the shortest allowable polling interval (in seconds). This enables the client to not send empty request elements more often than desired.</p>
     * </blockquote>
     *
     * @return The 'polling' attribute value.
     */
    public Short getPolling() {
        return polling;
    }

    /**
     * <blockquote>
     * <p>After receiving a request with an 'ack' value less than the 'rid' of the last request that it has already responded to, the connection manager MAY inform the client of the situation by sending its next response immediately instead of waiting until it has payloads to send to the client (e.g., if some time has passed since it responded). In this case it SHOULD include a 'report' attribute set to one greater than the 'ack' attribute it received from the client, and a 'time' attribute set to the number of milliseconds since it sent the response associated with the 'report' attribute.</p>
     * <p>Upon reception of a response with 'report' and 'time' attributes, if the client has still not received the response associated with the request identifier specified by the 'report' attribute, then it MAY choose to resend the request associated with the missing response.</p>
     * </blockquote>
     *
     * @return The 'report' attribute value.
     */
    public Integer getReport() {
        return report;
    }

    /**
     * @param report The 'report' attribute value.
     * @see #getReport()
     */
    public void setReport(Integer report) {
        this.report = report;
    }

    /**
     * <blockquote>
     * <p>This attribute enables the connection manager to limit the number of simultaneous requests the client makes (see Overactivity and Polling Sessions). The RECOMMENDED values are either "2" or one more than the value of the 'hold' attribute specified in the session request.</p>
     * </blockquote>
     *
     * @return The 'requests' attribute value.
     */
    public Byte getRequests() {
        return requests;
    }

    /**
     * @param requests The 'requests' attribute value.
     * @see #getRequests()
     */
    public void setRequests(Byte requests) {
        this.requests = requests;
    }

    /**
     * <blockquote>
     * <p>The {@code <body/>} element of every client request MUST possess a sequential request ID encapsulated via the 'rid' attribute.</p>
     * </blockquote>
     *
     * @return The 'rid' attribute value.
     */
    public Long getRid() {
        return rid;
    }

    /**
     * @param rid The 'rid' attribute value.
     * @see #getRid()
     */
    public void setRid(Long rid) {
        this.rid = rid;
    }

    /**
     * <blockquote>
     * <p>A connection manager MAY be configured to enable sessions with more than one server in different domains. When requesting a session with such a "proxy" connection manager, a client SHOULD include a 'route' attribute that specifies the protocol, hostname, and port of the server with which it wants to communicate, formatted as "proto:host:port" (e.g., "xmpp:example.com:9999").</p>
     * </blockquote>
     *
     * @return The 'route' attribute value.
     */
    public String getRoute() {
        return route;
    }

    /**
     * @param route The 'route' attribute value.
     * @see #getRoute()
     */
    public void setRoute(String route) {
        this.route = route;
    }

    /**
     * <blockquote>
     * <p>All requests after the first one MUST include a valid 'sid' attribute (provided by the connection manager in the Session Creation Response). The initialization request is unique in that the {@code <body/>} element MUST NOT possess a 'sid' attribute.</p>
     * </blockquote>
     *
     * @return The 'route' attribute value.
     */
    public String getSid() {
        return sid;
    }

    /**
     * @param sid The 'route' attribute value.
     * @see #getSid()
     */
    public void setSid(String sid) {
        this.sid = sid;
    }

    /**
     * <blockquote>
     * <p>If a connection manager supports the multi-streams feature, it MUST include a 'stream' attribute in its Session Creation Response. If a client does not receive the 'stream' attribute then it MUST assume that the connection manager does not support the feature.</p>
     * </blockquote>
     *
     * @return The 'stream' attribute value.
     */
    public String getStream() {
        return stream;
    }

    /**
     * @param stream The 'stream' attribute value.
     * @see #getStream()
     */
    public void setStream(String stream) {
        this.stream = stream;
    }

    /**
     * @return The 'time' attribute value.
     * @see #getReport()
     */
    public Short getTime() {
        return time;
    }

    /**
     * @param time The 'time' attribute value.
     * @see #getTime()
     */
    public void setTime(Short time) {
        this.time = time;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * <blockquote>
     * <p>This attribute specifies the highest version of the BOSH protocol that the client supports. The numbering scheme is "&lt;major&gt;.&lt;minor&gt;" (where the minor number MAY be incremented higher than a single digit, so it MUST be treated as a separate integer). Note: The 'ver' attribute should not be confused with the version of any protocol being transported.</p>
     * </blockquote>
     *
     * @return The 'version' attribute value.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The 'version' attribute value.
     * @see #getVersion()
     */
    public void setVersion(String version) {
        this.ver = version;
    }

    public Integer getWait() {
        return wait;
    }

    public void setWait(Integer wait) {
        this.wait = wait;
    }

    public String getLanguage() {
        return lang;
    }

    public void setLanguage(String language) {
        this.lang = language;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getXmppVersion() {
        return version;
    }

    public void setXmppVersion(String xmppVersion) {
        this.version = xmppVersion;
    }

    public Boolean getRestartLogic() {
        return restartLogic;
    }

    public void setRestartLogic(Boolean restartLogic) {
        this.restartLogic = restartLogic;
    }

    public Boolean getRestart() {
        return restart;
    }

    public void setRestart(Boolean restart) {
        this.restart = restart;
    }

    /**
     * @return The content.
     * @see #setContent(String)
     */
    public String getContent() {
        return content;
    }

    /**
     * <blockquote>
     * <p>Some clients are constrained to only accept HTTP responses with specific Content-Types (e.g., "text/html"). The {@code <body/>} element of the first request MAY possess a 'content' attribute. This specifies the value of the HTTP Content-Type header that MUST appear in all the connection manager's responses during the session. If the client request does not possess a 'content' attribute, then the HTTP Content-Type header of responses MUST be "text/xml; charset=utf-8".</p>
     * </blockquote>
     *
     * @param content The content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * The implementation of the 'condition' attribute of the {@code <body/>} wrapper element, which indicates an error reported by the connection manager.
     */
    @XmlEnum
    public enum Condition {
        /**
         * The format of an HTTP header or binding element received from the client is unacceptable (e.g., syntax error).
         */
        @XmlEnumValue(value = "bad-request")
        BAD_REQUEST,
        /**
         * The target domain specified in the 'to' attribute or the target host or port specified in the 'route' attribute is no longer serviced by the connection manager.
         */
        @XmlEnumValue(value = "host-gone")
        HOST_GONE,
        /**
         * The target domain specified in the 'to' attribute or the target host or port specified in the 'route' attribute is unknown to the connection manager.
         */
        @XmlEnumValue(value = "host-unknown")
        HOST_UNKNOWN,
        /**
         * The initialization element lacks a 'to' or 'route' attribute (or the attribute has no value) but the connection manager requires one.
         */
        @XmlEnumValue(value = "improper-addressing")
        IMPROPER_ADDRESSING,
        /**
         * The connection manager has experienced an internal error that prevents it from servicing the request.
         */
        @XmlEnumValue(value = "internal-server-error")
        INTERNAL_SERVER_ERROR,
        /**
         * (1) 'sid' is not valid, (2) 'stream' is not valid, (3) 'rid' is larger than the upper limit of the expected window, (4) connection manager is unable to resend response, (5) 'key' sequence is invalid.
         */
        @XmlEnumValue(value = "item-not-found")
        ITEM_NOT_FOUND,
        /**
         * Another request being processed at the same time as this request caused the session to terminate.
         */
        @XmlEnumValue(value = "other-request")
        OTHER_REQUEST,
        /**
         * The client has broken the session rules (polling too frequently, requesting too frequently, sending too many simultaneous requests).
         */
        @XmlEnumValue(value = "policy-violation")
        POLICY_VIOLATION,
        /**
         * The connection manager was unable to connect to, or unable to connect securely to, or has lost its connection to, the server.
         */
        @XmlEnumValue(value = "remote-connection-failed")
        REMOTE_CONNECTION_FAILED,
        /**
         * Encapsulates an error in the protocol being transported.
         */
        @XmlEnumValue(value = "remote-stream-error")
        REMOTE_STREAM_ERROR,
        /**
         * The connection manager does not operate at this URI (e.g., the connection manager accepts only SSL or TLS connections at some https: URI rather than the http: URI requested by the client). The client can try POSTing to the URI in the content of the {@code <uri/>} child element.
         */
        @XmlEnumValue(value = "see-other-uri")
        SEE_OTHER_URI,
        /**
         * The connection manager is being shut down. All active HTTP sessions are being terminated. No new sessions can be created.
         */
        @XmlEnumValue(value = "system-shutdown")
        SYSTEM_SHUTDOWN,
        /**
         * The error is not one of those defined herein; the connection manager SHOULD include application-specific information in the content of the {@code <body/>} wrapper.
         */
        @XmlEnumValue(value = "undefined-condition")
        UNDEFINED_CONDITION;

        @Override
        public String toString() {
            return this.name().toLowerCase().replace("_", "-");
        }
    }

    /**
     * The implementation of the 'type' attribute of the {@code <body/>} element.
     */
    @XmlEnum
    public enum Type {
        /**
         * The connection manager has reported a recoverable binding error.
         */
        @XmlEnumValue(value = "error")
        ERROR,
        /**
         * The connection manager or the client has terminated the connection.
         */
        @XmlEnumValue(value = "terminate")
        TERMINATE
    }
}
