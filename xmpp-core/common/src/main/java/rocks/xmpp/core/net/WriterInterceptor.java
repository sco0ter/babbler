package rocks.xmpp.core.net;

import rocks.xmpp.core.stream.model.StreamElement;

import java.io.Writer;

/**
 * Intercepts {@link StreamElement}s before writing them to the wire.
 *
 * @author Christian Schudt
 */
public interface WriterInterceptor {

    /**
     * The stream element which will be written.
     *
     * @param streamElement The stream element which will be written.
     * @param writer        The writer to which the stream element is written.
     * @param chain         The writer chain, which allows to proceed to the next interceptor.
     * @throws Exception Any exception happening during interception.
     */
    void process(StreamElement streamElement, Writer writer, WriterInterceptorChain chain) throws Exception;
}
