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

package org.xmpp.stanza;

import org.xmpp.Jid;
import org.xmpp.XmppError;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The abstract base class for a XML stanza.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas">8.  XML Stanzas</a></cite>
 * <p>After a client and a server (or two servers) have completed stream negotiation, either party can send XML stanzas. Three kinds of XML stanza are defined for the 'jabber:client' and 'jabber:server' namespaces: {@code <message/>}, {@code <presence/>}, and {@code <iq/>}.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlTransient
public abstract class Stanza {

    @XmlAttribute
    protected Jid to;

    @XmlAttribute
    protected Jid from;

    @XmlAttribute
    protected String id;

    @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
    protected String language;

    @XmlElement
    private Error error;

    /**
     * Gets the stanza's 'to' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-to">8.1.1.  to</a></cite></p>
     * <p>The 'to' attribute specifies the JID of the intended recipient for the stanza.</p>
     * </blockquote>
     *
     * @return The JID.
     * @see #setTo(org.xmpp.Jid)
     */
    public final Jid getTo() {
        return to;
    }

    /**
     * Sets the stanza's 'to' attribute.
     *
     * @param to The JID.
     * @see #getTo()
     */
    public final void setTo(Jid to) {
        this.to = to;
    }

    /**
     * Gets the stanza's 'id' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-id">8.1.3.  id</a></cite></p>
     * <p>The 'id' attribute is used by the originating entity to track any response or error stanza that it might receive in relation to the generated stanza from another entity (such as an intermediate server or the intended recipient).</p>
     * <p>It is up to the originating entity whether the value of the 'id' attribute is unique only within its current stream or unique globally.</p>
     * <p>For {@code <message/>} and {@code <presence/>} stanzas, it is RECOMMENDED for the originating entity to include an 'id' attribute; for {@code <iq/>} stanzas, it is REQUIRED.</p>
     * <p>If the generated stanza includes an 'id' attribute then it is REQUIRED for the response or error stanza to also include an 'id' attribute, where the value of the 'id' attribute MUST match that of the generated stanza.</p>
     * </blockquote>
     *
     * @return The id.
     * @see #setId(String)
     */
    public final String getId() {
        return id;
    }

    /**
     * Sets the stanza's 'id' attribute.
     *
     * @param id The id.
     * @see #getId()
     */
    public final void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the stanza's 'from' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-from">8.1.2.  from</a></cite></p>
     * <p>The 'from' attribute specifies the JID of the sender.</p>
     * </blockquote>
     *
     * @return The JID.
     * @see #setFrom(org.xmpp.Jid)
     */
    public final Jid getFrom() {
        return from;
    }

    /**
     * Sets the stanza's 'from' attribute.
     *
     * @param from The JID.
     * @see #getFrom()
     */
    public final void setFrom(Jid from) {
        this.from = from;
    }

    /**
     * Gets the stanza's 'xml:lang' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-lang">8.1.5.  xml:lang</a></cite></p>
     * <p>A stanza SHOULD possess an 'xml:lang' attribute (as defined in Section 2.12 of [XML]) if the stanza contains XML character data that is intended to be presented to a human user (as explained in [CHARSETS], "internationalization is for humans"). The value of the 'xml:lang' attribute specifies the default language of any such human-readable XML character data.</p>
     * </blockquote>
     *
     * @return The language.
     * @see #setLanguage(String)
     */
    public final String getLanguage() {
        return language;
    }

