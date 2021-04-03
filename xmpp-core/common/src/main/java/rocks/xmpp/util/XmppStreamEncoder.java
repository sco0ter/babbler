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

package rocks.xmpp.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import rocks.xmpp.core.net.WriterInterceptor;
import rocks.xmpp.core.net.WriterInterceptorChain;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.core.stream.model.errors.Condition;

/**
 * Encodes XMPP elements to binary data.
 * This class is capable to encode elements to either an {@link OutputStream} or to a {@link ByteBuffer}.
 * <p>
 * Encoding is thread-safe, as long as the supplied {@link Marshaller} is not shared by another thread, e.g. if a {@linkplain ThreadLocal thread-local} {@link Marshaller} is supplied.
 *
 * @author Christian Schudt
 */
public final class XmppStreamEncoder implements WriterInterceptor {

    private final XMLOutputFactory outputFactory;

    private final Supplier<Marshaller> marshaller;

    private final Function<StreamElement, Boolean> writeStreamNamespace;

    private String contentNamespace;

    /**
     * Creates the XMPP encoder.
     * <p>
     * Because {@link Marshaller} is not thread-safe, it is recommended to pass a {@code ThreadLocal<Marshaller>} to this constructor, which ensures thread-safety during marshalling.
     *
     * @param outputFactory        The XML output factory.
     * @param marshaller           Supplies the marshaller which will convert objects to XML.
     * @param writeStreamNamespace If the stream namespace should be written in the root element.
     */
    public XmppStreamEncoder(final XMLOutputFactory outputFactory, final Supplier<Marshaller> marshaller, final Function<StreamElement, Boolean> writeStreamNamespace) {
        this.marshaller = marshaller;
        this.outputFactory = outputFactory;
        this.writeStreamNamespace = writeStreamNamespace;
    }

    /**
     * Encodes an XMPP element to a byte buffer.
     * The returned {@link ByteBuffer} is ready to be read, i.e. its {@linkplain ByteBuffer#position() position} is 0.
     *
     * @param streamElement The stream element.
     * @return The byte buffer.
     * @throws StreamErrorException If the element could not be marshalled.
     */
    public final ByteBuffer encode(StreamElement streamElement) throws StreamErrorException {
        try (ByteBufferOutputStream outputStream = new ByteBufferOutputStream(512, false);
             Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            encode(streamElement, writer);
            return outputStream.getBuffer().flip();
        } catch (IOException e) {
            throw new StreamErrorException(new StreamError(Condition.INTERNAL_SERVER_ERROR), e);
        }
    }

    /**
     * Encodes an XMPP element to a {@link Writer}.
     *
     * @param streamElement The stream element.
     * @param writer        The writer to write to.
     * @throws StreamErrorException If the element could not be marshalled.
     */
    public final void encode(StreamElement streamElement, final Writer writer) throws StreamErrorException {
        try {
            XMLStreamWriter streamWriter = null;
            try {
                if (streamElement instanceof StreamHeader) {
                    try (Writer out = new StringWriter()) {
                        contentNamespace = ((StreamHeader) streamElement).getContentNamespace();
                        streamWriter = outputFactory.createXMLStreamWriter(out);
                        ((StreamHeader) streamElement).writeTo(streamWriter);
                        writer.write(out.toString());
                    }
                    return;
                } else if (streamElement == StreamHeader.CLOSING_STREAM_TAG) {
                    writer.write(StreamHeader.CLOSING_STREAM_TAG.toString());
                    writer.flush();
                    return;
                }

                streamWriter = XmppUtils.createXmppStreamWriter(outputFactory.createXMLStreamWriter(writer), writeStreamNamespace.apply(streamElement));
                streamWriter.setDefaultNamespace(contentNamespace != null ? contentNamespace : XMLConstants.DEFAULT_NS_PREFIX);
                final Marshaller m = marshaller.get();
                m.setProperty(Marshaller.JAXB_FRAGMENT, true);
                m.marshal(streamElement, streamWriter);
                streamWriter.flush();
            } finally {
                if (streamWriter != null) {
                    streamWriter.close();
                }
            }
        } catch (XMLStreamException | JAXBException | IOException e) {
            throw new StreamErrorException(new StreamError(Condition.INTERNAL_SERVER_ERROR), e);
        }
    }

    @Override
    public void process(StreamElement streamElement, Writer writer, WriterInterceptorChain chain) throws Exception {
        encode(streamElement, writer);
        chain.proceed(streamElement, writer);
    }
}