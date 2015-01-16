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

package rocks.xmpp.core.stream.model;

import rocks.xmpp.core.stream.model.errors.Condition;
import rocks.xmpp.core.stream.model.errors.Text;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <stream:error/>} element.
 * <p>
 * See <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error">4.9.  Stream Errors</a>
 * </p>
 */
@XmlRootElement(name = "error")
public final class StreamError implements ServerStreamElement {

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
    private StreamError() {
    }

    public StreamError(Condition condition) {
        this.condition = condition;
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
     * Gets the application specific condition, if any.
     *
     * @return The application specific condition.
     * @see <a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-app">4.9.4.  Application-Specific Conditions</a>
     */
    public final Object getExtension() {
        return extension;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCondition().toString());

        if (getText() != null) {
            sb.append("\n        ");
            sb.append(getText());
        }

        if (getExtension() != null) {
            sb.append("\n        ");
            sb.append(getExtension().toString());
        }
        return sb.toString();
    }
}