    /**
     * Sets the stanza's 'xml:lang' attribute.
     *
     * @param language The language.
     * @see #getLanguage()
     */
    public final void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets the stanza's 'error' element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error">8.3.  Stanza Errors</a></cite></p>
     * <div>
     * Stanza-related errors are handled in a manner similar to stream errors. Unlike stream errors, stanza errors are recoverable; therefore, they do not result in termination of the XML stream and underlying TCP connection. Instead, the entity that discovers the error condition returns an error stanza, which is a stanza that:
     * <ul>
     * <li>is of the same kind (message, presence, or IQ) as the generated stanza that triggered the error</li>
     * <li>has a 'type' attribute set to a value of "error"</li>
     * <li>typically swaps the 'from' and 'to' addresses of the generated stanza</li>
     * <li>mirrors the 'id' attribute (if any) of the generated stanza that triggered the error</li>
     * <li>contains an {@code <error/>} child element that specifies the error condition and therefore provides a hint regarding actions that the sender might be able to take in an effort to remedy the error (however, it is not always possible to remedy the error)</li>
     * </ul>
     * </div>
     * </blockquote>
     *
     * @return The error.
     * @see #setError(Stanza.Error)
     */
    public final Error getError() {
        return error;
    }

    /**
     * Sets the stanza's 'error' element.
     *
     * @param error The JID.
     * @see #getError()
     */
    public final void setError(Error error) {
        this.error = error;
    }

    /**
     * Creates an error response for this stanza.
     *
     * @param error The error which is appended to the stanza.
     * @return The error response.
     * @see #getError()
     * @see #createError(Stanza, Stanza.Error)
     */
    public abstract Stanza createError(Error error);

    /**
     * Swaps the 'from' and 'to' addresses from the generated stanza and adds a 'by' attribute to the error.
     *
     * @param stanza The stanza to create the error for.
     * @param error  The error.
     * @see #createError(Error)
     */
    protected void createError(Stanza stanza, Error error) {
        stanza.setId(id);
        stanza.setError(error);
        // The entity that returns an error stanza MAY pass along its JID to the sender of the generated stanza (e.g., for diagnostic or tracking purposes) through the addition of a 'by' attribute to the <error/> child element.
        error.setBy(to);
        stanza.setFrom(to);
        stanza.setTo(from);
    }

    public abstract <T> T getExtension(Class<T> type);

    /**
     * The implementation of a stanza's {@code <error/>} element.
     * <p>
     * See <a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-syntax">8.3.2.  Syntax</a>
     * </p>
     */
    @XmlRootElement(name = "error")
    @XmlSeeAlso({Error.BadRequest.class, Error.Conflict.class, Error.FeatureNotImplemented.class, Error.Conflict.class, Error.Forbidden.class, Error.Gone.class, Error.InternalServerError.class, Error.ItemNotFound.class, Error.JidMalformed.class, Error.NotAcceptable.class, Error.NotAllowed.class, Error.NotAuthorized.class, Error.PolicyViolation.class, Error.RecipientUnavailable.class, Error.Redirect.class, Error.RegistrationRequired.class, Error.RemoteServerNotFound.class, Error.RemoteServerTimeout.class, Error.ResourceConstraint.class, Error.ServiceUnavailable.class, Error.SubscriptionRequired.class, Error.UndefinedCondition.class, Error.UnexpectedRequest.class})
    public static final class Error extends XmppError {

        private static final String ERROR_NAMESPACE = "urn:ietf:params:xml:ns:xmpp-stanzas";

        private static final Map<Class<? extends Condition>, Type> associatedErrorType = new HashMap<>();

        @XmlAttribute(name = "by")
        private Jid by;

        @XmlAttribute(name = "type")
        private Type type;

        @XmlElement(namespace = ERROR_NAMESPACE)
        private Text text;

        @XmlAnyElement(lax = true)
        private Object extension;

        /**
         * Private default constructor for unmarshalling.
         * <p/>
         * By default set an undefined condition.
         * <blockquote><p>The "defined-condition" MUST correspond to one of the stanza error conditions defined under Section 8.3.3. However, because additional error conditions might be defined in the future, if an entity receives a stanza error condition that it does not understand then it MUST treat the unknown condition as equivalent to {@code <undefined-condition/>}</p></blockquote>
         */
        @SuppressWarnings("unused")
        private Error() {
            super(new UndefinedCondition());
        }

