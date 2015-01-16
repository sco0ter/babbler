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

package rocks.xmpp.core.stanza.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stanza.model.errors.Text;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Objects;

/**
 * The implementation of a stanza's {@code <error/>} element.
 * <p>
 * See <a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-syntax">8.3.2.  Syntax</a>
 * </p>
 */
public final class StanzaError {

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
        this(null, Condition.UNDEFINED_CONDITION, null, null, null, null);
    }

    /**
     * Creates an error with a given error type and a condition.
     * <blockquote><p>The {@code <error/>} element MUST contain a defined condition element.</p></blockquote>
     *
     * @param type      The error type.
     * @param condition The condition.
     */
    public StanzaError(Type type, Condition condition) {
        this(type, condition, null);
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
        this(type, condition, text, null, null, null);
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
        this(condition, null);
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
        this(null, condition, text, null, null, null);
    }

    /**
     * Creates an error with a given condition and extension.
     * <p>
     * The error type is set by the condition's associated error type.
     * </p>
     *
     * @param condition The condition.
     * @param extension The extension.
     */
    public StanzaError(Condition condition, Object extension) {
        this(null, condition, null, null, extension, null);
    }

    /**
     * Creates a stanza error with all possible values.
     *
     * @param type      The error type.
     * @param condition The condition.
     * @param text      The text.
     * @param language  The language.
     * @param extension The application specific condition.
     * @param by        The entity which returns the error.
     */
    public StanzaError(Type type, Condition condition, String text, String language, Object extension, Jid by) {
        Objects.requireNonNull(condition);
        if (type == null) {
            this.type = Condition.getErrorTypeByCondition(condition);
        } else {
            this.type = type;
        }
        this.condition = condition;
        if (text != null) {
            this.text = new Text(text, language);
        }
        this.extension = extension;
        this.by = by;
    }

    /**
     * Gets the 'by' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-rules">8.3.1.  Rules</a></cite></p>
     * The entity that returns an error stanza MAY pass along its JID to the sender of the generated stanza (e.g., for diagnostic or tracking purposes) through the addition of a 'by' attribute to the {@code <error/>} child element.
     * </blockquote>
     *
     * @return The JID.
     */
    public Jid getBy() {
        return by;
    }

    /**
     * Sets the 'by' attribute.
     *
     * @param by The JID.
     * @see #getBy()
     * @deprecated Use constructor.
     */
    @Deprecated
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
     * @see #getText()
     * @deprecated Use constructor.
     */
    @Deprecated
    public void setText(String text) {
        setText(text, null);
    }

    /**
     * Sets the optional error text and a language.
     *
     * @param text     The text.
     * @param language The language.
     * @see #getText()
     * @deprecated Use constructor.
     */
    @Deprecated
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
     */
    public Object getExtension() {
        return extension;
    }

    /**
     * Sets an application specific condition.
     *
     * @param extension The application specific condition.
     * @see #getExtension()
     * @deprecated Use constructor.
     */
    @Deprecated
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(condition);
        sb.append("  -  (");
        sb.append(type);
        sb.append(")");

        if (text != null) {
            sb.append("\n        ");
            sb.append(text.getText());
        }

        if (extension != null) {
            sb.append("\n        ");
            sb.append(extension);
        }
        return sb.toString();
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
        AUTH("retry after providing credentials"),
        /**
         * Do not retry (the error cannot be remedied).
         */
        @XmlEnumValue(value = "cancel")
        CANCEL("do not retry (the error cannot be remedied)"),
        /**
         * Proceed (the condition was only a warning).
         */
        @XmlEnumValue(value = "continue")
        CONTINUE("proceed (the condition was only a warning)"),
        /**
         * Retry after changing the data sent.
         */
        @XmlEnumValue(value = "modify")
        MODIFY("retry after changing the data sent"),
        /**
         * Retry after waiting (the error is temporary).
         */
        @XmlEnumValue(value = "wait")
        WAIT("retry after waiting (the error is temporary)");

        private final String errorText;

        private Type(String errorText) {
            this.errorText = errorText;
        }

        @Override
        public String toString() {
            return "type '" + name().toLowerCase() + "': " + errorText;
        }
    }
}