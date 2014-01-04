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

package org.xmpp;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlValue;

/**
 * A base class for {@linkplain org.xmpp.stream.StreamError stream errors} and {@linkplain org.xmpp.stanza.Stanza.Error stanza errors}.
 * <p>Both errors have a text, a language and a defined error condition in common.</p>
 *
 * @author Christian Schudt
 */
public abstract class XmppError {

    @XmlElementRef
    private Condition condition;

    protected XmppError() {
    }

    /**
     * Creates an XMPP error with a specified error condition.
     *
     * @param condition The error condition.
     */
    protected XmppError(Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("condition must not be null.");
        }
        this.condition = condition;
    }

    /**
     * Gets the defined error condition.
     * <p>
     * See also <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions">4.9.3.  Defined Stream Error Conditions</a>
     * </p>
     *
     * @return The error condition.
     */
    public final Condition getCondition() {
        return condition;
    }

    /**
     * Gets the language of the error text.
     *
     * @return The language.
     */
    public abstract String getLanguage();

    /**
     * Gets the optional error text.
     *
     * @return The text.
     */
    public abstract String getText();

    /**
     * Sets the optional error text.
     *
     * @param text The text.
     * @see #setText(String, String)
     * @see #getText()
     */
    public abstract void setText(String text);

    /**
     * Sets the optional error text and a language.
     *
     * @param text     The text.
     * @param language The language.
     * @see #setText(String)
     * @see #getText()
     */
    public abstract void setText(String text, String language);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getCondition() != null) {
            sb.append(getCondition().toString());
        }

        if (getText() != null) {
            sb.append(": ");
            sb.append(getText());
        }
        return sb.toString();
    }

    /**
     * An abstract implementation of a defined error condition.
     * <p>
     * See <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions">4.9.3.  Defined Stream Error Conditions</a>
     * and <a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions">8.3.3.  Defined Conditions</a>
     * </p>
     */
    public static abstract class Condition {

        @XmlValue
        protected String value;

        protected Condition() {
        }
    }

    /**
     * Wrapper class to represent the text element.
     */
    protected static final class Text {
        @XmlValue
        private String text;

        @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
        private String language;

        /**
         * Default constructor for unmarshalling.
         */
        @SuppressWarnings("unused")
        private Text() {
        }

        public Text(String text, String language) {
            this.text = text;
            this.language = language;
        }

        public String getText() {
            return text;
        }

        public String getLanguage() {
            return language;
        }
    }
}