        /**
         * Creates an error with a given error type and a condition.
         * <blockquote><p>The {@code <error/>} element MUST contain a defined condition element.</p></blockquote>
         *
         * @param type      The error type.
         * @param condition The condition.
         */
        public Error(Type type, Condition condition) {
            super(condition);
            if (type == null) {
                throw new IllegalArgumentException("type must not be null.");
            }
            this.type = type;
        }

        /**
         * Creates an error with a given error type and a condition.
         * <blockquote><p>The {@code <error/>} element MUST contain a defined condition element.</p></blockquote>
         *
         * @param type      The error type.
         * @param condition The condition.
         * @param text      The text.
         */
        public Error(Type type, Condition condition, String text) {
            super(condition);
            if (type == null) {
                throw new IllegalArgumentException("type must not be null.");
            }
            this.type = type;
            setText(text);
        }

        /**
         * Creates an error with a given condition.
         * <p>
         * The error type is set by the condition's associated error type.
         * </p>
         *
         * @param condition The condition.
         */
        public Error(Condition condition) {
            super(condition);
            this.type = associatedErrorType.get(condition.getClass());
        }

        /**
         * Creates an error with a given condition and text.
         * <p>
         * The error type is set by the condition's associated error type.
         * </p>
         *
         * @param condition The condition.
         * @param text      The text.
         */
        public Error(Condition condition, String text) {
            super(condition);
            this.type = associatedErrorType.get(condition.getClass());
            setText(text);
        }

        /**
         * Gets the 'by' attribute.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-rules">8.3.1.  Rules</a></cite></p>
         * The entity that returns an error stanza MAY pass along its JID to the sender of the generated stanza (e.g., for diagnostic or tracking purposes) through the addition of a 'by' attribute to the {@code <error/>} child element.
         * </blockquote>
         *
         * @return The JID.
         * @see #setBy(org.xmpp.Jid)
         */
        public final Jid getBy() {
            return by;
        }

        /**
         * Sets the 'by' attribute.
         *
         * @param by The JID.
         * @see #getBy()
         */
        public final void setBy(Jid by) {
            this.by = by;
        }

        /**
         * Gets the error type, either 'auth', 'cancel', 'continue', 'modify' or 'wait'.
         *
         * @return The type.
         */
        public final Type getType() {
            return type;
        }

        /**
         * Gets the optional error text.
         *
         * @return The text.
         * @see #setText(String)
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
            setText(text, null);
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
         * Gets the language of the error text.
         *
         * @return The language.
         * @see #setText(String, String)
         */
        @Override
        public final String getLanguage() {
            if (text != null) {
                return text.getLanguage();
            }
            return null;
        }

        /**
         * Gets the application specific condition, if any.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-app">8.3.4.  Application-Specific Conditions</a></cite></p>
         * <p>As noted, an application MAY provide application-specific stanza error information by including a properly namespaced child within the error element. Typically, the application-specific element supplements or further qualifies a defined element. Thus, the {@code <error/>} element will contain two or three child elements.</p>
         * </blockquote>
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
         * Represents a {@code <error/>} 'type' attribute.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-syntax">8.3.2.  Syntax</a></cite></p>
         * <div>
         * The "error-type" MUST be one of the following:
         * <ul>
         * <li>auth -- retry after providing credentials</li>
         * <li>cancel -- do not retry (the error cannot be remedied)</li>
         * <li>continue -- proceed (the condition was only a warning)</li>
         * <li>modify -- retry after changing the data sent</li>
         * <li>wait -- retry after waiting (the error is temporary)</li>
         * </ul>
         * </div>
         * </blockquote>
         */
        @XmlEnum
        public enum Type {
            /**
             * Retry after providing credentials.
             */
            @XmlEnumValue(value = "auth")
            AUTH,
            /**
             * Do not retry (the error cannot be remedied).
             */
            @XmlEnumValue(value = "cancel")
            CANCEL,
            /**
             * Proceed (the condition was only a warning).
             */
            @XmlEnumValue(value = "continue")
            CONTINUE,
            /**
             * Retry after changing the data sent.
             */
            @XmlEnumValue(value = "modify")
            MODIFY,
            /**
             * Retry after waiting (the error is temporary).
             */
            @XmlEnumValue(value = "wait")
            WAIT
        }

