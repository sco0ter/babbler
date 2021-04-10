/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.core.net;

import java.io.Reader;
import java.util.function.Consumer;

import rocks.xmpp.core.stream.model.StreamElement;

/**
 * Intercepts {@link StreamElement}s after reading them from the wire.
 *
 * @author Christian Schudt
 */
public interface ReaderInterceptor {

    /**
     * Processes
     *
     * @param reader                The reader from which the stream is read.
     * @param streamElementListener Listens for stream elements being read from the reader.
     * @param chain                 The reader chain, which allows to proceed to the next interceptor.
     * @throws Exception Any exception happening during interception.
     */
    void process(Reader reader, Consumer<StreamElement> streamElementListener, ReaderInterceptorChain chain)
            throws Exception;
}
