/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.core;

import java.util.ServiceLoader;

/**
 * Defines classes to be bound to the JAXBContext. Implementations of this interface can be found by using {@link
 * ServiceLoader} and by placing a file called {@code rocks.xmpp.core.XmppContext} containing the full qualified class
 * names of the implementations to {@code META-INF/services}.
 *
 * <p>Implementations must have a public no-arg default constructor.</p>
 *
 * @author Christian Schudt
 * @see rocks.xmpp.util.XmppUtils#createContext(Iterable)
 * @see ServiceLoader
 */
public interface XmppContext {

    /**
     * Gets the classes to be bound to the {@link jakarta.xml.bind.JAXBContext}.
     *
     * @return The clases to be bound.
     */
    Iterable<Class<?>> getClasses();
}