        /**
         * The implementation of the {@code <bad-request/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-bad-request">8.3.3.1.  bad-request</a></cite></p>
         * <p>The sender has sent a stanza containing XML that does not conform to the appropriate schema or that cannot be processed (e.g., an IQ stanza that includes an unrecognized value of the 'type' attribute, or an element that is qualified by a recognized namespace but that violates the defined syntax for the element); the associated error type SHOULD be "modify".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "bad-request", namespace = ERROR_NAMESPACE)
        public static final class BadRequest extends Condition {
            static {
                associatedErrorType.put(BadRequest.class, Type.MODIFY);
            }

            @Override
            public String toString() {
                return "bad-request";
            }
        }

        /**
         * The implementation of the {@code <conflict/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-conflict">8.3.3.2.  conflict</a></cite></p>
         * <p>Access cannot be granted because an existing resource exists with the same name or address; the associated error type SHOULD be "cancel".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "conflict", namespace = ERROR_NAMESPACE)
        public static final class Conflict extends Condition {
            static {
                associatedErrorType.put(Conflict.class, Type.CANCEL);
            }

            private Conflict() {
            }

            @Override
            public String toString() {
                return "conflict";
            }
        }

        /**
         * The implementation of the {@code <feature-not-implemented/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-feature-not-implemented">8.3.3.3.  feature-not-implemented</a></cite></p>
         * <p>The feature represented in the XML stanza is not implemented by the intended recipient or an intermediate server and therefore the stanza cannot be processed (e.g., the entity understands the namespace but does not recognize the element name); the associated error type SHOULD be "cancel" or "modify".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "feature-not-implemented", namespace = ERROR_NAMESPACE)
        public static final class FeatureNotImplemented extends Condition {
            static {
                associatedErrorType.put(FeatureNotImplemented.class, Type.CANCEL);
            }

            @Override
            public String toString() {
                return "feature-not-implemented";
            }
        }

        /**
         * The implementation of the {@code <forbidden/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-forbidden">8.3.3.4.  forbidden</a></cite></p>
         * <p>The requesting entity does not possess the necessary permissions to perform an action that only certain authorized roles or individuals are allowed to complete (i.e., it typically relates to authorization rather than authentication); the associated error type SHOULD be "auth".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "forbidden", namespace = ERROR_NAMESPACE)
        public static final class Forbidden extends Condition {
            static {
                associatedErrorType.put(Forbidden.class, Type.AUTH);
            }

            private Forbidden() {
            }

            @Override
            public String toString() {
                return "forbidden";
            }
        }

        /**
         * The implementation of the {@code <gone/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-gone">8.3.3.5.  gone</a></cite></p>
         * <p>The recipient or server can no longer be contacted at this address, typically on a permanent basis (as opposed to the {@code <redirect/>} error condition, which is used for temporary addressing failures); the associated error type SHOULD be "cancel" and the error stanza SHOULD include a new address (if available) as the XML character data of the {@code <gone/>} element (which MUST be a Uniform Resource Identifier [URI] or Internationalized Resource Identifier [IRI] at which the entity can be contacted, typically an XMPP IRI as specified in [XMPP-URI]).</p>
         * </blockquote>
         */
        @XmlRootElement(name = "gone", namespace = ERROR_NAMESPACE)
        public static final class Gone extends Condition {
            static {
                associatedErrorType.put(Gone.class, Type.CANCEL);
            }

            private Gone() {
            }

            /**
             * Gets the new address.
             *
             * @return The new address.
             */
            public String getNewAddress() {
                return value;
            }

            @Override
            public String toString() {
                return "gone";
            }
        }

