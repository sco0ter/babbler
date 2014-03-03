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
import org.xmpp.stanza.errors.*;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of a stanza's {@code <error/>} element.
 * <p>
 * See <a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-syntax">8.3.2.  Syntax</a>
 * </p>
 */
@XmlRootElement(name = "error")
@XmlSeeAlso({Text.class, BadRequest.class, Conflict.class, FeatureNotImplemented.class, Conflict.class, Forbidden.class, Gone.class, InternalServerError.class, ItemNotFound.class, JidMalformed.class, NotAcceptable.class, NotAllowed.class, NotAuthorized.class, PolicyViolation.class, RecipientUnavailable.class, Redirect.class, RegistrationRequired.class, RemoteServerNotFound.class, RemoteServerTimeout.class, ResourceConstraint.class, ServiceUnavailable.class, SubscriptionRequired.class, UndefinedCondition.class, UnexpectedRequest.class})
public final class StanzaError {

    private static final Map<Class<? extends Condition>, Type> ASSOCIATED_ERROR_TYPE = new HashMap<>();

    static {
        ASSOCIATED_ERROR_TYPE.put(BadRequest.class, StanzaError.Type.MODIFY);
        ASSOCIATED_ERROR_TYPE.put(Conflict.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(FeatureNotImplemented.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(Forbidden.class, StanzaError.Type.AUTH);
        ASSOCIATED_ERROR_TYPE.put(Gone.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(InternalServerError.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(ItemNotFound.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(JidMalformed.class, StanzaError.Type.MODIFY);
        ASSOCIATED_ERROR_TYPE.put(NotAcceptable.class, StanzaError.Type.MODIFY);
        ASSOCIATED_ERROR_TYPE.put(NotAllowed.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(NotAuthorized.class, StanzaError.Type.AUTH);
        ASSOCIATED_ERROR_TYPE.put(PolicyViolation.class, StanzaError.Type.MODIFY);
        ASSOCIATED_ERROR_TYPE.put(RecipientUnavailable.class, StanzaError.Type.WAIT);
        ASSOCIATED_ERROR_TYPE.put(Redirect.class, StanzaError.Type.MODIFY);
        ASSOCIATED_ERROR_TYPE.put(RegistrationRequired.class, StanzaError.Type.AUTH);
        ASSOCIATED_ERROR_TYPE.put(RemoteServerNotFound.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(RemoteServerTimeout.class, StanzaError.Type.WAIT);
        ASSOCIATED_ERROR_TYPE.put(ResourceConstraint.class, StanzaError.Type.WAIT);
        ASSOCIATED_ERROR_TYPE.put(ServiceUnavailable.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(SubscriptionRequired.class, StanzaError.Type.AUTH);
        ASSOCIATED_ERROR_TYPE.put(UndefinedCondition.class, StanzaError.Type.CANCEL);
        ASSOCIATED_ERROR_TYPE.put(UnexpectedRequest.class, StanzaError.Type.WAIT);
    }

    @XmlAttribute(name = "by")
    private Jid by;

    @XmlAttribute(name = "type")
    private Type type;

    @XmlElementRef
    private Condition condition;

    @XmlElementRef
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
    private StanzaError() {
        this.condition = new UndefinedCondition();
    }

    /**
     * Creates an error with a given error type and a condition.
     * <blockquote><p>The {@code <error/>} element MUST contain a defined condition element.</p></blockquote>
     *
     * @param type      The error type.
     * @param condition The condition.
     */
    public StanzaError(Type type, Condition condition) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null.");
        }
        this.type = type;
        this.condition = condition;
    }

    /**
     * Creates an error with a given error type and a condition.
     * <blockquote><p>The {@code <error/>} element MUST contain a defined condition element.</p></blockquote>
     *
     * @param type      The error type.
     * @param condition The condition.
     * @param text      The text.
     */
    public StanzaError(Type type, Condition condition, String text) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null.");
        }
        this.type = type;
        this.condition = condition;
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
    public StanzaError(Condition condition) {
        this.condition = condition;
        this.type = ASSOCIATED_ERROR_TYPE.get(condition.getClass());
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
    public StanzaError(Condition condition, String text) {
        this(condition);
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
    public Jid getBy() {
        return by;
    }

    /**
     * Sets the 'by' attribute.
     *
     * @param by The JID.
     * @see #getBy()
     */
    public void setBy(Jid by) {
        this.by = by;
    }

    /**
     * Gets the error type, either 'auth', 'cancel', 'continue', 'modify' or 'wait'.
     *
     * @return The type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the optional error text.
     *
     * @return The text.
     * @see #setText(String)
     */
    public String getText() {
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
    public void setText(String text) {
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
    public void setText(String text, String language) {
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
    public String getLanguage() {
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
    public Object getExtension() {
        return extension;
    }

    /**
     * Sets an application specific condition.
     *
     * @param extension The application specific condition.
     * @see #getExtension()
     */
    public void setExtension(Object extension) {
        this.extension = extension;
    }

    /**
     * Gets the defined error condition.
     *
     * @return The error condition.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions">8.3.3.  Defined Conditions</a>
     */
    public Condition getCondition() {
        return condition;
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
}