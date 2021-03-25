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
