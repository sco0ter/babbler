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
import java.util.Iterator;
import java.util.function.Consumer;

import rocks.xmpp.core.Session;
import rocks.xmpp.core.stream.model.StreamElement;

/**
 * An interceptor chain, which manages the sequential processing of multiple interceptors.
 *
 * @author Christian Schudt
 * @see ReaderInterceptor
 * @see WriterInterceptorChain
 */
public final class ReaderInterceptorChain extends AbstractInterceptorChain {

    private final Iterator<ReaderInterceptor> iterator;

    public ReaderInterceptorChain(Iterable<ReaderInterceptor> readerInterceptors, Session session,
                                  Connection connection) {
        super(session, connection);
        iterator = readerInterceptors.iterator();
    }

    /**
     * Proceeds to the next interceptor if present.
     *
     * @param reader        The reader.
     * @param streamElement Consumes the read stream element.
     * @throws Exception Any exception happening during interception.
     */
    public void proceed(Reader reader, Consumer<StreamElement> streamElement) throws Exception {
        if (iterator.hasNext()) {
            ReaderInterceptor readerInterceptor = iterator.next();
            readerInterceptor.process(reader, streamElement, this);
        }
    }
}