        /**
         * The implementation of the {@code <internal-server-error/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-internal-server-error">8.3.3.6.  internal-server-error</a></cite></p>
         * <p>The server has experienced a misconfiguration or other internal error that prevents it from processing the stanza; the associated error type SHOULD be "cancel".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "internal-server-error", namespace = ERROR_NAMESPACE)
        public static final class InternalServerError extends Condition {
            static {
                associatedErrorType.put(InternalServerError.class, Type.CANCEL);
            }

            private InternalServerError() {
            }

            @Override
            public String toString() {
                return "internal-server-error";
            }
        }

        /**
         * The implementation of the {@code <item-not-found/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-item-not-found">8.3.3.7.  item-not-found</a></cite></p>
         * <p>The addressed JID or item requested cannot be found; the associated error type SHOULD be "cancel".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "item-not-found", namespace = ERROR_NAMESPACE)
        public static final class ItemNotFound extends Condition {
            static {
                associatedErrorType.put(ItemNotFound.class, Type.CANCEL);
            }

            public ItemNotFound() {
            }

            @Override
            public String toString() {
                return "item-not-found";
            }
        }

        /**
         * The implementation of the {@code <jid-malformed/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-jid-malformed">8.3.3.8.  jid-malformed</a></cite></p>
         * <p>The sending entity has provided (e.g., during resource binding) or communicated (e.g., in the 'to' address of a stanza) an XMPP address or aspect thereof that violates the rules defined in [XMPP-ADDR]; the associated error type SHOULD be "modify".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "jid-malformed", namespace = ERROR_NAMESPACE)
        public static final class JidMalformed extends Condition {
            static {
                associatedErrorType.put(JidMalformed.class, Type.MODIFY);
            }

            private JidMalformed() {
            }

            @Override
            public String toString() {
                return "jid-malformed";
            }
        }

        /**
         * The implementation of the {@code <not-acceptable/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-not-acceptable">8.3.3.9.  not-acceptable</a></cite></p>
         * <p>The recipient or server understands the request but cannot process it because the request does not meet criteria defined by the recipient or server (e.g., a request to subscribe to information that does not simultaneously include configuration parameters needed by the recipient); the associated error type SHOULD be "modify".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "not-acceptable", namespace = ERROR_NAMESPACE)
        public static final class NotAcceptable extends Condition {
            static {
                associatedErrorType.put(NotAcceptable.class, Type.MODIFY);
            }

            @Override
            public String toString() {
                return "not-acceptable";
            }
        }

        /**
         * The implementation of the {@code <not-allowed/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-not-allowed">8.3.3.10.  not-allowed</a></cite></p>
         * <p>The recipient or server does not allow any entity to perform the action (e.g., sending to entities at a blacklisted domain); the associated error type SHOULD be "cancel".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "not-allowed", namespace = ERROR_NAMESPACE)
        public static final class NotAllowed extends Condition {
            static {
                associatedErrorType.put(NotAllowed.class, Type.CANCEL);
            }

            @Override
            public String toString() {
                return "not-allowed";
            }
        }

        /**
         * The implementation of the {@code <not-authorized/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-not-authorized">8.3.3.11.  not-authorized</a></cite></p>
         * <p>The sender needs to provide credentials before being allowed to perform the action, or has provided improper credentials (the name "not-authorized", which was borrowed from the "401 Unauthorized" error of [HTTP], might lead the reader to think that this condition relates to authorization, but instead it is typically used in relation to authentication); the associated error type SHOULD be "auth".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "not-authorized", namespace = ERROR_NAMESPACE)
        public static final class NotAuthorized extends Condition {
            static {
                associatedErrorType.put(NotAuthorized.class, Type.AUTH);
            }

            private NotAuthorized() {
            }

            @Override
            public String toString() {
                return "not-authorized";
            }
        }

        /**
         * The implementation of the {@code <policy-violation/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-policy-violation">8.3.3.12.  policy-violation</a></cite></p>
         * <p>The entity has violated some local service policy (e.g., a message contains words that are prohibited by the service) and the server MAY choose to specify the policy in the {@code <text/>} element or in an application-specific condition element; the associated error type SHOULD be "modify" or "wait" depending on the policy being violated.</p>
         * </blockquote>
         */
        @XmlRootElement(name = "policy-violation", namespace = ERROR_NAMESPACE)
        public static final class PolicyViolation extends Condition {
            static {
                associatedErrorType.put(PolicyViolation.class, Type.MODIFY);
            }

