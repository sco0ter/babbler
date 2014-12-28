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

package rocks.xmpp.extensions.compress.model;

import rocks.xmpp.core.stream.model.ServerStreamElement;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <failure/>} element in the {@code http://jabber.org/protocol/compress} namespace, which indicates failure during compression negotiation.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0138.html">XEP-0138: Stream Compression</a>
 * @see <a href="http://xmpp.org/extensions/xep-0138.html#schemas-protocol">XML Schema</a>
 */
@XmlRootElement(name = "failure")
public final class Failure implements ServerStreamElement {
    @XmlElements({@XmlElement(name = "setup-failed", type = SetupFailed.class),
            @XmlElement(name = "processing-failed", type = ProcessingFailed.class),
            @XmlElement(name = "unsupported-method", type = UnsupportedMethod.class)})
    private Condition condition;

    private Failure() {
    }

    /**
     * Gets the failure condition.
     *
     * @return The condition.
     * @see UnsupportedMethod
     * @see SetupFailed
     * @see ProcessingFailed
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * An abstract base class for a compression failure condition.
     */
    public abstract static class Condition {

        private String name;

        private Condition(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * If the receiving entity finds the requested method unacceptable or unworkable for any other reason.
     */
    public static final class SetupFailed extends Condition {
        public SetupFailed() {
            super("setup-failed");
        }
    }

    /**
     * If compression processing fails after the new (compressed) stream has been established.
     */
    public static final class ProcessingFailed extends Condition {
        public ProcessingFailed() {
            super("processing-failed");
        }
    }

    /**
     * If the initiating entity requests a stream compression method that is not supported by the receiving entity.
     */
    public static final class UnsupportedMethod extends Condition {
        public UnsupportedMethod() {
            super("unsupported-method");
        }
    }
}
