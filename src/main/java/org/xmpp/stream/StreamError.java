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

package org.xmpp.stream;

import org.xmpp.XmppError;

import javax.xml.bind.annotation.*;

/**
 * The implementation of the {@code <stream:error/>} element.
 * <p>
 * See <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error">4.9.  Stream Errors</a>
 * </p>
 */
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({StreamError.BadFormat.class, StreamError.BadNamespacePrefix.class, StreamError.Conflict.class, StreamError.ConnectionTimeout.class, StreamError.HostGone.class, StreamError.HostUnknown.class, StreamError.ImproperAddressing.class, StreamError.InternalServerError.class, StreamError.InvalidFrom.class, StreamError.InvalidNamespace.class, StreamError.InvalidXml.class, StreamError.NotAuthorized.class, StreamError.NotWellFormed.class, StreamError.PolicyViolation.class, StreamError.RemoteConnectionFailed.class, StreamError.Reset.class, StreamError.ResourceConstraint.class, StreamError.RestrictedXml.class, StreamError.SeeOtherHost.class, StreamError.SystemShutdown.class, StreamError.UndefinedCondition.class, StreamError.UnsupportedEncoding.class, StreamError.UnsupportedFeature.class, StreamError.UnsupportedStanzaType.class, StreamError.UnsupportedVersion.class})
public final class StreamError extends XmppError {

    private static final String ERROR_NAMESPACE = "urn:ietf:params:xml:ns:xmpp-streams";

    @XmlElement(namespace = ERROR_NAMESPACE)
    private Text text;

    @XmlAnyElement(lax = true)
    private Object extension;

    /**
     * Private default constructor for unmarshalling.
     */
    @SuppressWarnings("unused")
    private StreamError() {
    }

//    /**
//     * Creates a stream error with a given condition.
//     * <blockquote>
//     * <p>The {@code <error/>} element MUST contain a child element corresponding to one of the defined stream error conditions.</p>
//     * </blockquote>
//     *
//     * @param condition The condition.
//     */
//    public StreamError(Condition condition) {
//        super(condition);
//    }

    /**
     * Gets the language of the error text.
     *
     * @return The language.
     */
    @Override
    public final String getLanguage() {
        if (text != null) {
            return text.getLanguage();
        }
        return null;
    }

    /**
     * Gets the optional error text.
     *
     * @return The text.
     */
    @Override
    public final String getText() {
        if (text != null) {
            return text.getText();
        }
        return null;
    }

    /**
     * Sets the optional error text.
     *
     * @param text The text.
     * @see #setText(String, String)
     * @see #getText()
     */
    @Override
    public final void setText(String text) {
        if (text != null) {
            this.text = new Text(text, null);
        } else {
            this.text = null;
        }
    }

    /**
     * Sets the optional error text and a language.
     *
     * @param text     The text.
     * @param language The language.
     * @see #setText(String)
     * @see #getText()
     */
    @Override
    public final void setText(String text, String language) {
        if (text != null) {
            this.text = new Text(text, language);
        } else {
            this.text = null;
        }
    }

    /**
     * Gets the application specific condition, if any.
     * <p>
     * See also <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-app">4.9.4.  Application-Specific Conditions</a>
     * </p>
     *
     * @return The application specific condition.
     * @see #setExtension(Object)
     */
    public final Object getExtension() {
        return extension;
    }

    /**
     * Sets an application specific condition.
     *
     * @param extension The application specific condition.
     * @see #getExtension()
     */
    public final void setExtension(Object extension) {
        this.extension = extension;
    }

    /**
     * The implementation of the {@code <bad-format/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-bad-format">4.9.3.1.  bad-format</a></cite></p>
     * <p>The entity has sent XML that cannot be processed.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "bad-format", namespace = ERROR_NAMESPACE)
    public static final class BadFormat extends Condition {
        @Override
        public String toString() {
            return "bad-format";
        }
    }

    /**
     * The implementation of the {@code <bad-namespace-prefix/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-bad-namespace-prefix">4.9.3.2.  bad-namespace-prefix</a></cite></p>
     * <p>The entity has sent a namespace prefix that is unsupported, or has sent no namespace prefix on an element that needs such a prefix.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "bad-namespace-prefix", namespace = ERROR_NAMESPACE)
    public static final class BadNamespacePrefix extends Condition {
        @Override
        public String toString() {
            return "bad-namespace-prefix";
        }
    }

    /**
     * The implementation of the {@code <conflict/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-conflict">4.9.3.3.  conflict</a></cite></p>
     * <p>The server either (1) is closing the existing stream for this entity because a new stream has been initiated that conflicts with the existing stream, or (2) is refusing a new stream for this entity because allowing the new stream would conflict with an existing stream (e.g., because the server allows only a certain number of connections from the same IP address or allows only one server-to-server stream for a given domain pair as a way of helping to ensure in-order processing as described under Section 10.1).</p>
     * </blockquote>
     */
    @XmlRootElement(name = "conflict", namespace = ERROR_NAMESPACE)
    public static final class Conflict extends Condition {
        @Override
        public String toString() {
            return "conflict";
        }
    }

