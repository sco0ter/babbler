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

package org.xmpp.extension.compress;

import org.xmpp.stream.ServerStreamElement;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * The implementation of the {@code <failure/>} element in the {@code http://jabber.org/protocol/compress} namespace, which indicates failure during compression negotiation.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0138.html">XEP-0138: Stream Compression</a>
 * @see <a href="http://xmpp.org/extensions/xep-0138.html#schemas-protocol">XML Schema</a>
 */
@XmlRootElement(name = "failure")
@XmlSeeAlso({Failure.SetupFailed.class, Failure.ProcessingFailed.class, Failure.UnsupportedMethod.class})
public final class Failure implements ServerStreamElement {
    @XmlElementRef
    private Condition condition;

    private Failure() {
    }

    Condition getCondition() {
        return condition;
    }

    static abstract class Condition {

        private String name;

        private Condition() {
        }

        protected Condition(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @XmlRootElement(name = "setup-failed")
    static final class SetupFailed extends Condition {
        private SetupFailed() {
            super("setup-failed");
        }
    }

    @XmlRootElement(name = "processing-failed")
    static final class ProcessingFailed extends Condition {
        private ProcessingFailed() {
            super("processing-failed");
        }
    }

    @XmlRootElement(name = "unsupported-method")
    static final class UnsupportedMethod extends Condition {
        private UnsupportedMethod() {
            super("unsupported-method");
        }
    }
}
