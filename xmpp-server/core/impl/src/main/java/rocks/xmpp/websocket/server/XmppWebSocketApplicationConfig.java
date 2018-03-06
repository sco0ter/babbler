/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.websocket.server;

import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.websocket.codec.XmppWebSocketDecoder;
import rocks.xmpp.websocket.codec.XmppWebSocketEncoder;

import javax.enterprise.inject.spi.CDI;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The configuration for the a WebSocket server application.
 * It creates one endpoint only which speaks the "xmpp" sub-protocol.
 *
 * @author Christian Schudt
 */
public final class XmppWebSocketApplicationConfig implements ServerApplicationConfig {

    @Override
    public final Set<ServerEndpointConfig> getEndpointConfigs(final Set<Class<? extends Endpoint>> endpointClasses) {
        final ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(XmppWebSocketEndpoint.class, "/ws")
                .decoders(Collections.singletonList(XmppWebSocketDecoder.class))
                .encoders(Collections.singletonList(XmppWebSocketEncoder.class))
                .subprotocols(Collections.singletonList("xmpp"))
                .configurator(new XmppWebSocketConfigurator())
                .build();
        // CDI is not possible in this class, but it is when the Decoder/Encoder instance is initialized.
        serverEndpointConfig.getUserProperties().put(XmppWebSocketDecoder.UserProperties.UNMARSHALLER, (Supplier<Unmarshaller>) () -> CDI.current().select(ServerConfiguration.class).get().getUnmarshaller());
        serverEndpointConfig.getUserProperties().put(XmppWebSocketEncoder.UserProperties.MARSHALLER, (Supplier<Marshaller>) () -> CDI.current().select(ServerConfiguration.class).get().getMarshaller());
        return Collections.singleton(serverEndpointConfig);
    }

    @Override
    public final Set<Class<?>> getAnnotatedEndpointClasses(final Set<Class<?>> scanned) {
        return scanned;
    }
}