    /**
     * The implementation of the {@code <connection-timeout/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-connection-timeout">4.9.3.4.  connection-timeout</a></cite></p>
     * <p>One party is closing the stream because it has reason to believe that the other party has permanently lost the ability to communicate over the stream. The lack of ability to communicate can be discovered using various methods, such as whitespace keepalives as specified under Section 4.4, XMPP-level pings as defined in [XEP-0199], and XMPP Stream Management as defined in [XEP-0198].</p>
     * </blockquote>
     */
    @XmlRootElement(name = "connection-timeout", namespace = ERROR_NAMESPACE)
    public static final class ConnectionTimeout extends Condition {
        @Override
        public String toString() {
            return "connection-timeout";
        }
    }

    /**
     * The implementation of the {@code <host-gone/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-host-gone">4.9.3.5.  host-gone</a></cite></p>
     * <p>The value of the 'to' attribute provided in the initial stream header corresponds to an FQDN that is no longer serviced by the receiving entity.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "host-gone", namespace = ERROR_NAMESPACE)
    public static final class HostGone extends Condition {
        @Override
        public String toString() {
            return "host-gone";
        }
    }

    /**
     * The implementation of the {@code <host-unknown/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-host-unknown">4.9.3.6.  host-unknown</a></cite></p>
     * <p>The value of the 'to' attribute provided in the initial stream header does not correspond to an FQDN that is serviced by the receiving entity.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "host-unknown", namespace = ERROR_NAMESPACE)
    public static final class HostUnknown extends Condition {
        @Override
        public String toString() {
            return "host-unknown";
        }
    }

    /**
     * The implementation of the {@code <improper-addressing/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-improper-addressing">4.9.3.7.  improper-addressing</a></cite></p>
     * <p>A stanza sent between two servers lacks a 'to' or 'from' attribute, the 'from' or 'to' attribute has no value, or the value violates the rules for XMPP addresses.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "improper-addressing", namespace = ERROR_NAMESPACE)
    public static final class ImproperAddressing extends Condition {
        @Override
        public String toString() {
            return "improper-addressing";
        }
    }

    /**
     * The implementation of the {@code <internal-server-error/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-internal-server-error">4.9.3.8.  internal-server-error</a></cite></p>
     * <p>The server has experienced a misconfiguration or other internal error that prevents it from servicing the stream.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "internal-server-error", namespace = ERROR_NAMESPACE)
    public static final class InternalServerError extends Condition {
        @Override
        public String toString() {
            return "internal-server-error";
        }
    }

    /**
     * The implementation of the {@code <invalid-from/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-invalid-from">4.9.3.9.  invalid-from</a></cite></p>
     * <p>The data provided in a 'from' attribute does not match an authorized Jid or validated domain as negotiated (1) between two servers using SASL or Server Dialback, or (2) between a client and a server via SASL authentication and resource binding.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "invalid-from", namespace = ERROR_NAMESPACE)
    public static final class InvalidFrom extends Condition {
        @Override
        public String toString() {
            return "invalid-from";
        }
    }

    /**
     * The implementation of the {@code <invalid-namespace/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-invalid-namespace">4.9.3.10.  invalid-namespace</a></cite></p>
     * <p>The stream namespace name is something other than "http://etherx.jabber.org/streams" (see Section 11.2) or the content namespace declared as the default namespace is not supported (e.g., something other than "jabber:client" or "jabber:server").</p>
     * </blockquote>
     */
    @XmlRootElement(name = "invalid-namespace", namespace = ERROR_NAMESPACE)
    public static final class InvalidNamespace extends Condition {
        @Override
        public String toString() {
            return "invalid-namespace";
        }
    }

