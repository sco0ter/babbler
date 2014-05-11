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

import org.xmpp.XmppException;
import org.xmpp.stream.errors.*;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * The implementation of the {@code <stream:error/>} element.
 * <p>
 * See <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error">4.9.  Stream Errors</a>
 * </p>
 */
@XmlRootElement(name = "error")
@XmlSeeAlso({BadFormat.class, BadNamespacePrefix.class, Conflict.class, ConnectionTimeout.class, HostGone.class, HostUnknown.class, ImproperAddressing.class, InternalServerError.class, InvalidFrom.class, InvalidNamespace.class, InvalidXml.class, NotAuthorized.class, NotWellFormed.class, PolicyViolation.class, RemoteConnectionFailed.class, Reset.class, ResourceConstraint.class, RestrictedXml.class, SeeOtherHost.class, SystemShutdown.class, UndefinedCondition.class, UnsupportedEncoding.class, UnsupportedFeature.class, UnsupportedStanzaType.class, UnsupportedVersion.class})
public final class StreamException extends XmppException implements ServerStreamElement {

    @XmlElementRef
    private Condition condition;

    @XmlElementRef
    private Text text;

    @XmlAnyElement(lax = true)
    private Object extension;

    /**
     * Private default constructor for unmarshalling.
     */
    @SuppressWarnings("unused")
    private StreamException() {
    }

    public StreamException(Condition condition) {
        this.condition = condition;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(condition.toString());

        if (text != null) {
            sb.append("\n        ");
            sb.append(text.getText());
        }

        if (extension != null) {
            sb.append("\n        ");
            sb.append(extension.toString());
        }
        return sb.toString();
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
    public String getLanguage() {
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
    public final void setText(String text, String language) {
        if (text != null) {
            this.text = new Text(text, language);
        } else {
            this.text = null;
        }
    }

    /**
     * Gets the application specific condition, if any.
     *
     * @return The application specific condition.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-app">4.9.4.  Application-Specific Conditions</a>
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
     * Gets the defined stream error condition.
     *
     * @return The error condition.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions">4.9.3.  Defined Stream Error Conditions</a>
     */
    public Condition getCondition() {
        return condition;
    }
}