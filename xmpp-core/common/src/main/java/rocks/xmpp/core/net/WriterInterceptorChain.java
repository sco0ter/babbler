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

import rocks.xmpp.core.stream.model.StreamElement;

import java.io.Writer;
import java.util.Iterator;

/**
 * An interceptor chain, which manages the sequential processing of multiple interceptors.
 *
 * @author Christian Schudt
 * @see WriterInterceptor
 */
public final class WriterInterceptorChain {

    private final Iterator<WriterInterceptor> iterator;

    public WriterInterceptorChain(Iterable<WriterInterceptor> writerInterceptors) {
        iterator = writerInterceptors.iterator();
    }

    /**
     * Proceeds to the next interceptor if present.
     *
     * @param streamElement The stream element.
     * @param writer        The writer.
     * @throws Exception Any exception happening during interception.
     */
    public void proceed(StreamElement streamElement, Writer writer) throws Exception {
        if (iterator.hasNext()) {
            WriterInterceptor writerInterceptor = iterator.next();
            writerInterceptor.process(streamElement, writer, this);
        }
    }
}