    /**
     * The implementation of the {@code <invalid-xml/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-invalid-xml">4.9.3.11.  invalid-xml</a></cite></p>
     * <p>The entity has sent invalid XML over the stream to a server that performs validation (see Section 11.4).</p>
     * </blockquote>
     */
    @XmlRootElement(name = "invalid-xml", namespace = ERROR_NAMESPACE)
    public static final class InvalidXml extends Condition {
        @Override
        public String toString() {
            return "invalid-xml";
        }
    }

    /**
     * The implementation of the {@code <not-authorized/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-not-authorized">4.9.3.12.  not-authorized</a></cite></p>
     * <p>The entity has attempted to send XML stanzas or other outbound data before the stream has been authenticated, or otherwise is not authorized to perform an action related to stream negotiation; the receiving entity MUST NOT process the offending data before sending the stream error.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "not-authorized", namespace = ERROR_NAMESPACE)
    public static final class NotAuthorized extends Condition {
        @Override
        public String toString() {
            return "not-authorized";
        }
    }

    /**
     * The implementation of the {@code <not-well-formed/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-not-well-formed">4.9.3.13.  not-well-formed</a></cite></p>
     * <p>The initiating entity has sent XML that violates the well-formedness rules of [XML] or [XML-NAMES].</p>
     * </blockquote>
     */
    @XmlRootElement(name = "not-well-formed", namespace = ERROR_NAMESPACE)
    public static final class NotWellFormed extends Condition {
        @Override
        public String toString() {
            return "not-well-formed";
        }
    }

    /**
     * The implementation of the {@code <policy-violation/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-policy-violation">4.9.3.14.  policy-violation</a></cite></p>
     * <p>The entity has violated some local service policy (e.g., a stanza exceeds a configured size limit); the server MAY choose to specify the policy in the {@code <text/>} element or in an application-specific condition element.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "policy-violation", namespace = ERROR_NAMESPACE)
    public static final class PolicyViolation extends Condition {
        @Override
        public String toString() {
            return "policy-violation";
        }
    }

    /**
     * The implementation of the {@code <remote-connection-failed/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-remote-connection-failed">4.9.3.15.  remote-connection-failed</a></cite></p>
     * <p>The server is unable to properly connect to a remote entity that is needed for authentication or authorization (e.g., in certain scenarios related to Server Dialback [XEP-0220]); this condition is not to be used when the cause of the error is within the administrative domain of the XMPP service provider, in which case the {@code <internal-server-error/>} condition is more appropriate.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "remote-connection-failed", namespace = ERROR_NAMESPACE)
    public static final class RemoteConnectionFailed extends Condition {
        @Override
        public String toString() {
            return "remote-connection-failed";
        }
    }

    /**
     * The implementation of the {@code <reset/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-reset">4.9.3.16.  reset</a></cite></p>
     * <p>The server is closing the stream because it has new (typically security-critical) features to offer, because the keys or certificates used to establish a secure context for the stream have expired or have been revoked during the life of the stream (Section 13.7.2.3), because the TLS sequence number has wrapped (Section 5.3.5), etc. The reset applies to the stream and to any security context established for that stream (e.g., via TLS and SASL), which means that encryption and authentication need to be negotiated again for the new stream (e.g., TLS session resumption cannot be used).</p>
     * </blockquote>
     */
    @XmlRootElement(name = "reset", namespace = ERROR_NAMESPACE)
    public static final class Reset extends Condition {
        @Override
        public String toString() {
            return "reset";
        }
    }

    /**
     * The implementation of the {@code <resource-constraint/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-resource-constraint">4.9.3.17.  resource-constraint</a></cite></p>
     * <p>The server lacks the system resources necessary to service the stream.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "resource-constraint", namespace = ERROR_NAMESPACE)
    public static final class ResourceConstraint extends Condition {
        @Override
        public String toString() {
            return "resource-constraint";
        }
    }

    /**
     * The implementation of the {@code <restricted-xml/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-restricted-xml">4.9.3.18.  restricted-xml</a></cite></p>
     * <p>The entity has attempted to send restricted XML features such as a comment, processing instruction, DTD subset, or XML entity reference (see Section 11.1).</p>
     * </blockquote>
     */
    @XmlRootElement(name = "restricted-xml", namespace = ERROR_NAMESPACE)
    public static final class RestrictedXml extends Condition {
        @Override
        public String toString() {
            return "restricted-xml";
        }
    }

