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
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import javax.xml.XMLConstants;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;

import rocks.xmpp.core.net.ReaderInterceptor;
import rocks.xmpp.core.net.ReaderInterceptorChain;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.util.XmppStreamDecoder;

/**
 * Decodes WebSocket text messages to XMPP {@link StreamElement}s.
 *
 * <p>The required {@link Unmarshaller} for decoding must be supplied via {@link EndpointConfig#getUserProperties()}, see {@link UserProperties#UNMARSHALLER}.</p>
 *
 * <p>Optionally you can also provide a callback, which is called after unmarshalling with the XML string (the text message) and the unmarshalled element.
 * This is useful for debugging purposes, see {@link UserProperties#ON_READ}.</p>
 *
 * @author Christian Schudt
 * @see XmppWebSocketEncoder
 * @see UserProperties
 */
public final class XmppWebSocketDecoder implements Decoder.TextStream<StreamElement> {

    private Iterable<ReaderInterceptor> interceptors;

    @Override
    public final StreamElement decode(final Reader reader) throws DecodeException, IOException {
        try {
            ReaderInterceptorChain readerInterceptorChain = new ReaderInterceptorChain(interceptors);
            List<StreamElement> out = new ArrayList<>();
            readerInterceptorChain.proceed(reader, out::add);
            if (!out.isEmpty()) {
                return out.get(0);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new DecodeException(reader.toString(), e.getMessage(), e);
        }
        throw new IOException("Could not decode an element from the reader");
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void init(final EndpointConfig config) {
        XMLInputFactory xmlInputFactory = (XMLInputFactory) config.getUserProperties().get(XmppWebSocketDecoder.UserProperties.XML_INPUT_FACTORY);
        if (xmlInputFactory == null) {
            xmlInputFactory = XMLInputFactory.newFactory();
        }
        Supplier<Unmarshaller> unmarshaller = (Supplier<Unmarshaller>) config.getUserProperties().get(UserProperties.UNMARSHALLER);
        List<ReaderInterceptor> readerInterceptors = new ArrayList<>();
        Iterable<ReaderInterceptor> additionalInterceptors = (Iterable<ReaderInterceptor>) config.getUserProperties().get(UserProperties.ON_READ);
        if (additionalInterceptors != null) {
            additionalInterceptors.forEach(readerInterceptors::add);
        }
        readerInterceptors.add(new XmppStreamDecoder(xmlInputFactory, unmarshaller, XMLConstants.NULL_NS_URI));
        this.interceptors = readerInterceptors;
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
         * The property key to set the unmarshaller. The value must be a {@code java.util.function.Supplier<Unmarshaller>}.
         */
        public static final String UNMARSHALLER = "unmarshaller";

        /**
         * The property key to provide an optional {@link XMLInputFactory}.
         */
        public static final String XML_INPUT_FACTORY = "xmlInputFactory";

        /**
         * The property to set the read callback. The value must be a {@code java.util.function.BiConsumer<String, StreamElement>}.
         */
        public static final String ON_READ = "onRead";

        private UserProperties() {
        }
    }
}