            private PolicyViolation() {
            }

            @Override
            public String toString() {
                return "policy-violation";
            }
        }

        /**
         * The implementation of the {@code <recipient-unavailable/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-recipient-unavailable">8.3.3.13.  recipient-unavailable</a></cite></p>
         * <p>The intended recipient is temporarily unavailable, undergoing maintenance, etc.; the associated error type SHOULD be "wait".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "recipient-unavailable", namespace = ERROR_NAMESPACE)
        public static final class RecipientUnavailable extends Condition {
            static {
                associatedErrorType.put(RecipientUnavailable.class, Type.WAIT);
            }

            private RecipientUnavailable() {
            }

            @Override
            public String toString() {
                return "recipient-unavailable";
            }
        }

        /**
         * The implementation of the {@code <redirect/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-redirect">8.3.3.14.  redirect</a></cite></p>
         * <p>The recipient or server is redirecting requests for this information to another entity, typically in a temporary fashion (as opposed to the {@code <gone/>} error condition, which is used for permanent addressing failures); the associated error type SHOULD be "modify" and the error stanza SHOULD contain the alternate address in the XML character data of the {@code <redirect/>} element (which MUST be a URI or IRI with which the sender can communicate, typically an XMPP IRI as specified in [XMPP-URI]).</p>
         * </blockquote>
         */
        @XmlRootElement(name = "redirect", namespace = ERROR_NAMESPACE)
        public static final class Redirect extends Condition {
            static {
                associatedErrorType.put(Redirect.class, Type.MODIFY);
            }

            private Redirect() {
            }

            /**
             * Gets the alternate address.
             *
             * @return The alternate address.
             */
            public String getAlternateAddress() {
                return value;
            }

            @Override
            public String toString() {
                return "redirect";
            }
        }

        /**
         * The implementation of the {@code <registration-required/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-registration-required">8.3.3.15.  registration-required</a></cite></p>
         * <p>The requesting entity is not authorized to access the requested service because prior registration is necessary (examples of prior registration include members-only rooms in XMPP multi-user chat [XEP-0045] and gateways to non-XMPP instant messaging services, which traditionally required registration in order to use the gateway [XEP-0100]); the associated error type SHOULD be "auth".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "registration-required", namespace = ERROR_NAMESPACE)
        public static final class RegistrationRequired extends Condition {
            static {
                associatedErrorType.put(RegistrationRequired.class, Type.AUTH);
            }

            private RegistrationRequired() {
            }

            @Override
            public String toString() {
                return "registration-required";
            }
        }

        /**
         * The implementation of the {@code <remote-server-not-found/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-remote-server-not-found">8.3.3.16.  remote-server-not-found</a></cite></p>
         * <p>A remote server or service specified as part or all of the JID of the intended recipient does not exist or cannot be resolved (e.g., there is no _xmpp-server._tcp DNS SRV record, the A or AAAA fallback resolution fails, or A/AAAA lookups succeed but there is no response on the IANA-registered port 5269); the associated error type SHOULD be "cancel".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "remote-server-not-found", namespace = ERROR_NAMESPACE)
        public static final class RemoteServerNotFound extends Condition {
            static {
                associatedErrorType.put(RemoteServerNotFound.class, Type.CANCEL);
            }

            private RemoteServerNotFound() {
            }

            @Override
            public String toString() {
                return "remote-server-not-found";
            }
        }

        /**
         * The implementation of the {@code <remote-server-timeout/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-remote-server-timeout">8.3.3.17.  remote-server-timeout</a></cite></p>
         * <p>A remote server or service specified as part or all of the JID of the intended recipient (or needed to fulfill a request) was resolved but communications could not be established within a reasonable amount of time (e.g., an XML stream cannot be established at the resolved IP address and port, or an XML stream can be established but stream negotiation fails because of problems with TLS, SASL, Server Dialback, etc.); the associated error type SHOULD be "wait" (unless the error is of a more permanent nature, e.g., the remote server is found but it cannot be authenticated or it violates security policies).</p>
         * </blockquote>
         */
        @XmlRootElement(name = "remote-server-timeout", namespace = ERROR_NAMESPACE)
        public static final class RemoteServerTimeout extends Condition {
            static {
                associatedErrorType.put(RemoteServerTimeout.class, Type.WAIT);
            }

