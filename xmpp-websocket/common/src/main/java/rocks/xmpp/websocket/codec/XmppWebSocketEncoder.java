/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.websocket.codec;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;

import rocks.xmpp.core.net.WriterInterceptor;
import rocks.xmpp.core.net.WriterInterceptorChain;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.util.XmppStreamEncoder;

/**
 * Encodes XMPP {@link StreamElement}s to WebSocket text messages.
 * <p>
 * The required {@link Marshaller} for encoding must be supplied via {@link EndpointConfig#getUserProperties()}, see {@link XmppWebSocketEncoder.UserProperties#MARSHALLER}.
 * <p>
 * Optionally you can also provide a callback, which is called after marshalling with the encoded text message (the XML string) and the stream element.
 * This is useful for debugging purposes, see {@link UserProperties#ON_WRITE}.
 *
 * @author Christian Schudt
 * @see XmppWebSocketDecoder
 * @see UserProperties
 */
public final class XmppWebSocketEncoder implements Encoder.TextStream<StreamElement> {

    private Iterable<WriterInterceptor> interceptors;

    @Override
    public final void encode(final StreamElement object, final Writer writer) throws EncodeException, IOException {
        try {
            WriterInterceptorChain writerInterceptorChain = new WriterInterceptorChain(interceptors);
            writerInterceptorChain.proceed(object, writer);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new EncodeException(object, e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void init(final EndpointConfig config) {
        XMLOutputFactory xmlOutputFactory = (XMLOutputFactory) config.getUserProperties().get(UserProperties.XML_OUTPUT_FACTORY);
        if (xmlOutputFactory == null) {
            xmlOutputFactory = XMLOutputFactory.newFactory();
        }
        Supplier<Marshaller> marshaller = (Supplier<Marshaller>) config.getUserProperties().get(UserProperties.MARSHALLER);
        List<WriterInterceptor> writerInterceptors = new ArrayList<>();
        Iterable<WriterInterceptor> additionalInterceptors = (Iterable<WriterInterceptor>) config.getUserProperties().get(UserProperties.ON_WRITE);
        if (additionalInterceptors != null) {
            additionalInterceptors.forEach(writerInterceptors::add);
        }
        writerInterceptors.add(new XmppStreamEncoder(xmlOutputFactory, marshaller, streamElement -> streamElement instanceof StreamFeatures || streamElement instanceof StreamError));
        this.interceptors = writerInterceptors;
    }

    @Override
    public final void destroy() {
        this.interceptors = null;
    }

    /**
     * User properties for usage in {@link EndpointConfig#getUserProperties()}.
     */
    public static final class UserProperties {
        /**
         * The property key to provide the required {@link Marshaller}. The value must be a {@code java.util.function.Supplier<Marshaller>}.
         */
        public static final String MARSHALLER = "marshaller";

        /**
         * The property key to provide an optional {@link XMLOutputFactory}.
         */
        public static final String XML_OUTPUT_FACTORY = "xmlOutputFactory";

        /**
         * The property to set an optional write callback. The value must be a {@code java.util.function.BiConsumer<String, StreamElement>}.
         */
        public static final String ON_WRITE = "onWrite";

        private UserProperties() {
        }
    }
}