    /**
     * The implementation of the {@code <see-other-host/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-see-other-host">4.9.3.19.  see-other-host</a></cite></p>
     * <p>The server will not provide service to the initiating entity but is redirecting traffic to another host under the administrative control of the same service provider. The XML character data of the {@code <see-other-host/>} element returned by the server MUST specify the alternate FQDN or IP address at which to connect, which MUST be a valid domainpart or a domainpart plus port number (separated by the ':' character in the form "domainpart:port"). If the domainpart is the same as the source domain, derived domain, or resolved IPv4 or IPv6 address to which the initiating entity originally connected (differing only by the port number), then the initiating entity SHOULD simply attempt to reconnect at that address. (The format of an IPv6 address MUST follow [IPv6-ADDR], which includes the enclosing the IPv6 address in square brackets '[' and ']' as originally defined by [URI].) Otherwise, the initiating entity MUST resolve the FQDN specified in the {@code <see-other-host/>} element as described under Section 3.2.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "see-other-host", namespace = ERROR_NAMESPACE)
    public static final class SeeOtherHost extends Condition {
        public String getOtherHost() {
            return value;
        }
        @Override
        public String toString() {
            return "see-other-host";
        }
    }

    /**
     * The implementation of the {@code <system-shutdown/>} stream error.<blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-system-shutdown">4.9.3.20.  system-shutdown</a></cite></p>
     * <p>The server is being shut down and all active streams are being statusChanged.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "system-shutdown", namespace = ERROR_NAMESPACE)
    public static final class SystemShutdown extends Condition {
        @Override
        public String toString() {
            return "system-shutdown";
        }
    }

    /**
     * The implementation of the {@code <bad-format/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-undefined-condition">4.9.3.21.  undefined-condition</a></cite></p>
     * <p>The error condition is not one of those defined by the other conditions in this list; this error condition SHOULD NOT be used except in conjunction with an application-specific condition.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "undefined-condition", namespace = ERROR_NAMESPACE)
    public static final class UndefinedCondition extends Condition {
        @Override
        public String toString() {
            return "undefined-condition";
        }
    }

    /**
     * The implementation of the {@code <unsupported-encoding/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-unsupported-encoding">4.9.3.22.  unsupported-encoding</a></cite></p>
     * <p>The initiating entity has encoded the stream in an encoding that is not supported by the server (see Section 11.6) or has otherwise improperly encoded the stream (e.g., by violating the rules of the [UTF-8] encoding).</p>
     * </blockquote>
     */
    @XmlRootElement(name = "unsupported-encoding", namespace = ERROR_NAMESPACE)
    public static final class UnsupportedEncoding extends Condition {
        @Override
        public String toString() {
            return "unsupported-encoding";
        }
    }

    /**
     * The implementation of the {@code <unsupported-feature/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-unsupported-feature">4.9.3.23.  unsupported-feature</a></cite></p>
     * <p>The receiving entity has advertised a mandatory-to-negotiate stream feature that the initiating entity does not support, and has offered no other mandatory-to-negotiate feature alongside the unsupported feature.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "unsupported-feature", namespace = ERROR_NAMESPACE)
    public static final class UnsupportedFeature extends Condition {
        @Override
        public String toString() {
            return "unsupported-feature";
        }
    }

    /**
     * The implementation of the {@code <unsupported-stanza-type/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-unsupported-stanza-type">4.9.3.24.  unsupported-stanza-type</a></cite></p>
     * <p>The initiating entity has sent a first-level child of the stream that is not supported by the server, either because the receiving entity does not understand the namespace or because the receiving entity does not understand the element name for the applicable namespace (which might be the content namespace declared as the default namespace).</p>
     * </blockquote>
     */
    @XmlRootElement(name = "unsupported-stanza-type", namespace = ERROR_NAMESPACE)
    public static final class UnsupportedStanzaType extends Condition {
        @Override
        public String toString() {
            return "unsupported-stanza-type";
        }
    }

    /**
     * The implementation of the {@code <unsupported-version/>} stream error.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-unsupported-version">4.9.3.25.  unsupported-version</a></cite></p>
     * <p>The 'version' attribute provided by the initiating entity in the stream header specifies a version of XMPP that is not supported by the server.</p>
     * </blockquote>
     */
    @XmlRootElement(name = "unsupported-version", namespace = ERROR_NAMESPACE)
    public static final class UnsupportedVersion extends Condition {
        @Override
        public String toString() {
            return "unsupported-version";
        }
    }
}