            private RemoteServerTimeout() {
            }

            @Override
            public String toString() {
                return "remote-server-timeout";
            }
        }

        /**
         * The implementation of the {@code <resource-constraint/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-resource-constraint">8.3.3.18.  resource-constraint</a></cite></p>
         * <p>The server or recipient is busy or lacks the system resources necessary to service the request; the associated error type SHOULD be "wait".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "resource-constraint", namespace = ERROR_NAMESPACE)
        public static final class ResourceConstraint extends Condition {
            static {
                associatedErrorType.put(ResourceConstraint.class, Type.WAIT);
            }

            @Override
            public String toString() {
                return "resource-constraint";
            }
        }

        /**
         * The implementation of the {@code <service-unavailable/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-service-unavailable">8.3.3.19.  service-unavailable</a></cite></p>
         * <p>The server or recipient does not currently provide the requested service; the associated error type SHOULD be "cancel".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "service-unavailable", namespace = ERROR_NAMESPACE)
        public static final class ServiceUnavailable extends Condition {
            static {
                associatedErrorType.put(ServiceUnavailable.class, Type.CANCEL);
            }

            @Override
            public String toString() {
                return "service-unavailable";
            }
        }

        /**
         * The implementation of the {@code <subscription-required/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-subscription-required">8.3.3.20.  subscription-required</a></cite></p>
         * <p>The requesting entity is not authorized to access the requested service because a prior subscription is necessary (examples of prior subscription include authorization to receive presence information as defined in [XMPP-IM] and opt-in data feeds for XMPP publish-subscribe as defined in [XEP-0060]); the associated error type SHOULD be "auth".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "subscription-required", namespace = ERROR_NAMESPACE)
        public static final class SubscriptionRequired extends Condition {
            static {
                associatedErrorType.put(SubscriptionRequired.class, Type.AUTH);
            }

            private SubscriptionRequired() {
            }

            @Override
            public String toString() {
                return "subscription-required";
            }
        }

        /**
         * The implementation of the {@code <undefined-condition/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-undefined-condition">8.3.3.21.  undefined-condition</a></cite></p>
         * <p>The error condition is not one of those defined by the other conditions in this list; any error type can be associated with this condition, and it SHOULD NOT be used except in conjunction with an application-specific condition.</p>
         * </blockquote>
         */
        @XmlRootElement(name = "undefined-condition", namespace = ERROR_NAMESPACE)
        public static final class UndefinedCondition extends Condition {
            static {
                associatedErrorType.put(UndefinedCondition.class, Type.CANCEL);
            }

            @Override
            public String toString() {
                return "undefined-condition";
            }
        }

        /**
         * The implementation of the {@code <unexpected-request/>} stanza error.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-unexpected-request">8.3.3.22.  unexpected-request</a></cite></p>
         * <p>The recipient or server understood the request but was not expecting it at this time (e.g., the request was out of order); the associated error type SHOULD be "wait" or "modify".</p>
         * </blockquote>
         */
        @XmlRootElement(name = "unexpected-request", namespace = ERROR_NAMESPACE)
        public static final class UnexpectedRequest extends Condition {
            static {
                associatedErrorType.put(UnexpectedRequest.class, Type.WAIT);
            }

            @Override
            public String toString() {
                return "unexpected-request";
            }
        }
    }
}
