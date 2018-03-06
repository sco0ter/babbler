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

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;

/**
 * The server endpoint configurator for the XMPP WebSocket sub-protocol.
 * <p>
 * This configurator only allows WebSocket connections if the client included "xmpp" in the "Sec-WebSocket-Protocol" header field.
 * Otherwises it won't accept the connection.
 *
 * @author Christian Schudt
 */
public class XmppWebSocketConfigurator extends ServerEndpointConfig.Configurator {

    private String negotiatedSubprotocol;

    public XmppWebSocketConfigurator() {
        int i = 0;
    }

    @Override
    public final void modifyHandshake(final ServerEndpointConfig sec, final HandshakeRequest request, final HandshakeResponse response) {
        super.modifyHandshake(sec, request, response);
        // Don't accept any non-XMPP connections.
        if (!"xmpp".equals(negotiatedSubprotocol)) {
            // The |Sec-WebSocket-Accept| header field indicates whether the server is willing to accept the connection.
            // If present, this header field must include a hash of the client's nonce sent in |Sec-WebSocket-Key|
            // along with a predefined GUID. Any other value must not be interpreted as an acceptance of the connection by the server.
            response.getHeaders().remove(HandshakeResponse.SEC_WEBSOCKET_ACCEPT);
        }
    }

    @Override
    public final String getNegotiatedSubprotocol(final List<String> supported, final List<String> requested) {
        return negotiatedSubprotocol = super.getNegotiatedSubprotocol(supported, requested);
    }
}
