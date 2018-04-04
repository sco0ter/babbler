/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.core.stream.model.errors;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The implementation of the {@code <system-shutdown/>} stream error.<blockquote>
 * <p><cite><a href="https://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-system-shutdown">4.9.3.20.  system-shutdown</a></cite></p>
 * <p>The server is being shut down and all active streams are being statusChanged.</p>
 * </blockquote>
 * This class is a singleton.
 *
 * @see #SYSTEM_SHUTDOWN
 */
@XmlRootElement(name = "system-shutdown")
@XmlType(factoryMethod = "create")
final class SystemShutdown extends Condition {

    SystemShutdown() {
    }

    private static SystemShutdown create() {
        return (SystemShutdown) SYSTEM_SHUTDOWN;
    }
}